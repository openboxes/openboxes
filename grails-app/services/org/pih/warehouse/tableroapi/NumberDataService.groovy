package org.pih.warehouse.tableroapi

import org.joda.time.LocalDate
import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.tablero.NumberData

class NumberDataService {

    List<NumberData> getListNumberData(def user, def location) {
        Date tomorrow = LocalDate.now().plusDays(1).toDate();
        Date firstOfMonth = LocalDate.now().withDayOfMonth(1).toDate();

        def binLocations = InventorySnapshot.executeQuery("select count(*) from InventorySnapshot i where i.location = :location and i.date = :tomorrow and i.quantityOnHand > 0",
                ['location': location, 'tomorrow': tomorrow]);

        def shipments = Requisition.executeQuery("select count(*) from Requisition r where r.origin = :location and r.status <> 'ISSUED' and r.createdBy = :user",
                ['location': location, 'user': user]);

        def incompletePutaways = Order.executeQuery("select count(o.id) from Order o where o.orderTypeCode = 'TRANSFER_ORDER' AND o.status = 'PENDING' AND o.orderedBy = :user AND o.destination = :location",
                ['user': user, 'location': location]);

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

        def itemsInventoried = TransactionEntry.executeQuery("""
            SELECT COUNT(distinct ii.product.id) from TransactionEntry te
            INNER JOIN te.inventoryItem ii
            INNER JOIN te.transaction t
            INNER JOIN t.inventory i
            WHERE i = :location
            AND t.transactionType.id = 11
            AND t.transactionDate >= :firstOfMonth""",
                [
                        'location'    : location,
                        'firstOfMonth': firstOfMonth,
                ]);

        List<NumberData> numberDataList = [
                new NumberData("Lot and Bin", binLocations[0], "In stock", 1, "/openboxes/report/showBinLocationReport?location.id=" + location.id + "&status=inStock"),

                new NumberData("Products in Receiving Bin", receivingBin[0], "Products", 2, "/openboxes/report/showBinLocationReport?status=inStock"),

                new NumberData("Your in Progress Shipments", shipments[0], "Shipments", 3, "/openboxes/stockMovement/list?receiptStatusCode=PENDING&origin.id=" + location.id + "&createdBy.id=" + user.id),

                new NumberData("Your in Progress Putaways", incompletePutaways[0], "Putaways", 4, "/openboxes/order/list/listForm?orderedById=" + user.id),

                new NumberData("Items Inventoried this Month", itemsInventoried[0], "Items", 5),
        ] as List<NumberData>

        return numberDataList;
    }
}
