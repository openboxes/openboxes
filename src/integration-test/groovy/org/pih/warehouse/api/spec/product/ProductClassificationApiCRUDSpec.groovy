package org.pih.warehouse.api.spec.product

import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.product.ProductClassificationApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductClassificationDto

/**
 * Test the product classification endpoints.
 */
class ProductClassificationApiCRUDSpec extends ApiSpec {

    static final String CLASS_A = "A FOR TEST"
    static final String CLASS_B = "B FOR TEST"
    static final String CLASS_C = "C FOR TEST"
    static final String CLASS_D = "D FOR TEST"

    @Autowired
    ProductClassificationApiWrapper productClassificationApiWrapper

    void setupData() {
        Product.findOrBuild(abcClass: CLASS_A)
        Product.findOrBuild(abcClass: CLASS_B)

        Location facilityB = new LocationTestBuilder().asFacility().findOrBuild()

        InventoryLevel.findOrBuild(abcClass: CLASS_A, inventory: facility.inventory)  // A product has this class
        InventoryLevel.findOrBuild(abcClass: CLASS_C, inventory: facility.inventory)  // No product has this class
        InventoryLevel.findOrBuild(abcClass: CLASS_D, inventory: facilityB.inventory)  // A different facility
    }

    void cleanupData() {
        Product.where { abcClass in [CLASS_A, CLASS_B, CLASS_C] }.deleteAll()
    }

    void 'given a valid facility, list returns a unique list of all valid classifications'() {
        when:
        List<ProductClassificationDto> classifications = productClassificationApiWrapper.listOK(facility.id)

        then:
        assert classifications.size() >= 3  // Don't use '==' because other tests might have left data in the db

        List<String> names = asNames(classifications)
        assert names.containsAll([CLASS_A, CLASS_B, CLASS_C])
        assert names.size() == new HashSet<>(names).size()  // Ensure there are no duplicates
    }

    void 'given an invalid facility, list correctly errors'() {
        expect:
        // TODO: Change the API so that it returns a 400 so that it can display nicely.
        productClassificationApiWrapper.list("-1", HttpStatus.SC_INTERNAL_SERVER_ERROR)
    }

    private List<String> asNames(List<ProductClassificationDto> classifications) {
        return classifications.collect { it.name }
    }
}
