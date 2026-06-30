/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.stockTransfer

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.StockTransfer
import org.pih.warehouse.api.StockTransferItem
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.MailService
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UserService
import org.pih.warehouse.order.Order
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Transactional
class StockTransferEventService {

    GrailsApplication grailsApplication
    UserService userService
    MailService mailService

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onStockTransfer(StockTransferEvent event) {
        log.info "Application event ${event} has been published!"
        Order order = Order.get(event?.source)

        if (!order) {
            log.warn("Order not found; skipping notification.")
            return
        }

        StockTransfer stockTransfer = StockTransfer.createFromOrder(order)
        if (stockTransfer?.stockTransferItems?.any {
            (it.originBinLocation?.supports(ActivityCode.ENABLE_STOCK_TRANSFER_NOTIFICATIONS)
                    || it.destinationBinLocation?.supports(ActivityCode.ENABLE_STOCK_TRANSFER_NOTIFICATIONS))
        }) {
            sendStockTransferNotification(stockTransfer)
        }
    }

    private void sendStockTransferNotification(StockTransfer stockTransfer) {
        try {
            def recipientList = userService.findUsersByRoleType(RoleType.ROLE_STOCK_TRANSFER_NOTIFICATION).collect {
                it.email
            }
            if (recipientList) {
                def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
                // try to find StockTransferItem with destinationBinLocation that supports ENABLE_STOCK_TRANSFER_NOTIFICATIONS activity
                List<StockTransferItem> stockTransferItemWithSupportedLocations = stockTransfer?.stockTransferItems?.findAll { StockTransferItem it ->
                    (it.destinationBinLocation?.supports(ActivityCode.ENABLE_STOCK_TRANSFER_NOTIFICATIONS)
                            || it.originBinLocation?.supports(ActivityCode.ENABLE_STOCK_TRANSFER_NOTIFICATIONS))
                }
                // unique names of origin/destination bin locations that support stock transfer notifications
                List<String> uniqueBinNames = stockTransfer?.stockTransferItems
                        ?.collectMany { StockTransferItem it -> [it.originBinLocation, it.destinationBinLocation] }
                        ?.findAll { it?.supports(ActivityCode.ENABLE_STOCK_TRANSFER_NOTIFICATIONS) }
                        ?.collect { it.name }
                        ?.unique()
                String subject = g.message(code: 'email.stockTransfer.message.subject')
                def body = "${g.render(template: '/email/stockTransfer', model: [stockTransfer: stockTransfer, stockTransferItems: stockTransferItemWithSupportedLocations, uniqueBinNames: uniqueBinNames])}"
                mailService.sendHtmlMail(subject, body.toString(), recipientList)
            }
        }
        catch (Exception e) {
            log.error("Error sending stock transfer notification email: " + e.message, e)
        }
    }
}
