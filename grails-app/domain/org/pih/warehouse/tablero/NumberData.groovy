package org.pih.warehouse.tablero


class NumberData implements Serializable {
    
     String title;
     Long number;
     String subtitle;
     int id;

    NumberData(String title, Long number, String subtitle, int id)
    {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.id = id;
        
    }

}