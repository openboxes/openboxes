package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.ColorNumber

class NumberIndicator implements Serializable {
    
    ColorNumber first;
    ColorNumber second;
    ColorNumber third;
    Boolean gyrColors;
    String labelShipment;
    String labelName;

    NumberIndicator(ColorNumber first, ColorNumber second, ColorNumber third, Boolean gyrColors = true, String labelShipment = "", String labelName = "") {
        this.first = first;
        this.second = second;
        this.third = third;
        this.gyrColors = gyrColors;
        this.labelShipment = labelShipment;
        this.labelName = labelName;
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
