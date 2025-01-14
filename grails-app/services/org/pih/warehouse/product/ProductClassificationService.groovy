package org.pih.warehouse.product

import grails.gorm.transactions.Transactional

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
 * It's important to note that the classifications themselves contain no inherent value. They're simply values that
 * we can assign to products. It's up to the implementers to decide what a classification really means, which is
 * determined by the feature-specific rules that are configured for each classification.
 *
 * For example, if you're using the ABC Classification rules, the classes "A", "B", "C" are just strings.
 * The meaning comes in when we configure the cycle count feature to require class "A" products to be counted weekly,
 * class "B" products to be counted monthly, and class "C" products to be counted yearly.
 */
@Transactional
class ProductClassificationService {

    /**
     * Fetches all product classifications that we have configured at the facility.
     *
     * Currently, classifications exist as plain strings under the abcClass column of the Product and InventoryLevel
     * tables. Until we move classifications to their own entity, we simply fetch all unique values from those columns.
     */
    List<String> list() {
        // Use a projection because we only need the one field. The grouping is for uniqueness.
        // We do this in two separate queries then join the result in code for simplicity.
        Set<String> productClassifications = Product.createCriteria().listDistinct() {
            projections {
                groupProperty("abcClass")
            }
        } as Set<String>
        productClassifications += InventoryLevel.createCriteria().listDistinct() {
            projections {
                groupProperty("abcClass")
            }
        } as Set<String>

        // Filter out null since it's not a valid classification. Note that abcClass is allowed to be blank, so blank
        // strings are considered valid classifications here. Likely we'll want to sanitize the data at some point to
        // prevent blanks from being allowed, but since this is just a list call, we return whatever we have in the db.
        productClassifications.remove(null)

        return productClassifications.toList()
    }
}
