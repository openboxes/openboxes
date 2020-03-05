package org.pih.warehouse.tablero


class IndicatorDatasets implements Serializable {
    
    String label;
    List<Integer> data;
    Boolean fill;
    String type;
    def barThickness;

    IndicatorDatasets(String label, List<Integer> data, Boolean fill = false, String type = null, def barThickness = "flex") 
    {
        this.label = label;
        this.data = data;
        this.fill = fill;  
        this.type = type;
        this.barThickness = barThickness;      
    }

    Map toJson() {
        [
                "label"        : label,
                "data"         : data,
                "fill"         : fill,
                "type"         : type,
                "barThickness" : barThickness
        ]
    }
}