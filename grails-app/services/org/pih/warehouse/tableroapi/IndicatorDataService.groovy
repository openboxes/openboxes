package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.DataGraph
import org.pih.warehouse.tablero.IndicatorData
import org.pih.warehouse.tablero.IndicatorDatasets

class IndicatorDataService {

DataGraph getIndicatorData(){

    List<IndicatorDatasets> datasets = [new IndicatorDatasets(3, 'Expiration summary', [400, 380, 395, 375, 430, 370], true).toJson()];

    IndicatorData data = new IndicatorData(datasets, 2, ['January', 'February', 'March', 'April', 'May', 'June']).toJson();

    DataGraph indicatorData = new DataGraph(data, 1, "Expiration summary", "line").toJson();

    return indicatorData;
}
}