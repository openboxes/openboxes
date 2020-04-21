package org.pih.warehouse.tablero


class IndicatorDatasets implements Serializable {
    String label;
    List<Integer> data;
    List<String> links;
    String type;
    String yAxisID;
    def legendConfig;

    IndicatorDatasets(String label, List<Integer> data, List<String> links = null, String type = null, String yAxisID = null, def legendConfig = null) {
        this.label = label;
        this.data = data;
        this.links = links;
        this.type = type;
        this.yAxisID = yAxisID;
        this.legendConfig = legendConfig;
    }

    Map toJson() {
       Map result = [
                "label"        : label,
                "data"         : data,
                "links"        : links,
                "type"         : type,
                "yAxisID"      : yAxisID,
        ]
        for (config in legendConfig) {
            result.put(config.key, config.value)
        }

        return result
    }
}
