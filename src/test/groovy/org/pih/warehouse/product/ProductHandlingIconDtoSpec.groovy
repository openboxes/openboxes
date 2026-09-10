package org.pih.warehouse.product

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ProductHandlingIconDtoSpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains(Product)
    }

    void 'from should return no icons when there is no product'() {
        expect:
        ProductHandlingIconDto.from(null).isEmpty()
    }

    void 'from should return no icons for a product without any handling flag'() {
        expect:
        ProductHandlingIconDto.from(new Product()).isEmpty()
    }

    void 'from should return the #icon icon for a product with #flag set'() {
        given:
        Product product = new Product((flag): true)

        when:
        List<ProductHandlingIconDto> handlingIcons = ProductHandlingIconDto.from(product)

        then:
        assert handlingIcons.size() == 1
        assert handlingIcons.first().icon == icon
        assert handlingIcons.first().color == color

        where:
        flag                  | icon                      | color
        "coldChain"           | "fa-snowflake"            | "#3bafda"
        "controlledSubstance" | "fa-exclamation-circle"   | "#db1919"
        "hazardousMaterial"   | "fa-exclamation-triangle" | "#ffa500"
        "reconditioned"       | "fa-prescription-bottle"  | "#a9a9a9"
    }

    void 'from should return an icon per handling flag of the product'() {
        given:
        Product product = new Product(
                coldChain: true,
                controlledSubstance: true,
                hazardousMaterial: true,
                reconditioned: true,
        )

        expect:
        ProductHandlingIconDto.from(product)*.icon == [
                "fa-snowflake",
                "fa-exclamation-circle",
                "fa-exclamation-triangle",
                "fa-prescription-bottle",
        ]
    }
}