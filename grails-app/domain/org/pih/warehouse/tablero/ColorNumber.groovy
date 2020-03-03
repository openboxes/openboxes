
package org.pih.warehouse.tablero

class ColorNumber implements Serializable {
    
    Integer value;
    String subtitle;

    ColorNumber(Integer value, String subtitle)
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