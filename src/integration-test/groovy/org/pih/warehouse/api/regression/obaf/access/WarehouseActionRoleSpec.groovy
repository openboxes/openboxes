package org.pih.warehouse.api.regression.obaf.access

import grails.core.GrailsApplication
import groovy.json.JsonOutput
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Cookie
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Unroll

import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.support.CaptureServers
import org.pih.warehouse.api.regression.support.ConfigOverride
import org.pih.warehouse.api.regression.support.RegressionRoleFixtures
import org.pih.warehouse.core.RoleType
import util.ConfigHelper

/**
 * The minimum role of the warehouse task, allocation, packing, label, rollback, auto-receive and issue actions,
 * through the running server: every role below the action's minimum is refused by the role check, every role at
 * or above it reaches the action.
 *
 * The ids in the paths do not exist, so an action that is reached answers with an error of its own (404, 400, 500
 * or a redirect back to a page) and changes nothing. The label actions print to a loopback stand-in for the
 * printer. Each user holds one global role and is signed in at the main facility.
 */
class WarehouseActionRoleSpec extends RegressionApiSpec {

    static final String F = '1'
    static final String X = 'no-such-id'

    /** The roles each action is called as, lowest first. */
    static final List<RoleType> ROLES = [RoleType.ROLE_BROWSER, RoleType.ROLE_ASSISTANT, RoleType.ROLE_MANAGER, RoleType.ROLE_ADMIN]

    @Autowired
    GrailsApplication grailsApplication

    @Shared
    Map<String, Cookie> sessions = [:]

    ConfigOverride config
    CaptureServers.Printer printer

    @Override
    void setupData() {
        printer = new CaptureServers.Printer()
        config = new ConfigOverride(grailsApplication)
                .set('openboxes.barcode.printer.ipAddress', '127.0.0.1')
                .set('openboxes.barcode.printer.port', printer.port)
    }

    @Override
    void cleanupData() {
        config?.restore()
        printer?.close()
    }

    @Unroll
    void '#method #path needs #minimum'() {
        when:
        Map<RoleType, Response> responses = ROLES.collectEntries { RoleType role -> [(role): send(session(role), method, path, body)] }
        List<String> wrong = ROLES.findAll { RoleType role ->
            boolean belowMinimum = role.sortOrder > minimum.sortOrder
            belowMinimum != refused(responses[role])
        }.collect { RoleType role -> "${role}: ${responses[role].statusCode} ${responses[role].header('Location') ?: ''}".toString() }

        then:
        wrong.isEmpty()

        where:
        method  | path                                                   | body                || minimum
        // pick and putaway task changes
        'PATCH' | "/api/facilities/${F}/pick-tasks/${X}"                 | [action: 'start']   || RoleType.ROLE_ASSISTANT
        'POST'  | "/api/facilities/${F}/pick-tasks/${X}/reallocate"      | [picklistItems: []] || RoleType.ROLE_ASSISTANT
        'PATCH' | "/api/facilities/${F}/pick-tasks/containers/${X}"      | [action: 'drop']    || RoleType.ROLE_ASSISTANT
        'PATCH' | "/api/facilities/${F}/putaway-tasks/${X}"              | [action: 'noop']    || RoleType.ROLE_ASSISTANT
        'PATCH' | "/api/facilities/${F}/putaway-tasks/${X}/status"       | [action: 'noop']    || RoleType.ROLE_ASSISTANT
        // allocation
        'POST'  | "/api/requisitions/${X}/allocate"                      | [:]                 || RoleType.ROLE_MANAGER
        'POST'  | "/api/requisitions/${X}/deallocate"                    | [:]                 || RoleType.ROLE_MANAGER
        'POST'  | "/api/outbound-orders/${X}/items/${X}/allocations"     | [mode: 'AUTO']      || RoleType.ROLE_MANAGER
        'GET'   | "/allocation/allocate/${X}"                            | null                || RoleType.ROLE_MANAGER
        'GET'   | "/allocation/deallocate/${X}"                          | null                || RoleType.ROLE_MANAGER
        'GET'   | "/allocation/redoAutopick/${X}"                        | null                || RoleType.ROLE_MANAGER
        'GET'   | "/stockMovement/allocate/${X}"                         | null                || RoleType.ROLE_MANAGER
        'GET'   | "/stockMovement/clearPicklist/${X}"                    | null                || RoleType.ROLE_MANAGER
        'GET'   | "/stockMovement/redoAutopick/${X}"                     | null                || RoleType.ROLE_MANAGER
        // packing location, labels, rollbacks, auto-receive, issue
        'POST'  | "/api/stockMovements/${X}/packingLocation"             | [:]                 || RoleType.ROLE_ASSISTANT
        'POST'  | "/api/products/${X}/labels/${X}"                       | [:]                 || RoleType.ROLE_ASSISTANT
        'POST'  | "/api/containers/${X}/labels/${X}"                     | [:]                 || RoleType.ROLE_ASSISTANT
        'POST'  | "/api/internalLocations/${X}/labels/${X}"              | [:]                 || RoleType.ROLE_ASSISTANT
        'GET'   | "/putaway/rollback/${X}"                               | null                || RoleType.ROLE_ASSISTANT
        'GET'   | "/putaway/rollbackAndDelete/${X}"                      | null                || RoleType.ROLE_ASSISTANT
        'GET'   | "/stockMovement/rollbackAndDelete/${X}"                | null                || RoleType.ROLE_ASSISTANT
        'GET'   | "/partialReceiving/autoreceive/${X}"                   | null                || RoleType.ROLE_ASSISTANT
        'GET'   | "/requisition/issue/${X}"                              | null                || RoleType.ROLE_ASSISTANT
    }

    void 'each action has its own rule in openboxes.security.rbac.rules'() {
        expect:
        ConfigHelper.findAccessRule(controller, action)?.accessRules?.minimumRequiredRole == minimum

        where:
        controller            | action              || minimum
        'pickTaskApi'         | 'patch'             || RoleType.ROLE_ASSISTANT
        'pickTaskApi'         | 'reallocate'        || RoleType.ROLE_ASSISTANT
        'pickTaskApi'         | 'drop'              || RoleType.ROLE_ASSISTANT
        'putawayTaskApi'      | 'patch'             || RoleType.ROLE_ASSISTANT
        'allocationApi'       | 'allocate'          || RoleType.ROLE_MANAGER
        'allocationApi'       | 'deallocate'        || RoleType.ROLE_MANAGER
        'outboundOrderApi'    | 'allocate'          || RoleType.ROLE_MANAGER
        'allocation'          | 'allocate'          || RoleType.ROLE_MANAGER
        'allocation'          | 'deallocate'        || RoleType.ROLE_MANAGER
        'allocation'          | 'redoAutopick'      || RoleType.ROLE_MANAGER
        'stockMovement'       | 'allocate'          || RoleType.ROLE_MANAGER
        'stockMovement'       | 'clearPicklist'     || RoleType.ROLE_MANAGER
        'stockMovement'       | 'redoAutopick'      || RoleType.ROLE_MANAGER
        'stockMovementApi'    | 'packingLocation'   || RoleType.ROLE_ASSISTANT
        'mobileProductApi'    | 'printLabel'        || RoleType.ROLE_ASSISTANT
        'containerApi'        | 'printLabel'        || RoleType.ROLE_ASSISTANT
        'internalLocationApi' | 'printLabel'        || RoleType.ROLE_ASSISTANT
        'putaway'             | 'rollback'          || RoleType.ROLE_ASSISTANT
        'putaway'             | 'rollbackAndDelete' || RoleType.ROLE_ASSISTANT
        'stockMovement'       | 'rollbackAndDelete' || RoleType.ROLE_ASSISTANT
        'partialReceiving'    | 'autoreceive'       || RoleType.ROLE_ASSISTANT
        'requisition'         | 'issue'             || RoleType.ROLE_ASSISTANT
    }

    void 'a role held at the signed-in facility counts like a global one'() {
        given:
        String n = UUID.randomUUID().toString().take(8)
        String assistant = "test-war-location-assistant-${n}"
        String browser = "test-war-location-browser-${n}"
        RegressionRoleFixtures.ensureUser(assistant, [RoleType.ROLE_BROWSER], [(F): [RoleType.ROLE_ASSISTANT]])
        RegressionRoleFixtures.ensureUser(browser, [], [(F): [RoleType.ROLE_BROWSER]])
        String path = "/api/facilities/${F}/pick-tasks/${X}"

        expect:
        !refused(send(login(assistant), 'PATCH', path, [action: 'start']))
        refused(send(login(browser), 'PATCH', path, [action: 'start']))
        refused(send(login(assistant), 'GET', "/allocation/allocate/${X}"))
    }

    /** The role check refused the call: its redirect to the access denied page, or a 403. */
    static boolean refused(Response r) {
        return r.statusCode == 403 || (r.statusCode in [301, 302, 303] && r.header('Location')?.contains('handleForbidden'))
    }

    /** A session of a user holding only {@code role}, globally, signed in at the main facility (one user per role). */
    Cookie session(RoleType role) {
        String username = "test-war-${role.name().toLowerCase().replace('_', '-')}"
        return sessions.computeIfAbsent(username) {
            RegressionRoleFixtures.ensureUser(username, [role], [:])
            return login(username)
        }
    }

    Cookie login(String username) {
        Response r = request(null)
                .body(JsonOutput.toJson([username: username, password: RegressionRoleFixtures.PASSWORD, location: F]))
                .post('/api/login')
        assert r.statusCode == 200: "login as ${username} failed: ${r.statusCode}"
        return r.getDetailedCookie('JSESSIONID')
    }

    Response send(Cookie session, String method, String path, Map body = null) {
        RequestSpecification spec = request(session)
        if (body != null) {
            spec.body(JsonOutput.toJson(body))
        }
        return spec.request(method, path)
    }

    private RequestSpecification request(Cookie session) {
        RequestSpecification spec = RestAssured.given()
                .basePath('/openboxes')
                .redirects().follow(false)
                .urlEncodingEnabled(false)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
        if (session) {
            spec.cookie(session)
        }
        return spec
    }
}
