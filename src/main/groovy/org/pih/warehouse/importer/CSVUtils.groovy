/**
 * Copyright (c) 2021 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse.importer

import grails.plugins.csv.CSVMapReader
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang.StringUtils
import org.mozilla.universalchardet.UniversalDetector
import org.pih.warehouse.core.Constants
import org.pih.warehouse.LocalizationUtil

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat

/**
 * Handy functions for parsing numbers from, and writing them to, CSV files.
 *
 * Grails's numberFormat() routines are optimized for displaying human-
 * readable numbers on a display, and include things like grouping
 * punctuation (the comma in "1,234.5") that make CSV formatting tricky.
 *
 * These functions are locale-aware, and, for unit prices, support at
 * least four decimal places.
 */
class CSVUtils {

    /* support fractional cents in unit prices */
    static int UNIT_PRICE_MIN_DECIMAL_PLACES = 4

    static final Set<String> VALID_BOOLEAN_VALUES = ['t', 'f', 'true', 'false', '1', '0', 'y', 'n', 'yes', 'no']
    static final Set<String> TRUE_BOOLEAN_VALUES = ['t', 'true', '1', 'y', 'yes']

    /**
     * Parse a string representation of a number into a BigDecimal.
     *
     * This method correctly handles grouping punctuation so long as it
     * matches the current locale.
     */
    static BigDecimal parseNumber(String s, String fieldName = "unknown_field") {
        if (StringUtils.isBlank(s)) {
            throw new IllegalArgumentException("${fieldName} is empty or unset")
        }
        DecimalFormat format = DecimalFormat.getNumberInstance(LocalizationUtil.localizationService.currentLocale)
        format.parseBigDecimal = true
        /*
         * OB releases <=0.8.16, in certain locales, erroneously (and uniquely)
         * used daggers as grouping punctuation. No parser expects this behavior,
         * so to read our own old exports we need to filter them out ourselves.
         */
        try {
            return format.parse(s.replace("†", ""))
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse expected numeric value ${fieldName}=${s}")
        }
    }

    /**
     * Parse a string into an integer, even with grouping punctuation.
     */
    static int parseInteger(String s, String fieldName = "unknown_field") {
        try {
            return parseNumber(s, fieldName).intValueExact()
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Expected integer value for ${fieldName}=${s}")
        }
    }

    /**
     * Format an integer value for inclusion in a CSV file.
     *
     * @param number an integer, or a decimal/double/float representing an int
     * @return a string representation of `number` suitable for CSV export
     *
     * Unlike Grails's formatNumber, this method omits grouping punctuation:
     * output is "1234567", not "1,234,567".
     *
     * It is the caller's responsibility to call escapeCsv() if appropriate.
     */
    static String formatInteger(Number number) {
        NumberFormat format = NumberFormat.getIntegerInstance(LocalizationUtil.localizationService.currentLocale)
        format.groupingUsed = false
        return format.format(number).trim()
    }

    /* FIXME replace with @NamedVariant after grails migration */
    static String formatInteger(Map args) {
        return formatInteger(args.get("number"))
    }

    /**
     * Format a currency value for inclusion in a CSV file.
     *
     * @param number the actual amount of money
     * @param currencyCode a string representing the currency units ("USD", "EUR", etc.)
     * @param isUnitPrice if set, treat `value` as a unit price
     * @return a string representation of the currency value suitable for CSV
     *
     * This method returns a string representation of `number` with no currency
     * symbol and no grouping punctuation.
     *
     * The decimal separator depends on the current locale, while the number of
     * digits after the decimal point depends on the `currencyCode` parameter.
     *
     * If `isUnitPrice` is set, allow at least 4 decimal places, so we can
     * properly export e.g. fractional cents.
     *
     * It is the caller's responsibility to call escapeCsv() if appropriate.
     */
    static String formatCurrency(Number number, String currencyCode = null, boolean isUnitPrice = false) {
        DecimalFormat format = DecimalFormat.getCurrencyInstance(LocalizationUtil.localizationService.currentLocale)
        format.groupingUsed = false

        if (currencyCode != null) {
            format.currency = Currency.getInstance(currencyCode)
        }

        DecimalFormatSymbols symbols = format.decimalFormatSymbols
        symbols.currencySymbol = ""
        format.decimalFormatSymbols = symbols

        if (isUnitPrice) {
            format.maximumFractionDigits = Math.max(format.maximumFractionDigits, UNIT_PRICE_MIN_DECIMAL_PLACES)
        }

        return format.format(number).trim()
    }

    /* FIXME replace with @NamedVariant after grails migration */
    static String formatCurrency(Map args) {
        return formatCurrency(args.get("number"), args.get("currencyCode"), args.get("isUnitPrice", false))
    }

    /**
     * Format unit of measure information for CSV as a single column.
     */
    static String formatUnitOfMeasure(String quantityUom, Number quantityPerUom) {
        // FIXME default value should be localized, but presently is "EA" everywhere
        def numerator = quantityUom ?: "EA"
        def denominator = formatInteger(quantityPerUom ?: 1)
        return "${quantityUom}/${quantityPerUom}"
    }

    /**
     * Create a CSVPrinter object with sensible defaults.
     */
    static CSVPrinter getCSVPrinter() {
        return new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT)
    }


    /**
    * Return a CSV separator character based on provided data and expected number of columns.
    *
    * Depending on configuration of users preferred csv editor like Excel or Libre office,
    * when saving a csv file it might reformat the file with a different data separator.
    * Using this utils function we want to figure out which character is used as a data separator in the file.
    *
    * The function takes the first line (which is usually a header) as a sample text to check if it contains the CUSTOM_COLUMN_SEPARATOR character.
    * If it does contain this character, then there is a change that this is the right separator.
    * Then we initialize the field keys based on the provided sample
    * and if the amount of keys matches the expected number columns
    * we can assume that this file uses CUSTOM_COLUMN_SEPARATOR character as a separator
    * */
    static char getSeparator(String text, Integer columnCount) {
        String firstLine = text.split("\\r?\\n").first()
        List<String> customSeparators = [Constants.SEMICOLON_COLUMN_SEPARATOR, Constants.TAB_COLUMN_SEPARATOR]
        // Loop through custom separators and return separator that columns size after splitting == provided columnCount
        String separatorChar = customSeparators
                .find{ String separator ->
                    if (firstLine.contains(separator)) {
                        CSVMapReader csvMapReader = new CSVMapReader(new StringReader(firstLine), [separatorChar: separator])
                        if (csvMapReader.initFieldKeys().size() == columnCount) {
                            return true
                        }
                    }
                }

        return separatorChar ?: Constants.DEFAULT_COLUMN_SEPARATOR;
    }

    static String prependBomToCsvString(String csvString) {
        return '\uFEFF' + csvString
    }

    static String stripBomIfPresent(String csvString) {
        return csvString.replace("\uFEFF", "")
    }


    static String detectCsvCharset(File file) {
        def detector = new UniversalDetector()
        byte[] fileBytes = file.bytes
        detector.handleData(fileBytes, 0, fileBytes.length - 1);
        detector.dataEnd();
        return detector.getDetectedCharset() ?: 'MacRoman';
    }

    static List<Map<String, String>> csvToObjects(String csvString) {
        CSVParser parser = CSVParser.parse(
                csvString,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim()
        )

        return parser.collect { record ->
            record.toMap().collectEntries { k, v ->
                [(toCamelCase(k)): v]
            }
        }
    }

    private static String toCamelCase(String text) {
        String[] parts = text.trim().toLowerCase().split(/[^a-zA-Z0-9]+/)
        if (parts.size() == 0) {
            return ""
        }
        return parts[0] + parts.tail().collect { it.capitalize() }.join()
    }

    static List<String> getColumnData(String csvString, String columnName) {
        List<Map> rows = csvToObjects(csvString)
        return rows[columnName]
    }

    /**
     * Parses a CSV boolean field.
     *
     * @param value the string value from the CSV field
     * @param rowCount the row number in the CSV file, used for error reporting
     * @param defaultValue the value to return if the field is empty or blank
     * @return Boolean true if the value represents a true boolean, false if false
     * @throws RuntimeException if the value is not empty or blank and not a valid boolean
     */
    static Boolean parseCsvBooleanField(String value, int rowCount, Boolean defaultValue = false) {
        if (StringUtils.isBlank(value)) {
            return defaultValue
        }

        String parsedValue = value.trim().toLowerCase()

        if (!(parsedValue in VALID_BOOLEAN_VALUES)) {
            throw new RuntimeException("Active field has to be either empty or a boolean value ${VALID_BOOLEAN_VALUES} at row ${rowCount}")
        }

        return parsedValue in TRUE_BOOLEAN_VALUES
    }
}
