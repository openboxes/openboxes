// Vendored from org.grails.plugins:excel-import:3.0.2 (Apache-2.0); the plugin has no Grails 7 release.
// Source: repo.grails.org excel-import-3.0.2-sources.jar. Unmodified except this header.
package org.grails.plugins.excelimport

import org.apache.poi.ss.usermodel.Cell


public interface ImportCellCollector {

	void reportCell(Cell cell, propertyConfiguration)

	void checkReportRow(row, config, excelImportService)

	boolean checkReportValue(Object value, Cell cell, Object propertyConfiguration)

	void report(message)

	void reportPrepend(message)

}
