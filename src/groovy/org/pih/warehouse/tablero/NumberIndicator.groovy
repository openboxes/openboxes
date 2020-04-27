package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ColorNumber

class NumberIndicator implements Serializable {
    
    ColorNumber first;
    ColorNumber second;
    ColorNumber third;
    Boolean gyrColors;

    NumberIndicator(ColorNumber first, ColorNumber second, ColorNumber third, Boolean gyrColors = true) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.gyrColors = gyrColors;
    }

    Map toJson() {
        [
                "first"     : first.toJson(),
                "second"    : second.toJson(),
                "third"     : third.toJson(),
                "gyrColors" : gyrColors,
        ]
    }
}
