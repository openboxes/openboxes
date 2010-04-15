package org.pih.warehouse

class StockCard {

    Integer id
    Product product

    static hasMany = [ entries:StockCardEntry ]
    static belongsTo = [Product]
    
    static constraints = {
	product(blank:false, unique:true)
	
    }
}
