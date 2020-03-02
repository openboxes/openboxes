package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.DataGraph
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.IndicatorDatasets

class IndicatorDataService {

DataGraph getExpirationSummaryData(){

    List<IndicatorDatasets> datasets = [
        new IndicatorDatasets('Expiration summary', [400, 380, 395, 375, 430, 370], true, "bar", null)
    ];

    IndicatorData data = new IndicatorData(datasets, ['January', 'February', 'March', 'April', 'May', 'June']);

    DataGraph indicatorData = new DataGraph(data, 1, "Expiration summary", "line");

    return indicatorData;
}

DataGraph getFillRate() {
       List<IndicatorDatasets> datasets = [
        new IndicatorDatasets('Line1 Dataset', [17, 23, 28, 45, 30, 40], false, null, null),
        new IndicatorDatasets('Line2 Dataset', [35, 30, 30, 35, 25, 30], false, null, null),
        new IndicatorDatasets('Bar1 Dataset', [17, 23, 28, 45, 30, 40], false, 'bar', null),
        new IndicatorDatasets('Bar2 Dataset', [20, 23, 28, 45, 30, 40], false, 'bar', null),
    ];

    IndicatorData data = new IndicatorData(datasets, ['January', 'February', 'March', 'April', 'May', 'June']);

    DataGraph indicatorData = new DataGraph(data, 1, "Fill rate", "line");

    return indicatorData;
}

DataGraph getSentStockMovements(){
List<IndicatorDatasets> datasets = [
        new IndicatorDatasets('Inventory Summary', [24, 30, 26, 20, 18, 17], true, null , 35)
    ];

IndicatorData data = new IndicatorData(datasets, ['January', 'February', 'March', 'April', 'May', 'June']);

DataGraph indicatorData = new DataGraph(data, 1, "Sent stock movements", "bar");
}

}