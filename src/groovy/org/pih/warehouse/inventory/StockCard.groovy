package org.pih.warehouse.inventory

import org.pih.warehouse.Product

class StockCard {

    Integer id
    Product product
    SortedSet items

    static hasMany = [ items : StockCardItem]
    static belongsTo = [Product]
    
    static constraints = {
		product(blank:false, unique:true)	
    }
}
