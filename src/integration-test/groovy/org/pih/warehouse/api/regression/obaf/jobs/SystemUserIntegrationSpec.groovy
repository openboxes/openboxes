package org.pih.warehouse.api.regression.obaf.jobs

import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationService
import org.pih.warehouse.api.regression.obaf.support.ObafIntegrationSpec
import org.pih.warehouse.api.regression.obaf.support.QuartzRunner
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.User
import org.pih.warehouse.core.history.EventLogCode
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

/**
 * The system user OBAF's jobs act as: `openboxes.system.username` (default "admin"),
 * AuthService.getSystemUser / withSystemUser, and what the jobs do when that user is missing
 * or inactive. Config is overridden per feature and restored (ConfigOverride).
 */
class SystemUserIntegrationSpec extends ObafIntegrationSpec {

    @Autowired
    AllocationService allocationService

    static final Date SOON = new Date() + 30

    void 'openboxes.system.username defaults to admin and getSystemUser answers that user'() {
        expect:
        grailsApplication.config.getProperty(Constants.SYSTEM_USER_USERNAME_CONFIG_KEY, String) == 'admin'
        tx { authService.systemUser.username } == 'admin'
    }

    String newUser(boolean active) {
        String username = "test-sys-${uid()}".toString()
        tx {
            new User(username: username, password: 'password', firstName: 'Test', lastName: 'System', active: active,
                    email: "${username.toLowerCase()}@example.com".toString()).save(flush: true, failOnError: true)
        }
        return username
    }

    void 'a configured system username is honoured'() {
        given:
        String username = newUser(true)
        config.set(Constants.SYSTEM_USER_USERNAME_CONFIG_KEY, username)

        expect:
        tx { authService.systemUser.username } == username
    }

    void 'a missing system user: getSystemUser throws IllegalStateException naming the username'() {
        given:
        config.set(Constants.SYSTEM_USER_USERNAME_CONFIG_KEY, 'test-no-such-user')

        when:
        tx { authService.systemUser }

        then:
        IllegalStateException e = thrown()
        e.message == 'Unable to find system user with username test-no-such-user'
    }

    void 'withSystemUser swaps the current user in for the closure and restores the previous one'() {
        given:
        actAs(null)

        expect:
        tx {
            assert AuthService.currentUser == null
            String inside = authService.withSystemUser { AuthService.currentUser?.username }
            [inside, AuthService.currentUser]
        } == ['admin', null]
    }

    void 'control: a change committed while a user is current is audited as that user (the audit hook works)'() {
        given:
        String dc = depot('SU-CTRL')
        String req = requisition(dc, [[productId: product('SUC'), quantity: 1]])
        actAs('admin')

        when:
        tx { Requisition r = Requisition.get(req); r.description = "touched ${uid()}".toString() }

        then:
        tx { Requisition.get(req).updatedBy?.username } == 'admin'
    }

    void 'automatic allocation with no system user does nothing and records nothing on the order'() {
        given:
        config.set(Constants.SYSTEM_USER_USERNAME_CONFIG_KEY, 'test-no-such-user')
        String dc = depot('SU-AA', [ActivityCode.AUTOMATIC_ALLOCATION_ENABLED])
        String b1 = bin(dc, 'B1')
        String p = product('SUAA')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 2]])
        tx { Requisition r = Requisition.get(req); r.autoAllocationRequested = true; r.save(flush: true, failOnError: true) }

        when:
        tx { allocationService.allocateRequisition(req) }

        then:
        notThrown(Exception)
        picks(req) == []
        status(req) == RequisitionStatus.CREATED
        tx { Requisition.get(req).eventLogs.count { it.eventLogCode == EventLogCode.ERROR_OCCURRED } } == 0
    }

    void 'AutomaticIssuanceJob with no system user fails the run and leaves the order STAGED'() {
        given:
        config.set('openboxes.jobs.automaticIssuanceJob.enabled', true)
        String dc = depot('SU-AI')
        String b1 = bin(dc, 'B1')
        String p = product('SUAI')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 2]], [deliveryTypeCode: DeliveryTypeCode.PICK_UP])
        tx { allocationService.allocate(Requisition.get(req), AllocationMode.AUTO, []) }
        tx { Requisition r = Requisition.get(req); r.status = RequisitionStatus.STAGED; r.save(flush: true, failOnError: true) }
        config.set(Constants.SYSTEM_USER_USERNAME_CONFIG_KEY, 'test-no-such-user')

        when:
        QuartzRunner.Run run = QuartzRunner.fire(scheduler(), AutomaticIssuanceJob, [requisitionId: req])

        then:
        run.exception?.cause instanceof IllegalStateException
        run.exception.cause.message.contains('test-no-such-user')
        status(req) == RequisitionStatus.STAGED
    }
}
