package org.pih.warehouse.tablero

class NumberData implements Serializable {

     String title;
     Long number;
     String subtitle;
     String link;
     List<String> listTooltipData;

    NumberData(String title, Long number, String subtitle, String link = '', List<String> listTooltipData = []) {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.link = link;
        this.listTooltipData = listTooltipData;
    }
}
