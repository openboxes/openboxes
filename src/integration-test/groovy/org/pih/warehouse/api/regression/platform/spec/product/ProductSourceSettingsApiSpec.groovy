package org.pih.warehouse.api.regression.platform.spec.product



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import spock.lang.PendingFeature
import org.pih.warehouse.api.spec.product.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.product.ProductAttribute
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierPreference

/**
 * What the product source form saves after the details: preferences, attributes, and the package with its prices.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known response POST /productSuppliers',
])
class ProductSourceSettingsApiSpec extends ProductSourceSpec {

    // Preferences

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a default preference is created and shows on the product source'() {
        given:
        String sourceId = newSource()

        when:
        Response created = productSupplierPreferenceApi.create(preferenceBody(sourceId).toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .expectBody('data.comments', Matchers.equalTo('first choice'))
                .build())
        String id = created.jsonPath().getString('data.id')

        then:
        productSupplierApi.read(sourceId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.productSupplierPreferences.id', Matchers.contains(id))
                .expectBody('data.productSupplierPreferences[0].destinationParty', Matchers.nullValue())
                .build())
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a preference without a preference type answers 400'() {
        given:
        JSONObject body = preferenceBody(newSource())
        body.remove('preferenceType')

        expect:
        productSupplierPreferenceApi.create(body.toString(), error(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'updating a preference with #method changes it'() {
        given:
        String sourceId = newSource()
        String id = productSupplierPreferenceApi.create(preferenceBody(sourceId).toString(),
                responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath().getString('data.id')
        Organization ward = organization('Ward')
        JSONObject change = preferenceBody(sourceId, ward).put('comments', 'only for the ward')

        when:
        productSupplierPreferenceApi.update(id, change.toString(), method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id))
                .expectBody('data.comments', Matchers.equalTo('only for the ward'))
                .build())

        then:
        ProductSupplierPreference.withNewSession { ProductSupplierPreference.get(id).destinationParty?.id } == ward.id

        where:
        method << [Method.PUT, Method.POST]
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'updating an unknown preference answers 404'() {
        expect:
        productSupplierPreferenceApi.update(INVALID_ID, preferenceBody(newSource()).toString(), Method.PUT,
                error(HttpStatus.SC_NOT_FOUND))
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a preference is deleted; an unknown one answers 404'() {
        given:
        String id = productSupplierPreferenceApi.create(preferenceBody(newSource()).toString(),
                responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath().getString('data.id')

        when:
        productSupplierPreferenceApi.delete(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        ProductSupplierPreference.withNewSession { ProductSupplierPreference.get(id) } == null
        productSupplierPreferenceApi.delete(id, error(HttpStatus.SC_NOT_FOUND))
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a batch saves the default preference and one per destination'() {
        given:
        String sourceId = newSource()
        Organization ward = organization('Ward')
        JSONObject batch = new JSONObject().put('productSupplierPreferences', new JSONArray()
                .put(batchPreference(sourceId, null, 'default'))
                .put(batchPreference(sourceId, ward, 'ward')))

        when:
        productSupplierPreferenceApi.saveBatch(batch.toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.comments', Matchers.containsInAnyOrder('default', 'ward'))
                .build())

        then:
        ProductSupplier.withNewSession {
            ProductSupplier.get(sourceId).productSupplierPreferences*.comments as Set
        } == ['default', 'ward'] as Set
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a batch that adds a second default preference answers 400'() {
        given:
        String sourceId = newSource()
        productSupplierPreferenceApi.create(preferenceBody(sourceId).toString(), responseSpecUtil.CREATED_RESPONSE_SPEC)
        JSONObject batch = new JSONObject().put('productSupplierPreferences', new JSONArray()
                .put(batchPreference(sourceId, null, 'another default')))

        expect:
        productSupplierPreferenceApi.saveBatch(batch.toString(), error(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a batch updates an existing preference by its id'() {
        given:
        String sourceId = newSource()
        String id = productSupplierPreferenceApi.create(preferenceBody(sourceId).toString(),
                responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath().getString('data.id')
        JSONObject batch = new JSONObject().put('productSupplierPreferences', new JSONArray()
                .put(batchPreference(sourceId, null, 'changed').put('id', id)))

        when:
        productSupplierPreferenceApi.saveBatch(batch.toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(id))
                .build())

        then:
        ProductSupplierPreference.withNewSession { ProductSupplierPreference.get(id).comments } == 'changed'
    }

    /** A preference as the form's batch sends it: plain ids and MM/dd/yyyy dates. */
    private JSONObject batchPreference(String sourceId, Organization destination, String comments) {
        return new JSONObject()
                .put('productSupplier', sourceId)
                .put('preferenceType', preferred.id)
                .put('destinationParty', destination?.id)
                .put('validityStartDate', '01/01/2026')
                .put('validityEndDate', '12/31/2030')
                .put('comments', comments)
    }

    // Attributes

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'attribute values of a product source are created, changed and removed'() {
        given:
        String sourceId = newSource()
        Attribute color = supplierAttribute(['red', 'blue'], false)

        when: 'a value is set'
        Response created = productSupplierPreferenceApi.updateAttributes(attributes(sourceId, color, 'red'), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.createdAttributes.value', Matchers.contains('red'))
                .expectBody('data.updatedAttributes', Matchers.empty())
                .build())
        String productAttributeId = created.jsonPath().getString('data.createdAttributes[0].id')

        then:
        attributeValues(sourceId) == ['red']

        when: 'it is changed'
        productSupplierPreferenceApi.updateAttributes(attributes(sourceId, color, 'blue'), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.updatedAttributes.value', Matchers.contains('blue'))
                .expectBody('data.createdAttributes', Matchers.empty())
                .build())

        then:
        attributeValues(sourceId) == ['blue']

        when: 'it is cleared'
        productSupplierPreferenceApi.updateAttributes(attributes(sourceId, color, ''), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.deletedAttributes', Matchers.contains(productAttributeId))
                .build())

        then:
        attributeValues(sourceId) == []
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a free-text attribute takes any value; a coded one only its options'() {
        given:
        String sourceId = newSource()

        expect:
        productSupplierPreferenceApi.updateAttributes(attributes(sourceId, supplierAttribute([], true), 'anything'),
                responseSpecUtil.OK_RESPONSE_SPEC)
        productSupplierPreferenceApi.updateAttributes(attributes(sourceId, supplierAttribute(['red'], false), 'green'),
                error(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'an attribute that is not for product sources answers 400'() {
        given:
        Attribute productOnly = supplierAttribute(['red'], false, EntityTypeCode.PRODUCT)

        expect:
        productSupplierPreferenceApi.updateAttributes(attributes(newSource(), productOnly, 'red'),
                error(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a required attribute must be sent with a value'() {
        given:
        String sourceId = newSource()
        Attribute required = supplierAttribute(['yes'], false, EntityTypeCode.PRODUCT_SUPPLIER, true)
        Attribute other = supplierAttribute(['red'], false)

        expect: 'sent empty'
        productSupplierPreferenceApi.updateAttributes(attributes(sourceId, required, ''), error(HttpStatus.SC_BAD_REQUEST))

        and: 'not sent at all'
        productSupplierPreferenceApi.updateAttributes(attributes(sourceId, other, 'red'), error(HttpStatus.SC_BAD_REQUEST))

        and: 'sent with a value'
        productSupplierPreferenceApi.updateAttributes(new JSONObject().put('productAttributes', new JSONArray()
                .put(attributeRow(sourceId, required, 'yes'))
                .put(attributeRow(sourceId, other, 'red'))).toString(), responseSpecUtil.OK_RESPONSE_SPEC)

        cleanup: 'other tests do not send it'
        Attribute.withNewTransaction { Attribute.get(required.id).required = false }
    }

    private Attribute supplierAttribute(List<String> options, boolean allowOther,
                                        EntityTypeCode entity = EntityTypeCode.PRODUCT_SUPPLIER, boolean required = false) {
        return Attribute.withNewTransaction {
            Attribute attribute = new Attribute(code: randomUtil.randomStringFieldValue('ATTR'),
                    name: randomUtil.randomStringFieldValue('Attribute'), allowOther: allowOther, required: required,
                    options: options)
            attribute.addToEntityTypeCodes(entity)
            attribute.save(failOnError: true)
        }
    }

    private static JSONObject attributeRow(String sourceId, Attribute attribute, String value) {
        return new JSONObject().put('attribute', attribute.id).put('productSupplier', sourceId).put('value', value)
    }

    private static String attributes(String sourceId, Attribute attribute, String value) {
        return new JSONObject().put('productAttributes', new JSONArray().put(attributeRow(sourceId, attribute, value)))
                .toString()
    }

    private List<String> attributeValues(String sourceId) {
        return ProductAttribute.withNewSession {
            ProductAttribute.findAllByProductSupplier(ProductSupplier.get(sourceId))*.value
        }
    }

    // Package and prices

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'the package sets the default package, its price and the contract price'() {
        given:
        String sourceId = newSource()

        when:
        productSupplierPreferenceApi.savePackage(packageBody(sourceId, 10, 25)
                .put('minOrderQuantity', 20)
                .put('contractPricePrice', 2)
                .put('contractPriceValidUntil', '12/31/2030')
                .toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(sourceId))
                .expectBody('data.defaultProductPackage.uom.id', Matchers.equalTo(box.id))
                .expectBody('data.defaultProductPackage.quantity', Matchers.equalTo(10))
                .expectBody('data.packagePrice', Matchers.equalTo(25.0f))
                .expectBody('data.unitPrice', Matchers.equalTo(2.5f))
                .expectBody('data.minOrderQuantity', Matchers.equalTo(20))
                .expectBody('data.contractPrice.price', Matchers.equalTo(2))
                .build())

        then: 'saved'
        productSupplierApi.read(sourceId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.packagePrice', Matchers.equalTo(25.0f))
                .expectBody('data.contractPrice.price', Matchers.equalTo(2.0f))
                .build())
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'saving the same package again changes its price; clearing the contract price removes it'() {
        given:
        String sourceId = newSource()
        productSupplierPreferenceApi.savePackage(packageBody(sourceId, 10, 25).put('contractPricePrice', 2).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)

        when:
        productSupplierPreferenceApi.savePackage(packageBody(sourceId, 10, 40).toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.packagePrice', Matchers.equalTo(40.0f))
                .expectBody('data.contractPrice', Matchers.nullValue())
                .build())

        then: 'still one package'
        ProductSupplier.withNewSession { ProductSupplier.get(sourceId).productPackages.size() } == 1
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a package without a price keeps the source without a package price'() {
        given:
        String sourceId = newSource()

        expect:
        productSupplierPreferenceApi.savePackage(packageBody(sourceId, 6, null).toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.defaultProductPackage.quantity', Matchers.equalTo(6))
                .expectBody('data.packagePrice', Matchers.equalTo(0.0f))
                .build())
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a package of #quantity answers 400'() {
        expect:
        productSupplierPreferenceApi.savePackage(packageBody(newSource(), quantity, 10).toString(),
                error(HttpStatus.SC_BAD_REQUEST))

        where:
        quantity << [-1, 0]
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a package without a product source or with a contract price that ended long ago answers 400'() {
        given:
        JSONObject withoutSource = packageBody(newSource(), 10, 10)
        withoutSource.remove('productSupplier')

        expect:
        productSupplierPreferenceApi.savePackage(withoutSource.toString(), error(HttpStatus.SC_BAD_REQUEST))
        productSupplierPreferenceApi.savePackage(packageBody(newSource(), 10, 10)
                .put('contractPricePrice', 1).put('contractPriceValidUntil', '01/01/1990').toString(),
                error(HttpStatus.SC_BAD_REQUEST))
    }

    private JSONObject packageBody(String sourceId, Integer quantity, Integer price) {
        return new JSONObject()
                .put('productSupplier', sourceId)
                .put('uom', box.id)
                .put('productPackageQuantity', quantity)
                .put('productPackagePrice', price)
    }
}
