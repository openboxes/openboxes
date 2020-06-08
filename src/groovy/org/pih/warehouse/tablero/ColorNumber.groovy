
package org.pih.warehouse.tablero

class ColorNumber implements Serializable {
    
    def value;
    String subtitle;
    String link;
    String color;

    ColorNumber(def value, String subtitle, String link = null, String color = null ) {
        this.value = value;
        this.subtitle = subtitle;
        this.link = link;
        this.color = color;
    }

    def getConditionalColors(List listConditions) {
                switch(this.value) {
                    case {it >= listConditions[0]}:  this.color = 'success'; break;
                    case {it >= listConditions[1] && it < listConditions[0]}: this.color = 'warning'; break;
                    case {it < listConditions[1]}: this.color = 'error'; break;
                    default : this.color =  null;
                    }
        }

    Map toJson() {
        [
                "value"     : value,
                "subtitle"  : subtitle,
                "link"      : link,
                "color"     : color
        ]
    }
}
