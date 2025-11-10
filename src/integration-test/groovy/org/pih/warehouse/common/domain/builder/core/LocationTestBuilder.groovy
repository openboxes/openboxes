package org.pih.warehouse.common.domain.builder.core

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType

@InheritConstructors
class LocationTestBuilder extends TestBuilder<Location> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                name: "Test Location",
                description: "A location to be used by tests. Can be deleted safely.",
        ]
    }

    LocationTestBuilder name(String name) {
        args.name = name
        return this
    }

    LocationTestBuilder asFacility() {
       args.locationType = LocationType.read(2)  // Depot
       args.organization = Location.get(1).organization  // Use the same org as the "Main Warehouse"
       return this
    }

    LocationTestBuilder asBinLocation(Location facility) {
        args.locationType = LocationType.read("cab2b4f35ba2d867015ba2e17e390001")  // Bin Location
        args.parentLocation = facility
        return this
    }

    Location findOrBuildMainFacility() {
        return Location.createCriteria().get {
            // The "Main Warehouse" Depot location created in the changelog-insert-data.groovy migration. We put this
            // logic in the builder because it allows us to change the behaviour in the future if we want, such as
            // refactoring this method to not rely on static data and instead actually call findOrBuild().
            eq("id", "1")
            // We eager fetch all the associated entities so that we don't get LazyInitializationException when
            // trying to access them later in the tests after the hibernate session has been closed.
            join("inventory")
            join("organization")
            join("parentLocation")
            join("locationType")
            join("locationGroup")
        } as Location
    }
}
