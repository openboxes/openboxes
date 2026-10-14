package org.pih.warehouse.regression.platform.importer

import spock.lang.PendingFeature

import org.pih.warehouse.importer.*
import org.apache.commons.csv.CSVPrinter
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.LocalizationService

/**
 * The number helpers read the locale from the localization service, so each feature pins the locale through a
 * stubbed service to show both the US and the German separators.
 */
@Unroll
class CSVUtilsSpec extends Specification {

    private void useLocale(Locale locale) {
        LocalizationService localizationService = Stub(LocalizationService) {
            getCurrentLocale() >> locale
        }
        GroovyMock(LocalizationUtil, global: true)
        LocalizationUtil.getLocalizationService() >> localizationService
    }

    void 'parseNumber should read #text as #expected in the #locale locale'() {
        given:
        useLocale(locale)

        expect:
        CSVUtils.parseNumber(text) == expected

        where:
        text            | locale         || expected
        '1234.5'        | Locale.US      || 1234.5G
        '1,234,567.891' | Locale.US      || 1234567.891G
        '-12'           | Locale.US      || -12G
        '1.234,5'       | Locale.GERMANY || 1234.5G
        '0,25'          | Locale.GERMANY || 0.25G
    }

    void 'parseNumber should return an exact decimal rather than a floating point value'() {
        given:
        useLocale(Locale.US)

        expect:
        CSVUtils.parseNumber('0.1') instanceof BigDecimal
        CSVUtils.parseNumber('0.1') == 0.1G
    }

    void 'parseNumber should ignore the dagger grouping marks written by old exports'() {
        given:
        useLocale(Locale.US)

        expect:
        CSVUtils.parseNumber('1\u2020234\u2020567') == 1234567G
    }

    void 'parseNumber should reject a blank value and name the field'() {
        given:
        useLocale(Locale.US)

        when:
        CSVUtils.parseNumber(text, 'quantity')

        then:
        IllegalArgumentException e = thrown()
        e.message == 'quantity is empty or unset'

        where:
        text << [null, '', '   ']
    }

    void 'parseNumber should reject a value that is not a number'() {
        given:
        useLocale(Locale.US)

        when:
        CSVUtils.parseNumber('abc', 'unitPrice')

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Unable to parse expected numeric value unitPrice=abc'
    }

    void 'parseNumber should fall back to a generic field name'() {
        given:
        useLocale(Locale.US)

        when:
        CSVUtils.parseNumber('')

        then:
        IllegalArgumentException e = thrown()
        e.message == 'unknown_field is empty or unset'
    }

    void 'parseInteger should accept grouped whole numbers'() {
        given:
        useLocale(Locale.US)

        expect:
        CSVUtils.parseInteger('1,000') == 1000
        CSVUtils.parseInteger('12.0') == 12
        CSVUtils.parseInteger('-7') == -7
    }

    void 'parseInteger should reject a fractional value'() {
        given:
        useLocale(Locale.US)

        when:
        CSVUtils.parseInteger('1.5', 'quantity')

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Expected integer value for quantity=1.5'
    }

    void 'parseInteger should reject a blank value like parseNumber does'() {
        given:
        useLocale(Locale.US)

        when:
        CSVUtils.parseInteger(' ', 'quantity')

        then:
        IllegalArgumentException e = thrown()
        e.message == 'quantity is empty or unset'
    }

    void 'formatInteger should write #number without grouping as #expected'() {
        given:
        useLocale(locale)

        expect:
        CSVUtils.formatInteger(number) == expected

        where:
        number   | locale         || expected
        1234567  | Locale.US      || '1234567'
        1234567  | Locale.GERMANY || '1234567'
        -42      | Locale.US      || '-42'
        12.0G    | Locale.US      || '12'
        0        | Locale.US      || '0'
    }

    void 'formatInteger should also take the number as a named argument'() {
        given:
        useLocale(Locale.US)

        expect:
        CSVUtils.formatInteger(number: 5000) == '5000'
    }

    void 'formatCurrency should write #number in #currencyCode without a symbol or grouping as #expected'() {
        given:
        useLocale(locale)

        expect:
        CSVUtils.formatCurrency(number, currencyCode) == expected

        where:
        number   | currencyCode | locale         || expected
        1234.5G  | 'USD'        | Locale.US      || '1234.50'
        1234.5G  | 'EUR'        | Locale.GERMANY || '1234,50'
        2.5075G  | 'USD'        | Locale.US      || '2.51'
        5        | null         | Locale.US      || '5.00'
    }

    void 'formatCurrency should keep at least four decimals for a unit price'() {
        given:
        useLocale(Locale.US)

        expect:
        CSVUtils.formatCurrency(2.5075G, 'USD', true) == '2.5075'
        CSVUtils.formatCurrency(1.5G, 'USD', true) == '1.50'
    }

    void 'formatCurrency should also take its arguments by name'() {
        given:
        useLocale(Locale.US)

        expect:
        CSVUtils.formatCurrency(number: 2.5075G, currencyCode: 'USD', isUnitPrice: true) == '2.5075'
        CSVUtils.formatCurrency(number: 2.5075G, currencyCode: 'USD') == '2.51'
    }

    void 'formatCurrency should reject an unknown currency code'() {
        given:
        useLocale(Locale.US)

        when:
        CSVUtils.formatCurrency(1, 'XYZ1')

        then:
        thrown(IllegalArgumentException)
    }

    @PendingFeature(reason = 'OBAF formatUnitOfMeasure ignores the EA/1 defaults and integer format (BX/10.0, null/null)')
    void 'formatUnitOfMeasure should join the unit and the quantity per unit: #uom #perUom -> #expected'() {
        given:
        useLocale(Locale.US)

        expect:
        CSVUtils.formatUnitOfMeasure(uom, perUom) == expected

        where:
        uom  | perUom                 || expected
        'BX' | 10                     || 'BX/10'
        'BX' | new BigDecimal('10.0') || 'BX/10'
        'BX' | 1000                   || 'BX/1000'
        null | 10                     || 'EA/10'
        'BX' | null                   || 'BX/1'
        null | null                   || 'EA/1'
    }

    void 'the csv printer should quote values that contain the separator'() {
        given:
        CSVPrinter printer = CSVUtils.getCSVPrinter()

        when:
        printer.printRecord('Code', 'Name, with comma', 'Say "hi"')

        then:
        printer.out.toString() == 'Code,"Name, with comma","Say ""hi"""\r\n'
    }

    void 'getSeparator should detect #description'() {
        expect:
        CSVUtils.getSeparator(text, columnCount) == expected as char

        where:
        description                                      | text                       | columnCount || expected
        'a semicolon separated file'                     | 'a;b;c\n1;2;3'             | 3           || ';'
        'a tab separated file'                           | 'a\tb\tc\r\n1\t2\t3'       | 3           || '\t'
        'a comma separated file'                         | 'a,b,c\n1,2,3'             | 3           || ','
        'commas when semicolons give the wrong columns'  | 'a;b,c,d\n1;2,3,4'         | 3           || ','
        'only the header line'                           | 'a;b\n1,2,3,4,5'           | 2           || ';'
    }

    void 'the byte order mark should be added for spreadsheet tools and removed again on import'() {
        when:
        String withBom = CSVUtils.prependBomToCsvString('a,b')

        then:
        withBom.length() == 4
        withBom.charAt(0) == '\ufeff' as char
        CSVUtils.stripBomIfPresent(withBom) == 'a,b'
        CSVUtils.stripBomIfPresent('a,b') == 'a,b'
    }

    void 'detectCsvCharset should recognize a UTF-8 file'() {
        given:
        File file = File.createTempFile('csv-utils', '.csv')
        file.deleteOnExit()
        file.setText('Nom,Pr\u00e9nom,Quantit\u00e9\n' + ('Jos\u00e9,\u00c9lo\u00efse,cr\u00e8me br\u00fbl\u00e9e\n' * 10), 'UTF-8')

        expect:
        CSVUtils.detectCsvCharset(file) == 'UTF-8'

        cleanup:
        file.delete()
    }

    void 'detectCsvCharset should recognize a plain ASCII file'() {
        given:
        File file = File.createTempFile('csv-utils', '.csv')
        file.deleteOnExit()
        file.setText('code,name,quantity', 'US-ASCII')

        expect:
        CSVUtils.detectCsvCharset(file) == 'US-ASCII'

        cleanup:
        file.delete()
    }

    void 'detectCsvCharset should fall back to MacRoman when nothing can be detected'() {
        given: 'an escape character that starts no known escape based encoding'
        File file = File.createTempFile('csv-utils', '.csv')
        file.deleteOnExit()
        file.setText('code\u001bname,quantity', 'US-ASCII')

        expect:
        CSVUtils.detectCsvCharset(file) == 'MacRoman'

        cleanup:
        file.delete()
    }

    void 'csvToObjects should key each row by the camel cased header and trim the values'() {
        given:
        String csv = 'Product Code, Quantity Per UoM ,lot-number\n P1 , 10 ,L-1\nP2,,\n'

        when:
        List<Map<String, String>> rows = CSVUtils.csvToObjects(csv)

        then:
        rows == [
                [productCode: 'P1', quantityPerUom: '10', lotNumber: 'L-1'],
                [productCode: 'P2', quantityPerUom: '', lotNumber: ''],
        ]
    }

    @PendingFeature(reason = 'OBAF toCamelCase lowercases first, so binLocation becomes binlocation')
    void 'csvToObjects should key camelCase header #header as #key'() {
        expect:
        CSVUtils.csvToObjects("${header}\nX\n") == [[(key): 'X']]

        where:
        header            || key
        'binLocation'     || 'binLocation'
        'BinLocation'     || 'binLocation'
        'productCode'     || 'productCode'
        'expirationDate'  || 'expirationDate'
        'Bin Location'    || 'binLocation'
        'bin_location'    || 'binLocation'
        'quantity'        || 'quantity'
        'QUANTITY'        || 'quantity'
        'LOT NUMBER'      || 'lotNumber'
        ' lotNumber '     || 'lotNumber'
    }

    void 'getColumnData should return one column of every row'() {
        expect:
        CSVUtils.getColumnData('Code,Name\nA,Apple\nB,Banana\n', 'name') == ['Apple', 'Banana']
    }

    void 'parseCsvBooleanField should read #value as #expected'() {
        expect:
        CSVUtils.parseCsvBooleanField(value, 3) == expected

        where:
        value    || expected
        'true'   || true
        ' Yes '  || true
        'T'      || true
        '1'      || true
        'y'      || true
        'false'  || false
        'NO'     || false
        '0'      || false
        'f'      || false
        'n'      || false
    }

    void 'parseCsvBooleanField should return the default for a blank value'() {
        expect:
        CSVUtils.parseCsvBooleanField(value, 3, defaultValue) == defaultValue

        where:
        value | defaultValue
        null  | true
        ''    | false
        '  '  | true
        '  '  | null
    }

    void 'parseCsvBooleanField should reject anything else and report the row'() {
        when:
        CSVUtils.parseCsvBooleanField('maybe', 7)

        then:
        RuntimeException e = thrown()
        e.message.contains('at row 7')
        e.message.startsWith('Active field has to be either empty or a boolean value')
    }
}
