package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.DataGraph
import org.pih.warehouse.tablero.TableData
import org.pih.warehouse.tablero.ColorNumber
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.NumberIndicator
import org.pih.warehouse.tablero.IndicatorDatasets
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.core.Location
import org.joda.time.LocalDate

class IndicatorDataService {

    def dataService

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
        Date today = new Date()
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
        Integer querySize = params.querySize? params.querySize.toInteger()-1 : 5
        Date today = new Date()
        today.clearTime()
        
        // queryLimit limits the query and avoid of getting data older than wanted
        Date queryLimit = today.clone()
        queryLimit.set(month: today.month - querySize, date: 1) 

        List queryData = Shipment.executeQuery("SELECT COUNT(s.id), s.destination, MONTH(s.lastUpdated), YEAR(s.lastUpdated) FROM Shipment s WHERE s.origin = :location AND s.currentStatus <> 'PENDING' AND s.lastUpdated > :limit GROUP BY MONTH(s.lastUpdated), YEAR(s.lastUpdated), s.destination", 
        ['location': location, 'limit': queryLimit])
        // queryData gives an array of arrays [[count, destination, month, year], ...] of sent stock
        
        List listRes = []
        List listLabel = []
        // Loop 1: Each sent stock array in query data array
        for(item in queryData) {
            // item[0]: item total counted
            // item[1]: item destination
            // item[2]: item month
            // item[3]: item year
            Location itemLocation = item[1]
            List listData = []
            listLabel = []

            // Loop 2: Give each requested month a value, label and month label; value is 0 when month have no data
            for(int i = querySize; i >= 0; i--) {
                Date month = today.clone()
                month.set(month: today.month - i, date: 1)

                // Places 0 in months where there is no sent stock, else places item total counted
                Integer value = 0
                if (month.month == item[2]-1 && month.year + 1900 == item[3]) {
                    value = item[0]
                }

                // Pushs month label in label array and sent stock in the data array
                String monthLabel = new java.text.DateFormatSymbols().months[month.month].substring(0,3)
                listLabel.push(monthLabel)
                listData.push(value)
            }
            // Array of data lists: (stack it by destination)
            listRes.push(new IndicatorDatasets(itemLocation.name, listData))
        }
        List<IndicatorDatasets> datasets = listRes;

        IndicatorData data = new IndicatorData(datasets, listLabel);

        DataGraph indicatorData = new DataGraph(data, 1, "Stock Movements Sent by Month", "bar");

        return indicatorData;
    }

    DataGraph getReceivedStockData(def location) {
        List listData = []
        List listLabel = []
        Date today = new Date()
        today.clearTime()
        for(int i=5;i>=0;i--){
            def monthBegin = today.clone()
            def monthEnd = today.clone()
            monthBegin.set(month: today.month - i, date: 1)
            monthEnd.set(month: today.month - i + 1, date: 1)
                
            def temp = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :monthOne and r.dateCreated < :monthTwo and r.destination = :location""",
            ['monthOne': monthBegin, 'monthTwo': monthEnd, 'location': location]);
            String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month]

            listLabel.push(monthLabel)
            listData.push(temp[0])
        }

        List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Stock movements received', listData)
        ];

        IndicatorData data = new IndicatorData(datasets, listLabel);

        DataGraph indicatorData = new DataGraph(data, 1, "Stock movements received", "doughnut");

        return indicatorData;
    }

    NumberIndicator getOutgoingStock(def location) {
        Date today = new Date()
        today.clearTime();
        def m4 = today - 4;
        def m7 = today - 7;

        def greenData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated > :day and r.origin = :location and r.status <> 'ISSUED'""", 
        ['day': m4, 'location': location]);
    
        def yellowData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :dayOne and r.dateCreated <= :dayTwo and r.origin = :location and r.status <> 'ISSUED'""",
        ['dayOne': m7, 'dayTwo': m4, 'location': location]);

        def redData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated < :day and r.origin = :location and r.status <> 'ISSUED'""", 
        ['day': m7, 'location': location]);

        ColorNumber green = new ColorNumber(greenData[0], 'Created < 4 days ago');
        ColorNumber yellow = new ColorNumber(yellowData[0], 'Created > 4 days ago');
        ColorNumber red = new ColorNumber(redData[0], 'Created > 7 days ago');

        NumberIndicator indicatorData = new NumberIndicator(green, yellow, red)

        return indicatorData;
    }

    NumberIndicator getIncomingStock(def location) {
        Date today = new Date()
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

    List<TableData> getDiscrepancy(Location location, def params) {
        Integer querySize = params.querySize? params.querySize.toInteger() - 1 : 5

        List<TableData> indicatorData = []

        LocalDate date = LocalDate.now().minusMonths(querySize)

        def query = dataService.executeQuery("""select shipment.shipment_number as number, shipment.name, receipt_item.quantity_shipped - receipt_item.quantity_received as count, shipment.requisition_id as requisition from receipt_item inner join receipt inner join shipment WHERE receipt.receipt_status_code = 'RECEIVED' AND shipment.current_status = 'PARTIALLY_RECEIVED' AND shipment.destination_id = """ + location.id + """ AND receipt_item.quantity_shipped <> receipt_item.quantity_received AND receipt.actual_delivery_date > """ + date + """ GROUP BY shipment.shipment_number, shipment.id, receipt_item.quantity_shipped - receipt_item.quantity_received""")

        query.each{
            indicatorData.push(new TableData(it.number, it.name, it.count, it.requisition))
        }

        return indicatorData;
    }
}
