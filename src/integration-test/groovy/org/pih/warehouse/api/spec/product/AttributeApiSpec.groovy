package org.pih.warehouse.api.spec.product

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.product.AttributeApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.product.AttributeTestBuilder
import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.product.Attribute

class AttributeApiSpec extends ApiSpec {

    @Autowired
    AttributeApiWrapper attributeApiWrapper

    @Shared
    Attribute productAttribute

    @Shared
    Attribute productSupplierAttribute

    @Shared
    Attribute inactiveProductAttribute

    @Override
    void setupData() {
        productAttribute = new AttributeTestBuilder().entityTypeCodes(EntityTypeCode.PRODUCT).build(true)
        productSupplierAttribute = new AttributeTestBuilder().entityTypeCodes(EntityTypeCode.PRODUCT_SUPPLIER).build(true)
        inactiveProductAttribute = new AttributeTestBuilder().entityTypeCodes(EntityTypeCode.PRODUCT).active(false).build(true)
    }

    @Override
    void cleanupData() {
        productAttribute?.delete()
        productSupplierAttribute?.delete()
        inactiveProductAttribute?.delete()
    }

    void 'list attributes should return active attributes of all entity types when entity type is not provided'() {
        expect:
        attributeApiWrapper.api.list([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItems(productAttribute.id, productSupplierAttribute.id))
                .build())
    }

    void 'list attributes should not return inactive attributes'() {
        expect:
        attributeApiWrapper.api.list([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(inactiveProductAttribute.id)))
                .build())
    }

    void 'list attributes should return attribute name and code'() {
        expect:
        attributeApiWrapper.api.list([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("data.find { it.id == '${productAttribute.id}' }.name".toString(), Matchers.equalTo(productAttribute.name))
                .expectBody("data.find { it.id == '${productAttribute.id}' }.code".toString(), Matchers.equalTo(productAttribute.code))
                .build())
    }

    void 'list attributes should return only product attributes when entity type is PRODUCT'() {
        expect:
        attributeApiWrapper.api.list([entityType: EntityTypeCode.PRODUCT], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(productAttribute.id))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(productSupplierAttribute.id)))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(inactiveProductAttribute.id)))
                .build())
    }

    void 'list attributes should return only product supplier attributes when entity type is PRODUCT_SUPPLIER'() {
        expect:
        attributeApiWrapper.api.list([entityType: EntityTypeCode.PRODUCT_SUPPLIER], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(productSupplierAttribute.id))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(productAttribute.id)))
                .build())
    }
}
