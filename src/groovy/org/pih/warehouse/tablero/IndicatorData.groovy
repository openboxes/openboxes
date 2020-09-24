package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.IndicatorDatasets

class IndicatorData implements Serializable {
    
    List<IndicatorDatasets> datasets;
    List<Map> labels;
    ColorNumber colorNumber;

    IndicatorData(List<IndicatorDatasets> datasets, List<Map> labels, ColorNumber colorNumber = null) {
        this.labels = labels;
        this.datasets = datasets;
        this.colorNumber = colorNumber;
    }

    Map toJson() {
        for(int i=0;i<datasets.size();i++)
            datasets[i] = datasets[i].toJson();
        [
                "labels"      : labels,
                "datasets"    : datasets,
                "colorNumber" : colorNumber ? colorNumber.toJson() : null,
        ]
    }
}
