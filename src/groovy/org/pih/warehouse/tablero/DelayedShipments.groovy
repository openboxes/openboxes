package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ShipmentsData

class DelayedShipments implements Serializable {
    
     Integer numberByAir;
     Integer numberBySea;
     Integer numberByLand;
     String labelByAir;
     String labelBySea;
     String labelByLand;
     String labelShipment;
     String labelName;
     List<ShipmentsData> shipmentsData;

    DelayedShipments(Integer numberByAir, Integer numberBySea , Integer numberByLand , String labelByAir, String labelBySea, String labelByLand, String labelShipment, String labelName, List<ShipmentsData> shipmentsData) {
        this.numberByAir = numberByAir;
        this.numberBySea = numberBySea;
        this.numberByLand = numberByLand;
        this.labelByAir = labelByAir;
        this.labelBySea = labelBySea;
        this.labelByLand = labelByLand;
        this.labelShipment = labelShipment;
        this.labelName = labelName;
        this.shipmentsData = shipmentsData;
    }
}