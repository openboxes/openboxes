package org.pih.warehouse.api.putaway

import grails.databinding.BindUsing
import grails.databinding.DataBindingSource
import grails.validation.Validateable
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.springframework.validation.Errors

class SearchPutawayTaskCommand implements Validateable {

    StatusCategory statusCategory
    List<PutawayTaskStatus> status

    // Should be bound from the URL
    Location facility

    @BindUsing({ SearchPutawayTaskCommand obj, DataBindingSource src ->
        String identifier = src['product']
        // Used in constraints to validate whether an identifier was provided when a product is not found
        obj.productIdentifier = identifier
        if (!identifier) return null
        Product product = Product.find('from Product p where p.id = :id or p.productCode = :productCode',
                [id: identifier, productCode: identifier])
        if (identifier && !product) {
            obj.errors.rejectValue("product", "notFound", [identifier].toArray(), "Product {0} could not be found")
        }
        return product
    })
    Product product
    String productIdentifier

    @BindUsing({ SearchPutawayTaskCommand obj, DataBindingSource src ->
        String identifier = src['container']
        // Used in constraints to validate whether an identifier was provided when a container is not found
        obj.containerIdentifier = identifier
        if (!identifier) return null
        Location container = Location.findByIdOrLocationNumber(identifier, identifier)
        if (identifier && !container) {
            obj.errors.rejectValue("container", "notFound", [identifier].toArray(), "Container {0} could not be found")
        }
        return container
    })
    Location container
    String containerIdentifier

    static constraints = {
        status nullable: true
        statusCategory nullable: true
        facility nullable: true
        productIdentifier nullable: true
        containerIdentifier nullable: true
        product nullable: true, validator: { Product val, obj, Errors errors ->
            if (obj.productIdentifier && !val) {
                errors.rejectValue('product', 'notFound',
                        [obj.productIdentifier] as Object[], "Product {0} not found")
                return false
            }
            return true
        }
        container nullable: true, validator: { Location val, obj, Errors errors ->
            if (obj.containerIdentifier && !val) {
                errors.rejectValue('container', 'notFound',
                        [obj.containerIdentifier] as Object[], "Container {0} not found")
                return false
            }
            return true
        }
    }
}