package org.pih.warehouse.api.spec

import grails.gorm.transactions.Transactional
import io.restassured.path.json.JsonPath
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import org.pih.warehouse.TestingDates
import org.pih.warehouse.api.client.TestingDatesApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec

@Unroll
class TestingDatesApiSpec extends ApiSpec {

    @Autowired
    TestingDatesApiWrapper api

    @Transactional
    void setupData() {
        // Truncate the table before running each test
        TestingDates.executeUpdate('delete from TestingDates')
    }

    void 'test Instant: #scenario'() {
        given:
        JSONObject request = new JSONObject().put('myInstant', givenDate)

        when:
        JsonPath response = api.testDatesOK(request)

        then:
        assert response.get("data.myInstant") == expectedConvertedDate

        where:
        givenDate                   || expectedConvertedDate  | scenario
        "2000-01-01T00:00:00.000Z"  || "2000-01-01T00:00:00Z" | "Full string in proper ISO format"
        "2000-01-01T00:00:00.000"   || "2000-01-01T00:00:00Z" | "Full string, no timezone"
        "2000-01-01T00:00:00.00"    || "2000-01-01T00:00:00Z" | "Shorter millis"
        "2000-01-01T00:00:00.0"     || "2000-01-01T00:00:00Z" | "Shorter millis"
        "2000-01-01T00:00:00"       || "2000-01-01T00:00:00Z" | "No millis"
        "2000-01-01T00:00"          || "2000-01-01T00:00:00Z" | "No seconds"
        "2000-01-01"                || "2000-01-01T00:00:00Z" | "No time"
        "2000 01 01"                || "2000-01-01T00:00:00Z" | "Date with spaces"
        "2000/01/01"                || "2000-01-01T00:00:00Z" | "Date with slashes"
        "20000101"                  || "2000-01-01T00:00:00Z" | "Date with no separator"
        "2000-01-01 00:00:00.000Z"  || "2000-01-01T00:00:00Z" | "Replace 'T' with space"
        "2000-01-01T000000000Z"     || "2000-01-01T00:00:00Z" | "Time with no separator"
        "2000-01-01T00000000Z"      || "2000-01-01T00:00:00Z" | "Time with no separator, shorter millis"
        "2000-01-01T0000000Z"       || "2000-01-01T00:00:00Z" | "Time with no separator, shorter millis"
        "2000-01-01T000000Z"        || "2000-01-01T00:00:00Z" | "Time with no separator, no millis"
        "2000-01-01T0000Z"          || "2000-01-01T00:00:00Z" | "Time with no separator, no seconds"
        "2000-01-01T00:00:00:000Z"  || "2000-01-01T00:00:00Z" | "Time with millis replace '.' with ':'"
        "2000-01-01T00:00:00:00Z"   || "2000-01-01T00:00:00Z" | "Time with millis replace '.' with ':', shorter millis"
        "2000-01-01T00:00:00:0Z"    || "2000-01-01T00:00:00Z" | "Time with millis replace '.' with ':', shorter millis"
        // It can handle various timezone inputs. Yay!
        "2000-01-01T00:00:00-05"    || "2000-01-01T05:00:00Z" | "Timezone conforming to X format"
        "2000-01-01T00:00:00-0500"  || "2000-01-01T05:00:00Z" | "Timezone conforming to XX format"
        "2000-01-01T00:00:00-05:00" || "2000-01-01T05:00:00Z" | "Timezone conforming to XXX format"
    }

    void 'test LocalDate: #givenDate'() {
        given:
        JSONObject request = new JSONObject().put('myLocalDate', givenDate)

        when:
        JsonPath response = api.testDatesOK(request)

        then:
        assert response.get("data.myLocalDate") == expectedResult

        where:
        givenDate    || expectedResult | scenario
        "2000-01-01" || "2000-01-01"   | "Date with dashes"
        "2000 01 01" || "2000-01-01"   | "Date with spaces"
        "2000/01/01" || "2000-01-01"   | "Date with slashes"
        "20000101"   || "2000-01-01"   | "Date with no separator"
    }

    void 'test Date: #scenario'() {
        given:
        JSONObject request = new JSONObject().put('myDate', givenDate)

        when:
        JsonPath response = api.testDatesOK(request)

        then:
        assert response.get("data.myDate") == expectedResult
        assert response.get("data.myDateToString") == expectedToStringResult

        // See how the resulting format changes depending on whether you call Date.toString() or not in the domain's
        // toJson() method. This inconsistency is confusing and error-prone! Not a huge issue though because we'd
        // notice immediately if we accidentally did Date.toString() because it'd break the frontend.
        where:
        givenDate                    || expectedResult         | expectedToStringResult         | scenario
        // See how we input no tz information, but the server decides to treat it as local time (which for me currently
        // is CST). If someone runs this test against a server in a different tz, it'll fail. Bad!
        // To fix this, we need to add "TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))" to BootStrap.groovy
        "01/01/2000"                 || "2000-01-01T06:00:00Z" | "Sat Jan 01 00:00:00 CST 2000" | "Date only"

        // It handles Z (aka UTC) fine if it's provided (though toString() still displays local time).
        "01/01/2000 00:00:00 Z"      || "2000-01-01T00:00:00Z" | "Fri Dec 31 18:00:00 CST 1999" | "Datetime UTC"
        "2000-01-01T00:00:00Z"       || "2000-01-01T00:00:00Z" | "Fri Dec 31 18:00:00 CST 1999" | "Datetime ISO UTC"
        "01/01/2000 00:00 Z"         || "2000-01-01T00:00:00Z" | "Fri Dec 31 18:00:00 CST 1999" | "Datetime no seconds UTC"
        "2000-01-01T00:00Z"          || "2000-01-01T00:00:00Z" | "Fri Dec 31 18:00:00 CST 1999" | "Datetime no seconds ISO UTC"

        // It handles other timezone info fine if it's provided (though toString() still displays local time).
        "01/01/2000 00:00:00 -01:00" || "2000-01-01T01:00:00Z" | "Fri Dec 31 19:00:00 CST 1999" | "Datetime -01:00"
        "2000-01-01T00:00:00-01:00"  || "2000-01-01T01:00:00Z" | "Fri Dec 31 19:00:00 CST 1999" | "Datetime ISO -01:00"
        "01/01/2000 00:00 -01:00"    || "2000-01-01T01:00:00Z" | "Fri Dec 31 19:00:00 CST 1999" | "Datetime no seconds -01:00"
        "2000-01-01T00:00-01:00"     || "2000-01-01T01:00:00Z" | "Fri Dec 31 19:00:00 CST 1999" | "Datetime no seconds ISO -01:00"
    }
}
