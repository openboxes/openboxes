package org.pih.warehouse.core

import grails.databinding.BindUsing
import grails.validation.Validateable

import org.pih.warehouse.SortParam
import org.pih.warehouse.SortUtil

/**
 * Used to filter the returned results when listing/searching internal locations.
 */
class InternalLocationSearchCommand implements Validateable {

    Location location  // ie parent location, ie facility

    List<LocationTypeCode> locationTypeCode = LocationTypeCode.listInternalTypeCodes()

    List<String> locationNames = []

    List<ActivityCode> allActivityCodes = []
    List<ActivityCode> anyActivityCodes = []
    List<ActivityCode> ignoreActivityCodes = []

    @BindUsing({ obj, source -> SortUtil.bindSortParams(Location, source['sort'] as String) })
    List<SortParam> sortParams

    static constraints = {
        location(nullable: true)
        locationTypeCode(nullable: true)
        allActivityCodes(nullable: true)
        anyActivityCodes(nullable: true)
        ignoreActivityCodes(nullable: true)
    }
}
