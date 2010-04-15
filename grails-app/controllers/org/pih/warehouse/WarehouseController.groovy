package org.pih.warehouse

class WarehouseController {
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def beforeInterceptor = [action:this.&auth,except:'login']

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [warehouseInstanceList: Warehouse.list(params), warehouseInstanceTotal: Warehouse.count()]
    }

    def create = {
        def warehouseInstance = new Warehouse()
        warehouseInstance.properties = params
        return [warehouseInstance: warehouseInstance]
    }

    def save = {
        def warehouseInstance = new Warehouse(params)
        if (warehouseInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), warehouseInstance.id])}"
            redirect(action: "show", id: warehouseInstance.id)
        }
        else {
            render(view: "create", model: [warehouseInstance: warehouseInstance])
        }
    }

    def show = {
        def warehouseInstance = Warehouse.get(params.id)
        if (!warehouseInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
            redirect(action: "list")
        }
        else {
            [warehouseInstance: warehouseInstance]
        }
    }

    def edit = {
        def warehouseInstance = Warehouse.get(params.id)
        if (!warehouseInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [warehouseInstance: warehouseInstance]
        }
    }

    def update = {
        def warehouseInstance = Warehouse.get(params.id)
        if (warehouseInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (warehouseInstance.version > version) {
                    
                    warehouseInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'warehouse.label', default: 'Warehouse')] as Object[], "Another user has updated this Warehouse while you were editing")
                    render(view: "edit", model: [warehouseInstance: warehouseInstance])
                    return
                }
            }
            warehouseInstance.properties = params
            if (!warehouseInstance.hasErrors() && warehouseInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), warehouseInstance.id])}"
                redirect(action: "show", id: warehouseInstance.id)
            }
            else {
                render(view: "edit", model: [warehouseInstance: warehouseInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def warehouseInstance = Warehouse.get(params.id)
        if (warehouseInstance) {
            try {
                warehouseInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
            redirect(action: "list")
        }
    }
}
