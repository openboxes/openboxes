/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.importer

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator

// import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook

abstract class AbstractExcelImporter {
    String inFile = null
    InputStream inStr = null
    HSSFWorkbook workbook = null
    HSSFSheet sheet = null

    HSSFFormulaEvaluator evaluator = null

    AbstractExcelImporter(String fileName) {
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
