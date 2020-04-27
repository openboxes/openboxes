package org.pih.warehouse.tablero


class IndicatorDatasets implements Serializable {

    String label;
    List<Integer> data;
    List<String> links;
    String type;

    IndicatorDatasets(String label, List<Integer> data, List<String> links = null, String type = null) {
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
