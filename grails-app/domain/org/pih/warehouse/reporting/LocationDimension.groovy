/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.reporting

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType

class LocationDimension {

    Long id
    String locationName
    String locationNumber
    String locationGroupName
    String locationTypeCode
    String locationTypeName
    String parentLocationName

    Location location
    Location parentLocation
    LocationType locationType
    LocationGroup locationGroup

    static mapping = {
        id generator: 'increment'
        locationTypeCode indexColumn:[name:'location_type_code_idx']
    }


    static constraints = {
        locationName(nullable:false)
        locationNumber(nullable:true)
        locationGroupName(nullable:true)
        locationTypeCode(nullable:false)
        locationTypeName(nullable:false)
        location(nullable:false)
        locationType(nullable:false)
        locationGroup(nullable:true)
        parentLocation(nullable:true)
        parentLocationName(nullable:true)
    }
}
