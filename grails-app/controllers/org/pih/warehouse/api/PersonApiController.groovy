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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.pih.warehouse.core.Person

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

@SecurityRequirement(name="cookie")
@Tag(name="User", description="API for users")
class PersonApiController extends BaseDomainApiController {

    def userService

    @GET
    @Operation(
            summary = "Get a list of OpenBoxes users, as well as points of contact",
            description = """\
## Warning!

Do _not_ use Swagger's "Try it out" feature on this entry point!

OpenBoxes tracks a large number of users; the full list can
[make this page unresponsive](https://github.com/swagger-api/swagger-ui/issues/3832).""")
    @ApiResponse(
            content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(implementation=Person),
                            uniqueItems = true
                    ),
                    mediaType = "application/json"
            ),
            responseCode="200"
    )
    @Path("/api/persons")
    @Produces("text/json")
    def list() {
        String[] terms = params?.name?.split(",| ")?.findAll { it }
        def people = userService.findPersons(terms)
        render([data: people] as JSON)
    }
}
