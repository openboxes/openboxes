package org.pih.warehouse.api.regression.mobile

import spock.lang.PendingFeature

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

/**
 * Locations: the location picker, location / bin lookups by scanned code, bin
 * searches, product summary of a location, and the internal location detail
 * screen. Runs in Main Warehouse (Location 1) with bins made per feature.
 */
class LocationContractSpec extends MobileContractSpec {

    Location bin
    Product p

    void setupData() {
        bin = newBin('LC-BIN', [ActivityCode.PICK_STOCK.id, ActivityCode.PUTAWAY_STOCK.id])
        p = newProduct('LC')
        stock(p, [[bin, 7, "LOT-${uid()}".toString(), new Date() + 365]])
    }

    @Covers('src/apis/locations.ts#getLocations')
    void 'GET /locations?locationTypeCode=DEPOT&activityCodes=MANAGE_INVENTORY&applyUserFilter=true lists the depots'() {
        when:
        CallResult r = call('src/apis/locations.ts#getLocations')

        then:
        r.holds()
        (r.data.data as List<Map>)*.id.contains('1')
    }

    @Covers(['src/apis/locations.ts#searchLocationByLocationNumber', 'src/apis/createTransfer.ts#lookupLocationByCode'])
    void 'GET /locations/{scanned location number} answers the bin'() {
        when:
        CallResult byNumber = call('src/apis/locations.ts#searchLocationByLocationNumber', [locationNumber: bin.locationNumber])
        CallResult byCode = call('src/apis/createTransfer.ts#lookupLocationByCode', [code: bin.locationNumber])

        then:
        byNumber.holds()
        byCode.holds()
        byNumber.data.data.id == bin.id
        byCode.data.data.id == bin.id
    }

    @Covers('src/apis/locations.ts#fetchProductSummary')
    void 'GET /locations/{id}/productSummary answers the stock summary of the location'() {
        when:
        CallResult r = call('src/apis/locations.ts#fetchProductSummary', [id: '1'])

        then:
        r.holds()
        (r.data.data as List<Map>).find { it.productCode == p.productCode }?.quantityOnHand == 7
    }

    @Covers('src/apis/locations.ts#getBinLocations')
    void 'GET /internalLocations answers (saga wired, never dispatched)'() {
        when:
        CallResult r = call('src/apis/locations.ts#getBinLocations')

        then:
        r.holds()
    }

    @Covers('src/apis/locations.ts#internalLocations')
    void 'GET /internalLocations/?location.id= answers the bins of the facility (saga wired, never dispatched)'() {
        when:
        CallResult r = call('src/apis/locations.ts#internalLocations', [id: '1'])

        then:
        r.status == 200
    }

    @Covers('src/apis/locations.ts#internalLocations')
    @PendingFeature(reason = 'APP/SERVER MISMATCH (dead path): src/apis/locations.ts internalLocations is consumed as a bare array; InternalLocationApiController.list answers {data:[...]}')
    void 'GET /internalLocations/?location.id= answers a bare array as the saga expects'() {
        when:
        CallResult r = call('src/apis/locations.ts#internalLocations', [id: '1'])

        then:
        r.json instanceof List
    }

    @Covers('src/apis/locations.ts#searchInternalLocations')
    void 'GET /internalLocations/search with a typed term (global search) finds the bin'() {
        when:
        CallResult r = call('src/apis/locations.ts#searchInternalLocations', [searchTerm: bin.locationNumber])

        then:
        r.holds()
        (r.data.data as List<Map>)*.id.contains(bin.id)
    }

    @Covers('src/apis/locations.ts#searchInternalLocations')
    @PendingFeature(reason = 'KNOWN MISMATCH (minLength guard): every screen that lists bins (InboundReceiveDetail, Transfer, PackingLocationPage, PutawayItem) calls searchInternalLocations with searchTerm= (empty) plus parentLocation.id/max/offset; InternalLocationApiController.search answers {data:[]} for any term shorter than openboxes.typeahead.minLength (3), so those bin pickers are always empty')
    void 'GET /internalLocations/search with the screens empty term lists the facility bins'() {
        when:
        CallResult r = call('src/apis/locations.ts#searchInternalLocations',
                [searchTerm: '', additionalParams: ['parentLocation.id': '1', locationTypeCode: 'BIN_LOCATION', max: 25, offset: 0]])

        then:
        r.holds()
        (r.data.data as List<Map>)*.id.contains(bin.id)
    }

    @Covers('src/apis/createTransfer.ts#searchDestinationBins')
    void 'GET /internalLocations/search for destination bins finds the bin by its code'() {
        when:
        CallResult r = call('src/apis/createTransfer.ts#searchDestinationBins', [searchTerm: bin.locationNumber])

        then:
        r.holds()
        (r.data.data as List<Map>)*.id.contains(bin.id)
    }

    @Covers('src/apis/createTransfer.ts#searchDestinationBins')
    @PendingFeature(reason = 'APP/SERVER MISMATCH: src/apis/createTransfer.ts searchDestinationBins sends ignoreActivityCodes=RECEIVE_STOCK so receiving bins are not offered as transfer destinations; InternalLocationApiController.search ignores it (only listReceiving honours it) and returns receiving bins')
    void 'GET /internalLocations/search for destination bins leaves out receiving bins'() {
        given:
        String token = "RCVX-${uid()}".toString()
        Location receiving = newBin(token, [ActivityCode.RECEIVE_STOCK.id])

        when:
        CallResult r = call('src/apis/createTransfer.ts#searchDestinationBins', [searchTerm: receiving.locationNumber])

        then:
        r.holds([], [])
        !(r.data.data as List<Map>)*.id.contains(receiving.id)
    }

    @Covers('src/apis/locations.ts#internalLocationDetails')
    @PendingFeature(reason = 'KNOWN GAP (no route): src/apis/locations.ts internalLocationDetails (InternalLocationDetails screen, opened from Scan for a Location) calls GET /internalLocations/{id}/details; OBAF maps no such URL and InternalLocationApiController has no details action (the unmapped URL then fails as 500, see the 404 feature below)')
    void 'GET /internalLocations/{id}/details answers the bin detail'() {
        when:
        CallResult r = call('src/apis/locations.ts#internalLocationDetails', [id: bin.id])

        then:
        r.holds()
    }

    @Covers('src/apis/locations.ts#internalLocationsDetails')
    @PendingFeature(reason = 'KNOWN GAP (no route): src/apis/locations.ts internalLocationsDetails calls GET /internalLocations/{id}/details?location.id=; OBAF maps no such URL and InternalLocationApiController has no details action (the unmapped URL then fails as 500)')
    void 'GET /internalLocations/{id}/details?location.id= answers the bin with its stock'() {
        when:
        CallResult r = call('src/apis/locations.ts#internalLocationsDetails', [id: bin.id, location: '1'])

        then:
        r.holds()
    }

    @PendingFeature(reason = 'SERVER DEFECT: with the JSON "404" mapping commented out in UrlMappings, an unmapped /api URL (e.g. GET /internalLocations/{id}/details) is forwarded to the container error page, where SecurityInterceptor fails with HibernateException "No Session found for current thread": the app gets 500 instead of 404')
    void 'an unmapped API URL answers 404'() {
        when:
        CallResult r = call('src/apis/locations.ts#internalLocationDetails', [id: bin.id])

        then:
        r.status == 404
    }
}
