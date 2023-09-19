package org.pih.warehouse.core

import grails.core.GrailsApplication
import grails.util.Environment
import grails.util.Holders
import org.springframework.context.ApplicationContext

import java.lang.reflect.Modifier

class EventService {
    static transactional = false


    GrailsApplication grailsApplication
    /**
     * The global map of event names to the list of closures that need to be invoked when the event is fired.
     * Once populated during the bootstrap process, it should not be changed at runtime (not thread safe)
     */
    private Map<String, List<Closure>> globalEventObservers = new HashMap<String, List<Closure>>()


    /**
     * Scans through the collection of classes looking for closures that start with "onEvent" and adds these
     * to the global list of event observers (using the EventService class).
     * We currently do no validation, but the closure must be defined with the following signature:
     *  "static def onEventXXX = {...}"
     * Note that the static is optional for Service classes (whereby it assumed that the service is of singleton scope)
     * but is manditory for domain classes.
     * Set the log4j level to "trace" to view the list of closures that are added to the global list
     */
    void autoWireEvents() {
        log.info "Autowiring events"
        grailsApplication.getArtefacts("Service").each { theClass ->
            log.info "1Autowiring events:${theClass}"
            theClass.clazz.metaClass.properties.each { MetaProperty prop ->
                log.info "2Autowiring events:${theClass}"
                if(prop.name.startsWith('onEvent'))  {
                    log.info "3Autowiring events:${theClass}"
                    if(Environment.current == Environment.PRODUCTION){
                        log.debug "******* SERVICE CLASS METAPROP for ${theClass.clazz.name}: ${prop.dump()}"
                    }
                    if(Modifier.isStatic(prop.modifiers)) {
                        log.info "Registring event ${theClass.clazz.name}: ${prop.dump()}"
                        register("$prop.name", theClass.clazz."$prop.name") // class level method
                    }
                    else { // not a static closure
                        log.info "Registring event ${theClass.clazz.name}: ${prop.dump()}"
                        ApplicationContext ctx = (ApplicationContext) Holders.getApplicationContext();
                        def serviceInstance = ctx.getBean(theClass.clazz) // we are assuming that the service scope is singleton
                        register("$prop.name", serviceInstance."$prop.name")
                    }
                }
            }
        }
    }

    /**
     * Register a closure for a particular event name. The closure could either be defined in a service or a domain
     * object and MUST BE STATIC for obvious reasons.
     * By convention, the @eventName must be defined in AppEvents.groovy
     * Once populated during the bootstrap process, it should not be changed at runtime (not thread safe)
     */
    boolean register(String eventName, Closure observer) {
        List<Closure> observers = globalEventObservers.get(eventName)
        if(observers == null) {
            observers = new ArrayList<Closure>()
            globalEventObservers.put(eventName, observers)
        }
        if(Environment.current == Environment.PRODUCTION) {
            log.info "Registering [$eventName] for observer: [${observer.dump()}]"
        }
        return observers.add(observer)
    }

    /**
     * This method is called by the event publisher to notify all registered subscribers about a biz. event.
     * By convention, the @eventName must be defined in AppEvents.groovy
     */
    def event(String eventName, Object... args) {
        // first do the synchronous observers
        List<Closure> observers = globalEventObservers.get(eventName)
        if (observers) {
            observers.each { Closure observer ->
                log.info "EventService invoking Event [$eventName] with closure [${observer.dump()}]"
                if (args?.size() == 1) {
                    observer(args[0])
                } else if (args?.size() == 2) {
                    observer(args[0], args[1])
                } else if (args?.size() == 3) {
                    observer(args[0], args[1], args[2])
                }
            }
        }
    }
}
