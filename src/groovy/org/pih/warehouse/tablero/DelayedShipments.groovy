package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ShipmentsData

class DelayedShipments implements Serializable {
    
     Integer numberByAir;
     Integer numberBySea;
     Integer numberByLand;
     List<ShipmentsData> shipmentsData;

    DelayedShipments(Integer numberByAir, Integer numberBySea , Integer numberByLand, List<ShipmentsData> shipmentsData) {
        this.numberByAir = numberByAir;
        this.numberBySea = numberBySea;
        this.numberByLand = numberByLand;
        this.shipmentsData = shipmentsData;
    }
}