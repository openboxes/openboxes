package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.DataGraph
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.IndicatorDatasets
import org.pih.warehouse.tablero.NumberIndicator
import org.pih.warehouse.tablero.ColorNumber

class IndicatorDataService {

DataGraph getExpirationSummaryData(def expirationData){

    List<IndicatorDatasets> datasets = [
        new IndicatorDatasets('Expiration summary', [expirationData["expired"], expirationData["within30Days"], expirationData["within60Days"], expirationData["within90Days"], expirationData["within180Days"], expirationData["within365Days"], expirationData["greaterThan365Days"]], true)
    ];

    IndicatorData data = new IndicatorData(datasets, ['expired', 'within30Days', 'within60Days', 'within90Days', 'within180Days', 'within365Days', 'greaterThan365Days']);

    DataGraph indicatorData = new DataGraph(data, 1, "Expiration summary", "line");

    return indicatorData;
}

DataGraph getFillRate() {
       List<IndicatorDatasets> datasets = [
        new IndicatorDatasets('Line1 Dataset', [17, 23, 28, 45, 30, 40]),
        new IndicatorDatasets('Line2 Dataset', [35, 30, 30, 35, 25, 30]),
        new IndicatorDatasets('Bar1 Dataset', [17, 23, 28, 45, 30, 40], false, 'bar'),
        new IndicatorDatasets('Bar2 Dataset', [20, 23, 28, 45, 30, 40], false, 'bar'),
    ];

    IndicatorData data = new IndicatorData(datasets, ['January', 'February', 'March', 'April', 'May', 'June']);

    DataGraph indicatorData = new DataGraph(data, 1, "Fill rate", "line");

    return indicatorData;
}

DataGraph getInventorySummaryData(def inventoryData){

    List<IndicatorDatasets> datasets = [
        new IndicatorDatasets('Inventory Summary', [inventoryData["inStockCount"], inventoryData["overStockCount"], inventoryData["reoderStockCount"], inventoryData["lowStockCount"], inventoryData["stockOutCount"]])
    ];

    IndicatorData data = new IndicatorData(datasets, ['In stock', 'Above maximum', 'Below reorder', 'Below minimum', 'No longer in stock']);

    DataGraph indicatorData = new DataGraph(data, 1, "Inventory Summary", "horizontalBar");

    return indicatorData;
}

DataGraph getSentStockMovements(){
    List<IndicatorDatasets> datasets = [
            new IndicatorDatasets('Inventory Summary', [24, 30, 26, 20, 18, 17], false, null, 35)
        ];

    IndicatorData data = new IndicatorData(datasets, ['January', 'February', 'March', 'April', 'May', 'June']);

    DataGraph indicatorData = new DataGraph(data, 1, "Sent stock movements", "bar");

    return indicatorData;
}

DataGraph getReceivedStockData(){

    List<IndicatorDatasets> datasets = [
        new IndicatorDatasets('Stock movements received', [12, 3, 26, 70, 18])
    ];

    IndicatorData data = new IndicatorData(datasets, ['January', 'February', 'March', 'April', 'May']);

    DataGraph indicatorData = new DataGraph(data, 1, "Stock movements received", "doughnut");

    return indicatorData;
}

NumberIndicator getOutgoingStock(){

    ColorNumber green = new ColorNumber(18, 'Created < 4 days ago');
    ColorNumber yellow = new ColorNumber(48, 'Created > 4 days ago');
    ColorNumber red = new ColorNumber(24, 'Created > 7 days ago');

    NumberIndicator indicatorData = new NumberIndicator(green, yellow, red)

    return indicatorData;
}
}