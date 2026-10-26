package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import grails.core.GrailsApplication
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.springframework.context.ApplicationContext
import spock.lang.Ignore
import spock.lang.Specification

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemSortByCode
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType

/**
 * The spreadsheets and images the document service produces, read back with POI and ImageIO. Labels come from a
 * message tag lib that echoes the code, so the headers show which label sits in which column.
 */
class DocumentServiceSpec extends Specification implements ServiceUnitTest<DocumentService>, DataTest {

    void setupSpec() {
        mockDomains(Document, DocumentType, Shipment, ShipmentItem, ShipmentType, Container, ReferenceNumber,
                ReferenceNumberType, InventoryItem, Product, Category, Person, Location, Order, OrderItem, OrderType,
                ProductSupplier, Organization, Requisition, RequisitionItem, ProductPackage, UnitOfMeasure, Event, EventType)
    }

    void setup() {
        GroovyMock(LocalizationUtil, global: true)
        LocalizationUtil.currentLocale >> Locale.ENGLISH
        LocalizationUtil.localizationService >> Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        Object messageTagLib = new EchoMessageTagLib()
        Object formatTagLib = new EchoFormatTagLib()
        service.grailsApplication = Stub(GrailsApplication) {
            getMainContext() >> Stub(ApplicationContext) {
                getBean('org.pih.warehouse.MessageTagLib') >> messageTagLib
                getBean('org.pih.warehouse.FormatTagLib') >> formatTagLib
            }
        }
    }

    private static Sheet firstSheet(ByteArrayOutputStream outputStream) {
        return new HSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray())).getSheetAt(0)
    }

    private static List<Object> values(Row row) {
        if (!row) {
            return []
        }
        return (0..<Math.max(row.lastCellNum, 0)).collect { int index ->
            Cell cell = row.getCell(index)
            if (cell == null) {
                return null
            }
            switch (cell.cellType) {
                case Cell.CELL_TYPE_NUMERIC: return cell.numericCellValue
                case Cell.CELL_TYPE_BOOLEAN: return cell.booleanCellValue
                default: return cell.stringCellValue
            }
        }
    }

    /** All rows of the sheet as lists of cell values, empty rows included. */
    private static List<List<Object>> rows(Sheet sheet) {
        return (0..sheet.lastRowNum).collect { values(sheet.getRow(it)) }
    }

    private static Product product(String code, String name) {
        return new Product(productCode: code, name: name, unitOfMeasure: 'EA', synonyms: [])
    }

    private static InventoryItem item(Product product, String lotNumber, Date expirationDate = null) {
        return new InventoryItem(product: product, lotNumber: lotNumber, expirationDate: expirationDate)
    }

    /** One unpacked item, one item on a pallet and one item in a box on that pallet. */
    private static Shipment packedShipment() {
        Container pallet = new Container(name: 'Pallet 1', sortOrder: 1)
        Container box = new Container(name: 'Box A', sortOrder: 0, parentContainer: pallet)
        Product aspirin = product('AS1', 'Aspirin')
        Product bandage = product('BA1', 'Bandage')
        Product cotton = product('CO1', 'Cotton')
        Shipment shipment = new Shipment(name: 'To clinic', shipmentNumber: 'S-1',
                origin: new Location(name: 'Depot'), destination: new Location(name: 'Clinic'),
                shipmentType: new ShipmentType(name: 'Air'), expectedShippingDate: new Date(), additionalInformation: 'Fragile')
        // a persisted item always has a (possibly empty) set of order items
        shipment.addToShipmentItems(new ShipmentItem(product: aspirin, inventoryItem: item(aspirin, 'L-1'), quantity: 5,
                recipient: new Person(firstName: 'Ann', lastName: 'Lee', email: 'ann@example.com'), orderItems: [] as Set))
        shipment.addToShipmentItems(new ShipmentItem(product: bandage, inventoryItem: item(bandage, 'L-2', new Date()),
                quantity: 7, container: pallet, orderItems: [] as Set))
        shipment.addToShipmentItems(new ShipmentItem(product: cotton, inventoryItem: item(cotton, 'L-3'), quantity: 9,
                container: box, orderItems: [] as Set))
        return shipment
    }

    void 'generateExcel writes the keys of the first row as header and stringifies everything but numbers and booleans'() {
        given:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        List<Map> data = [
                [name: 'Aspirin', quantity: 5, active: true, expires: null],
                [name: 'Gauze', quantity: 2.5, active: false, expires: new Location(name: 'Not a date')],
        ]

        when:
        service.generateExcel(outputStream, data)
        List<List<Object>> sheetRows = rows(firstSheet(outputStream))

        then:
        sheetRows[0] == ['name', 'quantity', 'active', 'expires']
        sheetRows[1] == ['Aspirin', 5d, true, '']
        sheetRows[2] == ['Gauze', 2.5d, false, 'Not a date']
    }

    void 'generateInventoryTemplate writes expiration dates as dates and other values as text or numbers'() {
        given:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        List<Map> data = [
                ['Product code': 'AS1', 'Expiration date': '03/15/2030', 'Quantity': 4],
                ['Product code': 'BA1', 'Expiration date': '', 'Quantity': null],
        ]

        when:
        service.generateInventoryTemplate(outputStream, data)
        Sheet sheet = firstSheet(outputStream)
        Cell expirationDate = sheet.getRow(1).getCell(1)

        then:
        values(sheet.getRow(0)) == ['Product code', 'Expiration date', 'Quantity']
        expirationDate.dateCellValue.format('yyyy-MM-dd') == '2030-03-15'
        expirationDate.cellStyle.dataFormatString == 'MM/dd/yyyy'
        sheet.getRow(1).getCell(2).numericCellValue == 4d
        values(sheet.getRow(2)) == ['BA1', '', '']
    }

    void 'generatePartialPackingList lists every item with its pallet and box'() {
        given:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        when:
        boolean generated = service.generatePartialPackingList(outputStream, packedShipment())
        List<List<Object>> sheetRows = rows(firstSheet(outputStream))

        then:
        generated
        sheetRows[0] == ['[packLevel1.label]', '[packLevel2.label]', '[product.productCode.label]', '[product.label]',
                         '[inventory.lotNumber.label]', '[inventoryItem.expires.label]', '[default.qty.label]',
                         '[default.units.label]', '[shipping.recipient.label]']
        sheetRows.size() == 4

        and: 'the unpacked item has no containers'
        sheetRows[1][0..4] == ['', '', 'AS1', 'Aspirin', 'L-1']
        sheetRows[1][6..8] == [5d, '[default.each.label]', 'ann@example.com']

        and: 'the pallet item names its pallet only and the boxed item names both'
        sheetRows[2][0..2] == ['Pallet 1', '', 'BA1']
        sheetRows[2][6] == 7d
        sheetRows[3][0..2] == ['Pallet 1', 'Box A', 'CO1']
        sheetRows[3][6] == 9d
    }

    void 'generatePackingList writes the shipment details and the items grouped by pack level'() {
        given:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        Shipment shipment = packedShipment()
        shipment.addToReferenceNumbers(new ReferenceNumber(identifier: 'TRACK-9',
                referenceNumberType: new ReferenceNumberType(name: 'Tracking number')))

        when:
        service.generatePackingList(outputStream, shipment)
        List<List<Object>> sheetRows = rows(firstSheet(outputStream))
        List<Object> header = sheetRows.find { it && it[0] == '[packLevel1.label]' }
        int headerIndex = sheetRows.indexOf(header)

        then: 'the details block'
        sheetRows[0] == ['[shipping.name.label]', 'To clinic']
        sheetRows[1] == ['[shipping.shipmentNumber.label]', 'S-1']
        sheetRows[2] == ['[shipping.shipmentType.label]', 'Air']
        sheetRows[3] == ['Tracking number', 'TRACK-9']
        sheetRows.find { it && it[0] == '[shipping.origin.label]' }[1] == 'Depot'
        sheetRows.find { it && it[0] == '[shipping.destination.label]' }[1] == 'Clinic'
        sheetRows.find { it && it[0] == '[shipping.actualShippingDate.label]' }[1] == '[default.notAvailable.label]'
        sheetRows.find { it && it[0] == '[shipping.actualDeliveryDate.label]' }[1] == '[default.notAvailable.label]'
        sheetRows.find { it && it[0] == '[default.comments.label]' }[1] == 'Fragile'

        and: 'no order number column for a shipment that is not from a purchase order'
        header[2] == '[packingList.productCode.label]'
        header.size() == 13

        and: 'the items'
        sheetRows[headerIndex + 1][0..3] == ['[shipping.unpacked.label]', '[shipping.unpacked.label]', 'AS1', 'Aspirin']
        sheetRows[headerIndex + 1][10] == 5d
        sheetRows[headerIndex + 1][12] == 'Ann Lee'
        sheetRows[headerIndex + 2][0..2] == ['Pallet 1', '[shipping.unpacked.label]', 'BA1']
        sheetRows[headerIndex + 3][0..2] == ['', 'Box A', 'CO1']
    }

    void 'generatePackingList adds the order number and the supplier details of items from a purchase order'() {
        given:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        Container pallet = new Container(name: 'Pallet 1', sortOrder: 1)
        Product aspirin = product('AS1', 'Aspirin')
        Order order = new Order(orderNumber: 'PO-7', orderType: new OrderType(code: OrderTypeCode.PURCHASE_ORDER.name(),
                orderTypeCode: OrderTypeCode.PURCHASE_ORDER))
        ProductSupplier supplier = new ProductSupplier(name: 'Aspirin 100', supplierCode: 'SUP-1', manufacturerCode: 'M-1',
                manufacturer: new Organization(name: 'Acme'))
        OrderItem orderItem = new OrderItem(order: order, product: aspirin, productSupplier: supplier)
        Shipment shipment = new Shipment(name: 'Inbound', shipmentNumber: 'S-2', shipmentType: new ShipmentType(name: 'Sea'),
                expectedShippingDate: new Date())
        shipment.addToEvents(new Event(eventDate: new Date() - 2, eventType: new EventType(eventCode: EventCode.SHIPPED)))
        shipment.addToEvents(new Event(eventDate: new Date() - 1, eventType: new EventType(eventCode: EventCode.RECEIVED)))
        [3, 4].each { int quantity ->
            ShipmentItem shipmentItem = new ShipmentItem(product: aspirin, inventoryItem: item(aspirin, "L-${quantity}"),
                    quantity: quantity, container: pallet)
            shipmentItem.addToOrderItems(orderItem)
            shipment.addToShipmentItems(shipmentItem)
        }

        when:
        service.generatePackingList(outputStream, shipment)
        Sheet sheet = firstSheet(outputStream)
        List<List<Object>> sheetRows = rows(sheet)
        int headerIndex = sheetRows.findIndexOf { it && it[0] == '[packLevel1.label]' }

        then:
        sheetRows[headerIndex][2] == '[order.orderNumber.label]'
        sheetRows[headerIndex + 1][0..7] == ['Pallet 1', '[shipping.unpacked.label]', 'PO-7', 'AS1', 'Aspirin', 'SUP-1',
                                            'Aspirin 100', 'Acme']
        sheetRows[headerIndex + 1][8] == 'M-1'
        sheetRows[headerIndex + 2][0..1] == ['', '']

        and: 'the shipped and received events give real dates'
        sheet.getRow(sheetRows.findIndexOf { it && it[0] == '[shipping.actualShippingDate.label]' }).getCell(1)
                .dateCellValue.format('yyyy-MM-dd') == (new Date() - 2).format('yyyy-MM-dd')
        sheet.getRow(sheetRows.findIndexOf { it && it[0] == '[shipping.actualDeliveryDate.label]' }).getCell(1)
                .dateCellValue.format('yyyy-MM-dd') == (new Date() - 1).format('yyyy-MM-dd')

        and: 'the pallet and box cells of the last item are merged with the first one'
        sheet.mergedRegions*.formatAsString().containsAll(
                ["A${headerIndex + 2}:A${headerIndex + 3}".toString(), "B${headerIndex + 2}:B${headerIndex + 3}".toString()])
    }

    private static byte[] png(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ImageIO.write(image, 'png', outputStream)
        return outputStream.toByteArray()
    }

    void 'scaleImage shrinks an image to fit the box and keeps its aspect ratio'() {
        given:
        Document document = new Document(filename: 'photo.png', contentType: 'image/png', fileContents: png(400, 200))
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        when:
        service.scaleImage(document, outputStream, 100, 100)
        BufferedImage scaled = ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()))

        then:
        scaled.width == 100
        scaled.height == 50
    }

    void 'scaleImage leaves an image that already fits unchanged'() {
        given:
        Document document = new Document(filename: 'icon', extension: 'PNG', contentType: 'image/png',
                fileContents: png(40, 30))
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        when:
        service.scaleImage(document, outputStream, 100, 100)
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()))

        then:
        image.width == 40
        image.height == 30
    }

    void 'scaleImage writes nothing for contents that are not an image'() {
        given:
        Document document = new Document(filename: 'notes.png', fileContents: 'not an image'.bytes)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        when:
        service.scaleImage(document, outputStream, 100, 100)

        then:
        noExceptionThrown()
        outputStream.size() == 0
    }

    void 'findFile resolves a path under the configuration directory when the app is not deployed as a war'() {
        expect:
        service.findFile('templates/receive-shipment-template.docx').path ==
                "grails-app${File.separator}conf${File.separator}templates/receive-shipment-template.docx"
        service.findFile('templates/receive-shipment-template.docx').exists()
    }

    void 'generateChecklistAsDocx fills the receipt template into a new word document'() {
        when:
        File file = service.generateChecklistAsDocx()
        WordprocessingMLPackage document = WordprocessingMLPackage.load(file)

        then:
        file.name.startsWith('Checklist')
        file.name.endsWith('.docx')
        document.mainDocumentPart.content

        cleanup:
        file?.delete()
    }

    void 'getDocuments filters by name and type and pages the result'() {
        given:
        DocumentType invoice = new DocumentType(name: 'Invoice').save(validate: false)
        DocumentType label = new DocumentType(name: 'Label').save(validate: false)
        new Document(name: 'Invoice March', documentType: invoice).save(validate: false)
        new Document(name: 'Invoice April', documentType: invoice).save(validate: false)
        new Document(name: 'Shipping label', documentType: label).save(validate: false)

        expect:
        service.getDocuments(new DocumentFilterCommand(q: 'invoice', sort: 'name', max: 10, offset: 0))*.name ==
                ['Invoice April', 'Invoice March']
        service.getDocuments(new DocumentFilterCommand(documentType: label, max: 10, offset: 0))*.name == ['Shipping label']
        service.getDocuments(new DocumentFilterCommand(sort: 'name', order: 'desc', max: 1, offset: 0))*.name ==
                ['Shipping label']
        service.getDocuments(new DocumentFilterCommand(max: 1, offset: 0)).totalCount == 3
    }

    void 'getNonTemplateDocumentTypes leaves out the template and data export types and sorts by name'() {
        given:
        new DocumentType(name: 'Zebra template', documentCode: DocumentCode.ZEBRA_TEMPLATE).save(validate: false)
        new DocumentType(name: 'Data export', documentCode: DocumentCode.DATA_EXPORT).save(validate: false)
        new DocumentType(name: 'Invoice', documentCode: DocumentCode.SHIPPING_DOCUMENT).save(validate: false)
        new DocumentType(name: 'Another', documentCode: null).save(validate: false)

        expect:
        service.nonTemplateDocumentTypes*.name == ['Another', 'Invoice']
    }
}

/** Stands in for the message tag lib: renders the message code in brackets. */
class EchoMessageTagLib {
    String message(Map attrs) {
        return "[${attrs.code}]"
    }
}

/** Stands in for the format tag lib: renders the name of the metadata object. */
class EchoFormatTagLib {
    String metadata(Map attrs) {
        return attrs.obj?.name
    }
}
