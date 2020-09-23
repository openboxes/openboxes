package org.pih.warehouse.tablero

class NumberData implements Serializable {

     def title;
     Long number;
     // It can be a string or a map for translation. The idea is to have just a map in the end.
     def subtitle;
     String link;
     String tooltipData;

    NumberData(def title, Long number, def subtitle, String link = '', tooltipData = null) {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.link = link;
        this.tooltipData = tooltipData;
    }
}
