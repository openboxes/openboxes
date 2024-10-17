package org.pih.warehouse.core

class LocationIdentifierService extends IdentifierService {

    @Override
    String getEntityKey() {
        return "location"
    }

    @Override
    protected Integer countDuplicates(String locationNumber) {
        return Location.countByLocationNumber(locationNumber)
    }
}
