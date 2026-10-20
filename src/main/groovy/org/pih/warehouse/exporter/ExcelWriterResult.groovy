package org.pih.warehouse.exporter

import org.apache.poi.ss.usermodel.Workbook

/**
 * The result of writing to an Excel file.
 */
class ExcelWriterResult extends BulkDataWriterResult<Workbook> {

    @Override
    void close() {
        result.close()
    }
}
