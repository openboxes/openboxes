package org.pih.warehouse.api.regression.platform.spec.generic


import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericApi
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericApiWrapper
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericResource
import groovy.json.JsonOutput
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.Tag

/**
 * The generic domain API (/api/generic/{resource}...) with product tags as the resource: create (one or several),
 * read, update (PUT or POST), delete and search, and the errors for unknown resources and bad searches.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class GenericApiSpec extends RegressionApiSpec {

    @Autowired
    RegressionGenericApi regressionGenericApi

    private String uniqueTag() {
        return randomUtil.randomStringFieldValue('Tag')
    }

    private static String tagValue(String id) {
        return Tag.withNewSession { Tag.get(id)?.tag }
    }

    private static String newTag(String value) {
        return Tag.withNewTransaction { new Tag(tag: value).save(failOnError: true, flush: true).id }
    }

    void 'create a tag: 201 with the tag, and it is stored'() {
        given:
        String value = uniqueTag()

        when:
        Response response = regressionGenericApi.create(RegressionGenericResource.TAG, JsonOutput.toJson([tag: value]), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .expectBody('data.tag', Matchers.equalTo(value))
                .build())

        then:
        tagValue(response.jsonPath().getString('data.id')) == value
    }

    void 'create several tags from a JSON array'() {
        given:
        List<String> values = [uniqueTag(), uniqueTag()]

        when:
        Response response = regressionGenericApi.create(RegressionGenericResource.TAG, JsonOutput.toJson(values.collect { [tag: it] }),
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_CREATED)
                        .expectBody('data.tag', Matchers.equalTo(values))
                        .build())

        then:
        response.jsonPath().getList('data.id').collect { tagValue(it as String) } == values
    }

    void 'create an invalid tag: 400, nothing stored'() {
        given:
        int before = Tag.withNewSession { Tag.count() }

        expect:
        regressionGenericApi.create(RegressionGenericResource.TAG, JsonOutput.toJson([tag: '']), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_BAD_REQUEST))
                .build())
        Tag.withNewSession { Tag.count() } == before
    }

    void 'read a tag'() {
        given:
        String value = uniqueTag()
        String id = newTag(value)

        expect:
        regressionGenericApi.get(RegressionGenericResource.TAG, id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id))
                .expectBody('data.tag', Matchers.equalTo(value))
                .build())
    }

    void 'update a tag with #method'() {
        given:
        String id = newTag(uniqueTag())
        String value = uniqueTag()

        when:
        Response response = method == Method.PUT
                ? regressionGenericApi.update(RegressionGenericResource.TAG, id, JsonOutput.toJson([tag: value]), responseSpecUtil.OK_RESPONSE_SPEC)
                : regressionGenericApi.updateWithPost(RegressionGenericResource.TAG, id, JsonOutput.toJson([tag: value]), responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        response.jsonPath().getString('data.tag') == value
        tagValue(id) == value

        where:
        method << [Method.PUT, Method.POST]
    }

    void 'delete a tag: 204 and gone'() {
        given:
        String id = newTag(uniqueTag())

        when:
        regressionGenericApi.delete(RegressionGenericResource.TAG, id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        tagValue(id) == null
    }

    void 'an unknown tag: 404 to read, update and delete'() {
        expect:
        regressionGenericApi.get(RegressionGenericResource.TAG, INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        regressionGenericApi.update(RegressionGenericResource.TAG, INVALID_ID, JsonOutput.toJson([tag: 'x']), responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        regressionGenericApi.delete(RegressionGenericResource.TAG, INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'search tags with #operator'() {
        given:
        String value = uniqueTag()
        String id = newTag(value)
        String other = newTag(uniqueTag())

        when:
        Response response = regressionGenericApi.search(RegressionGenericResource.TAG, JsonOutput.toJson([searchAttributes: [
                [property: 'tag', operator: operator, value: pattern(value)]]]), Method.POST, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        response.jsonPath().getList('data.id') == [id]
        other != id

        where:
        operator | pattern
        null     | { String v -> v }
        'eq'     | { String v -> v }
        'like'   | { String v -> v.take(v.length() - 2) + '%' }
        'ilike'  | { String v -> v.toUpperCase() }
    }

    void 'search without attributes (GET, no body) lists every tag'() {
        given:
        String id = newTag(uniqueTag())

        expect:
        regressionGenericApi.search(RegressionGenericResource.TAG, null, Method.GET, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(id))
                .build())
    }

}
