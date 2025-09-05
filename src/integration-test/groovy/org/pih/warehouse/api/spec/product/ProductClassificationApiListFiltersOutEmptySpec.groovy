package org.pih.warehouse.api.spec.product

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.product.ProductClassificationApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductClassificationDto

class ProductClassificationApiListFiltersOutEmptySpec extends ApiSpec {

    @Autowired
    ProductClassificationApiWrapper productClassificationApiWrapper

    @Shared
    Product productBlankAbcClass

    @Shared
    InventoryLevel inventoryLevel

    void setupData() {
        productBlankAbcClass = Product.build(abcClass: "")
        inventoryLevel = InventoryLevel.build(abcClass: "", inventory: facility.inventory)
    }

    void cleanupData() {
        if (productBlankAbcClass) {
            productBlankAbcClass.delete()
        }
        if (inventoryLevel) {
            inventoryLevel.delete()
        }
    }

    void 'given a valid facility, list excludes the empty string'() {
        when:
        List<ProductClassificationDto> classifications = productClassificationApiWrapper.listOK(facility.id)

        then:
        assert !asNames(classifications).contains("")
    }

    private List<String> asNames(List<ProductClassificationDto> classifications) {
        return classifications.collect { it.name }
    }
}
