package org.pih.warehouse.api.regression.obaf.events

import org.springframework.transaction.TransactionStatus
import spock.lang.PendingFeature

import org.pih.warehouse.api.regression.obaf.events.support.ObafEventsIntegrationSpec
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec.PutawaySite
import org.pih.warehouse.api.regression.obaf.events.support.RecordingMailService
import org.pih.warehouse.api.regression.support.RegressionRoleFixtures
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductBarcodeUpdatedEvent
import org.pih.warehouse.stockTransfer.StockTransferEvent

/**
 * The two e-mail event services OBAF added, with the mail service recorded (nothing sent):
 * - StockTransferEventService (AFTER_COMMIT): a completed stock transfer e-mails the
 *   ROLE_STOCK_TRANSFER_NOTIFICATION users only when one of its bins has
 *   ENABLE_STOCK_TRANSFER_NOTIFICATIONS;
 * - ProductBarcodeUpdatedEventService (a plain ApplicationListener, synchronous): a UPC change
 *   e-mails the ROLE_PRODUCT_NOTIFICATION users with the old and new UPC;
 * - PutawayDiscrepancyEventService (also a plain ApplicationListener): completing a putaway task
 *   with "cancel remaining" e-mails the ROLE_DISCREPANCY_NOTIFICATION users.
 * The e-mail bodies are GSP templates rendered through the taglib, so the publishing thread has a
 * (mock) web request, as the HTTP request that publishes them in production does.
 */
class NotificationEventsIntegrationSpec extends ObafEventsIntegrationSpec {

    static final Date SOON = new Date() + 90

    String notified(RoleType role) {
        String username = "test-notify-${uid().toLowerCase()}".toString()
        RegressionRoleFixtures.ensureUser(username, [role], [:])
        return "${username}@example.com".toString()
    }

    /** A stock transfer order (one item, bin to bin) whose origin bin has the notification activity or not. */
    Map transferOrder(String prefix, boolean notifyingBin) {
        String dc = depot(prefix)
        String from = bin(dc, 'FROM', notifyingBin ? [ActivityCode.ENABLE_STOCK_TRANSFER_NOTIFICATIONS] : [])
        String to = bin(dc, 'TO')
        String p = product(prefix)
        stock(dc, p, [[from, 4, 'LST', SOON]])
        Map order = putawayOrder(dc, p, 'LST', 2, from, to)
        return order + [dc: dc]
    }

    // ------------------------------------------------------------------ stock transfer

    void 'a stock transfer touching a notifying bin e-mails the stock transfer recipients, after commit'() {
        given:
        String to = notified(RoleType.ROLE_STOCK_TRANSFER_NOTIFICATION)
        Map order = transferOrder('NT-ST', true)
        RecordingMailService mail = recordMail()

        when:
        withWebRequest {
            tx { grailsApplication.mainContext.publishEvent(new StockTransferEvent(order.orderId as String)) }
        }

        then:
        mail.sent.size() == 1
        to in mail.sent[0].to
        mail.sent[0].body.contains(tx { Order.get(order.orderId).orderNumber } as String)
    }

    void 'a stock transfer with no notifying bin e-mails nobody'() {
        given:
        notified(RoleType.ROLE_STOCK_TRANSFER_NOTIFICATION)
        Map order = transferOrder('NT-STN', false)
        RecordingMailService mail = recordMail()

        when:
        withWebRequest {
            tx { grailsApplication.mainContext.publishEvent(new StockTransferEvent(order.orderId as String)) }
        }

        then:
        mail.sent.isEmpty()
    }

    void 'a stock transfer event in a transaction that rolls back e-mails nobody'() {
        given:
        notified(RoleType.ROLE_STOCK_TRANSFER_NOTIFICATION)
        Map order = transferOrder('NT-STRB', true)
        RecordingMailService mail = recordMail()

        when:
        withWebRequest {
            Order.withNewTransaction { TransactionStatus st ->
                grailsApplication.mainContext.publishEvent(new StockTransferEvent(order.orderId as String))
                st.setRollbackOnly()
            }
        }

        then:
        mail.sent.isEmpty()
    }

    void 'an unknown order id is skipped quietly'() {
        given:
        RecordingMailService mail = recordMail()

        when:
        tx { grailsApplication.mainContext.publishEvent(new StockTransferEvent('no-such-order')) }

        then:
        noExceptionThrown()
        mail.sent.isEmpty()
    }

    // ------------------------------------------------------------------ product barcode

    void 'a UPC change e-mails every product notification recipient with the old and the new UPC'() {
        given:
        String to = notified(RoleType.ROLE_PRODUCT_NOTIFICATION)
        String p = product('NT-UPC')
        String name = tx { Product.get(p).name } as String
        RecordingMailService mail = recordMail()

        when:
        withWebRequest {
            tx {
                Product product = Product.get(p)
                grailsApplication.mainContext.publishEvent(new ProductBarcodeUpdatedEvent(User.findByUsername('admin'), product, 'OLD-UPC-1', 'NEW-UPC-2'))
            }
        }

        then: 'one e-mail per recipient (admin holds the role too in the seed data)'
        mail.sent.every { it.to.size() == 1 }
        mail.sent*.to.flatten().count { it == to } == 1
        RecordingMailService.Mail ours = mail.sent.find { to in it.to }
        ours.subject.contains(name)
        ours.body.contains('OLD-UPC-1')
        ours.body.contains('NEW-UPC-2')
    }

    @PendingFeature(reason = 'ProductBarcodeUpdatedEventService.groovy:12 is a plain ApplicationListener, so the e-mail goes out synchronously inside the UPC update\'s transaction, before commit: an update that is then rolled back has still told every product manager the barcode changed (StockTransferEventService uses AFTER_COMMIT)')
    void 'a UPC change that is rolled back e-mails nobody'() {
        given:
        notified(RoleType.ROLE_PRODUCT_NOTIFICATION)
        String p = product('NT-UPCRB')
        RecordingMailService mail = recordMail()

        when:
        withWebRequest {
            Product.withNewTransaction { TransactionStatus st ->
                Product product = Product.get(p)
                product.upc = 'RB-UPC'
                product.save(flush: true)
                grailsApplication.mainContext.publishEvent(new ProductBarcodeUpdatedEvent(User.findByUsername('admin'), product, null, 'RB-UPC'))
                st.setRollbackOnly()
            }
        }

        then:
        tx { Product.get(p).upc } == null
        mail.sent.isEmpty()
    }

    // ------------------------------------------------------------------ putaway discrepancy

    void 'a putaway completed with "cancel remaining" e-mails each discrepancy recipient once; a plain complete e-mails nobody'() {
        given:
        String to = notified(RoleType.ROLE_DISCREPANCY_NOTIFICATION)
        PutawaySite s = putawaySite('NT-DISC')
        Map discrepant = putawayOrderOf(s.dc, s.product, s.lotId, [[4, s.recv, s.stor1]])
        Map plain = putawayOrderOf(s.dc, s.product, s.lotId, [[3, s.recv, s.stor1]])
        RecordingMailService mail = recordMail()

        when:
        Map ok = withWebRequest { putawayPatch(plain.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true]) } as Map

        then:
        ok.error == null
        mail.sent.isEmpty()

        when:
        Map r = withWebRequest {
            putawayPatch(discrepant.itemIds[0], [action: 'complete', destination: s.stor1, isCancelRemaining: true,
                                                 reasonCode: 'PUTAWAY_DISCREPANCY', isDirectPutaway: true])
        } as Map

        then:
        r.error == null
        mail.sent*.to.flatten().count { it == to } == 1
    }
}
