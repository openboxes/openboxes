package org.pih.warehouse.exporter

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Workbook

/**
 * Holds the configuration for styling the individual cells of an Excel file.
 *
 * Cell styles are always associated with a workbook (they cannot exist in isolation) so we can't make them static.
 */
class ExcelCellStyles {

    // We standardize on ISO strings for dates, so that they display the same for every user regardless
    // of their timezone, locale, or the configuration of their Excel install.
    //
    // CSV exports use the same patterns (see the getCsvFormatter implementations), since CSVs are often opened in
    // Excel. If you change these, change those as well. Note that Excel can't store timezones, so datetimes are
    // written in the user's timezone (see ExcelWriter), which is also what imports assume.
    //
    // NOTE: These are Excel number format codes, NOT Java DateTimeFormatter patterns. In Excel, "mm" means month
    //       unless it directly follows "hh" or precedes "ss", in which case it means minutes.
    private static final String DATE_FORMAT = "yyyy-mm-dd"
    private static final String DATE_TIME_FORMAT = "yyyy-mm-dd hh:mm:ss"

    private final Workbook workbook

    private CellStyle dateStyle
    private CellStyle dateTimeStyle

    ExcelCellStyles(Workbook workbook) {
        this.workbook = workbook
    }

    /**
     * Fetches the {@link CellStyle} to use for the given class.
     *
     * @param valueClass The value type to fetch the style for
     * @return The style to apply to the cell, or null if the cell doesn't need styling
     */
    CellStyle getStyleForValue(Class valueClass) {
        // java.util.Date is converted to a LocalDate or Instant by this point (see BulkDataWriter.normalizeValue)
        switch (valueClass) {
            case LocalDate:
                return getDateStyle()
            case Instant:
                return getDateTimeStyle()
            case ZonedDateTime:
                return getDateTimeStyle()
            default:
                return null
        }
    }

    private CellStyle getDateStyle() {
        if (dateStyle) {
            return dateStyle
        }

        CellStyle style = workbook.createCellStyle()
        style.setDataFormat(workbook.creationHelper.createDataFormat().getFormat(DATE_FORMAT))

        dateStyle = style
        return dateStyle
    }

    private CellStyle getDateTimeStyle() {
        if (dateTimeStyle) {
            return dateTimeStyle
        }

        CellStyle style = workbook.createCellStyle()
        style.setDataFormat(workbook.creationHelper.createDataFormat().getFormat(DATE_TIME_FORMAT))

        dateTimeStyle = style
        return dateTimeStyle
    }
}
