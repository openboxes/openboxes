package org.pih.warehouse.product

import spock.lang.Specification

import testutil.MessageLocalizerStub

/**
 * The stubbed message localizer returns the message code itself, so the label texts asserted here
 * are the codes that the labels are named by.
 */
class ProductSimpleDtoMapperSpec extends Specification {

    ProductSimpleDtoMapper mapper

    void setup() {
        mapper = new ProductSimpleDtoMapper(messageLocalizer: MessageLocalizerStub.MESSAGE_LOCALIZER_STUB)
    }

    void 'map should name every handling label of the product'() {
        given:
        Product product = new Product(
                productCode: "PC1",
                name: "Ibuprofen 200mg",
                coldChain: true,
                controlledSubstance: true,
                hazardousMaterial: true,
                reconditioned: true,
        )
        product.id = "1"

        when:
        ProductSimpleDto dto = mapper.map(product)

        then:
        assert dto.id == "1"
        assert dto.productCode == "PC1"
        assert dto.name == "Ibuprofen 200mg"
        assert dto.handlingLabels*.labelCode == [
                ProductHandlingLabel.COLD_CHAIN,
                ProductHandlingLabel.CONTROLLED_SUBSTANCE,
                ProductHandlingLabel.HAZARDOUS_MATERIAL,
                ProductHandlingLabel.RECONDITIONED,
        ]
        assert dto.handlingLabels*.labelText == [
                "product.coldChain.label",
                "product.controlledSubstance.label",
                "product.hazardousMaterial.label",
                "product.reconditioned.label",
        ]
    }

    void 'map should name only the handling labels of the product that are true'() {
        given:
        Product product = new Product(
                coldChain: true,
                controlledSubstance: true,
                hazardousMaterial: false,
                reconditioned: false,
        )

        when:
        ProductSimpleDto dto = mapper.map(product)

        then:
        assert dto.handlingLabels*.labelCode == [
                ProductHandlingLabel.COLD_CHAIN,
                ProductHandlingLabel.CONTROLLED_SUBSTANCE,
        ]
        assert dto.handlingLabels*.labelText == [
                "product.coldChain.label",
                "product.controlledSubstance.label",
        ]
    }

    void 'map should return no handling labels for a product without any'() {
        given:
        Product product = new Product(name: "Ibuprofen 200mg")

        expect:
        assert mapper.map(product).handlingLabels.isEmpty()
    }
}
