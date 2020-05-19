package org.pih.warehouse.tablero

class TooltipData implements Serializable {

    String name;
    String value;

    TooltipData(String name, String value) {
        this.name = name;
        this.value = value;
    }

    Map toJson() {
        [
                "name"   : name.toJson(),
                "value"  : value.toJson(),
        ]
    }
}