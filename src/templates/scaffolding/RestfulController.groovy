/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
<%=packageName ? "package ${packageName}\n\n" : ''%>

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class ${className}Controller {

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond ${className}.list(params), [status: OK]
    }

    @Transactional
    def save(${className} ${propertyName}) {
        if (${propertyName} == null) {
            render status: NOT_FOUND
            return
        }

        ${propertyName}.validate()
        if (${propertyName}.hasErrors()) {
            render status: NOT_ACCEPTABLE
            return
        }

        ${propertyName}.save flush:true
        respond ${propertyName}, [status: CREATED]
    }

    @Transactional
    def update(${className} ${propertyName}) {
        if (${propertyName} == null) {
            render status: NOT_FOUND
            return
        }

        ${propertyName}.validate()
        if (${propertyName}.hasErrors()) {
            render status: NOT_ACCEPTABLE
            return
        }

        ${propertyName}.save flush:true
        respond ${propertyName}, [status: OK]
    }

    @Transactional
    def delete(${className} ${propertyName}) {

        if (${propertyName} == null) {
            render status: NOT_FOUND
            return
        }

        ${propertyName}.delete flush:true
        render status: NO_CONTENT
    }
}
