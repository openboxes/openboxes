package org.pih.warehouse.api.regression.obaf.sortation

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.PendingFeature

import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.api.regression.obaf.support.QuartzRunner
import org.pih.warehouse.jobs.AutomaticReceiptJob
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.inboundSortation.DemandService
import org.pih.warehouse.inboundSortation.InboundSortationService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayTask
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment

/**
 * Inbound sortation and cross-dock on a real database: an inbound shipment received into the
 * facility's INBOUND_SORTATION bin at an AUTOMATED_PUTAWAY_CREATION facility gets putaway orders
 * from the strategy chain wired in resources.groovy (crossDockPutawayStrategy, then
 * directedPutawayStrategy): the part a backorder still needs goes to the cross-dock lane of the
 * backorder's delivery type, the rest to the product's preferred bin; what no strategy places is
 * reported on the shipment. Also DemandService (the unmet-demand calculation behind the
 * deprecated demand-based CrossDockingStrategy) and the cross-dock putaway helpers.
 *
 * The receipt is made with ReceiptService.receiveInboundShipment(shipment, forceAutoReceipt=true)
 * (the manual "auto-receive" button); ShipmentEventService then creates the putaways after commit.
 */
class InboundSortationIntegrationSpec extends ObafTaskIntegrationSpec {

    @Autowired
    ReceiptService receiptService

    @Autowired
    InboundSortationService inboundSortationService

    @Autowired
    DemandService demandService

    static class Site {
        String dc, sort, stor, lane, cart, product
    }

    /** AUTOMATED_PUTAWAY_CREATION depot: sortation bin, storage bin (preferred), LOCAL_DELIVERY cross-dock lane. */
    Site site(String prefix, Map opts = [:]) {
        Site s = new Site()
        s.dc = depot(prefix, [ActivityCode.AUTOMATED_PUTAWAY_CREATION, ActivityCode.AUTO_RECEIVING] + (opts.depotExtra ?: []))
        s.sort = bin(s.dc, 'SORT', [ActivityCode.INBOUND_SORTATION, ActivityCode.RECEIVE_STOCK], false)
        s.stor = bin(s.dc, 'STOR')
        s.lane = bin(s.dc, 'LANE', [ActivityCode.CROSS_DOCKING, ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY], opts.lanePickable != false)
        if (opts.cart) {
            s.cart = bin(s.dc, 'CART', [ActivityCode.PUTAWAY_CART, ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY])
        }
        s.product = product(prefix)
        if (opts.preferred != false) {
            preferredBin(s.dc, s.product, s.stor)
        }
        return s
    }

    /**
     * A backorder (outbound order from the depot) whose single line of {@code quantity} is backordered
     * (only a backordered line is cross-docked); returns its request number.
     */
    String backorder(Site s, int quantity, DeliveryTypeCode deliveryType = DeliveryTypeCode.LOCAL_DELIVERY) {
        String req = requisition(s.dc, [[productId: s.product, quantity: quantity, quantityBackordered: quantity,
                                          backorderedReasonCode: ReasonCode.BACKORDER.toString()]], [deliveryTypeCode: deliveryType])
        return tx { org.pih.warehouse.requisition.Requisition.get(req).requestNumber } as String
    }

    /**
     * Receives the shipment the way production does it unattended: AutomaticReceiptJob fired for
     * the shipment through the running scheduler (the depot supports AUTO_RECEIVING), which receives
     * it as the system user; ShipmentEventService then creates the putaways after commit.
     */
    void receive(String shipmentId) {
        QuartzRunner.Run run = QuartzRunner.fire(scheduler(), AutomaticReceiptJob, [shipmentId: shipmentId])
        assert run.exception == null
    }

    /** The putaway order items of the depot as [from, to, container, quantity, comment], one ORDER each. */
    List<List> putaways(String dc) {
        return tx {
            Order.findAllByOriginAndOrderType(Location.get(dc), OrderType.findByCode(Constants.PUTAWAY_ORDER)).collect { Order o ->
                assert o.orderItems.size() == 1
                OrderItem i = o.orderItems.first()
                [prefixOf(i.originBinLocation?.name), prefixOf(i.destinationBinLocation?.name), prefixOf(i.containerLocation?.name),
                 i.quantity as Integer, i.description]
            }.sort { a, b -> a[1] <=> b[1] ?: a[3] <=> b[3] }
        } as List<List>
    }

    List<String> comments(String shipmentId) {
        return tx { Shipment.get(shipmentId).comments*.comment.sort() } as List<String>
    }

    // ------------------------------------------------------------------ directed

    void 'no backorder: the received quantity gets one putaway from the sortation bin to the preferred bin'() {
        given:
        Site s = site('IS-DIR')
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 10, lotNumber: 'L1']])

        when:
        receive(shipment)

        then:
        putaways(s.dc) == [['SORT', 'STOR', null, 10, 'Default Location']]
        comments(shipment) == []
        onHand(s.dc, s.product) == [SORT: 10]
    }

    void 'no preferred bin and no UNDEFINED default location: no putaway, the unplaced quantity is reported on the shipment'() {
        given:
        Site s = site('IS-NONE', [preferred: false])
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 7, lotNumber: 'L1']])
        String code = tx { Product.get(s.product).productCode } as String

        when:
        receive(shipment)

        then:
        putaways(s.dc) == []
        comments(shipment) == ["Could not assign a putaway location for 7 of product ${code}".toString()]
    }

    void 'a default putaway location that is UNDEFINED is used when there is no preferred bin'() {
        given:
        Site s = site('IS-UND', [preferred: false])
        String undefined = bin(s.dc, 'UNDEF', [ActivityCode.UNDEFINED_LOCATION])
        tx {
            new org.pih.warehouse.inventory.InventoryLevel(product: Product.get(s.product), inventory: Location.get(s.dc).inventory,
                    internalLocation: Location.get(undefined)).save(flush: true, failOnError: true)
        }
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 4, lotNumber: 'L1']])

        when:
        receive(shipment)

        then:
        putaways(s.dc) == [['SORT', 'UNDEF', null, 4, 'Default Internal Location']]
    }

    void 'a facility without AUTOMATED_PUTAWAY_CREATION, or a receipt outside a sortation bin, creates no putaway'() {
        given:
        String plain = depot('IS-OFF', [ActivityCode.AUTO_RECEIVING])
        bin(plain, 'SORT', [ActivityCode.INBOUND_SORTATION, ActivityCode.RECEIVE_STOCK], false)
        String p = product('IS-OFF')
        preferredBin(plain, p, bin(plain, 'STOR'))
        String s1 = inboundShipment(plain, [[productId: p, quantity: 3, lotNumber: 'L1']])

        String auto = depot('IS-NOSORT', [ActivityCode.AUTOMATED_PUTAWAY_CREATION, ActivityCode.AUTO_RECEIVING])
        String p2 = product('IS-NOSORT')
        preferredBin(auto, p2, bin(auto, 'STOR'))
        String s2 = inboundShipment(auto, [[productId: p2, quantity: 3, lotNumber: 'L1']])

        when:
        receive(s1)
        receive(s2)

        then: 'the second is received into a temporary receiving bin (no sortation bin), so it is not sorted'
        putaways(plain) == []
        putaways(auto) == []
        onHand(auto, p2).keySet().every { it != 'STOR' }
    }

    @PendingFeature(reason = 'ShipmentEventService.groovy:18-29 / InboundSortationService.groovy:25-30 the putaways are saved AFTER COMMIT in a new session, but Order.beforeInsert stamps createdBy = AuthService.currentUser, an instance of the caller\'s still-open session: on the manual auto-receive path (PartialReceivingController.autoreceive -> receiveInboundShipment(shipment, true), user bound for the whole request) the flush fails (two open sessions / a different object with the same id) and no putaway is created, only logged. The job path works because withSystemUser has already cleared the user')
    void 'manual auto-receive with the user bound (web request path) creates the putaways too'() {
        given:
        Site s = site('IS-WEB')
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 10, lotNumber: 'L1']])

        when: 'received through the service with the caller still bound as the current user'
        tx { receiptService.receiveInboundShipment(Shipment.get(shipment), true) }

        then:
        tx { Shipment.get(shipment).receipts.size() } == 1
        putaways(s.dc) == [['SORT', 'STOR', null, 10, 'Default Location']]
    }

    // ------------------------------------------------------------------ cross-dock

    void 'cross-dock: the quantity a backorder still needs goes to its delivery-type lane, the rest to the preferred bin'() {
        given:
        Site s = site('IS-XD')
        String number = backorder(s, 3)
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 10, lotNumber: 'L1', backorderReference: number]])

        when:
        receive(shipment)

        then:
        putaways(s.dc) == [['SORT', 'LANE', null, 3, 'Cross-Docking'], ['SORT', 'STOR', null, 7, 'Default Location']]
        comments(shipment) == []
    }

    void 'cross-dock: a backorder needing more than arrived takes all of it'() {
        given:
        Site s = site('IS-XDA')
        String number = backorder(s, 20)
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 6, lotNumber: 'L1', backorderReference: number]])

        when:
        receive(shipment)

        then:
        putaways(s.dc) == [['SORT', 'LANE', null, 6, 'Cross-Docking']]
    }

    void 'cross-dock: two shipment lines for two backorders each go to their lane'() {
        given:
        Site s = site('IS-XD2')
        String pickUpLane = bin(s.dc, 'PULANE', [ActivityCode.CROSS_DOCKING, ActivityCode.DELIVERY_TYPE_PICKUP])
        String local = backorder(s, 2)
        String pickUp = backorder(s, 3, DeliveryTypeCode.PICK_UP)
        String shipment = inboundShipment(s.dc, [
                [productId: s.product, quantity: 2, lotNumber: 'L1', backorderReference: local],
                [productId: s.product, quantity: 5, lotNumber: 'L1', backorderReference: pickUp]])

        when:
        receive(shipment)

        then:
        putaways(s.dc) == [['SORT', 'LANE', null, 2, 'Cross-Docking'], ['SORT', 'PULANE', null, 3, 'Cross-Docking'], ['SORT', 'STOR', null, 2, 'Default Location']]
    }

    void 'cross-dock declined: a backorder already covered, or no lane for its delivery type, sends everything to storage and says so on the shipment'() {
        given:
        Site s = site('IS-XDN')
        String shipToOrder = backorder(s, 3, DeliveryTypeCode.SHIP_TO)
        String code = tx { Product.get(s.product).productCode } as String
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 5, lotNumber: 'L1', backorderReference: shipToOrder]])
        String unknown = inboundShipment(s.dc, [[productId: s.product, quantity: 1, lotNumber: 'L2', backorderReference: 'NO-SUCH-ORDER']])

        when:
        receive(shipment)
        receive(unknown)

        then:
        putaways(s.dc) == [['SORT', 'STOR', null, 1, 'Default Location'], ['SORT', 'STOR', null, 5, 'Default Location']]
        comments(shipment).size() == 1
        comments(shipment)[0].startsWith("Product ${code} on backorder ${shipToOrder} was not cross-docked".toString())
        comments(unknown).size() == 1
        comments(unknown)[0].startsWith("Product ${code} on backorder NO-SUCH-ORDER was not cross-docked".toString())
    }

    void 'cross-dock lanes and carts are only found among PICK_STOCK locations (activeStorageLocations)'() {
        given:
        Site s = site('IS-XDP', [lanePickable: false])
        String number = backorder(s, 3)
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 5, lotNumber: 'L1', backorderReference: number]])

        when:
        receive(shipment)

        then:
        putaways(s.dc) == [['SORT', 'STOR', null, 5, 'Default Location']]
    }

    void 'PUTAWAY_CONTAINER_ASSIGNMENT_BY_DELIVERY_TYPE puts the cross-dock putaway on the cart of its delivery type, and the storage rest too (characterised)'() {
        given:
        Site s = site('IS-XDC', [cart: true, depotExtra: [ActivityCode.PUTAWAY_CONTAINER_ASSIGNMENT_BY_DELIVERY_TYPE]])
        String number = backorder(s, 3)
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 5, lotNumber: 'L1', backorderReference: number]])

        when:
        receive(shipment)

        then: 'the cross-dock strategy writes the backorder delivery type into the shared context, so the directed rest rides the same cart'
        putaways(s.dc) == [['SORT', 'LANE', 'CART', 3, 'Cross-Docking'], ['SORT', 'STOR', 'CART', 2, 'Default Location']]
    }

    void 'a cross-dock putaway task: CROSS_DOCK type, the lane delivery type, the backorder reference; the shipment counts as cross-docked only once it is completed'() {
        given:
        Site s = site('IS-XDT')
        String number = backorder(s, 3)
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 5, lotNumber: 'L1', backorderReference: number]])
        receive(shipment)
        String laneTask = tx {
            PutawayTask.findAllByFacility(Location.get(s.dc)).find { it.destination?.id == s.lane }.id
        } as String

        expect:
        putawayTask(laneTask).subMap(['putawayTypeCode', 'deliveryTypeCode', 'status']) == [putawayTypeCode: 'CROSS_DOCK', deliveryTypeCode: 'LOCAL_DELIVERY', status: 'PENDING']
        tx { PutawayTask.get(laneTask).backorderReferenceNumber } == number
        !tx { putawayTaskService.isCrossDockPutawayCompleted(Shipment.get(shipment)) }

        when:
        putawayPatch(laneTask, [action: 'complete', destination: s.lane, isDirectPutaway: true])

        then:
        tx { putawayTaskService.isCrossDockPutawayCompleted(Shipment.get(shipment)) }
        onHand(s.dc, s.product) == [SORT: 2, LANE: 3]
    }

    @PendingFeature(reason = 'InboundSortationService.groovy:67-79 rerunPutawayStrategy applies only the FIRST strategy result to the whole task: when the chain would split the quantity (cross-dock part + storage rest) the full task quantity is routed to the cross-dock lane, more than the backorder needs')
    void 'rerunning the strategy never routes more to the cross-dock lane than the backorder still needs'() {
        given: 'a 3-unit cross-dock task for a 3-unit backorder'
        Site s = site('IS-RR', [preferred: false])
        String number = backorder(s, 3)
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 3, lotNumber: 'L1', backorderReference: number]])
        receive(shipment)
        String laneTask = tx { PutawayTask.findAllByFacility(Location.get(s.dc)).find { it.destination?.id == s.lane }.id } as String

        and: 'the backorder shrinks to 2 and the product gets a preferred bin'
        tx {
            org.pih.warehouse.requisition.RequisitionItem ri = org.pih.warehouse.requisition.Requisition.findByRequestNumber(number).requisitionItems.first()
            ri.quantity = 2
            ri.quantityApproved = 2
            ri.save(flush: true, failOnError: true)
        }
        preferredBin(s.dc, s.product, s.stor)

        when:
        Map r = putawayPatch(laneTask, [action: 'rerunStrategy'])
        int toLane = tx {
            PutawayTask.findAllByFacility(Location.get(s.dc)).findAll {
                it.destination?.id == s.lane && it.status != org.pih.warehouse.api.PutawayTaskStatus.CANCELED
            }*.quantity.sum() ?: 0
        } as int

        then:
        r.error == null
        toLane <= 2
    }

    @PendingFeature(reason = 'ShipmentEventService.groovy (SHIPMENT_RECEIVED) creates putaways from shipment.receipt, which is receipts.first() of a SortedSet ordered by dateCreated, i.e. the OLDEST receipt: auto-receiving the rest of a partly received shipment re-sorts the first receipt and never sorts the new one')
    void 'auto-receiving the rest of a partly received shipment sorts the new receipt'() {
        given:
        Site s = site('IS-2R')
        String shipment = inboundShipment(s.dc, [[productId: s.product, quantity: 10, lotNumber: 'L1']])
        tx {
            PartialReceipt pr = receiptService.getPartialReceipt(shipment, '1')
            pr.partialReceiptItems.each { it.quantityReceiving = 4; it.binLocation = Location.get(s.sort); it.shouldSave = true }
            receiptService.saveAndCompletePartialReceipt(pr)
        }
        assert putaways(s.dc) == []

        when:
        receive(shipment)

        then:
        putaways(s.dc) == [['SORT', 'STOR', null, 6, 'Default Location']]
    }

    // ------------------------------------------------------------------ DemandService

    void 'unmet demand per delivery type: open orders minus open putaway tasks of that delivery type, by priority'() {
        given:
        Site s = site('IS-DM')
        backorder(s, 5)
        backorder(s, 3, DeliveryTypeCode.PICK_UP)
        backorder(s, 2)
        String picking = requisition(s.dc, [[productId: s.product, quantity: 9]], [status: RequisitionStatus.PICKING])
        stock(s.dc, s.product, [[s.sort, 4, 'L1', SOON]])
        putawayOrder(s.dc, s.product, 'L1', 4, s.sort, s.lane)

        when:
        Map unmet = tx { demandService.calculateUnmetDemand(Location.get(s.dc), Product.get(s.product)) } as Map

        then: 'LOCAL_DELIVERY 7 demanded, 4 already on their way to the lane; the PICKING order is no longer demand'
        unmet.collect { k, v -> [k.name(), v] } == [['PICK_UP', 3], ['LOCAL_DELIVERY', 3]]
    }

    void 'unmet demand: none without orders; never negative when more is on its way than ordered'() {
        given:
        Site s = site('IS-DM0')
        stock(s.dc, s.product, [[s.sort, 9, 'L1', SOON]])

        expect:
        tx { demandService.calculateUnmetDemand(Location.get(s.dc), Product.get(s.product)) } == [:]

        when:
        backorder(s, 2)
        putawayOrder(s.dc, s.product, 'L1', 9, s.sort, s.lane)

        then:
        tx { demandService.calculateUnmetDemand(Location.get(s.dc), Product.get(s.product)) }.collect { k, v -> [k.name(), v] } == [['LOCAL_DELIVERY', 0]]
    }

    @PendingFeature(reason = 'DemandService.groovy:70-72 counts as demand every requisition status whose sortOrder is below PICKING, which includes REJECTED (5) and the sortOrder-0 statuses: a rejected order is still unmet demand to cross-dock to')
    void 'a rejected order is not unmet demand'() {
        given:
        Site s = site('IS-DMR')
        requisition(s.dc, [[productId: s.product, quantity: 4]], [status: RequisitionStatus.REJECTED, deliveryTypeCode: DeliveryTypeCode.LOCAL_DELIVERY])

        expect:
        tx { demandService.calculateUnmetDemand(Location.get(s.dc), Product.get(s.product)) } == [:]
    }
}
