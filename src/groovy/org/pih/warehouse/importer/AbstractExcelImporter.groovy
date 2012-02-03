package org.pih.warehouse.importer

import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public abstract class AbstractExcelImporter {
	String inFile = null
	InputStream inStr = null
	HSSFWorkbook workbook = null
	HSSFSheet sheet = null

	HSSFFormulaEvaluator evaluator = null;
	public AbstractExcelImporter(String fileName) {
		this.inFile = fileName
		inStr = new FileInputStream(inFile)
		workbook = new HSSFWorkbook(inStr)
		evaluator = new HSSFFormulaEvaluator(workbook)
	}

	def close() {
		inStr.close()
	}

	abstract void validateData(ImportDataCommand command);
	
	abstract void importData(ImportDataCommand command)
		
}
