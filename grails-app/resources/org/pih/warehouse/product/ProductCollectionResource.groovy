package org.pih.warehouse.product

import static org.grails.jaxrs.response.Responses.*

import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.POST
import javax.ws.rs.core.Response

@Path('/api/product')
@Consumes(['application/xml','application/json'])
@Produces(['application/xml','application/json'])
class ProductCollectionResource {

    @POST
    Response create(Product dto) {
        created dto.save()
    }

    @GET
    Response readAll() {
        ok Product.findAll()
    }
    
    @Path('/{id}')
    ProductResource getResource(@PathParam('id') String id) {
        new ProductResource(id:id)
    }
        
}
