package org.pih.warehouse.product

import spock.lang.Specification

import org.pih.warehouse.core.mapper.HydrationLevel
import org.pih.warehouse.core.mapper.MapperConfig
import testutil.MessageLocalizerStub

/**
 * The stubbed message localizer returns the message code itself, so the label texts asserted here
 * are the codes that the labels are named by.
 */
class ProductDtoMapperSpec extends Specification {

    ProductDtoMapper mapper

    void setup() {
        mapper = new ProductDtoMapper(messageLocalizer: MessageLocalizerStub.MESSAGE_LOCALIZER_STUB)
    }

    void 'map should populate every field of the product'() {
        given:
        Product product = new Product(
                productCode: "PC1",
                name: "Ibuprofen 200mg",
                description: "Pain relief",
                active: true,
                lotControl: true,
                lotAndExpiryControl: true,
                serialized: false,
                coldChain: true,
                controlledSubstance: false,
                hazardousMaterial: false,
                reconditioned: false,
                essential: true,
                abcClass: "A",
                unitOfMeasure: "EA",
                upc: "012345678905",
                ndc: "0002-1433-80",
                packageSize: 10,
                color: "#FF0000",
        )
        product.id = "1"

        when:
        ProductDto dto = mapper.map(product)

        then:
        assert dto.id == "1"
        assert dto.productCode == "PC1"
        assert dto.name == "Ibuprofen 200mg"
        assert dto.description == "Pain relief"
        assert dto.active
        assert dto.lotControl
        assert dto.lotAndExpiryControl
        assert !dto.serialized
        assert dto.coldChain
        assert !dto.controlledSubstance
        assert !dto.hazardousMaterial
        assert !dto.reconditioned
        assert dto.essential
        assert dto.abcClass == "A"
        assert dto.unitOfMeasure == "EA"
        assert dto.upc == "012345678905"
        assert dto.ndc == "0002-1433-80"
        assert dto.packageSize == 10
        assert dto.color == "#FF0000"
        assert dto.handlingLabels*.labelCode == [ProductHandlingLabel.COLD_CHAIN]
        assert dto.handlingLabels*.labelText == ["product.coldChain.label"]
    }

    void 'map should populate only the id when the hydration level is ID_ONLY'() {
        given:
        Product product = new Product(productCode: "PC1", name: "Ibuprofen 200mg", lotAndExpiryControl: true)
        product.id = "1"

        when:
        ProductDto dto = mapper.map(product, new MapperConfig(hydrationLevel: HydrationLevel.ID_ONLY))

        then:
        assert dto.id == "1"
        assert dto.productCode == null
        assert dto.name == null
        assert dto.lotAndExpiryControl == null
        assert dto.handlingLabels.isEmpty()
    }

    void 'map should return null for no product'() {
        expect:
        assert mapper.map(null) == null
    }
}
