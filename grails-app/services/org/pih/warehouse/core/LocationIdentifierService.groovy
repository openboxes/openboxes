package org.pih.warehouse.core

class LocationIdentifierService extends IdentifierService<Location> {

    @Override
    String getIdentifierName() {
        return "location"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        return Location.countByLocationNumber(id)
    }
}
