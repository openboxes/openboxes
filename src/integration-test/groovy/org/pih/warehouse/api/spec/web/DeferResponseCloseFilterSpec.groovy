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
import org.hibernate.event.spi.EventType
import org.hibernate.event.spi.PostInsertEvent
import org.hibernate.event.spi.PostInsertEventListener
import org.hibernate.persister.entity.EntityPersister
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.product.Category

/**
 * Verifies that a response rendered inside a transactional controller action is not completed for the client
 * before the transaction has committed.
 *
 * The test blocks the commit of a category save from inside Hibernate's post-insert event (which fires when the
 * INSERT is flushed, after the controller has already rendered the response) and checks that the HTTP client is
 * still waiting while the commit is blocked. Without DeferResponseCloseFilter the client receives the complete
 * response as soon as the JSON converter closes the response writer, which is before the INSERT is even flushed.
 */
class DeferResponseCloseFilterSpec extends ApiSpec {

    static final String CATEGORY_NAME_PREFIX = 'Test Category (defer response close)'

    // Hibernate 5.2 has no removeListener, and clearing the listener group would also drop its duplication
    // strategies, so the listener is registered once per session factory and left in place. It is inert unless
    // armed, so it does not affect other specs.
    static BlockingCategoryInsertListener listener
    static SessionFactory listenerSessionFactory

    @Autowired
    SessionFactory sessionFactory

    ExecutorService executor

    @Override
    void setupData() {
        if (listener == null || listenerSessionFactory != sessionFactory) {
            listener = new BlockingCategoryInsertListener()
            ((SessionFactoryImplementor) sessionFactory)
                    .getServiceRegistry()
                    .getService(EventListenerRegistry)
                    .getEventListenerGroup(EventType.POST_INSERT)
                    .appendListener(listener)
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
}
