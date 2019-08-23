package org.pih.warehouse.product

class ProductCatalog implements Comparable, Serializable {

    String id
    String code
    String name
    String description
    Boolean active = Boolean.TRUE
    String color

    static hasMany = [
            productCatalogItems: ProductCatalogItem
    ]

    static constraints = {
        code(nullable:false, unique: true)
        name(nullable:false)
        description(nullable:true)
        active(nullable:true)
        color(nullable: true)
    }

    // Auditing fields
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
        description type: 'text'
        code index: 'code_idx'
        cache true
        productCatalogItems sort: 'product'
    }

    static namedQueries = {
        includesProduct { product ->
            productCatalogItems {
                eq 'product', product
            }
            order("name", "asc")
        }
    }


    boolean contains(Product product) {
        return productCatalogItems.find { it.product == product }
    }

    /**
     * Remove all catalog items for the given product.
     *
     * @param product
     */
    void removeProduct(Product product) {
        def list = productCatalogItems.findAll { it.product = product }
        list.toArray().each {
            removeFromProductCatalogItems(it)
            it.delete()
        }
    }

    /**
     * Returns a string representation of the catalog.
     *
     * @return
     */
    String toString() {
        return "${name}"
    }

    /**
     * Sort by name
     */
    int compareTo(obj) {
        def sortOrder =
                name <=> obj?.name ?:
                        id <=> obj?.id
        return sortOrder

    }

    static PROPERTIES = [
            "id"         : "id",
            "code"       : "code",
            "name"       : "name",
            "description": "description"
    ]

}
