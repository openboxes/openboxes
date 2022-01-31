package org.pih.warehouse.dashboard

class GraphData implements Serializable {

    def data
    String link

    GraphData(def data, String link = null) {
        this.data = data
        this.link = link
    }

    Map toJson() {
        [
                "data" : data.toJson(),
                "link" : link,
        ]
    }
}
