package org.pih.warehouse.tablero

import org.pih.warehouse.tablero.TableData
import org.pih.warehouse.tablero.NumberIndicator

class NumberTableData implements Serializable {

    Table tableData;
    NumberIndicator numberIndicator;

    NumberTableData(Table tableData, NumberIndicator numberIndicator) {
        this.tableData = tableData;
        this.numberIndicator = numberIndicator;
    }
}
