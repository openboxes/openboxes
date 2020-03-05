package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.DataGraph
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.IndicatorDatasets
import org.pih.warehouse.tablero.NumberIndicator
import org.pih.warehouse.tablero.ColorNumber

import org.pih.warehouse.requisition.Requisition
import groovy.time.TimeCategory

class IndicatorDataService {

Date today = new Date()

DataGraph getExpirationSummaryData(def expirationData){
    List listData = []
    for(item in expirationData){
        def tmp = item.value? item.value : 0
        listData.push(tmp)
    }
    
    List<IndicatorDatasets> datasets = [
        new IndicatorDatasets('Expiration summary', listData, true)
    ];

    IndicatorData data = new IndicatorData(datasets, ['Expired', '30 Days', '60 Days', '90 Days', '180 Days', '365 Days', '+365 Days']);

    DataGraph indicatorData = new DataGraph(data, 1, "Expiration summary", "line");

    return indicatorData;
}

DataGraph getFillRate() {
    List listData = []
    List bar2Data  = []
    List listLabel = []
    for(int i=4;i>=-1;i--){
        use(TimeCategory) { 
            def m1 = today + 1 - today.date - i.months
            def m2 = today + 1 - today.date - (i-1).months
            m1.clearTime()
            m2.clearTime()
            
            def temp = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ?""", [m1, m2]);
            String monthLabel = new java.text.DateFormatSymbols().months[m1.month]

            def tmp2 = Requisition.executeQuery("""select count(*) from RequisitionItem where dateCreated >= ? and dateCreated < ? and quantityCanceled > 0 and (cancelReasonCode = 'STOCKOUT' or cancelReasonCode = 'LOW_STOCK' or cancelReasonCode = 'COULD_NOT_LOCATE')""", [m1, m2]);
            
            if(i == -1) return listLabel.push(monthLabel) //To fix : in the front, indicator do not show both of bars in the first and last month label
            listLabel.push(monthLabel)
            listData.push(temp[0])
            bar2Data.push(tmp2[0])
        }
    }
    
    List<IndicatorDatasets> datasets = [
        new IndicatorDatasets('Line1 Dataset', listData),
        new IndicatorDatasets('Line2 Dataset', [15, 15, 15, 15, 15, 15, 15]),
        new IndicatorDatasets('Bar1 Dataset', listData, false, 'bar'),
        new IndicatorDatasets('Bar2 Dataset', bar2Data, false, 'bar'),
    ];

    IndicatorData data = new IndicatorData(datasets, listLabel);

    DataGraph indicatorData = new DataGraph(data, 1, "Fill rate", "line");

    return indicatorData;
}

DataGraph getInventorySummaryData(def results){
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

DataGraph getSentStockMovements(def location){
    List listData = []
    List listLabel = []
    for(int i=5;i>=0;i--){
        use(TimeCategory) { 
            def m1 = today + 1 - today.date - i.months
            def m2 = today + 1 - today.date - (i-1).months
            m1.clearTime()
            m2.clearTime()
            
            def temp = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :monthOne and r.dateCreated < :monthTwo and r.origin = :location""",
            ['monthOne': m1, 'monthTwo': m2, 'location': location]);
            String monthLabel = new java.text.DateFormatSymbols().months[m1.month]

            listLabel.push(monthLabel)
            listData.push(temp[0])
        }
    }

    List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Inventory Summary', listData, false, null, 35)
        ];

    IndicatorData data = new IndicatorData(datasets, listLabel);

    DataGraph indicatorData = new DataGraph(data, 1, "Sent stock movements", "bar");

    return indicatorData;
}

DataGraph getReceivedStockData(def location){
    List listData = []
    List listLabel = []
    for(int i=5;i>=0;i--){
        use(TimeCategory) { 
            def m1 = today + 1 - today.date - i.months
            def m2 = today + 1 - today.date - (i-1).months
            m1.clearTime()
            m2.clearTime()
            
            def temp = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :monthOne and r.dateCreated < :monthTwo and r.destination = :location""",
            ['monthOne': m1, 'monthTwo': m2, 'location': location]);
            String monthLabel = new java.text.DateFormatSymbols().months[m1.month]

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

NumberIndicator getOutgoingStock(def location){
    def m4 = today - 4;
    def m7 = today - 7;
    m4.clearTime();
    m7.clearTime();

    def greenData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :day and r.origin = :location""", 
    ['day': m4, 'location': location]);
    
    def yellowData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated >= :dayOne and r.dateCreated < :dayTwo and r.origin = :location""",
    ['dayOne': m7, 'dayTwo': m4, 'location': location]);

    def redData = Requisition.executeQuery("""select count(r) from Requisition r where r.dateCreated < :day and r.origin = :location""", // + than 7 days untill when? getting too old data
    ['day': m7, 'location': location]);

    ColorNumber green = new ColorNumber(greenData[0], 'Created < 4 days ago');
    ColorNumber yellow = new ColorNumber(yellowData[0], 'Created > 4 days ago');
    ColorNumber red = new ColorNumber(redData[0], 'Created > 7 days ago');

    NumberIndicator indicatorData = new NumberIndicator(green, yellow, red)

    return indicatorData;
}

NumberIndicator getInComingStock(def location){
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