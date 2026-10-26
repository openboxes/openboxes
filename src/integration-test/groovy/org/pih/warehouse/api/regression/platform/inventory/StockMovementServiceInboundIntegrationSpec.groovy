package org.pih.warehouse.api.regression.platform.inventory


import spock.lang.Ignore
import org.pih.warehouse.inventory.*
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.ShipOrderCommand
import org.pih.warehouse.order.ShipOrderItemCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentType

/**
 * Shipment-based (inbound) stock movements, from a supplier to a depot: created, read, edited line by line, packed,
 * documented, commented, sent, rolled back and deleted through the service.
 */
class StockMovementServiceInboundIntegrationSpec extends ServiceIntegrationSpec {

    StockMovementService stockMovementService

    Location depot
    Location supplier
    Product product

    void setup() {
        depot = newDepot('Inbound depot')
        supplier = newLocation('Supplier', Constants.SUPPLIER_LOCATION_TYPE_ID)
        product = newProduct()
    }

    /** Creates an inbound stock movement of 10 of the product, lot A; returns its id. */
    private String createInbound(String description = 'Inbound from supplier') {
        return asAdminAt(depot) {
            StockMovement command = new StockMovement(
                    origin: Location.get(supplier.id),
                    destination: Location.get(depot.id),
                    description: description,
                    dateRequested: new Date(),
                    requestedBy: User.findByUsername('admin'))
            command.lineItems = [new StockMovementItem(product: Product.get(product.id), lotNumber: 'LOT-A',
                    expirationDate: new Date() + 365, quantityRequested: 10, sortOrder: 0, stockMovement: command)]
            // The shipment itself; reading it back as a stock movement happens in a later session
            stockMovementService.createInboundShipment(command).id
        }
    }

    void 'an inbound stock movement is a pending shipment with its items, read back by id'() {
        when:
        String id = createInbound()
        Map read = asAdminAt(depot) {
            StockMovement stockMovement = stockMovementService.getStockMovement(id)
            List items = stockMovementService.getStockMovementItems(id, null, null, null, false)
            List page = stockMovementService.getStockMovementItems(id, null, 10, 0, false)
            StockMovementItem item = stockMovementService.getStockMovementItem(items[0].id, null, false, false)
            [
                    identifier   : stockMovement.identifier,
                    status       : stockMovement.shipment.currentStatus,
                    statusCode   : stockMovement.statusCode,
                    origin       : stockMovement.origin.id,
                    direction    : stockMovementService.getStockMovementDirection(stockMovement),
                    documents    : stockMovement.documents,
                    lots         : items*.lotNumber,
                    quantities   : items*.quantityRequested,
                    pageSize     : page.size(),
                    itemProduct  : item.product.id,
                    receiptItems : stockMovementService.getStockMovementReceiptItems(stockMovement),
            ]
        }

        then:
        read.identifier
        read.status == ShipmentStatusCode.PENDING
        read.statusCode == RequisitionStatus.PENDING.toString()
        read.origin == supplier.id
        read.direction == StockMovementDirection.INBOUND
        read.documents instanceof List
        read.lots == ['LOT-A']
        read.quantities == [10]
        read.pageSize == 1
        read.itemProduct == product.id
        !read.receiptItems
    }

    void 'an unknown id is not a stock movement'() {
        when:
        asAdminAt(depot) { stockMovementService.getStockMovement('no-such-stock-movement') }

        then:
        thrown(ObjectNotFoundException)
    }

    void 'the header and the line items of an inbound stock movement are edited'() {
        given:
        String id = createInbound()

        when: 'the description changes'
        String description = asAdminAt(depot) {
            StockMovement command = new StockMovement(id: id, description: 'Edited inbound', origin: Location.get(supplier.id),
                    destination: Location.get(depot.id), requestedBy: User.findByUsername('admin'), dateRequested: new Date())
            stockMovementService.updateShipmentBasedStockMovement(command).description
        }

        then:
        description == 'Edited inbound'

        when: 'the item is packed with a new quantity'
        Map edited = asAdminAt(depot) {
            StockMovement stockMovement = stockMovementService.getStockMovement(id)
            StockMovementItem existing = stockMovement.lineItems[0]
            existing.quantityRequested = 7
            existing.palletName = 'Pallet 1'
            existing.boxName = 'Box 1'
            stockMovement.lineItems = [existing]
            StockMovement updated = stockMovementService.updateItems(stockMovement, true)
            Shipment shipment = Shipment.get(id)
            ShipmentItem packed = shipment.shipmentItems.first()
            [
                    count     : shipment.shipmentItems.size(),
                    quantity  : packed.quantity,
                    box       : packed.container?.name,
                    pallet    : packed.container?.parentContainer?.name,
                    lineItems : updated.lineItems*.quantityRequested,
            ]
        }

        then:
        edited.count == 1
        edited.quantity == 7
        edited.box == 'Box 1'
        edited.pallet == 'Pallet 1'
        edited.lineItems == [7]
    }

    void 'an item set to zero is removed from the shipment'() {
        given:
        String id = createInbound()

        when:
        Integer remaining = asAdminAt(depot) {
            StockMovement stockMovement = stockMovementService.getStockMovement(id)
            StockMovementItem item = stockMovement.lineItems[0]
            item.quantityRequested = 0
            stockMovement.lineItems = [item]
            stockMovementService.updateItems(stockMovement, true)
            Shipment.get(id).shipmentItems.size()
        }

        then:
        remaining == 0
    }

    void 'an item that is not on the shipment cannot be edited'() {
        given:
        String id = createInbound()

        when:
        asAdminAt(depot) {
            StockMovement stockMovement = stockMovementService.getStockMovement(id)
            stockMovement.lineItems = [new StockMovementItem(id: 'not-an-item', product: Product.get(product.id), quantityRequested: 1,
                    stockMovement: stockMovement)]
            stockMovementService.updateItems(stockMovement, true)
        }

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('not-an-item')
    }

    void 'a line item is removed by its shipment item id'() {
        given:
        String id = createInbound()
        String shipmentItemId = asAdminAt(depot) { Shipment.get(id).shipmentItems.first().id }

        when:
        Integer remaining = asAdminAt(depot) {
            stockMovementService.removeStockMovementItem(shipmentItemId)
            Shipment.get(id).shipmentItems.size()
        }

        then:
        remaining == 0
        asAdminAt(depot) { ShipmentItem.get(shipmentItemId) } == null
    }

    void 'an inbound stock movement is sent, rolled back and deleted'() {
        given:
        String id = createInbound()

        when:
        // Shipped yesterday, so that the ship date is not after today in any time zone
        asAdminAt(depot) {
            Shipment shipment = Shipment.get(id)
            shipment.expectedShippingDate = new Date() - 1
            shipment.save(flush: true)
        }
        asAdminAt(depot) {
            StockMovement stockMovement = stockMovementService.getStockMovement(id)
            stockMovementService.transitionStockMovement(stockMovement, new JSONObject([status: 'ISSUED']))
        }
        // The current status is recomputed when the shipment is saved, so it is read in a later transaction
        ShipmentStatusCode sent = asAdminAt(depot) { Shipment.get(id).currentStatus }
        asAdminAt(depot) { stockMovementService.rollbackStockMovement(id) }
        ShipmentStatusCode rolledBack = asAdminAt(depot) { Shipment.get(id).currentStatus }
        asAdminAt(depot) { stockMovementService.deleteStockMovement(id) }

        then:
        sent == ShipmentStatusCode.SHIPPED
        rolledBack == ShipmentStatusCode.PENDING
        asAdminAt(depot) { Shipment.get(id) } == null
    }

    void 'the inbound list of a depot filters and sorts its stock movements'() {
        given:
        String description = uniqueName('Listed inbound')
        String id = createInbound(description)
        String identifier = asAdminAt(depot) { Shipment.get(id).shipmentNumber }
        String today = new Date().format('MM/dd/yyyy')

        when:
        Map<String, List<String>> results = asAdminAt(depot) {
            ['destination.name', 'origin.name', 'dateRequested', 'stocklist.name', 'identifier', 'name', null].collectEntries { String sort ->
                GrailsParameterMap params = new GrailsParameterMap([max: '10', offset: '0', sort: sort, order: 'asc',
                        createdAfter: today, createdBefore: (new Date() + 1).format('MM/dd/yyyy'),
                        shipmentType: Constants.DEFAULT_SHIPMENT_TYPE_ID], new MockHttpServletRequest())
                StockMovement criteria = new StockMovement(stockMovementDirection: StockMovementDirection.INBOUND,
                        destination: Location.get(depot.id), origin: Location.get(supplier.id), description: description,
                        receiptStatusCodes: [ShipmentStatusCode.PENDING],
                        createdBy: User.findByUsername('admin'))
                [(String.valueOf(sort)): stockMovementService.getStockMovements(criteria, params)*.id]
            }
        }
        List<String> byIdentifier = asAdminAt(depot) {
            GrailsParameterMap params = new GrailsParameterMap([:], new MockHttpServletRequest())
            stockMovementService.getStockMovements(new StockMovement(stockMovementDirection: StockMovementDirection.INBOUND,
                    identifier: identifier), params)*.id
        }

        then:
        results.values().every { it == [id] }
        byIdentifier == [id]
    }

    void 'the outbound list of a depot without requests is empty, whatever the filters'() {
        when:
        List all = asAdminAt(depot) {
            StockMovement criteria = new StockMovement(origin: Location.get(depot.id), destination: Location.get(depot.id),
                    identifier: 'none', name: 'none', description: 'none',
                    requisitionStatusCodes: [RequisitionStatus.PICKING], requestedBy: User.findByUsername('admin'),
                    createdBy: User.findByUsername('admin'), updatedBy: User.findByUsername('admin'),
                    sourceType: RequisitionSourceType.ELECTRONIC, receiptStatusCodes: [ShipmentStatusCode.PENDING])
            stockMovementService.getOutboundStockMovements(criteria, [max: 5, offset: 0, createdAfter: new Date() - 1,
                                                                      createdBefore: new Date() + 1, sort: 'dateCreated', order: 'desc'])
        }
        List fromDepot = asAdminAt(depot) {
            stockMovementService.getOutboundStockMovements(new StockMovement(origin: Location.get(depot.id)), [max: 5, offset: 0])
        }

        then:
        all.empty
        fromDepot.empty
    }

    private String purchaseOrder() {
        return asAdminAt(depot) {
            Order order = new Order(orderType: OrderType.get('PURCHASE_ORDER'), name: 'Order for inbound', orderNumber: uniqueName('PO'),
                    origin: Location.get(supplier.id), destination: Location.get(depot.id), orderedBy: User.findByUsername('admin'),
                    dateOrdered: new Date())
            order.addToOrderItems(new OrderItem(product: Product.get(product.id), quantity: 10, quantityPerUom: 1, unitPrice: 1.0))
            order.save(failOnError: true, flush: true).id
        }
    }

    void 'a shipment is created from a purchase order, for the quantities to ship'() {
        given:
        String orderId = purchaseOrder()

        when:
        String shipmentId = asAdminAt(depot) {
            Order order = Order.get(orderId)
            ShipOrderCommand command = new ShipOrderCommand(order: order)
            command.shipOrderItems = [new ShipOrderItemCommand(orderItem: order.orderItems.first(), quantityToShip: 4)]
            stockMovementService.createInboundShipment(command).id
        }
        Map shipped = asAdminAt(depot) {
            Shipment shipment = Shipment.get(shipmentId)
            StockMovement stockMovement = stockMovementService.getStockMovement(shipmentId)
            [
                    quantities: shipment.shipmentItems*.quantity,
                    linked    : shipment.shipmentItems.first().orderItems*.order*.id,
                    fromOrder : stockMovement.isFromOrder,
                    name      : shipment.name,
            ]
        }

        then:
        shipped.quantities == [4]
        shipped.linked == [orderId]
        shipped.fromOrder
        shipped.name == 'Order for inbound'

        when:
        asAdminAt(depot) {
            Order order = Order.get(orderId)
            ShipOrderCommand command = new ShipOrderCommand(order: order)
            command.shipOrderItems = [new ShipOrderItemCommand(orderItem: order.orderItems.first(), quantityToShip: 0)]
            stockMovementService.createInboundShipment(command)
        }

        then:
        grails.validation.ValidationException e = thrown()
        e.message.contains('Invalid shipment')
    }
}
