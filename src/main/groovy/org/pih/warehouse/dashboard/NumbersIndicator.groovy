package org.pih.warehouse.dashboard

class NumbersIndicator implements Serializable {

    ColorNumber first
    ColorNumber second
    ColorNumber third
    ColorNumber fourth

    NumbersIndicator(ColorNumber first, ColorNumber second, ColorNumber third, ColorNumber fourth = null) {
        this.first = first
        this.second = second
        this.third = third
        this.fourth = fourth
    }

    Map toJson() {
        [
                "first"     : first.toJson(),
                "second"    : second.toJson(),
                "third"     : third.toJson(),
                "fourth"    : fourth ? fourth.toJson() : null,
        ]
    }
}
