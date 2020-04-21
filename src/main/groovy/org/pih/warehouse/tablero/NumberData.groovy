package org.pih.warehouse.tablero

class NumberData implements Serializable {

     String title;
     Long number;
     String subtitle;
     String link;
     String tooltipData;

    NumberData(String title, Long number, String subtitle, String link = '', tooltipData = null) {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.link = link;
        this.tooltipData = tooltipData;
    }
}
