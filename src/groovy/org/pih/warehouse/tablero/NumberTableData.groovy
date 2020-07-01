package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.TableData
import org.pih.warehouse.tablero.NumbersIndicator

class NumberTableData implements Serializable {

    Table tableData;
    NumbersIndicator numbersIndicator;

    NumberTableData(Table tableData, NumbersIndicator numbersIndicator) {
        this.tableData = tableData;
        this.numbersIndicator = numbersIndicator;
    }
}
