package org.pih.warehouse.core

import grails.gorm.transactions.Transactional

@Transactional
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
