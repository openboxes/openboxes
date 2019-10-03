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

/**
 * Should not extend BaseDomainApiController since stocklistItem is not a valid domain.
 */
class StocklistItemApiController {

    def stocklistItemService

    def list = {
        String productId = params?.product?.id
        if (!productId) {
            throw new IllegalArgumentException("Must provide product.id as request parameter")
        }

        List<StocklistItem> stocklistItems = stocklistItemService.getStocklistItems(productId)
        render([data: stocklistItems] as JSON)
    }

    def read = {
        StocklistItem stocklistItem = stocklistItemService.getStocklistItem(params.id)

        if (!stocklistItem) {
            throw new ObjectNotFoundException(params.id, StocklistItem.class.toString())
        }

        render([data: stocklistItem] as JSON)
    }

    def create = { StocklistItem stocklistItem ->
        String productId = params?.product?.id
        if (!productId) {
            throw new IllegalArgumentException("Must provide product.id as request parameter")
        }

        JSONObject jsonObject = request.JSON
        log.debug "create " + jsonObject.toString(4)

        stocklistItem = stocklistItemService.createStocklistItem(stocklistItem, productId)

        response.status = 201
        render([data: stocklistItem] as JSON)
    }

    def update = {
        JSONObject jsonObject = request.JSON
        log.debug "update: " + jsonObject.toString(4)

        StocklistItem stocklistItem = stocklistItemService.getStocklistItem(params.id)
        if (!stocklistItem) {
            throw new IllegalArgumentException("No stocklist item fund with ID ${params.id}")
        }

        bindData(stocklistItem, jsonObject)
        stocklistItem = stocklistItemService.updateStocklistItem(stocklistItem)

        render([data: stocklistItem] as JSON)
    }

    def remove = {
        stocklistItemService.deleteStocklistItem(params.id)

        render status: 204
    }

    def availableStocklists = {
        def availableStocklists = stocklistItemService.getAvailableStocklists()
        render([data: availableStocklists] as JSON)
    }
}
