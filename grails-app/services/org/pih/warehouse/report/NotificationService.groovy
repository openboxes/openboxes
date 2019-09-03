/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.report

import org.codehaus.groovy.grails.plugins.web.taglib.RenderTagLib
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils

class NotificationService {

    def dataService
    def userService
    def mailService

    boolean transactional = false

    def getExpiryAlertsByLocation(Location location, Integer daysUntilExpiry = 0) {
        String query = """
            select * 
            from product_inventory_extended_expiry_view 
            where days_until_expiry <= ${daysUntilExpiry} 
            and facility_id = '${location.id}'
            and quantity_on_hand > 0
            order by days_until_expiry asc
            """
        return dataService.executeQuery(query)
    }

    def sendExpiryAlertsByLocation(Location location, Integer daysUntilExpiry) {
        def expiryAlerts = getExpiryAlertsByLocation(location, daysUntilExpiry)
        def subscribers = userService.findUsersByLocationRole(location, RoleType.ROLE_ITEM_ALL_NOTIFICATION)
        log.info "Sending ${expiryAlerts.size()} alerts and ${subscribers.size()} subscribers for location ${location} "

        // Render template
        //String templateContent = "${'<h1>Expiry Alert</h1>${data}'}"
        //String body = templateService.renderTemplate(templateContent, "Expiry Alerts", [data:expiryAlerts])
        //def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')

        // FIXME Need to fix this when we migrate to grails 3
        // Hack to ensure that the GSP template engine has access to a request.
        def webRequest = RequestContextHolder.getRequestAttributes()
        if(!webRequest) {
            def servletContext = ServletContextHolder.getServletContext()
            def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
            grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
        }

        def expiring = expiryAlerts.findAll {it.days_until_expiry > 0}
        def expired = expiryAlerts.findAll {it.days_until_expiry <= 0}
        def renderTagLib = new RenderTagLib()
        String body = renderTagLib.render(template: "/email/expiryAlerts",
                model: [location: location, expiring: expiring, expired:expired, daysUntilExpiry: daysUntilExpiry])

        // Send email
        if (subscribers) {
            Collection toList = subscribers.collect { it.email }.findAll{ it != null }.toArray()
            mailService.sendHtmlMail("Expiry Alerts", "${body}",  toList)
        }


    }
}
