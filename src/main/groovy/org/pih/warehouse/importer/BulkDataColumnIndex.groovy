package org.pih.warehouse.importer

import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.util.CellReference

import org.pih.warehouse.core.parser.IntegerParser

/**
 * For working with the column indexes of bulk data documents. Columns can be represented as either zero-indexed
 * numerical Strings, or as letters (which is how they appear in Excel).
 *
 * The first column can be represented as "0" or "A", the second as "1" or "B", the 27th as "26" or "AA"...
 */
class BulkDataColumnIndex {

    /**
     * @param columnIndex The numerical or letter index of the column.
     * @return The zero-indexed numerical index of the column, or null if the given index is blank.
     */
    static Integer toInt(String columnIndex) {
        String index = columnIndex?.trim()
        if (StringUtils.isBlank(index)) {
            return null
        }
        if (StringUtils.isNumeric(index)) {
            return IntegerParser.parseString(index)
        }
        if (StringUtils.isAlpha(index)) {
            return CellReference.convertColStringToIndex(index.toUpperCase())
        }
        throw new IllegalArgumentException("Invalid column index: '${columnIndex}'. Column indexes must be " +
                "zero-index integers (ex: '0', '1') or letters as they appear in Excel columns (ex: 'A', 'B').")
    }

    /**
     * Converts a column mapping keyed on numerical or letter column indexes to one keyed on zero-index numerical
     * column indexes.
     */
    static Map<Integer, String> toIntKeys(Map<String, String> columnMapping) {
        Map<Integer, String> mapping = [:]
        Map<Integer, String> originalKeys = [:]
        columnMapping?.each { String columnIndex, String fieldName ->
            Integer index = toInt(columnIndex)
            if (index == null) {
                throw new IllegalArgumentException("Column mapping contains a blank column index for '${fieldName}'.")
            }
            if (mapping.containsKey(index)) {
                throw new IllegalArgumentException("Column mapping contains columns '${originalKeys[index]}' " +
                        "and '${columnIndex}' that refer to the same column index '${index}'.")
            }
            mapping.put(index, fieldName)
            originalKeys.put(index, columnIndex)
        }
        return mapping
    }
}
