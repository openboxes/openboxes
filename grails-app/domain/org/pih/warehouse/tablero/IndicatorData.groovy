package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.IndicatorDatasets

class IndicatorData implements Serializable {
    
    List<IndicatorDatasets> datasets;
    List<String> labels;
    Integer id;

    IndicatorData(List<IndicatorDatasets> datasets, Integer id, List<String> labels)
    {
        this.id = id;
        this.labels = labels;
        this.datasets = datasets;

    }

    Map toJson() {
        [
                "id": id,
                "labels"      : labels,
                "datasets"    : datasets,
        ]
    }
}