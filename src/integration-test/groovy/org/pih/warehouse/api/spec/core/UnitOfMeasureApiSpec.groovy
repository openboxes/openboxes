package org.pih.warehouse.api.spec.core

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.core.UnitOfMeasureApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.UnitOfMeasureTestBuilder
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureType

class UnitOfMeasureApiSpec extends ApiSpec {

    @Autowired
    UnitOfMeasureApiWrapper unitOfMeasureApiWrapper

    UnitOfMeasure currencyUom

    UnitOfMeasure quantityUom

    @Override
    void setupData() {
        currencyUom = new UnitOfMeasureTestBuilder().type(UnitOfMeasureType.CURRENCY).build(true)
        quantityUom = new UnitOfMeasureTestBuilder().type(UnitOfMeasureType.QUANTITY).build(true)
    }

    @Override
    void cleanupData() {
        UnitOfMeasure.get(currencyUom.id)?.delete()
        UnitOfMeasure.get(quantityUom.id)?.delete()
    }

    void 'get currencies should return unit of measures of currency type'() {
        expect:
        unitOfMeasureApiWrapper.api.currencies(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(currencyUom.id))
                .expectBody('data.code', Matchers.hasItem(currencyUom.code))
                .build())
    }

    void 'get currencies should not return unit of measures of other types'() {
        expect:
        unitOfMeasureApiWrapper.api.currencies(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(quantityUom.id)))
                .build())
    }

    void 'get uom options should return unit of measures of the given type'() {
        expect:
        unitOfMeasureApiWrapper.api.uomOptions(UnitOfMeasureType.QUANTITY.name(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(quantityUom.id))
                .expectBody('data.value', Matchers.hasItem(quantityUom.id))
                .expectBody('data.label', Matchers.hasItem(quantityUom.name))
                .build())
    }

    void 'get uom options should not return unit of measures of other types'() {
        expect:
        unitOfMeasureApiWrapper.api.uomOptions(UnitOfMeasureType.QUANTITY.name(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(currencyUom.id)))
                .build())
    }
}
