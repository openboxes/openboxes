package org.pih.warehouse.tablero

class ColorNumber implements Serializable {

    def value;
    String subtitle;
    String link;
    String color;

    ColorNumber(def value, String subtitle, String link = null, String color = null) {
        this.value = value;
        this.subtitle = subtitle;
        this.link = link;
        this.color = color;
    }

    def setConditionalColors(def errorCondition, def successCondition) {
        switch (this.value) {
            case { it >= successCondition }: this.color = 'success'; break;
            case { it >= errorCondition && it < successCondition }: this.color = 'warning'; break;
            case { it < errorCondition }: this.color = 'error'; break;
            default: this.color = 'state6';
        }
    }

    Map toJson() {
        [
                "value"   : value,
                "subtitle": subtitle,
                "link"    : link,
                "color"   : color
        ]
    }
}
