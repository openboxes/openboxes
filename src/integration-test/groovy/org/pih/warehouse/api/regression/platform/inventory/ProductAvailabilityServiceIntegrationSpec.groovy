package org.pih.warehouse.api.regression.platform.inventory


import spock.lang.Ignore
import org.pih.warehouse.inventory.*
import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability

/**
 * The quantity queries over product availability, against a depot holding 5 of lot A in bin A and 3 of lot B with
 * no bin, and 4 of another product.
 */
class ProductAvailabilityServiceIntegrationSpec extends ServiceIntegrationSpec {

    Location depot
    Location binA
    Product product
    Product otherProduct
    InventoryItem lotA
    InventoryItem lotB

    void setup() {
        depot = newDepot('Availability depot')
        binA = newBin(depot, uniqueName('A'))
        product = newProduct()
        otherProduct = newProduct()
        lotA = stock(depot, product, uniqueName('A'), null, 5, binA)
        lotB = stock(depot, product, uniqueName('B'), null, 3)
        stock(depot, otherProduct, uniqueName('C'), null, 4)
    }

    void 'product availability is refreshed for a whole depot, for given bins, or not at all without a location'() {
        given:
        Closure<Map> quantities = {
            asAdminAt(depot) {
                ProductAvailability.findAllByLocation(Location.get(depot.id)).collectEntries { [(it.inventoryItem.id): it.quantityOnHand] }
            }
        }

        when:
        committed {
            ProductAvailability.findAllByLocation(Location.get(depot.id))*.delete()
        }
        Map deleted = quantities()
        asAdminAt(depot) { productAvailabilityService.refreshProductsAvailability(depot.id, null, false) }
        Map wholeDepot = quantities()
        asAdminAt(depot) { productAvailabilityService.refreshProductsAvailability(depot.id, [product.id], [binA.id, null], true) }
        Map forBins = quantities()
        asAdminAt(depot) { productAvailabilityService.refreshProductsAvailability(depot.id, [product.id], [], false) }
        asAdminAt(depot) { productAvailabilityService.refreshProductsAvailability(null, [product.id], [binA.id], false) }
        Map unchanged = quantities()

        then:
        deleted == [:]
        wholeDepot.size() == 3
        wholeDepot[lotA.id] == 5
        forBins == wholeDepot
        unchanged == wholeDepot
    }

    void 'the bin locations of a depot as of a date'() {
        when:
        Map result = asAdminAt(depot) {
            Location location = Location.get(depot.id)
            [
                    before: productAvailabilityService.calculateBinLocationsAsOfDate(location, new Date() - 30).size(),
                    after : productAvailabilityService.calculateBinLocationsAsOfDate(location, new Date() + 1)
                            .findAll { it.product?.id == product.id }.sum { it.quantity },
            ]
        }

        then:
        result.before == 0
        result.after == 8
    }

    void 'when products are merged, the availability of the obsolete lots moves to the primary product'() {
        given:
        Product obsolete = newProduct()
        String sharedLot = lotA.lotNumber
        InventoryItem obsoleteShared = stock(depot, obsolete, sharedLot, null, 3, binA)
        InventoryItem obsoleteOwn = stock(depot, obsolete, uniqueName('M'), null, 2)

        when:
        asAdminAt(depot) {
            productAvailabilityService.updateProductAvailabilityOnMergeProduct(InventoryItem.get(obsoleteOwn.id), Product.get(product.id),
                    Product.get(obsolete.id))
        }
        asAdminAt(depot) {
            productAvailabilityService.updateProductAvailabilityOnMergeProduct(InventoryItem.get(lotA.id), InventoryItem.get(obsoleteShared.id),
                    Product.get(product.id), Product.get(obsolete.id))
        }
        Map result = asAdminAt(depot) {
            Location location = Location.get(depot.id)
            [
                    primary : ProductAvailability.findAllByLocationAndProduct(location, Product.get(product.id))
                            .collectEntries { [(it.lotNumber): it.quantityOnHand] },
                    obsolete: ProductAvailability.findAllByLocationAndProduct(location, Product.get(obsolete.id))*.quantityOnHand.sum() ?: 0,
            ]
        }

        then:
        result.primary[sharedLot] == 8
        result.primary[obsoleteOwn.lotNumber] == 2
        result.primary[lotB.lotNumber] == 3
        result.obsolete == 0

        expect: 'nothing happens without the items to merge'
        asAdminAt(depot) { productAvailabilityService.updateProductAvailabilityOnMergeProduct(null, Product.get(product.id), null) } == null
        asAdminAt(depot) { productAvailabilityService.updateProductAvailabilityOnMergeProduct(null, null, Product.get(product.id), null) } == null
    }
}
