package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ColorNumber

class GraphData implements Serializable {

    def data;
    String title;
    String type;
    String link;
    ColorNumber colorNumber;

    GraphData(data, String title, String type, String link = null, ColorNumber colorNumber = null) {
        this.data = data;
        this.title = title;
        this.type = type;
        this.link = link;
        this.colorNumber = colorNumber;
    }

    Map toJson() {
        [
                "data" : data.toJson(),
                "title": title,
                "type" : type,
                "link" : link,
                "colorNumber" : colorNumber ? colorNumber.toJson() : null,
        ]
    }
}
