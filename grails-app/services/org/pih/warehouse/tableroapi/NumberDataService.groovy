package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.NumberData
import org.pih.warehouse.requisition.Requisition

class NumberDataService {

    def inventorySnapshotService
    def inventoryService
    def dataService

    List<NumberData> getListNumberData(def user, def location){
        def binLocations = inventorySnapshotService.getQuantityOnHandByBinLocation(location);
        def binLocationData = inventoryService.getBinLocationSummary(binLocations);

        def shipments = Requisition.executeQuery("""select count(*) from Requisition r where r.origin = :location and r.status <> 'ISSUED' and r.createdBy = :user""", 
        ['location': location, 'user': user]);

        def pending = dataService.executeQuery("select count(*) from shipment where shipment.current_status = 'PENDING'");
        def notCompleted = dataService.executeQuery("select count(*) from openboxes.order  where order.status != 'COMPLETED'");
        def discrepancy = dataService.executeQuery("select count(*) from receipt_item where quantity_shipped != quantity_received");

        List<NumberData> numberDataList = [
            new NumberData("Bin Location Summary", binLocationData[0]["count"], binLocationData[0]["label"], 1),
            new NumberData("Bin Location Summary", binLocationData[1]["count"], binLocationData[1]["label"], 2),
            new NumberData("Your shipments", shipments[0], "In Progress", 3, "/openboxes/stockMovement/list?receiptStatusCode=PENDING&origin.id=" + location.id + "&createdBy.id=" + user.id),
            new NumberData("User Incomplete Tasks", pending[0][0], "Not shiped", 4),
            new NumberData("User Incomplete Tasks",notCompleted[0][0], "Not completed", 5),
            new NumberData("Discrepancy",discrepancy[0][0], "Items received", 6)
        ] as List<NumberData>

        return numberDataList;
    }
}
