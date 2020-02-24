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

    String toString() {
        return "id" + this.id  + "title" + this.title + "number" + this.number + "subtitle" + this.subtitle;
    }

    Map toJson() {
        
        return [
                "id"       : id,
                "title"     : title,
                "number": number,
                "subtitle" : subtitle
        ]
    }

}