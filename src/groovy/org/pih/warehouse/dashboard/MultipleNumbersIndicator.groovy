package org.pih.warehouse.dashboard

class MultipleNumbersIndicator implements Serializable {

    List<ColorNumber> listColorNumber

    MultipleNumbersIndicator(List<ColorNumber> listColorNumber) {
        this.listColorNumber = listColorNumber
    }

    Map toJson() {
        for (int i=0; i<listColorNumber.size(); i++) {
            listColorNumber[i] = listColorNumber[i].toJson()
        }

        return [
                "listColorNumber" : listColorNumber
        ]
    }
}
