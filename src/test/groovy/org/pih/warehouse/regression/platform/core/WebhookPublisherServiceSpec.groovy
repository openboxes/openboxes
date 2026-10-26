package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.shipping.Shipment

class WebhookPublisherServiceSpec extends Specification implements ServiceUnitTest<WebhookPublisherService>, DataTest {

    ApiClientService apiClientService = Mock(ApiClientService)

    void setup() {
        service.apiClientService = apiClientService
    }

    @Unroll
    void 'shipped event: location webhooks=#locationWebhooks enabled=#enabled posts #posts time(s)'() {
        given:
        config.openboxes.webhook.enabled = enabled
        config.openboxes.webhook.endpoint.url = 'https://example.org/hook'
        Location origin = Stub(Location) {
            supports(ActivityCode.ENABLE_WEBHOOKS) >> locationWebhooks
            getId() >> 'origin'
        }
        Shipment shipment = Stub(Shipment) {
            getId() >> 'shipment'
            getOrigin() >> origin
            getDestination() >> Stub(Location) { getId() >> 'destination' }
            getShipmentItems() >> ([] as SortedSet)
        }

        when:
        service.publishShippedEvent(shipment)

        then:
        posts * apiClientService.post('https://example.org/hook', { it.type == 'shipment.shipped' && it.data.id == 'shipment' }, _)

        where:
        locationWebhooks | enabled || posts
        false            | true    || 0
        true             | false   || 0
        true             | true    || 1
    }

    void 'a failing endpoint does not break the caller'() {
        given:
        config.openboxes.webhook.enabled = true
        config.openboxes.webhook.endpoint.url = 'https://example.org/hook'
        apiClientService.post(*_) >> { throw new RuntimeException('endpoint down') }

        when:
        service.publishEvent([type: 'shipment.shipped'])

        then:
        noExceptionThrown()
    }
}
