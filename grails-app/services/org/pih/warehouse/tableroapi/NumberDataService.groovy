package org.pih.warehouse.tableroapi

import org.pih.warehouse.order.Order
import org.pih.warehouse.tablero.NumberData
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.inventory.InventorySnapshot

class NumberDataService {

    def dataService

    List<NumberData> getListNumberData(def user, def location){
        Date tomorrow = new Date() + 1;
        tomorrow.clearTime();

        def binLocations = InventorySnapshot.executeQuery("select count(*) from InventorySnapshot i where i.location=:location and i.date = :tomorrow and i.quantityOnHand > 0", ['location': location, 'tomorrow': tomorrow]);

        def shipments = Requisition.executeQuery("select count(*) from Requisition r where r.origin = :location and r.status <> 'ISSUED' and r.createdBy = :user", 
        ['location': location, 'user': user]);
        def incompletePutaways = Order.executeQuery("select count(o.id) from Order o where o.orderTypeCode = 'TRANSFER_ORDER' AND o.status = 'PENDING' AND o.orderedBy = :user AND o.destination = :location", ['user':user, 'location': location]);

        def receivingBin = InventorySnapshot.executeQuery("""
            SELECT COUNT(distinct i.product) from InventorySnapshot i 
            LEFT JOIN i.location l 
            WHERE l = :location AND i.quantityOnHand > 0 
            AND i.date = :tomorrow AND l.locationType = 'ff8081816482352b01648249e8cc0001'""",
            ['location': location, 'tomorrow': tomorrow]);

        def pending = dataService.executeQuery("select count(*) from shipment where shipment.current_status = 'PENDING'");
        def notCompleted = dataService.executeQuery("select count(*) from openboxes.order  where order.status != 'COMPLETED'");

        List<NumberData> numberDataList = [
            new NumberData("Lot and Bin", binLocations[0], 'In stock', 1, "/openboxes/report/showBinLocationReport?location.id=" + location.id + "&status=inStock"),

            new NumberData("Receiving Bin ",receivingBin[0], "Products", 2, "/openboxes/report/showBinLocationReport?status=inStock"),

            new NumberData("Your Shipments", shipments[0], "In Progress", 3, "/openboxes/stockMovement/list?receiptStatusCode=PENDING&origin.id=" + location.id + "&createdBy.id=" + user.id),

            new NumberData("Your Putaways",incompletePutaways[0], "In Progress", 4, "/openboxes/order/list/listForm?orderedById=" + user.id),

            new NumberData("User Incomplete Tasks", pending[0][0], "Not shipped", 5),

            new NumberData("User Incomplete Tasks",notCompleted[0][0], "Not completed", 6),
        ] as List<NumberData>

        return numberDataList;
    }
}
