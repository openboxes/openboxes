package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ColorNumber

class NumberIndicator implements Serializable {
    
    ColorNumber green;
    ColorNumber yellow;
    ColorNumber red;

    NumberIndicator(ColorNumber green, ColorNumber yellow, ColorNumber red) {
        this.green = green;
        this.yellow = yellow;
        this.red = red;
    }

    Map toJson() {
        [
                "green"  : green.toJson(),
                "yellow" : yellow.toJson(),
                "red"    : red.toJson(),
        ]
    }
}
