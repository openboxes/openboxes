package org.pih.warehouse.api.putaway

import grails.databinding.BindUsing
import grails.databinding.DataBindingSource
import grails.validation.Validateable
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.springframework.validation.Errors

class SearchPutawayTaskCommand implements Validateable {

    StatusCategory statusCategory
    List<PutawayTaskStatus> status

    // Should be bound from the URL
    Location facility

    // Fuzzy search by product code (prefix), name, or description (substring)
    String searchTerm

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

    @BindUsing({ SearchPutawayTaskCommand obj, DataBindingSource src ->
        String identifier = src['destination']
        obj.destinationIdentifier = identifier
        if (!identifier) return null
        Location destination = Location.findByIdOrLocationNumber(identifier, identifier)
        if (identifier && !destination) {
            obj.errors.rejectValue("destination", "notFound", [identifier].toArray(), "Destination {0} could not be found")
        }
        return destination
    })
    Location destination
    String destinationIdentifier

    @BindUsing({ SearchPutawayTaskCommand obj, DataBindingSource src ->
        String identifier = src['order']
        obj.orderIdentifier = identifier
        if (!identifier) return null

        Order order = Order.find('from Order o where o.id = :id or o.orderNumber = :orderNumber',
                [id: identifier, orderNumber: identifier])

        if (identifier && !order) {
            obj.errors.rejectValue("order", "notFound", [identifier].toArray(), "Order {0} could not be found")
        }
        return order
    })
    Order order
    String orderIdentifier

    static constraints = {
        status nullable: true
        statusCategory nullable: true
        facility nullable: true
        searchTerm nullable: true
        productIdentifier nullable: true
        containerIdentifier nullable: true
        destinationIdentifier nullable: true
        order nullable: true
        orderIdentifier nullable: true
        destination nullable: true, validator: { Location val, obj, Errors errors ->
            if (obj.destinationIdentifier && !val) {
                errors.rejectValue('destination', 'notFound',
                        [obj.destinationIdentifier] as Object[], "Destination {0} not found")
                return false
            }
            return true
        }
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