package org.pih.warehouse.inventory

import grails.databinding.BindUsing
import grails.validation.Validateable
import java.time.LocalDate

import org.pih.warehouse.core.Person

class CycleCountRequestUpdateCommand implements Validateable {

    @BindUsing({ obj, source -> CycleCountRequest.get(source['id'] as Serializable) })
    CycleCountRequest cycleCountRequest

    Person requestedCountBy
    LocalDate requestedCountDate
    Person requestedRecountBy
    LocalDate requestedRecountDate

    static constraints = {
        requestedCountBy(nullable: true)
        requestedCountDate(nullable: true)
        requestedRecountBy(nullable: true)
        requestedRecountDate(nullable: true)
    }
}
