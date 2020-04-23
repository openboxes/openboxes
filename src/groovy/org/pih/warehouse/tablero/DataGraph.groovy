package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.IndicatorData

class DataGraph implements Serializable {
    
     IndicatorData data;
     String title;
     String type;
     Boolean archived;
     Integer id;

    DataGraph(IndicatorData data, Integer id, Boolean archived = 0 ,String title, String type) {
        this.title = title;
        this.type = type;
        this.id = id;
        this.archived = archived;
        this.data = data; 
    }

    Map toJson() {
        [
                "id"       : id,
                "title"    : title,
                "type"     : type,
                "archived" : archived,
                "data"     : data.toJson(),
        ]
    }
}
