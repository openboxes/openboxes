package org.pih.warehouse.product

import grails.validation.Validateable
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus

class ProductEvents {

    Product product

    static constraints = {
        product(nullable: false)
    }

    static transients = [
        // Product Merge related transients
        "productMergeEvents",
        "productMergeSummary",
        "otherProductId" // other product id that was involved in the first product merge
    ]

    static ProductEvents createFromProduct(Product product) {
        ProductEvents productEvents = new ProductEvents(product: product)
        return productEvents
    }

    def getProductMergeEvents() {
        List<ProductMergeLogger> mergeAsPrimary = ProductMergeLogger.findAllByPrimaryProduct(product).unique { it.obsoleteProduct }
        List<ProductMergeEvent> mergeEventsAsPrimary = mergeAsPrimary?.collect {new ProductMergeEvent(
            primaryProduct: product,
            obsoleteProduct: it.obsoleteProduct,
            dateMerged: it.dateMerged,
            mergedBy: it.createdBy,
            comments: it.comments
        )}

        List<ProductMergeLogger> mergeAsObsolete = ProductMergeLogger.findAllByObsoleteProduct(product).unique { it.primaryProduct }
        List<ProductMergeEvent> mergeEventsAsObsolete = mergeAsObsolete?.collect {new ProductMergeEvent(
            primaryProduct: it.primaryProduct,
            obsoleteProduct: product,
            dateMerged: it.dateMerged,
            mergedBy: it.createdBy,
            comments: it.comments
        )}

        mergeEventsAsPrimary + mergeEventsAsObsolete
    }

    def getProductMergeSummary() {
        String mergeSummary = ""
        productMergeEvents?.each { ProductMergeEvent it ->
            if (it.primaryProduct == product) {
                mergeSummary += "Obsolete product: ${it.obsoleteProduct.productCode} ${it.obsoleteProduct.name}\n"
            } else if (it.obsoleteProduct == product) {
                mergeSummary += "Primary product: ${it.primaryProduct.productCode} ${it.primaryProduct.name}\n"
            }
        }
        return mergeSummary
    }

    def getOtherProductId() {
        ProductMergeEvent mergeEvent = productMergeEvents?.last()

        if (!mergeEvent) {
            return null
        }

        return mergeEvent.primaryProduct == product ? mergeEvent.obsoleteProduct.id : mergeEvent.primaryProduct.id
    }
}
