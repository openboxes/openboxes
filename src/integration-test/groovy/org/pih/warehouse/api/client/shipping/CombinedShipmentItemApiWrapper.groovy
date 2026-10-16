package org.pih.warehouse.api.client.shipping

import groovy.transform.InheritConstructors
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper

@TestComponent
@InheritConstructors
class CombinedShipmentItemApiWrapper extends ApiWrapper<CombinedShipmentItemApi> {
}
