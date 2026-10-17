package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import grails.core.GrailsApplication
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.docx4j.XmlUtils
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.wml.Body
import org.docx4j.wml.P
import org.docx4j.wml.R
import org.docx4j.wml.Tbl
import org.docx4j.wml.Tc
import org.docx4j.wml.Text
import org.docx4j.wml.Tr
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.springframework.context.ApplicationContext
import spock.lang.Specification
import javax.xml.bind.JAXBElement
import java.text.SimpleDateFormat

import org.pih.warehouse.FormatTagLib
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAttribute
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Shipments are built in memory (never saved) since the service only reads them. The default locale is pinned to
 * US English because the service formats numbers and dates with the JVM default locale.
 */
class FileServiceSpec extends Specification implements ServiceUnitTest<FileService>, DataTest {

    static final Date EXPIRATION_DATE = new GregorianCalendar(2027, Calendar.MARCH, 5).time
    static final Date SHIPPING_DATE = new GregorianCalendar(2026, Calendar.JULY, 4).time

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File tempDir

    Locale originalLocale
    Locale originalFormatLocale
    Locale originalDisplayLocale
    List<File> renderedFiles = []

    void setupSpec() {
        mockDomains(Shipment, ShipmentItem, Container, Product, ProductAttribute, Attribute, InventoryItem,
                Location, LocationGroup, Address, Person, ReferenceNumber, ReferenceNumberType,
                Order, OrderItem, ProductSupplier, Organization, Document, Synonym)
    }

    void setup() {
        originalLocale = Locale.getDefault()
        originalFormatLocale = Locale.getDefault(Locale.Category.FORMAT)
        originalDisplayLocale = Locale.getDefault(Locale.Category.DISPLAY)
        Locale.setDefault(Locale.US)

        tempDir = temporaryFolder.root

        // Product display names are looked up for the current locale of the localization service
        LocalizationService localizationStub = Stub(LocalizationService) {
            getCurrentLocale() >> Locale.US
        }
        defineBeans {
            localizationService(InstanceFactoryBean, localizationStub, LocalizationService)
        }
        service.grailsDomainClassMappingContext = Shipment.gormPersistentEntity.mappingContext
    }

    void cleanup() {
        Locale.setDefault(originalLocale)
        Locale.setDefault(Locale.Category.FORMAT, originalFormatLocale)
        Locale.setDefault(Locale.Category.DISPLAY, originalDisplayLocale)
        renderedFiles*.delete()
    }

    void 'createDirectory should create missing parent directories'() {
        given:
        File expected = new File(tempDir, 'a/b/c')

        when:
        File folder = service.createDirectory(expected.path)

        then:
        folder == expected
        folder.isDirectory()
    }

    void 'createDirectory should return an existing directory untouched'() {
        given:
        File existing = new File(tempDir, 'existing')
        existing.mkdir()
        File marker = new File(existing, 'marker.txt')
        marker.text = 'keep'

        when:
        File folder = service.createDirectory(existing.path)

        then:
        folder == existing
        marker.text == 'keep'
    }

    void 'createDirectory should return the folder even when it cannot be created'() {
        given: 'a path below a regular file'
        File file = new File(tempDir, 'plain.txt')
        file.text = 'not a directory'

        when:
        File folder = service.createDirectory(new File(file, 'child').path)

        then:
        folder.path == new File(file, 'child').path
        !folder.exists()
    }

    void 'getPackingList should list the items in pack order with both pack levels'() {
        given:
        Container pallet = new Container(name: 'Pallet 1', sortOrder: 1)
        Container box = new Container(name: 'Box 1', parentContainer: pallet, sortOrder: 1)
        Container looseBox = new Container(name: 'Box 2', sortOrder: 2)
        Shipment shipment = new Shipment(name: 'Shipment A')
        shipment.shipmentItems = [
                shipmentItem(product('P2', 'Paracetamol', 'EA'), 'LOT-2', null, 5, looseBox),
                shipmentItem(product('P1', 'Aspirin', 'BX'), 'LOT-1', EXPIRATION_DATE, 10, box),
        ] as Set

        when:
        Map packingList = service.getPackingList(shipment)

        then:
        packingList.columns.keySet() as List == ['parentContainer', 'childContainer', 'productName', 'lotNumber', 'expirationDate', 'quantity']
        packingList.columns.productName == [label: 'Product', ratio: 2.0]

        and: 'a nested container is listed under its parent; items without a parent container sort first'
        packingList.data == [
                [parentContainer: 'Box 2', childContainer: null, productName: 'Paracetamol', lotNumber: 'LOT-2',
                 expirationDate: '', quantity: '5 EA'],
                [parentContainer: 'Pallet 1', childContainer: 'Box 1', productName: 'Aspirin', lotNumber: 'LOT-1',
                 expirationDate: '05-Mar-2027', quantity: '10 BX'],
        ]
    }

    void 'getPackingList should drop the pack level columns when nothing is packed'() {
        given:
        Product aspirin = product('P1', 'Aspirin', null)
        aspirin.addToSynonyms(new Synonym(synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.US, name: 'Aspirin 100mg'))
        aspirin.addToSynonyms(new Synonym(synonymTypeCode: SynonymTypeCode.DISPLAY_NAME, locale: Locale.FRENCH, name: 'Aspirine'))
        Shipment shipment = new Shipment(name: 'Shipment A')
        shipment.shipmentItems = [shipmentItem(aspirin, null, null, 3, null)] as Set

        when:
        Map packingList = service.getPackingList(shipment)

        then:
        packingList.columns.keySet() as List == ['productName', 'lotNumber', 'expirationDate', 'quantity']
        packingList.data == [[parentContainer: null, childContainer: null, productName: 'Aspirin 100mg', lotNumber: '',
                              expirationDate: '', quantity: '3 ']]
    }

    void 'getCommercialInvoice should price each line, sort by product code and append a total'() {
        given:
        Attribute hsCode = new Attribute(code: Constants.HS_CODE_PRODUCT_ATTRIBUTE_CODE, name: 'HS Code').save(validate: false)
        Product aspirin = product('P1', 'Aspirin', 'BX')
        aspirin.pricePerUnit = 2.5
        aspirin.addToAttributes(new ProductAttribute(attribute: hsCode, value: '3004.90'))
        Product paracetamol = product('P2', 'Paracetamol', 'EA')
        paracetamol.attributes = []

        and: 'the manufacturer comes from the order line the item was shipped for'
        Organization maker = new Organization(name: 'Maker Inc')
        ShipmentItem aspirinItem = shipmentItem(aspirin, 'LOT-1', EXPIRATION_DATE, 1200, null)
        aspirinItem.orderItems = [new OrderItem(productSupplier: new ProductSupplier(manufacturer: maker))] as Set
        ShipmentItem paracetamolItem = shipmentItem(paracetamol, null, null, 7, null)

        Shipment shipment = new Shipment(name: 'Shipment A')
        shipment.shipmentItems = [paracetamolItem, aspirinItem] as Set

        when:
        Map invoice = service.getCommercialInvoice(shipment)

        then:
        invoice.columns.keySet() as List == ['hsCode', 'productCode', 'productName', 'manufacturerName', 'unitOfMeasure',
                                            'lotNumber', 'expirationDate', 'quantity', 'unitCost', 'totalCost']
        invoice.columns.unitCost.label == 'Unit Cost (USD)'

        and: 'a product without a price is listed at zero cost'
        invoice.data == [
                [hsCode: '3004.90', productCode: 'P1', productName: 'Aspirin', manufacturerName: 'Maker Inc',
                 unitOfMeasure: 'BX', lotNumber: 'LOT-1', expirationDate: '05-Mar-2027', quantity: '1,200',
                 unitCost: '2.50', totalCost: '3,000.00'],
                [hsCode: '', productCode: 'P2', productName: 'Paracetamol', manufacturerName: '',
                 unitOfMeasure: 'EA', lotNumber: '', expirationDate: '', quantity: '7',
                 unitCost: '0.00', totalCost: '0.00'],
                [hsCode: null, productCode: null, productName: null, manufacturerName: null, unitOfMeasure: null,
                 lotNumber: null, expirationDate: null, quantity: null, unitCost: 'Total', totalCost: '3,000.00'],
        ]
    }

    void 'createTable should build a bordered table with a repeating bold header and one row per data entry'() {
        given:
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage()
        int writableWidth = wordPackage.documentModel.sections[0].pageDimensions.writableWidthTwips
        int defaultWidth = Math.floor(writableWidth / 2) as int
        Map columns = [name: [label: 'Name', ratio: 1.0], value: [label: 'Value', ratio: 0.5]]
        List data = [[name: 'A', value: '1'], [name: 'B', value: null]]

        when:
        Tbl table = service.createTable(wordPackage, columns, data)

        then:
        table.tblPr.tblBorders.insideH.val.value() == 'single'
        table.tblPr.tblBorders.top.sz == 4G

        and:
        table.tblGrid.gridCol*.w == [BigInteger.valueOf(defaultWidth), BigInteger.valueOf((0.5 * defaultWidth) as int)]

        and:
        List<Tr> rows = table.content as List<Tr>
        rows.size() == 3
        cellTexts(rows[0]) == ['Name', 'Value']
        cellTexts(rows[1]) == ['A', '1']
        cellTexts(rows[2]) == ['B', null]

        and: 'the header is bold and repeats on each page, the data rows are not bold'
        runOf(rows[0].content[0] as Tc).rPr.b.val
        runOf(rows[0].content[0] as Tc).rPr.rFonts.ascii == 'Book Antiqua'
        runOf(rows[0].content[0] as Tc).rPr.sz.val == 20G
        runOf(rows[1].content[0] as Tc).rPr.b == null
        (rows[0].trPr.cnfStyleOrDivIdOrGridBefore[0] as JAXBElement).name.localPart == 'tblHeader'
    }

    void 'addTc without font settings should add a cell with plain run properties, bold: #applyBold'() {
        given:
        Tr row = new Tr()

        when:
        service.addTc(row, 'Plain', applyBold)

        then:
        row.content.size() == 1
        R run = runOf(row.content[0] as Tc)
        (run.content[0] as Text).value == 'Plain'
        run.rPr?.rFonts == null
        run.rPr?.sz == null
        (run.rPr?.b?.val ?: false) == applyBold

        where:
        applyBold << [true, false]
    }

    void 'insertTable should replace the paragraphs starting with the placeholder'() {
        given:
        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage()
        wordPackage.mainDocumentPart.addParagraphOfText('Intro')
        wordPackage.mainDocumentPart.addParagraphOfText('{{PACKING_LIST}} goes here')
        wordPackage.mainDocumentPart.addParagraphOfText('Outro')
        Tbl table = new Tbl()

        when:
        service.insertTable(wordPackage, '{{PACKING_LIST}}', table)
        service.insertTable(wordPackage, '{{MISSING}}', new Tbl())

        then:
        List content = body(wordPackage).content
        content.size() == 3
        service.getElementText(content[0]) == 'Intro'
        content[1].is(table)
        service.getElementText(content[2]) == 'Outro'
    }

    void 'getDataMappings should expose the shipment, its locations and the computed values'() {
        given:
        service.userService = Stub(UserService) { hasRoleFinance() >> true }
        Shipment shipment = mappedShipment()

        when:
        Date before = new Date()
        Map mappings = service.getDataMappings(shipment)
        Date after = new Date()

        then:
        SimpleDateFormat formatter = new SimpleDateFormat('MMMMM dd, yyyy')
        mappings.DATE == 'July 04, 2026'
        mappings.TODAY in [formatter.format(before), formatter.format(after)]

        and: 'plain properties are mapped under their prefix, associations are left out'
        mappings['SHIPMENT.NAME'] == 'Shipment A'
        mappings['SHIPMENT.DESCRIPTION'] == ''
        !mappings.containsKey('SHIPMENT.ORIGIN')
        mappings['ORIGIN.NAME'] == 'Boston'
        mappings['DESTINATION.NAME'] == 'Miami'
        mappings['DESTINATION.ADDRESS.CITY'] == 'Miami'
        !mappings.keySet().any { it.startsWith('ORIGIN.ADDRESS.') }
        !mappings.keySet().any { it.startsWith('CARRIER.') }

        and: 'logos are never exposed'
        !mappings.containsKey('ORIGIN.LOGO')
        !mappings.containsKey('DESTINATION.LOGO')

        and:
        mappings.TOTAL_VALUE == '$25.00'
        mappings.STATUS.code == ShipmentStatusCode.PENDING
        mappings.DRIVER_NAME == 'Joe'
        mappings.TRACKING_NUMBER == null
        mappings.ACTUAL_SHIPPING_DATE == null
    }

    void 'getDataMappings should map reference numbers by their type and pick the tracking number'() {
        given:
        service.userService = Stub(UserService) { hasRoleFinance() >> true }
        ReferenceNumberType trackingType = new ReferenceNumberType(name: 'Tracking number')
        trackingType.id = Constants.TRACKING_NUMBER_TYPE_ID
        trackingType.save(validate: false)
        ReferenceNumberType poType = new ReferenceNumberType(name: 'Purchase order')

        and: 'the format tag lib renders a reference number type as its name'
        FormatTagLib formatTagLib = new NameFormatTagLib()
        service.grailsApplication = grailsApplicationWithBeans(['org.pih.warehouse.FormatTagLib': formatTagLib])

        and:
        Shipment shipment = mappedShipment()
        shipment.referenceNumbers = [
                new ReferenceNumber(referenceNumberType: trackingType, identifier: '1Z999'),
                new ReferenceNumber(referenceNumberType: poType, identifier: 'PO-42'),
        ]

        when:
        Map mappings = service.getDataMappings(shipment)

        then:
        mappings.TRACKING_NUMBER == '1Z999'
        mappings.PURCHASE_ORDER == 'PO-42'
    }

    void 'getDataMappings should hide the total value from users without the finance role'() {
        given:
        service.userService = Stub(UserService) { hasRoleFinance() >> false }
        service.grailsApplication = grailsApplicationWithBeans(['org.grails.plugins.web.taglib.ApplicationTagLib': new Expando(
                message: { Map attrs -> attrs.code == 'access.accessDenied.label' ? 'Access denied' : attrs.code })])

        when:
        Map mappings = service.getDataMappings(mappedShipment())

        then:
        mappings.TOTAL_VALUE == 'Access denied'
        mappings.TOTALVALUE == 'Access denied'
    }

    void 'renderShippingTemplate should fill the placeholders and insert the tables into a copy of the template'() {
        given:
        service.userService = Stub(UserService) { hasRoleFinance() >> true }
        Shipment shipment = mappedShipment()

        and: 'a template with a variable and the three table placeholders'
        WordprocessingMLPackage template = WordprocessingMLPackage.createPackage()
        template.mainDocumentPart.addParagraphOfText('Shipment ${SHIPMENT.NAME} to ${DESTINATION.NAME}')
        template.mainDocumentPart.addParagraphOfText('{{PACKING_LIST}}')
        template.mainDocumentPart.addParagraphOfText('{{COMMERCIAL_INVOICE}}')
        template.mainDocumentPart.addParagraphOfText('{{VARIABLES}}')
        ByteArrayOutputStream bytes = new ByteArrayOutputStream()
        template.save(bytes)
        Document document = new Document(name: 'Packing slip', fileContents: bytes.toByteArray())

        when:
        File rendered = service.renderShippingTemplate(document, shipment)
        renderedFiles << rendered

        then:
        rendered.name.startsWith('Shipment A - Packing slip')
        rendered.name.endsWith('.docx')

        and: 'tables come back wrapped in JAXB elements when the document is read again'
        List content = body(WordprocessingMLPackage.load(rendered)).content.collect { XmlUtils.unwrap(it) }
        service.getElementText(content[0]) == 'Shipment Shipment A to Miami'
        content[1..3].every { it instanceof Tbl }

        and: 'the packing list has a header and the one item, the invoice also a total row'
        (content[1] as Tbl).content.size() == 2
        (content[2] as Tbl).content.size() == 3
        cellTexts((content[1] as Tbl).content[1] as Tr) == ['Aspirin', 'LOT-1', '05-Mar-2027', '10 BX']
    }

    private Shipment mappedShipment() {
        Location boston = new Location(name: 'Boston')
        boston.logo = [1, 2, 3] as byte[]
        Location miami = new Location(name: 'Miami', address: new Address(address: '2 Ocean Dr', city: 'Miami'))
        Product aspirin = product('P1', 'Aspirin', 'BX')
        aspirin.pricePerUnit = 2.5

        Shipment shipment = new Shipment(name: 'Shipment A', expectedShippingDate: SHIPPING_DATE, origin: boston,
                destination: miami, driverName: 'Joe')
        shipment.shipmentItems = [shipmentItem(aspirin, 'LOT-1', EXPIRATION_DATE, 10, null)] as Set
        return shipment
    }

    private GrailsApplication grailsApplicationWithBeans(Map<String, Object> beans) {
        ApplicationContext context = Stub(ApplicationContext) {
            getBean(_ as String) >> { String name -> beans[name] }
        }
        return Stub(GrailsApplication) {
            getMainContext() >> context
        }
    }

    private static Product product(String productCode, String name, String unitOfMeasure) {
        return new Product(productCode: productCode, name: name, unitOfMeasure: unitOfMeasure)
    }

    private static ShipmentItem shipmentItem(Product product, String lotNumber, Date expirationDate, Integer quantity,
                                             Container container) {
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: lotNumber, expirationDate: expirationDate)
        return new ShipmentItem(product: product, inventoryItem: inventoryItem, lotNumber: lotNumber, quantity: quantity,
                container: container)
    }

    private static Body body(WordprocessingMLPackage wordPackage) {
        return wordPackage.mainDocumentPart.jaxbElement.body
    }

    private static R runOf(Tc cell) {
        return XmlUtils.unwrap((XmlUtils.unwrap(cell.content[0]) as P).content[0]) as R
    }

    private static List<String> cellTexts(Tr row) {
        // A document read back from disk wraps cells and texts in JAXB elements, a freshly built one does not
        return row.content.collect { (XmlUtils.unwrap(runOf(XmlUtils.unwrap(it) as Tc).content[0]) as Text).value }
    }
}

/**
 * Renders the metadata of an object as its name, without the web request the real tag needs.
 */
class NameFormatTagLib extends FormatTagLib {

    def metadata(Map attrs) {
        return attrs.obj.name
    }
}
