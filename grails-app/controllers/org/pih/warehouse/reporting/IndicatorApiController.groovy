/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.reporting
import grails.converters.JSON
import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.report.ReportService

class IndicatorApiController {

    ReportService reportService

    static scaffold = Indicator

    // Proof of concept to see if we could evalute a string of code
    // Could be used to create dynamic indicators for the dashboard
    def evaluate() {

        String code = """
            import Product;
            def products = Product.list();
            return products.size()
        """

        // String code, boolean captureStdout, request
        render consoleService.eval(code, true, request)
    }

    def getTotalCount(IndicatorApiCommand command) {
        Map data = reportService.getTotalCount(command)
        render([data: data] as JSON)
    }

    def getItemsCounted(IndicatorApiCommand command) {
        Map data = reportService.getItemsCounted(command)
        render([data: data] as JSON)
    }

    def getTargetProgress(IndicatorApiCommand command) {
        Map data = reportService.getTargetProgress(command)
        render([data: data] as JSON)
    }

    def getNotFinishedItems(IndicatorApiCommand command) {
        Map data = reportService.getNotFinishedItems(command)
        render([data: data] as JSON)
    }
}

