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
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang.StringUtils
import org.pih.warehouse.util.LocalizationUtil

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
}
