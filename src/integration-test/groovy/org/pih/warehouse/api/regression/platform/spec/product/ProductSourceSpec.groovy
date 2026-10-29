package org.pih.warehouse.api.regression.platform.spec.product

import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.product.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.regression.platform.client.core.UnitOfMeasureApi
import org.pih.warehouse.api.regression.platform.client.product.ProductSupplierApi
import org.pih.warehouse.api.regression.platform.client.product.ProductSupplierPreferenceApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product

/**
 * Shared fixtures for the product source specs (a product source is a product as a given supplier sells it): a
 * product of its own per test; a supplier, a manufacturer, a preference type and a unit of measure once per spec.
 * Tests never rely on a clean database: names and codes are unique per run.
 */
abstract class ProductSourceSpec extends RegressionApiSpec {

    @Autowired
    ProductSupplierApi productSupplierApi

    @Autowired
    ProductSupplierPreferenceApi regressionProductSupplierPreferenceApi

    @Autowired
    UnitOfMeasureApi regressionUnitOfMeasureApi

    /** A product of this test only, so earlier tests' sources don't show up in its lists. */
    Product item

    @Shared
    Organization supplier

    @Shared
    Organization manufacturer

    @Shared
    PreferenceType preferred

    @Shared
    UnitOfMeasure box

    @Override
    void setupData() {
        item = Product.withNewTransaction {
            new ProductTestBuilder().name(randomUtil.randomStringFieldValue('Sourced product')).category(rootCategory)
                    .build(true)
        }
        if (preferred) {
            return
        }
        (supplier, manufacturer) = ['Supplier', 'Manufacturer'].collect { String kind -> organization(kind) }
        preferred = PreferenceType.withNewTransaction {
            new PreferenceType(name: randomUtil.randomStringFieldValue('Preferred')).save(failOnError: true)
        }
        // Through the API, as a user creates it (the unit list documents who created each unit)
        String code = "BX${UUID.randomUUID().toString().take(8)}"
        String boxId = regressionUnitOfMeasureApi.create(new JSONObject().put('name', "Box ${code}".toString()).put('code', code)
                .toString(), responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath().getString('data.id')
        box = UnitOfMeasure.withNewSession { UnitOfMeasure.get(boxId) }
    }

    Organization organization(String kind) {
        return Organization.withNewTransaction {
            new Organization(code: randomUtil.randomStringFieldValue(kind.take(3).toUpperCase()),
                    name: randomUtil.randomStringFieldValue(kind), active: true,
                    partyType: PartyType.findByCode('ORG')).save(failOnError: true)
        }
    }

    /** The details the product source form sends (references as plain ids). */
    JSONObject sourceBody(String name = randomUtil.randomStringFieldValue('Source')) {
        return new JSONObject()
                .put('product', item.id)
                .put('supplier', supplier.id)
                .put('manufacturer', manufacturer.id)
                .put('name', name)
                .put('supplierCode', randomUtil.randomStringFieldValue('SUP'))
                .put('manufacturerCode', 'MFG-1')
                .put('ratingTypeCode', 'GOOD')
                .put('active', true)
    }

    /** A new product source of the test's product; returns its id. */
    String newSource(JSONObject body = sourceBody()) {
        Response created = productSupplierApi.create(body.toString(), responseSpecUtil.CREATED_RESPONSE_SPEC)
        return created.jsonPath().getString('data.id')
    }

    /** A preference of the source: the default one (no destination) unless a destination organization is given. */
    JSONObject preferenceBody(String sourceId, Organization destination = null) {
        JSONObject body = new JSONObject()
                .put('productSupplier', new JSONObject().put('id', sourceId))
                .put('preferenceType', new JSONObject().put('id', preferred.id))
                .put('comments', 'first choice')
        if (destination) {
            body.put('destinationParty', new JSONObject().put('id', destination.id))
        }
        return body
    }

    /** An error response: the status and the common error body (errorCode is the status, errorMessage is set). */
    static ResponseSpecification error(int status) {
        return new ResponseSpecBuilder()
                .expectStatusCode(status)
                .expectBody('errorCode', Matchers.equalTo(status))
                .expectBody('errorMessage', Matchers.notNullValue())
                .build()
    }
}
