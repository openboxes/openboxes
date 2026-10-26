package org.pih.warehouse.regression.platform.importer

import org.pih.warehouse.importer.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.core.UnitOfMeasureType
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAttribute
import org.pih.warehouse.product.ProductSupplier

@Unroll
class ProductSupplierAttributeImportDataServiceSpec extends Specification implements ServiceUnitTest<ProductSupplierAttributeImportDataService>, DataTest {

    Product aspirin
    ProductSupplier aspirinSource
    Attribute color
    Attribute weight
    UnitOfMeasure kilogram
    UnitOfMeasure liter

    void setupSpec() {
        mockDomains(Product, ProductSupplier, Attribute, ProductAttribute, UnitOfMeasure, UnitOfMeasureClass)
    }

    void setup() {
        aspirin = new Product(productCode: 'ASP', name: 'Aspirin').save(validate: false)
        aspirinSource = new ProductSupplier(code: 'PS-1', name: 'Aspirin from Acme', product: aspirin).save(validate: false)

        UnitOfMeasureClass mass = new UnitOfMeasureClass(code: 'MASS', name: 'Mass', type: UnitOfMeasureType.MASS).save(validate: false)
        UnitOfMeasureClass volume = new UnitOfMeasureClass(code: 'VOL', name: 'Volume', type: UnitOfMeasureType.VOLUME).save(validate: false)
        kilogram = new UnitOfMeasure(code: 'KG', name: 'Kilogram', uomClass: mass).save(validate: false)
        liter = new UnitOfMeasure(code: 'L', name: 'Liter', uomClass: volume).save(validate: false)

        color = saveAttribute('COLOR', [EntityTypeCode.PRODUCT_SUPPLIER], ['Red', 'Blue'], false, null)
        weight = saveAttribute('WEIGHT', [EntityTypeCode.PRODUCT_SUPPLIER], [], true, mass)
        saveAttribute('SIZE', [EntityTypeCode.PRODUCT], [], true, null)
    }

    void 'validateData should accept #description'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + overrides])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()

        where:
        description                                   | overrides
        'a value from the options of the attribute'   | [:]
        'free text for an attribute that allows it'   | [attributeCode: 'WEIGHT', attributeValue: '12.5', unitOfMeasure: 'KG']
    }

    void 'validateData should stop at the first problem of a row: #description'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + overrides])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == [expectedError]

        where:
        description                       | overrides                                  || expectedError
        'missing product code'            | [productCode: null]                        || 'Row 1: Product code is required'
        'unknown product'                 | [productCode: 'NOPE']                      || 'Row 1: Product with code NOPE does not exist'
        'missing source code'             | [productSupplierCode: '']                  || 'Row 1: Product source code is required'
        'unknown source'                  | [productSupplierCode: 'PS-9']              || 'Row 1: Product source with code PS-9 does not exist'
        'missing attribute code'          | [attributeCode: null]                      || 'Row 1: Attribute code is required'
        'unknown attribute'               | [attributeCode: 'SHAPE']                   || 'Row 1: Attribute with code SHAPE does not exist'
        'attribute not for sources'       | [attributeCode: 'SIZE']                    || 'Row 1: Attribute for given attribute code SIZE is not supplier type'
        'missing attribute value'         | [attributeValue: '']                       || 'Row 1: Attribute value is required'
        'value outside of the options'    | [attributeValue: 'Green']                  || 'Row 1: Attribute value Green is not valid option for attribute with code COLOR'
    }

    void 'validateData should reject a unit of measure of the wrong class (#unitOfMeasure)'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [attributeCode: 'WEIGHT', attributeValue: '1', unitOfMeasure: unitOfMeasure]])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == ["Row 1: Unit of Measure ${unitOfMeasure} data type does not match the unit of measure data type for given attribute".toString()]

        where: 'a liter is a volume, and a missing or unknown unit has no class at all'
        unitOfMeasure << ['L', null, 'XX']
    }

    void 'validateData should ignore the unit of measure for an attribute without a unit class'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [unitOfMeasure: 'L']])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    void 'validateData should validate every row'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [attributeValue: 'Green'], validRow(), validRow() + [productSupplierCode: 'PS-9']])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == [
                'Row 1: Attribute value Green is not valid option for attribute with code COLOR',
                'Row 3: Product source with code PS-9 does not exist',
        ]
    }

    void 'importData should add a new source attribute to the product'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [attributeCode: 'WEIGHT', attributeValue: '12.5', unitOfMeasure: 'KG']])

        when:
        service.importData(command)

        then:
        ProductAttribute.count() == 1
        ProductAttribute productAttribute = ProductAttribute.list().first()
        productAttribute.product == aspirin
        productAttribute.productSupplier == aspirinSource
        productAttribute.attribute == weight
        productAttribute.value == '12.5'
        productAttribute.unitOfMeasure == kilogram
        aspirin.attributes.toList() == [productAttribute]
    }

    void 'importData should update the existing value of the source attribute'() {
        given:
        ProductAttribute existing = new ProductAttribute(attribute: color, productSupplier: aspirinSource, value: 'Blue', unitOfMeasure: kilogram)
        aspirin.addToAttributes(existing)
        existing.save(validate: false)

        when:
        service.importData(new ImportDataCommand(data: [validRow()]))

        then:
        ProductAttribute.count() == 1
        ProductAttribute updated = ProductAttribute.get(existing.id)
        updated.value == 'Red'
        updated.unitOfMeasure == null
    }

    void 'importData should not touch the attribute of the product itself or of another source'() {
        given:
        ProductSupplier otherSource = new ProductSupplier(code: 'PS-2', name: 'Other', product: aspirin).save(validate: false)
        ProductAttribute productLevel = new ProductAttribute(attribute: color, value: 'Blue')
        ProductAttribute otherSourceLevel = new ProductAttribute(attribute: color, productSupplier: otherSource, value: 'Blue')
        aspirin.addToAttributes(productLevel)
        aspirin.addToAttributes(otherSourceLevel)
        productLevel.save(validate: false)
        otherSourceLevel.save(validate: false)

        when:
        service.importData(new ImportDataCommand(data: [validRow()]))

        then:
        ProductAttribute.count() == 3
        ProductAttribute.get(productLevel.id).value == 'Blue'
        ProductAttribute.get(otherSourceLevel.id).value == 'Blue'
        ProductAttribute.findByAttributeAndProductSupplier(color, aspirinSource).value == 'Red'
    }

    void 'importData should skip a value that does not pass validation'() {
        given: 'a value longer than the 255 characters a product attribute can hold'
        ImportDataCommand command = new ImportDataCommand(data: [validRow() + [attributeCode: 'WEIGHT', attributeValue: 'x' * 256]])

        when:
        service.importData(command)

        then:
        notThrown(Exception)
        ProductAttribute.count() == 0
    }

    private static Map validRow() {
        return [productCode: 'ASP', productSupplierCode: 'PS-1', attributeCode: 'COLOR', attributeValue: 'Red']
    }

    private static Attribute saveAttribute(String code, List<EntityTypeCode> entityTypeCodes, List<String> options,
                                           boolean allowOther, UnitOfMeasureClass unitOfMeasureClass) {
        Attribute attribute = new Attribute(code: code, name: code, allowOther: allowOther, unitOfMeasureClass: unitOfMeasureClass)
        attribute.entityTypeCodes = entityTypeCodes as Set
        attribute.options = options
        return attribute.save(validate: false)
    }
}
