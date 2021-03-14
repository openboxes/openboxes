package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ColorNumber

class GraphData implements Serializable {

    def data;
    Map title;
    Map info;
    String type;
    String link;

    GraphData(def data, Map title, Map info, String type, String link = null) {
        this.data = data;
        this.title = title;
        this.info = info;
        this.type = type;
        this.link = link;
    }

    Map toJson() {
        [
                "data" : data.toJson(),
                "title": title,
                "info": info,
                "type" : type,
                "link" : link,
        ]
    }
}
