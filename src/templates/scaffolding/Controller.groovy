/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
<%=packageName ? "package ${packageName}\n\n" : ''%>class ${className}Controller {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [${propertyName}List: ${className}.list(params), ${propertyName}Total: ${className}.count()]
    }

    def create = {
        def ${propertyName} = new ${className}()
        ${propertyName}.properties = params
        return [${propertyName}: ${propertyName}]
    }

    def save = {
        def ${propertyName} = new ${className}(params)
        if (${propertyName}.save(flush: true)) {
            flash.message = "\${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}'), ${propertyName}.id])}"
            redirect(action: "list", id: ${propertyName}.id)
        }
        else {
            render(view: "create", model: [${propertyName}: ${propertyName}])
        }
    }

    def show = {
        def ${propertyName} = ${className}.get(params.id)
        if (!${propertyName}) {
            flash.message = "\${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
            redirect(action: "list")
        }
        else {
            [${propertyName}: ${propertyName}]
        }
    }

    def edit = {
        def ${propertyName} = ${className}.get(params.id)
        if (!${propertyName}) {
            flash.message = "\${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [${propertyName}: ${propertyName}]
        }
    }

    def update = {
        def ${propertyName} = ${className}.get(params.id)
        if (${propertyName}) {
            if (params.version) {
                def version = params.version.toLong()
                if (${propertyName}.version > version) {
                    <% def lowerCaseName = grails.util.GrailsNameUtils.getPropertyName(className) %>
                    ${propertyName}.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}')] as Object[], "Another user has updated this ${className} while you were editing")
                    render(view: "edit", model: [${propertyName}: ${propertyName}])
                    return
                }
            }
            ${propertyName}.properties = params
            if (!${propertyName}.hasErrors() && ${propertyName}.save(flush: true)) {
                flash.message = "\${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}'), ${propertyName}.id])}"
                redirect(action: "list", id: ${propertyName}.id)
            }
            else {
                render(view: "edit", model: [${propertyName}: ${propertyName}])
            }
        }
        else {
            flash.message = "\${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def ${propertyName} = ${className}.get(params.id)
        if (${propertyName}) {
            try {
                ${propertyName}.delete(flush: true)
                flash.message = "\${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "\${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "\${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
            redirect(action: "list")
        }
    }
}
