package org.pih.warehouse.core
import grails.gorm.services.Service

@Service(Organization)
interface OrganizationDataService {
    void delete(String id)

    Organization save(Organization organization)

    Organization get(String id)
}
