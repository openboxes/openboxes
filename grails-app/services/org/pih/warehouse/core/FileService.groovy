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

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.lang.text.StrSubstitutor
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.docx4j.TextUtils
import org.docx4j.XmlUtils
import org.docx4j.convert.out.pdf.PdfConversion
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion
import org.docx4j.fonts.IdentityPlusMapper
import org.docx4j.jaxb.Context
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import org.docx4j.wml.Body
import org.docx4j.wml.BooleanDefaultTrue
import org.docx4j.wml.CTBorder
import org.docx4j.wml.Document
import org.docx4j.wml.HpsMeasure
import org.docx4j.wml.P
import org.docx4j.wml.R
import org.docx4j.wml.RFonts
import org.docx4j.wml.RPr
import org.docx4j.wml.STBorder
import org.docx4j.wml.Tbl
import org.docx4j.wml.TblBorders
import org.docx4j.wml.TblGrid
import org.docx4j.wml.TblGridCol
import org.docx4j.wml.TblPr
import org.docx4j.wml.Tc
import org.docx4j.wml.Text
import org.docx4j.wml.Tr
import org.docx4j.wml.TrPr
import org.pih.warehouse.FormatTagLib
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

import javax.xml.bind.JAXBElement
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

class FileService {
    boolean transactional = false

    def userService
    def grailsApplication

    File createDirectory(String directoryPath) {
        File folder = new File(directoryPath)
        log.info("Attempting to create directory ${folder?.absolutePath}")
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                log.info("- Directory ${folder?.absolutePath} has been created")
                if (!folder.canWrite()) {
                    log.error("- Directory ${folder?.absolutePath} is not writable")
                } else {
                    log.info("- Directory ${folder?.absolutePath} is writable")
                }
            } else {
                log.error("- Directory ${folder?.absolutePath} cannot be created")
            }
        } else {
            log.info("- Directory ${folder?.absolutePath} already exists")
        }
        return folder
    }

    File renderShippingTemplate(org.pih.warehouse.core.Document documentTemplate, Shipment shipmentInstance) {

        //log.info "Document template: " + documentTemplate.fileContents

        ByteArrayInputStream inputStream = new ByteArrayInputStream(documentTemplate.fileContents)

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputStream)

        // 2. Fetch the document part
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart()

        Document wmlDocumentEl = (Document) documentPart.getJaxbElement()

        // Get document as XML string
        def xml = XmlUtils.marshaltoString(wmlDocumentEl, true)

        def dataMappings = getDataMappings(shipmentInstance)

        StrSubstitutor strSubstitutor = new StrSubstitutor(dataMappings)
        Object obj = XmlUtils.unmarshalString(strSubstitutor.replace(xml));

        documentPart.setJaxbElement((Document) obj)

        // Create a new table for the Packing List
        Map packingList = getPackingList(shipmentInstance)
        Tbl table = createTable(wordMLPackage, packingList.columns, packingList.data)
        insertTable(wordMLPackage, "{{PACKING_LIST}}", table)

        // Create a table for commercial invoice
        Map commercialInvoice = getCommercialInvoice(shipmentInstance)
        Tbl commercialInvoiceTable = createTable(wordMLPackage, commercialInvoice.columns, commercialInvoice.data)
        insertTable(wordMLPackage, "{{COMMERCIAL_INVOICE}}", commercialInvoiceTable)

        // Create table for variables
        def dataMappingsTable = []
        dataMappings.keySet().collect { key ->
            dataMappingsTable.add(["propertyName": key, "propertyValue": dataMappings.get(key).toString()])
        }

        Map columns = [
                propertyName : [label: "Name", ratio: 1.0],
                propertyValue: [label: "Value", ratio: 1.0]
        ]
        Tbl variablesTable = createTable(wordMLPackage, columns, dataMappingsTable)
        insertTable(wordMLPackage, "{{VARIABLES}}", variablesTable)

        // FIXME Try to generate a BAOS
        File tempFile = File.createTempFile("${shipmentInstance?.name} - ${documentTemplate.name}", ".docx")
        wordMLPackage.save(tempFile)

        return tempFile

    }

    Map getPackingList(Shipment shipment) {

        def shipmentItems = shipment?.shipmentItems?.sort()
        def data = shipmentItems.collect { ShipmentItem shipmentItem ->
            def parentContainer = shipmentItem?.container?.parentContainer
            def childContainer = shipmentItem?.container
            return [
                    parentContainer: parentContainer ? parentContainer?.name : childContainer?.name,
                    childContainer : parentContainer ? childContainer?.name : null,
                    productName    : shipmentItem.inventoryItem?.product?.name,
                    lotNumber      : shipmentItem?.inventoryItem?.lotNumber ?: "",
                    expirationDate : shipmentItem?.inventoryItem?.expirationDate?.format("dd-MMM-yyyy") ?: "",
                    quantity       : "${shipmentItem?.quantity} ${shipmentItem?.inventoryItem?.product?.unitOfMeasure ?: ''}",
            ]
        }

        // FIXME Need to remove this once the container sort order bug is fixed
        data = data.sort(new OrderBy([{NumberUtils.toInt(it.parentContainer, 0)},
                                      {it?.parentContainer},
                                      {it?.childContainer},
                                      {it?.productName}]))

        def columns = [
                parentContainer: [label: "Pack level 1", ratio: 0.75],
                childContainer : [label: "Pack level 2", ratio: 0.75],
                productName    : [label: "Product", ratio: 2.0],
                lotNumber      : [label: "Lot", ratio: 0.75],
                expirationDate : [label: "Exp", ratio: 0.75],
                quantity       : [label: "Qty", ratio: 0.5]
        ]

        def hasNoContainers = data.every { !it.parentContainer && !it.childContainer }
        if (hasNoContainers) {
            columns.remove("parentContainer")
            columns.remove("childContainer")
        }
        return [columns: columns, data: data]
    }

    Map getCommercialInvoice(Shipment shipment) {

        NumberFormat numberFormat = NumberFormat.getNumberInstance()
        DecimalFormat decimalFormat = new DecimalFormat("#,###,##0.00");
        def data = shipment?.shipmentItems?.collect { ShipmentItem shipmentItem ->
            BigDecimal quantity = shipmentItem?.quantity ?: 0
            BigDecimal unitCost = shipmentItem?.inventoryItem?.product?.pricePerUnit ?: 0.0
            BigDecimal totalCost = quantity * unitCost
            return [
                    productCode   : shipmentItem.inventoryItem?.product?.productCode,
                    productName   : shipmentItem.inventoryItem?.product?.name,
                    unitOfMeasure : shipmentItem.inventoryItem?.product?.unitOfMeasure,
                    lotNumber     : shipmentItem?.inventoryItem?.lotNumber ?: "",
                    expirationDate: shipmentItem?.inventoryItem?.expirationDate?.format("dd-MMM-yyyy") ?: "",
                    quantity      : numberFormat.format(quantity),
                    unitCost      : decimalFormat.format(unitCost),
                    totalCost     : decimalFormat.format(totalCost)
            ]
        }

        data.sort { it.productCode }

        // Calculate total cost across shipment items
        BigDecimal totalCost = shipment?.shipmentItems?.sum { shipmentItem ->
            BigDecimal quantity = shipmentItem?.quantity ?: 0
            BigDecimal unitCost = shipmentItem?.inventoryItem?.product?.pricePerUnit ?: 0.0
            return quantity * unitCost
        }
        data.add(
                [
                        productCode   : null,
                        productName   : null,
                        unitOfMeasure : null,
                        lotNumber     : null,
                        expirationDate: null,
                        quantity      : null,
                        unitCost      : "Total",
                        totalCost     : decimalFormat.format(totalCost)
                ]
        )

        def columns = [
                productCode   : [label: "Code", ratio: 0.75],
                productName   : [label: "Description", ratio: 2.0],
                unitOfMeasure : [label: "UoM", ratio: 0.75],
                lotNumber     : [label: "Batch No", ratio: 1.0],
                expirationDate: [label: "Exp Date", ratio: 1.25],
                quantity      : [label: "Quantity", ratio: 0.75],
                unitCost      : [label: "Unit Cost (USD)", ratio: 0.75],
                totalCost     : [label: "Total Cost (USD)", ratio: 1.0],
        ]

        return [columns: columns, data: data]
    }

    Tbl createTable(WordprocessingMLPackage wordMLPackage, Map columns, List data) {

        Tbl table = Context.getWmlObjectFactory().createTbl()
        addBorders(table)

        // Add columns to header
        Tr thead = Context.getWmlObjectFactory().createTr()
        for(def key in columns.keySet()) {
            addTc(thead, columns[key].label, "Book Antiqua", 10, true)
        }

        // Set table header to repeat on subsequent pages
        TrPr trPr = createRepeatingHeader()
        thead.setTrPr(trPr);


        // Add table header to table
        table.getContent().add(thead)

        int cols = columns.size()
        int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips()
        int cellWidthTwipsDefault = new Double(Math.floor((writableWidthTwips / cols))).intValue()
        def cellWidth = { ratio -> (ratio * cellWidthTwipsDefault).intValue(); }

        // Set width for header columns
        def headerColumnKeys = columns.keySet().toArray()
        TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid()
        table.setTblGrid(tblGrid)
        for (int i = 0; i < cols; i++) {
            TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol()
            def column = columns.get(headerColumnKeys[i])
            gridCol.setW(BigInteger.valueOf(cellWidth(column.ratio)))
            tblGrid.getGridCol().add(gridCol)
        }

        // Iterate over data rows and add a cell for each column
        for (def row : data) {
            Tr tr = Context.getWmlObjectFactory().createTr()
            table.getContent().add(tr)
            for (def key in columns.keySet()) {
                addTc(tr, row[key], "Book Antiqua", 10, false)
            }
        }
        return table
    }

    def addBorders(Tbl table) {
        table.setTblPr(new TblPr())
        CTBorder border = new CTBorder()
        border.setColor("auto")
        border.setSz(new BigInteger("4"))
        border.setSpace(new BigInteger("0"))
        border.setVal(STBorder.SINGLE)

        TblBorders borders = new TblBorders()
        borders.setBottom(border)
        borders.setLeft(border)
        borders.setRight(border)
        borders.setTop(border)
        borders.setInsideH(border)
        borders.setInsideV(border)
        table.getTblPr().setTblBorders(borders)
    }

    private TrPr createRepeatingHeader() {
        TrPr trPr = Context.getWmlObjectFactory().createTrPr();
        BooleanDefaultTrue defaultTrue = Context.getWmlObjectFactory().createBooleanDefaultTrue();
        JAXBElement<BooleanDefaultTrue> defaultTrueElement = Context.getWmlObjectFactory().createCTTrPrBaseTblHeader(defaultTrue);
        trPr.getCnfStyleOrDivIdOrGridBefore().add(defaultTrueElement);
        return trPr
    }

    private RPr createRunProperties(final String fontName, final int fontSize, final boolean applyBold) {
        final RPr rpr = Context.getWmlObjectFactory().createRPr();

        if (fontName) {
            final RFonts font = Context.getWmlObjectFactory().createRFonts();
            font.setAscii(fontName);
            font.setHAnsi(fontName);
            rpr.setRFonts(font);
        }

        if (fontSize) {
            final HpsMeasure size = Context.getWmlObjectFactory().createHpsMeasure();
            size.setVal(BigInteger.valueOf(fontSize * 2));
            rpr.setSz(size);
        }

        if (applyBold) {
            final BooleanDefaultTrue isBold = Context.getWmlObjectFactory().createBooleanDefaultTrue();
            isBold.setVal(applyBold);
            rpr.setB(isBold);
        }
        return rpr;
    }

    /**
     *
     * TODO Clean this up a bit.
     *
     * @param shipmentInstance
     * @return
     */
    Map<String, String> getDataMappings(Shipment shipmentInstance) {

        Boolean hasRoleFinance = userService.hasRoleFinance()

        // Map of key/value pairs that will be used to hold variables
        def mappings = new HashMap<String, String>()

        // Add today's date as an implicit variable
        def formatter = new SimpleDateFormat("MMMMM dd, yyyy")
        def date = formatter.format(shipmentInstance.getExpectedShippingDate())
        def today = formatter.format(new Date())
        mappings.put("TODAY", today)
        mappings.put("DATE", date)

        // Shipment
        addObjectProperties(mappings, "SHIPMENT", shipmentInstance, Shipment.class)

        // Origin
        addObjectProperties(mappings, "ORIGIN", shipmentInstance?.origin, Location.class)
        addObjectProperties(mappings, "ORIGIN.ADDRESS", shipmentInstance?.origin?.address, Address.class)
        addObjectProperties(mappings, "ORIGIN.LOCATION_GROUP.ADDRESS",
                shipmentInstance?.origin?.locationGroup?.address, Address.class)

        // Destination
        addObjectProperties(mappings, "DESTINATION", shipmentInstance?.destination, Location.class)
        addObjectProperties(mappings, "DESTINATION.ADDRESS", shipmentInstance?.destination?.address, Address.class)
        addObjectProperties(mappings, "DESTINATION.LOCATION_GROUP.ADDRESS",
                shipmentInstance?.destination?.locationGroup?.address, Address.class)

        // Other associations
        addObjectProperties(mappings, "CARRIER", shipmentInstance?.carrier, Person.class)

        // Add all reference numbers
        shipmentInstance.referenceNumbers.each { ReferenceNumber referenceNumber ->
            log.info "Reference number ${referenceNumber?.referenceNumberType} = " + referenceNumber?.identifier
            FormatTagLib formatTag = grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
            String referenceNumberType = formatTag.metadata(obj: referenceNumber.referenceNumberType)
            referenceNumberType = referenceNumberType.toUpperCase().replaceAll(" ", "_")
            mappings.put(referenceNumberType, referenceNumber?.identifier)
        }

        // Add additional properties generated
        def decimalFormatter = new DecimalFormat("\$###,##0.00")
        String totalValue = decimalFormatter.format(shipmentInstance?.calculateTotalValue() ?: 0.0)
        mappings.put("STATUS", shipmentInstance?.getStatus())
        mappings.put("TOTAL_VALUE", totalValue)

        ReferenceNumberType trackingNumberType = ReferenceNumberType.findById(Constants.TRACKING_NUMBER_TYPE_ID)
        ReferenceNumber trackingNumber = shipmentInstance?.referenceNumbers?.find { ReferenceNumber rn ->
            rn.referenceNumberType?.id == trackingNumberType?.id
        }

        mappings.put("TRACKING_NUMBER", trackingNumber?.identifier)
        mappings.put("DRIVER_NAME", shipmentInstance?.driverName)
        mappings.put("FREIGHT_FORWARDER", shipmentInstance?.shipmentMethod?.shipper?.name)
        mappings.put("ACTUAL_SHIPPING_DATE", shipmentInstance?.getActualShippingDate())
        mappings.put("ACTUAL_DELIVERY_DATE", shipmentInstance?.getActualDeliveryDate())

        if (!hasRoleFinance) {
            def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
            String accessDeniedMessage = "${g.message(code: 'access.accessDenied.label')}"
            mappings.put("TOTAL_VALUE", accessDeniedMessage)
            mappings.put("TOTALVALUE", accessDeniedMessage) // for backwards compatibility
        }


        // Remove any sensitive or
        mappings.remove("ORIGIN.LOGO")
        mappings.remove("DESTINATION.LOGO")


        return mappings
    }

    def addObjectProperties(Map dataMappings, String prefix, Object object, Class domainClass) {
        if (object) {
            new DefaultGrailsDomainClass(domainClass).persistentProperties.each { property ->
                if (!property.isAssociation()) {
                    log.info " [included] property " + property.name + " = " + property.naturalName + " " + property.fieldName
                    String propertyName = prefix ? prefix + "." + property?.fieldName : property?.fieldName
                    dataMappings.put(propertyName, object.properties[property.name])
                } else {
                    log.info " [excluded] association " + property.name + " = " + property.naturalName + " " + property.fieldName

                }
            }
        }
    }

    /**
     *
     * @param pkg
     * @param afterText
     * @param table
     * @throws Exception
     */
    void insertTable(WordprocessingMLPackage pkg, String afterText, Tbl table) throws Exception {
        Body b = pkg.getMainDocumentPart().getJaxbElement().getBody()
        int addPoint = -1, index = 0
        for (Object o : b.getContent()) {
            if (o instanceof P && getElementText(o).startsWith(afterText)) {
                b.getContent().set(index, table)
            }
            index++
        }
    }

    String getElementText(Object jaxbElem) throws Exception {
        StringWriter sw = new StringWriter()
        TextUtils.extractText(jaxbElem, sw)
        return sw.toString()
    }

    /**
     *
     * @param wmlPackage
     * @param tr
     * @param text
     * @param applyBold
     */
    void addTc(Tr tr, String text, boolean applyBold) {
        addTc(tr, text, null, 0, applyBold)
    }

    void addTc(Tr tr, String text, String fontName, int fontSize, boolean applyBold) {
        Tc tc = Context.getWmlObjectFactory().createTc()
        tc.getContent().add(createParagraphOfText(text, fontName, fontSize, applyBold))
        tr.getContent().add(tc)
    }

    P createParagraphOfText(String simpleText, final String fontName, final int fontSize, boolean applyBold) {
        P para = Context.getWmlObjectFactory().createP()
        // Create the text element
        Text t = Context.getWmlObjectFactory().createText()
        t.setValue(simpleText)
        // Create the run
        R run = Context.getWmlObjectFactory().createR()
        run.getContent().add(t)

        RPr rPr = createRunProperties(fontName, fontSize, applyBold)
        run.setRPr(rPr)
        para.getContent().add(run)

        return para
    }

    /**
     *
     * @param wordMLPackage
     * @return
     */
    void convertToPdf(WordprocessingMLPackage wordMLPackage, OutputStream outputStream) {
        wordMLPackage.setFontMapper(new IdentityPlusMapper())
        PdfConversion conversion = new Conversion(wordMLPackage)
        conversion.output(outputStream)
    }
}
