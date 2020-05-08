package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ColorNumber

class NumberIndicator implements Serializable {
    
    ColorNumber first;
    ColorNumber second;
    ColorNumber third;

    NumberIndicator(ColorNumber first, ColorNumber second, ColorNumber third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    Map toJson() {
        [
                "first"     : first.toJson(),
                "second"    : second.toJson(),
                "third"     : third.toJson()
        ]
    }
}
