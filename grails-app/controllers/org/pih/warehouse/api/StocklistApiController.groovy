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
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.requisition.Requisition

/**
 * Should not extend BaseDomainApiController since stocklist is not a valid domain.
 */
class StocklistApiController {

    def requisitionService
    def stocklistService

    def list = {
        Requisition requisition = new Requisition(params)
        requisition.isTemplate = true
        requisition.isPublished = true
        List<Requisition> requisitions = requisitionService.getAllRequisitionTemplates(requisition, params)
        render([data: requisitions] as JSON)
    }

    def read = {
        Stocklist stocklist = stocklistService.getStocklist(params.id)

        if (!stocklist) {
            throw new ObjectNotFoundException(params.id, Stocklist.class.toString())
        }

        render([data: stocklist] as JSON)
    }

    def create = { Stocklist stocklist ->

        JSONObject jsonObject = request.JSON
        log.debug "create " + jsonObject.toString(4)

        stocklist = stocklistService.createStocklist(stocklist)

        response.status = 201
        render([data: stocklist] as JSON)
    }

    def update = {
        JSONObject jsonObject = request.JSON
        log.debug "update: " + jsonObject.toString(4)

        Stocklist stocklist = stocklistService.getStocklist(params.id)
        if (!stocklist) {
            stocklist = new Stocklist()
        }

        bindData(stocklist, jsonObject)
        stocklist = stocklistService.updateStocklist(stocklist)

        render([data: stocklist] as JSON)
    }

    def delete = {
        stocklistService.deleteStocklist(params.id)

        render status: 204
    }

    def sendMail = {
        JSONObject jsonObject = request.JSON
        log.debug "send mail: " + jsonObject.toString(4)
        def emailBody = jsonObject.text + "\n\n" + "Sent by " + session.user.name
        stocklistService.sendMail(params.id, jsonObject.subject, emailBody, jsonObject.recipients, jsonObject.includePdf, jsonObject.includeXls)

        render status: 200
    }
}
