package org.pih.warehouse.requisition

import grails.gorm.services.Join
import grails.gorm.services.Service

@Service(Requisition)
interface RequisitionDataService {

    void delete(String id)

    Requisition save(Requisition requisition)

    Requisition get(String id)

    @Join("events")
    Requisition getRequisitionWithEvents(String id)
}
