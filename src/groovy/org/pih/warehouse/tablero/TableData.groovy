package org.pih.warehouse.tablero

class TableData implements Serializable {
    
    String name;
    String shipments;
    Long discrepancy;

    TableData(String shipments, String name, Long discrepancy) {
        this.shipments = shipments;
        this.name = name;
        this.discrepancy = discrepancy;
    }

    Map toJson() {
        [
                "name"        : name.toJson(),
                "shipments"   : shipments.toJson(),
                "discrepancy" : discrepancy.toJson(),
        ]
    }
}
