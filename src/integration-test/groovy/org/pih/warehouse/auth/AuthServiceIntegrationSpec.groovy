package org.pih.warehouse.auth

import grails.gorm.transactions.Rollback
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.core.User

/**
 * Verifies that background jobs can authenticate as the configured robot user via
 * {@link AuthService#withRobotUser(groovy.lang.Closure)}, including when that user is disabled.
 */
@Rollback
class AuthServiceIntegrationSpec extends IntegrationSpec {

    // Injected only to exercise the instance setCurrentUser API; the robot user methods are static.
    AuthService authService

    void 'getRobotUser returns the configured robot user'() {
        when:
        User robotUser = AuthService.getRobotUser()

        then:
        robotUser != null
        // Defaults to the built-in admin user (openboxes.robot.username)
        robotUser.username == AuthService.DEFAULT_ROBOT_USERNAME
    }

    void 'withRobotUser sets the current user for the duration of the closure and restores it afterward'() {
        given:
        assert AuthService.currentUser == null

        when:
        User userDuringExecution = AuthService.withRobotUser {
            return AuthService.currentUser
        }

        then: 'the robot user is the current user while the closure runs'
        userDuringExecution != null
        userDuringExecution.username == AuthService.DEFAULT_ROBOT_USERNAME

        and: 'the previous (empty) user context is restored afterward'
        AuthService.currentUser == null
    }

    void 'withRobotUser restores the previously authenticated user'() {
        given:
        User previousUser = User.findByUsername(AuthService.DEFAULT_ROBOT_USERNAME)
        authService.setCurrentUser(previousUser)

        when:
        AuthService.withRobotUser {
            assert AuthService.currentUser != null
        }

        then:
        AuthService.currentUser?.id == previousUser.id

        cleanup:
        authService.setCurrentUser(null)
    }

    void 'getRobotUser resolves the robot user even when the account is disabled'() {
        given: 'the robot user is disabled'
        User robotUser = User.findByUsername(AuthService.DEFAULT_ROBOT_USERNAME)
        robotUser.active = false
        robotUser.save(flush: true)

        when: 'the robot user is resolved (active flag is intentionally not enforced)'
        User resolved = AuthService.getRobotUser()

        then: 'the disabled user is still returned for job authentication'
        resolved != null
        resolved.username == AuthService.DEFAULT_ROBOT_USERNAME
        !resolved.active
    }
}
