package org.pih.warehouse.core

import grails.gorm.services.Service

@Service(LocationGroup)
interface LocationGroupDataService {

    LocationGroup get(String id)

    LocationGroup save(LocationGroup locationGroup)
}
