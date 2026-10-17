package org.pih.warehouse.regression.platform.importer

import org.pih.warehouse.importer.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.product.ProductAssociationValidator

/**
 * Rows are shaped like the import binder hands them over: product codes under "product.productCode" and
 * "associatedProduct.productCode". validateData rewrites each row in place (product ids, upper-cased code, quantity),
 * and importData expects those rewritten rows, so the import tests run both steps like the import controller does.
 */
@Unroll
class ProductAssociationImportDataServiceSpec extends Specification implements ServiceUnitTest<ProductAssociationImportDataService>, DataTest {

    Product aspirin
    Product paracetamol
    Product retired

    void setupSpec() {
        mockDomains(Product, ProductAssociation)

        // ProductAssociation.validate() runs javax validation through "defaultValidator" and the custom
        // ProductAssociationValidator bean; neither is registered in a DataTest context by default.
        defineBeans {
            defaultValidator(LocalValidatorFactoryBean)
            productAssociationValidator(ProductAssociationValidator)
        }
    }

    void setup() {
        aspirin = saveProduct('p-aspirin', 'ASP', 'Aspirin', true)
        paracetamol = saveProduct('p-paracetamol', 'PAR', 'Paracetamol', true)
        retired = saveProduct('p-retired', 'OLD', 'Retired', false)
    }

    void 'validateData should accept a new association and rewrite the row for the import'() {
        given:
        Map row = row('ASP', 'PAR', 'substitute') + [conversion: 5, hasMutualAssociation: 'yes']
        ImportDataCommand command = new ImportDataCommand(data: [row])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()

        and:
        row.code == 'SUBSTITUTE'
        row.quantity == 5
        row.product == 'p-aspirin'
        row['product.name'] == 'Aspirin'
        row.associatedProduct == 'p-paracetamol'
        row['associatedProduct.name'] == 'Paracetamol'
        row.hasMutualAssociation == true
    }

    void 'validateData should read the mutual flag #value as #expected'() {
        given:
        Map row = row('ASP', 'PAR', 'SUBSTITUTE') + [hasMutualAssociation: value]

        when:
        service.validateData(new ImportDataCommand(data: [row]))

        then:
        row.hasMutualAssociation == expected

        where:
        value   || expected
        'true'  || true
        'YES'   || true
        true    || true
        'no'    || false
        null    || false
    }

    void 'validateData should default the quantity to one when no conversion is given'() {
        given:
        Map row = row('ASP', 'PAR', 'SUBSTITUTE')

        when:
        service.validateData(new ImportDataCommand(data: [row]))

        then:
        row.quantity == 1
    }

    void 'validateData should reject #description'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [row(productCode, associatedCode, code) + extra])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == expectedErrors

        where:
        description                     | productCode | associatedCode | code         | extra       || expectedErrors
        'an existing association id'    | 'ASP'       | 'PAR'          | 'SUBSTITUTE' | [id: 'a-1'] || ['Row 1: Cannot edit existing associations via import']
        'a missing association type'    | 'ASP'       | 'PAR'          | null         | [:]         || ['Row 1: Association Type field can not be empty']
        'an unknown association type'   | 'ASP'       | 'PAR'          | 'nonsense'   | [:]         || ["Row 1: Association Type code 'NONSENSE' does not exist"]
        'an unknown product'            | 'NOPE'      | 'PAR'          | 'SUBSTITUTE' | [:]         || ["Row 1: Product with code 'NOPE' does not exist"]
        'an unknown associated product' | 'ASP'       | 'NOPE'         | 'SUBSTITUTE' | [:]         || ["Row 1: Product with code 'NOPE' does not exist"]
        'an inactive product'           | 'OLD'       | 'PAR'          | 'SUBSTITUTE' | [:]         || ["Row 1: Product with code 'OLD' is inactive"]
        'an inactive associated product'| 'ASP'       | 'OLD'          | 'SUBSTITUTE' | [:]         || ["Row 1: Product with code 'OLD' is inactive"]
        'a product associated to itself'| 'ASP'       | 'ASP'          | 'SUBSTITUTE' | [:]         || ['Cannot associate a product with itself']
    }

    void 'validateData should reject a one-way association that already exists'() {
        given:
        saveAssociation(aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        ImportDataCommand command = new ImportDataCommand(data: [row('ASP', 'PAR', 'SUBSTITUTE')])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == ['Row 1: association already exists']
    }

    void 'validateData should accept a one-way association of another type or in the other direction'() {
        given:
        saveAssociation(aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        ImportDataCommand command = new ImportDataCommand(data: [
                row('ASP', 'PAR', 'EQUIVALENT'),
                row('PAR', 'ASP', 'SUBSTITUTE'),
        ])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    void 'validateData should reject a two-way association when #existing'() {
        given:
        ProductAssociation forward = saveAssociation(aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        if (mutual) {
            ProductAssociation backward = saveAssociation(paracetamol, aspirin, ProductAssociationTypeCode.SUBSTITUTE)
            forward.mutualAssociation = backward
            backward.mutualAssociation = forward
        }

        and: 'the import names the pair in the opposite direction'
        ImportDataCommand command = new ImportDataCommand(data: [row('PAR', 'ASP', 'SUBSTITUTE') + [hasMutualAssociation: 'true']])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == [expectedError]

        where:
        existing                             | mutual || expectedError
        'one side already exists on its own' | false  || 'Row 1: One-way of the association already exists'
        'the same two-way pair exists'       | true   || 'Row 1: Same two-way association already exists'
    }

    void 'validateData should report a row repeating an earlier one of the import'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [
                row('ASP', 'PAR', 'SUBSTITUTE'),
                row('ASP', 'PAR', 'EQUIVALENT'),
                row('ASP', 'PAR', 'substitute'),
        ])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == ['Row 3: Duplicate association on row: 1']
    }

    void 'validateData should treat the reverse of a two-way row as a duplicate (mutual on row #mutualRow)'() {
        given:
        Map first = row('ASP', 'PAR', 'SUBSTITUTE') + [hasMutualAssociation: mutualRow == 1]
        Map second = row('PAR', 'ASP', 'SUBSTITUTE') + [hasMutualAssociation: mutualRow == 2]
        ImportDataCommand command = new ImportDataCommand(data: [first, second])

        when:
        service.validateData(command)

        then:
        command.errors.allErrors*.code == ['Row 2: Duplicate association on row: 1']

        where:
        mutualRow << [1, 2]
    }

    void 'validateData should not treat the reverse of a one-way row as a duplicate'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [row('ASP', 'PAR', 'SUBSTITUTE'), row('PAR', 'ASP', 'SUBSTITUTE')])

        when:
        service.validateData(command)

        then:
        !command.errors.hasErrors()
    }

    void 'importData should save a one-way association with its quantity and comments'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [row('ASP', 'PAR', 'substitute') + [conversion: 3, comments: 'Same molecule']])
        service.validateData(command)

        when:
        service.importData(command)

        then:
        ProductAssociation.count() == 1
        ProductAssociation association = ProductAssociation.list().first()
        association.product == aspirin
        association.associatedProduct == paracetamol
        association.code == ProductAssociationTypeCode.SUBSTITUTE
        association.quantity == 3
        association.comments == 'Same molecule'
        association.mutualAssociation == null
    }

    void 'importData should save both sides of a two-way association pointing at each other'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [row('ASP', 'PAR', 'EQUIVALENT') + [hasMutualAssociation: 'yes']])
        service.validateData(command)

        when:
        service.importData(command)

        then:
        ProductAssociation.count() == 2
        ProductAssociation forward = ProductAssociation.findByProduct(aspirin)
        ProductAssociation backward = ProductAssociation.findByProduct(paracetamol)
        forward.associatedProduct == paracetamol
        backward.associatedProduct == aspirin
        backward.code == ProductAssociationTypeCode.EQUIVALENT
        forward.mutualAssociation == backward
        backward.mutualAssociation == forward
    }

    void 'importData should fail on an association that does not pass validation'() {
        given: 'a row that skipped validateData and so still lacks the product ids'
        ImportDataCommand command = new ImportDataCommand(data: [[code: 'SUBSTITUTE']])

        when:
        service.importData(command)

        then:
        ValidationException e = thrown()
        e.errors.hasFieldErrors('associatedProduct')
        ProductAssociation.count() == 0
    }

    private static Map row(String productCode, String associatedProductCode, String code) {
        return [
                'product.productCode'          : productCode,
                'associatedProduct.productCode': associatedProductCode,
                code                           : code,
        ]
    }

    private static Product saveProduct(String id, String productCode, String name, boolean active) {
        Product product = new Product(productCode: productCode, name: name, active: active)
        product.id = id
        return product.save(validate: false)
    }

    private static ProductAssociation saveAssociation(Product product, Product associatedProduct, ProductAssociationTypeCode code) {
        return new ProductAssociation(product: product, associatedProduct: associatedProduct, code: code)
                .save(validate: false)
    }
}
