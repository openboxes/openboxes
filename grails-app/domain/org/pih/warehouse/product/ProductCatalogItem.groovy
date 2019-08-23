package org.pih.warehouse.product

class ProductCatalogItem implements Comparable, Serializable {

    String id
    Product product
    Boolean active = Boolean.TRUE

    // Auditing fields
    Date dateCreated
    Date lastUpdated

    static constraints = {
        product(nullable: false)
        active(nullable: false)
    }

    static belongsTo = [productCatalog: ProductCatalog]

    static mapping = {
        id generator: 'uuid'
        cache true
    }

    /**
     * Returns a string representation of the catalog.
     *
     * @return
     */
    String toString() {
        return "${id}"
    }

    /**
     * Sort by name
     */
    int compareTo(obj) {
        def sortOrder = id <=> obj?.id
        return sortOrder
    }

    static PROPERTIES = [
            "productCatalogCode": "productCatalog.code",
            "productCode"       : "product.productCode",
            "productName"       : "product.name",
    ]

}
