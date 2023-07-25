package org.pih.warehouse.core

import grails.gorm.services.Service

@Service(LocationRole)
interface LocationRoleDataService {
    LocationRole get(String id)

    void delete(String id)
}
