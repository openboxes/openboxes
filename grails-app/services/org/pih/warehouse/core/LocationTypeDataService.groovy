package org.pih.warehouse.core

import grails.gorm.services.Service

@Service(LocationType)
interface LocationTypeDataService {

    LocationType save(LocationType locationType)

    void delete(String id)

    LocationType get(String id)
}
