package org.pih.warehouse.dashboard

import org.joda.time.LocalDate
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.ShipmentStatus
import org.pih.warehouse.shipping.ShipmentStatusCode

class NumberDataService {

    def dataService

    NumberData getInventoryByLotAndBin(def location) {
        def binLocations = ProductAvailability.executeQuery("select count(*) from ProductAvailability pa where pa.location = :location and pa.quantityOnHand > 0",
                ['location': location])

        return new NumberData(binLocations[0], "/openboxes/report/showBinLocationReport?location.id=" + location.id + "&status=inStock")
    }

    NumberData getInProgressShipments(def user, def location) {
        def shipments = Requisition.executeQuery("select count(*) from Requisition r join r.shipments s where r.origin = :location and s.currentStatus = 'PENDING' and r.createdBy = :user",
                ['location': location, 'user': user]);

        return new NumberData(shipments[0], "/openboxes/stockMovement/list?receiptStatusCode=PENDING&origin.id=" + location.id + "&createdBy.id=" + user.id)
    }

    NumberData getInProgressPutaways(def user, def location) {
        def incompletePutaways = Order.executeQuery("select count(o.id) from Order o where o.orderType = :orderType AND o.status = 'PENDING' AND o.orderedBy = :user AND o.destination = :location",
                ['user': user, 'location': location, 'orderType': OrderType.findByCode(Constants.PUTAWAY_ORDER)])

        return new NumberData(incompletePutaways[0], "/openboxes/order/list?orderType=PUTAWAY_ORDER&status=PENDING&orderedBy=" + user.id)
    }

    NumberData getReceivingBin(def location) {
        def receivingBin = ProductAvailability.executeQuery("""
            SELECT COUNT(distinct pa.product.id) from ProductAvailability pa 
            LEFT JOIN pa.location l 
            LEFT JOIN pa.binLocation bl
            WHERE l = :location AND pa.quantityOnHand > 0 
            AND bl.locationType.id = :locationType""",
                [
                    'location'    : location,
                    'locationType': Constants.RECEIVING_LOCATION_TYPE_ID,
                ])

        return new NumberData(receivingBin[0], "/openboxes/report/showBinLocationReport?status=inStock")
    }

    NumberData getItemsInventoried(def location) {
        Date firstOfMonth = LocalDate.now().withDayOfMonth(1).toDate();

        def itemsInventoried = TransactionEntry.executeQuery("""
            SELECT COUNT(distinct ii.product.id) from TransactionEntry te
            INNER JOIN te.inventoryItem ii
            INNER JOIN te.transaction t
            WHERE t.inventory = :inventory
            AND t.transactionType.transactionCode = :transactionCode 
            AND t.transactionDate >= :firstOfMonth""",
                [
                        inventory      : location?.inventory,
                        transactionCode: TransactionCode.PRODUCT_INVENTORY,
                        firstOfMonth   : firstOfMonth,
                ])

        return new NumberData(itemsInventoried[0])
    }

    NumberData getDefaultBin(def location) {
        def productsInDefaultBin = ProductAvailability.executeQuery("""
            SELECT COUNT(distinct pa.product.id) FROM ProductAvailability pa
            WHERE pa.location = :location
            AND pa.quantityOnHand > 0
            AND pa.binLocation is null""",
                [
                    'location': location
                ])

        return new NumberData(productsInDefaultBin[0], "/openboxes/report/showBinLocationReport?location.id=" + location.id + "&status=inStock")
    }

    NumberData getProductWithNegativeInventory(def location) {

        def productsWithNegativeInventory = ProductAvailability.executeQuery("""
            SELECT pa.productCode, pa.product.name, pa.lotNumber, pa.binLocationName, pa.quantityOnHand
            FROM ProductAvailability pa
            WHERE pa.location = :location
            AND pa.quantityOnHand < 0
            ORDER BY pa.quantityOnHand ASC
            """,
                [
                        'location': location
                ])

        def numberOfProducts = productsWithNegativeInventory.size()

        String tooltipData = null

        if (numberOfProducts) {
            // Display only the first item in the tooltip
            // productsWithNegativeInventory[0][0] product code
            // productsWithNegativeInventory[0][1] Product name
            // productsWithNegativeInventory[0][2] Lot number
            // productsWithNegativeInventory[0][3] Bin location name
            // productsWithNegativeInventory[0][4] Quantity on hand
            tooltipData = """\
                Code: ${productsWithNegativeInventory[0][0]}
                Name: ${productsWithNegativeInventory[0][1]}
                Lot number: ${productsWithNegativeInventory[0][2]}
                Bin location: ${productsWithNegativeInventory[0][3]}
                Quantity: ${productsWithNegativeInventory[0][4]}"""
            tooltipData = tooltipData.stripIndent()
        }

        return new NumberData(numberOfProducts, "/openboxes/report/showBinLocationReport?location.id=" + location.id, tooltipData)
    }

    NumberData getExpiredProductsInStock(def location) {
        Date today = LocalDate.now().toDate()
        def expiredProductsInStock = ProductAvailability.executeQuery("""
            SELECT COUNT(distinct pa.inventoryItem) FROM ProductAvailability pa
            WHERE pa.location = :location
            AND pa.quantityOnHand > 0
            AND pa.inventoryItem.expirationDate < :today
            """,
                [
                    'location': location,
                    'today' : today,
                ])

        return new NumberData(expiredProductsInStock[0], "/openboxes/inventory/listExpiredStock?status=expired")
    }

    NumberData getOpenStockRequests(def location) {
        def openStockRequests = Requisition.executeQuery("""
            SELECT COUNT(distinct r.id) FROM Requisition r
            WHERE r.origin = :location
            AND r.sourceType = :sourceType
            AND r.status NOT IN (:statuses)
            """,
                [
                        'location': location,
                        'sourceType' : RequisitionSourceType.ELECTRONIC,
                        'statuses' : [RequisitionStatus.CREATED, RequisitionStatus.ISSUED, RequisitionStatus.CANCELED],
                ])

        return new NumberData(openStockRequests[0], "/openboxes/stockMovement/list?direction=OUTBOUND&sourceType=ELECTRONIC")
    }

    NumberData getInventoryValue (def location) {
        def inventoryValue = ProductAvailability.executeQuery("""select sum (pa.quantityOnHand * p.pricePerUnit) 
                from ProductAvailability as pa
                inner join pa.product as p 
                where pa.location = :location""",
                ['location': location])

        return new NumberData(inventoryValue[0])
    }

    NumberData getOpenPurchaseOrdersCount (Map params) {
        def openPurchaseOrdersCount = 0

        if (params.value) {
            def supplierIds = params.list('value').toList().collect { "'$it'" }.join(',')
            def pendingShipmentStatues = ShipmentStatusCode.listPending().collect { "'$it'" }.join(',')
            def pendingOrderStatuses = OrderStatus.listPending().collect { "'$it'" }.join(',')
            def openPurchaseOrders = dataService.executeQuery("""
                SELECT 
                    COUNT(DISTINCT o.id) AS openPurchaseOrdersCount
                FROM `order` AS o
                    LEFT OUTER JOIN order_item ON o.id = order_item.order_id
                    LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
                    LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
                    LEFT OUTER JOIN shipment ON shipment.id = shipment_item.shipment_id
                WHERE 
                    o.order_type_id = '${OrderTypeCode.PURCHASE_ORDER.name()}' AND 
                    o.origin_id in (${supplierIds}) AND 
                    (
                        (shipment.id IS NOT NULL AND shipment.current_status IN (${pendingShipmentStatues})) OR 
                        (shipment.id IS NULL AND o.status IN (${pendingOrderStatuses}))
                    )
            """)

            openPurchaseOrdersCount = openPurchaseOrders?.size() ? openPurchaseOrders[0].openPurchaseOrdersCount : 0
        }

        return new NumberData(openPurchaseOrdersCount)
    }
}
