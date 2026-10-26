package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.core.*
import grails.core.GrailsApplication
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import org.pih.warehouse.api.regression.platform.client.core.ReferenceDataApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.requisition.RequisitionType

// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ReferenceDataApiSpec extends RegressionApiSpec {

    @Autowired
    ReferenceDataApi referenceDataApi

    @Autowired
    GrailsApplication grailsApplication

    @Unroll
    void 'reason codes for #activityCode are the #listName list, in its order'() {
        expect:
        referenceDataApi.reasonCodes(activityCode ? [activityCode: activityCode.name()] : [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(expected*.name()))
                .expectBody('data.name', Matchers.everyItem(Matchers.not(Matchers.isEmptyOrNullString())))
                .build())

        where:
        activityCode                             | listName                        || expected
        ActivityCode.SUBSTITUTE_REQUISITION_ITEM | 'requisition substitution'      || ReasonCode.listRequisitionSubstitutionReasonCodes()
        ActivityCode.MODIFY_REQUISITION_ITEM     | 'requisition quantity change'   || ReasonCode.listRequisitionQuantityChangeReasonCodes()
        ActivityCode.ADJUST_INVENTORY            | 'inventory adjustment'          || ReasonCode.listInventoryAdjustmentReasonCodes()
        ActivityCode.CYCLE_COUNT                 | 'cycle count'                   || ReasonCode.listCycleCountReasonCodes()
        null                                     | 'default'                       || ReasonCode.listDefault()
    }

    void 'a reason code by its id has its localized name and sort order'() {
        given:
        ReasonCode code = ReasonCode.listCycleCountReasonCodes().first()

        expect:
        referenceDataApi.reasonCode(code.name(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(code.name()))
                .expectBody('data.name', Matchers.not(Matchers.isEmptyOrNullString()))
                .expectBody('data.sortOrder', Matchers.equalTo(code.sortOrder))
                .build())
    }

    @PendingFeature(reason = 'an unknown reason code answers 500, not 404')
    void 'an unknown reason code is a 404'() {
        expect:
        referenceDataApi.reasonCode('NO_SUCH_REASON', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_NOT_FOUND))
                .build())
    }

    void 'the support links are the configured ones'() {
        given:
        Map links = grailsApplication.config.openboxes.supportLinks as Map

        expect:
        referenceDataApi.supportLinks(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.equalTo(links))
                .build())
    }

    void 'the instance reset command is the configured one'() {
        expect:
        referenceDataApi.resettingInstanceCommand(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.equalTo(grailsApplication.config.openboxes.resettingInstance.command))
                .build())
    }

    void 'the demo data summary is the configured one'() {
        given:
        Map summary = grailsApplication.config.openboxes.configurationWizard.listOfDemoData as Map

        expect:
        referenceDataApi.listOfDemoData(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.title', Matchers.equalTo(summary.title))
                .expectBody('data.description', Matchers.equalTo(summary.description))
                .build())
    }

    void 'the request types are the requisition types with their names'() {
        expect:
        referenceDataApi.requestTypes(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(RequisitionType.listRequestTypes()*.name()))
                .expectBody('data.name', Matchers.everyItem(Matchers.not(Matchers.isEmptyOrNullString())))
                .build())
    }

    void 'the help widget configuration has the configured color and no promised response times'() {
        expect:
        referenceDataApi.helpScoutConfiguration(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('color', Matchers.equalTo(grailsApplication.config.openboxes.helpscout.widget.color))
                .expectBody('enableFabAnimation', Matchers.equalTo(false))
                .expectBody('labels.responseTime', Matchers.nullValue())
                .build())
    }

    void 'attributes: the active ones, optionally only those for one kind of record'() {
        given:
        Map<String, String> ids = Attribute.withNewTransaction {
            [product : attribute(randomUtil.randomStringFieldValue('Product attr'), [EntityTypeCode.PRODUCT], true),
             supplier: attribute(randomUtil.randomStringFieldValue('Supplier attr'), [EntityTypeCode.PRODUCT_SUPPLIER], true),
             inactive: attribute(randomUtil.randomStringFieldValue('Inactive attr'), [EntityTypeCode.PRODUCT], false)]
        }

        expect:
        referenceDataApi.attributes(null, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItems(ids.product, ids.supplier))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(ids.inactive)))
                .build())
        referenceDataApi.attributes(EntityTypeCode.PRODUCT.name(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(ids.product))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(ids.supplier)))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(ids.inactive)))
                .build())
    }

    @PendingFeature(reason = 'attributes with an unknown entity type answer 500, not 400')
    void 'attributes for an unknown kind of record: 400'() {
        expect:
        referenceDataApi.attributes('NO_SUCH_TYPE', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_BAD_REQUEST))
                .build())
    }

    private static String attribute(String name, List<EntityTypeCode> entityTypes, boolean active) {
        Attribute attribute = new Attribute(code: name, name: name, active: active, allowOther: false)
        entityTypes.each { attribute.addToEntityTypeCodes(it) }
        return attribute.save(failOnError: true, flush: true).id
    }
}
