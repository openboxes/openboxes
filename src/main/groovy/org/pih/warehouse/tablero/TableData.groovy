package org.pih.warehouse.tablero

class TableData implements Serializable {

    String name;
    String number;
    String link;
    String value;

    TableData(String number, String name, String value, String link = null) {
        this.number = number;
        this.name = name;
        this.value = value;
        this.link = link;
    }

    Map toJson() {
        [
                "name"   : name.toJson(),
                "number" : number.toJson(),
                "value"  : value.toJson(),
                "link"   : link.toJson(),
        ]
    }
}
