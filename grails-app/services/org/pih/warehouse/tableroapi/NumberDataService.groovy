package org.pih.warehouse.tableroapi

import org.pih.warehouse.tablero.NumberData

class NumberDataService {

List<NumberData> getListNumberData(){
    NumberData numberData = new NumberData("Bin Location Summary",2696, "In stock", 1 );
    NumberData numberData2 = new NumberData("Bin Location Summary",1082, "Out of stock", 2 );
    NumberData numberData3 = new NumberData("Stock Movements",468, "Not shipped", 3 );
    NumberData numberData4 = new NumberData("User Incomplete Tasks",246, "Not shiped", 4 );
    NumberData numberData5 = new NumberData("User Incomplete Tasks",188, "Not completed", 5 );
    NumberData numberData6 = new NumberData("Discrepancy",290, "Items received", 6);

    List<NumberData> numberDataList = [] as List<NumberData>;
    numberDataList.add(numberData);
    numberDataList.add(numberData2);
    numberDataList.add(numberData3);
    numberDataList.add(numberData4);
    numberDataList.add(numberData5);
    numberDataList.add(numberData6);
    

    return numberDataList;
}
}