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

import grails.core.GrailsApplication
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.docx4j.TextUtils
import org.docx4j.XmlUtils
import org.docx4j.convert.out.pdf.PdfConversion
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion
import org.docx4j.jaxb.Context
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import org.docx4j.openpackaging.parts.relationships.Namespaces
import org.docx4j.wml.BooleanDefaultTrue
import org.docx4j.wml.Document
import org.docx4j.wml.P
import org.docx4j.wml.R
import org.docx4j.wml.RPr
import org.docx4j.wml.Tbl
import org.docx4j.wml.TblGrid
import org.docx4j.wml.TblGridCol
import org.docx4j.wml.TblPr
import org.docx4j.wml.Tc
import org.docx4j.wml.Text
import org.docx4j.wml.Tr
import org.docx4j.wml.TrPr
//import org.groovydev.SimpleImageBuilder
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemSortByCode
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.Shipment

import javax.imageio.ImageIO
import javax.xml.bind.JAXBException
import java.awt.Image
import java.awt.image.BufferedImage
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class DocumentService {

    GrailsApplication grailsApplication
    def userService

    private getMessageTagLib() {
        return grailsApplication.mainContext.getBean('org.pih.warehouse.MessageTagLib')
    }

    private getFormatTagLib() {
        return grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
    }

    Map getContainerBarcodeLabel(Container container) {
        return getBarcodeLabel(Constants.DEFAULT_CONTAINER_LABEL_DOCUMENT_NUMBER,
                "/api/containers/%s/labels/%s", container?.id)
    }

    Map getBarcodeLabel(String documentNumber, String urlTemplate, String objectId) {
        def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        org.pih.warehouse.core.Document document = getDocument(DocumentCode.ZEBRA_TEMPLATE, documentNumber)
        String url = String.format(urlTemplate, objectId, document?.id)
        return document ? [id: document.id, name: document.name, url: g.createLink(uri: url, absolute: true)] : null
    }

    org.pih.warehouse.core.Document getDocument(DocumentCode documentCode, String documentNumber) {
        return org.pih.warehouse.core.Document.createCriteria().get {
            documentType {
                eq("documentCode", documentCode)
            }
            eq("documentNumber", documentNumber)
        } as org.pih.warehouse.core.Document
    }

    /**
     * Render `document` to `outputStream` so it fits in a box of `width` x `height`.
     *
     * If the supplied image already fits within the box, don't change it.
     *
     * For best results on a hiDPI display, set `width` and `height` to 2x or 3x
     * the desired size, then use the nailthumb jquery plugin to place the image.
     */
    void scaleImage(org.pih.warehouse.core.Document document, OutputStream outputStream, int width, int height) {
        log.info "Scale image ${document.filename} width=${width} height=${height} contentType=${document.contentType}"
        try {
            final formatName = (document.extension ?: FilenameUtils.getExtension(document.filename))?.toLowerCase()
            final original = ImageIO.read(new ByteArrayInputStream(document?.fileContents))
            final scalingFactor = Math.min(width / (original.width as float), height / (original.height as float))

            if (scalingFactor >= 1) {
                log.debug "Existing dimensions suffice: width=${original.width} height=${original.height}"
                ImageIO.write(original, formatName, outputStream)
            } else {
                final thumbnail = original.getScaledInstance(
                    Math.floor(scalingFactor * original.width) as int,
                    Math.floor(scalingFactor * original.height) as int,
                    Image.SCALE_SMOOTH)
                final result = new BufferedImage(
                    thumbnail.getWidth(null),
                    thumbnail.getHeight(null),
                    original.type)
                result.graphics.drawImage(thumbnail, 0, 0, null)
//                    }
                log.debug "Scaled contentType=${document.contentType} to width=${result.width} height=${result.height}"
//            } else {
                ImageIO.write(result, formatName, outputStream)
            }
        } catch (Exception e) {
            log.warn("Error scaling image ${document?.filename}: ${e.message}", e)
        }
    }

    /**
     *
     * @param filePath
     * @return
     */
    File findFile(String filePath) {
        def file
        def appContext = Holders.getGrailsApplication().getParentContext()
        def archiveDirectory = filePath
        if (Holders.getGrailsApplication().isWarDeployed()) {
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
            def actualValue = dataRow.get(columnName)
            // We can't just check the truthiness of the actualValue, because the false boolean would be evaluated to an empty string
            def cellValue = actualValue == null ? "" : actualValue
            // POI can't handle objects so we need to convert all objects to strings unless they are numeric or boolean
            if (!(cellValue instanceof Number) && !(cellValue instanceof Boolean)) {
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
        boldFont.setBold(true)

        // Bold cell style
        CellStyle labelStyle = workbook.createCellStyle()
        labelStyle.setFont(boldFont)

        CellStyle tableHeaderCenterStyle = workbook.createCellStyle()
        CellStyle tableHeaderLeftStyle = workbook.createCellStyle()

        // Bold and align center cell style
        CellStyle boldAndCenterStyle = workbook.createCellStyle()
        boldAndCenterStyle.setAlignment(HorizontalAlignment.CENTER)
        boldAndCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)
        //boldAndCenterStyle.setWrapText(true)

        // Align center cell style
        CellStyle tableDataCenterStyle = workbook.createCellStyle()
        tableDataCenterStyle.setAlignment(HorizontalAlignment.CENTER)
        tableDataCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)

        // Align center cell style
        CellStyle tableDataPalletStyle = workbook.createCellStyle()
        tableDataPalletStyle.setAlignment(HorizontalAlignment.LEFT)
        tableDataPalletStyle.setVerticalAlignment(VerticalAlignment.CENTER)

        // Align left cell style
        CellStyle tableDataLeftStyle = workbook.createCellStyle()
        tableDataLeftStyle.setAlignment(HorizontalAlignment.LEFT)
        tableDataLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER)

        // Align left cell style
        CellStyle tableDataDateStyle = workbook.createCellStyle()
        tableDataDateStyle.setAlignment(HorizontalAlignment.CENTER)
        tableDataDateStyle.setVerticalAlignment(VerticalAlignment.CENTER)
        tableDataDateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy"))


        // Wrap text cell style
        CellStyle wrapTextCellStyle = workbook.createCellStyle()
        wrapTextCellStyle.setWrapText(true)

        // Date cell style
        CellStyle dateStyle = workbook.createCellStyle()
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy"))
        dateStyle.setAlignment(HorizontalAlignment.LEFT)
        dateStyle.setVerticalAlignment(VerticalAlignment.CENTER)

        // Date cell style
        CellStyle timestampStyle = workbook.createCellStyle()
        timestampStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy hh:mm:ss"))
        timestampStyle.setAlignment(HorizontalAlignment.RIGHT)
        timestampStyle.setVerticalAlignment(VerticalAlignment.CENTER)

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
        shipmentInstance.sortShipmentItemsBySortOrder().each { itemInstance ->

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

            row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.displayNameWithLocaleCode)
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
            sheet.setColumnWidth((short) counter++, (short) ((50 * 12) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 5) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 6) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 5) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 4) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 3) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 3) / ((double) 1 / 20)))
            sheet.setColumnWidth((short) counter++, (short) ((50 * 5) / ((double) 1 / 20)))

            // Bold font
            Font boldFont = workbook.createFont()
            boldFont.setBold(true)

            // Bold cell style
            CellStyle labelStyle = workbook.createCellStyle()
            labelStyle.setFont(boldFont)
            labelStyle.setVerticalAlignment(VerticalAlignment.CENTER)

            CellStyle tableHeaderCenterStyle = workbook.createCellStyle()
            tableHeaderCenterStyle.setBorderBottom(BorderStyle.THIN)
            tableHeaderCenterStyle.setBorderLeft(BorderStyle.THIN)
            tableHeaderCenterStyle.setBorderRight(BorderStyle.THIN)
            tableHeaderCenterStyle.setBorderTop(BorderStyle.THIN)
            tableHeaderCenterStyle.setFont(boldFont)
            tableHeaderCenterStyle.setWrapText(true)

            CellStyle tableHeaderLeftStyle = workbook.createCellStyle()
            tableHeaderLeftStyle.setBorderBottom(BorderStyle.THIN)
            tableHeaderLeftStyle.setBorderLeft(BorderStyle.THIN)
            tableHeaderLeftStyle.setBorderRight(BorderStyle.THIN)
            tableHeaderLeftStyle.setBorderTop(BorderStyle.THIN)
            tableHeaderLeftStyle.setFont(boldFont)
            tableHeaderLeftStyle.setWrapText(true)

            // Bold and align center cell style
            CellStyle boldAndCenterStyle = workbook.createCellStyle()
            boldAndCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            boldAndCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            boldAndCenterStyle.setBorderBottom(BorderStyle.THIN)
            boldAndCenterStyle.setBorderLeft(BorderStyle.THIN)
            boldAndCenterStyle.setBorderRight(BorderStyle.THIN)
            boldAndCenterStyle.setBorderTop(BorderStyle.THIN)
            boldAndCenterStyle.setFont(boldFont)
            boldAndCenterStyle.setWrapText(true)

            // Align center cell style
            CellStyle tableDataCenterStyle = workbook.createCellStyle()
            tableDataCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            tableDataCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataCenterStyle.setBorderBottom(BorderStyle.THIN)
            tableDataCenterStyle.setBorderLeft(BorderStyle.THIN)
            tableDataCenterStyle.setBorderRight(BorderStyle.THIN)
            tableDataCenterStyle.setBorderTop(BorderStyle.THIN)

            // Align center cell style
            CellStyle tableDataPalletStyle = workbook.createCellStyle()
            tableDataPalletStyle.setAlignment(HorizontalAlignment.LEFT)
            tableDataPalletStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataPalletStyle.setBorderBottom(BorderStyle.THIN)
            tableDataPalletStyle.setBorderLeft(BorderStyle.THIN)
            tableDataPalletStyle.setBorderRight(BorderStyle.THIN)
            tableDataPalletStyle.setBorderTop(BorderStyle.THIN)

            // Align left cell style
            CellStyle tableDataLeftStyle = workbook.createCellStyle()
            tableDataLeftStyle.setAlignment(HorizontalAlignment.LEFT)
            tableDataLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataLeftStyle.setBorderBottom(BorderStyle.THIN)
            tableDataLeftStyle.setBorderLeft(BorderStyle.THIN)
            tableDataLeftStyle.setBorderRight(BorderStyle.THIN)
            tableDataLeftStyle.setBorderTop(BorderStyle.THIN)
            tableDataLeftStyle.setWrapText(true)

            // Align left cell style
            CellStyle tableDataDateStyle = workbook.createCellStyle()
            tableDataDateStyle.setAlignment(HorizontalAlignment.CENTER)
            tableDataDateStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataDateStyle.setBorderBottom(BorderStyle.THIN)
            tableDataDateStyle.setBorderLeft(BorderStyle.THIN)
            tableDataDateStyle.setBorderRight(BorderStyle.THIN)
            tableDataDateStyle.setBorderTop(BorderStyle.THIN)
            tableDataDateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy"))


            // Wrap text cell style
            CellStyle wrapTextCellStyle = workbook.createCellStyle()
            wrapTextCellStyle.setWrapText(true)

            CellStyle topAlignedTextCellStyle = workbook.createCellStyle()
            topAlignedTextCellStyle.setWrapText(true)
            topAlignedTextCellStyle.setVerticalAlignment(VerticalAlignment.TOP)

            // Date cell style
            CellStyle dateStyle = workbook.createCellStyle()
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy"))
            dateStyle.setAlignment(HorizontalAlignment.LEFT)
            dateStyle.setVerticalAlignment(VerticalAlignment.CENTER)

            // Date cell style
            CellStyle timestampStyle = workbook.createCellStyle()
            timestampStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-mmm-yyyy hh:mm:ss"))
            timestampStyle.setAlignment(HorizontalAlignment.RIGHT)
            timestampStyle.setVerticalAlignment(VerticalAlignment.CENTER)

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

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'productSupplier.supplierCode.label', default: 'Supplier code'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'productSupplier.supplierName.label', default: 'Supplier Product Name'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'productSupplier.manufacturer.label', default: 'Manufacturer'))
            row.getCell(CELL_INDEX++).setCellStyle(tableHeaderLeftStyle)

            row.createCell(CELL_INDEX).setCellValue("" + getMessageTagLib().message(code: 'productSupplier.manufacturerCode.label', default: 'Manufacturer Code'))
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
            shipmentInstance.sortShipmentItemsBySortOrder().eachWithIndex { itemInstance, index ->

                CELL_INDEX = 0
                log.debug "Adding item  to packing list " + itemInstance?.product?.name + " -> " + itemInstance?.container?.name
                row = sheet.createRow((short) counter++)
                boolean isLastItem = index == shipmentInstance.sortShipmentItemsBySortOrder().size() - 1

                if ((itemInstance?.container?.parentContainer && previousParentContainer != itemInstance?.container?.parentContainer?.name)
                        || (!itemInstance?.container?.parentContainer && previousParentContainer != itemInstance?.container?.name)) {
                    row.createCell(CELL_INDEX).setCellValue(itemInstance?.container?.parentContainer?.name ?: itemInstance?.container?.name ?: getMessageTagLib().message(code: 'shipping.unpacked.label').toString())
                    row.getCell(CELL_INDEX++).setCellStyle(tableDataPalletStyle)
                    // If we're at a place in the XLS file where we want to merge cells (e.g. the packing list)
                    // Then we merge rows when the container name is different from the previous container name
                    if (packLevelOneInitialRowIndex != packLevelOneFinalRowIndex) {
                        sheet.addMergedRegion(CellRangeAddress.valueOf("A${packLevelOneInitialRowIndex + 1}:A${packLevelOneFinalRowIndex + 1}"))
                    }

                    packLevelOneInitialRowIndex = row.getRowNum()
                    packLevelOneFinalRowIndex = row.getRowNum()
                } else {
                    packLevelOneFinalRowIndex = row.getRowNum()
                    // Merge columns if pack level one is same as previous one but it's the last element in array
                    if (isLastItem) {
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
                    if (packLevelTwoInitialRowIndex != packLevelTwoFinalRowIndex) {
                        sheet.addMergedRegion(CellRangeAddress.valueOf("B${packLevelTwoInitialRowIndex + 1}:B${packLevelTwoFinalRowIndex + 1}"))
                    }
                    packLevelTwoInitialRowIndex = row.getRowNum()
                    packLevelTwoFinalRowIndex = row.getRowNum()
                } else {
                    packLevelTwoFinalRowIndex = row.getRowNum()
                    // Merge columns if pack level two is same as previous one but it's the last element in array
                    if (isLastItem) {
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

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.displayNameWithLocaleCode)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)


                def suppliersList = itemInstance?.orderItems*.productSupplier
                ProductSupplier supplier = suppliersList.size() > 0 ? suppliersList.get(0) : null

                row.createCell(CELL_INDEX).setCellValue(supplier?.supplierCode ?: '')
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.createCell(CELL_INDEX).setCellValue(supplier?.name ?: '')
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(supplier?.manufacturer?.toString() ?: '')
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.createCell(CELL_INDEX).setCellValue(supplier?.manufacturerCode ?: '')
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

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
            throw e
        }
    }

    void generateInventoryTemplate(OutputStream outputStream, List<Map> data) {
        try {
            Workbook workbook = new HSSFWorkbook()
            CreationHelper createHelper = workbook.getCreationHelper()
            HSSFSheet sheet = workbook.createSheet("Sheet1")
            createExcelHeader(sheet, 0, data.get(0).keySet().toList())
            data.eachWithIndex { Map dataRow, rowIndex ->
                Row excelRow = sheet.createRow(rowIndex + 1)
                dataRow.keySet().eachWithIndex { columnName, cellIndex ->
                    def cellValue = dataRow.get(columnName) ?: ""
                    // POI can't handle objects so we need to convert all objects to strings unless they are numeric
                    if (!(cellValue instanceof Number)) {
                        cellValue = cellValue.toString()
                    }
                    excelRow.createCell(cellIndex)
                    if (columnName == 'Expiration date' && cellValue != '') {
                        def formatter = new SimpleDateFormat("MM/dd/yyyy")
                        Date date = formatter.parse(cellValue)
                        CellStyle cellStyle = workbook.createCellStyle()
                        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("MM/dd/yyyy"))
                        excelRow.getCell(cellIndex).setCellValue(date)
                        excelRow.getCell(cellIndex).setCellStyle(cellStyle)
                    } else {
                        excelRow.getCell(cellIndex).setCellValue(cellValue)
                    }
                }
            }
            for (int i = 0; i <= data.get(0).size(); i++) {
                sheet.autoSizeColumn(i)
            }
            workbook.write(outputStream)
            outputStream.close()
        } catch (IOException e) {
            log.error("IO exception while generating import template")
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
            boldFont.setBold(true)

            // Bold cell style
            CellStyle labelStyle = workbook.createCellStyle()
            labelStyle.setFont(boldFont)

            // Label center style
            CellStyle labelCenterStyle = workbook.createCellStyle()
            labelCenterStyle.setFont(boldFont)
            labelCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            labelCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)

            CellStyle tableHeaderCenterStyle = workbook.createCellStyle()
            tableHeaderCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            tableHeaderCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableHeaderCenterStyle.setBorderBottom(BorderStyle.THIN)
            tableHeaderCenterStyle.setBorderLeft(BorderStyle.THIN)
            tableHeaderCenterStyle.setBorderRight(BorderStyle.THIN)
            tableHeaderCenterStyle.setBorderTop(BorderStyle.THIN)
            tableHeaderCenterStyle.setFont(boldFont)
            tableHeaderCenterStyle.setWrapText(true)

            CellStyle tableHeaderLeftStyle = workbook.createCellStyle()
            tableHeaderLeftStyle.setBorderBottom(BorderStyle.THIN)
            tableHeaderLeftStyle.setBorderLeft(BorderStyle.THIN)
            tableHeaderLeftStyle.setBorderRight(BorderStyle.THIN)
            tableHeaderLeftStyle.setBorderTop(BorderStyle.THIN)
            tableHeaderLeftStyle.setFont(boldFont)
            tableHeaderLeftStyle.setWrapText(true)

            // Bold and align center cell style
            CellStyle boldAndCenterStyle = workbook.createCellStyle()
            boldAndCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            boldAndCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            boldAndCenterStyle.setBorderBottom(BorderStyle.THIN)
            boldAndCenterStyle.setBorderLeft(BorderStyle.THIN)
            boldAndCenterStyle.setBorderRight(BorderStyle.THIN)
            boldAndCenterStyle.setBorderTop(BorderStyle.THIN)
            boldAndCenterStyle.setFont(boldFont)
            boldAndCenterStyle.setWrapText(true)

            // Align center cell style
            CellStyle tableDataCenterStyle = workbook.createCellStyle()
            tableDataCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            tableDataCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataCenterStyle.setBorderBottom(BorderStyle.THIN)
            tableDataCenterStyle.setBorderLeft(BorderStyle.THIN)
            tableDataCenterStyle.setBorderRight(BorderStyle.THIN)
            tableDataCenterStyle.setBorderTop(BorderStyle.THIN)

            // Align left cell style
            CellStyle tableDataLeftStyle = workbook.createCellStyle()
            tableDataLeftStyle.setAlignment(HorizontalAlignment.LEFT)
            tableDataLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataLeftStyle.setBorderBottom(BorderStyle.THIN)
            tableDataLeftStyle.setBorderLeft(BorderStyle.THIN)
            tableDataLeftStyle.setBorderRight(BorderStyle.THIN)
            tableDataLeftStyle.setBorderTop(BorderStyle.THIN)
            tableDataLeftStyle.setWrapText(true)

            // Align left cell style
            CellStyle tableDataDateStyle = workbook.createCellStyle()
            tableDataDateStyle.setAlignment(HorizontalAlignment.CENTER)
            tableDataDateStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataDateStyle.setBorderBottom(BorderStyle.THIN)
            tableDataDateStyle.setBorderLeft(BorderStyle.THIN)
            tableDataDateStyle.setBorderRight(BorderStyle.THIN)
            tableDataDateStyle.setBorderTop(BorderStyle.THIN)
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
            def previousContainer = ""
            shipmentInstance.sortShipmentItemsBySortOrder().each { itemInstance ->

                CELL_INDEX = 0
                row = sheet.createRow((short) counter++)
                def totalCost = 0
                if (itemInstance?.product?.pricePerUnit && hasRoleFinance) {
                    totalCost = itemInstance?.quantity * itemInstance?.product?.pricePerUnit
                }

                row.createCell(CELL_INDEX).setCellValue(row.getRowNum() - 6)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataCenterStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.productCode)
                row.getCell(CELL_INDEX++).setCellStyle(tableDataLeftStyle)

                row.createCell(CELL_INDEX).setCellValue(itemInstance?.inventoryItem?.product?.displayNameWithLocaleCode)
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
            boldFont.setBold(true)

            // Bold cell style
            CellStyle labelStyle = workbook.createCellStyle()
            labelStyle.setFont(boldFont)

            // Label center style
            CellStyle labelCenterStyle = workbook.createCellStyle()
            labelCenterStyle.setFont(boldFont)
            labelCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            labelCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)

            CellStyle tableHeaderCenterStyle = workbook.createCellStyle()
            tableHeaderCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            tableHeaderCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableHeaderCenterStyle.setBorderBottom(BorderStyle.THIN)
            tableHeaderCenterStyle.setBorderLeft(BorderStyle.THIN)
            tableHeaderCenterStyle.setBorderRight(BorderStyle.THIN)
            tableHeaderCenterStyle.setBorderTop(BorderStyle.THIN)
            tableHeaderCenterStyle.setFont(boldFont)
            tableHeaderCenterStyle.setWrapText(true)

            CellStyle tableHeaderLeftStyle = workbook.createCellStyle()
            tableHeaderLeftStyle.setBorderBottom(BorderStyle.THIN)
            tableHeaderLeftStyle.setBorderLeft(BorderStyle.THIN)
            tableHeaderLeftStyle.setBorderRight(BorderStyle.THIN)
            tableHeaderLeftStyle.setBorderTop(BorderStyle.THIN)
            tableHeaderLeftStyle.setFont(boldFont)
            tableHeaderLeftStyle.setWrapText(true)

            // Bold and align center cell style
            CellStyle boldAndCenterStyle = workbook.createCellStyle()
            boldAndCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            boldAndCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            boldAndCenterStyle.setBorderBottom(BorderStyle.THIN)
            boldAndCenterStyle.setBorderLeft(BorderStyle.THIN)
            boldAndCenterStyle.setBorderRight(BorderStyle.THIN)
            boldAndCenterStyle.setBorderTop(BorderStyle.THIN)
            boldAndCenterStyle.setFont(boldFont)
            boldAndCenterStyle.setWrapText(true)

            // Align center cell style
            CellStyle tableDataCenterStyle = workbook.createCellStyle()
            tableDataCenterStyle.setAlignment(HorizontalAlignment.CENTER)
            tableDataCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataCenterStyle.setBorderBottom(BorderStyle.THIN)
            tableDataCenterStyle.setBorderLeft(BorderStyle.THIN)
            tableDataCenterStyle.setBorderRight(BorderStyle.THIN)
            tableDataCenterStyle.setBorderTop(BorderStyle.THIN)

            // Align left cell style
            CellStyle tableDataLeftStyle = workbook.createCellStyle()
            tableDataLeftStyle.setAlignment(HorizontalAlignment.LEFT)
            tableDataLeftStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataLeftStyle.setBorderBottom(BorderStyle.THIN)
            tableDataLeftStyle.setBorderLeft(BorderStyle.THIN)
            tableDataLeftStyle.setBorderRight(BorderStyle.THIN)
            tableDataLeftStyle.setBorderTop(BorderStyle.THIN)
            tableDataLeftStyle.setWrapText(true)

            // Align left cell style
            CellStyle tableDataDateStyle = workbook.createCellStyle()
            tableDataDateStyle.setAlignment(HorizontalAlignment.CENTER)
            tableDataDateStyle.setVerticalAlignment(VerticalAlignment.CENTER)
            tableDataDateStyle.setBorderBottom(BorderStyle.THIN)
            tableDataDateStyle.setBorderLeft(BorderStyle.THIN)
            tableDataDateStyle.setBorderRight(BorderStyle.THIN)
            tableDataDateStyle.setBorderTop(BorderStyle.THIN)
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
            throw e
        }
    }

    List<Document> getAllDocumentsBySupplierOrganization(Organization supplierOrganization) {
        def documents = Order.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("id", "orderId")
                    property("orderNumber", "orderNumber")
                    property("name", "orderDescription")
                    origin {
                        property("name", "origin")
                    }
                    destination {
                        property("name", "destination")
                    }
                    documents {
                        property("id", "documentId")
                        property("documentNumber", "documentNumber")
                        documentType(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                            property("name", "documentType")
                        }
                        property("name", "documentName")
                        property("contentType", "fileType")
                        property("fileUri", "fileUri")
                    }
                }
                eq("originParty", supplierOrganization)
                eq("orderType", OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()))
            }

        return documents
    }

    List<DocumentType> getNonTemplateDocumentTypes() {
        return DocumentType.createCriteria().list() {
            or {
                isNull("documentCode")
                not {
                    'in'('documentCode', DocumentCode.templateList())
                }
            }
        }.sort { it.name }
    }

}
