package org.pih.warehouse.regression.obaf.jobs

import org.springframework.context.ApplicationEvent
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.allocation.AutomaticAllocationEventService
import org.pih.warehouse.allocation.AutomaticBackorderAllocationEventService
import org.pih.warehouse.core.RequisitionEventService
import org.pih.warehouse.inventory.InventoryLevelUpdatedEventService
import org.pih.warehouse.inventory.RefreshPicklistStatusEventService

/**
 * Verifies the event-publication semantics the job-scheduling listeners rely on: a listener declared
 * with AFTER_COMMIT + fallbackExecution must not run before commit, must run once after commit, must not
 * run on rollback, and must still run when there is no surrounding transaction. The migrated listeners
 * are checked to carry exactly that declaration.
 */
class TransactionalEventListenerPhaseSpec extends Specification {

    static class TestEvent extends ApplicationEvent {
        TestEvent() { super('test') }
    }

    static class RecordingListener {
        List<String> calls = []

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
        void onTestEvent(TestEvent event) {
            calls << 'handled'
        }
    }

    static class NoOpTransactionManager extends AbstractPlatformTransactionManager {
        protected Object doGetTransaction() { return new Object() }
        protected void doBegin(Object transaction, TransactionDefinition definition) { }
        protected void doCommit(DefaultTransactionStatus status) { }
        protected void doRollback(DefaultTransactionStatus status) { }
    }

    @Shared AnnotationConfigApplicationContext context
    @Shared RecordingListener listener
    @Shared TransactionTemplate tx

    void setupSpec() {
        context = new AnnotationConfigApplicationContext()
        listener = new RecordingListener()
        context.beanFactory.registerSingleton('listener', listener)
        context.refresh()
        PlatformTransactionManager txManager = new NoOpTransactionManager()
        tx = new TransactionTemplate(txManager)
    }

    void cleanupSpec() {
        context.close()
    }

    void setup() {
        listener.calls.clear()
    }

    void 'listener does not run before commit and runs once after commit'() {
        when:
        tx.execute { TransactionStatus status ->
            context.publishEvent(new TestEvent())
            assert listener.calls.isEmpty()
        }

        then:
        listener.calls == ['handled']
    }

    void 'listener does not run when the transaction rolls back'() {
        when:
        tx.execute { TransactionStatus status ->
            context.publishEvent(new TestEvent())
            status.setRollbackOnly()
        }

        then:
        listener.calls.isEmpty()
    }

    void 'listener still runs when there is no transaction (fallback execution)'() {
        when:
        context.publishEvent(new TestEvent())

        then:
        listener.calls == ['handled']
    }

    @Unroll
    void '#listener.simpleName.#method is declared AFTER_COMMIT with fallbackExecution'() {
        when:
        TransactionalEventListener annotation = listener.declaredMethods.find { it.name == method }
                ?.getAnnotation(TransactionalEventListener)

        then:
        annotation != null
        annotation.phase() == TransactionPhase.AFTER_COMMIT
        annotation.fallbackExecution()

        where:
        listener                                  | method
        AutomaticAllocationEventService           | 'onAutomaticAllocationEvent'
        AutomaticBackorderAllocationEventService  | 'onAutomaticBackorderReallocationEvent'
        RequisitionEventService                   | 'onRequisitionEvent'
        RefreshPicklistStatusEventService         | 'onRefreshPicklistStatusEvent'
        InventoryLevelUpdatedEventService         | 'onInventoryLevelUpdated'
    }
}
