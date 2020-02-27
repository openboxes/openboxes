package org.pih.warehouse.tablero


class NumberData implements Serializable {
    
     String title;
     int number;
     String subtitle;
     int id;

    NumberData(String title, int number, String subtitle, int id)
    {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.id = id;
        
    }

}