package org.pih.warehouse.api.regression.platform.pages.user

import spock.lang.PendingFeature


import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import io.restassured.response.Response

import org.pih.warehouse.core.User
import org.pih.warehouse.api.regression.platform.pages.ServerPages

import static io.restassured.RestAssured.given
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.page
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * The dashboard and sign-in server pages (DashboardController, AuthController): the global search, location
 * choice, menus and downloads, and signing in and out.
 */
class DashboardAndAuthPagesSpec extends RegressionApiSpec {

    ServerPages pages

    @Override
    void setupData() {
        pages = new ServerPages(serverPort, cookie)
    }

    /** A browser without a session. */
    def anonymous() {
        return given().baseUri('http://localhost').port(serverPort).basePath('/openboxes').redirects().follow(false)
                .accept('text/html')
    }

    void 'the global search goes to what the term names'() {
        expect: 'a product code opens its stock card'
        redirectPath(pages.get('/dashboard/globalSearch', [searchTerms: product.productCode])) ==
                "/inventoryItem/showStockCard/${product.id}"

        and: 'anything else goes to the inventory browser'
        redirectPath(pages.get('/dashboard/globalSearch', [searchTerms: 'no-such-thing-anywhere'])) ==
                '/inventory/browse'
    }

    void 'the dashboard pages and fragments'() {
        expect:
        page(pages.get('/dashboard/footer')) != null
        page(pages.get('/dashboard/megamenu')) != null
        page(pages.get('/dashboard/changeLocation')).contains(facility.name)
        page(pages.get('/dashboard/chooseLocation')).contains(facility.name)
        pages.request().accept('application/json').get('/dashboard/status').statusCode == 200
        pages.request().accept('application/json').get('/dashboard/expirationSummary').statusCode == 200
        redirectPath(pages.get('/dashboard/chooseLayout', [layout: 'custom'])) == '/'
    }

    void 'the dashboard downloads'() {
        when:
        Response fastMovers = pages.get('/dashboard/downloadFastMoversAsCsv', [date: '01/01/2026'])

        then:
        fastMovers.statusCode == 200
        fastMovers.contentType.startsWith('text/csv')
    }

    @PendingFeature(reason = 'the generic product summary CSV download fails')
    void 'the generic product summary downloads as csv'() {
        when:
        Response summary = pages.get('/dashboard/downloadGenericProductSummaryAsCsv', [status: 'ALL'])

        then:
        summary.statusCode == 200
        summary.contentType.startsWith('text/csv')
    }

    void 'an inactive user cannot sign in'() {
        given:
        String inactive = randomUtil.randomStringFieldValue('inactive').toLowerCase()
        User.withNewTransaction {
            new User(username: inactive, firstName: 'In', lastName: 'Active', password: 'x'.encodeAsPassword(),
                    passwordConfirm: 'x'.encodeAsPassword(), active: false).save(failOnError: true, flush: true)
        }

        when:
        Response refused = anonymous().contentType('application/x-www-form-urlencoded')
                .formParam('username', inactive).formParam('password', 'x').post('/auth/handleLogin')

        then:
        redirectPath(refused) == '/auth/login'
    }

    void 'the account emails render and signing out ends the session'() {
        given:
        String userId = User.withNewSession { User.findByUsername(username).id }

        expect:
        page(pages.get("/auth/renderAccountCreatedEmail/${userId}")) != null
        page(pages.get("/auth/renderAccountConfirmedEmail/${userId}")) != null

        when:
        Response loggedOut = pages.get('/auth/logout')

        then:
        redirectPath(loggedOut) == '/auth/login'
    }
}
