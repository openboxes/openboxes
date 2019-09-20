/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

import com.google.zxing.BarcodeFormat
import org.pih.warehouse.core.Location

class ImageTagLib {

    def grailsApplication

    // Cannot cache logo because it might change between locations
    def displayLogo = { attrs, body ->

        // For the main logo, we want the logo config to be used as the default
        // and allow location logo to override
        def logoUrl = grailsApplication.config.openboxes.logo.url
        def logoLabel = grailsApplication.config.openboxes.logo.label

        // Use custom location logo if one exists
        Location location = Location.get(session?.warehouse?.id)
        if (location?.logo) {
            logoUrl = "${createLink(controller: 'location', action: 'viewLogo', id: session?.warehouse?.id)}"
        }
        attrs.logoUrl = logoUrl
        attrs.logoLabel = logoLabel
        attrs.showLabel = (attrs.showLabel != null) ? attrs.showLabel : true

        out << g.render(template: '/taglib/displayLogo', model: [attrs: attrs])
    }


    def displayReportLogo = { attrs, body ->

        // For the report logo we'll use the logo config unless there's no logo,
        // then we'll try to use the location logo
        attrs.logoUrl = grailsApplication.config.openboxes.report.logo.url
        attrs.showLabel = (attrs.showLabel != null) ? attrs.showLabel : true

        out << g.render(template: '/taglib/displayLogo', model: [attrs: attrs])
    }


    def displayBarcode = { attrs, body ->

        def defaultFormat = grailsApplication.config.openboxes.identifier.barcode.format
        if (!attrs.format && defaultFormat) {
            try {
                attrs.format = BarcodeFormat.valueOf(defaultFormat)
            } catch (Exception e) {
                println("Unable to locate default barcode format ${defaultFormat}")
            }
        }

        def defaultHeight = grailsApplication.config.openboxes.identifier.barcode.height
        if (!attrs.height && defaultHeight) {
            try {
                attrs.height = Integer.parseInt(defaultHeight)
            } catch (Exception e) {
                println("Unable to parse default barcode height ${defaultHeight}")
            }
        }

        def defaultWidth = grailsApplication.config.openboxes.identifier.barcode.width
        if (!attrs.width && defaultWidth) {
            try {
                attrs.width = Integer.parseInt(defaultWidth)
            } catch (Exception e) {
                println("Unable to parse default barcode width ${defaultWidth}")
            }
        }

        attrs.showData = (attrs.showData != null) ? attrs.showData : true


        out << g.render(template: '/taglib/displayBarcode', model: [attrs: attrs])

    }
}
