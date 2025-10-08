package org.pih.warehouse.api.spec.product

import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.Constants
import org.pih.warehouse.product.ProductAvailability

class ProductApiProductAvailabilitySpec extends ApiSpec {

    private static final int QUANTITY_ON_HAND = 10

    void 'product availability can successfully be fetched for products with no stock'() {
        given: 'a product with no stock'
        setStock(product, null, null, 0)

        expect: 'product availability to be empty'
        assert productApiWrapper.getProductAvailabilityOK(product).size() == 0
    }

    void 'product availability can successfully be fetched for products with stock'() {
        given: 'a product with some stock in the default bin and lot'
        setStock(product, null, null, QUANTITY_ON_HAND)

        when:
        List<ProductAvailability> availability = productApiWrapper.getProductAvailabilityOK(product)

        then: 'product availability should return that stock'
        assert availability.size() == 1

        when:
        ProductAvailability defaultBinAndLotAvailability = availability[0]

        then:
        assert defaultBinAndLotAvailability.location.id == facility.id
        assert defaultBinAndLotAvailability.product.id == product.id
        assert defaultBinAndLotAvailability.productCode == product.productCode
        assert defaultBinAndLotAvailability.binLocation == null
        assert defaultBinAndLotAvailability.binLocationName == Constants.DEFAULT
        assert defaultBinAndLotAvailability.inventoryItem != null
        assert defaultBinAndLotAvailability.lotNumber == Constants.DEFAULT
        assert defaultBinAndLotAvailability.quantityOnHand == QUANTITY_ON_HAND
        assert defaultBinAndLotAvailability.quantityAllocated == 0
        assert defaultBinAndLotAvailability.quantityOnHold == 0
        assert defaultBinAndLotAvailability.quantityAvailableToPromise == QUANTITY_ON_HAND
        assert defaultBinAndLotAvailability.quantityNotPicked == QUANTITY_ON_HAND
    }
}
