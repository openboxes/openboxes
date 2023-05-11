package org.pih.warehouse.product

import org.pih.warehouse.core.User

class ProductMergeEvent {

    Product primaryProduct
    Product obsoleteProduct
    Date dateMerged
    User mergedBy
    String comments

    static constraints = {
        primaryProduct(nullable: false)
        obsoleteProduct(nullable: false)
        dateMerged(nullable: false)
        mergedBy(nullable: false)
        comments(nullable: true)
    }
}
