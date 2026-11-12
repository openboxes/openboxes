package org.pih.warehouse.api.spec.inventory

import grails.gorm.transactions.Transactional
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Cookie
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.inventory.ProductAvailabilityApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.api.util.ResponseSpecUtil
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User

class ProductAvailabilityApiSpec extends ApiSpec {

    @Autowired
    ProductAvailabilityApiWrapper productAvailabilityApiWrapper

    @Autowired
    ResponseSpecUtil responseSpecUtil

    void 'list returns stock for the facility with required fields'() {
        given:
        setStock(product, null, null, 10)

        when:
        def response = productAvailabilityApiWrapper.listOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        assert data.size() >= 1
        def row = data.find { it.productCode == product.productCode }

        assert row != null
        assert row["product.name"] == product.name
        assert row.productCode == product.productCode
        assert row.quantityOnHand == 10
        assert row.containsKey("quantityAvailable")
        assert row.containsKey("lotNumber")
        assert row.containsKey("expirationDate")
        assert row.containsKey("binLocation")
        assert row.containsKey("zone")
    }

    void 'list excludes zero quantity on hand rows'() {
        given:
        setStock(product, null, null, 0)

        when:
        def response = productAvailabilityApiWrapper.listOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        assert !data.any { it.productCode == product.productCode && it.quantityOnHand == 0 }
    }

    void 'list returns error for unknown facility id'() {
        expect:
        productAvailabilityApiWrapper.listExpectingStatus(INVALID_ID, HttpStatus.SC_BAD_REQUEST)
    }

    // ---- Authorization: the user must hold a role at the facility in the URL ----

    static final String RESTRICTED_USERNAME = "authz-browser-main-only"
    static final String RESTRICTED_PASSWORD = "password"

    /**
     * Creates (once) a user with no global roles and only a Browser role at the main facility, plus a second
     * facility they hold no role at.
     */
    @Transactional
    private Location setupRestrictedUserAndOtherFacility() {
        Location otherFacility = new LocationTestBuilder()
                .name("Authorization Test Facility")
                .asFacility()
                .findOrBuild()

        if (!User.findByUsername(RESTRICTED_USERNAME)) {
            User user = new User(
                    username: RESTRICTED_USERNAME,
                    password: RESTRICTED_PASSWORD.encodeAsPassword(),
                    firstName: "Authz",
                    lastName: "Browser",
                    email: "${RESTRICTED_USERNAME}@example.com",
                    active: true)
            user.save(failOnError: true)
            user.addToLocationRoles(new LocationRole(
                    user: user,
                    location: Location.get(facility.id),
                    role: Role.findByRoleType(RoleType.ROLE_BROWSER)))
            user.save(failOnError: true, flush: true)
        }
        return otherFacility
    }

    /**
     * Points every subsequent authenticated API call in this test at the restricted user's session.
     * ApiSpec.setup() reloads the default (admin) context before each test.
     */
    private void logInAsRestrictedUser() {
        Cookie restrictedCookie = authApiWrapper.loginOK(RESTRICTED_USERNAME, RESTRICTED_PASSWORD, facility.id)
                .getDetailedCookie("JSESSIONID")
        authenticatedApiContext.loadContext(new RequestSpecBuilder()
                .addCookie(restrictedCookie)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .build())
    }

    void 'list allows a user with a role at the requested facility'() {
        given:
        setupRestrictedUserAndOtherFacility()
        logInAsRestrictedUser()

        expect:
        productAvailabilityApiWrapper.listOK(facility.id)
    }

    void 'list forbids a user with no role at the requested facility'() {
        given:
        Location otherFacility = setupRestrictedUserAndOtherFacility()
        logInAsRestrictedUser()

        expect:
        productAvailabilityApiWrapper.listExpectingStatus(otherFacility.id, HttpStatus.SC_FORBIDDEN)
    }

    void 'list forbids a user with no global role from probing unknown facility ids'() {
        given:
        setupRestrictedUserAndOtherFacility()
        logInAsRestrictedUser()

        expect: 'a 403 rather than a 400, so ids cannot be enumerated'
        productAvailabilityApiWrapper.listExpectingStatus(INVALID_ID, HttpStatus.SC_FORBIDDEN)
    }
}
