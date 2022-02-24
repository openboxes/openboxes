/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import org.codehaus.groovy.grails.commons.GrailsApplication

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile;

class UploadService {

    GrailsApplication grailsApplication
    FileService fileService

    boolean transactional = false

    File createLocalFile(String filename) {
        log.info "Create local file ${filename}"
        def uploadsDirectory = findOrCreateUploadsDirectory()
        return new File(uploadsDirectory, filename)
    }

    File findOrCreateUploadsDirectory() {
        String directoryPath = grailsApplication.config.openboxes.uploads.location
        log.info("Find or create uploads directory ${directoryPath}")
        if (!directoryPath) {
            throw new IllegalStateException("Directory path for uploads directory must be configured in openboxes-config.properties [openboxes.uploads.location]")
        }
        // Replace tilde with user home
        directoryPath = directoryPath.replaceFirst("^~", System.getProperty("user.home"))
        return fileService.createDirectory(directoryPath)
    }

    public File convertXlsx2xls(MultipartFile inpFn) throws InvalidFormatException,IOException {
        InputStream inString = inpFn.inputStream;
        String outFn = inpFn.getOriginalFilename()?.replaceAll(".xlsx", "_copy.xls");
        File outF = null
        try {
            XSSFWorkbook wbIn = new XSSFWorkbook(inString);
            outF = new File(outFn);
            if (outF.exists()) {
                outF.delete();
            }

            Workbook wbOut = new HSSFWorkbook();
            int sheetCnt = wbIn.getNumberOfSheets();
            for (int i = 0; i < sheetCnt; i++) {
                Sheet sIn = wbIn.getSheetAt(0);
                Sheet sOut = wbOut.getSheet(sIn.getSheetName());
                if(!sOut){
                    sOut = wbOut.createSheet(sIn.getSheetName())
                }
                Iterator<Row> rowIt = sIn.rowIterator();
                while (rowIt.hasNext()) {
                    Row rowIn = rowIt.next();
                    Row rowOut = sOut.createRow(rowIn.getRowNum());

                    Iterator<Cell> cellIt = rowIn.cellIterator();
                    while (cellIt.hasNext()) {
                        Cell cellIn = cellIt.next();
                        Cell cellOut = rowOut.createCell(cellIn.getColumnIndex(), cellIn.getCellType());

                        switch (cellIn.getCellType()) {
                            case Cell.CELL_TYPE_BLANK: break;

                            case Cell.CELL_TYPE_BOOLEAN:
                                cellOut.setCellValue(cellIn.getBooleanCellValue());
                                break;

                            case Cell.CELL_TYPE_ERROR:
                                cellOut.setCellValue(cellIn.getErrorCellValue());
                                break;

                            case Cell.CELL_TYPE_FORMULA:
                                cellOut.setCellFormula(cellIn.getCellFormula());
                                break;

                            case Cell.CELL_TYPE_NUMERIC:
                                cellOut.setCellValue(cellIn.getNumericCellValue());
                                break;

                            case Cell.CELL_TYPE_STRING:
                                cellOut.setCellValue(cellIn.getStringCellValue());
                                break;
                        }

                        CellStyle styleIn = cellIn.getCellStyle();
                        CellStyle styleOut = cellOut.getCellStyle();
                        styleOut.setDataFormat(styleIn.getDataFormat());
                        cellOut.setCellComment(cellIn.getCellComment());

                    }
                }
            }
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outF));
            try {
                wbOut.write(out);
            } finally {
                out.close();
            }
        } finally {
            inString.close();
        }
        return outF;
    }

}
