package org.pih.warehouse.core

class LocationIdentifierService extends IdentifierService {

    @Override
    String getPropertyKey() {
        return "location"
    }

    @Override
    protected Integer countDuplicates(String locationNumber) {
        return Location.countByLocationNumber(locationNumber)
    }
}
