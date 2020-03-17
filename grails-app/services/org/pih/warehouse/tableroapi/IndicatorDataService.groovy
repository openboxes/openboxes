package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.DataGraph
import org.pih.warehouse.tablero.ColorNumber
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.NumberIndicator
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

import org.pih.warehouse.tablero.IndicatorDatasets

class IndicatorDataService {

    def dataService
    Date today = new Date()

    DataGraph getExpirationSummaryData(def expirationData) {
        List listData = []
        for(item in expirationData){
            def tmp = item.value? item.value : 0
            listData.push(tmp)
        }
        
        List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Expiration summary', listData)
        ];

        IndicatorData data = new IndicatorData(datasets, ['Expired', '30 Days', '60 Days', '90 Days', '180 Days', '365 Days', '+365 Days']);

        DataGraph indicatorData = new DataGraph(data, 1, "Expiration summary", "line");

        return indicatorData;
    }

    DataGraph getFillRate() {
        List listData = []
        List bar2Data  = []
        List listLabel = []
        today.clearTime()
        for(int i=5;i>=0;i--){
            def monthBegin = today.clone()
            def monthEnd = today.clone()
            monthBegin.set(month: today.month - i, date: 1)
            monthEnd.set(month: today.month - i + 1, date: 1)
                
            def query1 = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ?""", [monthBegin, monthEnd]);

            def query2 = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ? and quantityCanceled > 0 and (cancelReasonCode = 'STOCKOUT' or cancelReasonCode = 'LOW_STOCK' or cancelReasonCode = 'COULD_NOT_LOCATE')""", [monthBegin, monthEnd]);
            String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month]
                
            listLabel.push(monthLabel)
            listData.push(query1[0])
            bar2Data.push(query2[0])
        }
        
        List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Line1 Dataset', listData, 'line'),
            new IndicatorDatasets('Line2 Dataset', [15, 15, 15, 15, 15, 15], 'line'),
            new IndicatorDatasets('Bar1 Dataset', listData),
            new IndicatorDatasets('Bar2 Dataset', bar2Data),
        ];

        IndicatorData data = new IndicatorData(datasets, listLabel);

        DataGraph indicatorData = new DataGraph(data, 1, "Fill rate", "line");

        return indicatorData;
    }

    DataGraph getInventorySummaryData(def results) {
        def inStockCount = results.findAll {
            it.quantityOnHand > 0
        }.size()
        def lowStockCount = results.findAll {
            it.quantityOnHand > 0 && it.quantityOnHand <= it.minQuantity
        }.size()
        def reoderStockCount = results.findAll {
            it.quantityOnHand > it.minQuantity && it.quantityOnHand <= it.reorderQuantity
        }.size()
        def overStockCount = results.findAll {
            it.quantityOnHand > it.reorderQuantity && it.quantityOnHand <= it.maxQuantity
        }.size()
        def stockOutCount = results.findAll {
            it.quantityOnHand <= 0
        }.size()
        //def totalCount = results.size()
        
        def inventoryData = [
                    inStockCount    : inStockCount,
                    lowStockCount   : lowStockCount,
                    reoderStockCount: reoderStockCount,
                    overStockCount  : overStockCount,
                    stockOutCount   : stockOutCount,
                    //totalCount      : totalCount
                ];
        
        List listData = []
        for(item in inventoryData){
            listData.push(item.value? item.value : 0)
        }

        List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Inventory Summary', listData)
        ];

        IndicatorData data = new IndicatorData(datasets, ['In stock', 'Above maximum', 'Below reorder', 'Below minimum', 'No longer in stock']);

        DataGraph indicatorData = new DataGraph(data, 1, "Inventory Summary", "horizontalBar");

        return indicatorData;
    }

    DataGraph getSentStockMovements(def location, def params) {
        Integer querySize = params.querySize? params.querySize.toInteger() : 5
        List allLocations = dataService.executeQuery("select id from openboxes.location")
        List listDatasets = []
        List listLabel = []
        today.clearTime()
        // Query data for all locations
        for(item in allLocations) {
            List listData = []
            listLabel = []
            try {
                List locationName = dataService.executeQuery("select name from location where id="+item[0].value)
                // querySize is the quantity of months in the filter : until which month query data
                for(int i = querySize; i >= 0; i--) {
                    def monthBegin = today.clone()
                    def monthEnd = today.clone()
                    monthBegin.set(month: today.month - i, date: 1)
                    monthEnd.set(month: today.month - i + 1, date: 1)

                    def query = Shipment.executeQuery("select count(*) from Shipment s where s.lastUpdated >= :monthOne and s.lastUpdated < :monthTwo and s.origin = "+ item[0].value +" and s.currentStatus <> 'PENDING'", 
                    // The column transaction_transaction_date doesn't exist, using lastUpdated instead
                    ['monthOne': monthBegin, 'monthTwo': monthEnd]);
                    String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month].substring(0,3)

                    listLabel.push(monthLabel)
                    listData.push(query[0])
                }
                listDatasets.push(new IndicatorDatasets(locationName[0].name, listData))
            } catch(err) {
                log.error "Query error in getSentStockMovements : " + err
            }
        }
        List<IndicatorDatasets> datasets = listDatasets;

        IndicatorData data = new IndicatorData(datasets, listLabel);

        DataGraph indicatorData = new DataGraph(data, 1, "Stock Movements Sent by Month", "bar");

        return indicatorData;
    }

    DataGraph getReceivedStockData(def location, def params) {
        Integer querySize = params.querySize? params.querySize.toInteger() : 5
        List allLocations = dataService.executeQuery("select id from openboxes.location")
        List listDatasets = []
        List listLabel = []
        today.clearTime()
        for(item in allLocations) {
            List listData = []
            listLabel = []
            try {
                List locationName = dataService.executeQuery("select name from location where id="+item[0].value)
                for(int i = querySize ;i >= 0; i--) {
                    def monthBegin = today.clone()
                    def monthEnd = today.clone()
                    monthBegin.set(month: today.month - i, date: 1)
                    monthEnd.set(month: today.month - i + 1, date: 1)

                    def query = Shipment.executeQuery("select count(*) from Shipment s where s.lastUpdated >= :monthOne and s.lastUpdated < :monthTwo and s.destination = "+ item[0].value +" and s.currentStatus <> 'PENDING'",
                    // The column transaction_transaction_date doesn't exist, using lastUpdated instead
                    ['monthOne': monthBegin, 'monthTwo': monthEnd]);
                    String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month].substring(0,3)

                    listLabel.push(monthLabel)
                    listData.push(query[0])
                }
                listDatasets.push(new IndicatorDatasets(locationName[0].name, listData))
            } catch(err) {
                log.error "Query error in getReceivedStockData : " + err
            }
        }
        List<IndicatorDatasets> datasets = listDatasets;

        IndicatorData data = new IndicatorData(datasets, listLabel);

        DataGraph indicatorData = new DataGraph(data, 1, "Incoming Stock Movements by Month", "bar");

        return indicatorData;
    }

    NumberIndicator getOutgoingStock(def location) {
        today.clearTime();
        def m4 = today - 4;
        def m7 = today - 7;

        def greenData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :day and r.origin = :location""", 
        ['day': m4, 'location': location]);
        
        def yellowData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :dayOne and r.dateCreated < :dayTwo and r.origin = :location""",
        ['dayOne': m7, 'dayTwo': m4, 'location': location]);

        def redData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated < :day and r.origin = :location""",
        ['day': m7, 'location': location]);

        ColorNumber green = new ColorNumber(greenData[0], 'Created < 4 days ago');
        ColorNumber yellow = new ColorNumber(yellowData[0], 'Created > 4 days ago');
        ColorNumber red = new ColorNumber(redData[0], 'Created > 7 days ago');

        NumberIndicator indicatorData = new NumberIndicator(green, yellow, red)

        return indicatorData;
    }

    NumberIndicator getIncomingStock(def location) {
        today.clearTime();
        def m4 = today - 4;
        def m7 = today - 7;

        def greenData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :day and r.destination = :location""",
        ['day': m4, 'location': location]);
        
        def yellowData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :dayOne and r.dateCreated < :dayTwo and r.destination = :location""",
        ['dayOne': m7, 'dayTwo': m4, 'location': location]);

        def redData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated < :day and r.destination = :location""",
        ['day': m7, 'location': location]);

        ColorNumber green = new ColorNumber(greenData[0], 'Created < 4 days ago');
        ColorNumber yellow = new ColorNumber(yellowData[0], 'Created > 4 days ago');
        ColorNumber red = new ColorNumber(redData[0], 'Created > 7 days ago');

        NumberIndicator indicatorData = new NumberIndicator(green, yellow, red)

        return indicatorData;
    }
}
