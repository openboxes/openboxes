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

class BreadcrumbTagLib {

    def breadcrumb = { attrs, body ->

        def breadcrumb = "<a class=\"home\" href=\"${createLink(uri: '/home/index')}\">Home</a>"

        if (session.user) {
            breadcrumb += "&nbsp; &raquo; &nbsp;"
            breadcrumb += "<a class=\"building\" href=\"${createLink(uri: '${request.contextPath}/show/' + session.warehouse?.id)}\">${session.warehouse?.name}</a>"
            def baseUrl = "/"
            def currentUrl = baseUrl
            request.getServletPath().split("/").each {
                if (it != '') {
                    currentUrl += it
                    breadcrumb += "&nbsp; &raquo; &nbsp;"
                    // TODO figure out the correct URL to match with each part of the servlet path
                    // It's a bit tricky so we're just going to display the breadcrumb i18n message
                    breadcrumb += message(code: "breadcrumb." + it + ".label")
                }
            }

        } else {
            breadcrumb += "&nbsp; &raquo; &nbsp;"
            breadcrumb += "(unknown warehouse)"
        }
        out << "" + breadcrumb


    }

}
