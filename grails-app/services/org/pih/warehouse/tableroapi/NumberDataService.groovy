package org.pih.warehouse.tableroapi

import org.joda.time.LocalDate
import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.tablero.NumberData

class NumberDataService {

    NumberData getInventoryByLotAndBin(def location) {
        Date tomorrow = LocalDate.now().plusDays(1).toDate();

        def binLocations = InventorySnapshot.executeQuery("select count(*) from InventorySnapshot i where i.location = :location and i.date = :tomorrow and i.quantityOnHand > 0",
                ['location': location, 'tomorrow': tomorrow]);

        return new NumberData("Lot and Bin", binLocations[0], "In stock", "/openboxes/report/showBinLocationReport?location.id=" + location.id + "&status=inStock")
    }

    NumberData getInProgressShipments(def user, def location) {
        def shipments = Requisition.executeQuery("select count(*) from Requisition r where r.origin = :location and r.status <> 'ISSUED' and r.createdBy = :user",
                ['location': location, 'user': user]);

        return new NumberData("Your in Progress Shipments", shipments[0], "Shipments", "/openboxes/stockMovement/list?receiptStatusCode=PENDING&origin.id=" + location.id + "&createdBy.id=" + user.id)
    }

    NumberData getInProgressPutaways(def user, def location) {
        def incompletePutaways = Order.executeQuery("select count(o.id) from Order o where o.orderTypeCode = 'TRANSFER_ORDER' AND o.status = 'PENDING' AND o.orderedBy = :user AND o.destination = :location",
                ['user': user, 'location': location]);

        return new NumberData("Your in Progress Putaways", incompletePutaways[0], "Putaways", "/openboxes/order/list/listForm?orderedById=" + user.id)
    }

    NumberData getReceivingBin(def location) {
        Date tomorrow = LocalDate.now().plusDays(1).toDate();

        def receivingBin = InventorySnapshot.executeQuery("""
            SELECT COUNT(distinct i.product.id) from InventorySnapshot i 
            LEFT JOIN i.location l 
            LEFT JOIN i.binLocation bl
            WHERE l = :location AND i.quantityOnHand > 0 
            AND i.date = :tomorrow AND bl.locationType.id = :locationType""",
                [
                        'location'    : location,
                        'tomorrow'    : tomorrow,
                        'locationType': Constants.RECEIVING_LOCATION_TYPE_ID,
                ]);

        return new NumberData("Products in Receiving Bin", receivingBin[0], "Products", "/openboxes/report/showBinLocationReport?status=inStock")
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
                ]);

        return new NumberData("Items Inventoried this Month", itemsInventoried[0], "Items");
    }

    NumberData getDefaultBin(def location) {
        Date tomorrow = LocalDate.now().plusDays(1).toDate();

        def productsInDefaultBin = InventorySnapshot.executeQuery("""
            SELECT COUNT(distinct i.product.id) FROM InventorySnapshot i
            WHERE i.location = :location
            AND i.quantityOnHand > 0
            AND i.binLocationName = 'DEFAULT'
            AND i.date = :tomorrow""",
                [
                        'location': location,
                        'tomorrow': tomorrow
                ]);

        return new NumberData("Products in Default Bin", productsInDefaultBin[0], "Products", "/openboxes/report/showBinLocationReport?location.id=" + location.id + "&status=inStock")
    }

      NumberData getProductWithNegativeInventory(def location) {

        Date tomorrow = LocalDate.now().plusDays(1).toDate();

         def productsWithNegativeInventory = InventorySnapshot.executeQuery("""
            SELECT COUNT(distinct i.product.id) FROM InventorySnapshot i
            WHERE i.location = :location
            AND i.quantityOnHand < 0
            AND i.date = :tomorrow
            """,
                [
                        'location': location,
                        'tomorrow': tomorrow
                ]);

        return new NumberData("Products with negative inventory", productsWithNegativeInventory[0], "Products", "/openboxes/report/showBinLocationReport?location.id=" + location.id)
    }
}
