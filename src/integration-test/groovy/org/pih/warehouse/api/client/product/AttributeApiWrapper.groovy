package org.pih.warehouse.api.client.product

import groovy.transform.InheritConstructors
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper

@TestComponent
@InheritConstructors
class AttributeApiWrapper extends ApiWrapper<AttributeApi> {
}
