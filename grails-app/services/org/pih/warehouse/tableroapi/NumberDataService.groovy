package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.NumberData

class NumberDataService {

List<NumberData> getListNumberData(def binLocationData){

    List<NumberData> numberDataList = [
        new NumberData("Bin Location Summary",binLocationData[0]["count"], binLocationData[0]["label"], 1),
        new NumberData("Bin Location Summary",binLocationData[1]["count"], binLocationData[1]["label"], 2),
        new NumberData("Stock Movements",468, "Not shipped", 3),
        new NumberData("User Incomplete Tasks",246, "Not shiped", 4),
        new NumberData("User Incomplete Tasks",188, "Not completed", 5),
        new NumberData("Discrepancy",290, "Items received", 6)
    ] as List<NumberData>

    

    return numberDataList;
}
}