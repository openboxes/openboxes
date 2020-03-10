package org.pih.warehouse.tablero


class NumberData implements Serializable {

     String title;
     Long number;
     String subtitle;
     int id;
     String link;

    NumberData(String title, Long number, String subtitle, int id, String link = null) {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.id = id;
        this.link = link;
    }

}
