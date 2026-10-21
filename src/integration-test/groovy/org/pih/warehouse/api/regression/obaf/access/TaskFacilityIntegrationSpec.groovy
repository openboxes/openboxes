package org.pih.warehouse.api.regression.obaf.access

import groovy.json.JsonOutput
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Cookie
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.api.regression.support.RegressionRoleFixtures
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.order.Order
import org.pih.warehouse.picklist.PicklistItem

/**
 * The pick and putaway task endpoints through the running server, with users whose roles are held at one facility:
 * the facility in the URL must be one where the user holds a role (403 otherwise), a task is found only under its
 * own facility (404 otherwise), the signed-in user is recorded as the picker, stager and completer, and a task is
 * assigned to another user only by an administrator at the task's facility.
 *
 * Facility B holds the tasks (a pick task of a PICKING order and a putaway task from its storage bin); facility A
 * is another depot. "worker A" holds the Assistant role at A only and is signed in at A, "worker B" the same at B.
 */
class TaskFacilityIntegrationSpec extends ObafTaskIntegrationSpec {

    /** The admin user upstream's API specs sign in as (ApiSpec). */
    static final String ADMIN_PASSWORD = System.getProperty('password') ?: 'password'

    PickSite b
    String a
    PickOrder pick
    String putawayTaskId
    String putawayOrderId
    String workerA, workerB, otherUser
    String workerAId, workerBId, otherUserId

    void setup() {
        b = pickSite('TF-B')
        a = depot('TF-A')
        pick = pickOrder(b, [3])
        Map po = putawayOrderOf(b.dc, b.product, b.lotId, [[2, b.alt, b.bin]])
        putawayTaskId = po.itemIds[0]
        putawayOrderId = po.orderId

        String n = uid().toLowerCase()
        workerA = "test-tf-worker-a-${n}"
        workerB = "test-tf-worker-b-${n}"
        otherUser = "test-tf-other-${n}"
        workerAId = RegressionRoleFixtures.ensureUser(workerA, [], [(a): [RoleType.ROLE_ASSISTANT]]).id
        workerBId = RegressionRoleFixtures.ensureUser(workerB, [], [(b.dc): [RoleType.ROLE_ASSISTANT]]).id
        otherUserId = RegressionRoleFixtures.ensureUser(otherUser, [RoleType.ROLE_ASSISTANT], [:]).id
    }

    // ------------------------------------------------------------------ pick tasks

    void 'a worker at the facility reads, starts and picks its pick task, and is recorded as assignee and picker'() {
        given:
        Cookie worker = login(workerB, b.dc)
        String task = "/api/facilities/${b.dc}/pick-tasks/${pick.taskIds[0]}"

        when:
        Response read = send(worker, 'GET', task)
        Response start = send(worker, 'PATCH', task, [action: 'start'])
        Response picked = send(worker, 'PATCH', task, [action: 'pick', outboundContainerId: b.tote, pickedById: otherUserId])

        then:
        read.statusCode == 200
        read.body().asString().contains(pick.taskIds[0])
        start.statusCode == 200
        picked.statusCode == 200
        pickTask(pick.taskIds[0]).subMap(['status', 'assignee', 'pickedBy']) ==
                [status: 'PICKED', assignee: workerBId, pickedBy: workerBId]
    }

    void 'a pick task is not found through the path of another facility, whatever the role'() {
        given:
        Cookie worker = login(workerA, a)
        Cookie admin = login('admin', ADMIN_PASSWORD, a)
        String task = "/api/facilities/${a}/pick-tasks/${pick.taskIds[0]}"

        expect:
        [worker, admin].every { Cookie c ->
            send(c, 'GET', task).statusCode == 404 &&
                    send(c, 'PATCH', task, [action: 'start']).statusCode == 404 &&
                    send(c, 'POST', "${task}/reallocate", [picklistItems: [[quantity: 1, binLocation: [id: b.alt],
                                                                             inventoryItem: [id: b.lotId]]]]).statusCode == 404
        }
        pickTask(pick.taskIds[0]).subMap(['status', 'assignee']) == [status: 'PENDING', assignee: null]
        tx { PicklistItem.get(pick.taskIds[0]) } != null
    }

    void 'a worker without a role at the facility in the path is refused there'() {
        given:
        Cookie worker = login(workerA, a)
        String tasks = "/api/facilities/${b.dc}/pick-tasks"

        expect:
        send(worker, 'GET', tasks).statusCode == 403
        send(worker, 'GET', "${tasks}/counts").statusCode == 403
        send(worker, 'GET', "${tasks}/${pick.taskIds[0]}").statusCode == 403
        send(worker, 'PATCH', "${tasks}/${pick.taskIds[0]}", [action: 'start']).statusCode == 403
        send(worker, 'PATCH', "${tasks}/containers/${b.tote}", [action: 'drop', stagingLocationId: b.staging]).statusCode == 403
        pickTask(pick.taskIds[0]).assignee == null

        and: 'the facility of the path is where the role is looked up: worker B sees the tasks of B'
        send(login(workerB, b.dc), 'GET', tasks).body().asString().contains(pick.taskIds[0])
    }

    void 'an outbound container is found only among the bins of the facility in the path'() {
        given:
        Cookie admin = login('admin', ADMIN_PASSWORD, a)
        String toteNumber = tx { Location.get(b.tote).locationNumber }

        expect:
        send(admin, 'PATCH', "/api/facilities/${a}/pick-tasks/containers/${b.tote}",
                [action: 'drop', stagingLocationId: b.staging]).statusCode == 404
        send(admin, 'PATCH', "/api/facilities/${a}/pick-tasks/containers/${toteNumber}",
                [action: 'drop', stagingLocationId: b.staging]).statusCode == 404
    }

    void 'starting a pick task for another user needs the admin role at the task facility'() {
        given:
        String adminAtB = "test-tf-admin-b-${uid().toLowerCase()}"
        RegressionRoleFixtures.ensureUser(adminAtB, [], [(b.dc): [RoleType.ROLE_ADMIN]])
        String task = "/api/facilities/${b.dc}/pick-tasks/${pick.taskIds[0]}"

        when:
        Response byWorker = send(login(workerB, b.dc), 'PATCH', task, [action: 'start', assigneeId: otherUserId])

        then:
        byWorker.statusCode == 400
        pickTask(pick.taskIds[0]).assignee == null

        when:
        Response byAdmin = send(login(adminAtB, b.dc), 'PATCH', task, [action: 'start', assigneeId: otherUserId])

        then:
        byAdmin.statusCode == 200
        pickTask(pick.taskIds[0]).assignee == otherUserId
    }

    void 'naming yourself as the assignee is the same as naming nobody'() {
        when:
        Response start = send(login(workerB, b.dc), 'PATCH', "/api/facilities/${b.dc}/pick-tasks/${pick.taskIds[0]}",
                [action: 'start', assigneeId: workerBId])

        then:
        start.statusCode == 200
        pickTask(pick.taskIds[0]).assignee == workerBId
    }

    void 'the bins a pick request names must belong to the task facility'() {
        given: 'a tote and a bin at A, set up like those of B'
        String toteAtA = bin(a, 'TOTE', [ActivityCode.OUTBOUND_CONTAINER, ActivityCode.HOLD_STOCK, b.deliveryType.activityCode], false)
        String binAtA = bin(a, 'BIN')
        Cookie admin = login('admin', ADMIN_PASSWORD, b.dc)
        String task = "/api/facilities/${b.dc}/pick-tasks/${pick.taskIds[0]}"

        when:
        Response pickedIntoA = send(admin, 'PATCH', task, [action: 'pick', outboundContainerId: toteAtA])
        Response reallocatedToA = send(admin, 'POST', "${task}/reallocate",
                [picklistItems: [[quantity: 3, binLocation: [id: binAtA], inventoryItem: [id: b.lotId]]]])
        Response reallocatedToNowhere = send(admin, 'POST', "${task}/reallocate",
                [picklistItems: [[quantity: 3, binLocation: [id: 'no-such-bin'], inventoryItem: [id: b.lotId]]]])

        then:
        pickedIntoA.statusCode >= 400
        reallocatedToA.statusCode >= 400
        reallocatedToNowhere.statusCode >= 400
        pickTask(pick.taskIds[0]).subMap(['status', 'outboundContainer']) == [status: 'PENDING', outboundContainer: null]
        tx { PicklistItem.get(pick.taskIds[0]) } != null
    }

    void 'a facility that does not exist is not found; a Browser at the facility reads its tasks but cannot change them'() {
        given:
        String browserAtB = "test-tf-browser-b-${uid().toLowerCase()}"
        RegressionRoleFixtures.ensureUser(browserAtB, [], [(b.dc): [RoleType.ROLE_BROWSER]])
        Cookie browser = login(browserAtB, b.dc)
        String task = "/api/facilities/${b.dc}/pick-tasks/${pick.taskIds[0]}"

        expect:
        send(login(workerB, b.dc), 'GET', "/api/facilities/no-such-facility/pick-tasks/${pick.taskIds[0]}").statusCode == 404
        send(browser, 'GET', task).statusCode == 200
        send(browser, 'GET', "/api/facilities/${b.dc}/pick-tasks/counts").statusCode == 200
        send(browser, 'GET', "/api/facilities/${b.dc}/putaway-tasks/${putawayTaskId}").statusCode == 200
        refused(send(browser, 'PATCH', task, [action: 'start']))
        refused(send(browser, 'PATCH', "/api/facilities/${b.dc}/putaway-tasks/${putawayTaskId}", [action: 'start']))
        pickTask(pick.taskIds[0]).assignee == null
    }

    // ------------------------------------------------------------------ putaway tasks

    void 'a worker at the facility reads and completes its putaway task, and is recorded as the completer'() {
        given:
        Cookie worker = login(workerB, b.dc)
        String task = "/api/facilities/${b.dc}/putaway-tasks/${putawayTaskId}"

        when:
        Response read = send(worker, 'GET', task)
        Response alternates = send(worker, 'GET', "${task}/alternate-destinations")
        Response completed = send(worker, 'PATCH', task, [action: 'complete', destination: b.bin, isDirectPutaway: true,
                                                         completedBy: otherUserId])

        then:
        read.statusCode == 200
        read.body().asString().contains(putawayTaskId)
        alternates.statusCode == 200
        completed.statusCode == 200
        completedBy() == workerBId
    }

    void 'a putaway task is not found through the path of another facility, and is refused at a facility without a role'() {
        given:
        Cookie worker = login(workerA, a)
        Cookie admin = login('admin', ADMIN_PASSWORD, a)
        String viaA = "/api/facilities/${a}/putaway-tasks/${putawayTaskId}"
        String viaB = "/api/facilities/${b.dc}/putaway-tasks/${putawayTaskId}"
        Map complete = [action: 'complete', destination: b.bin, isDirectPutaway: true]

        expect: 'through the path of A: not found, whatever the role'
        [worker, admin].every { Cookie c ->
            send(c, 'GET', viaA).statusCode == 404 &&
                    send(c, 'GET', "${viaA}/alternate-destinations").statusCode == 404 &&
                    send(c, 'PATCH', viaA, [action: 'start']).statusCode == 404 &&
                    send(c, 'PATCH', viaA, complete).statusCode == 404
        }

        and: 'through the path of B, for a worker without a role at B: refused'
        send(worker, 'GET', viaB).statusCode == 403
        send(worker, 'GET', "/api/facilities/${b.dc}/putaway-tasks").statusCode == 403
        send(worker, 'PATCH', viaB, [action: 'start']).statusCode == 403
        send(worker, 'PATCH', "${viaB}/status", complete).statusCode == 403

        and:
        putawayTask(putawayTaskId).subMap(['status', 'assignee', 'completedBy']) ==
                [status: 'PENDING', assignee: null, completedBy: null]
    }

    void 'assigning a putaway task to another user needs the admin role at the task facility'() {
        given:
        String adminAtB = "test-tf-admin-b-${uid().toLowerCase()}"
        RegressionRoleFixtures.ensureUser(adminAtB, [], [(b.dc): [RoleType.ROLE_ADMIN]])
        String task = "/api/facilities/${b.dc}/putaway-tasks/${putawayTaskId}"

        when:
        Response byWorker = send(login(workerB, b.dc), 'PATCH', task, [action: 'assign', assignee: otherUserId])

        then:
        byWorker.statusCode == 400
        putawayTask(putawayTaskId).assignee == null

        when:
        Response toSelf = send(login(workerB, b.dc), 'PATCH', task, [action: 'assign', assignee: workerBId])

        then:
        toSelf.statusCode == 200
        putawayTask(putawayTaskId).assignee == workerBId

        when:
        Response byAdmin = send(login(adminAtB, b.dc), 'PATCH', task, [action: 'assign', assignee: otherUserId])

        then:
        byAdmin.statusCode == 200
        putawayTask(putawayTaskId).assignee == otherUserId
    }

    void 'the destination a putaway request names must belong to the task facility; an unknown assignee is refused'() {
        given:
        String binAtA = bin(a, 'DEST')
        Cookie admin = login('admin', ADMIN_PASSWORD, b.dc)
        String task = "/api/facilities/${b.dc}/putaway-tasks/${putawayTaskId}"

        expect:
        send(admin, 'PATCH', task, [action: 'complete', destination: binAtA, isDirectPutaway: true]).statusCode >= 400
        send(admin, 'PATCH', task, [action: 'assign', assignee: 'no-such-user']).statusCode == 400
        putawayTask(putawayTaskId).subMap(['status', 'destination', 'assignee', 'completedBy']) ==
                [status: 'PENDING', destination: 'BIN', assignee: null, completedBy: null]
    }

    // ------------------------------------------------------------------ sign-in without a location

    void 'signed in without a location: a global role counts at every facility, a role held at one facility does not'() {
        given:
        String global = "test-tf-global-${uid().toLowerCase()}"
        RegressionRoleFixtures.ensureUser(global, [RoleType.ROLE_ASSISTANT], [:])
        Cookie globalWorker = login(global, null)
        Cookie locationWorker = login(workerB, null)

        expect: 'the global role reaches the tasks of B through the path of B only'
        send(globalWorker, 'GET', "/api/facilities/${b.dc}/pick-tasks/${pick.taskIds[0]}").statusCode == 200
        send(globalWorker, 'GET', "/api/facilities/${a}/pick-tasks/${pick.taskIds[0]}").statusCode == 404
        send(globalWorker, 'GET', "/api/facilities/${b.dc}/putaway-tasks/${putawayTaskId}").statusCode == 200
        send(globalWorker, 'GET', "/api/facilities/${a}/putaway-tasks/${putawayTaskId}").statusCode == 404

        and: 'a role held only at B needs the session at B'
        refused(send(locationWorker, 'GET', "/api/facilities/${b.dc}/pick-tasks/${pick.taskIds[0]}"))
        refused(send(locationWorker, 'PATCH', "/api/facilities/${b.dc}/pick-tasks/${pick.taskIds[0]}", [action: 'start']))
        pickTask(pick.taskIds[0]).assignee == null
    }

    // ------------------------------------------------------------------ helpers

    private String completedBy() {
        return tx { Order.get(putawayOrderId).completedBy?.id } as String
    }

    /** A 403 from the controller or the role check's redirect to the access denied page. */
    private static boolean refused(Response r) {
        return r.statusCode == 403 || (r.statusCode == 302 && r.header('Location')?.contains('handleForbidden'))
    }

    private Cookie login(String username, String locationId) {
        return login(username, RegressionRoleFixtures.PASSWORD, locationId)
    }

    /** Signs in through POST /api/login, at the location when one is given, and returns the session cookie. */
    private Cookie login(String username, String password, String locationId) {
        Map body = [username: username, password: password]
        if (locationId) {
            body.location = locationId
        }
        Response r = request(null).body(JsonOutput.toJson(body)).post('/api/login')
        assert r.statusCode == 200: "login as ${username} failed: ${r.statusCode}"
        return r.getDetailedCookie('JSESSIONID')
    }

    private Response send(Cookie session, String method, String path, Map body = null) {
        RequestSpecification spec = request(session)
        if (body != null) {
            spec.body(JsonOutput.toJson(body))
        }
        return spec.request(method, path)
    }

    private RequestSpecification request(Cookie session) {
        RequestSpecification spec = RestAssured.given()
                .port(serverPort)
                .basePath('/openboxes')
                .redirects().follow(false)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header('X-Requested-With', 'XMLHttpRequest')
        if (session) {
            spec.cookie(session)
        }
        return spec
    }
}
