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

import fr.w3blog.zpl.utils.ZebraUtils
import grails.converters.JSON
import groovyx.net.http.HTTPBuilder
import org.hibernate.Criteria
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.ProductAvailability

class LocationApiController extends BaseDomainApiController {

    def locationService
    def userService
    def grailsApplication
    def templateService


    def list = {

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

    def read = {
        Location location = locationService.getLocation(params.id)
        render ([data:location] as JSON)
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

    def renderLabel = {
        Location location = locationService.getLocation(params.id)
        Document document = Document.get(params.documentId?:"ff8081817c7d2003017c7d21d20a0002")  // FIXME get ids from params or session
        if (!document) {
            throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
        }
        Map model = [location: location]
        String body = templateService.renderTemplate(document, model)
        String renderApiUrl = grailsApplication.config.openboxes.barcode.labelaryApi.url
        def http = new HTTPBuilder(renderApiUrl)
        def html = http.post(body: body)
        response.contentType = "image/png"
        response.outputStream << html
    }

    def printLabel = {
        try {
            Location location = locationService.getLocation(params.id)

            // FIXME OBKN-98 Temporarily default to first barcode template if documentId is not provided
            Document document = (params.documentId) ?
                    Document.get(params.documentId) :
                    Document.get("ff8081817c7d2003017c7d21d20a0002") // FIXME get ids from params or session
            if (!document) {
                throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
            }
            Map model = [location: location]
            String renderedContent = templateService.renderTemplate(document, model)
            String ipAddress = grailsApplication.config.openboxes.barcode.printer.ipAddress
            Integer port = grailsApplication.config.openboxes.barcode.printer.port
            log.info "Printing ${renderedContent} to ${ipAddress}:${port}"
            ZebraUtils.printZpl(renderedContent, ipAddress, port)
            render([data: "Location label has been printed to ${ipAddress}:${port}"] as JSON)
            return
        } catch (Exception e) {
            render([errorCode: 500, cause: e?.class, errorMessage: e?.message] as JSON)
        }
    }
}
