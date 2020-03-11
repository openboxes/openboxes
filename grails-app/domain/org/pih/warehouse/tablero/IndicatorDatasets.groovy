package org.pih.warehouse.tablero


class IndicatorDatasets implements Serializable {
    
    String label;
    List<Integer> data;
    Boolean fill;
    String type;

    IndicatorDatasets(String label, List<Integer> data, Boolean fill = false, String type = null) 
    {
        this.label = label;
        this.data = data;
        this.fill = fill;  
        this.type = type;
    }

    Map toJson() {
        [
                "label"        : label,
                "data"         : data,
                "fill"         : fill,
                "type"         : type,
        ]
    }
}