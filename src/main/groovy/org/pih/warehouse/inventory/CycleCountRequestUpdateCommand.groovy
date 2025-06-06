package org.pih.warehouse.inventory

import grails.databinding.BindUsing
import grails.validation.Validateable
import java.time.LocalDate

import org.pih.warehouse.core.Person

class CycleCountRequestUpdateCommand implements Validateable {

    @BindUsing({ obj, source -> CycleCountRequest.get(source['id'] as Serializable) })
    CycleCountRequest cycleCountRequest

    Person countAssignee
    LocalDate countDeadline
    Person recountAssignee
    LocalDate recountDeadline

    static constraints = {
        countAssignee(nullable: true)
        countDeadline(nullable: true)
        recountAssignee(nullable: true)
        recountDeadline(nullable: true)
    }
}
