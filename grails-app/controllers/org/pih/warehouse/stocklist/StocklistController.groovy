/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse.stocklist

import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryLevel

class StocklistController {

    def stocklistService
    def documentService

    def show = {
        println "stocklist " + params
        def location = Location.get(params.id)
        def inventoryLevels = InventoryLevel.findAllByInventory(location.inventory)

        [location: location, inventoryLevels: inventoryLevels]
    }


    def renderHtml = {
        Stocklist stocklist = stocklistService.getStocklist(params.id)
        render(
                template: "/stocklist/print",
                model: [stocklist: stocklist])
    }

    def renderPdf = {
        Stocklist stocklist = stocklistService.getStocklist(params.id)

        renderPdf(
                template: "/stocklist/print",
                model: [stocklist: stocklist],
                filename: "Stocklist - ${stocklist?.requisition?.name}.pdf"
        )
    }

    def generateCsv = {
        Stocklist stocklist = stocklistService.getStocklist(params.id)

        render ""

        def filename = "Stocklist - " + stocklist?.requisition?.name + ".xls"

        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")

        log.info response
        documentService.generateStocklistCsv(response.outputStream, stocklist)
    }

    def sendMail = {
        Stocklist stocklist = stocklistService.getStocklist(params.id)

        if (!params.recipients || !params.id || !params.body || !params.subject) {
            flash.error = "${warehouse.message(code: 'email.noParams.message')}"
            redirect(controller: "requisitionTemplate", action: "sendMail", params: [id: params.id])
        } else if (!Arrays.asList(params.recipients).contains(stocklist.requestedBy.email)) {
            flash.error = "${warehouse.message(code: 'stockList.noManagerSelected.label')}"
            redirect(controller: "requisitionTemplate", action: "sendMail", params: [id: params.id])
        } else {
            def emailBody = params.body + "\n\n" + "Sent by " + session.user.name
            stocklistService.sendMail(params.id, params.subject, emailBody, [params.recipients], params.includePdf == "on", params.includeXls == "on")
            flash.message = "${warehouse.message(code: 'email.sent.message')}"
            redirect(controller: "requisitionTemplate", action: "show", params: [id: params.id])
        }
    }
}
