// Vendored from org.grails.plugins:excel-import:3.0.2 (Apache-2.0); the plugin has no Grails 7 release.
// Source: repo.grails.org excel-import-3.0.2-sources.jar. Unmodified except this header.
package org.grails.plugins.excelimport

import org.apache.poi.ss.usermodel.Cell

enum ImportSeverityLevelEnum {
	Ignore,
	Warning, //just provide warnings during smaller issues
	Error //prevent upload for severe errors
}

enum ImportSeverityMappingEnum {
	//add another mappings as needed
	IgnoreBlankWarningOtherwise({actualType, expectedType ->
		actualType == Cell.CELL_TYPE_BLANK? ImportSeverityLevelEnum.Ignore: ImportSeverityLevelEnum.Warning
	}),
	IgnoreBlankErrorOtherwise({actualType, expectedType ->
		actualType == Cell.CELL_TYPE_BLANK? ImportSeverityLevelEnum.Ignore: ImportSeverityLevelEnum.Error
	}),
	//Crucial
	ErrorAll({actualType, expectedType ->
		ImportSeverityLevelEnum.Error
	}),
	//Required - All fields indicated required (green on excel, starred on edit prop page)
	WarningAll({actualType, expectedType ->
		ImportSeverityLevelEnum.Warning
	}),
	IgnoreAll({actualType, expectedType ->
		ImportSeverityLevelEnum.Ignore
	});


	private final def mappingClo;

	ImportSeverityMappingEnum(mappingClo) {
		this.mappingClo = mappingClo
	}

	/**
	 *
	 * @param actualType value from Cell.CELL_TYPE_*
	 * @param expectedType value from ExpectedPropertyType enum
	 * @return value from ImportSeverityLevelEnum
	 */
	def severityLevel(actualType, expectedType) {
		mappingClo(actualType, expectedType) ?: ImportSeverityLevelEnum.Warning
	}
}


