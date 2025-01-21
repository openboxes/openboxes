package org.pih.warehouse.product

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryLevel

/**
 * Product Classification is a way of categorizing products for the purposes of measuring their impact/importance,
 * which in turn can be used for prioritization/configuration of feature-specific tasks.
 *
 * For example, the stock of fast moving items is more volatile compared to other items. They also often account for
 * a higher percentage of the financial value of the products that are moving through a facility. As such, we may want
 * to view them as more "important", which we designate via a classification ("A" for example). Rules can then be
 * applied to the classification as needed. For instance, we can decide that all "A" products should be be counted at
 * least once a week.
 *
 * It's important to note that the classifications themselves contain no inherent value. It's up to the implementers
 * to decide what a classification really means, which is determined by the feature-specific rules that are configured
 * for each classification.
 *
 * For example, if you're using the ABC Classification rules, the classes "A", "B", "C" are just strings.
 * The meaning comes in when we configure the cycle count feature to require class "A" products to be counted weekly,
 * class "B" products to be counted monthly, and class "C" products to be counted yearly.
 */
@Transactional
class ProductClassificationService {

    /**
     * Fetches all product classifications that we have configured.
     *
     * Currently, classifications exist as plain strings under the "abcClass" column of the Product and InventoryLevel
     * tables. Until we move classifications to their own entity, we simply fetch all unique values from those columns.
     */
    List<ProductClassificationDto> list(String facilityId) {
        Location facility = Location.read(facilityId)
        if (!facility) {
            throw new IllegalArgumentException("Invalid facilityId: ${facilityId}")
        }

        List<String> productClassifications = Product.createCriteria().listDistinct() {
            projections {
                groupProperty("abcClass")
            }
            isNotNull("abcClass")
        } as List<String>

        productClassifications += InventoryLevel.createCriteria().listDistinct() {
            projections {
                groupProperty("abcClass")
            }
            // Only fetch abcClasses that are configured for the requesting facility.
            eq("inventory", facility.inventory)
            isNotNull("abcClass")
        } as List<String>

        // Sort the results by ABC class name. Once ABC class becomes its own domain, this can be moved to the query.
        Collections.sort(productClassifications)

        // We convert to a DTO here instead of returning the raw string because it will make things easier
        // once product classification becomes a proper entity.
        return productClassifications.collect {
            new ProductClassificationDto(name: it)
        }
    }
}
