package org.pih.warehouse.api.spec.core

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.core.InternalLocationApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode

/**
 * Covers LocationService#searchInternalLocations (via InternalLocationApiController#search), specifically its
 * activityCodes filter and how it mirrors Location#supports(): a location's own supportedActivities take
 * precedence when present, otherwise its location type's supportedActivities are used as a fallback. Also
 * covers case-insensitive searchTerm matching, inactive exclusion, and totalCount accuracy (including under
 * pagination).
 */
class InternalLocationApiSpec extends ApiSpec {

    @Autowired
    InternalLocationApiWrapper internalLocationApiWrapper

    @Shared
    String runId

    @Shared
    String searchTerm

    @Shared
    LocationType typeWithActivity

    @Shared
    LocationType typeWithoutActivity

    // Own supportedActivities include the target activity - should match regardless of location type.
    @Shared
    Location ownMatch1

    @Shared
    Location ownMatch2

    // Own supportedActivities are non-empty but don't include the target activity, even though the location
    // type does - own supportedActivities should take precedence and the location type should be ignored.
    @Shared
    Location ownNonMatch

    // No own supportedActivities - should fall back to the location type, which supports the target activity.
    @Shared
    Location typeFallbackMatch

    // No own supportedActivities, and the location type doesn't support the target activity either.
    @Shared
    Location typeFallbackNonMatch

    // Matches on activity like ownMatch1/2, but inactive - should be excluded unless includeInactive is set.
    @Shared
    Location inactiveMatch

    @Override
    void setupData() {
        runId = UUID.randomUUID().toString().take(8)
        searchTerm = "IntLocSpec-${runId}"

        typeWithActivity = build([save: true], LocationType, [
                name              : "${searchTerm} Type With Activity",
                locationTypeCode  : LocationTypeCode.BIN_LOCATION,
                supportedActivities: [ActivityCode.PUTAWAY_CART.id] as Set,
        ])

        typeWithoutActivity = build([save: true], LocationType, [
                name              : "${searchTerm} Type Without Activity",
                locationTypeCode  : LocationTypeCode.BIN_LOCATION,
                supportedActivities: [ActivityCode.PICK_STOCK.id] as Set,
        ])

        ownMatch1 = build([save: true], Location, [
                name               : "${searchTerm} Own Match 1",
                parentLocation     : facility,
                locationType       : typeWithoutActivity,
                active             : true,
                supportedActivities: [ActivityCode.PUTAWAY_CART.id] as Set,
        ])

        ownMatch2 = build([save: true], Location, [
                name               : "${searchTerm} Own Match 2",
                parentLocation     : facility,
                locationType       : typeWithoutActivity,
                active             : true,
                supportedActivities: [ActivityCode.PUTAWAY_CART.id] as Set,
        ])

        ownNonMatch = build([save: true], Location, [
                name               : "${searchTerm} Own Non Match",
                parentLocation     : facility,
                locationType       : typeWithActivity,
                active             : true,
                supportedActivities: [ActivityCode.PICK_STOCK.id] as Set,
        ])

        typeFallbackMatch = build([save: true], Location, [
                name               : "${searchTerm} Type Fallback Match",
                parentLocation     : facility,
                locationType       : typeWithActivity,
                active             : true,
                supportedActivities: [] as Set,
        ])

        typeFallbackNonMatch = build([save: true], Location, [
                name               : "${searchTerm} Type Fallback Non Match",
                parentLocation     : facility,
                locationType       : typeWithoutActivity,
                active             : true,
                supportedActivities: [] as Set,
        ])

        inactiveMatch = build([save: true], Location, [
                name               : "${searchTerm} Inactive Match",
                parentLocation     : facility,
                locationType       : typeWithoutActivity,
                active             : false,
                supportedActivities: [ActivityCode.PUTAWAY_CART.id] as Set,
        ])
    }

    @Override
    void cleanupData() {
        [ownMatch1, ownMatch2, ownNonMatch, typeFallbackMatch, typeFallbackNonMatch, inactiveMatch].each {
            Location.get(it?.id)?.delete(flush: true)
        }
        [typeWithActivity, typeWithoutActivity].each {
            LocationType.get(it?.id)?.delete(flush: true)
        }
    }

    private Map<String, Object> baseParams() {
        return [
                searchTerm        : searchTerm,
                'parentLocation.id': facility.id,
        ]
    }

    void 'search without an activityCodes filter should return all active matching locations regardless of activity'() {
        expect:
        internalLocationApiWrapper.api.search(baseParams(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.containsInAnyOrder(
                        ownMatch1.id, ownMatch2.id, ownNonMatch.id, typeFallbackMatch.id, typeFallbackNonMatch.id))
                .expectBody('totalCount', Matchers.equalTo(5))
                .build())
    }

    void 'search should match searchTerm case-insensitively against name'() {
        expect:
        internalLocationApiWrapper.api.search(baseParams() + [searchTerm: searchTerm.toUpperCase()], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(ownMatch1.id))
                .build())
    }

    void 'search with activityCodes should include a location whose own supportedActivities include the activity'() {
        expect:
        internalLocationApiWrapper.api.search(baseParams() + [activityCodes: ActivityCode.PUTAWAY_CART.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(ownMatch1.id))
                .build())
    }

    void 'search with activityCodes should exclude a location whose own supportedActivities exist but do not include the activity, even though its location type supports it'() {
        expect:
        internalLocationApiWrapper.api.search(baseParams() + [activityCodes: ActivityCode.PUTAWAY_CART.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(ownNonMatch.id)))
                .build())
    }

    void 'search with activityCodes should include a location with no own supportedActivities when its location type supports the activity'() {
        expect:
        internalLocationApiWrapper.api.search(baseParams() + [activityCodes: ActivityCode.PUTAWAY_CART.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(typeFallbackMatch.id))
                .build())
    }

    void 'search with activityCodes should exclude a location with no own supportedActivities when its location type does not support the activity either'() {
        expect:
        internalLocationApiWrapper.api.search(baseParams() + [activityCodes: ActivityCode.PUTAWAY_CART.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(typeFallbackNonMatch.id)))
                .build())
    }

    void 'search with activityCodes should have an accurate totalCount reflecting only the activity-matching locations'() {
        expect:
        internalLocationApiWrapper.api.search(baseParams() + [activityCodes: ActivityCode.PUTAWAY_CART.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('totalCount', Matchers.equalTo(3))
                .build())
    }

    void 'search should exclude inactive locations by default even if they match the activity filter'() {
        expect:
        internalLocationApiWrapper.api.search(baseParams() + [activityCodes: ActivityCode.PUTAWAY_CART.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(inactiveMatch.id)))
                .build())
    }

    void 'search should include inactive locations when includeInactive is set'() {
        expect:
        internalLocationApiWrapper.api.search(
                baseParams() + [activityCodes: ActivityCode.PUTAWAY_CART.id, includeInactive: true],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.id', Matchers.hasItem(inactiveMatch.id))
                        .expectBody('totalCount', Matchers.equalTo(4))
                        .build())
    }

    void 'search with offset should return the remaining page of results with a consistent totalCount and no overlap with the first page'() {
        given:
        List<String> expectedIds = [ownMatch1.id, ownMatch2.id, ownNonMatch.id, typeFallbackMatch.id, typeFallbackNonMatch.id]

        when:
        List<String> page1 = internalLocationApiWrapper.api
                .search(baseParams() + [max: 3, offset: 0], responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getList('data.id', String)
        List<String> page2 = internalLocationApiWrapper.api
                .search(baseParams() + [max: 3, offset: 3], responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getList('data.id', String)

        then:
        page1.size() == 3
        page2.size() == 2
        page1.intersect(page2).isEmpty()
        (page1 + page2).toSet() == expectedIds.toSet()
    }

    void 'search with activityCodes and a max lower than the number of matches should cap the results but keep an accurate totalCount'() {
        expect:
        internalLocationApiWrapper.api.search(baseParams() + [activityCodes: ActivityCode.PUTAWAY_CART.id, max: 2], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.size()', Matchers.equalTo(2))
                .expectBody('totalCount', Matchers.equalTo(3))
                .build())
    }
}
