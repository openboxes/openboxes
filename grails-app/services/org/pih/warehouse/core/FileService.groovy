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

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.docx4j.TextUtils
import org.docx4j.XmlUtils
import org.docx4j.convert.out.pdf.PdfConversion
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion
import org.docx4j.fonts.IdentityPlusMapper
import org.docx4j.jaxb.Context
import org.docx4j.model.table.TblFactory
import org.docx4j.openpackaging.io.SaveToZipFile
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import org.docx4j.openpackaging.parts.relationships.Namespaces
import org.docx4j.wml.Body
import org.docx4j.wml.BooleanDefaultTrue
import org.docx4j.wml.CTBorder
import org.docx4j.wml.Document
import org.docx4j.wml.P
import org.docx4j.wml.R
import org.docx4j.wml.RPr
import org.docx4j.wml.STBorder
import org.docx4j.wml.Tbl
import org.docx4j.wml.TblBorders
import org.docx4j.wml.TblGrid
import org.docx4j.wml.TblGridCol
import org.docx4j.wml.TblPr
import org.docx4j.wml.TblWidth
import org.docx4j.wml.Tc
import org.docx4j.wml.TcPr
import org.docx4j.wml.Text
import org.docx4j.wml.Tr
import org.docx4j.wml.TrPr
import org.pih.warehouse.FormatTagLib
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

import javax.xml.bind.JAXBException
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class FileService {
    boolean transactional = false

    def userService
    def grailsApplication

    File findFile(String filePath) {
        def file
        def appContext = ApplicationHolder.application.parentContext
        def archiveDirectory = filePath
        if (ApplicationHolder.application.isWarDeployed()) {
            //archiveDirectory = "${File.separator}WEB-INF${File.separator}grails-app${File.separator}conf${File.separator}${filePath}"
            archiveDirectory = "classpath:$filePath"
            file = appContext.getResource(archiveDirectory)?.getFile()
        } else {
            archiveDirectory = "grails-app${File.separator}conf${File.separator}${filePath}"
            file = new File(archiveDirectory)
        }
        return file
    }

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

        log.info "Document template: " + documentTemplate.fileContents

        ByteArrayInputStream inputStream = new ByteArrayInputStream(documentTemplate.fileContents)

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputStream)

        // 2. Fetch the document part
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart()

        Document wmlDocumentEl = (Document) documentPart.getJaxbElement()

        // Get document as XML string
        def xml = XmlUtils.marshaltoString(wmlDocumentEl, true)

        def dataMappings = getDataMappings(shipmentInstance)


        log.info("mappings: " + dataMappings)
        log.debug("XML before: " + xml)
        Object obj = XmlUtils.unmarshallFromTemplate(xml, dataMappings)
        log.debug("XML after: " + xml)

        //change  JaxbElement
        documentPart.setJaxbElement((Document) obj)

        // Create a new table for the Packing List
        Tbl table = createPackingListTable(wordMLPackage, shipmentInstance)

        // Add table to document
        //wordMLPackage.getMainDocumentPart().addObject(table);

        insertTable(wordMLPackage, "{{PACKING_LIST}}", table)


        // Get the data mappings
        def dataMappingsTable = []
        dataMappings.each { key, value ->
            def rowMap = new HashMap()
            rowMap.put("propertyName", key)
            rowMap.put("propertyValue", value)
            log.info("${key} = ${value}")
            dataMappingsTable.add(rowMap)

        }
        Tbl debugTable = createTable(wordMLPackage, dataMappingsTable)
        insertTable(wordMLPackage, "{{VARIABLES}}", debugTable)

        // FIXME Try to generate a BAOS
        File tempFile = File.createTempFile("${shipmentInstance?.name} - ${documentTemplate.name}", ".docx")
        wordMLPackage.save(tempFile)

        return tempFile

    }

    /**
     *
     * @param shipmentInstance
     * @return
     */
    File generateLetterAsDocx(Shipment shipmentInstance) {
        // Save document to temporary file
        WordprocessingMLPackage wordMLPackage = generateLetter(shipmentInstance)
        File tempFile = File.createTempFile(shipmentInstance?.name + " - Certificate of Donation", ".docx")
        wordMLPackage.save(tempFile)
        return tempFile
    }

    /**
     *
     * @param shipmentInstance
     * @return
     */
    void generateLetterAsPdf(Shipment shipmentInstance, OutputStream outputStream) {
        WordprocessingMLPackage wordMLPackage = generateLetter(shipmentInstance)
        convertToPdf(wordMLPackage, outputStream)
    }


    /**
     * Generate the 'Certificate of Donation' letter from a template.
     *
     * @param shipmentInstance
     * @return
     */
    WordprocessingMLPackage generateLetter(Shipment shipmentInstance) {

        //File template = findFile("templates/cod-pl-template.docx")
        File template = findFile("templates/shipping/Haiti.COD&PL.docx")
        if (!template) {
            throw new FileNotFoundException("templates/shipping/Haiti.COD&PL.docx")
        }

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(template)

        // 2. Fetch the document part
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart()

        Document wmlDocumentEl = (Document) documentPart.getJaxbElement()

        // Get document as XML string
        def xml = XmlUtils.marshaltoString(wmlDocumentEl, true)

        def dataMappings = getDataMappings(shipmentInstance)


        log.info("mappings: " + dataMappings)
        log.debug("XML before: " + xml)
        Object obj = XmlUtils.unmarshallFromTemplate(xml, dataMappings)
        log.debug("XML after: " + xml)

        //change  JaxbElement
        documentPart.setJaxbElement((Document) obj)

        // Create a new table for the Packing List
        Tbl table = createPackingListTable(wordMLPackage, shipmentInstance)

        // Add table to document
        //wordMLPackage.getMainDocumentPart().addObject(table);

        insertTable(wordMLPackage, "{{PACKING_LIST}}", table)

        def dataMappingsTable = []
        dataMappings.each { key, value ->
            def rowMap = [:]
            rowMap.put("propertyName", key)
            rowMap.put("propertyValue", value)
            dataMappingsTable.add(rowMap)
        }
        Tbl debugTable = createTable(wordMLPackage, dataMappingsTable)
        insertTable(wordMLPackage, "{{VARIABLES}}", debugTable)


//        Body b = wordMLPackage.getMainDocumentPart().getJaxbElement().getBody();
//        b.getContent().add(debugTable)
//        dataMappings.each { key, value ->
//            P paragraph = createParagraphOfText("${key} = ${value}", false)
//            b.getContent().add(paragraph)
//        }

        return wordMLPackage
    }

    def convertToPdf(WordprocessingMLPackage wordMLPackage) {
        wordMLPackage.setFontMapper(new IdentityPlusMapper())
        PdfConversion conversion = new Conversion(wordMLPackage)
        OutputStream os = new ByteArrayOutputStream()
        conversion.output(os)
    }


    Tbl createTable(WordprocessingMLPackage wordMLPackage, List<Map<String, Object>> data) {

        String[] columns = data[0].keySet()
        int cols = columns.length

        int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips()
        int cellWidthTwipsDefault = new Double(Math.floor((writableWidthTwips / cols))).intValue()

        //Map cellWidthTwipsRatio = [1: 0.75, 2: 2.5, 3: 1.0, 4: 1.0, 5: 0.5, 6: 0.5]

        //TblFactory.createTable(4, 4, cellWidthTwipsDefault);

        Tbl table = Context.getWmlObjectFactory().createTbl()
        TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid()
        table.setTblGrid(tblGrid)
        // Add required <w:gridCol w:w="4788"/>
        for (int i = 1; i <= cols; i++) {
            TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol()
            int cellWidthTwips = cellWidthTwipsDefault
            gridCol.setW(BigInteger.valueOf(cellWidthTwips))
            tblGrid.getGridCol().add(gridCol)
        }

        // Add table headers
        Tr thead = Context.getWmlObjectFactory().createTr()
        columns.each { columnName ->
            addTc(thead, columnName, true)
        }
        table.getContent().add(thead)

        // Add table rows
        for (Map row : data) {
            Tr tr = Context.getWmlObjectFactory().createTr()
            table.getContent().add(tr)

            // Add each table cell
            row.each { columnName, value ->
                addTc(tr, value, cellWidthTwipsDefault)
            }
        }

        log.debug "Table: " + XmlUtils.marshaltoString(table, true)


        return table
    }


    Tbl createPackingListTable(WordprocessingMLPackage wordMLPackage, Shipment shipment) {
        int cols = 5
        int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips()
        int cellWidthTwipsDefault = new Double(Math.floor((writableWidthTwips / cols))).intValue()

        Map cellWidthTwipsRatio = [1: 1.0, 2: 1.0, 3: 1.0, 4: 1.0, 5: 1.0]

        //TblFactory.createTable(4, 4, cellWidthTwipsDefault);

        Tbl table = Context.getWmlObjectFactory().createTbl()
        addBorders(table)

        TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid()
        table.setTblGrid(tblGrid)

        // Add required <w:gridCol w:w="4788"/>
        for (int i = 1; i <= cols; i++) {
            TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol()
            int cellWidthTwips = (cellWidthTwipsDefault * cellWidthTwipsRatio[i]).intValue()
            gridCol.setW(BigInteger.valueOf(cellWidthTwips))
            tblGrid.getGridCol().add(gridCol)
        }

        Tr thead = Context.getWmlObjectFactory().createTr()
        addTc(thead, "Box", true)
        addTc(thead, "Product", true)
        addTc(thead, "Lot", true)
        addTc(thead, "Exp", true)
        addTc(thead, "Qty", true)
        table.getContent().add(thead)

        def previousContainer = null
        def shipmentItems = shipment?.shipmentItems?.sort { it?.container?.sortOrder }

        // Iterate over shipment items and add them to the table
        for (ShipmentItem shipmentItem : shipmentItems) {
            Tr tr = Context.getWmlObjectFactory().createTr()
            table.getContent().add(tr)

            if (shipmentItem?.container != previousContainer) {
                addTc(tr, shipmentItem?.container?.name?.replaceAll("\n", "") ?: "None", cellWidthTwipsDefault)
            } else {
                addTc(tr, "", cellWidthTwipsDefault)
            }
            addTc(tr, shipmentItem.inventoryItem?.product?.name ?: "", cellWidthTwipsDefault)
            addTc(tr, shipmentItem?.inventoryItem?.lotNumber ?: "", cellWidthTwipsDefault)
            addTc(tr, shipmentItem?.inventoryItem?.expirationDate?.format("MM-dd-yyyy") ?: "", cellWidthTwipsDefault)
            addTc(tr, "${shipmentItem?.quantity} ${shipmentItem?.inventoryItem?.product?.unitOfMeasure ?: ''}", cellWidthTwipsDefault)
            previousContainer = shipmentItem?.container
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

    def addTc(Tr tr, Object value, int cellWidthTwips) {
        Tc tc = Context.getWmlObjectFactory().createTc()
        tr.getContent().add(tc)

        TcPr tcPr = Context.getWmlObjectFactory().createTcPr()
        tc.setTcPr(tcPr)
        // <w:tcW w:w="4788" w:type="dxa"/>
        TblWidth cellWidth = Context.getWmlObjectFactory().createTblWidth()
        tcPr.setTcW(cellWidth)
        cellWidth.setType("auto")
        cellWidth.setW(BigInteger.valueOf(cellWidthTwips))

        // Cell content - an empty <w:p/>
        value = value.toString().replace("\n", "")
        P paragraph = createParagraphOfText(value.toString(), false)
        tc.getContent().add(paragraph)

        //log.info "Add TC: " + XmlUtils.marshaltoString(tc, true)


        return tc
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

        // Add all shipment properties to mappings
//        new DefaultGrailsDomainClass(Shipment.class).persistentProperties.each { property ->
//            log.info "property " + property.name + " = " + property.naturalName + " " + property.fieldName
//            mappings.put(property.fieldName, shipmentInstance.properties[property.name]);
//        }

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

        // Causes freeze when opening document
        //addObjectProperties(mappings, "CURRENT_USER", AuthService.currentUser.get(), User.class)
        //addObjectProperties(mappings, "CURRENT_LOCATION", AuthService.currentLocation.get(), Location.class)

        // Add all reference numbers
        shipmentInstance.referenceNumbers.each { ReferenceNumber referenceNumber ->
            log.info "Reference number ${referenceNumber?.referenceNumberType} = " + referenceNumber?.identifier
            FormatTagLib formatTag = grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
            String referenceNumberType = formatTag.metadata(obj: referenceNumber.referenceNumberType)
            referenceNumberType = referenceNumberType.toUpperCase().replaceAll(" ", "_")
            //String referenceNumberType = referenceNumber?.referenceNumberType?.name?.toUpperCase()
            mappings.put(referenceNumberType, referenceNumber?.identifier)
        }

        // Add additional properties generated
        def decimalFormatter = new DecimalFormat("\$###,##0.00")
        String totalValue = decimalFormatter.format(shipmentInstance?.calculateTotalValue() ?: 0.0)
        mappings.put("STATUS", shipmentInstance?.getStatus())
        mappings.put("TOTAL_VALUE", totalValue)
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
     * @param wordMLPackage
     * @param filePath
     */
    void savePackageToFile(WordprocessingMLPackage wordMLPackage, String filePath) {
        SaveToZipFile saver = new SaveToZipFile(wordMLPackage)
        saver.save(filePath)
        log.info("Saved output to:" + filePath)
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
     * @param shipmentInstance
     * @param cols
     * @param cellWidthTwips
     * @return
     */
    Tbl createPackingListTable(Shipment shipmentInstance, int cols, int cellWidthTwips) {

        Tbl tbl = Context.getWmlObjectFactory().createTbl()
        // w:tblPr
        // xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
        log.info("Namespace: " + Namespaces.W_NAMESPACE_DECLARATION)

        TblPr tblPr = null
        try {
            String strTblPr = "<w:tblPr " + Namespaces.W_NAMESPACE_DECLARATION + "><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblLook w:val=\"04A0\"/></w:tblPr>"
            tblPr = (TblPr) XmlUtils.unmarshalString(strTblPr)
        } catch (JAXBException e) {
            // Shouldn't happen
            e.printStackTrace()
        }
        tbl.setTblPr(tblPr)

        /*
        // <w:tblGrid><w:gridCol w:w="4788"/>
        TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid();
        tbl.setTblGrid(tblGrid);
        // Add required <w:gridCol w:w="4788"/>
        for (int i=1 ; i<=cols; i++) {
            TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol();
            gridCol.setW(BigInteger.valueOf(cellWidthTwips));
            tblGrid.getGridCol().add(gridCol);
        }

        // Now the rows
        for (int j=1 ; j<=rows; j++) {
            //shipmentInstance?.shipmentItems.each { item ->

            Tr tr = Context.getWmlObjectFactory().createTr();
            tbl.getEGContentRowContent().add(tr);
            createPackingListCell(tr, cellWidthTwips);


        }
        */
        return tbl
    }

    /**
     *
     * @param tr
     * @param cellWidthTwips
     */
    void createPackingListCell(Tr tr, int cellWidthTwips) {

        Tc tc = Context.getWmlObjectFactory().createTc()
        tr.getContent().add(tc)

        TcPr tcPr = Context.getWmlObjectFactory().createTcPr()
        tc.setTcPr(tcPr)

        // <w:tcW w:w="4788" w:type="dxa"/>
        TblWidth cellWidth = Context.getWmlObjectFactory().createTblWidth()
        tcPr.setTcW(cellWidth)
        cellWidth.setType("dxa")
        cellWidth.setW(BigInteger.valueOf(cellWidthTwips))

        // Cell content - an empty <w:p/>
        P paragraph = Context.getWmlObjectFactory().createP()
        //R run = Context.getWmlObjectFactory().createR();
        //Text text = Context.getWmlObjectFactory().createText();
        //text.setValue("testing");
        //run.getRunContent().add(text)
        //paragraph.getParagraphContent().add(run);
        tc.getContent().add(paragraph)

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

        int writableWidthTwips = wmlPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips()
        log.info "writableWidthTwips: " + writableWidthTwips
        cellWidthTwips = new Double(Math.floor((writableWidthTwips / cols))).intValue()
        log.info "cellWidthTwips: " + cellWidthTwips

        Tbl tbl = TblFactory.createTable(cols, cols, cellWidthTwips)

        //Tbl tbl = Context.getWmlObjectFactory().createTbl();
        //TblBorders tblBorders = Context.getWmlObjectFactory().createTblBorders();

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

        for (int i = 1; i <= cols; i++) {
            TblGridCol gridCol = Context.getWmlObjectFactory().createTblGridCol()
            gridCol.setW(BigInteger.valueOf(cellWidthTwips))
            tblGrid.getGridCol().add(gridCol)
        }

        // Create a repeating header
        Tr trHeader = Context.getWmlObjectFactory().createTr()
        tbl.getContent().add(trHeader)
        BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue()

        TrPr trPr = Context.getWmlObjectFactory().createTrPr()
        trHeader.setTrPr(trPr)


        //TrPr trPr = trHeader.getTrPr();
        trPr.getCnfStyleOrDivIdOrGridBefore().add(Context.getWmlObjectFactory().createCTTrPrBaseTblHeader(bdt))
        addTc(wmlPackage, trHeader, "Pallet/Box #", true)
        addTc(wmlPackage, trHeader, "Item", true)
        addTc(wmlPackage, trHeader, "Qty", true)


        def previousContainer = null
        def shipmentItems = shipmentInstance?.shipmentItems?.sort { it?.container?.sortOrder }
        // Iterate over shipment items and add them to the table
        shipmentItems?.each { itemInstance ->

            log.info "previous: " + previousContainer + ", current: " + itemInstance?.container + ", same: " + (itemInstance?.container == previousContainer)
            Tr tr = Context.getWmlObjectFactory().createTr()
            tbl.getContent().add(tr)
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
    void addTc(Tr tr, String text, boolean applyBold) {
        Tc tc = Context.getWmlObjectFactory().createTc()
        // wmlPackage.getMainDocumentPart().createParagraphOfText(text)
        tc.getContent().add(createParagraphOfText(text, applyBold))
        tr.getContent().add(tc)
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
        run.getContent().add(t)
        //run.setRPr(Context.getWmlObjectFactory().createRPr())
        //run.getRPr().setB(true);
        // Set bold property
        if (applyBold) {
            RPr rpr = Context.getWmlObjectFactory().createRPr()
            BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue()
            rpr.setB(bdt)
            run.setRPr(rpr)
        }
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
        //((Conversion)conversion).setSaveFO(new File(inputfilepath + ".fo"));
        //OutputStream outputStream = new FileOutputStream(inputfilepath + ".pdf");
        conversion.output(outputStream)
        //return outputStream;
    }


}
