package org.pih.warehouse.api.regression.obaf.events

import org.springframework.transaction.TransactionStatus

import org.pih.warehouse.api.regression.obaf.events.support.LoopbackReceiver
import org.pih.warehouse.api.regression.obaf.events.support.ObafEventsIntegrationSpec
import org.pih.warehouse.api.regression.obaf.events.support.RecordingMailService
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec.PickSite
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec.ReplenOrder
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.picklist.PicklistItem

/**
 * ReplenishmentShortageEventService on a real database: a shortage reported on a replenishment pick is sent as one
 * pick.shortage webhook (@Async, after commit) with what n8n needs to notify people, only for facilities with
 * ENABLE_WEBHOOKS, never for a short pick that is rolled back, and without an email from OpenBoxes.
 */
class ReplenishmentShortageWebhookIntegrationSpec extends ObafEventsIntegrationSpec {

    static final Map SHORT_2_DAMAGED = [action: 'short-pick', quantityPicked: 2, reasonCode: 'DAMAGED']

    String number(String locationId) {
        return tx { Location.get(locationId).locationNumber } as String
    }

    void 'a shortage is sent once as pick.shortage, with quantities, reason, people, hold, link and reallocation; no email'() {
        given: 'a replenishment of DISP, 5 to pick from BIN, more stock in ALT'
        PickSite s = pickSite('RS-WH')
        addActivities(s.dc, [ActivityCode.ENABLE_WEBHOOKS])
        String disp = bin(s.dc, 'DISP')
        ReplenOrder o = replenOrder(s, [5], disp)
        replenPatch(o.taskIds[0], [action: 'start'])
        LoopbackReceiver r = webhookReceiver()
        RecordingMailService mail = recordMail()

        when: '2 are picked, 3 reported damaged'
        Throwable e = replenPatch(o.taskIds[0], SHORT_2_DAMAGED + [outboundContainerId: s.tote])

        then:
        e == null
        eventually { events(r, 'pick.shortage', o.orderNumber).size() == 1 }

        when:
        settle()
        Map event = events(r, 'pick.shortage', o.orderNumber)[0]
        String reallocatedId = tx {
            PicklistItem.findAllByPicklist(PicklistItem.get(o.taskIds[0]).picklist)*.id.find { it != o.taskIds[0] }
        } as String

        then: 'one event only'
        events(r, 'pick.shortage', o.orderNumber).size() == 1

        and: 'what was expected and found, why, by whom'
        event.orderType == 'replenishment'
        event.order.orderNumber == o.orderNumber
        event.order.createdBy?.username == 'admin'
        event.location.locationNumber == number(s.bin)
        event.destinationLocation.locationNumber == number(disp)
        event.subMap(['quantityExpected', 'quantityPicked', 'quantityShorted', 'reasonCode']) ==
                [quantityExpected: 5, quantityPicked: 2, quantityShorted: 3, reasonCode: 'DAMAGED']
        event.reportedBy.username == 'admin'
        event.dateReported
        event.metadata.facilityId == s.dc

        and: 'what was held, where to investigate, what was reallocated'
        event.hold == [location: [id: s.lostAndFound, locationNumber: number(s.lostAndFound),
                                  name: tx { Location.get(s.lostAndFound).name }], quantity: 3]
        event.investigationUrl.contains('/report/showLostAndFoundReport')
        event.investigationUrl.contains(s.dc)
        event.reallocations == [[taskId: reallocatedId, location: [id: s.alt, locationNumber: number(s.alt),
                                                                    name: tx { Location.get(s.alt).name }], quantity: 3]]

        and: 'OpenBoxes sends no email'
        mail.sent.isEmpty()
    }

    void 'a facility without ENABLE_WEBHOOKS sends nothing'() {
        given:
        PickSite s = pickSite('RS-NW')
        ReplenOrder o = replenOrder(s, [5], bin(s.dc, 'DISP'))
        replenPatch(o.taskIds[0], [action: 'start'])
        LoopbackReceiver r = webhookReceiver()

        when:
        Throwable e = replenPatch(o.taskIds[0], SHORT_2_DAMAGED + [outboundContainerId: s.tote])
        settle()

        then:
        e == null
        events(r, 'pick.shortage', o.orderNumber).isEmpty()
    }

    void 'a short pick that is rolled back sends nothing'() {
        given:
        PickSite s = pickSite('RS-RB')
        addActivities(s.dc, [ActivityCode.ENABLE_WEBHOOKS])
        ReplenOrder o = replenOrder(s, [5], bin(s.dc, 'DISP'))
        replenPatch(o.taskIds[0], [action: 'start'])
        LoopbackReceiver r = webhookReceiver()

        when:
        PicklistItem.withNewTransaction { TransactionStatus st ->
            authService.setCurrentUser(User.findByUsername('admin'))
            replenishmentTaskService.patch(o.taskIds[0], SHORT_2_DAMAGED + [outboundContainerId: s.tote])
            st.setRollbackOnly()
        }
        settle()

        then:
        events(r, 'pick.shortage', o.orderNumber).isEmpty()
        replenTask(o.taskIds[0]).status == 'PICKING'
    }
}
