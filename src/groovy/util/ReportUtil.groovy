package util

import org.apache.commons.lang.StringEscapeUtils

class ReportUtil {

    /**
     * This method should be taken out back and shot. Refactor to use one of the methods below.
     *
     * @deprecated
     * @param list
     * @return
     */
    static getCsv(list) {

        def csv = "";

        csv+= "Status,"
        csv+= "Product group,"
        csv+= "Product codes,"
        csv+= "Min,"
        csv+= "Reorder,"
        csv+= "Max,"
        csv+= "QoH,"
        csv+= "Value"
        csv+= "\n"

        //StringEscapeUtils.escapeCsv(product?.name?:"")
        // "${warehouse.message(code: 'inventoryLevel.currentQuantity.label', default: 'Current quantity')}"
        list.each { row ->
            csv += row.status + ","
            csv += StringEscapeUtils.escapeCsv(row.name) + ","
            csv += StringEscapeUtils.escapeCsv(row.productCodes.join(",")) + ","
            csv += row.minQuantity + ","
            csv += row.reorderQuantity + ","
            csv += row.maxQuantity + ","
            csv += row.onHandQuantity + ","
            csv += row.totalValue + ","
            csv += "\n"
        }

        return csv;
    }


    static getCsvForListOfMapEntries(List list) {
        def csv = ""
        if (list) {
            list[0].eachWithIndex { k, v, index ->
                csv += StringEscapeUtils.escapeCsv(k) + ","
            }
            csv+= "\n"

            list.each { entry ->
                entry.eachWithIndex { k, v, index ->
                    csv += StringEscapeUtils.escapeCsv(v ? v.toString() : "") + ","
                }
                csv += "\n"
            }
        }
        return csv
    }


    static getCsvForListOfMapEntries(List list, Closure csvHeader, Closure csvRow) {
        def csv = ""
        if (list) {

            csv += csvHeader(list[0])

            list.each { entry ->
                csv += csvRow(entry)
            }
        }
        return csv
    }


}
