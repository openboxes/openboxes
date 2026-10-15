package org.pih.warehouse.api.spec.product

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.product.ProductSupplierPreferenceApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.PreferenceTypeTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductSupplierTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierPreference

class ProductSupplierPreferenceApiSpec extends ApiSpec {

    @Autowired
    ProductSupplierPreferenceApiWrapper productSupplierPreferenceApiWrapper

    @Shared
    Product supplierProduct

    @Shared
    PreferenceType preferenceType

    @Shared
    ProductSupplier productSupplier

    @Shared
    ProductSupplier productSupplierWithPreference

    @Shared
    ProductSupplierPreference preference

    @Override
    void setupData() {
        supplierProduct = new ProductTestBuilder().randomizedName().category(rootCategory).build(true)
        preferenceType = new PreferenceTypeTestBuilder().build(true)
        productSupplier = new ProductSupplierTestBuilder().product(supplierProduct).build(true)
        productSupplierWithPreference = new ProductSupplierTestBuilder().product(supplierProduct).build(true)
        preference = new ProductSupplierPreference(productSupplier: productSupplierWithPreference, preferenceType: preferenceType)
                .save(failOnError: true)
    }

    @Override
    void cleanupData() {
        ProductSupplierPreference.findAllByPreferenceType(preferenceType)*.delete()
        ProductSupplier.get(productSupplier.id)?.delete()
        ProductSupplier.get(productSupplierWithPreference.id)?.delete()
        preferenceType?.delete()
        supplierProduct?.delete()
    }

    void 'create product supplier preference should succeed when product supplier and preference type are provided'() {
        given:
        JSONObject body = new JSONObject()
                .put('productSupplier', productSupplier.id)
                .put('preferenceType', preferenceType.id)

        expect:
        assert productSupplierPreferenceApiWrapper.createOK(body) != null
    }

    void 'create product supplier preference should fail when preference type is missing'() {
        given:
        String body = new JSONObject()
                .put('productSupplier', productSupplier.id)
                .toString()

        expect:
        productSupplierPreferenceApiWrapper.api.create(body, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    void 'update product supplier preference should succeed and return updated data'() {
        given:
        String body = new JSONObject()
                .put('comments', 'Updated preference')
                .toString()

        expect:
        productSupplierPreferenceApiWrapper.api.update(preference.id, body, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(preference.id))
                .expectBody('data.comments', Matchers.equalTo('Updated preference'))
                .build())
    }

    void 'delete product supplier preference should succeed when preference exists'() {
        expect:
        productSupplierPreferenceApiWrapper.deleteOK(preference.id)
    }

    void 'create or update batch should save all given preferences'() {
        given:
        JSONObject newPreference = new JSONObject()
                .put('productSupplier', productSupplier.id)
                .put('preferenceType', preferenceType.id)
        JSONObject updatedPreference = new JSONObject()
                .put('id', preference.id)
                .put('productSupplier', productSupplierWithPreference.id)
                .put('preferenceType', preferenceType.id)
                .put('comments', 'Updated in batch')
        String body = new JSONObject()
                .put('productSupplierPreferences', [newPreference, updatedPreference])
                .toString()

        expect:
        productSupplierPreferenceApiWrapper.api.createOrUpdateBatch(body, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.hasSize(2))
                .expectBody('data.id', Matchers.everyItem(Matchers.notNullValue()))
                .expectBody('data.id', Matchers.hasItem(preference.id))
                .expectBody('data.comments', Matchers.hasItem('Updated in batch'))
                .build())
    }

    void 'create or update batch should fail when product supplier already has a default preference'() {
        given:
        JSONObject secondDefaultPreference = new JSONObject()
                .put('productSupplier', productSupplierWithPreference.id)
                .put('preferenceType', preferenceType.id)
        String body = new JSONObject()
                .put('productSupplierPreferences', [secondDefaultPreference])
                .toString()

        expect:
        productSupplierPreferenceApiWrapper.api.createOrUpdateBatch(body, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }
}
