package org.pih.warehouse.api

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Person

@Validateable
class Putaway {

    String id
    String putawayNumber
    Person putawayAssignee
    Date putawayDate

    PutawayStatus putawayStatus
    List<PutawayItem> putawayItems = []
    //LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(PutawayItem.class));

    static constrants = {
        putawayNumber(nullable:true)
        putawayStatus(nullable:true)
        putawayAssignee(nullable:true)
        putawayDate(nullable:true)
        putawayItems(nullable:true)
    }

    Map toJson() {
        return [
                id: id,
                putawayNumber: putawayNumber,
                putawayStatus: putawayStatus?.name(),
                putawayDate: putawayDate,
                putawayAssignee: putawayAssignee,
                putawayItems: putawayItems.collect { it?.toJson() }
        ]
    }

}
