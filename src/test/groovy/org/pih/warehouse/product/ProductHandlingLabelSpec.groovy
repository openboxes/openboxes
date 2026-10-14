package org.pih.warehouse.product

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ProductHandlingLabelSpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains(Product)
    }

    void 'of should return no labels when there is no product'() {
        expect:
        ProductHandlingLabel.of(null).isEmpty()
    }

    void 'of should return no labels for a product without any handling flag'() {
        expect:
        ProductHandlingLabel.of(new Product()).isEmpty()
    }

    void 'of should return the #label label for a product with #flag set'() {
        given:
        Product product = new Product((flag): true)

        expect:
        ProductHandlingLabel.of(product) == [label]

        where:
        flag                  | label
        "coldChain"           | ProductHandlingLabel.COLD_CHAIN
        "controlledSubstance" | ProductHandlingLabel.CONTROLLED_SUBSTANCE
        "hazardousMaterial"   | ProductHandlingLabel.HAZARDOUS_MATERIAL
        "reconditioned"       | ProductHandlingLabel.RECONDITIONED
    }

    void 'of should return a label per handling flag of the product'() {
        given:
        Product product = new Product(
                coldChain: true,
                controlledSubstance: true,
                hazardousMaterial: true,
                reconditioned: true,
        )

        expect:
        ProductHandlingLabel.of(product) == [
                ProductHandlingLabel.COLD_CHAIN,
                ProductHandlingLabel.CONTROLLED_SUBSTANCE,
                ProductHandlingLabel.HAZARDOUS_MATERIAL,
                ProductHandlingLabel.RECONDITIONED,
        ]
    }
}
