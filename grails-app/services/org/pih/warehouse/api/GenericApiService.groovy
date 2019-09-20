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

import grails.validation.ValidationException
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.hibernate.SessionFactory
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.Restrictions

class GenericApiService {

    boolean transactional = true

    SessionFactory sessionFactory
    GrailsApplication grailsApplication

    GrailsDomainClass getDomainClassByName(String className) {
        GrailsDomainClass grailsDomainClass = grailsApplication.domainClasses.find {
            it.clazz.simpleName == className
        }
        if (!grailsDomainClass) {
            throw new IllegalAccessException("Unable to locate domain ${className}")
        }
        return grailsDomainClass
    }

    Class getDomainClass(String resourceName) {
        def className = resourceName.capitalize()
        def domainClass = getDomainClassByName(className)
        if (!domainClass) {
            throw new IllegalAccessException("No domain class ${className} could be found")
        }
        return domainClass.clazz
    }

    List getList(String resourceName, Map params) {
        Class domainClass = getDomainClass(resourceName)
        List list = domainClass.list(params)
        return list
    }

    Object getObject(String resourceName, String id) {
        def domainClass = getDomainClass(resourceName)
        Object domainObject = domainClass.get(id)
        if (!domainObject) {
            throw new ObjectNotFoundException(id, domainClass.simpleName)
        }
        return domainObject
    }

    Object createObject(String resourceName, JSONObject jsonObject) {
        log.debug "Create object " + jsonObject.class + ": " + jsonObject
        def domainClass = getDomainClass(resourceName)

        def domainObject
        if (jsonObject.id) {
            domainObject = getObject(resourceName, jsonObject.id)
        } else {
            domainObject = domainClass.newInstance()
        }
        domainObject.properties = jsonObject
        if (domainObject.hasErrors() || !domainObject.save()) {
            throw new ValidationException("Cannot create product due to validation errors", domainObject.errors)
        }
        return domainObject
    }

    Object createObjects(String resourceName, JSONArray jsonArray) {
        log.debug "Create objects " + jsonArray.class + ": " + jsonArray
        def domainObjects = []
        jsonArray.each { JSONObject jsonObject ->
            domainObjects << createObject(resourceName, jsonObject)
        }
        return domainObjects
    }

    Object updateObject(String resourceName, String id, JSONObject jsonObject) {
        log.debug "Update " + jsonObject
        def domainObject = getObject(resourceName, id)
        domainObject.properties = jsonObject
        if (domainObject.hasErrors() || !domainObject.save()) {
            throw new ValidationException("Cannot create product due to validation errors", domainObject.errors)
        }
        return domainObject
    }

    boolean deleteObject(String resourceName, String id) {
        log.debug "Delete " + id
        def domainObject = getObject(resourceName, id)
        return domainObject.delete()
    }

    def searchObjects(String resourceName, JSONObject jsonObject, Map params) {
        Class domainClass = getDomainClass(resourceName)
        def session = sessionFactory.currentSession
        def criteria = session.createCriteria(domainClass)
        jsonObject.searchAttributes.each { attr ->
            Criterion criterion = buildCriterion(attr.property, attr.operator, attr.value)
            criteria.add(criterion)
        }
        return criteria.list()
    }

    Criterion buildCriterion(String propertyName, String operator, String value) {
        if (!propertyName || !value) {
            throw new IllegalArgumentException("Property and value are required in order to perform searches")
        }

        // Default operator should be eq
        operator = operator ?: "eq"

        switch (operator) {

            case "eq":
                Restrictions.eq(propertyName, value)
                break
            case "like":
                Restrictions.like(propertyName, value)
                break
            case "ilike":
                Restrictions.ilike(propertyName, value)
                break
            default:
                throw new UnsupportedOperationException("Operator ${operator} is not supported at this time")
        }
    }

}
