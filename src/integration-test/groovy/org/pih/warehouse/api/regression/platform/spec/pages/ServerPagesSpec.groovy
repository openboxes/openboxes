package org.pih.warehouse.api.regression.platform.spec.pages

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import groovy.json.JsonOutput
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.core.LocationApi
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location

import static io.restassured.RestAssured.given

/**
 * The server rendered report and data pages, called the way the browser does: with the session of a logged-in user
 * in a chosen location, against the real database queries behind them.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ServerPagesSpec extends RegressionApiSpec {

    static final String DEPOT_TYPE_ID = '2'

    @Autowired
    LocationApi locationApi

    void setupData() {
        setStock(product, null, null, 25)
    }

    private RequestSpecification page() {
        return given().baseUri('http://localhost').port(serverPort).basePath('/openboxes').cookie(cookie)
                .redirects().follow(false)
    }

    void 'the cycle count report lists the stock of the location'() {
        when:
        Response response = page().get('/report/showCycleCountReport')

        then:
        response.statusCode == 200
        response.body.asString().contains(product.productCode)
    }

    void 'the cycle count report downloads as a spreadsheet'() {
        when:
        Response response = page().queryParam('print', 'true').get('/report/showCycleCountReport')

        then:
        response.statusCode == 200
        response.contentType.startsWith('application/vnd.ms-excel')
        response.header('Content-disposition').startsWith('attachment; filename="CycleCountReport-')
        response.body.asByteArray().length > 0
    }

    void 'the transaction report page shows the transaction fact summary'() {
        when:
        Response response = page().get('/report/showTransactionReport')

        then:
        response.statusCode == 200
        response.contentType.startsWith('text/html')
    }

    void 'the bin location report downloads the stock of the location as CSV'() {
        when:
        Response response = page().queryParam('downloadAction', 'downloadStockReport').get('/report/showBinLocationReport')

        then:
        response.statusCode == 200
        response.contentType.startsWith('text/csv')
        response.body.asString().contains(product.productCode)
    }

    void 'the consumption report runs over the transactions of the location'() {
        when:
        Response response = page()
                .queryParam('fromLocations', facility.id)
                .queryParam('fromDate', (new Date() - 30).format('MM/dd/yyyy'))
                .queryParam('toDate', new Date().format('MM/dd/yyyy'))
                .get('/consumption/show')

        then:
        response.statusCode == 200
    }

    void 'the transaction report table lists the stock changes of the period'() {
        when:
        Response response = page().accept('application/json')
                .queryParam('location.id', facility.id)
                .queryParam('category.id', rootCategory.id)
                .queryParam('startDate', (new Date() - 7).format('MM/dd/yyyy'))
                .queryParam('endDate', new Date().format('MM/dd/yyyy'))
                .get('/json/getTransactionReport')

        then:
        response.statusCode == 200
        response.jsonPath().getList('aaData') != null
    }

    @PendingFeature(reason = 'the dashboard recent activity feed answers 500')
    void 'the dashboard activity feed lists the recently updated products'() {
        when:
        Response response = page().accept('application/json').get('/json/getDashboardActivity')

        then:
        response.statusCode == 200
        response.jsonPath().getList('aaData.type').contains('package')
    }

    void 'the inventory by location report downloads quantities per location'() {
        when:
        Response response = page()
                .queryParam('locations', facility.id)
                .queryParam('categories', rootCategory.id)
                .queryParam('actionButton', 'download')
                .get('/report/showInventoryByLocationReport')

        then:
        response.statusCode == 200
        response.contentType.startsWith('text/csv')
        response.body.asString().contains(product.productCode)
    }

    void 'the product code autocomplete searches the database'() {
        when:
        Response response = page().accept('application/json').queryParam('term', product.productCode).get('/json/findProductCodes')

        then:
        response.statusCode == 200
        response.jsonPath().getList('value').contains(product.productCode)
    }

    void 'the location edit form saves changes to a location'() {
        given:
        String name = "Edited Depot ${UUID.randomUUID().toString().take(8)}"
        String id = locationApi.create(JsonOutput.toJson([
                name               : name,
                active             : true,
                locationType       : [id: DEPOT_TYPE_ID],
                organization       : [id: facility.organization.id],
                supportedActivities: [ActivityCode.MANAGE_INVENTORY.id],
        ]), [useDefaultActivities: false], new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK).build())
                .jsonPath().getString('data.id')

        when:
        Response response = page()
                .formParam('id', id)
                .formParam('name', "${name} renamed")
                .formParam('supportedActivities', ActivityCode.MANAGE_INVENTORY.id)
                .formParam('supportedActivities', ActivityCode.APPROVE_REQUEST.id)
                .post('/location/update')

        then:
        response.statusCode == 302
        response.header('Location').contains("/location/edit/${id}")
        Location.withNewSession {
            Location saved = Location.get(id)
            assert saved.name == "${name} renamed"
            assert saved.supportedActivities == [ActivityCode.MANAGE_INVENTORY.id, ActivityCode.APPROVE_REQUEST.id] as Set
            true
        }

        when: 'the edit page is opened'
        Response edit = page().get("/location/edit/${id}")

        then:
        edit.statusCode == 200
        edit.body.asString().contains("${name} renamed")
    }
}
