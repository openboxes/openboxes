package org.pih.warehouse.inventory

import grails.databinding.BindUsing
import grails.validation.Validateable
import java.time.LocalDate

import org.pih.warehouse.core.Person

class CycleCountRequestUpdateCommand implements Validateable {

    @BindUsing({ obj, source -> CycleCountRequest.get(source['id'] as Serializable) })
    CycleCountRequest cycleCountRequest

    Person countAssignedTo
    LocalDate countDeadline
    Person recountAssignedTo
    LocalDate recountDeadline

    static constraints = {
        countAssignedTo(nullable: true)
        countDeadline(nullable: true)
        recountAssignedTo(nullable: true)
        recountDeadline(nullable: true)
    }
}
