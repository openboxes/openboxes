package org.pih.warehouse.regression.obaf.backorder

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import org.pih.warehouse.allocation.BackorderMatch
import org.pih.warehouse.allocation.BackorderMatchingService
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

import static org.pih.warehouse.regression.obaf.support.UnitAllocationData.*

/**
 * BackorderMatchingService: which inbound quantity covers which outbound demand line.
 *
 * remainingDemand(line) is 0 unless the line is backordered (only a backordered line is a
 * cross-dock line); for a backordered line it is max(0, ordered - canceled - allocated), i.e. the
 * whole uncovered line, not just the backordered part. match() walks inbound items in order and, for
 * each, the demand lines of the same product with the SMALLEST remaining demand first, never
 * matching more than either side holds. resolveCrossDockMatch sums the remaining demand of every
 * line of the product. Demand lines are the end of each revision/substitution chain; fully canceled lines carry
 * no demand. Allocated quantity comes from the line's picks (PicklistItem.findAllByRequisitionItem,
 * stubbed here).
 */
class BackorderMatchingServiceSpec extends Specification implements ServiceUnitTest<BackorderMatchingService>, DataTest {

    Map<RequisitionItem, List<PicklistItem>> picks = [:]
    Product product = product('BO')
    Product other = product('BO-OTHER')
    Requisition backorder

    void setup() {
        PicklistItem.metaClass.static.findAllByRequisitionItem = { RequisitionItem ri -> picks[ri] ?: [] }
        backorder = new Requisition(requestNumber: 'SO-1')
        backorder.id = nextId('req')
        backorder.requisitionItems = [] as LinkedHashSet
    }

    void cleanup() {
        GroovySystem.metaClassRegistry.removeMetaClass(PicklistItem)
    }

    RequisitionItem demand(int quantity, Map props = [:]) {
        RequisitionItem ri = new RequisitionItem([product: product, quantity: quantity, requisition: backorder] + props)
        ri.id = nextId('ri')
        ri.requisitionItems = [] as LinkedHashSet
        backorder.requisitionItems << ri
        return ri
    }

    RequisitionItem backordered(int quantity, int quantityBackordered, Map props = [:]) {
        return demand(quantity, [quantityBackordered: quantityBackordered, backorderedReasonCode: ReasonCode.BACKORDER.toString()] + props)
    }

    void allocated(RequisitionItem ri, int quantity) {
        picks[ri] = [new PicklistItem(quantity: quantity)]
    }

    ShipmentItem inbound(int quantity, Product p = product, Map props = [:]) {
        ShipmentItem si = new ShipmentItem([product: p, quantity: quantity] + props)
        si.id = nextId('si')
        return si
    }

    static List<List> summary(List<BackorderMatch> matches) {
        return matches.collect { [it.inboundItem?.id, it.demand?.id, it.quantityMatched] }
    }

    // ------------------------------------------------------------------ remainingDemand

    void 'remainingDemand: a line that is not backordered is no cross-dock demand, however much it still needs'() {
        given:
        RequisitionItem partly = demand(7)
        allocated(partly, 2)

        expect:
        service.remainingDemand(demand(7)) == 0
        service.remainingDemand(partly) == 0
    }

    void 'remainingDemand: a backordered line is its whole ordered quantity less everything allocated'() {
        given:
        RequisitionItem ri = backordered(10, 6)
        allocated(ri, 3)

        expect: 'ordered 10, allocated 3; the backordered quantity (6) is not added on top'
        service.remainingDemand(ri) == 7
    }

    void 'remainingDemand: a fully allocated backordered line has none left; over-allocation does not go negative'() {
        given:
        RequisitionItem exact = backordered(5, 5)
        allocated(exact, 5)
        RequisitionItem over = backordered(5, 5)
        allocated(over, 9)

        expect:
        service.remainingDemand(exact) == 0
        service.remainingDemand(over) == 0
    }

    void 'remainingDemand: canceled quantity is not demand'() {
        given:
        RequisitionItem ri = backordered(10, 6, [quantityCanceled: 4])

        expect:
        service.remainingDemand(ri) == 6
    }

    void 'remainingDemand: a short-picked pick (reason code) counts only what was picked as allocated'() {
        given:
        RequisitionItem ri = backordered(5, 5)
        picks[ri] = [new PicklistItem(quantity: 5, quantityPicked: 2, reasonCode: 'STOCKOUT')]

        expect:
        service.remainingDemand(ri) == 3
    }

    // ------------------------------------------------------------------ match

    void 'one inbound item fully covering one demand line'() {
        given:
        RequisitionItem d = backordered(5, 5)
        ShipmentItem in1 = inbound(5)

        expect:
        summary(service.match(backorder, [in1])) == [[in1.id, d.id, 5]]
    }

    void 'an inbound item larger than the demand matches only the demand'() {
        given:
        RequisitionItem d = backordered(4, 4)
        ShipmentItem in1 = inbound(10)

        expect:
        summary(service.match(backorder, [in1])) == [[in1.id, d.id, 4]]
    }

    void 'an inbound item smaller than the demand is matched in part'() {
        given:
        RequisitionItem d = backordered(10, 10)
        ShipmentItem in1 = inbound(3)

        expect:
        summary(service.match(backorder, [in1])) == [[in1.id, d.id, 3]]
    }

    void 'one inbound item is split across demand lines, smallest remaining demand first'() {
        given:
        RequisitionItem big = backordered(8, 8)
        RequisitionItem small = backordered(2, 2)
        RequisitionItem mid = backordered(5, 5)
        ShipmentItem in1 = inbound(9)

        expect:
        summary(service.match(backorder, [in1])) == [[in1.id, small.id, 2], [in1.id, mid.id, 5], [in1.id, big.id, 2]]
    }

    void 'several inbound items never cover the same demand twice (no double allocation)'() {
        given:
        RequisitionItem d = backordered(6, 6)
        ShipmentItem in1 = inbound(4)
        ShipmentItem in2 = inbound(4)
        ShipmentItem in3 = inbound(4)

        when:
        List<BackorderMatch> matches = service.match(backorder, [in1, in2, in3])

        then:
        summary(matches) == [[in1.id, d.id, 4], [in2.id, d.id, 2]]
        matches.sum { it.quantityMatched } == 6
    }

    void 'inbound items only match demand of the same product'() {
        given:
        RequisitionItem d = backordered(5, 5)
        ShipmentItem wrong = inbound(5, other)
        ShipmentItem right = inbound(2)

        expect:
        summary(service.match(backorder, [wrong, right])) == [[right.id, d.id, 2]]
    }

    void 'a line that is not backordered gets no inbound match, covered or not'() {
        given:
        RequisitionItem d = demand(5)
        allocated(d, 5)
        demand(4)

        expect:
        service.match(backorder, [inbound(5)]) == []
    }

    void 'a zero or null quantity inbound item matches nothing'() {
        given:
        backordered(5, 5)

        expect:
        service.match(backorder, [inbound(0), new ShipmentItem(product: product)]) == []
    }

    void 'a fully canceled line carries no demand'() {
        given:
        backordered(5, 5, [quantityCanceled: 5])

        expect:
        service.match(backorder, [inbound(5)]) == []
    }

    void 'a revised line is matched through its modification item, never the original row'() {
        given:
        RequisitionItem original = backordered(10, 10)
        RequisitionItem revised = new RequisitionItem(product: product, quantity: 6, requisition: backorder,
                parentRequisitionItem: original, quantityBackordered: 6, backorderedReasonCode: ReasonCode.BACKORDER.toString())
        revised.id = nextId('ri')
        backorder.requisitionItems << revised
        original.modificationItem = revised
        ShipmentItem in1 = inbound(20)

        expect:
        summary(service.match(backorder, [in1])) == [[in1.id, revised.id, 6]]
    }

    void 'a substituted line is matched through its substitution, which does not inherit the original backorder'() {
        given:
        RequisitionItem original = backordered(10, 10, [quantityCanceled: 10])
        RequisitionItem substitution = new RequisitionItem(product: other, quantity: 4, requisition: backorder,
                parentRequisitionItem: original, requisitionItemType: RequisitionItemType.SUBSTITUTION)
        substitution.id = nextId('ri')
        substitution.requisitionItems = [] as LinkedHashSet
        original.requisitionItems = [substitution] as LinkedHashSet
        original.substitutionItem = substitution
        backorder.requisitionItems << substitution
        ShipmentItem forOriginalProduct = inbound(10)
        ShipmentItem forSubstitute = inbound(10, other)

        expect: 'without a backorder of its own the substitution is no cross-dock demand, and the original is never matched'
        service.match(backorder, [forOriginalProduct, forSubstitute]) == []

        when: 'the substitution is backordered in its own right'
        substitution.quantityBackordered = 4
        substitution.backorderedReasonCode = ReasonCode.BACKORDER.toString()

        then:
        summary(service.match(backorder, [forOriginalProduct, forSubstitute])) == [[forSubstitute.id, substitution.id, 4]]
    }

    // ------------------------------------------------------------------ resolveCrossDockMatch

    void 'resolveCrossDockMatch with a hard link matches that line up to its remaining demand'() {
        given:
        RequisitionItem d = backordered(5, 5)

        when:
        BackorderMatch m = service.resolveCrossDockMatch(null, d, product, 8)

        then:
        m.demand == d
        m.quantityMatched == 5
    }

    void 'resolveCrossDockMatch through the soft reference sums the remaining demand of every backordered line of the product'() {
        given:
        Requisition.metaClass.static.findByRequestNumber = { String n -> n == 'SO-1' ? backorder : null }
        RequisitionItem big = backordered(9, 9)
        backordered(3, 3)
        demand(5)
        backordered(4, 4, [product: other])

        expect: '9 + 3 for the product; the line that is not backordered and the other product do not count'
        service.resolveCrossDockMatch('SO-1', null, product, 10).quantityMatched == 10
        service.resolveCrossDockMatch('SO-1', null, product, 20).quantityMatched == 12

        and: 'the match is recorded against the first line that still needs cover'
        service.resolveCrossDockMatch('SO-1', null, product, 10).demand == big

        cleanup:
        GroovySystem.metaClassRegistry.removeMetaClass(Requisition)
    }

    void 'resolveCrossDockMatch answers null when there is nothing to cross-dock'() {
        given:
        Requisition.metaClass.static.findByRequestNumber = { String n -> n == 'SO-1' ? backorder : null }
        RequisitionItem covered = demand(5)
        allocated(covered, 5)

        expect: 'no quantity, an unknown reference, no reference at all, or no demand left'
        service.resolveCrossDockMatch('SO-1', null, product, 0) == null
        service.resolveCrossDockMatch('SO-1', null, product, null) == null
        service.resolveCrossDockMatch('NOPE', null, product, 5) == null
        service.resolveCrossDockMatch(null, null, product, 5) == null
        service.resolveCrossDockMatch('SO-1', null, product, 5) == null
        service.resolveCrossDockMatch(null, covered, product, 5) == null

        cleanup:
        GroovySystem.metaClassRegistry.removeMetaClass(Requisition)
    }

    // ------------------------------------------------------------------ inbound item lookup

    void 'findInboundItemsForRequisition sees items linked either way, and nothing else'() {
        given:
        RequisitionItem d = backordered(5, 5)
        ShipmentItem soft = inbound(1, product, [backorderReference: 'SO-1'])
        ShipmentItem hard = inbound(1, product, [backorderReference: 'SO-1', backorderItem: d])
        ShipmentItem hardOnly = inbound(1, product, [backorderItem: d])
        ShipmentItem unrelated = inbound(1, product, [backorderReference: 'SO-2'])
        ShipmentItem plain = inbound(1)
        Shipment shipment = new Shipment(shipmentItems: [soft, hard, hardOnly, unrelated, plain] as LinkedHashSet)

        expect:
        service.findInboundItemsForRequisition(shipment, backorder) as Set == [soft, hard, hardOnly] as Set
    }
}
