package spring

import org.grails.orm.hibernate.HibernateEventListeners
import org.pih.warehouse.event.PersistenceEventHandlerImpl

// Place your Spring DSL code here

beans = {
    auditListener(PersistenceEventHandlerImpl){
        eventService = ref("eventService")
    }

    hibernateEventListeners(HibernateEventListeners) {
        listenerMap = ['post-insert': auditListener,
                       'pre-update': auditListener,
                       'pre-delete': auditListener]
    }


}