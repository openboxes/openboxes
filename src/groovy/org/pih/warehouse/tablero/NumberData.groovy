package org.pih.warehouse.tablero

class NumberData implements Serializable {

     Map title;
     Long number;
     // It can be a string or a map for translation. The idea is to have just a map in the end.
     def subtitle;
     String link;
     String tooltipData;
     String type;

    NumberData(Map title, Long number, def subtitle, String link = '', tooltipData = null, String type = 'number') {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.link = link;
        this.tooltipData = tooltipData;
        this.type = type
    }
}
