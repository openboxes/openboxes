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
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.hibernate.Criteria
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.User
import org.pih.warehouse.product.ProductAvailability

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

@SecurityRequirement(name = "cookie")
@Tag(name = "Location", description = "API for locations")
@Transactional
class LocationApiController extends BaseDomainApiController {

    def locationService
    def userService
    GrailsApplication grailsApplication

    class ListResponse
    {
        List<Location> data
    }

    @GET
    @Operation(
        summary = "get a list of locations",
        description = """\
## Warning!

Do _not_ use Swagger UI's "Try it out" feature on this entry point without setting `locationTypeCode`!

OpenBoxes tracks a large number of locations; the full list can
[make this page unresponsive](https://github.com/swagger-api/swagger-ui/issues/3832).""",
        operationId = "list_locations",
        parameters = [
            @Parameter(
                description = "optionally restrict the search to a particular type of location",
                in = ParameterIn.QUERY,
                name = "locationTypeCode",
                schema = @Schema(implementation = LocationTypeCode)
            )
        ]
    )
    @ApiResponse(
        content = @Content(
            schema = @Schema(implementation = ListResponse)
        ),
        description = "a list of locations",
        responseCode = "200"
    )
    @Path("/api/locations")
    @Produces("application/json")
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
