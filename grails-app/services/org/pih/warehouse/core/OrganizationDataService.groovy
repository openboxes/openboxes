package org.pih.warehouse.core
import grails.gorm.services.Service

@Service(Organization)
interface OrganizationDataService {
    void delete(String id)

    Organization save(Organization productGroup)

    Organization get(String id)
}
