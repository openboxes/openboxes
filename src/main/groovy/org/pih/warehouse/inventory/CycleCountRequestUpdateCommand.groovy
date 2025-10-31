package org.pih.warehouse.inventory

import grails.databinding.SimpleMapDataBindingSource
import grails.web.databinding.DataBindingUtils

import java.time.LocalDate

import org.pih.warehouse.core.Person

class CycleCountRequestUpdateCommand {
    Map<String, CycleCountAssignmentCommand> assignments
    CycleCountRequest cycleCountRequest

    void setAssignments(Map input) {
        assignments = input.collectEntries { k, v ->
            CycleCountAssignmentCommand command = new CycleCountAssignmentCommand()
            DataBindingUtils.bindObjectToInstance(command, new SimpleMapDataBindingSource(v))
            [(k): command]
        }
    }

    CycleCountAssignmentCommand getAssignmentByCountIndex(String countIndex) {
        return assignments.get(countIndex)
    }
}

class CycleCountAssignmentCommand {
    Person assignee
    LocalDate deadline
}
