package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ColorNumber

class NumberIndicator implements Serializable {
    
    ColorNumber first;
    ColorNumber second;
    ColorNumber third;
    ColorNumber fourth;
    ColorNumber fifth;

    NumberIndicator(ColorNumber first, ColorNumber second, ColorNumber third, ColorNumber fourth = null, ColorNumber fifth = null) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
    }

    Map toJson() {
        [
                "first"     : first.toJson(),
                "second"    : second.toJson(),
                "third"     : third.toJson(),
                "fourth"     : fourth != null ? fourth.toJson() : null,
                "fifth"     : fifth != null ? fifth.toJson() : null
        ]
    }
}
