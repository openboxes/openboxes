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

    void 'asResponseBody should name every handling label of the product'() {
        given:
        ProductSimpleDto product = new ProductSimpleDto(
                id: "1",
                productCode: "PC1",
                name: "Ibuprofen 200mg",
                handlingLabels: [ProductHandlingLabel.COLD_CHAIN, ProductHandlingLabel.RECONDITIONED],
        )

        when:
        Map response = mapper.asResponseBody(product)

        then:
        assert response.id == "1"
        assert response.productCode == "PC1"
        assert response.name == "Ibuprofen 200mg"
        assert response.handlingLabels*.labelCode == [
                ProductHandlingLabel.COLD_CHAIN,
                ProductHandlingLabel.RECONDITIONED,
        ]
        assert response.handlingLabels*.labelText == [
                "product.coldChain.label",
                "product.reconditioned.label",
        ]
    }

    void 'asResponseBody should return no handling labels for a product without any'() {
        given:
        ProductSimpleDto product = new ProductSimpleDto(id: "1", name: "Ibuprofen 200mg")

        expect:
        mapper.asResponseBody(product).handlingLabels.isEmpty()
    }
}
