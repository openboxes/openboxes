package org.pih.warehouse.dashboard

class NumberData implements Serializable {

    Double number

    String link
    String tooltipData

    NumberData(Double number, String link = '', tooltipData = null) {
        this.number = number
        this.link = link
        this.tooltipData = tooltipData
    }
}
