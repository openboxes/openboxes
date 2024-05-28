package org.pih.warehouse.dashboard

class ColorNumber implements Serializable {

    def value
    String subtitle
    String link
    String color
    def value2
    Integer order

    ColorNumber(def value, String subtitle = null, String link = null, String color = null, def value2 = null, Integer order = null) {
        this.value = value
        this.subtitle = subtitle
        this.link = link
        this.color = color
        this.value2 = value2
        this.order = order
    }

    ColorNumber(Map args) {
        this.value = args.value
        this.subtitle = args.subtitle
        this.link = args.link
        this.color = args.color
        this.value2 = args.value2
        this.order = args.order
    }

    def setConditionalColors(def errorCondition, def successCondition) {
        switch (this.value) {
            case { it >= successCondition }: this.color = 'success'; break
            case { it >= errorCondition && it < successCondition }: this.color = 'warning'; break
            case { it < errorCondition }: this.color = 'error'; break
            default: this.color = 'state6'
        }
    }

    Map toJson() {
        [
                "value"   : value,
                "subtitle": subtitle,
                "link"    : link,
                "color"   : color,
                "value2"  : value2,
                "order"   : order,
        ]
    }
}
