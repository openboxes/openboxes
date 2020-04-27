package org.pih.warehouse.tablero


class IndicatorDatasets implements Serializable {

    String label;
    List<Integer> data;
    String type;

    IndicatorDatasets(String label, List<Integer> data, String type = null) {
        this.label = label;
        this.data = data;
        this.type = type;
    }

    Map toJson() {
        [
                "label": label,
                "data" : data,
                "type" : type
        ]
    }
}
