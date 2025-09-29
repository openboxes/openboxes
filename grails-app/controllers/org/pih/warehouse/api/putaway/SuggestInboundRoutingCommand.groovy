package org.pih.warehouse.api.putaway

import grails.databinding.BindUsing
import grails.databinding.DataBindingSource
import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class SuggestInboundRoutingCommand implements Validateable {

    // Should be bound from the URL
    Location facility

    @BindUsing({ SuggestInboundRoutingCommand obj, DataBindingSource src ->
        String identifier = src['product']
        Product product = Product.find('from Product p where p.id = :id or p.productCode = :productCode',
                [id: identifier, productCode: identifier])
        if (identifier && !product) {
            obj.errors.rejectValue("product", "notFound", [identifier].toArray(), "Product {0} could not be found")
        }
        return product
    })
    Product product


    static constraints = {
        product nullable: false
        facility nullable: false
    }
}