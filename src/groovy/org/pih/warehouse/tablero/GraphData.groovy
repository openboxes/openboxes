package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ColorNumber

class GraphData implements Serializable {

    def data;
    def title;
    String type;
    String link;

    GraphData(data, def title, String type, String link = null) {
        this.data = data;
        this.title = title;
        this.type = type;
        this.link = link;
    }

    Map toJson() {
        [
                "data" : data.toJson(),
                "title": title,
                "type" : type,
                "link" : link,
        ]
    }
}
