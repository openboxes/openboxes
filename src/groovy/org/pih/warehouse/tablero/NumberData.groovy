package org.pih.warehouse.tablero


class NumberData implements Serializable {

     String title;
     Long number;
     String subtitle;
     String link;

    NumberData(String title, Long number, String subtitle, String link = '') {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.link = link;
    }
}
