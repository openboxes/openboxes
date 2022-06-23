package org.pih.warehouse.dashboard

class NumberTableData implements Serializable {

    Table tableData
    NumbersIndicator numbersIndicator

    NumberTableData(Table tableData, NumbersIndicator numbersIndicator) {
        this.tableData = tableData
        this.numbersIndicator = numbersIndicator
    }
}
