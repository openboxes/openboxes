package org.pih.warehouse.api.regression.platform.pages.order

import spock.lang.PendingFeature



import io.restassured.response.Response

import org.pih.warehouse.api.regression.platform.spec.purchasing.PurchasingSpec
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustmentType
import org.pih.warehouse.order.OrderAdjustmentTypeCode
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.api.regression.platform.pages.ServerPages
import org.pih.warehouse.product.ProductSupplier

import static org.pih.warehouse.api.regression.platform.pages.ServerPages.page
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * The order server pages (OrderController, PurchaseOrderController, OrderAdjustmentTypeController): listing and
 * exporting purchase orders, their lines, adjustments, comments and the order detail tabs.
 */
class OrderPagesSpec extends PurchasingSpec {

    ServerPages pages

    @Override
    void setupData() {
        super.setupData()
        pages = new ServerPages(serverPort, cookie)
    }

    def <T> T withOrder(String id, Closure<T> closure) {
        return Order.withNewSession { closure(Order.get(id)) }
    }

    void 'the order page and its tabs show the order'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED, null, true)

        expect:
        page(pages.get("/order/show/${po.order}")).contains(po.orderNumber)
        page(pages.get("/order/print/${po.order}")).contains(po.orderNumber)
        page(pages.get("/order/itemDetails/${po.order}")).contains(product.productCode)
        page(pages.get("/order/orderAdjustments/${po.order}")).contains('Freight')
        page(pages.get("/order/orderSummary/${po.order}")) != null
        page(pages.get("/order/itemStatus/${po.order}")).contains(product.productCode)
        page(pages.get("/order/orderShipments/${po.order}")) != null
        page(pages.get("/order/orderInvoices/${po.order}")) != null
        page(pages.get("/order/orderDocuments/${po.order}")) != null
        page(pages.get("/order/orderComments/${po.order}")) != null
        pages.get("/order/getTotalPrice/${po.order}").body.asString() as BigDecimal == 125.0
        pages.get("/order/getTotalAdjustments/${po.order}").body.asString() as BigDecimal == 100.0

        and: 'unknown orders go back to the list'
        redirectPath(pages.get('/order/show/unknown-order')) == '/order/list'
        redirectPath(pages.get('/order/print/unknown-order')) == '/order/list'
        redirectPath(pages.get('/order/download/unknown-order')) == '/order/list'
        redirectPath(pages.get('/order/downloadOrderItems/unknown-order')) == '/order/list'
        redirectPath(pages.get('/order/addComment/unknown-order')) == '/order/list'
        redirectPath(pages.get('/order/addDocument/unknown-order')) == '/order/list'
        redirectPath(pages.get('/order/addAdjustment/unknown-order')) == '/order/list'
    }

    void 'the order and its lines download as csv'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED, null, true)

        when:
        Response order = pages.get("/order/download/${po.order}")
        Response items = pages.get("/order/downloadOrderItems/${po.order}")
        Response template = pages.get('/order/exportTemplate', ['order.id': po.order])
        Response noTemplate = pages.get('/order/exportTemplate', ['order.id': purchaseOrder().order])

        then:
        order.statusCode == 200
        order.body.asString().readLines()[0] == "PO Number,${po.orderNumber}"
        order.body.asString().contains(product.productCode)
        items.statusCode == 200
        items.body.asString().readLines()[1].startsWith("${po.line},${product.productCode}")
        template.statusCode == 200
        template.contentType.startsWith('text/csv')
        template.body.asString().contains(po.line)
        noTemplate.statusCode == 404
    }

    void 'the order line JSON lists the lines with their prices'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PENDING, null, true)

        when:
        List lines = pages.request().accept('application/json').get("/order/getOrderItems/${po.order}").jsonPath()
                .getList('')
        List adjustments = pages.request().accept('application/json')
                .get("/order/getOrderAdjustments/${po.order}").jsonPath().getList('')

        then:
        lines*.id == [po.line]
        lines[0].quantity == 10
        lines[0].canEdit == true
        lines[0].isOrderPending == true
        lines[0].orderItemStatusCode == 'PENDING'
        adjustments*.id == [po.adjustment]
        adjustments[0].description == 'Freight'
        adjustments[0].isCanceled == false
    }

    void 'order lines are added, canceled, restored and removed'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PENDING, null, true)

        expect:
        page(pages.get("/order/orderItemFormDialog/${po.line}")).contains(product.productCode)
        page(pages.get('/order/productSourceFormDialog', [productId: product.id, supplierId: vendor.id])) != null

        when:
        Response added = pages.post('/order/saveOrderItem', ['order.id': po.order, 'orderItem.id': '',
                'product.id': product.id, quantity: '4', quantityPerUom: '1', unitPrice: '3'])

        then:
        added.statusCode == 200
        added.body.asString() == 'Successfully added order item'
        withOrder(po.order) { it.orderItems.size() } == 2

        when: 'the existing line is edited'
        Response edited = pages.post('/order/saveOrderItem', ['order.id': po.order, 'orderItem.id': po.line,
                'product.id': product.id, quantity: '12'])

        then:
        edited.statusCode == 200
        OrderItem.withNewSession { OrderItem.get(po.line).quantity } == 12

        when:
        Response canceled = pages.post("/order/cancelOrderItem/${po.line}")

        then:
        canceled.body.asString() == 'Item canceled successfully'
        OrderItem.withNewSession { OrderItem.get(po.line).orderItemStatusCode } == OrderItemStatusCode.CANCELED

        when:
        Response restored = pages.post("/order/restoreOrderItem/${po.line}")

        then:
        restored.body.asString() == 'Item restored successfully'
        OrderItem.withNewSession { OrderItem.get(po.line).orderItemStatusCode } == OrderItemStatusCode.PENDING

        when:
        Response removed = pages.post("/order/removeOrderItem/${po.line}")

        then:
        removed.body.asString() == 'Successfully deleted order item'
        OrderItem.withNewSession { OrderItem.get(po.line) } == null

        expect: 'a blocked supplier is refused'
        pages.post('/order/saveOrderItem', ['order.id': po.order, 'orderItem.id': '', validationCode: 'BLOCK'])
                .statusCode == 500
    }

    void 'an empty order line import is refused'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PENDING)

        when:
        Response imported = pages.request().multiPart('fileContents', 'items.csv', new byte[0], 'text/csv')
                .post("/order/importOrderItems/${po.order}")

        then:
        redirectPath(imported) == "/order/show/${po.order}"
    }

    void 'a combined shipment needs something left to ship'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED)

        when:
        Response nothing = pages.get('/order/createCombinedShipment', [orderId: po.order])

        then:
        redirectPath(nothing) == "/order/show/${po.order}"
    }

    void 'the order summary lists'() {
        expect:
        page(pages.get('/order/orderSummaryList')) != null
        page(pages.get('/order/orderItemSummary')) != null
    }

    void 'the purchase order workflow pages'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PENDING, null, true)

        expect:
        page(pages.get('/purchaseOrder/create')) != null
        page(pages.get("/purchaseOrder/edit/${po.order}")).contains(po.orderNumber)
        page(pages.get("/purchaseOrder/addItems/${po.order}")).contains('Edit line items')
        redirectPath(pages.get('/purchaseOrder/edit/unknown-order')) == '/purchaseOrder/create'
        redirectPath(pages.get('/purchaseOrder/addItems/unknown-order')) == '/purchaseOrder/create'
        redirectPath(pages.post('/purchaseOrder/saveOrderDetails', [:])) == '/purchaseOrder/list'
    }

    void 'order adjustment types are created, edited and deleted'() {
        given:
        String name = randomUtil.randomStringFieldValue('Adjustment type')

        expect:
        page(pages.get('/orderAdjustmentType/list')) != null
        page(pages.get('/orderAdjustmentType/create')) != null
        redirectPath(pages.get('/orderAdjustmentType/index')) == '/orderAdjustmentType/list'

        when:
        Response saved = pages.post('/orderAdjustmentType/save', [name: name, code: OrderAdjustmentTypeCode.FEE.name()])
        String id = OrderAdjustmentType.withNewSession { OrderAdjustmentType.findByName(name)?.id }

        then:
        id
        redirectPath(saved) == "/orderAdjustmentType/edit/${id}"
        page(pages.get("/orderAdjustmentType/edit/${id}")).contains(name)

        when:
        Response updated = pages.post('/orderAdjustmentType/update', [id: id, name: "${name} 2"])

        then:
        redirectPath(updated) == '/orderAdjustmentType/list'
        OrderAdjustmentType.withNewSession { OrderAdjustmentType.get(id).name } == "${name} 2"

        when: 'invalid'
        String invalid = page(pages.post('/orderAdjustmentType/save', [name: name]))

        then:
        invalid != null

        when:
        Response deleted = pages.post('/orderAdjustmentType/delete', [id: id])

        then:
        redirectPath(deleted) == '/orderAdjustmentType/list'
        OrderAdjustmentType.withNewSession { OrderAdjustmentType.get(id) } == null
        redirectPath(pages.post('/orderAdjustmentType/delete', [id: 'unknown'])) == '/orderAdjustmentType/list'
        redirectPath(pages.post('/orderAdjustmentType/update', [id: 'unknown'])) == '/orderAdjustmentType/list'
    }

    void 'a product source is created from the order line form'() {
        given:
        String code = randomUtil.randomStringFieldValue('SRC')

        when:
        Response created = pages.post('/order/createProductSource', ['supplier.id': vendor.id,
                'product.id': product.id, sourceCode: code, supplierCode: 'VND-1'])
        String id = ProductSupplier.withNewSession { ProductSupplier.findByCode(code)?.id }

        then:
        created.statusCode == 200
        id
        created.body.asString() == id

        when: 'the same code again'
        Response duplicate = pages.post('/order/createProductSource', ['supplier.id': vendor.id,
                'product.id': product.id, sourceCode: code])

        then:
        duplicate.statusCode == 500
        duplicate.body.asString() == 'Product source with given code for your supplier already exists'
    }

    void 'a combined shipment is started for what is left to ship'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED, null, true)

        when:
        Response started = pages.get('/order/createCombinedShipment', [orderId: po.order])

        then:
        redirectPath(started).startsWith('/stockMovement/createCombinedShipments/')
        started.header('Location').contains('direction=INBOUND')
    }

    void 'order lines are imported from a csv file'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PENDING)
        String csv = 'id,code,name,source code,source name,supplier code,manufacturer,manufacturer code,quantity,' +
                'uom,cost,total,recipient,ready date,expected date,budget code\n' +
                ",${product.productCode},,,,,,,3,EA/1,2.00,,,,,\n"

        when:
        Response imported = pages.request().multiPart('fileContents', 'items.csv', csv.getBytes('UTF-8'), 'text/csv')
                .multiPart('supplierId', vendor.id).post("/order/importOrderItems/${po.order}")

        then:
        imported.statusCode == 200
        imported.body.asString() == 'Successfully added order items'
        withOrder(po.order) { Order order -> order.orderItems.collect { [it.product.id, it.quantity, it.unitPrice as double] } } ==
                [[product.id, 3, 2.0d]]
    }

    @PendingFeature(reason = 'the order item details page fails to render')
    void 'the order item details list shows the order lines'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED, null, true)

        expect:
        page(pages.get('/order/orderItemDetails', [orderNumber: po.orderNumber])).contains(po.orderNumber)
    }
}
