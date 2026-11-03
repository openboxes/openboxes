// Vendored from org.grails.plugins:excel-import:3.0.2 (Apache-2.0); the plugin has no Grails 7 release.
// Source: repo.grails.org excel-import-3.0.2-sources.jar. Unmodified except this header.
package org.grails.plugins.excelimport

import org.apache.poi.ss.usermodel.Cell

class NoopImportCellCollector implements ImportCellCollector {

	@Override
	void reportCell(Cell cell, Object propertyConfiguration) {
	}

	@Override
	void checkReportRow(Object row, Object config, Object excelImportService) {
	}

	@Override
	boolean checkReportValue(Object value, Cell cell, Object propertyConfiguration) {
		return false
	}

	@Override
	void report(Object message) {
	}

	@Override
	void reportPrepend(Object message) {
	}

	public static final NoopInstance = new NoopImportCellCollector()

}
