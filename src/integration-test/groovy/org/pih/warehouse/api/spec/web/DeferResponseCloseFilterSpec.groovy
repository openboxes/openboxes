package org.pih.warehouse.api.spec.web

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.hibernate.SessionFactory
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.event.service.spi.EventListenerRegistry
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.hibernate.event.spi.EventType
import org.hibernate.event.spi.PostDeleteEvent
import org.hibernate.event.spi.PostDeleteEventListener
import org.hibernate.event.spi.PostInsertEvent
import org.hibernate.event.spi.PostInsertEventListener
import org.hibernate.persister.entity.EntityPersister
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager

import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.product.Category

/**
 * Verifies that a response rendered inside a transactional controller action is not completed for the client
 * before the transaction has committed.
 *
 * Step by step:
 *  1. Register a Hibernate post-insert listener on the session factory. It ignores everything except a Category
 *     with the name the test armed it with.
 *  2. Post that category through the API from a second thread, so that this thread stays free to observe.
 *  3. When the INSERT is flushed - which happens inside the commit, after the controller has already rendered its
 *     response - the listener fires, counts down the "inserted" latch and then waits on the "released" latch.
 *     That holds the transaction open with the response already rendered: the exact window the flakiness lives in.
 *  4. While it is held, assert that the HTTP client is still waiting (its Future times out). Without
 *     DeferResponseCloseFilter this assertion fails, because the client receives the complete response as soon as
 *     the JSON converter closes the response writer, which is before the INSERT is even flushed.
 *  5. Release the listener, then assert that the client completes and can read the category back.
 *
 * The delete features cover a response that has no body at all: CategoryApiController.delete flushes the DELETE
 * inside the action and then calls render(status: 204), which flushes the response buffer. A 204 has no body, so
 * once its headers are out the client has the complete response. The delete listener therefore cannot hold the
 * commit from the post-delete event itself (that fires before the render); instead it registers a transaction
 * synchronization whose beforeCommit runs after the action has returned, and either holds the commit there or
 * makes it fail.
 */
class DeferResponseCloseFilterSpec extends ApiSpec {

    static final String CATEGORY_NAME_PREFIX = 'Test Category (defer response close)'

    // Hibernate 5.2 has no removeListener, and clearing the listener group would also drop its duplication
    // strategies, so the listener is registered once per session factory and left in place. It is inert unless
    // armed, so it does not affect other specs.
    static BlockingCategoryInsertListener listener
    static BlockingCategoryDeleteListener deleteListener
    static SessionFactory listenerSessionFactory

    @Autowired
    SessionFactory sessionFactory

    ExecutorService executor

    @Override
    void setupData() {
        if (listener == null || listenerSessionFactory != sessionFactory) {
            EventListenerRegistry registry = ((SessionFactoryImplementor) sessionFactory)
                    .getServiceRegistry()
                    .getService(EventListenerRegistry)
            listener = new BlockingCategoryInsertListener()
            registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(listener)
            deleteListener = new BlockingCategoryDeleteListener()
            registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(deleteListener)
            listenerSessionFactory = sessionFactory
        }

        // A daemon thread so that a request that never returns cannot keep the test JVM alive.
        executor = Executors.newSingleThreadExecutor({ Runnable runnable ->
            Thread thread = new Thread(runnable, 'defer-response-close-client')
            thread.daemon = true
            return thread
        } as ThreadFactory)
    }

    @Override
    void cleanupData() {
        try {
            listener?.disarm()
            deleteListener?.disarm()
        } finally {
            executor?.shutdownNow()
            executor?.awaitTermination(10, TimeUnit.SECONDS)
        }
    }

    void 'response for a transactional save is not completed until the transaction commits'() {
        given: 'a category name that the listener will recognize'
        String categoryName = "${CATEGORY_NAME_PREFIX} ${randomUtil.randomStringFieldValue('name')}"
        listener.arm(categoryName)

        when: 'the category is saved through the API on another thread'
        Future<Category> savedCategory = executor.submit({
            categoryApiWrapper.createOK(new CategoryTestBuilder().name(categoryName).rootCategory().build())
        } as java.util.concurrent.Callable<Category>)

        then: 'the INSERT is flushed and the commit is now blocked by the listener'
        assert listener.inserted.await(10, TimeUnit.SECONDS)

        and: 'the client is still waiting for the response (a generous bound, so that a slow client thread cannot mask a completed response)'
        assert !completesWithin(savedCategory, 1000)

        when: 'the commit is allowed to proceed'
        listener.release()
        Category category = savedCategory.get(10, TimeUnit.SECONDS)

        then: 'the client gets the response and the category is visible to a follow-up request'
        assert category.id
        assert categoryApiWrapper.api.get(category.id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getString('name') == categoryName
    }

    void 'response for a transactional delete rendered as a bare status is not completed until the transaction commits'() {
        given: 'a category that the delete listener will recognize'
        Category category = createCategoryForDelete()
        deleteListener.arm(category.name, BlockingCategoryDeleteListener.Mode.HOLD)

        when: 'the category is deleted through the API on another thread (the action ends with render status: 204)'
        Future<Response> deleted = executor.submit({
            categoryApiWrapper.api.delete(category.id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)
        } as java.util.concurrent.Callable<Response>)

        then: 'the action has returned and the commit is now blocked by the listener'
        assert deleteListener.committing.await(10, TimeUnit.SECONDS)

        and: 'the client is still waiting for the 204, although the action rendered it before the commit'
        assert !completesWithin(deleted, 1000)

        when: 'the commit is allowed to proceed'
        deleteListener.release()
        Response response = deleted.get(10, TimeUnit.SECONDS)

        then: 'the client gets the 204 and the category is gone for a follow-up request'
        assert response.statusCode == 204
        assert categoryApiWrapper.api.get(category.id, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC).statusCode == 404
    }

    void 'a commit that fails after a bare-status render reaches the client as an error, not as the status'() {
        given: 'a category whose delete will fail at commit'
        Category category = createCategoryForDelete()
        deleteListener.arm(category.name, BlockingCategoryDeleteListener.Mode.FAIL)

        when: 'the category is deleted through the API (the action ends with render status: 204, then the commit fails)'
        Response response = executor.submit({
            categoryApiWrapper.api.delete(category.id, new ResponseSpecBuilder().build())
        } as java.util.concurrent.Callable<Response>).get(10, TimeUnit.SECONDS)

        then: 'the commit was attempted after the render'
        assert deleteListener.committing.count == 0

        and: 'the client gets the JSON error response (the spec client sends Accept: application/json), not the 204'
        // Without the deferred flush the 204 is already committed when the commit fails, so the exception resolver
        // cannot forward to the error page and the failure never reaches the client.
        assert response.statusCode == 500
        assert response.jsonPath().getInt('errorCode') == 500
        assert response.jsonPath().getString('errorMessage') == BlockingCategoryDeleteListener.COMMIT_FAILURE_MESSAGE

        and: 'the delete was rolled back'
        assert categoryApiWrapper.api.get(category.id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getString('name') == category.name
    }

    private Category createCategoryForDelete() {
        String categoryName = "${CATEGORY_NAME_PREFIX} ${randomUtil.randomStringFieldValue('name')}"
        Category category = categoryApiWrapper.createOK(new CategoryTestBuilder().name(categoryName).rootCategory().build())
        assert category.id
        category.name = categoryName
        return category
    }

    /**
     * True if the future completes within the given time. An ExecutionException (the API call itself failed) is
     * deliberately left to propagate and fail the test.
     */
    private static boolean completesWithin(Future<?> future, long millis) {
        try {
            future.get(millis, TimeUnit.MILLISECONDS)
            return true
        } catch (TimeoutException ignored) {
            return false
        }
    }

    /**
     * Blocks the thread that inserted a category with the armed name until released. Because the post-insert
     * event fires during the flush at commit time, blocking here holds the transaction open after the controller
     * has rendered its response.
     */
    static class BlockingCategoryInsertListener implements PostInsertEventListener {

        private volatile String armedCategoryName
        CountDownLatch inserted
        CountDownLatch released

        void arm(String categoryName) {
            inserted = new CountDownLatch(1)
            released = new CountDownLatch(1)
            armedCategoryName = categoryName
        }

        void release() {
            released?.countDown()
        }

        void disarm() {
            armedCategoryName = null
            release()
        }

        @Override
        void onPostInsert(PostInsertEvent event) {
            String name = armedCategoryName
            if (name != null && event.entity instanceof Category && ((Category) event.entity).name == name) {
                inserted.countDown()
                // Bounded so that a failing test cannot hang the suite.
                released.await(10, TimeUnit.SECONDS)
            }
        }

        @Override
        boolean requiresPostCommitHanding(EntityPersister persister) {
            return false
        }
    }

    /**
     * Acts on the transaction that deleted a category with the armed name. The post-delete event fires inside the
     * controller action (CategoryApiController.delete flushes the delete before it renders), so the listener only
     * registers a transaction synchronization there; its beforeCommit runs once the action has returned and its
     * render status: 204 has run, and either holds the commit until released (HOLD) or fails it (FAIL).
     */
    static class BlockingCategoryDeleteListener implements PostDeleteEventListener {

        enum Mode { HOLD, FAIL }

        static final String COMMIT_FAILURE_MESSAGE = 'Commit refused by DeferResponseCloseFilterSpec'

        private volatile String armedCategoryName
        private volatile Mode mode
        CountDownLatch committing
        CountDownLatch released

        void arm(String categoryName, Mode mode) {
            committing = new CountDownLatch(1)
            released = new CountDownLatch(1)
            this.mode = mode
            armedCategoryName = categoryName
        }

        void release() {
            released?.countDown()
        }

        void disarm() {
            armedCategoryName = null
            release()
        }

        @Override
        void onPostDelete(PostDeleteEvent event) {
            String name = armedCategoryName
            if (name == null || !(event.entity instanceof Category) || ((Category) event.entity).name != name) {
                return
            }
            // One transaction per arming.
            armedCategoryName = null
            assert TransactionSynchronizationManager.isSynchronizationActive()
            CountDownLatch committingLatch = committing
            CountDownLatch releasedLatch = released
            Mode armedMode = mode
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                void beforeCommit(boolean readOnly) {
                    committingLatch.countDown()
                    if (armedMode == Mode.FAIL) {
                        throw new IllegalStateException(COMMIT_FAILURE_MESSAGE)
                    }
                    // Bounded so that a failing test cannot hang the suite.
                    releasedLatch.await(10, TimeUnit.SECONDS)
                }
            })
        }

        @Override
        boolean requiresPostCommitHanding(EntityPersister persister) {
            return false
        }
    }
}
