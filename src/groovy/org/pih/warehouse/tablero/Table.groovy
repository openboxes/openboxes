package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.TableData

class Table implements Serializable {
    
    TableData head;
    List<TableData> body;

    Table(TableData head, List<TableData> body) {
        this.head = head;
        this.body = body;
    }

    Map toJson() {
        [
                "head" : head.toJson(),
                "body" : body.toJson(),
        ]
    }
}
