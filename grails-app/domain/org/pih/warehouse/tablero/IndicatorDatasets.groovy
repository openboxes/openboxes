package org.pih.warehouse.tablero


class IndicatorDatasets implements Serializable {
    
    String label;
    List<Integer> data;
    Boolean fill;
    Integer id;
     
    IndicatorDatasets(Integer id, String label, List<Integer> data, Boolean fill)
    {
        this.id = id;
        this.label = label;
        this.data = data;
        this.fill = fill;        

    }

    Map toJson() {
        [
                "id": id,
                "label"      : label,
                "data"    : data,
                "fill"      : fill,
        ]
    }
}