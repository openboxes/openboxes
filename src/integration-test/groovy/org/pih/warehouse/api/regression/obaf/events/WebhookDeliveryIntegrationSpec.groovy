package org.pih.warehouse.api.regression.obaf.events

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory
import spock.lang.PendingFeature

import org.pih.warehouse.api.regression.obaf.events.support.LoopbackReceiver
import org.pih.warehouse.api.regression.obaf.events.support.ObafEventsIntegrationSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.ApiClientService
import org.pih.warehouse.core.WebhookEventType
import org.pih.warehouse.core.WebhookPublisherService
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition

/**
 * WebhookPublisherService + ApiClientService on the wire, against a loopback receiver: the two
 * config paths (`openboxes.webhook`, the legacy shipped event; `openboxes.n8n`, every OBAF event),
 * their defaults, headers, what happens when the receiver is down, slow, or answers badly, and the
 * shape of the requisition payload (who it names).
 *
 * The event TRIGGERS (when each event is sent, after commit or not, duplicates) are in
 * RequisitionWebhookEventsIntegrationSpec and AdjustmentWebhookIntegrationSpec.
 */
class WebhookDeliveryIntegrationSpec extends ObafEventsIntegrationSpec {

    static final String ISO_8601 = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[+-]\d{2}:?\d{2})/
    static final String UUID_RE = /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/

    // ------------------------------------------------------------------ config defaults

    void 'disabled by default: both webhook paths ship enabled = false; the event path has no url and no headers'() {
        expect:
        grailsApplication.config.getProperty('openboxes.n8n.enabled', Boolean) == false
        grailsApplication.config.getProperty('openboxes.n8n.endpoint.url', String) == ''
        (grailsApplication.config.getProperty('openboxes.n8n.endpoint.headers', Map) ?: [:]).isEmpty()
        grailsApplication.config.getProperty('openboxes.webhook.enabled', Boolean) == false
    }

    void 'a disabled path posts nothing, even with a reachable url'() {
        given:
        LoopbackReceiver r = webhookReceiver()
        config.set('openboxes.n8n.enabled', false)

        when:
        webhookPublisherService.publishEvent([eventType: 'regression.test'], 'openboxes.n8n')

        then:
        r.hits.isEmpty()
    }

    // ------------------------------------------------------------------ the request

    void 'publishEvent POSTs the payload as UTF-8 JSON with the configured headers'() {
        given:
        LoopbackReceiver r = webhookReceiver(['X-Test-Token': 'tok-123'])

        when:
        webhookPublisherService.publishEvent([eventType: 'regression.test', name: 'Café ü Ω', n: 3], 'openboxes.n8n')

        then:
        r.hits.size() == 1
        r.hits[0].method == 'POST'
        r.hits[0].path == '/webhook/test'
        r.hits[0].headers['content-type'].startsWith('application/json')
        r.hits[0].headers['x-test-token'] == 'tok-123'
        payloads(r) == [[eventType: 'regression.test', name: 'Café ü Ω', n: 3]]
    }

    // ------------------------------------------------------------------ failures

    void 'receiver down (connection refused): publishEvent returns quietly at once and does not retry'() {
        given:
        LogCapture logs = LogCapture.of(WebhookPublisherService)
        config.set('openboxes.n8n.enabled', true)
        config.set('openboxes.n8n.endpoint.url', "http://127.0.0.1:${LoopbackReceiver.closedPort()}/webhook".toString())
        config.set('openboxes.n8n.endpoint.headers', [:])
        long t0 = System.currentTimeMillis()

        when:
        webhookPublisherService.publishEvent([eventType: 'regression.test'], 'openboxes.n8n')

        then:
        noExceptionThrown()
        System.currentTimeMillis() - t0 < 5000
        logs.errors().any { it.contains('Failed to publish webhook event') }

        cleanup:
        logs?.detach()
    }

    void 'characterization: a 500 answer is accepted as delivered: no exception, one attempt, nothing logged as failed'() {
        given:
        LoopbackReceiver r = webhookReceiver()
        r.status = 500
        r.responseBody = '{"message":"Error in workflow"}'
        LogCapture logs = LogCapture.of(WebhookPublisherService)

        when:
        webhookPublisherService.publishEvent([eventType: 'regression.test'], 'openboxes.n8n')

        then:
        noExceptionThrown()
        r.hits.size() == 1
        logs.errors().isEmpty()

        cleanup:
        logs?.detach()
    }

    @PendingFeature(reason = 'ApiClientService.groovy:65-86 never reads the response status: a receiver answering 4xx/5xx with a JSON (or empty) body counts as delivered: no retry, nothing logged; the payload carries metadata.attemptNumber = 1 but no attempt 2 exists')
    void 'a 5xx answer is treated as a failed delivery: retried, or at least logged as an error'() {
        given:
        LoopbackReceiver r = webhookReceiver()
        r.status = 503
        r.responseBody = '{"message":"Service unavailable"}'
        LogCapture logs = LogCapture.of(WebhookPublisherService, ApiClientService)

        when:
        webhookPublisherService.publishEvent([eventType: 'regression.test'], 'openboxes.n8n')

        then:
        r.hits.size() > 1 || !logs.errors().isEmpty()

        cleanup:
        logs?.detach()
    }

    void 'characterization: a 200 answer whose body is not JSON (a receiver answering plain text) is logged as a failed publish'() {
        given:
        LoopbackReceiver r = webhookReceiver()
        r.responseBody = 'OK'
        r.contentType = 'text/plain'
        LogCapture logs = LogCapture.of(WebhookPublisherService)

        when:
        webhookPublisherService.publishEvent([eventType: 'regression.test'], 'openboxes.n8n')

        then:
        noExceptionThrown()
        r.hits.size() == 1
        logs.errors().any { it.contains('Failed to publish webhook event') }

        cleanup:
        logs?.detach()
    }

    void 'an invalid url (enabled, url blank or not a URL) is logged, never thrown'() {
        given:
        config.set('openboxes.n8n.enabled', true)
        config.set('openboxes.n8n.endpoint.url', url)
        config.set('openboxes.n8n.endpoint.headers', [:])

        when:
        webhookPublisherService.publishEvent([eventType: 'regression.test'], 'openboxes.n8n')

        then:
        noExceptionThrown()

        where:
        url << ['', 'not a url', 'ftp://127.0.0.1:1/x']
    }

    // ------------------------------------------------------------------ the requisition payload

    void 'publishRequisitionEvent: the payload names the event, the order, each line and the facility'() {
        given:
        String dc = depot('WH-PAY', [ActivityCode.ENABLE_WEBHOOKS])
        String p1 = product('PAY1')
        String p2 = product('PAY2')
        String req = requisition(dc, [[productId: p1, quantity: 5], [productId: p2, quantity: 2]])
        Map codes = tx { [p1: Product.get(p1).productCode, p2: Product.get(p2).productCode] } as Map
        Map facility = tx { [name: org.pih.warehouse.core.Location.get(dc).name, number: org.pih.warehouse.core.Location.get(dc).locationNumber] } as Map
        String number = tx { Requisition.get(req).requestNumber } as String
        LoopbackReceiver r = webhookReceiver()

        expect:
        tx { Requisition.get(req).requisitionItems.size() } == 2

        when:
        tx { webhookPublisherService.publishRequisitionEvent(Requisition.get(req), WebhookEventType.PICK_STARTED) }
        tx { webhookPublisherService.publishRequisitionEvent(Requisition.get(req), WebhookEventType.PICK_STARTED) }

        then:
        List<Map> sent = events(r, 'pick.started', number)
        sent.size() == 2
        Map p = sent[0]
        p.keySet() == ['eventId', 'eventType', 'eventDate', 'triggeredBy', 'requisitionNumber', 'requisitionType',
                       'deliveryTypeCode', 'requestedBy', 'createdBy', 'lines', 'metadata'] as Set
        p.eventId ==~ UUID_RE
        sent[1].eventId != p.eventId
        p.requisitionNumber == number
        p.deliveryTypeCode == 'LOCAL_DELIVERY'
        p.lines.collect { [it.productCode, it.requisitionItemType, it.quantityRequested, it.quantityCanceled, it.quantityAllocated, it.quantityPicked] } as Set ==
                [[codes.p1, 'ORIGINAL', 5, 0, 0, 0], [codes.p2, 'ORIGINAL', 2, 0, 0, 0]] as Set
        p.lines.every { it.outboundContainers == [] && it.stagingLocations == [] && it.stagedBy == [] && it.dateStaged == [] && it.reasonCode == [] }
        p.metadata.facilityId == dc
        p.metadata.facilityCode == facility.number
        p.metadata.facilityName == facility.name
        p.metadata.webhookId ==~ UUID_RE
        p.metadata.attemptNumber == 1
    }

    void 'with openboxes.anonymize.enabled the people in the payload are masked (last initial, masked e-mail and username)'() {
        given:
        String dc = depot('WH-ANON', [ActivityCode.ENABLE_WEBHOOKS])
        String req = requisition(dc, [[productId: product('ANON'), quantity: 1]])
        String number = tx { Requisition.get(req).requestNumber } as String
        LoopbackReceiver r = webhookReceiver()
        config.set('openboxes.anonymize.enabled', true)

        when:
        tx { webhookPublisherService.publishRequisitionEvent(Requisition.get(req), WebhookEventType.PICK_STARTED) }
        Map p = events(r, 'pick.started', number)[0]

        then:
        p.createdBy.username ==~ /\*+/
        p.createdBy.email == null || p.createdBy.email ==~ /\*+/
        p.createdBy.lastName.size() <= 1
    }

    @PendingFeature(reason = 'WebhookPublisherService.groovy:94 sends the requisition events\' eventDate as Date.toString() ("Tue Oct 13 12:00:00 UTC 2026", the JVM zone, not machine-parseable), while adjustment.created and cycleCount.completed send ISO-8601 with an offset; a receiver needs two parsers')
    void 'requisition events carry an ISO-8601 eventDate, like the adjustment and cycle count events'() {
        given:
        String dc = depot('WH-DATE', [ActivityCode.ENABLE_WEBHOOKS])
        String req = requisition(dc, [[productId: product('DATE'), quantity: 1]])
        String number = tx { Requisition.get(req).requestNumber } as String
        LoopbackReceiver r = webhookReceiver()

        when:
        tx { webhookPublisherService.publishRequisitionEvent(Requisition.get(req), WebhookEventType.PICK_STARTED) }

        then:
        events(r, 'pick.started', number)[0].eventDate ==~ ISO_8601
    }

    void 'no ENABLE_WEBHOOKS at the origin, or no requisition at all: nothing is posted'() {
        given:
        String dc = depot('WH-OFF')
        String req = requisition(dc, [[productId: product('OFF'), quantity: 1]])
        LoopbackReceiver r = webhookReceiver()

        when:
        tx { webhookPublisherService.publishRequisitionEvent(Requisition.get(req), WebhookEventType.PICK_STARTED) }
        webhookPublisherService.publishRequisitionEvent(null, WebhookEventType.PICK_STARTED)

        then:
        r.hits.isEmpty()
    }

    // ------------------------------------------------------------------ log capture

    static class LogCapture {
        List<Logger> loggers = []
        ListAppender<ILoggingEvent> appender = new ListAppender<>()

        static LogCapture of(Class... classes) {
            LogCapture c = new LogCapture()
            c.appender.start()
            classes.each { Class k ->
                Logger l = (Logger) LoggerFactory.getLogger(k)
                l.addAppender(c.appender)
                c.loggers << l
            }
            return c
        }

        List<String> errors() {
            return appender.list.findAll { it.level.isGreaterOrEqual(Level.ERROR) }*.formattedMessage
        }

        void detach() {
            loggers.each { it.detachAppender(appender) }
        }
    }
}
