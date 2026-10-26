package org.pih.warehouse.regression.platform.importer

import org.pih.warehouse.importer.*
import grails.testing.gorm.DataTest
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.grails.plugins.excelimport.ExcelImportService
import org.grails.plugins.excelimport.ExpectedPropertyType
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Field
import java.lang.reflect.Modifier

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.CycleCountImportService
import org.pih.warehouse.product.Product

/**
 * Every Excel importer reads the columns of its sheet into rows keyed by property name. The workbooks are built here
 * from each importer's own column map, so a column that is mapped to the wrong property shows up as a wrong value.
 */
class ExcelImportersSpec extends Specification implements DataTest {

    static final List<Class> SIMPLE_IMPORTERS = [
            CategoryExcelImporter, InventoryExcelImporter, InventoryLevelExcelImporter,
            LocationExcelImporter, OutboundStockMovementExcelImporter, PackingListExcelImporter, PersonExcelImporter,
            ProductAssociationExcelImporter, ProductAttributeExcelImporter, ProductCatalogExcelImporter,
            ProductCatalogItemExcelImporter, ProductExcelImporter, ProductGroupExcelImporter,
            ProductPackageExcelImporter, ProductSupplierAttributeExcelImporter, ProductSupplierExcelImporter,
            ProductSupplierPreferenceExcelImporter, ProductSynonymExcelImporter,
            PurchaseOrderActualReadyDateExcelImporter, PurchaseOrderExcelImporter, TagExcelImporter,
            UserExcelImporter, UserLocationExcelImporter,
    ]

    List<File> files = []

    void setupSpec() {
        mockDomains(Location, LocationType, Product, Person)
    }

    void cleanup() {
        files*.delete()
    }

    /** The services an importer looks up by field name, as mocks, plus the real Excel import service. */
    private void defineServicesOf(Class importer) {
        Map<String, Object> services = [excelImportService: new ExcelImportService()]
        importer.declaredFields.findAll { Field field ->
            !Modifier.isStatic(field.modifiers) && field.name.endsWith('Service') && field.type != ExcelImportService
        }.each { Field field -> services[field.name] = Mock(field.type) }
        defineBeans {
            services.each { String name, Object service ->
                "${name}"(InstanceFactoryBean, service, service.getClass())
            }
        }
    }

    private static Object staticMap(Class importer, String... names) {
        return names.collect { name -> importer.declaredFields.find { it.name == name } }.find()?.with {
            it.accessible = true
            it.get(null)
        }
    }

    /** Writes a header row and one row of values, one per mapped column, and returns the file name. */
    private String workbookFor(Map columnMap, Map<String, Object> valuesByProperty) {
        HSSFWorkbook workbook = new HSSFWorkbook()
        Sheet sheet = workbook.createSheet(columnMap.sheet as String)
        Row header = sheet.createRow(0)
        Row row = sheet.createRow(columnMap.startRow as int)
        (columnMap.columnMap as Map<String, String>).each { String column, String property ->
            int index = org.apache.poi.ss.util.CellReference.convertColStringToIndex(column)
            header.createCell(index).setCellValue(property)
            Object value = valuesByProperty[property]
            if (value instanceof Number) {
                row.createCell(index).setCellValue(value as double)
            } else if (value != null) {
                row.createCell(index).setCellValue(value as String)
            }
        }
        File file = File.createTempFile('importer', '.xls')
        files << file
        file.withOutputStream { workbook.write(it) }
        return file.absolutePath
    }

    @Unroll
    void '#importer.simpleName reads every mapped column into its property'() {
        given:
        defineServicesOf(importer)
        Map columnMap = staticMap(importer, 'columnMap', 'COLUMN_MAP') as Map
        Map propertyMap = staticMap(importer, 'propertyMap', 'PROPERTY_MAP') as Map
        Map<String, Object> values = propertyMap.collectEntries { String property, Map config ->
            switch (config.expectedType) {
                case ExpectedPropertyType.StringType: return [property, "v-${property}".toString()]
                case ExpectedPropertyType.IntType: return [property, 7]
                case ExpectedPropertyType.DoubleType: return [property, 2.5]
                default: return [property, null]
            }
        }

        when:
        List<Map> rows = importer.newInstance(workbookFor(columnMap, values)).data

        then:
        rows.size() == 1
        values.findAll { it.value != null }.every { String property, Object value ->
            rows[0][property] == value || rows[0][property] == (value as Number)?.toInteger()
        }

        where:
        importer << SIMPLE_IMPORTERS
    }

    @Unroll
    void '#importer.simpleName resolves bin locations, products and assignees of the counted rows'() {
        given:
        defineServicesOf(importer)
        Location bin = new Location(name: 'A-1').save(validate: false)
        Product product = new Product(name: 'Gloves', productCode: 'G1').save(validate: false)
        Person counter = new Person(firstName: 'Ann', lastName: 'Lee').save(validate: false, flush: true)
        GroovySpy(Person, global: true)
        Person.findAllByNameOrEmail(['Ann Lee']) >> [counter]
        Map columnMap = staticMap(importer, 'COLUMN_MAP') as Map
        ImportDataCommand command = new ImportDataCommand()
        String file = workbookFor(columnMap, [cycleCountId: 'cc1', cycleCountItemId: 'cci1', productCode: 'G1',
                                              'product.name': 'Gloves', lotNumber: 'L1', binLocation: 'A-1',
                                              quantityCounted: 4, quantityRecounted: 4, comment: 'ok',
                                              assignee: 'Ann Lee', recountAssignee: 'Ann Lee'])

        when:
        def importerInstance = importer.newInstance(file)
        List<Map> rows = importerInstance.data

        then:
        rows.size() == 1
        rows[0].binLocation == [id: bin.id, name: 'A-1']
        rows[0].product == [id: product.id, name: 'Gloves', productCode: 'G1']
        rows[0][assigneeKey] == [id: counter.id, name: 'Ann Lee', label: 'Ann Lee']
        rows[0].comment == 'ok'

        when:
        importerInstance.validateData(command)

        then:
        1 * importerInstance.cycleCountImportService./validate.*Import/(command)

        when:
        importerInstance.importData(command)

        then:
        thrown(UnsupportedOperationException)

        where:
        importer                            | assigneeKey
        CycleCountItemsExcelImporter        | 'assignee'
        CycleCountRecountItemsExcelImporter | 'recountAssignee'
    }
}
