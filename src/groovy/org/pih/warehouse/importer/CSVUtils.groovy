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

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.pih.warehouse.util.LocalizationUtil
import org.springframework.web.multipart.MultipartFile

import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat

/**
 * Handy functions for parsing data from, and writing it to, CSV files.
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

    /**
     * Parse a string representation of a number into a BigDecimal.
     *
     * This method correctly handles grouping punctuation so long as it
     * matches the current locale.
     */
    static BigDecimal parseNumber(String s, String fieldName = 'unknown_field') {
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
            return format.parse(s.replace('†', ''))
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse expected numeric value ${fieldName}=${s}", e)
        }
    }

    /**
     * Parse a string into an integer, even with grouping punctuation.
     */
    static int parseInteger(String s, String fieldName = 'unknown_field') {
        try {
            return parseNumber(s, fieldName).intValueExact()
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Expected integer value for ${fieldName}=${s}", e)
        }
    }

    /**
     * Format a date for inclusion in a CSV file.
     *
     * @param date a Date object
     * @return a localized string representation of `date` that Excel can reliably import via CSV.
     *
     * We start with the current locale's SHORT format for day, month and year,
     * then force days and months to be two characters wide, and years, four:
     * e.g., January 2, 2023, in the US locale, is formatted 01/02/2023.
     */
    static String formatDate(Date date, Boolean includeTime = false) {
        if (!date) {
            return ''
        }

        String localizedDateFormat = (DateFormat.getDateInstance(
            DateFormat.SHORT, LocalizationUtil.localizationService.currentLocale
        ) as SimpleDateFormat).toPattern()

        def adjustedFormatter = new SimpleDateFormat(
            localizedDateFormat.concat(includeTime ? ' hh:mm:ss' : '').replaceAll(
                /y+/, 'yyyy').replaceAll(
                /M+/, 'MM').replaceAll(
                /d+/, 'dd'),
            LocalizationUtil.localizationService.currentLocale
        )

        return adjustedFormatter.format(date)
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
        return formatInteger(args.get('number'))
    }

    /**
     * Format a floating-point value for inclusion in a CSV file.
     *
     * @param number a decimal/double/float representing an int
     * @return a string representation of `number` suitable for CSV export
     *
     * Unlike Grails's formatNumber, this method omits grouping punctuation:
     * output is "1234567.8", not "1,234,567.8".
     *
     * It is the caller's responsibility to call escapeCsv() if appropriate.
     */
    static String formatNumber(Number number, Integer maxFractionDigits = null) {
        DecimalFormat format = DecimalFormat.getNumberInstance(LocalizationUtil.localizationService.currentLocale)
        format.parseBigDecimal = true
        if (maxFractionDigits) {
            format.maximumFractionDigits = maxFractionDigits
        }
        return format.format(number).trim()
    }

    /* FIXME replace with @NamedVariant after grails migration */
    static String formatNumber(Map args) {
        return formatNumber(args.get('number'), args.get('maxFractionDigits'))
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
        symbols.currencySymbol = ''
        format.decimalFormatSymbols = symbols

        if (isUnitPrice) {
            format.maximumFractionDigits = Math.max(format.maximumFractionDigits, UNIT_PRICE_MIN_DECIMAL_PLACES)
        }

        return format.format(number).trim()
    }

    /* FIXME replace with @NamedVariant after grails migration */
    static String formatCurrency(Map args) {
        return formatCurrency(args.get('number'), args.get('currencyCode'), args.get('isUnitPrice', false))
    }

    /**
     * Format unit of measure information for CSV as a single column.
     */
    static String formatUnitOfMeasure(String quantityUom, Number quantityPerUom) {
        // FIXME default value should be localized, but presently is "EA" everywhere
        def prefix = quantityUom ?: 'EA'
        def suffix = formatInteger(quantityPerUom ?: 1)
        return "${prefix}/${suffix}"
    }

    /**
     * Create a CSVPrinter object with sensible defaults.
     */
    static CSVPrinter getCSVPrinter() {
        return new CSVPrinter(new StringBuilder(), CSVFormat.DEFAULT)
    }

    /**
     * Print a list of maps to CSV.
     * @param list a List of maps, all containing identical keys.
     * @return a string representation of the data in CSV format.
     *
     * The keys from the first map encountered are taken as column headers.
     */
    static String dumpMaps(List<Map<String, String>> list) {
        def csv = getCSVPrinter()
        if (list) {
            csv.printRecord(list[0].keySet())
            list.each { entry ->
                csv.printRecord(entry.values())
            }
        }
        return csv.out.toString()
    }

    /**
     * Parse CSV records from a File object.
     */
    static List<CSVRecord> parseRecords(File file, List<String> requiredColumnNames = null, boolean hasHeader = true, boolean overrideHeaders = false, char delimiter = 0) {
        final InputStream stream = FileUtils.openInputStream(file)
        try {
            return parseRecords(stream, requiredColumnNames, hasHeader, overrideHeaders, delimiter)
        } finally {
            stream.close()
        }
    }

    /**
     * Parse CSV records from a MultipartFile.
     */
    static List<CSVRecord> parseRecords(MultipartFile multipartFile, List<String> requiredColumnNames = null, boolean hasHeader = true, boolean overrideHeaders = false, char delimiter = 0) {
        if (multipartFile.empty) {
            throw new IllegalArgumentException('File cannot be empty')
        }

        if (multipartFile.contentType != 'text/csv') {
            throw new IllegalArgumentException('File must be in CSV format')
        }

        final InputStream stream = multipartFile.inputStream
        try {
            return parseRecords(stream, requiredColumnNames, hasHeader, overrideHeaders, delimiter)
        } finally {
            stream.close()
        }
    }


    /**
     * Parse CSV records from a String.
     */
    static List<CSVRecord> parseRecords(String text, List<String> requiredColumnNames = null, boolean hasHeader = true, boolean overrideHeaders = false, char delimiter = 0) {
        final InputStream stream = IOUtils.toInputStream(text)
        try {
            return parseRecords(stream, requiredColumnNames, hasHeader, overrideHeaders, delimiter)
        } finally {
            stream.close()
        }
    }

    /**
     * Parse CSV records from a URL.
     */
    static List<CSVRecord> parseRecords(URL url, List<String> requiredColumnNames = null, boolean hasHeader = true, boolean overrideHeaders = false, char delimiter = 0) {
        final InputStream stream = url.openStream()
        try {
            return parseRecords(stream, requiredColumnNames, hasHeader, overrideHeaders, delimiter)
        } finally {
            stream.close()
        }
    }

    /**
     * Parse CSV records from an open InputStream.
     *
     * @param stream an open InputStream
     * @param requiredColumnNames a list of column names that must be
     *        present in the CSV file
     * @param hasHeader if true, the first line of the CSV file is
     *        assumed to contain column headers
     * @param overrideHeaders if true, the column headers in the CSV
     *        file are ignored (if present) and the column names in
     *        `requiredColumnNames` are used instead
     * @param delimiter the delimiter to use (defaults to comma)
     */
    static List<CSVRecord> parseRecords(InputStream stream, List<String> requiredColumnNames = null, boolean hasHeader = true, boolean overrideHeaders = false, char delimiter = 0) {
        CSVFormat format = CSVFormat.DEFAULT
        if (delimiter) {
            format = format.withDelimiter(delimiter)
        }
        if (overrideHeaders) {
            format = format.withHeader(requiredColumnNames).withSkipHeaderRecord(hasHeader)
        } else if (hasHeader) {
            format = format.withFirstRecordAsHeader()
        }

        CSVParser parser

        try {
            parser = CSVParser.parse(stream, StandardCharsets.UTF_8, format)
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file, expected headers: ${requiredColumnNames}", e)
        }

        validateColumnNames(parser, requiredColumnNames)
        parser.records.each { validateRow(it) }
        return parser.records
    }

    /**
     * Make sure CSV input has the column names we expect.
     */
    static void validateColumnNames(CSVParser parser, List<String> requiredColumnNames) {
        if (!parser.headerMap || !requiredColumnNames) {
            return  // can't validate much without column headers
        }
        if (parser.headerMap.keySet().size() < requiredColumnNames.size()) {
            throw new IllegalArgumentException(
                "Expected at least ${requiredColumnNames.size()} columns, got just ${parser.headerMap.keySet().size()}"
            )
        }
        requiredColumnNames.each {
            if (!parser.headerMap.containsKey(it)) {
                throw new IllegalArgumentException(
                    "CSV column names ${parser.headerMap.keySet()} do not match required column names: ${requiredColumnNames}"
                )
            }
        }
    }

    /**
     * Make sure a row from a CSV file has the fields we expect (OK if they are null).
     */
    static void validateRow(CSVRecord record) {
        if (!record.consistent) {
            throw new IllegalArgumentException("CSV record is missing required field(s): ${record.toMap()}")
        }
    }
}
