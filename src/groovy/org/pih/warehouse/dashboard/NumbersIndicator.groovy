package org.pih.warehouse.dashboard

class NumbersIndicator implements Serializable {

    ColorNumber first
    ColorNumber second
    ColorNumber third

    NumbersIndicator(ColorNumber first, ColorNumber second, ColorNumber third) {
        this.first = first
        this.second = second
        this.third = third
    }

    Map toJson() {
        [
                "first"     : first.toJson(),
                "second"    : second.toJson(),
                "third"     : third.toJson(),
        ]
    }
}
