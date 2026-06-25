package org.pih.warehouse.core

import grails.gorm.services.Service

@Service(value = Location, name = "locationGormService")

interface LocationDataService {
    void delete(String id)

    Location save(Location location)

    Location get(String id)
}
