// Vendored from org.grails.plugins:excel-import:3.0.2 (Apache-2.0); the plugin has no Grails 7 release.
// Source: repo.grails.org excel-import-3.0.2-sources.jar. Unmodified except this header.
package org.grails.plugins.excelimport

import org.apache.poi.hssf.util.CellReference
import org.apache.poi.ss.usermodel.Cell
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


class DefaultImportCellCollector implements ImportCellCollector  {

	static Log log = LogFactory.getLog(DefaultImportCellCollector.class)

	def messagesBySeverityLevel = (ImportSeverityLevelEnum.enumConstants as List).inject([:]){acc, lvl ->
		acc[lvl] = []
		return acc
	}.asImmutable()

	@Override
	void reportCell(Cell cell, Object propertyConfiguration) {
		try {
			log.debug "Reporting cell $cell, config: $propertyConfiguration"
			def message = [
				cell: cell,
				propertyConfiguration: propertyConfiguration,
				severityLevel: severityLevel(cell, propertyConfiguration),
				cellKey: cellKey(cell),
				text: (propertyConfiguration?.expectedType ? """
					expecting a ${propertyConfiguration.expectedType.userViewableName},
					but saw ${cellTypeToString[cell.cellType]} field
				""" : """
					unexpected ${cellTypeToString[cell.cellType]} field
				""")
			]
			message = prependCellMessageText(message)
			report(message)
		} catch (e) {
			log.error "Exception while trying to report potential problem", e
		}
	}

	@Override
	void checkReportRow(row, config, excelImportService) {
		try {
			(config?.rowValidations ?: []).collect {validation ->
				validation(row, config, excelImportService)
			}.findAll {it}.each {message ->
				message = [
					severityLevel: ImportSeverityLevelEnum.Warning, //validation closure could override this
					cellKey: cellKey(message.cell), //validation should return cell
					text: 'Provided value is not valid' //validation closure could override this
				] + message
				message = prependCellMessageText(message)
				report(message)
			}
		} catch (e) {
			log.error "Exception while trying to report potential problem", e
		}
	}

	@Override
	boolean checkReportValue(Object value, Cell cell, Object propertyConfiguration) {
		try {
			(propertyConfiguration?.valueValidations ?: []).collect {validation ->
				validation(value)
			}.findAll {it}.each {message ->
				message = [
					cell: cell,
					propertyConfiguration: propertyConfiguration,
					severityLevel: ImportSeverityLevelEnum.Warning, //validation closure could override this
					cellKey: cellKey(cell),
					text: 'Provided value is not valid' //validation closure could override this
				] + message
				message = prependCellMessageText(message)
				report(message)
			}.isEmpty() == false
		} catch (e) {
			log.error "Exception while trying to report potential problem", e
			return false
		}
	}

	@Override
	void report(message) {
		try {
			messagesBySeverityLevel[message.severityLevel].add(message)
		} catch (e) {
			log.error "Exception while trying to report potential problem", e
		}
	}

	@Override
	void reportPrepend(message) {
		try {
			messagesBySeverityLevel[message.severityLevel].add(0, message)
		} catch (e) {
			log.error "Exception while trying to report potential problem", e
		}
	}

	protected def severityLevel(Cell cell, Object propertyConfiguration) {
		def severityMapping = propertyConfiguration?.severityMapping ?: ImportSeverityMappingEnum.IgnoreBlankWarningOtherwise
		severityMapping.severityLevel(cell.cellType, propertyConfiguration?.expectedType)
	}

	protected def cellKey(Cell cell) {
		new CellReference(cell.sheet.sheetName, cell.rowIndex, cell.columnIndex, true, true).cellRefParts.toList()
	}

	protected def prependCellMessageText(message) {
		def cellMessageText = """
			Problem with
			${message.propertyConfiguration?.userViewableName? "value for '${message.propertyConfiguration?.userViewableName}'": ''}
			(Cell ${message.cellKey[2]}${message.cellKey[1]} on Sheet "${message.cellKey[0]}") -
			${message.text}
		"""
		message + [text: cellMessageText]
	}

	//toString for Cell.CELL_TYPE_*
	protected static def cellTypeToString = [
		(Cell.CELL_TYPE_BLANK): 'blank',
		(Cell.CELL_TYPE_ERROR): 'error',
		(Cell.CELL_TYPE_STRING): 'text',
		(Cell.CELL_TYPE_BOOLEAN): 'boolean',
		(Cell.CELL_TYPE_FORMULA): 'formula',
		(Cell.CELL_TYPE_NUMERIC): 'number'
	].asImmutable()

}	
