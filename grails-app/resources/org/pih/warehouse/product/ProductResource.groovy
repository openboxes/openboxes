package org.pih.warehouse.product

import static org.grails.jaxrs.response.Responses.*

import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.PUT
import javax.ws.rs.core.Response

import org.grails.jaxrs.provider.DomainObjectNotFoundException

@Consumes(['application/xml','application/json'])
@Produces(['application/xml','application/json'])
class ProductResource {
    
    def id
    
    @GET
    Response read() {
        def obj = Product.get(id)
        if (!obj) {
            throw new DomainObjectNotFoundException(Product.class, id)
        }
        ok obj
    }
    
    @PUT
    Response update(Product dto) {
        def obj = Product.get(id)
        if (!obj) {
            throw new DomainObjectNotFoundException(Product.class, id)
        }
        obj.properties = dto.properties 
        ok obj
    }
    
    @DELETE
    void delete() {
        def obj = Product.get(id)
        if (obj) { 
            obj.delete()
        }
    }
    
}

