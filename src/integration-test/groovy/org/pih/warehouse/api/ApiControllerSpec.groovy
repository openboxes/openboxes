package org.pih.warehouse.api

import grails.test.mixin.TestFor
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*
import org.pih.warehouse.Application
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.PartyTypeCode
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore
@Rollback
@TestFor(ApiController)
@Integration(applicationClass = Application.class)
class ApiControllerSpec extends Specification {

    @Autowired
    UserService userService

    @Autowired
    LocalizationService localizationService

    @Shared
    Location location

    def setup() {
        // Create new location
        PartyType orgPartyType = PartyType.findByPartyTypeCode(PartyTypeCode.ORGANIZATION)
        Organization organization = Organization.findOrCreateWhere([code: "TEST", name: "Test Corporation", partyType: orgPartyType]).save()
        LocationType defaultLocationType = LocationType.findByLocationTypeCode(LocationTypeCode.DEPOT)
        location = Location.findOrCreateWhere([locationNumber: "TEST", name: "Test Warehouse", locationType: defaultLocationType, organization: organization]).save()
    }

    void "login should succeed"() {
        given:
        controller.userService = userService
        controller.request.json = [
                username: "admin",
                password: "password",
                location: location.id
        ]

        when:
        controller.login()

        then:
        response.status == 200
        response.text == "Authentication was successful"

    }

    void "login should fail due to invalid password"() {
        given:
        controller.userService = userService
        controller.request.json = [
                username: "admin",
                password: "wrongpassword",
                location: location.id
        ]

        when:
        controller.login()

        then:
        response.status == 401
        response.text == "Authentication failed"

    }

    void "status should return 200"() {
        when:
        println "session before status>>::${controller.session}"
        controller.status()

        then:
        response.status == 200
        response.json.status == "OK"

    }

    void "get app context should return data"() {
        given:
        controller.userService = userService
        controller.localizationService = localizationService
        controller.session.user = User.findByUsername("admin")
        controller.session.warehouse = Location.findByLocationNumber("TEST")

        when:
        controller.getAppContext()

        then:
        response.status == 200

    }
}
