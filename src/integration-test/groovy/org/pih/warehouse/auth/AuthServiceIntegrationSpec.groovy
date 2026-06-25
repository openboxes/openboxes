package org.pih.warehouse.auth

import grails.gorm.transactions.Rollback
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.User

/**
 * Verifies that background jobs can authenticate as the configured system user via
 * {@link AuthService#withSystemUser(groovy.lang.Closure)}, including when that user is disabled.
 */
@Rollback
class AuthServiceIntegrationSpec extends IntegrationSpec {

    AuthService authService

    void 'getSystemUser returns the configured system user'() {
        when:
        User systemUser = authService.getSystemUser()

        then:
        systemUser != null
        // Defaults to the built-in admin user (openboxes.system.username)
        systemUser.username == Constants.SYSTEM_USER_USERNAME_DEFAULT_VALUE
    }

    void 'withSystemUser sets the current user for the duration of the closure and restores it afterward'() {
        given:
        assert AuthService.currentUser == null

        when:
        User userDuringExecution = authService.withSystemUser {
            return AuthService.currentUser
        }

        then: 'the system user is the current user while the closure runs'
        userDuringExecution != null
        userDuringExecution.username == Constants.SYSTEM_USER_USERNAME_DEFAULT_VALUE

        and: 'the previous (empty) user context is restored afterward'
        AuthService.currentUser == null
    }

    void 'withSystemUser restores the previously authenticated user'() {
        given:
        User previousUser = User.findByUsername(Constants.SYSTEM_USER_USERNAME_DEFAULT_VALUE)
        authService.setCurrentUser(previousUser)

        when:
        authService.withSystemUser {
            assert AuthService.currentUser != null
        }

        then:
        AuthService.currentUser?.id == previousUser.id

        cleanup:
        authService.setCurrentUser(null)
    }

    void 'getSystemUser resolves the system user even when the account is disabled'() {
        given: 'the system user is disabled'
        User systemUser = User.findByUsername(Constants.SYSTEM_USER_USERNAME_DEFAULT_VALUE)
        systemUser.active = false
        systemUser.save(flush: true)

        when: 'the system user is resolved (active flag is intentionally not enforced)'
        User resolved = authService.getSystemUser()

        then: 'the disabled user is still returned for job authentication'
        resolved != null
        resolved.username == Constants.SYSTEM_USER_USERNAME_DEFAULT_VALUE
        !resolved.active
    }
}
