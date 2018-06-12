/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.api

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.hibernate.ObjectNotFoundException

class GenericApiService {

    boolean transactional = true

    GrailsApplication grailsApplication

    GrailsDomainClass getDomainClassByName(String className) {
        GrailsDomainClass grailsDomainClass = grailsApplication.domainClasses.find { it.clazz.simpleName == className }
        if (!grailsDomainClass) {
            throw new IllegalAccessException("Unable to locate domain ${className}")
        }
        return grailsDomainClass
    }

    Class getDomainClass(String resourceName) {
        def className = resourceName.capitalize()
        def domainClass = getDomainClassByName(className)
        if (!domainClass) {
            throw new IllegalAccessException("No resource named ${className} could be found")
        }
        return domainClass.clazz
    }

    List getList(String resourceName, Map params) {
        Class domainClass = getDomainClass(resourceName)
        if (!domainClass) {
            throw new IllegalAccessException("Unable to locate domain ${resourceName}")
        }
        List list = domainClass.list(params)

        return list

    }

    Object getObject(String resourceName, String id) {
        def domainClass = getDomainClass(resourceName)
        if (!domainClass) {
            throw new IllegalAccessException("Unable to locate domain ${resourceName}")
        }
        Object object = domainClass.get(id)
        if (!object) {
            throw new ObjectNotFoundException(id, domainClass.simpleName)
        }
        return object
    }
}
