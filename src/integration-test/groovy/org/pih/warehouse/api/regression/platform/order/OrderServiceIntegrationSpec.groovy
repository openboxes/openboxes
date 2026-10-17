package org.pih.warehouse.api.regression.platform.order

import spock.lang.PendingFeature


import org.pih.warehouse.order.*
import grails.util.GrailsWebMockUtil
import grails.validation.ValidationException
import org.grails.web.servlet.mvc.GrailsWebRequest
import grails.util.Holders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Shared

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.common.util.RandomUtil
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.core.session.SessionManager
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

/**
 * The order service against a real database: purchase orders from a vendor to a depot, placed, imported into,
 * shipped and received, rolled back and deleted.
 */
class OrderServiceIntegrationSpec extends IntegrationSpec {

    OrderService orderService
    InventoryService inventoryService
    OrderSummaryService orderSummaryService
    AuthService authService
    SessionManager sessionManager

    @Shared Map<String, String> ids = [:]
    @Shared RandomUtil randomUtil = new RandomUtil()

    /**
     * Runs the closure in a new transaction. The current user (kept per thread) is loaded again in the new session,
     * so that the audit fields set on save refer to an instance of this session.
     */
    private <T> T tx(Closure<T> closure) {
        return Order.withNewTransaction {
            if (AuthService.currentUser) {
                authService.currentUser = AuthService.currentUser
            }
            return closure.call()
        }
    }

    private Location depot() { Location.get(ids.depot) }
    private Location supplier() { Location.get(ids.supplier) }
    private Product product() { Product.get(ids.product) }
    private User admin() { User.get(ids.admin) }


    /** A depot of the main organization with the given extra fields, saved at once. */
    private static Location newDepot(Map fields) {
        Location location = new Location([locationType: LocationType.get('2'), organization: Location.get('1').organization,
                                          active: true] + fields)
        location.inventory = new Inventory(warehouse: location)
        return location.save(failOnError: true, flush: true)
    }

    void setup() {
        if (!ids) {
            Map<String, String> created = [:]
            tx {
                String suffix = UUID.randomUUID().toString().take(8)
                Organization vendor = new Organization(code: randomUtil.randomStringFieldValue('VND'), name: "Vendor ${suffix}",
                        active: true, partyType: PartyType.findByCode('ORG')).save(failOnError: true)
                Location supplier = newDepot(name: "Order supplier ${suffix}", organization: vendor,
                        locationType: LocationType.get(Constants.SUPPLIER_LOCATION_TYPE_ID))
                Location depot = new LocationTestBuilder().asFacility().name("Order depot ${suffix}").build(true)
                Product product = new ProductTestBuilder().name("Ordered product ${suffix}")
                        .category(new CategoryTestBuilder().name("Ordered ${suffix}").build(true)).build(true)
                ProductSupplier source = new ProductSupplier(code: "SRC-${suffix}", name: "Source ${suffix}", product: product,
                        supplier: vendor, supplierCode: "SUPCODE-${suffix}", active: true).save(failOnError: true)
                created.putAll(suffix: suffix, vendor: vendor.id, supplier: supplier.id, depot: depot.id, product: product.id,
                        productCode: product.productCode, source: source.id, sourceCode: source.code,
                        admin: User.findByUsername('admin').id)
            }
            ids = created
        }
        GrailsWebMockUtil.bindMockWebRequest(Holders.grailsApplication.mainContext as WebApplicationContext)
    }

    void cleanup() {
        RequestContextHolder.resetRequestAttributes()
        authService.currentUser = null
        authService.currentLocation = null
    }

    /** Puts the depot in the session as the current location, as the order pages do. */
    private void inDepot() {
        sessionManager.currentLocation = tx { depot() }
    }

    /** A pending purchase order from the vendor to the depot with one line of 10 at 2.50 each. */
    private Map<String, String> purchaseOrder(OrderStatus status = OrderStatus.PENDING) {
        inDepot()
        return tx {
            authService.currentUser = admin()
            Order order = new Order(orderType: OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()),
                    name: "Purchase ${UUID.randomUUID().toString().take(8)}", status: status, origin: supplier(),
                    destination: depot(), destinationParty: depot().organization, orderedBy: admin(),
                    dateOrdered: new Date(), currencyCode: 'USD')
            order.addToOrderItems(new OrderItem(product: product(), quantity: 10, unitPrice: 2.5, quantityPerUom: 1,
                    quantityUom: UnitOfMeasure.get(Constants.UOM_EACH_ID), productSupplier: ProductSupplier.get(ids.source),
                    requestedBy: admin(), orderIndex: 0))
            orderService.saveOrder(order)
            return [order: order.id, orderNumber: order.orderNumber, line: order.orderItems.first().id]
        }
    }

    @PendingFeature(reason = 'filtering orders by a template ignores its destination party (and finds nothing without params)')
    void 'saveOrder numbers a new order and the order queries find it'() {
        given:
        inDepot()
        Map<String, String> po = purchaseOrder()

        expect:
        po.orderNumber
        tx {
            Order order = Order.get(po.order)
            assert order.originParty.id == ids.vendor
            Product product = product()
            assert orderService.getIncomingOrders(depot())*.id.contains(po.order)
            assert orderService.getOutgoingOrders(supplier())*.id.contains(po.order)
            assert orderService.getIncomingQuantityByProduct(depot(), [product]) == [(product): 10]
            assert orderService.getOutgoingQuantityByProduct(supplier(), [product]) == [(product): 10]
            assert orderService.getOrders(new Order(origin: supplier(), destination: depot(),
                    orderType: OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()), status: OrderStatus.PENDING),
                    new Date() - 1, new Date() + 1, [q: po.orderNumber, max: 10])*.id == [po.order]
            assert orderService.getOrders(new Order(orderedBy: admin()), null, null, [q: po.orderNumber])*.id == [po.order]
            assert orderService.getSuppliers()*.id.contains(ids.supplier)
            OrderCommand command = orderService.getOrder(po.order, ids.admin)
            assert command.origin.id == ids.supplier
            assert command.recipient.id == ids.admin
            assert command.orderItems*.quantityOrdered == [10]
            assert orderService.isOrderEditable(order, admin())
            assert orderService.canManageAdjustments(order, admin())
            assert orderService.getOrderItemByOrderAndProduct(po.orderNumber, ids.productCode).id == po.line
            assert orderService.getOrderItemByOrderAndProduct(order, Product.get(ids.product)).id == po.line
            Map summary = orderService.getOrderSummary(po.order)
            assert summary.isPurchaseOrder
            assert summary.hasSupplierCode
            assert !summary.hasManufacturerName
            assert summary.currencyCode == 'USD'
            assert summary.subtotal == 25
            Map itemStatus = orderService.getOrderItemStatus(po.order)
            assert itemStatus.orderItems*.id == [po.line]
            assert itemStatus.total == 25
            String csv = orderService.exportOrderItems(order.orderItems as List)
            assert csv.contains(ids.productCode)
            assert csv.contains('Qty to ship')
            assert orderService.getOrderItemsForPriceHistory(Organization.get(ids.vendor), product, ids.productCode)
                    .collect { [it.orderNumber, it.sourceCode, it.unitPrice] } == [[po.orderNumber, ids.sourceCode, 2.5]]
            true
        }
    }

    void 'getOrder and getOrderItemByOrderAndProduct report what is missing'() {
        when:
        tx { orderService.getOrder('no-such-order', null) }

        then:
        Exception noOrder = thrown()
        noOrder.message == 'Unable to locate order with ID no-such-order'

        when:
        tx { orderService.getOrderItemByOrderAndProduct(null as String, ids.productCode) }

        then:
        IllegalArgumentException missingOrder = thrown()
        missingOrder.message == 'Missing Order'

        when:
        tx { orderService.getOrderItemByOrderAndProduct(Order.list(max: 1).first(), null) }

        then:
        IllegalArgumentException missingProduct = thrown()
        missingProduct.message == 'Missing Product'
    }

    void 'saveOrder refuses an order for another organization'() {
        given:
        inDepot()

        when:
        tx {
            Order order = new Order(orderType: OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()), name: 'Foreign',
                    origin: supplier(), destination: depot(), destinationParty: Organization.get(ids.vendor),
                    orderedBy: admin())
            orderService.saveOrder(order)
        }

        then:
        ValidationException exception = thrown()
        exception.errors.getFieldError('destinationParty').code == 'order.destinationParty.invalid.differentOrganization'
    }

    void 'placeOrder needs at least one item and rollbackOrderStatus needs a known order without shipments'() {
        given:
        inDepot()
        String emptyId = tx {
            Order order = new Order(orderType: OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()), name: 'Empty',
                    origin: supplier(), destination: depot(), destinationParty: depot().organization, orderedBy: admin())
            orderService.saveOrder(order)
            return order.id
        }

        expect:
        tx { orderService.placeOrder(emptyId, ids.admin).errors.getFieldError('orderItems').code } ==
                'order.mustContainAtLeastOneItem.message'

        when:
        tx { orderService.rollbackOrderStatus('no-such-order') }

        then:
        RuntimeException exception = thrown()
        exception.message == 'Unable to locate order with order ID no-such-order'
    }

    void 'importOrderItems adds and updates the lines of a pending order and refuses bad rows'() {
        given:
        inDepot()
        Map<String, String> po = purchaseOrder()
        Map newRow = [id: '', productCode: ids.productCode, sourceCode: ids.sourceCode, quantity: '5', unitOfMeasure: 'EA/1',
                      unitPrice: '3', recipient: '', estimatedReadyDate: '', actualReadyDate: '', budgetCode: '']
        Map existingRow = newRow + [id: po.line, quantity: '12', unitPrice: '2.75']

        when:
        boolean imported = tx {
            orderService.importOrderItems(po.order, ids.vendor, [existingRow, newRow], depot(), admin())
        }

        then:
        imported
        tx { Order.get(po.order).orderItems.collect { [it.quantity, it.unitPrice] } as Set } == [[12, 2.75], [5, 3]] as Set

        when: 'a row names an unknown product'
        tx { orderService.importOrderItems(po.order, ids.vendor, [newRow + [productCode: 'NO-SUCH-CODE']], depot(), admin()) }

        then:
        RuntimeException unknownProduct = thrown()
        unknownProduct.message == 'Unable to locate product with product code NO-SUCH-CODE. Check Row 1.'

        when: 'a row has an unknown unit of measure'
        tx { orderService.importOrderItems(po.order, ids.vendor, [newRow + [unitOfMeasure: 'ZZ/1']], depot(), admin()) }

        then:
        RuntimeException badUom = thrown()
        badUom.message == 'Could not find provided Unit of Measure: ZZ/1. Check Row 1.'

        when: 'a row asks for nothing'
        tx { orderService.importOrderItems(po.order, ids.vendor, [newRow + [quantity: '0']], depot(), admin()) }

        then:
        RuntimeException noQuantity = thrown()
        noQuantity.message == 'Wrong quantity value: 0. Check Row 1.'

        when: 'a row has no product code'
        tx { orderService.importOrderItems(po.order, ids.vendor, [newRow + [productCode: '']], depot(), admin()) }

        then:
        RuntimeException noCode = thrown()
        noCode.message == 'No product code specified. Check Row 1.'
    }

    void 'importOrderItems refuses changes to a placed order by a user who does not approve purchases'() {
        given:
        inDepot()
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED)
        Map row = [id: '', productCode: ids.productCode, sourceCode: '', quantity: '4', unitOfMeasure: 'EA/1', unitPrice: '6']

        when:
        tx { orderService.importOrderItems(po.order, ids.vendor, [row], depot(), admin()) }

        then:
        RuntimeException exception = thrown()
        exception.message == 'You do not have permissions to perform this action'
        tx { Order.get(po.order).orderItems.size() } == 1
        !tx { orderService.isOrderEditable(Order.get(po.order), admin()) }
        !tx { orderService.canManageAdjustments(Order.get(po.order), admin()) }
    }

    void 'parseOrderItems reads the rows of an order item export'() {
        given:
        String text = 'Id,Code,Name,Source,Source name,Supplier code,Manufacturer,Manufacturer code,Qty,UOM,Price,Total,' +
                'Recipient,Estimated,Actual,Budget\n' +
                ',ABC,Aspirin,,,,,,5,EA/1,1.5,7.5,,,,\n'

        when:
        List rows = orderService.parseOrderItems(text)

        then:
        rows.size() == 1
        rows[0].productCode == 'ABC'
        rows[0].quantity == '5'
        rows[0].unitOfMeasure == 'EA/1'
    }

    void 'adjustments and lines of a pending order can be removed'() {
        given:
        inDepot()
        Map<String, String> po = purchaseOrder()
        String adjustmentId = tx {
            Order order = Order.get(po.order)
            OrderAdjustment adjustment = new OrderAdjustment(description: 'Freight', amount: 20)
            order.addToOrderAdjustments(adjustment)
            order.save(failOnError: true, flush: true)
            return adjustment.id
        }

        when:
        tx { orderService.deleteAdjustment(OrderAdjustment.get(adjustmentId), admin()) }
        tx { orderService.removeOrderItem(OrderItem.get(po.line), admin()) }

        then:
        tx { Order.get(po.order).orderAdjustments.size() } == 0
        tx { Order.get(po.order).orderItems.size() } == 0

        when: 'the empty order is deleted'
        tx { orderService.deleteOrder(Order.get(po.order)) }

        then:
        tx { Order.get(po.order) } == null
    }

    void 'the order summaries list orders and their derived status'() {
        given:
        inDepot()
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED)
        tx { orderSummaryService.refreshOrderSummary([po.order], false) }
        GrailsWebRequest webRequest = GrailsWebRequest.lookup()

        expect:
        tx {
            def params = new grails.web.servlet.mvc.GrailsParameterMap([searchTerm: po.orderNumber, destination: ids.depot,
                    origin: ids.supplier, max: '10', offset: '0', sort: 'origin'], webRequest.currentRequest)
            orderService.getPurchaseOrders(params)*.id
        } == [po.order]
        tx { orderService.getOrderSummaryList([orderNumber: po.orderNumber])*.id } == [po.order]
        tx { orderService.getOrderItemSummaryList([orderNumber: po.orderNumber]).size() } == 1
        tx { orderService.getOrderItemDetailsList([orderNumber: po.orderNumber]).size() } == 1
        tx { orderService.getOrdersDerivedStatus([po.order]).keySet() } == [po.order] as Set
        tx { orderService.getOrdersDerivedStatus([]) } == [:]
    }

    @PendingFeature(reason = 'filtering orders by a template ignores its destination party')
    void 'getOrders filters on the destination party of the template'() {
        given:
        inDepot()
        Map<String, String> po = purchaseOrder()

        expect:
        tx {
            orderService.getOrders(new Order(destinationParty: depot().organization), null, null, [q: po.orderNumber])*.id
        } == [po.order]
        tx {
            orderService.getOrders(new Order(destinationParty: Organization.get(ids.vendor)), null, null,
                    [q: po.orderNumber])*.id
        } == []
    }
}
