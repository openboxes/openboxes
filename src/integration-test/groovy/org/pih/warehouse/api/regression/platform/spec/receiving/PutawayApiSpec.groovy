package org.pih.warehouse.api.regression.platform.spec.receiving

import spock.lang.PendingFeature

import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.receiving.PutawayApi

/**
 * Putting received stock away: putaways, putaway items and candidate locations that do not exist.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known response POST /partialReceiving/.*',
])
class PutawayApiSpec extends ReceivingSpec {

    @Autowired
    PutawayApi putawayApi

    @PendingFeature(reason = 'unknown putaways answer 500, not 404')
    void 'putaways, putaway items and locations that do not exist are a 404'() {
        expect:
        putawayApi.read('no-such-putaway', [:], responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        putawayApi.removeItem('no-such-item', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        putawayApi.candidates(['location.id': 'no-such-location'], responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }
}
