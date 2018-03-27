package org.pih.warehouse.product

class ProductCatalog implements Comparable, Serializable {

    String id
    String code
    String name
    String description
    Boolean active = Boolean.TRUE

    static hasMany = [
        productCatalogItems: ProductCatalogItem
    ]

    static constraints = {
        code(nullable:false, unique: true)
        name(nullable:false)
        description(nullable:true)
        active(nullable:true)
    }

    // Auditing fields
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
        description type: 'text'
        code index: 'code_idx'
        cache true
        productCatalogItems sort:'product'
    }


    boolean contains(Product product) {
        return productCatalogItems.find { it.product == product }
    }

    /**
     * Returns a string representation of the catalog.
     *
     * @return
     */
    String toString() {
        return "${name}";
    }

    /**
     * Sort by name
     */
    int compareTo(obj) {
        def sortOrder =
                name <=> obj?.name ?:
                        id <=> obj?.id
        return sortOrder;

    }


}
