/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.core

class InternalLocation {

    String id
    String locationName
    String locationNumber
    Integer quantityOnHand
    Integer quantityAvailable
    Boolean isLocked
    Boolean isAvailable
    Boolean allowsMultipleItems
    Integer productCount
    Integer inventoryItemCount

    String supportedActivitiesString

    Location location
    LocationType locationType
    Location parentLocation

    List<ActivityCode> getSupportedActivities() {
        return supportedActivitiesString?.split(",").collect { String supportedActivity -> ActivityCode.valueOf(supportedActivity) }
    }

    static mapping = {
        id generator: "assigned"
        cache true
        version false
    }

    Map toJson() {
        return [
                id                  : id,
                name                : locationName,
                locationNumber      : locationNumber,
                locationType        : locationType?.name,
                locationTypeCode    : locationType?.locationTypeCode?.name(),
                zoneId              : location?.zone?.id,
                zoneName            : location?.zone?.name,
                isAvailable         : isAvailable,
                isLocked            : isLocked,
                allowsMultipleItems : allowsMultipleItems,
                productCount        : productCount,
                inventoryItemCount  : inventoryItemCount,
                hasQuantityAvailable: (quantityAvailable > 0),
                hasQuantityOnHand   : (quantityOnHand > 0)
        ]
    }
}
