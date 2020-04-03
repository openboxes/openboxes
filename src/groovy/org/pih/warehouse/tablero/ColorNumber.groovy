
package org.pih.warehouse.tablero

class ColorNumber implements Serializable {
    
    Long value;
    String subtitle;
    String link;

    ColorNumber(Long value, String subtitle, String link = null) {
        this.value = value;
        this.subtitle = subtitle;
        this.link = link;
    }

    Map toJson() {
        [
                "value"     : value,
                "subtitle"  : subtitle,
                "link"      : link,
        ]
    }
}
