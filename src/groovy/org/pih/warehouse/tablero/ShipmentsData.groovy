package org.pih.warehouse.tablero

class ShipmentsData implements Serializable {
    
     String number;
     String name;
     String link;

    ShipmentsData(String number, String name , String link) {
        this.number = number;
        this.name = name;
        this.link = link;
    }
}