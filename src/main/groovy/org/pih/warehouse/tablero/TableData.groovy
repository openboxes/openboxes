package org.pih.warehouse.tablero

class TableData implements Serializable {

    String number;
    String name;
    String value;
    String link;
    String icon;

    TableData(String number, String name, String value = null, String link = null, String icon = null) {
        this.number = number;
        this.name = name;
        this.value = value;
        this.link = link;
        this.icon = icon
    }

    Map toJson() {
        [
                "name"   : name.toJson(),
                "number" : number.toJson(),
                "value"  : value.toJson(),
                "link"   : link.toJson(),
                "icon"   : icon.toJson(),
        ]
    }
}
