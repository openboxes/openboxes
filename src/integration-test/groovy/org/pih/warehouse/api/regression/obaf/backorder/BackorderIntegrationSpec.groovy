package org.pih.warehouse.api.regression.obaf.backorder

import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.allocation.BackorderMatch
import org.pih.warehouse.allocation.BackorderMatchingService
import org.pih.warehouse.allocation.BackorderService
import org.pih.warehouse.api.regression.obaf.support.ObafIntegrationSpec
import org.pih.warehouse.api.regression.obaf.support.QuartzRunner
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.jobs.AutomaticReceiptJob
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Backorders on a real database: an inbound shipment's sales-order references must resolve to an
 * order (BackorderService); what they cover is matched against the open backordered demand
 * (BackorderMatchingService). An AUTO_RECEIVING +
 * CROSS_DOCKING facility refuses to auto-receive a shipment whose reference is invalid, logging
 * why on the shipment. The release of the backorder itself: ObafJobsIntegrationSpec.
 */
class BackorderIntegrationSpec extends ObafIntegrationSpec {

    @Autowired
    BackorderService backorderService

    @Autowired
    BackorderMatchingService backorderMatchingService

    static final Date SOON = new Date() + 30

    Map salesOrder(String dc, String productId, int quantity, int backordered) {
        String so = requisition(dc, [[productId: productId, quantity: quantity, quantityBackordered: backordered,
                                      backorderedReasonCode: ReasonCode.BACKORDER.toString()]])
        return [id: so, number: tx { Requisition.get(so).requestNumber }]
    }

    List<String> validationErrors(String shipmentId) {
        return tx {
            Shipment s = Shipment.get(shipmentId)
            backorderService.validateBackorderReferences(s)
            s.errors.globalErrors*.code
        } as List<String>
    }

    void 'a shipment whose references match open demand validates'() {
        given:
        String dc = depot('BO-OK')
        String p = product('BOK')
        Map so = salesOrder(dc, p, 5, 5)
        String shipment = inboundShipment(dc, [[productId: p, quantity: 5, lotNumber: 'L', backorderReference: so.number]])

        expect:
        validationErrors(shipment) == []
    }

    void 'an unknown sales-order reference is rejected'() {
        given:
        String dc = depot('BO-404')
        String p = product('B404')
        String shipment = inboundShipment(dc, [[productId: p, quantity: 5, lotNumber: 'L', backorderReference: "NOPE-${uid()}".toString()]])

        expect:
        validationErrors(shipment) == ['backorder.notFound.message']
    }

    void 'an inbound line for a product the sales order does not want (or no longer needs) is accepted and matches nothing'() {
        given:
        String dc = depot('BO-NONE')
        String p = product('BNONE')
        String other = product('BOTHER')
        Map so = salesOrder(dc, p, 5, 5)
        String shipment = inboundShipment(dc, [[productId: other, quantity: 5, lotNumber: 'L', backorderReference: so.number]])

        expect: 'it is received like any other stock; the putaway strategy sends it to storage'
        validationErrors(shipment) == []
        tx {
            Requisition r = Requisition.get(so.id)
            backorderMatchingService.match(r, backorderMatchingService.findInboundItemsForRequisition(Shipment.get(shipment), r))
        } == []
    }

    void 'matching on real rows: two inbound lines never cover more than the demand'() {
        given:
        String dc = depot('BO-MATCH')
        String p = product('BMATCH')
        Map so = salesOrder(dc, p, 6, 6)
        String shipment = inboundShipment(dc, [[productId: p, quantity: 4, lotNumber: 'A', backorderReference: so.number],
                                               [productId: p, quantity: 4, lotNumber: 'B', backorderReference: so.number]])

        when:
        List<Integer> matched = tx {
            Requisition r = Requisition.get(so.id)
            List<BackorderMatch> matches = backorderMatchingService.match(r,
                    backorderMatchingService.findInboundItemsForRequisition(Shipment.get(shipment), r))
            matches*.quantityMatched
        } as List<Integer>

        then:
        matched.sum() == 6
        matched.size() == 2
        tx { backorderMatchingService.remainingDemand(Requisition.get(so.id).requisitionItems[0] as RequisitionItem) } == 6
    }

    void 'auto-receipt at a CROSS_DOCKING facility refuses a shipment with an invalid reference and says why on the shipment'() {
        given:
        String dc = depot('BO-AR', [ActivityCode.AUTO_RECEIVING, ActivityCode.CROSS_DOCKING])
        bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION])
        String p = product('BOAR')
        String badRef = "NOPE-${uid()}".toString()
        String shipment = inboundShipment(dc, [[productId: p, quantity: 3, lotNumber: 'L', backorderReference: badRef]])

        when:
        QuartzRunner.Run run = QuartzRunner.fire(scheduler(), AutomaticReceiptJob, [shipmentId: shipment])

        then:
        run.exception == null
        tx { Shipment.get(shipment).currentStatus } == ShipmentStatusCode.SHIPPED
        tx { Shipment.get(shipment).comments*.comment.any { it.contains(badRef) } }
    }
}
