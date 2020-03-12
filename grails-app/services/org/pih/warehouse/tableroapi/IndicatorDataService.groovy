package org.pih.warehouse.tableroapi

import groovy.time.TimeCategory
import org.pih.warehouse.tablero.DataGraph
import org.pih.warehouse.tablero.ColorNumber
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.NumberIndicator
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.tablero.IndicatorDatasets

class IndicatorDataService {

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
        for(int i=5;i>=0;i--){
            // Using TimeCategory to calculate i months ago (i.months = i months instead i days as default)
            use(TimeCategory) { 
                // monthBegin = begin of the month i months ago
                // monthEnd = begin of the month i-1 months ago (end of monthBegin)
                def monthBegin = today + 1 - today.date - i.months
                def monthEnd = today + 1 - today.date - (i-1).months
                monthBegin.clearTime()
                monthEnd.clearTime()
                
                def query1 = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ?""", [monthBegin, monthEnd]);
                String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month]

                def query2 = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ? and quantityCanceled > 0 and (cancelReasonCode = 'STOCKOUT' or cancelReasonCode = 'LOW_STOCK' or cancelReasonCode = 'COULD_NOT_LOCATE')""", [monthBegin, monthEnd]);
                
                listLabel.push(monthLabel)
                listData.push(query1[0])
                bar2Data.push(query2[0])
            }
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

    DataGraph getSentStockMovements(def location) {
        List listData = []
        List listLabel = []
        for(int i=5;i>=0;i--){
            use(TimeCategory) { 
                def monthBegin = today + 1 - today.date - i.months
                def monthEnd = today + 1 - today.date - (i-1).months
                monthBegin.clearTime()
                monthEnd.clearTime()
                
                def temp = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :monthOne and r.dateCreated < :monthTwo and r.origin = :location""",
                ['monthOne': monthBegin, 'monthTwo': monthEnd, 'location': location]);
                String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month]

                listLabel.push(monthLabel)
                listData.push(temp[0])
            }
        }

        List<IndicatorDatasets> datasets = [
                new IndicatorDatasets('Inventory Summary', listData)
            ];

        IndicatorData data = new IndicatorData(datasets, listLabel);

        DataGraph indicatorData = new DataGraph(data, 1, "Sent stock movements", "bar");

        return indicatorData;
    }

    DataGraph getReceivedStockData(def location) {
        List listData = []
        List listLabel = []
        for(int i=5;i>=0;i--){
            use(TimeCategory) { 
                def monthBegin = today + 1 - today.date - i.months
                def monthEnd = today + 1 - today.date - (i-1).months
                monthBegin.clearTime()
                monthEnd.clearTime()
                
                def temp = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :monthOne and r.dateCreated < :monthTwo and r.destination = :location""",
                ['monthOne': monthBegin, 'monthTwo': monthEnd, 'location': location]);
                String monthLabel = new java.text.DateFormatSymbols().months[monthBegin.month]

                listLabel.push(monthLabel)
                listData.push(temp[0])
            }
        }

        List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Stock movements received', listData)
        ];

        IndicatorData data = new IndicatorData(datasets, listLabel);

        DataGraph indicatorData = new DataGraph(data, 1, "Stock movements received", "doughnut");

        return indicatorData;
    }

    NumberIndicator getOutgoingStock(def location) {
        def m4 = today - 4;
        def m7 = today - 7;
        m4.clearTime();
        m7.clearTime();

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
        def m4 = today - 4;
        def m7 = today - 7;
        m4.clearTime();
        m7.clearTime();

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