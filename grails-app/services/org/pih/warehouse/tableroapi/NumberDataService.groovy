package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.NumberData
import org.pih.warehouse.inventory.InventorySnapshot

class NumberDataService {

    def dataService

    List<NumberData> getListNumberData(def location){
        def tomorrow = new Date() + 1;
        tomorrow.clearTime();

        def binLocations = InventorySnapshot.executeQuery('select count(*) from InventorySnapshot i where i.location=:location and i.date = :tomorrow', ['location': location, 'tomorrow': tomorrow]);
        def shipments = dataService.executeQuery("select count(*) from shipment where shipment.current_status = 'PARTIALLY_RECEIVED' or 'NOT_RECEIVED'");
        def pending = dataService.executeQuery("select count(*) from shipment where shipment.current_status = 'PENDING'");
        def notCompleted = dataService.executeQuery("select count(*) from openboxes.order  where order.status != 'COMPLETED'");
        def discrepancy = dataService.executeQuery("select count(*) from receipt_item where quantity_shipped != quantity_received");

        List<NumberData> numberDataList = [
            new NumberData("Bin Location Summary", binLocations[0], 'In / Out of stock', 1, "/openboxes/report/showBinLocationReport?location.id="+ location.id),
            new NumberData("Stock Movements", shipments[0][0], "Not shipped", 3),
            new NumberData("User Incomplete Tasks", pending[0][0], "Not shiped", 4),
            new NumberData("User Incomplete Tasks",notCompleted[0][0], "Not completed", 5),
            new NumberData("Discrepancy",discrepancy[0][0], "Items received", 6)
        ] as List<NumberData>

        

        return numberDataList;
    }
}