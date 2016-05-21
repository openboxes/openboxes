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

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        ${className}.async.task {
            [${propertyName}List: list(params), count: count() ]
        }.then { result ->
            respond result.${propertyName}List, model:[${propertyName}Count: result.count]
        }
    }

    def show(String id) {
        ${className}.async.get(id).then { ${propertyName} ->
            respond ${propertyName}
        }
    }

    def create() {
        respond new ${className}(params)
    }

    def save(${className} ${propertyName}) {
        ${className}.async.withTransaction {
            if (${propertyName} == null) {
                notFound()
                return
            }

            if(${propertyName}.hasErrors()) {
                respond ${propertyName}.errors, view:'create' // STATUS CODE 422
                return
            }

            ${propertyName}.save flush:true
            request.withFormat {
                form multipartForm {
                    flash.message = message(code: 'default.created.message', args: [message(code: '${propertyName}.label', default: '${className}'), ${propertyName}.id])
                    redirect ${propertyName}
                }
                '*' { respond ${propertyName}, [status: CREATED] }
            }
        }
    }

    def edit(String id) {
        ${className}.async.get(id).then { ${propertyName} ->
            respond ${propertyName}
        }
    }

    def update(String id) {
        ${className}.async.withTransaction {
            def ${propertyName} = ${className}.get(id)
            if (${propertyName} == null) {
                notFound()
                return
            }

            ${propertyName}.properties = params
            if( !${propertyName}.save(flush:true) ) {
                respond ${propertyName}.errors, view:'edit' // STATUS CODE 422
                return
            }

            request.withFormat {
                form multipartForm {
                    flash.message = message(code: 'default.updated.message', args: [message(code: '${className}.label', default: '${className}'), ${propertyName}.id])
                    redirect ${propertyName}
                }
                '*'{ respond ${propertyName}, [status: OK] }
            }
        }
    }

    def delete(String id) {
        ${className}.async.withTransaction {
            def ${propertyName} = ${className}.get(id)
            if (${propertyName} == null) {
                notFound()
                return
            }

            ${propertyName}.delete flush:true

            request.withFormat {
                form multipartForm {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: '${className}.label', default: '${className}'), ${propertyName}.id])
                    redirect action:"index", method:"GET"
                }
                '*'{ render status: NO_CONTENT }
            }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: '${propertyName}.label', default: '${className}'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
