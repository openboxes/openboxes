package org.pih.warehouse.api.regression.mobile

import io.restassured.http.Cookie
import io.restassured.response.Response

/**
 * Sign-in and session context, exactly as the app does it: POST /login with
 * {username,password} (no location: the app picks one afterwards), GET
 * /getAppContext, PUT /chooseLocation/{id}; POST /logout with no body.
 */
class AuthContextSpec extends MobileContractSpec {

    @Covers('src/apis/auth.ts#login')
    void 'POST /login with the app body opens a session'() {
        when:
        Response login = raw(null, 'POST', MobileContractSpec.url('src/apis/auth.ts#login', [:]),
                [username: 'admin', password: 'password'])
        CallResult r = CallResult.of('src/apis/auth.ts#login', 'POST', '/login', login.statusCode, login.contentType, login.asString())

        then:
        r.holds()
        login.getDetailedCookie('JSESSIONID') != null
    }

    void 'POST /login with a wrong password is refused with 401'() {
        when:
        Response login = raw(null, 'POST', '/login', [username: 'admin', password: 'not-the-password'])

        then:
        login.statusCode == 401
    }

    @Covers('src/apis/auth.ts#getSession')
    void 'GET /getAppContext answers the user and the chosen location'() {
        when:
        CallResult r = call('src/apis/auth.ts#getSession')

        then:
        r.holds()
        r.data.data.location.id == '1'
        r.data.data.user.username == 'admin'
    }

    @Covers('src/apis/locations.ts#setCurrentLocation')
    void 'PUT /chooseLocation/{id} switches the session location'() {
        given:
        Cookie c = appSignIn()

        when:
        CallResult r = call('src/apis/locations.ts#setCurrentLocation', ['location.id': '1'], null, c)
        CallResult ctx = call('src/apis/auth.ts#getSession', [:], null, c)

        then:
        r.holds()
        ctx.data.data.location.id == '1'
    }

    @Covers('src/apis/auth.ts#logout')
    void 'POST /logout with no body ends the session'() {
        given:
        Cookie c = appSignIn()

        when:
        CallResult r = call('src/apis/auth.ts#logout', [:], null, c)
        Response after = raw(c, 'GET', '/getAppContext', null)

        then:
        r.holds()
        after.statusCode != 200
    }
}
