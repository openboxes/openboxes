package util

import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.core.Constants

class ReportUtil {

    /**
     * This method should be taken out back and shot. Refactor to use one of the methods below.
     *
     * @deprecated
     * @param list
     * @return
     */
    static getCsv(list) {

        def csv = ""

        csv += "Status,"
        csv += "Product group,"
        csv += "Product codes,"
        csv += "Min,"
        csv += "Reorder,"
        csv += "Max,"
        csv += "QoH,"
        csv += "Value"
        csv += "\n"

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

        return csv
    }


    static getCsvForListOfMapEntries(List list) {
        StringBuffer sb = new StringBuffer()
        if (list) {
            list[0].eachWithIndex { k, v, index ->
                sb.append(StringEscapeUtils.escapeCsv(k))
                sb.append(Constants.DEFAULT_COLUMN_SEPARATOR)
            }
            sb.append(System.lineSeparator())

            list.each { entry ->
                entry.eachWithIndex { k, v, index ->
                    sb.append(v ? StringEscapeUtils.escapeCsv(v.toString()) : "")
                    sb.append(Constants.DEFAULT_COLUMN_SEPARATOR)
                }
                sb.append(System.lineSeparator())
            }
        }
        return sb.toString()
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
