
package org.pih.warehouse.tablero

class ColorNumber implements Serializable {
    
    Long value;
    String subtitle;

    ColorNumber(Long value, String subtitle)
    {
        this.value = value;
        this.subtitle = subtitle;
    }

    Map toJson() {
        [
                "value"     : value,
                "subtitle"  : subtitle,
        ]
    }

}