package org.pih.warehouse.api.regression.platform.base

import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification

import org.pih.warehouse.api.spec.base.ApiSpec

/**
 * Base class for the regression API specs on OBAF: a spec extends RegressionApiSpec instead of ApiSpec.
 *
 * What it adds to upstream's ApiSpec (which it does not change):
 *  - {@link #authenticatedRequestSpec}: a request spec for raw authenticated calls
 *    (`given(authenticatedRequestSpec)...`), carrying the session cookie from POST /api/login;
 *  - every request of the spec carries X-Requested-With: XMLHttpRequest ({@link ObafAjaxHeaderFilter}), so OBAF
 *    answers errors as JSON where it can.
 *
 * Spock runs this class's setup() after ApiSpec.setup() (which logs in, then calls setupData()) and before a
 * subclass's own setup(); a spec keeps overriding setupData()/cleanupData() as with ApiSpec.
 */
abstract class RegressionApiSpec extends ApiSpec {

    /** The session-authenticated request spec: use it for raw authenticated calls. */
    RequestSpecification authenticatedRequestSpec

    void setup() {
        authenticatedRequestSpec = defaults().addCookie(cookie).build()
        authenticatedApiContext.loadContext(authenticatedRequestSpec)
    }

    protected static RequestSpecBuilder defaults() {
        return new RequestSpecBuilder().setAccept(ContentType.JSON).setContentType(ContentType.JSON)
    }
}
