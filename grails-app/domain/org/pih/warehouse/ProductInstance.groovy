package org.pih.warehouse


/**
 * An item is an instance of a product.  For instance,
 * the product might be Ibuprofen, but the product instance is
 *
 * We only track product instances in the warehouse.
 *
 *
 */


class Item {

    Integer id;

    String sku		    // Is this appropriate or should we add an EAN?

    Product product

    

    static constraints = {
    }
}
