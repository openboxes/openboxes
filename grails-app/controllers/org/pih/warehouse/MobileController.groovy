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

import org.apache.commons.io.IOUtils
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.integration.AcceptanceStatusEvent
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSummary
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.xml.acceptancestatus.AcceptanceStatus

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

class MobileController {

    def userService
    def productService
    def inventoryService
    def locationService
    def megamenuService
    def stockMovementService
    def fileTransferService
    def grailsApplication

    def index = {

        Location location = Location.get(session.warehouse.id)
        def productCount = ProductSummary.countByLocation(location)
        def productListUrl = g.createLink(controller: "mobile", action: "productList")

        def orderCount = Order.createCriteria().count {
            eq("destination", location)
            orderType {
                eq("orderTypeCode", OrderTypeCode.PURCHASE_ORDER)
            }
        }

        def requisitionCount = Requisition.createCriteria().count {
            eq("origin", location)
        }

        def messageCount = fileTransferService.listMessages().size()
        [
                data: [
                        [name: "Inventory", class: "fa fa-box", count: productCount, url: g.createLink(controller: "mobile", action: "productList")],
                        [name: "Inbound Orders", class: "fa fa-shopping-cart", count: orderCount, url: g.createLink(controller: "order", action: "list", params: ['origin.id', location.id])],
                        [name: "Outbound Orders", class: "fa fa-truck", count: requisitionCount, url: g.createLink(controller: "mobile", action: "outboundList", params: ['origin.id', location.id])],
                        [name: "Messages", class: "fa fa-envelope", count: messageCount, url: g.createLink(controller: "mobile", action: "messageList", params: ['origin.id', location.id])],
                ]
        ]
    }

    def login = {

    }

    def menu = {
        Map menuConfig = grailsApplication.config.openboxes.megamenu
        //User user = User.get(session?.user?.id)
        //Location location = Location.get(session.warehouse?.id)
        //List translatedMenu = megamenuService.buildAndTranslateMenu(menuConfig, user, location)
        [menuConfig:menuConfig]
    }

    def chooseLocation = {
        User user = User.get(session.user.id)
        Location warehouse = Location.get(session.warehouse.id)
        render (view: "/mobile/chooseLocation",
            model: [savedLocations: user.warehouse ? [user.warehouse] : null, loginLocationsMap: locationService.getLoginLocationsMap(user, warehouse)])
    }

    def productList = {
        Location location = Location.get(session.warehouse.id)
        def terms = params?.q ? params?.q?.split(" ") : "".split(" ")
        def productSummaries = ProductSummary.createCriteria().list(max: params.max ?: 10, offset: params.offset ?: 0) {
            eq("location", location)
            order("product", "asc")
        }
        [productSummaries:productSummaries]
    }

    def productDetails = {
        Product product = Product.findByIdOrProductCode(params.id, params.id)
        Location location = Location.get(session.warehouse.id)
        def productSummary = ProductSummary.findByProductAndLocation(product, location)
        if (productSummary) {
            [productSummary: productSummary]
        }
        else {
            flash.message = "Product ${product.productCode} is not available in ${location.locationNumber}"
            redirect(action: "productList")
        }
    }

    def outboundList = {
        Location origin = Location.get(params.origin?params.origin.id:session.warehouse.id)
        StockMovement stockMovement = new StockMovement(origin: origin, stockMovementType: StockMovementType.OUTBOUND, stockMovementStatusCode: StockMovementStatusCode.PENDING)
        def stockMovements = stockMovementService.getStockMovements(stockMovement, [max:params.max?:10, offset: params.offset?:0])
        [stockMovements:stockMovements]
    }

    def messageList = {
        def messages = fileTransferService.listMessages()
        [messages:messages]
    }

    def messageDetails = {
        def content = fileTransferService.retrieveMessage(params.filename)
        //def xml = new XML(content)
        render text: content, contentType: 'text/xml', encoding: 'UTF-8'
        return
    }

    def messageUpload = {
        def messageFile = request.getFile('messageFile')
        if(messageFile) {
            String fileName = messageFile.originalFilename
            File file = new File ("/tmp/${fileName}")
            messageFile.transferTo(file)
            fileTransferService.storeMessage(file)
        }
    }

    def messageProcess = {

        // We need to move this into an integration service
        String xmlContent = fileTransferService.retrieveMessage(params.filename)
        InputStream xmlContents = IOUtils.toInputStream(xmlContent)
        JAXBContext jaxbContext = JAXBContext.newInstance(AcceptanceStatus.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        //TripExecution tripExecution = (TripExecution) unmarshaller.unmarshal(xmlContents);
        AcceptanceStatus acceptanceStatus = (AcceptanceStatus) unmarshaller.unmarshal(xmlContents);

        // Publish message to event bus
        log.info "publish to event bus"
        grailsApplication.mainContext.publishEvent(new AcceptanceStatusEvent(acceptanceStatus))

        flash.message = "Message has been processed"
        redirect(action: "messageList")

    }


}
