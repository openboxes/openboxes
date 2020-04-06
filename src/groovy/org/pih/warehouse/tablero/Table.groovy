package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.TableData

class Table implements Serializable {
    
    String name;
    String number;
    String value;
    List<TableData> body;

    Table(String number, String name, String value, List<TableData> body) {
        this.number = number;
        this.name = name;
        this.value = value;
        this.body = body;
    }

    Map toJson() {
        [
                "name"   : name.toJson(),
                "number" : number.toJson(),
                "value"  : value.toJson(),
                "body"   : body.toJson(),
        ]
    }
}
