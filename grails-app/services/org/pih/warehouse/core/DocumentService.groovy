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


import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.docx4j.TextUtils
import org.docx4j.XmlUtils
import org.docx4j.convert.out.pdf.PdfConversion
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion
import org.docx4j.jaxb.Context
import org.docx4j.openpackaging.io.SaveToZipFile
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import org.docx4j.openpackaging.parts.relationships.Namespaces
import org.docx4j.wml.Body
import org.docx4j.wml.BooleanDefaultTrue
import org.docx4j.wml.Document
import org.docx4j.wml.P
import org.docx4j.wml.R
import org.docx4j.wml.RPr
import org.docx4j.wml.Tbl
import org.docx4j.wml.TblGrid
import org.docx4j.wml.TblGridCol
import org.docx4j.wml.TblPr
import org.docx4j.wml.TblWidth
import org.docx4j.wml.Tc
import org.docx4j.wml.TcPr
import org.docx4j.wml.Text
import org.docx4j.wml.Tr
import org.docx4j.wml.TrPr
import org.groovydev.SimpleImageBuilder
import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemSortByCode
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.Shipment

import javax.xml.bind.JAXBException
import java.text.DecimalFormat
import java.text.SimpleDateFormat


class DocumentService {

    def grailsApplication
    def userService
    boolean transactional = false


    private getMessageTagLib() {
        return grailsApplication.mainContext.getBean('org.pih.warehouse.MessageTagLib')
    }

    private getFormatTagLib() {
        return grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
    }


    File writeImage(org.pih.warehouse.core.Document document) {
        File file
        try {
            file = new File(document.filename)
            println "Attempt to write to " + file?.absolutePath
            FileOutputStream fos = new FileOutputStream(file)
            fos << document?.fileContents
            fos.close()
        } catch (Exception e) {
            log.error("Error occurred while writing file " + document.filename, e)
        }
        return file
    }


    void scaleImage(org.pih.warehouse.core.Document document, OutputStream outputStream, String width, String height) {

        log.info("Scale image " + document.filename + " width=" + width + " height=" + height + " contentType=" + document.contentType)
        File file
        FileInputStream fileInputStream
        try {
            file = writeImage(document)
            def extension = document.extension ?: document.filename.substring(document.filename.lastIndexOf(".") + 1)
            log.info "Scale image " + document.filename + " (" + width + ", " + height + "), format=" + extension
            fileInputStream = new FileInputStream(file)
            def builder = new SimpleImageBuilder()
            if (builder) {
                def result = builder.image(stream: fileInputStream) {
                    fit(width: width, height: height) {
                        save(stream: outputStream, format: extension?.toLowerCase())
                    }
                }
            } else {
                log.warn("Unable to scale image " + document.filename + " (" + width + ", " + height + "), format=" + extension)
            }

        } catch (Exception e) {
            log.warn("Error scaling image " + document?.filename + ": " + e.message, e)
        } finally {
            if (fileInputStream) fileInputStream?.close()
            if (file) file?.delete()
        }
    }

    /**
     *
     * @param filePath
     * @return
     */
    File findFile(String filePath) {
        def file
        def appContext = ApplicationHolder.application.parentContext
        def archiveDirectory = filePath
        if (ApplicationHolder.application.isWarDeployed()) {
            archiveDirectory = "classpath:$filePath"
            file = appContext.getResource(archiveDirectory)?.getFile()
        } else {
            archiveDirectory = "grails-app${File.separator}conf${File.separator}${filePath}"
            file = new File(archiveDirectory)
        }
        return file
    }

    /**
     *
     * @param shipmentInstance
     * @return
     */
    File generateChecklistAsDocx() {
        // Save document to temporary file
        WordprocessingMLPackage wordMLPackage = generateChecklist()
        File tempFile = File.createTempFile("Checklist", ".docx")
        wordMLPackage.save(tempFile)
        return tempFile
    }

    /**
     * Generate the Checklist from a template.
     *
     * @param
     * @return
     */
    WordprocessingMLPackage generateChecklist() {

        File template = findFile("templates/receive-shipment-template.docx")
        if (!template) {
            throw new FileNotFoundException("Could not find template")
        }

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(template)

        // 2. Fetch the document part
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart()

        Document wmlDocumentEl = (Document) documentPart.getJaxbElement()

        //xml --> string
        def xml = XmlUtils.marshaltoString(wmlDocumentEl, true)
        def mappings = new HashMap<String, String>()

        //valorize template
        Object obj = XmlUtils.unmarshallFromTemplate(xml, mappings)

        //change  JaxbElement
        documentPart.setJaxbElement((Document) obj)

        return wordMLPackage
    }

    /**
     * Generate the 'Certificate of Donation' letter from a template.
     *
     * @param shipmentInstance
     * @return
     */
    WordprocessingMLPackage generateLetter(Shipment shipmentInstance) {

        File template = findFile("templates/cod-pl-template.docx")
        if (!template) {
            throw new FileNotFoundException("templates/cod-pl-template.docx")
        }

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(template)

        // 2. Fetch the document part
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart()

        Document wmlDocumentEl = (Document) documentPart.getJaxbElement()

        //xml --> string
        def xml = XmlUtils.marshaltoString(wmlDocumentEl, true)
        def mappings = new HashMap<String, String>()

        def formatter = new SimpleDateFormat("MMM dd, yyyy")
        def date = formatter.format(shipmentInstance.getExpectedShippingDate())
        mappings.put("date", date)

        String subtitle = ""
        if ("Sea".equals(shipmentInstance?.shipmentType?.name)) {
            ReferenceNumber containerNumber = shipmentInstance.getReferenceNumber("Container Number")
            if (containerNumber) {
                subtitle = "Container #${containerNumber.identifier} "
            }
            ReferenceNumber sealNumber = shipmentInstance.getReferenceNumber("Seal Number")
            if (sealNumber) {
                subtitle += "Seal #${sealNumber.identifier}"
            }
            log.info("sea shipment " + subtitle)
        } else if ("Air".equals(shipmentInstance?.shipmentType?.name)) {
            subtitle = "Freight Forwarder ${shipmentInstance?.shipmentMethod?.shipper?.name}"
            log.info("air shipment " + subtitle)
        }
        mappings.put("subtitle", subtitle)

        def value = ""
        if (shipmentInstance?.statedValue) {
            def decimalFormatter = new DecimalFormat("\$###,###.00")
            value = decimalFormatter.format(shipmentInstance?.statedValue)
        }
        mappings.put("value", value)

        log.debug("mappings: " + mappings)
        log.debug("xml before: " + xml)
        //valorize template
        Object obj = XmlUtils.unmarshallFromTemplate(xml, mappings)
        log.debug("xml after: " + xml)
        log.debug("mappings: " + mappings)

        //change  JaxbElement
        documentPart.setJaxbElement((Document) obj)

        // Create a new table for the Packing List
        Tbl table = createTable(wordMLPackage, shipmentInstance, 3, 1200)

        // Add table to document
        wordMLPackage.getMainDocumentPart().addObject(table)

        return wordMLPackage
    }

    String getElementText(Object jaxbElem) throws Exception {
        StringWriter sw = new StringWriter()
        TextUtils.extractText(jaxbElem, sw)
        return sw.toString()
    }

    /**
     *
     * @param wmlPackage
     * @param shipmentInstance
     * @param cols
     * @param cellWidthTwips
     * @return
     */
    Tbl createTable(WordprocessingMLPackage wmlPackage, Shipment shipmentInstance, int cols, int cellWidthTwips) {

        Tbl tbl = Context.getWmlObjectFactory().createTbl()
        // w:tblPr
        log.info("Namespace: " + Namespaces.W_NAMESPACE_DECLARATION)

        TblPr tblPr = null
        try {
            String strTblPr =
                    "<w:tblPr " + Namespaces.W_NAMESPACE_DECLARATION + "><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblLook w:val=\"04A0\"/></w:tblPr>"
            tblPr = (TblPr) XmlUtils.unmarshalString(strTblPr)
        } catch (JAXBException e) {
            log.error("Exception occurred while creating the table prolog")
        }
        tbl.setTblPr(tblPr)

        // <w:tblGrid><w:gridCol w:w="4788"/>
        TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid()
        tbl.setTblGrid(tblGrid)
        // Add required <w:gridCol w:w="4788"/>
        int writableWidthTwips = wmlPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips()
        cellWidthTwips = writableWidthTwips / 3

        for (int i = 1; i <= cols; i++) {
            TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol()
            gridCol.setW(BigInteger.valueOf(cellWidthTwips))
            tblGrid.getGridCol().add(gridCol)
        }

        // Create a repeating header
        Tr trHeader = Context.getWmlObjectFactory().createTr()
        tbl.getEGContentRowContent().add(trHeader)
        BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue()

        TrPr trPr = Context.getWmlObjectFactory().createTrPr()
        trHeader.setTrPr(trPr)

        trPr.getCnfStyleOrDivIdOrGridBefore().add(Context.getWmlObjectFactory().createCTTrPrBaseTblHeader(bdt))
        addTc(wmlPackage, trHeader, "Pack level 1/2 #", true)
        addTc(wmlPackage, trHeader, "Item", true)
        addTc(wmlPackage, trHeader, "Qty", true)


        def previousContainer = null
        def shipmentItems = shipmentInstance?.shipmentItems?.sort { it?.container?.sortOrder }
        // Iterate over shipment items and add them to the table
        shipmentItems?.each { itemInstance ->

            log.info "previous: " + previousContainer + ", current: " + itemInstance?.container + ", same: " + (itemInstance?.container == previousContainer)
            Tr tr = Context.getWmlObjectFactory().createTr()
            tbl.getEGContentRowContent().add(tr)
            if (itemInstance?.container != previousContainer) {
                addTc(wmlPackage, tr, itemInstance?.container?.name, false)
            } else {
                addTc(wmlPackage, tr, "", false)
            }
            addTc(wmlPackage, tr, itemInstance?.product?.name, false)
            addTc(wmlPackage, tr, String.valueOf(itemInstance?.quantity), false)
            previousContainer = itemInstance?.container

        }
        return tbl
    }

    /**
     *
     * @param wmlPackage
     * @param tr
     * @param text
     * @param applyBold
     */
    void addTc(WordprocessingMLPackage wmlPackage, Tr tr, String text, boolean applyBold) {
        Tc tc = Context.getWmlObjectFactory().createTc()
        tc.getEGBlockLevelElts().add(createParagraphOfText(text, applyBold))
        tr.getEGContentCellContent().add(tc)
    }

    /**
     *
     * @param simpleText
     * @param applyBold
     * @return
     */
    P createParagraphOfText(String simpleText, boolean applyBold) {
        P para = Context.getWmlObjectFactory().createP()
        // Create the text element
        Text t = Context.getWmlObjectFactory().createText()
        t.setValue(simpleText)
        // Create the run
        R run = Context.getWmlObjectFactory().createR()
        run.getRunContent().add(t)
        // Set bold property
        if (applyBold) {
            RPr rpr = Context.getWmlObjectFactory().createRPr()
            BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue()
            rpr.setB(bdt)
            run.setRPr(rpr)
        }
        para.getParagraphContent().add(run)

        return para
    }

    /**
     *
     * @param wordMLPackage
     * @return
     */
    void convertToPdf(WordprocessingMLPackage wordMLPackage, OutputStream outputStream) {
        PdfConversion conversion = new Conversion(wordMLPackage)
        conversion.output(outputStream, null)
    }

    void generateExcel(OutputStream outputStream, List<Map> data) {
        try {
            Workbook workbook = new HSSFWorkbook()
            HSSFSheet sheet = workbook.createSheet("Sheet1")
            createExcelHeader(sheet, 0, data.get(0).keySet().toList())
            data.eachWithIndex { Map dataRow, index ->
                createExcelRow(sheet, index + 1, dataRow)
            }
            workbook.write(outputStream)
            outputStream.close()
        } catch (IOException e) {
            log.error("IO exception while generating excel file")
        }
    }

    void createExcelHeader(HSSFSheet sheet, int rowNumber, List columnNames) {
        Row excelRow = sheet.createRow(rowNumber)
        columnNames.eachWithIndex { columnName, index ->
            excelRow.createCell(index).setCellValue(columnName)
        }
    }

    void createExcelRow(HSSFSheet sheet, int rowNumber, Map dataRow) {
        Row excelRow = sheet.createRow(rowNumber)
        dataRow.keySet().eachWithIndex { columnName, index ->
            def cellValue = dataRow.get(columnName) ?: ""
            // POI can't handle objects so we need to convert all objects to strings unless they are numeric
            if (!(cellValue instanceof Number)) {
                cellValue = cellValue.toString()
            }
            excelRow.createCell(index).setCellValue(cellValue)
        }
    }


    boolean generatePartialPackingList(OutputStream outputStream, Shipment shipmentInstance) {

        Workbook workbook = new HSSFWorkbook()
        CreationHelper createHelper = workbook.getCreationHelper()
        Sheet sheet = workbook.createSheet()

        // Bold font
        Font boldFont = workbook.createFont()
        boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD)

        // Bold cell style
        CellStyle labelStyle = workbook.createCellStyle()
        labelStyle.setFont(boldFont)

        CellStyle tableHeaderCenterStyle = workbook.createCellStyle()
        CellStyle tableHeaderLeftStyle = workbook.createCellStyle()

        // Bold and align center cell style
        CellStyle boldAndCenterStyle = workbook.createCellStyle()
        boldAndCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
        boldAndCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
        //boldAndCenterStyle.setWrapText(true)

        // Align center cell style
        CellStyle tableDataCenterStyle = workbook.createCellStyle()
        tableDataCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
        tableDataCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)

        // Align center cell style
        CellStyle tableDataPalletStyle = workbook.createCellStyle()
        tableDataPalletStyle.setAlignment(CellStyle.ALIGN_LEFT)
        tableDataPalletStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)

        // Align left cell style
        CellStyle tableDataLeftStyle = workbook.createCellStyle()
        tableDataLeftStyle.setAlignment(CellStyle.ALIGN_LEFT)
        tableDataLeftStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)

        // Align left cell style
        CellStyle tableDataDateStyle = workbook.createCellStyle()
        tableDataDateStyle.setAlignment(CellStyle.ALIGN_CENTER)
        tableDataDateStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
        tableDataDateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy"))


        // Wrap text cell style
        CellStyle wrapTextCellStyle = workbook.createCellStyle()
        wrapTextCellStyle.setWrapText(true)

        // Date cell style
        CellStyle dateStyle = workbook.createCellStyle()
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy"))
        dateStyle.setAlignment(CellStyle.ALIGN_LEFT)
        dateStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)

        // Date cell style
        CellStyle timestampStyle = workbook.createCellStyle()
        timestampStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy hh:mm:ss"))
        timestampStyle.setAlignment(CellStyle.ALIGN_RIGHT)
        timestampStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)

        sheet.setColumnWidth((short) 0, (short) ((50 * 3) / ((double) 1 / 20)))
        sheet.setColumnWidth((short) 1, (short) ((50 * 3) / ((double) 1 / 20)))
        sheet.setColumnWidth((short) 2, (short) ((50 * 3) / ((double) 1 / 20)))
        sheet.setColumnWidth((short) 3, (short) ((50 * 10) / ((double) 1 / 20)))
        sheet.setColumnWidth((short) 4, (short) ((50 * 5) / ((double) 1 / 20)))
        sheet.setColumnWidth((short) 5, (short) ((50 * 3) / ((double) 1 / 20)))
        sheet.setColumnWidth((short) 6, (short) ((50 * 3) / ((double) 1 / 20)))
        sheet.setColumnWidth((short) 7, (short) ((50 * 3) / ((double) 1 / 20)))
        sheet.setColumnWidth((short) 8, (short) ((50 * 5) / ((double) 1 / 20)))

        // SHIPMENT NAME
        int counter = 0
        int CELL_INDEX = 0

        // ITEM TABLE HEADER
        Row row = sheet.createRow((short) counter++)
        row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'packLevel1.label', default: 'Pack level 1'))
        row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

        row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'packLevel2.label', default: 'Pack level 2'))
        row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

        row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'product.productCode.label', default: 'SKU'))
        row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

        row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'product.label'))
        row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

        row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'inventory.lotNumber.label'))
        row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

        row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'inventoryItem.expires.label'))
        row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

        row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'default.qty.label'))
        row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

        row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'default.units.label'))
        row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

        row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'shipping.recipient.label'))
        row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

        def previousContainer = "", initialRowIndex = 0, finalRowIndex = 0
        shipmentInstance.shipmentItems.sort().each { itemInstance ->

            CELL_INDEX = 0
            log.debug "Adding item  to packing list " + itemInstance?.product?.name + " -> " + itemInstance?.container?.name
            row = sheet.createRow((short) counter++)
            
            if (itemInstance?.container?.parentContainer) {
                row.createCell(CELL_INDEX).setCellValue(itemInstance?.container?.parentContainer?.name)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataPalletStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.container?.name)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)
            } else if (itemInstance?.container) {
                row.createCell(CELL_INDEX).setCellValue(itemInstance?.container?.name)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataPalletStyle)

                row.createCell(CELL_INDEX).setCellValue("")
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)
            } else {
                row.createCell(CELL_INDEX).setCellValue("")
                row.getCell(CELL_INDEX++).setCellStyle(tableDataPalletStyle)

                row.createCell(CELL_INDEX).setCellValue("")
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)
            }

            row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.productCode)
            row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

            row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.name)
            row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

            row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.lotNumber)
            row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

            row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.expirationDate)
            row.getCell(CELL_INDEX++).setCellStyle(tableDataDateStyle)

            row.createCell(CELL_INDEX).setCellValue(itemInstance?.quantity)
            row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'default.each.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

            row.createCell(CELL_INDEX).setCellValue(itemInstance?.recipient?.email)
            row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

            row.setHeightInPoints(30.0)
            previousContainer = itemInstance?.container?.name
        }

        log.info("workbook " + workbook)
        workbook.write(outputStream)

        return true

    }


    void generatePackingList(OutputStream outputStream, Shipment shipmentInstance) {
        // TODO Move to PoiService

        try {
            Workbook workbook = new HSSFWorkbook()
            CreationHelper createHelper = workbook.getCreationHelper()
            Sheet sheet = workbook.createSheet()

            int counter = 0

            if(shipmentInstance.isFromPurchaseOrder) {
                sheet.setColumnWidth((short) counter++, (short) ((50 * 5) / ((double) 1 / 20)))
            }
            sheet.setColumnWidth((short) counter++, (short) ((50 * 5) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 5) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 3) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 12) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 5) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 4) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 3) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 3) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 5) / ((double) 1 / 20)))

            // Bold font
            Font boldFont = workbook.createFont()
            boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD)

            // Bold cell style
            CellStyle labelStyle = workbook.createCellStyle()
            labelStyle.setFont(boldFont)
            labelStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP)

            CellStyle tableHeaderCenterStyle = workbook.createCellStyle()
            tableHeaderCenterStyle.setBorderBottom((short) 1)
            tableHeaderCenterStyle.setBorderLeft((short) 1)
            tableHeaderCenterStyle.setBorderRight((short) 1)
            tableHeaderCenterStyle.setBorderTop((short) 1)
            tableHeaderCenterStyle.setFont(boldFont)
            tableHeaderCenterStyle.setWrapText(true)

            CellStyle tableHeaderLeftStyle = workbook.createCellStyle()
            tableHeaderLeftStyle.setBorderBottom((short) 1)
            tableHeaderLeftStyle.setBorderLeft((short) 1)
            tableHeaderLeftStyle.setBorderRight((short) 1)
            tableHeaderLeftStyle.setBorderTop((short) 1)
            tableHeaderLeftStyle.setFont(boldFont)
            tableHeaderLeftStyle.setWrapText(true)

            // Bold and align center cell style
            CellStyle boldAndCenterStyle = workbook.createCellStyle()
            boldAndCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            boldAndCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            boldAndCenterStyle.setBorderBottom((short) 1)
            boldAndCenterStyle.setBorderLeft((short) 1)
            boldAndCenterStyle.setBorderRight((short) 1)
            boldAndCenterStyle.setBorderTop((short) 1)
            boldAndCenterStyle.setFont(boldFont)
            boldAndCenterStyle.setWrapText(true)

            // Align center cell style
            CellStyle tableDataCenterStyle = workbook.createCellStyle()
            tableDataCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            tableDataCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataCenterStyle.setBorderBottom((short) 1)
            tableDataCenterStyle.setBorderLeft((short) 1)
            tableDataCenterStyle.setBorderRight((short) 1)
            tableDataCenterStyle.setBorderTop((short) 1)

            // Align center cell style
            CellStyle tableDataPalletStyle = workbook.createCellStyle()
            tableDataPalletStyle.setAlignment(CellStyle.ALIGN_LEFT)
            tableDataPalletStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataPalletStyle.setBorderBottom((short) 1)
            tableDataPalletStyle.setBorderLeft((short) 1)
            tableDataPalletStyle.setBorderRight((short) 1)
            tableDataPalletStyle.setBorderTop((short) 1)

            // Align left cell style
            CellStyle tableDataLeftStyle = workbook.createCellStyle()
            tableDataLeftStyle.setAlignment(CellStyle.ALIGN_LEFT)
            tableDataLeftStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataLeftStyle.setBorderBottom((short) 1)
            tableDataLeftStyle.setBorderLeft((short) 1)
            tableDataLeftStyle.setBorderRight((short) 1)
            tableDataLeftStyle.setBorderTop((short) 1)
            tableDataLeftStyle.setWrapText(true)

            // Align left cell style
            CellStyle tableDataDateStyle = workbook.createCellStyle()
            tableDataDateStyle.setAlignment(CellStyle.ALIGN_CENTER)
            tableDataDateStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataDateStyle.setBorderBottom((short) 1)
            tableDataDateStyle.setBorderLeft((short) 1)
            tableDataDateStyle.setBorderRight((short) 1)
            tableDataDateStyle.setBorderTop((short) 1)
            tableDataDateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy"))


            // Wrap text cell style
            CellStyle wrapTextCellStyle = workbook.createCellStyle()
            wrapTextCellStyle.setWrapText(true)

            CellStyle topAlignedTextCellStyle = workbook.createCellStyle()
            topAlignedTextCellStyle.setWrapText(true)
            topAlignedTextCellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP)

            // Date cell style
            CellStyle dateStyle = workbook.createCellStyle()
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy"))
            dateStyle.setAlignment(CellStyle.ALIGN_LEFT)
            dateStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)

            // Date cell style
            CellStyle timestampStyle = workbook.createCellStyle()
            timestampStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy hh:mm:ss"))
            timestampStyle.setAlignment(CellStyle.ALIGN_RIGHT)
            timestampStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)

            // SHIPMENT NAME
            counter = 0
            Row row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'shipping.name.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue(shipmentInstance?.name)

            // SHIPMENT NUMBER
            row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'shipping.shipmentNumber.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue(shipmentInstance?.shipmentNumber)

            // SHIPMENT TYPE
            row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'shipping.shipmentType.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue("" + getFormatTagLib().metadata(obj: shipmentInstance?.shipmentType))

            // REFERENCE NUMBERS
            shipmentInstance.referenceNumbers.each {
                row = sheet.createRow((short) counter++)
                row.createCell(0).setCellValue("" + getFormatTagLib().metadata(obj: it?.referenceNumberType))
                row.getCell(0).setCellStyle(labelStyle)
                row.createCell(1).setCellValue(it?.identifier)
            }

            // EMPTY ROW
            row = sheet.createRow((short) counter++)

            // FROM
            row = sheet.createRow((short) counter++)
            row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue("" + getMessageTagLib().message(code: 'shipping.origin.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue(shipmentInstance?.origin?.name)
            row = sheet.createRow((short) counter++)

            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'shipping.destination.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue(shipmentInstance?.destination?.name)

            // EMPTY ROW
            row = sheet.createRow((short) counter++)

            // EXPECTED SHIPMENT DATE
            row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'shipping.expectedShippingDate.label'))
            row.getCell(0).setCellStyle(labelStyle)
            Cell expectedShipmentDateCell = row.createCell(1)
            expectedShipmentDateCell.setCellValue(shipmentInstance?.expectedShippingDate)
            expectedShipmentDateCell.setCellStyle(dateStyle)

            // ACTUAL SHIPMENT DATE
            row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'shipping.actualShippingDate.label'))
            row.getCell(0).setCellStyle(labelStyle)
            Cell actualShipmentDateCell = row.createCell(1)
            if (shipmentInstance?.actualShippingDate) {
                actualShipmentDateCell.setCellValue(shipmentInstance?.actualShippingDate)
                actualShipmentDateCell.setCellStyle(dateStyle)
            } else {
                actualShipmentDateCell.setCellValue("" + getMessageTagLib().message(code: 'default.notAvailable.label'))
            }

            // ACTUAL ARRIVAL DATE
            row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'shipping.actualDeliveryDate.label'))
            row.getCell(0).setCellStyle(labelStyle)
            Cell actualArrivalDateCell = row.createCell(1)
            if (shipmentInstance?.actualDeliveryDate) {
                actualArrivalDateCell.setCellValue(shipmentInstance?.actualDeliveryDate)
                actualArrivalDateCell.setCellStyle(dateStyle)
            } else {
                actualArrivalDateCell.setCellValue("" + getMessageTagLib().message(code: 'default.notAvailable.label'))
            }

            // COMMENTS
            row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'default.comments.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue(shipmentInstance?.additionalInformation)
            row.getCell(1).setCellStyle(topAlignedTextCellStyle)
            row.setHeightInPoints(30.0)

            // TWO EMPTY ROWS
            row = sheet.createRow((short) counter++)
            row = sheet.createRow((short) counter++)

            // Merge cells
            //first row (0-based)
            //last row (0-based)
            //first column (0-based)
            //last column (0-based)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4)) // Name
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 4)) // Shipment number
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 4)) // Shipment type
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 4)) // Empty
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 4)) // Origin
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 1, 4)) // Destination
            sheet.addMergedRegion(new CellRangeAddress(6, 6, 1, 4)) // Empty
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 1, 4)) // Expected shipping date
            sheet.addMergedRegion(new CellRangeAddress(8, 8, 1, 4)) //  Actual shipping date
            sheet.addMergedRegion(new CellRangeAddress(9, 9, 1, 4)) // Actual delivery date
            if (!shipmentInstance.referenceNumbers) {
                sheet.addMergedRegion(new CellRangeAddress(10, 10, 1, 4)) // Comments
            } else {
                sheet.addMergedRegion(new CellRangeAddress(10, 10, 1, 4)) // Empty
                sheet.addMergedRegion(new CellRangeAddress(11, 11, 1, 4)) // Comments
            }


            int CELL_INDEX = 0

            // ITEM TABLE HEADER
            row = sheet.createRow((short) counter++)
            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'packLevel1.label', default: 'Pack level 1'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'packLevel2.label', default: 'Pack level 2'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            if(shipmentInstance.isFromPurchaseOrder) {
                row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'order.orderNumber.label', default: 'Order Number'))
                row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)
            }

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'packingList.productCode.label', default: 'Code'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'product.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'inventory.lotNumber.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'inventoryItem.expires.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'default.qty.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'default.units.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'shipping.recipient.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            def previousContainer = "", previousParentContainer = "", packLevelOneInitialRowIndex = 0, packLevelOneFinalRowIndex = 0, packLevelTwoInitialRowIndex = 0, packLevelTwoFinalRowIndex = 0
            shipmentInstance.shipmentItems.sort { a, b ->
                a.container?.parentContainer && !b.container?.parentContainer ?
                    a.container?.parentContainer <=> b.container :
                        a.container?.parentContainer <=> b.container?.parentContainer }.each { itemInstance ->

                CELL_INDEX = 0
                log.debug "Adding item  to packing list " + itemInstance?.product?.name + " -> " + itemInstance?.container?.name
                row = sheet.createRow((short) counter++)

                if ((itemInstance?.container?.parentContainer && previousParentContainer != itemInstance?.container?.parentContainer?.name)
                        || (!itemInstance?.container?.parentContainer && previousParentContainer != itemInstance?.container?.name)) {
                    row.createCell(CELL_INDEX).setCellValue(itemInstance?.container?.parentContainer?.name ?: itemInstance?.container?.name ?: getMessageTagLib().message(code: 'shipping.unpacked.label').toString())
                    row.getCell(CELL_INDEX++).setCellStyle(tableDataPalletStyle)
                    // If we're at a place in the XLS file where we want to merge cells (e.g. the packing list)
                    // Then we merge rows when the container name is different from the previous container name
                    sheet.addMergedRegion(CellRangeAddress.valueOf("A${packLevelOneInitialRowIndex + 1}:A${packLevelOneFinalRowIndex + 1}"))

                    packLevelOneInitialRowIndex = row.getRowNum()
                    packLevelOneFinalRowIndex = row.getRowNum()
                } else {
                    packLevelOneFinalRowIndex = row.getRowNum()
                    // Merge columns if pack level one is same as previous one but it's the last element in array
                    if (itemInstance == shipmentInstance.shipmentItems.last()) {
                        sheet.addMergedRegion(CellRangeAddress.valueOf("A${packLevelOneInitialRowIndex + 1}:A${packLevelOneFinalRowIndex + 1}"))
                    }
                    row.createCell(CELL_INDEX).setCellValue("")
                    row.getCell(CELL_INDEX++).setCellStyle(tableDataPalletStyle)
                }

                if (previousContainer != itemInstance?.container?.name) {
                    row.createCell(CELL_INDEX).setCellValue(itemInstance?.container?.parentContainer ? itemInstance?.container?.name : getMessageTagLib().message(code: 'shipping.unpacked.label').toString())
                    row.getCell(CELL_INDEX++).setCellStyle(tableDataPalletStyle)
                    // If we're at a place in the XLS file where we want to merge cells (e.g. the packing list)
                    // Then we merge rows when the container name is different from the previous container name
                    sheet.addMergedRegion(CellRangeAddress.valueOf("B${packLevelTwoInitialRowIndex + 1}:B${packLevelTwoFinalRowIndex + 1}"))

                    packLevelTwoInitialRowIndex = row.getRowNum()
                    packLevelTwoFinalRowIndex = row.getRowNum()
                } else {
                    packLevelTwoFinalRowIndex = row.getRowNum()
                    // Merge columns if pack level two is same as previous one but it's the last element in array
                    if (itemInstance == shipmentInstance.shipmentItems.last()) {
                        sheet.addMergedRegion(CellRangeAddress.valueOf("B${packLevelTwoInitialRowIndex + 1}:B${packLevelTwoFinalRowIndex + 1}"))
                    }
                    row.createCell(CELL_INDEX).setCellValue("")
                    row.getCell(CELL_INDEX++).setCellStyle(tableDataPalletStyle)
                }

                if(shipmentInstance.isFromPurchaseOrder) {
                    row.createCell(CELL_INDEX).setCellValue(itemInstance?.orderNumber)
                    row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)
                }

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.productCode)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.name)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.lotNumber)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.expirationDate)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataDateStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.quantity)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'default.each.label'))
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.recipient?.name)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.setHeightInPoints(30.0)
                previousContainer = itemInstance?.container?.name
                previousParentContainer = itemInstance?.container?.parentContainer?.name ?: itemInstance?.container?.name
            }

            log.info("workbook " + workbook)
            workbook.write(outputStream)
        }
        catch (Exception e) {
            log.error e
            throw e
        }
    }

    void generateCertificateOfDonation(OutputStream outputStream, Shipment shipmentInstance) {

        try {

            Boolean hasRoleFinance = userService.hasRoleFinance()

            Workbook workbook = new HSSFWorkbook()
            CreationHelper createHelper = workbook.getCreationHelper()
            Sheet sheet = workbook.createSheet()
            sheet.setColumnWidth((short) 0, (short) ((50 * 5) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 1, (short) ((50 * 3) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 2, (short) ((50 * 7) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 3, (short) ((50 * 3) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 4, (short) ((50 * 3) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 5, (short) ((50 * 7) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 6, (short) ((50 * 4) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 7, (short) ((50 * 4) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 8, (short) ((50 * 4) / ((double) 1 / 20)))

            // Bold font
            Font boldFont = workbook.createFont()
            boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD)

            // Bold cell style
            CellStyle labelStyle = workbook.createCellStyle()
            labelStyle.setFont(boldFont)

            // Label center style
            CellStyle labelCenterStyle = workbook.createCellStyle()
            labelCenterStyle.setFont(boldFont)
            labelCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            labelCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)

            CellStyle tableHeaderCenterStyle = workbook.createCellStyle()
            tableHeaderCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            tableHeaderCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableHeaderCenterStyle.setBorderBottom((short) 1)
            tableHeaderCenterStyle.setBorderLeft((short) 1)
            tableHeaderCenterStyle.setBorderRight((short) 1)
            tableHeaderCenterStyle.setBorderTop((short) 1)
            tableHeaderCenterStyle.setFont(boldFont)
            tableHeaderCenterStyle.setWrapText(true)

            CellStyle tableHeaderLeftStyle = workbook.createCellStyle()
            tableHeaderLeftStyle.setBorderBottom((short) 1)
            tableHeaderLeftStyle.setBorderLeft((short) 1)
            tableHeaderLeftStyle.setBorderRight((short) 1)
            tableHeaderLeftStyle.setBorderTop((short) 1)
            tableHeaderLeftStyle.setFont(boldFont)
            tableHeaderLeftStyle.setWrapText(true)

            // Bold and align center cell style
            CellStyle boldAndCenterStyle = workbook.createCellStyle()
            boldAndCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            boldAndCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            boldAndCenterStyle.setBorderBottom((short) 1)
            boldAndCenterStyle.setBorderLeft((short) 1)
            boldAndCenterStyle.setBorderRight((short) 1)
            boldAndCenterStyle.setBorderTop((short) 1)
            boldAndCenterStyle.setFont(boldFont)
            boldAndCenterStyle.setWrapText(true)

            // Align center cell style
            CellStyle tableDataCenterStyle = workbook.createCellStyle()
            tableDataCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            tableDataCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataCenterStyle.setBorderBottom((short) 1)
            tableDataCenterStyle.setBorderLeft((short) 1)
            tableDataCenterStyle.setBorderRight((short) 1)
            tableDataCenterStyle.setBorderTop((short) 1)

            // Align left cell style
            CellStyle tableDataLeftStyle = workbook.createCellStyle()
            tableDataLeftStyle.setAlignment(CellStyle.ALIGN_LEFT)
            tableDataLeftStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataLeftStyle.setBorderBottom((short) 1)
            tableDataLeftStyle.setBorderLeft((short) 1)
            tableDataLeftStyle.setBorderRight((short) 1)
            tableDataLeftStyle.setBorderTop((short) 1)
            tableDataLeftStyle.setWrapText(true)

            // Align left cell style
            CellStyle tableDataDateStyle = workbook.createCellStyle()
            tableDataDateStyle.setAlignment(CellStyle.ALIGN_CENTER)
            tableDataDateStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataDateStyle.setBorderBottom((short) 1)
            tableDataDateStyle.setBorderLeft((short) 1)
            tableDataDateStyle.setBorderRight((short) 1)
            tableDataDateStyle.setBorderTop((short) 1)
            tableDataDateStyle.setDataFormat(createHelper.createDataFormat().getFormat("MMMM dd, yyyy"))

            // COMMERCIAL INVOICE
            int counter = 0
            Row row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("Commercial Invoice")
            row.getCell(0).setCellStyle(labelCenterStyle)

            // EMPTY ROW
            row = sheet.createRow((short) counter++)

            // SHIPMENT NUMBER
            row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'shipping.shipmentNumber.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue(shipmentInstance?.shipmentNumber)

            // ORIGIN
            row = sheet.createRow((short) counter++)
            row.createCell(0, Cell.CELL_TYPE_STRING).setCellValue("" + getMessageTagLib().message(code: 'shipping.origin.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue(shipmentInstance?.origin?.name)

            // DESTINATION
            row = sheet.createRow((short) counter++)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'shipping.destination.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue(shipmentInstance?.destination?.name)

            // EMPTY ROW
            row = sheet.createRow((short) counter++)

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8)) // Commercial Invoice
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8)) // Empty row
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 4)) // Shipment number
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 4)) // Origin
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 4)) // Destination

            int CELL_INDEX = 0

            // ITEM TABLE HEADER
            row = sheet.createRow((short) counter++)
            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'certificateOfDonation.number.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'certificateOfDonation.code.label', default: 'Code'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'certificateOfDonation.itemDescription.label', default: 'Item Description'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'default.uom.label', default: 'UoM'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'certificateOfDonation.batchNumber.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'certificateOfDonation.expDate.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'certificateOfDonation.quantity.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'certificateOfDonation.unitPrice.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'certificateOfDonation.totalCost.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            def totalPrice = 0
            def previousContainer = "", initialRowIndex = 0, finalRowIndex = 0
            shipmentInstance.shipmentItems.sort().each { itemInstance ->

                CELL_INDEX = 0
                row = sheet.createRow((short) counter++)
                def totalCost = 0
                if (itemInstance?.product?.pricePerUnit && hasRoleFinance) {
                    totalCost = itemInstance?.quantity * itemInstance?.product?.pricePerUnit
                }

                row.createCell(CELL_INDEX).setCellValue(row.getRowNum() - 6)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                if (row.getRowNum() > 16) {
                    sheet.addMergedRegion(CellRangeAddress.valueOf("A${initialRowIndex + 1}:A${finalRowIndex + 1}"))
                }
                initialRowIndex = row.getRowNum()
                finalRowIndex = row.getRowNum()

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.productCode)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.name)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.unitOfMeasure)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.lotNumber)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.expirationDate)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataDateStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.quantity)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                def pricePerUnit = hasRoleFinance ? itemInstance?.product?.pricePerUnit : 0.0
                row.createCell(CELL_INDEX).setCellValue(pricePerUnit ?: 0)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.createCell(CELL_INDEX).setCellValue(totalCost)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.setHeightInPoints(30.0)
                previousContainer = itemInstance?.container?.name
                totalPrice += totalCost
            }

            // TOTAL PRICE
            row = sheet.createRow((short) counter++)
            row.createCell(7).setCellValue("Total")
            row.getCell(7).setCellStyle(tableHeaderCenterStyle)
            row.createCell(8).setCellValue(totalPrice)
            row.getCell(8).setCellStyle(boldAndCenterStyle)

            // THREE EMPTY ROWS
            row = sheet.createRow((short) counter++)
            row = sheet.createRow((short) counter++)
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(30.0)

            // PREPARED ON
            row.createCell(1).setCellValue("Prepared on")
            row.getCell(1).setCellStyle(tableDataLeftStyle)
            row.createCell(2).setCellValue("")
            row.getCell(2).setCellStyle(tableDataLeftStyle)

            // APPROVED ON
            row.createCell(4).setCellValue("Approved on")
            row.getCell(4).setCellStyle(tableDataLeftStyle)
            row.createCell(5).setCellValue("")
            row.getCell(5).setCellStyle(tableDataLeftStyle)

            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(30.0)

            // BY
            row.createCell(1).setCellValue("By")
            row.getCell(1).setCellStyle(tableDataLeftStyle)
            row.createCell(2).setCellValue("")
            row.getCell(2).setCellStyle(tableDataLeftStyle)

            row.createCell(4).setCellValue("By")
            row.getCell(4).setCellStyle(tableDataLeftStyle)
            row.createCell(5).setCellValue("")
            row.getCell(5).setCellStyle(tableDataLeftStyle)

            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(30.0)

            // SIGNATURE
            row.createCell(1).setCellValue("Signature")
            row.getCell(1).setCellStyle(tableDataLeftStyle)
            row.createCell(2).setCellValue("")
            row.getCell(2).setCellStyle(tableDataLeftStyle)

            row.createCell(4).setCellValue("Signature")
            row.getCell(4).setCellStyle(tableDataLeftStyle)
            row.createCell(5).setCellValue("")
            row.getCell(5).setCellStyle(tableDataLeftStyle)

            // THREE EMPTY ROWS
            row = sheet.createRow((short) counter++)
            row = sheet.createRow((short) counter++)
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(30.0)

            // PREPARED ON
            row.createCell(1).setCellValue("Checked on")
            row.getCell(1).setCellStyle(tableDataLeftStyle)
            row.createCell(2).setCellValue("")
            row.getCell(2).setCellStyle(tableDataLeftStyle)

            // RECEIVED ON
            row.createCell(4).setCellValue("Received on")
            row.getCell(4).setCellStyle(tableDataLeftStyle)
            row.createCell(5).setCellValue("")
            row.getCell(5).setCellStyle(tableDataLeftStyle)

            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(30.0)

            // BY
            row.createCell(1).setCellValue("By")
            row.getCell(1).setCellStyle(tableDataLeftStyle)
            row.createCell(2).setCellValue("")
            row.getCell(2).setCellStyle(tableDataLeftStyle)

            row.createCell(4).setCellValue("By")
            row.getCell(4).setCellStyle(tableDataLeftStyle)
            row.createCell(5).setCellValue("")
            row.getCell(5).setCellStyle(tableDataLeftStyle)

            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(30.0)

            // SIGNATURE
            row.createCell(1).setCellValue("Signature")
            row.getCell(1).setCellStyle(tableDataLeftStyle)
            row.createCell(2).setCellValue("")
            row.getCell(2).setCellStyle(tableDataLeftStyle)

            row.createCell(4).setCellValue("Signature")
            row.getCell(4).setCellStyle(tableDataLeftStyle)
            row.createCell(5).setCellValue("")
            row.getCell(5).setCellStyle(tableDataLeftStyle)

            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(30.0)

            // POSITION
            row.createCell(1).setCellValue("Position")
            row.getCell(1).setCellStyle(tableDataLeftStyle)
            row.createCell(2).setCellValue("")
            row.getCell(2).setCellStyle(tableDataLeftStyle)

            row.createCell(4).setCellValue("Position")
            row.getCell(4).setCellStyle(tableDataLeftStyle)
            row.createCell(5).setCellValue("")
            row.getCell(5).setCellStyle(tableDataLeftStyle)

            log.info("workbook " + workbook)
            workbook.write(outputStream)
        }
        catch (Exception e) {
            log.error e
            throw e
        }
    }

    void generateStocklistCsv(OutputStream outputStream, Stocklist stocklistInstance) {

        try {
            Workbook workbook = new HSSFWorkbook()
            CreationHelper createHelper = workbook.getCreationHelper()
            Sheet sheet = workbook.createSheet()
            sheet.setColumnWidth((short) 0, (short) ((50 * 4) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 1, (short) ((50 * 6) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 2, (short) ((50 * 2) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 3, (short) ((50 * 4) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 4, (short) ((50 * 6) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 5, (short) ((50 * 6) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 6, (short) ((50 * 6) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) 7, (short) ((50 * 6) / ((double) 1 / 20)))

            // Bold font
            Font boldFont = workbook.createFont()
            boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD)

            // Bold cell style
            CellStyle labelStyle = workbook.createCellStyle()
            labelStyle.setFont(boldFont)

            // Label center style
            CellStyle labelCenterStyle = workbook.createCellStyle()
            labelCenterStyle.setFont(boldFont)
            labelCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            labelCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)

            CellStyle tableHeaderCenterStyle = workbook.createCellStyle()
            tableHeaderCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            tableHeaderCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableHeaderCenterStyle.setBorderBottom((short) 1)
            tableHeaderCenterStyle.setBorderLeft((short) 1)
            tableHeaderCenterStyle.setBorderRight((short) 1)
            tableHeaderCenterStyle.setBorderTop((short) 1)
            tableHeaderCenterStyle.setFont(boldFont)
            tableHeaderCenterStyle.setWrapText(true)

            CellStyle tableHeaderLeftStyle = workbook.createCellStyle()
            tableHeaderLeftStyle.setBorderBottom((short) 1)
            tableHeaderLeftStyle.setBorderLeft((short) 1)
            tableHeaderLeftStyle.setBorderRight((short) 1)
            tableHeaderLeftStyle.setBorderTop((short) 1)
            tableHeaderLeftStyle.setFont(boldFont)
            tableHeaderLeftStyle.setWrapText(true)

            // Bold and align center cell style
            CellStyle boldAndCenterStyle = workbook.createCellStyle()
            boldAndCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            boldAndCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            boldAndCenterStyle.setBorderBottom((short) 1)
            boldAndCenterStyle.setBorderLeft((short) 1)
            boldAndCenterStyle.setBorderRight((short) 1)
            boldAndCenterStyle.setBorderTop((short) 1)
            boldAndCenterStyle.setFont(boldFont)
            boldAndCenterStyle.setWrapText(true)

            // Align center cell style
            CellStyle tableDataCenterStyle = workbook.createCellStyle()
            tableDataCenterStyle.setAlignment(CellStyle.ALIGN_CENTER)
            tableDataCenterStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataCenterStyle.setBorderBottom((short) 1)
            tableDataCenterStyle.setBorderLeft((short) 1)
            tableDataCenterStyle.setBorderRight((short) 1)
            tableDataCenterStyle.setBorderTop((short) 1)

            // Align left cell style
            CellStyle tableDataLeftStyle = workbook.createCellStyle()
            tableDataLeftStyle.setAlignment(CellStyle.ALIGN_LEFT)
            tableDataLeftStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataLeftStyle.setBorderBottom((short) 1)
            tableDataLeftStyle.setBorderLeft((short) 1)
            tableDataLeftStyle.setBorderRight((short) 1)
            tableDataLeftStyle.setBorderTop((short) 1)
            tableDataLeftStyle.setWrapText(true)

            // Align left cell style
            CellStyle tableDataDateStyle = workbook.createCellStyle()
            tableDataDateStyle.setAlignment(CellStyle.ALIGN_CENTER)
            tableDataDateStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            tableDataDateStyle.setBorderBottom((short) 1)
            tableDataDateStyle.setBorderLeft((short) 1)
            tableDataDateStyle.setBorderRight((short) 1)
            tableDataDateStyle.setBorderTop((short) 1)
            tableDataDateStyle.setDataFormat(createHelper.createDataFormat().getFormat("MMMM dd, yyyy"))

            // SHIPMENT NUMBER
            int counter = 0
            Row row = sheet.createRow((short) counter++)
            row.setHeightInPoints(20.0)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'report.stockRequisition.label'))
            row.getCell(0).setCellStyle(labelStyle)
            row.createCell(1).setCellValue(stocklistInstance?.requisition?.name)

            // For warehouse use
            row.createCell(6).setCellValue("" + getMessageTagLib().message(code: 'report.forWarehouseUse.label'))
            row.getCell(6).setCellStyle(tableDataCenterStyle)
            row.createCell(7).setCellValue("")
            row.getCell(7).setCellStyle(tableDataLeftStyle)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 7)) // Commercial Invoice

            // Destination
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(20.0)
            row.createCell(0).setCellValue(stocklistInstance?.destination?.name)
            row.getCell(0).setCellStyle(labelStyle)

            // Approved by
            row.createCell(6).setCellValue("" + getMessageTagLib().message(code: 'deliveryNote.approvedBy.label'))
            row.getCell(6).setCellStyle(tableDataLeftStyle)
            row.createCell(7).setCellValue("")
            row.getCell(7).setCellStyle(tableDataLeftStyle)

            // Signature
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(20.0)
            row.createCell(6).setCellValue("" + getMessageTagLib().message(code: 'default.signature.label'))
            row.getCell(6).setCellStyle(tableDataLeftStyle)
            row.createCell(7).setCellValue("")
            row.getCell(7).setCellStyle(tableDataLeftStyle)

            // Date
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(20.0)
            row.createCell(6).setCellValue("" + getMessageTagLib().message(code: 'default.date.label'))
            row.getCell(6).setCellStyle(tableDataLeftStyle)
            row.createCell(7).setCellValue("")
            row.getCell(7).setCellStyle(tableDataLeftStyle)

            // Processed by
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(20.0)
            row.createCell(6).setCellValue("" + getMessageTagLib().message(code: 'requisition.processedBy.label'))
            row.getCell(6).setCellStyle(tableDataLeftStyle)
            row.createCell(7).setCellValue("")
            row.getCell(7).setCellStyle(tableDataLeftStyle)

            // Date
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(20.0)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'default.date.label'))
            row.getCell(0).setCellStyle(tableDataLeftStyle)
            row.createCell(1).setCellValue("")
            row.getCell(1).setCellStyle(tableDataLeftStyle)

            row.createCell(3).setCellValue("" + getMessageTagLib().message(code: 'default.date.label'))
            row.getCell(3).setCellStyle(tableDataLeftStyle)
            row.createCell(4).setCellValue("")
            row.getCell(4).setCellStyle(tableDataLeftStyle)


            // Signature
            row.createCell(6).setCellValue("" + getMessageTagLib().message(code: 'default.signature.label'))
            row.getCell(6).setCellStyle(tableDataLeftStyle)
            row.createCell(7).setCellValue("")
            row.getCell(7).setCellStyle(tableDataLeftStyle)

            // Requested by
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(20.0)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'requisition.requestedBy.label'))
            row.getCell(0).setCellStyle(tableDataLeftStyle)
            row.createCell(1).setCellValue("")
            row.getCell(1).setCellStyle(tableDataLeftStyle)

            // Approved by
            row.createCell(3).setCellValue("" + getMessageTagLib().message(code: 'deliveryNote.approvedBy.label'))
            row.getCell(3).setCellStyle(tableDataLeftStyle)
            row.createCell(4).setCellValue("")
            row.getCell(4).setCellStyle(tableDataLeftStyle)

            // Date
            row.createCell(6).setCellValue("" + getMessageTagLib().message(code: 'default.date.label'))
            row.getCell(6).setCellStyle(tableDataLeftStyle)
            row.createCell(7).setCellValue("")
            row.getCell(7).setCellStyle(tableDataLeftStyle)

            // Signature
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(20.0)
            row.createCell(0).setCellValue("" + getMessageTagLib().message(code: 'default.signature.label'))
            row.getCell(0).setCellStyle(tableDataLeftStyle)
            row.createCell(1).setCellValue("")
            row.getCell(1).setCellStyle(tableDataLeftStyle)

            row.createCell(3).setCellValue("" + getMessageTagLib().message(code: 'default.signature.label'))
            row.getCell(3).setCellStyle(tableDataLeftStyle)
            row.createCell(4).setCellValue("")
            row.getCell(4).setCellStyle(tableDataLeftStyle)

            // Requisition number
            row.createCell(6).setCellValue("" + getMessageTagLib().message(code: 'requisition.requisitionNumber.label'))
            row.getCell(6).setCellStyle(tableDataLeftStyle)
            row.createCell(7).setCellValue("")
            row.getCell(7).setCellStyle(tableDataLeftStyle)

            row = sheet.createRow((short) counter++)
            int CELL_INDEX = 0

            // ITEM TABLE HEADER
            row = sheet.createRow((short) counter++)
            row.setHeightInPoints(25.0)
            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'report.pihCode.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'report.productDescription.label', default: 'Product description'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'import.unit.label', default: 'Unit'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'import.maxQuantity.label', default: 'Max quantity'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'requisition.quantityOnHand.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'report.quantityRequested.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'report.quantityApproved.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'comments.label'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderCenterStyle)

            RequisitionItemSortByCode sortByCode = stocklistInstance.requisition.sortByCode ?: RequisitionItemSortByCode.SORT_INDEX
            stocklistInstance.requisition."${sortByCode.methodName}"?.each { RequisitionItem requisitionItem ->

                CELL_INDEX = 0
                row = sheet.createRow((short) counter++)

                row.createCell(CELL_INDEX).setCellValue(requisitionItem?.product?.productCode)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(requisitionItem?.product?.name)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(requisitionItem?.productPackage ? requisitionItem.productPackage.uom?.code + "/" + requisitionItem.productPackage.quantity + " -- " + requisitionItem.productPackage.uom?.name : 'EA/1')
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(requisitionItem?.quantity)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue("")
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.createCell(CELL_INDEX).setCellValue("")
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.createCell(CELL_INDEX).setCellValue("")
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.createCell(CELL_INDEX).setCellValue("")
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.setHeightInPoints(30.0)
            }

            sheet.autoSizeColumn(1)
            sheet.autoSizeColumn(2)

            log.info("workbook " + workbook)
            workbook.write(outputStream)
        }
        catch (Exception e) {
            log.error e
            throw e
        }
    }


}
