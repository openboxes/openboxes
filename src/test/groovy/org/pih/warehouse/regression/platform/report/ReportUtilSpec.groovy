package org.pih.warehouse.regression.platform.report

import org.pih.warehouse.report.*
import spock.lang.Specification
import util.ReportUtil

class ReportUtilSpec extends Specification {

    static final String BOM = '﻿'
    static final String EOL = System.lineSeparator()

    void 'the inventory level csv has a header and one escaped line per product group'() {
        given:
        List rows = [[status: 'BELOW_MINIMUM', name: 'Gloves, nitrile', productCodes: ['G1', 'G2'], minQuantity: 10,
                      reorderQuantity: 20, maxQuantity: 100, onHandQuantity: 5, totalValue: 12.5]]

        when:
        String csv = ReportUtil.getCsv(rows)

        then:
        csv == BOM + 'Status,Product group,Product codes,Min,Reorder,Max,QoH,Value\n' +
                'BELOW_MINIMUM,"Gloves, nitrile","G1,G2",10,20,100,5,12.5,\n'
    }

    void 'a list of maps becomes a csv with the keys of the first row as header'() {
        given:
        List rows = [[code: 'P1', name: 'Bandage "large"', qty: 3], [code: 'P2', name: null, qty: 0]]

        when:
        String csv = ReportUtil.getCsvForListOfMapEntries(rows)

        then:
        csv == BOM + 'code,name,qty,' + EOL + 'P1,"Bandage ""large""",3,' + EOL + 'P2,,,' + EOL
    }

    void 'an empty list gives just the byte order mark'() {
        expect:
        ReportUtil.getCsvForListOfMapEntries([]) == BOM
        ReportUtil.getCsvForListOfMapEntries([], { 'h' }, { 'r' }) == BOM
    }

    void 'custom header and row closures build the csv'() {
        when:
        String csv = ReportUtil.getCsvForListOfMapEntries([[a: 1], [a: 2]],
                { first -> first.keySet().join(',') + '\n' },
                { row -> "${row.a * 10}\n" })

        then:
        csv == BOM + 'a\n10\n20\n'
    }
}
