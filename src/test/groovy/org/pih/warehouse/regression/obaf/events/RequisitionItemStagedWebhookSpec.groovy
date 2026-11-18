package org.pih.warehouse.regression.obaf.events

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.ApiClientService
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.WebhookEventType
import org.pih.warehouse.core.WebhookPublisherService
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.requisition.RequisitionStatus

class RequisitionItemStagedWebhookSpec extends Specification implements ServiceUnitTest<WebhookPublisherService>, DataTest {

    ApiClientService apiClientService = Mock(ApiClientService)

    void setup() {
        service.apiClientService = apiClientService
        config.openboxes.n8n.enabled = true
        config.openboxes.n8n.endpoint.url = 'https://example.org/n8n'
    }

    private RequisitionItem stubItem(boolean locationWebhooks) {
        Location origin = Stub(Location) {
            supports(ActivityCode.ENABLE_WEBHOOKS) >> locationWebhooks
            getId() >> 'origin'
            getLocationNumber() >> 'FAC-1'
            getName() >> 'Facility'
        }
        Requisition requisition = Stub(Requisition) {
            getOrigin() >> origin
            getRequestNumber() >> 'REQ-1'
            getStatus() >> RequisitionStatus.PICKED
            getDeliveryTypeCode() >> DeliveryTypeCode.DEFAULT
        }
        Product product = Stub(Product) {
            getId() >> 'product'
            getProductCode() >> 'P-1'
        }
        return Stub(RequisitionItem) {
            getRequisition() >> requisition
            getIdentifier() >> 'LINE-1'
            getProduct() >> product
            getRequisitionItemType() >> RequisitionItemType.ORIGINAL
            getQuantity() >> 5
            calculateQuantityAllocated() >> 5
            calculateQuantityPicked() >> 5
            getPicklistItems() >> []
        }
    }

    @Unroll
    void 'requisitionItem.staged: location webhooks=#locationWebhooks posts #posts time(s)'() {
        given:
        RequisitionItem item = stubItem(locationWebhooks)

        when:
        service.publishRequisitionItemEvent(item, WebhookEventType.REQUISITION_ITEM_STAGED)

        then:
        posts * apiClientService.post('https://example.org/n8n', { Map p ->
            p.eventType == 'requisitionItem.staged' &&
                    p.requisitionNumber == 'REQ-1' &&
                    p.requisitionStatus == 'PICKED' &&
                    p.item.identifier == 'LINE-1' &&
                    p.item.productCode == 'P-1' &&
                    p.item.quantityPicked == 5 &&
                    p.item.outboundContainers == [] &&
                    p.metadata.facilityId == 'origin'
        }, _)

        where:
        locationWebhooks || posts
        false            || 0
        true             || 1
    }

    void 'null item is ignored'() {
        when:
        service.publishRequisitionItemEvent(null, WebhookEventType.REQUISITION_ITEM_STAGED)

        then:
        0 * apiClientService.post(*_)
        noExceptionThrown()
    }
}
