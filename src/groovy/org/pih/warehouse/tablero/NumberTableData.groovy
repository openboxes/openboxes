package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.TableData
import org.pih.warehouse.tablero.NumberIndicator

class NumberTableData implements Serializable {

    List<TableData> tableData;
    NumberIndicator numberIndicator;

    NumberTableData(List<TableData> tableData, NumberIndicator numberIndicator){
        this.tableData = tableData;
        this.numberIndicator = numberIndicator;
    }
}