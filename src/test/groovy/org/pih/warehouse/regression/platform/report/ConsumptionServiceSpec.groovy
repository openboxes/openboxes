package org.pih.warehouse.regression.platform.report

import spock.lang.PendingFeature

import org.pih.warehouse.report.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.ConsumptionFact
import org.pih.warehouse.reporting.DateDimension
import org.pih.warehouse.reporting.ProductDimension

class ConsumptionServiceSpec extends Specification implements ServiceUnitTest<ConsumptionService>, DataTest {

    TimeZone originalTimeZone
    Locale originalLocale

    void setupSpec() {
        mockDomains(Product, Tag, ConsumptionFact, ProductDimension, DateDimension)
    }

    void setup() {
        // The cross tab formats dates with the shared formatters in Constants, which keep the time zone they were
        // created with, so the default time zone is aligned with them; numbers are formatted in the default locale.
        originalTimeZone = TimeZone.default
        originalLocale = Locale.default
        TimeZone.setDefault(Constants.dayFormat.timeZone)
        Locale.setDefault(Locale.US)
    }

    void cleanup() {
        TimeZone.setDefault(originalTimeZone)
        Locale.setDefault(originalLocale)
    }

    /** Noon on the given day, so that day boundaries are never close. */
    private static Date day(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance(Constants.dayFormat.timeZone)
        calendar.clear()
        calendar.set(year, month - 1, dayOfMonth, 12, 0, 0)
        return calendar.time
    }

    private static ConsumptionFact fact(String productId, Date date, BigDecimal quantity) {
        return new ConsumptionFact(productKey: new ProductDimension(productId: productId),
                transactionDateKey: new DateDimension(date: date), quantity: quantity)
    }

    private static Map<String, Object> stringKeys(Map row) {
        return row.collectEntries { key, value -> [(key.toString()): value] }
    }

    private Product product(String id, Map properties) {
        Product product = new Product(properties)
        product.id = id
        return product.save(validate: false, failOnError: true)
    }

    void 'the daily cross tab has one row per product with a column per day and totals'() {
        given:
        product('p1', [productCode: 'P1', name: 'Paracetamol', costPerUnit: 1.5,
                       tags: [new Tag(tag: 'urgent'), new Tag(tag: 'cold')] as Set])
        product('p2', [productCode: 'P2', name: 'Bandage', pricePerUnit: 2])
        List<ConsumptionFact> facts = [
                fact('p1', day(2026, 6, 1), 4),
                fact('p1', day(2026, 6, 1), 1),
                fact('p2', day(2026, 6, 2), 3),
                fact('p1', day(2026, 6, 3), 2),
        ]

        when:
        List<Map> rows = service.generateCrossTab(facts, day(2026, 6, 1), day(2026, 6, 3), 'daily')

        then:
        rows.size() == 2
        stringKeys(rows[0]) == [
                'Code'          : 'P1',
                'Name'          : 'Paracetamol',
                'Tags'          : '"cold,urgent"',
                'Catalogs'      : '',
                'Unit Cost'     : '1.5',
                '01'            : 5,
                '02'            : 0,
                '03'            : 2,
                'Total Demand'  : 0,
                'Total Canceled': 0,
                'Total Issued'  : 7,
                'Total Cost'    : '10.5',
                'Average Issued': 2.3333333333,
                'Average Cost'  : '3.5',
        ]
        // Without a cost per unit the price per unit is used
        stringKeys(rows[1]).subMap(['Code', 'Unit Cost', '01', '02', '03', 'Total Issued', 'Total Cost']) == [
                'Code'        : 'P2',
                'Unit Cost'   : '2',
                '01'          : 0,
                '02'          : 3,
                '03'          : 0,
                'Total Issued': 3,
                'Total Cost'  : '6',
        ]
    }

    void 'a product without cost or price is valued at zero'() {
        given:
        product('p1', [productCode: 'P1', name: 'Free sample'])

        when:
        List<Map> rows = service.generateCrossTab([fact('p1', day(2026, 6, 1), 10)], day(2026, 6, 1), day(2026, 6, 2), 'daily')

        then:
        rows.size() == 1
        rows[0]['Unit Cost'] == '0'
        rows[0]['Total Issued'] == 10
        rows[0]['Total Cost'] == '0'
        rows[0]['Average Issued'] == 5
    }

    @Unroll
    @PendingFeature(reason = 'OBAF does not default a null groupBy (CSV download) to default, so 3 days come out as one month column')
    void 'groupBy #groupBy over #days day(s) uses #description columns'() {
        given:
        product('p1', [productCode: 'P1', name: 'Paracetamol'])
        Date start = day(2025, 1, 10)
        Date end = start + days

        when:
        Map row = stringKeys(service.generateCrossTab([fact('p1', start, 1)], start, end, groupBy)[0])
        List<String> dateColumns = row.keySet().findAll { it ==~ /\d+(-\d+)?/ } as List

        then:
        dateColumns == expectedColumns(start, end, format)

        where:
        groupBy   | days || format                    | description
        'default' | 3    || Constants.dayFormat       | 'day'
        null      | 3    || Constants.dayFormat       | 'day (no grouping given, as the CSV download sends)'
        null      | 90   || Constants.yearMonthFormat | 'month (no grouping given)'
        'default' | 30   || Constants.weekFormat      | 'week'
        'default' | 90   || Constants.yearMonthFormat | 'month'
        'default' | 400  || Constants.yearFormat      | 'year'
        'default' | 0    || Constants.yearMonthFormat | 'month'
        'daily'   | 3    || Constants.dayFormat       | 'day'
        'weekly'  | 3    || Constants.weekFormat      | 'week'
        'monthly' | 3    || Constants.yearMonthFormat | 'month'
        'yearly'  | 3    || Constants.yearFormat      | 'year'
    }

    private static List<String> expectedColumns(Date start, Date end, java.text.DateFormat format) {
        return (start..end).collect { format.format(it) }.unique()
    }

    void 'quantities are summed into the month column of a monthly cross tab'() {
        given:
        product('p1', [productCode: 'P1', name: 'Paracetamol'])
        List<ConsumptionFact> facts = [
                fact('p1', day(2026, 1, 5), 2),
                fact('p1', day(2026, 1, 25), 3),
                fact('p1', day(2026, 3, 1), 7),
        ]

        when:
        Map row = stringKeys(service.generateCrossTab(facts, day(2026, 1, 1), day(2026, 3, 31), 'monthly')[0])

        then:
        row.subMap(['2026-01', '2026-02', '2026-03', 'Total Issued', 'Average Issued']) ==
                ['2026-01': 5, '2026-02': 0, '2026-03': 7, 'Total Issued': 12, 'Average Issued': 4]
    }

    void 'an empty fact list gives an empty cross tab'() {
        expect:
        service.generateCrossTab([], day(2026, 6, 1), day(2026, 6, 3), 'daily') == []
    }
}
