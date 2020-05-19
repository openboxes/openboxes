package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.TooltipData

class NumberData implements Serializable {

     String title;
     Long number;
     String subtitle;
     String link;
     List<TooltipData> listTooltipData;

    NumberData(String title, Long number, String subtitle, String link = '', List<TooltipData> listTooltipData = []) {
        this.title = title;
        this.number = number;
        this.subtitle = subtitle;
        this.link = link;
        this.listTooltipData = listTooltipData;
    }
}
