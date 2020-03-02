package org.pih.warehouse.tablero


class IndicatorDatasets implements Serializable {
    
    String label;
    List<Integer> data;
    Boolean fill;
    String type;
    int barThickness;

    IndicatorDatasets(String label, List<Integer> data, Boolean fill, String type, int barThickness)
    {
        this.label = label;
        this.data = data;
        this.fill = fill;        
    }

    Map toJson() {
        [
                "label"      : label,
                "data"    : data,
                "fill"      : fill,
                "type" : type,
                "barThickness" : barThickness
        ]
    }
}