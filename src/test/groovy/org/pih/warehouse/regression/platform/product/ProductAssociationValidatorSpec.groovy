package org.pih.warehouse.regression.platform.product

import org.pih.warehouse.product.*
import grails.testing.gorm.DataTest
import grails.validation.ValidationException
import spock.lang.Ignore
import spock.lang.Specification

import org.pih.warehouse.core.validation.ObjectValidationResult

/**
 * The rules are asserted through doValidate so the error codes can be read off the returned result. Existing
 * associations are saved without validation so that only the validator under test decides what is a duplicate.
 */
class ProductAssociationValidatorSpec extends Specification implements DataTest {

    ProductAssociationValidator validator = new ProductAssociationValidator()

    Product aspirin
    Product paracetamol
    Product ibuprofen

    void setupSpec() {
        mockDomains(Product, ProductAssociation)
    }

    void setup() {
        aspirin = saveProduct('p-aspirin', 'Aspirin')
        paracetamol = saveProduct('p-paracetamol', 'Paracetamol')
        ibuprofen = saveProduct('p-ibuprofen', 'Ibuprofen')
    }

    void 'a new association between two different products should be valid'() {
        given:
        ProductAssociation association = buildAssociation(aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)

        expect:
        validator.doValidate(association).valid
    }

    void 'a product should not be associated with itself'() {
        given:
        ProductAssociation association = buildAssociation(aspirin, aspirin, ProductAssociationTypeCode.SUBSTITUTE)

        when:
        ObjectValidationResult result = validator.doValidate(association)

        then:
        result.errors*.code == ['productAssociation.associatedProduct.invalid.selfAssociation']
        result.errors[0].field == 'associatedProduct'
        result.errors[0].rejectedValue == aspirin
    }

    void 'an association repeating an existing one with the same type should be a duplicate'() {
        given:
        saveAssociation('a-1', aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        ProductAssociation association = buildAssociation(aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)

        when:
        ObjectValidationResult result = validator.doValidate(association)

        then:
        result.errors*.code == ['productAssociation.associatedProduct.invalid.duplicateAssociation']
    }

    void 'the same two products may be associated again with a different type'() {
        given:
        saveAssociation('a-1', aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        ProductAssociation association = buildAssociation(aspirin, paracetamol, ProductAssociationTypeCode.ACCESSORY)

        expect:
        validator.doValidate(association).valid
    }

    void 'an association to another product should not clash with an existing one of the same type'() {
        given:
        saveAssociation('a-1', aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        ProductAssociation association = buildAssociation(aspirin, ibuprofen, ProductAssociationTypeCode.SUBSTITUTE)

        expect:
        validator.doValidate(association).valid
    }

    void 'updating an existing association should not count it as its own duplicate'() {
        given:
        ProductAssociation association = saveAssociation('a-1', aspirin, paracetamol,
                ProductAssociationTypeCode.SUBSTITUTE)
        association.comments = 'updated'

        expect:
        validator.doValidate(association).valid
    }

    void 'a one way association should not clash with an existing association in the opposite direction'() {
        given:
        saveAssociation('a-1', paracetamol, aspirin, ProductAssociationTypeCode.SUBSTITUTE)
        ProductAssociation association = buildAssociation(aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)

        expect:
        validator.doValidate(association).valid
    }

    void 'a mutual association should be a duplicate when the opposite direction already exists on its own'() {
        given:
        saveAssociation('a-1', paracetamol, aspirin, ProductAssociationTypeCode.SUBSTITUTE)
        ProductAssociation association = buildAssociation(aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        association.mutualAssociation = buildAssociation(paracetamol, aspirin, ProductAssociationTypeCode.SUBSTITUTE)

        when:
        ObjectValidationResult result = validator.doValidate(association)

        then:
        result.errors*.code == ['productAssociation.associatedProduct.invalid.duplicateMutualAssociation']
    }

    void 'a mutual association repeating an existing association should report both duplicates'() {
        given:
        saveAssociation('a-1', aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        ProductAssociation association = buildAssociation(aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        association.mutualAssociation = buildAssociation(paracetamol, aspirin, ProductAssociationTypeCode.SUBSTITUTE)

        when:
        ObjectValidationResult result = validator.doValidate(association)

        then:
        result.errors*.code == [
                'productAssociation.associatedProduct.invalid.duplicateAssociation',
                'productAssociation.associatedProduct.invalid.duplicateMutualAssociation',
        ]
    }

    void 'an existing mutual pair should not count either side as a duplicate of the other'() {
        given:
        ProductAssociation forward = saveAssociation('a-1', aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        ProductAssociation backward = saveAssociation('a-2', paracetamol, aspirin,
                ProductAssociationTypeCode.SUBSTITUTE)
        forward.mutualAssociation = backward
        backward.mutualAssociation = forward

        expect:
        validator.doValidate(forward).valid
        validator.doValidate(backward).valid
    }

    void 'an existing mutual pair should still clash with a third association of the same type'() {
        given:
        ProductAssociation forward = saveAssociation('a-1', aspirin, paracetamol, ProductAssociationTypeCode.SUBSTITUTE)
        ProductAssociation backward = saveAssociation('a-2', paracetamol, aspirin,
                ProductAssociationTypeCode.SUBSTITUTE)
        forward.mutualAssociation = backward
        backward.mutualAssociation = forward
        saveAssociation('a-3', paracetamol, aspirin, ProductAssociationTypeCode.SUBSTITUTE)

        when:
        ObjectValidationResult result = validator.doValidate(forward)

        then:
        result.errors*.code == ['productAssociation.associatedProduct.invalid.duplicateMutualAssociation']
    }

    private static Product saveProduct(String id, String name) {
        Product product = new Product(name: name)
        product.id = id
        return product.save(validate: false)
    }

    private static ProductAssociation buildAssociation(Product product, Product associatedProduct,
                                                       ProductAssociationTypeCode code) {
        return new ProductAssociation(product: product, associatedProduct: associatedProduct, code: code)
    }

    private static ProductAssociation saveAssociation(String id, Product product, Product associatedProduct,
                                                      ProductAssociationTypeCode code) {
        ProductAssociation association = buildAssociation(product, associatedProduct, code)
        association.id = id
        return association.save(validate: false)
    }
}
