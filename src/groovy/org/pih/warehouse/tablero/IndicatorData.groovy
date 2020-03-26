package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.IndicatorDatasets

class IndicatorData implements Serializable {
    
    List<IndicatorDatasets> datasets;
    List<String> labels;

    IndicatorData(List<IndicatorDatasets> datasets, List<String> labels) {
        this.labels = labels;
        this.datasets = datasets;
    }

    Map toJson() {
        for(int i=0;i<datasets.size();i++)
            datasets[i] = datasets[i].toJson();
        [
                "labels"   : labels,
                "datasets" : datasets,
        ]
    }
}
