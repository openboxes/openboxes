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

import org.apache.commons.collections.comparators.NullComparator
import util.ConfigHelper;

// import java.text.DecimalFormat
// import java.text.SimpleDateFormat

class LocationService {
	
	def grailsApplication
	boolean transactional = true
	
	
	def getAllLocations() {
		return Location.findAllByActive(true);
	}

	def getLoginLocations(Integer currentLocationId) {
		return getLoginLocations(Location.get(currentLocationId))
	}
	
	def getLoginLocations(Location currentLocation) {
        log.info "Get login locations (currentLocation=${currentLocation?.name})"

        // Get all locations that match the required activity (using inclusive OR)
		def locations = new HashSet()
		def requiredActivities = ConfigHelper.listValue(grailsApplication.config.openboxes.chooseLocation.requiredActivities)
		if (requiredActivities) {
			requiredActivities.each { activity ->
				locations += getAllLocations()?.findAll { it.supports(activity) }
			}			
		}
		return locations
	}


	Map getLoginLocationsMap(Location currentLocation) {
        log.info "TEST Get login locations map (currentLocation=${currentLocation?.name})"
        def locationMap = [:]
        def nullHigh = new NullComparator(true)
        def locations = getLoginLocations(currentLocation)
        if (locations) {
            locationMap = locations.groupBy { it?.locationGroup }
            locationMap = locationMap.sort { a, b -> nullHigh.compare(a?.key, b?.key) }
        }
        return locationMap;
        //return getLoginLocations(currentLocation).sort { it?.locationGroup }.reverse().groupBy { it?.locationGroup }
	}
	
	List getDepots() {
		return getAllLocations()?.findAll { it.supports(ActivityCode.MANAGE_INVENTORY) }
	}

	List getNearbyLocations(Location currentLocation) { 
		return Location.findAllByActiveAndLocationGroup(true, currentLocation.locationGroup)
	}
	
	List getExternalLocations() { 
		return getAllLocations()?.findAll { it.supports(ActivityCode.EXTERNAL) } 
	}
	
	List getDispensaries(Location currentLocation) { 
		return getNearbyLocations(currentLocation)?.findAll { it.supports(ActivityCode.RECEIVE_STOCK) && !it.supports(ActivityCode.EXTERNAL) } 
	}
	
	List getLocationsSupportingActivity(ActivityCode activity) { 
		return getAllLocations()?.findAll { it.supports(activity) }
		
	}
	
	List getShipmentOrigins() { 
		return getLocationsSupportingActivity(ActivityCode.SEND_STOCK)
	}
	
	List getShipmentDestinations() {
		return getLocationsSupportingActivity(ActivityCode.RECEIVE_STOCK)
	}

	List getOrderSuppliers(Location currentLocation) {
		return getLocationsSupportingActivity(ActivityCode.FULFILL_ORDER) - currentLocation
	}

	List getRequestOrigins(Location currentLocation) {
		return getLocationsSupportingActivity(ActivityCode.FULFILL_REQUEST)// - currentLocation
	}

	List getRequestDestinations(Location currentLocation) {
		return getLocationsSupportingActivity(ActivityCode.FULFILL_REQUEST)// - currentLocation
	}

	List getTransactionSources(Location currentLocation) { 
		return getLocationsSupportingActivity(ActivityCode.SEND_STOCK) - currentLocation
	}
	
	List getTransactionDestinations(Location currentLocation) { 
		// Always get nearby locations		
		def locations = getNearbyLocations(currentLocation);		
		
		// Get all external locations (if supports external) 
		if (currentLocation.supports(ActivityCode.EXTERNAL)) { 			
			locations += getExternalLocations();			
		}

		// Of those locations remaining, we need to return only locations that can receive stock
		locations = locations.findAll { it.supports(ActivityCode.RECEIVE_STOCK) }
		
		// Remove current location from list
		locations = locations?.unique() - currentLocation

		return locations
		
	}
	
   
}
