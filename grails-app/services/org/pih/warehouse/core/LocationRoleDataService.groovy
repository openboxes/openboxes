package org.pih.warehouse.core

import grails.gorm.services.Service

@Service(LocationRole)
abstract class LocationRoleDataService {
    abstract LocationRole get(String id)

    abstract void delete(String id)
}
