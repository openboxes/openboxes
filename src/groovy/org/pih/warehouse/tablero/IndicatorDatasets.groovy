package org.pih.warehouse.tablero


class IndicatorDatasets implements Serializable {
    // Label can be a simple string or a list
    def label;
    List<Integer> data;
    List<String> links;
    String type;

    IndicatorDatasets(def label, List<Integer> data, List<String> links = null, String type = null) {
        this.label = label;
        this.data = data;
        this.links = links;
        this.type = type;
    }

    Map toJson() {
        [
                "label"        : label,
                "data"         : data,
                "links"        : links,
                "type"         : type
        ]
    }
}
