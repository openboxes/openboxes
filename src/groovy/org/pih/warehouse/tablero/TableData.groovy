package org.pih.warehouse.tablero

class TableData implements Serializable {
    
    String name;
    String shipments;
    String requisition;
    Long discrepancy;

    TableData(String shipments, String name, Long discrepancy, String requisition) {
        this.shipments = shipments;
        this.name = name;
        this.discrepancy = discrepancy;
        this.requisition = requisition;
    }

    Map toJson() {
        [
                "name"        : name.toJson(),
                "shipments"   : shipments.toJson(),
                "discrepancy" : discrepancy.toJson(),
                "requisiton"  : requisition.toJson(),
        ]
    }
}
