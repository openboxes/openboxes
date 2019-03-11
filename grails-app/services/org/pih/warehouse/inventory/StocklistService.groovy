/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.inventory

import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.requisition.Requisition

class StocklistService {

    def requisitionService
    def locationService
    def mailService
    def pdfRenderingService

    boolean transactional = true

    Stocklist getStocklist(String id) {
        Requisition requisition = Requisition.findByIdAndIsTemplate(id, true)

        if (!requisition) {
            return null
        }

        return Stocklist.createFromRequisition(requisition)
    }

    Stocklist createStocklist(Stocklist stocklist) {
        Requisition requisition = new Requisition()
        requisition.isTemplate = true
        requisition.name = stocklist.name
        requisition.destination = stocklist.destination
        requisition.origin = stocklist.origin
        requisition.requestedBy = stocklist.requestedBy

        requisition = requisitionService.saveTemplateRequisition(requisition)

        return Stocklist.createFromRequisition(requisition)
    }

    Stocklist updateStocklist(Stocklist stocklist) {
        Requisition requisition = stocklist.requisition
        requisition.name = stocklist.name
        requisition.destination = stocklist.destination
        requisition.origin = stocklist.origin
        requisition.requestedBy = stocklist.requestedBy

        requisition = requisitionService.saveTemplateRequisition(requisition)

        return Stocklist.createFromRequisition(requisition)
    }

    void deleteStocklist(String id) {
        Requisition requisition = Requisition.findByIdAndIsTemplate(id, true)
        if (!requisition) {
            throw new IllegalArgumentException("No Stock List found with ID ${id}")
        }

        requisitionService.deleteRequisition(requisition)
    }

    void sendMail(String stocklistId, String subject, String body, Collection to) {
        Stocklist stocklist = getStocklist(stocklistId)
        ByteArrayOutputStream bytes = pdfRenderingService.render(template: "/stocklist/print", model: [stocklist: stocklist])
        mailService.sendHtmlMailWithAttachment(to, subject, body, bytes.toByteArray(), "Stocklist - ${stocklist?.requisition?.name}.pdf", "application/pdf")
    }
}
