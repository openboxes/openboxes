/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.hibernate.Criteria
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.ProductAvailability

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

@Transactional
class LocationApiController extends BaseDomainApiController {

    def locationService
    def userService
    GrailsApplication grailsApplication

    // @Path("/api/locations")  // raises jackson errors if used with other annotations, see OBDS-73 for details
    @GET
    @Operation(
            summary = "Get locations",
            description = "Get list of locations",
            responses = [
                    @ApiResponse(
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation=Location.class)
                            )
                    ),
                    @ApiResponse(responseCode="400", description="Invalid ID supplied"),
                    @ApiResponse(responseCode="404", description="Not found")
            ]
    )
    @Produces("text/json")
    def list() {
        def minLength = grailsApplication.config.openboxes.typeahead.minLength
        if (params.name && params.name.size() < minLength) {
            render([data: []])
            return
        }

        Location currentLocation = Location.get(session?.warehouse?.id)
        User currentUser = User.get(session?.user?.id)
        boolean isSuperuser = userService.isSuperuser(session?.user)
        String direction = params?.direction
        def fields = params.fields ? params.fields.split(",") : null
        def locations = locationService.getLocations(fields, params, isSuperuser, direction, currentLocation, currentUser)
        render ([data:locations] as JSON)
     }


    def productSummary = {
        Location currentLocation = Location.load(session.warehouse.id)
        def data = ProductAvailability.createCriteria().list {
            resultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
            projections {
                product {
                    groupProperty("id", "productId")
                    groupProperty("name", "productName")
                    groupProperty("productCode", "productCode")
                }
                sum("quantityOnHand", "quantityOnHand")
            }
            eq("location", currentLocation)
        }
        render ([data:data] as JSON)
    }
}
