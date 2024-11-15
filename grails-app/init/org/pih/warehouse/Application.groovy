package org.pih.warehouse

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.annotation.ComponentScan

// Searches for Spring components (not just Grails ones), which allows us to define beans outside of the "/grails-app"
// folder as long as those beans are still under the "org.pih.warehouse" package. This is useful for things like
// defining Spring Components under the "/src" folder.
// https://tedvinke.wordpress.com/2017/04/04/grails-anti-pattern-everything-is-a-service/
@ComponentScan
// Searches for components tagged with the "WebFilter", "WebListener", and "WebServlet" Servlet 3.0 annotations.
@ServletComponentScan
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
