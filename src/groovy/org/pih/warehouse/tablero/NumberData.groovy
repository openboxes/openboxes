package org.pih.warehouse.tablero

class NumberData implements Serializable {

     Map title;
     Double number;
     // It can be a string or a map for translation. The idea is to have just a map in the end.
     def subtitle;
     String link;
     String tooltipData;
     // Either 'number' or 'dollars'
     String numberType;

    NumberData(Map title, Double number, def subtitle, String link = '', tooltipData = null, numberType = 'number') {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.link = link;
        this.tooltipData = tooltipData;
        this.numberType  = numberType;
    }
}
