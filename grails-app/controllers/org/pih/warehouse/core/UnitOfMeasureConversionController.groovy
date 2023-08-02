/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

class UnitOfMeasureConversionController {

    UnitOfMeasureConversionDataService unitOfMeasureConversionDataService

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [unitOfMeasureConversions: UnitOfMeasureConversion.list(params), unitOfMeasureConversionsTotal: UnitOfMeasureConversion.count()]
    }

    def create() {
        UnitOfMeasureConversion unitOfMeasureConversion = new UnitOfMeasureConversion()
        unitOfMeasureConversion.properties = params
        return [unitOfMeasureConversion: unitOfMeasureConversion]
    }

    def edit() {
        UnitOfMeasureConversion unitOfMeasureConversion = unitOfMeasureConversionDataService.get(params.id)
        return [unitOfMeasureConversion: unitOfMeasureConversion]
    }

    def save() {
        UnitOfMeasureConversion unitOfMeasureConversion = new UnitOfMeasureConversion(params)
        if (!unitOfMeasureConversion.hasErrors() && unitOfMeasureConversionDataService.save(unitOfMeasureConversion)) {
            def messageArgs = [warehouse.message(code: 'unitOfMeasureConversion.label', default: 'Unit of Measure conversion'), unitOfMeasureConversion.id]
            flash.message = "${warehouse.message(code: 'default.created.message', args: messageArgs)}"
            redirect(controller: "unitOfMeasureConversion", action: "edit", id: unitOfMeasureConversion?.id)
        } else {
            render(view: "create", model: [unitOfMeasureConversion: unitOfMeasureConversion])
        }
    }

    def update() {
        UnitOfMeasureConversion unitOfMeasureConversion = unitOfMeasureConversionDataService.get(params.id)
        def messageArgs = [warehouse.message(code: 'unitOfMeasureConversion.label', default: 'Unit of Measure conversion'), params.id]
        if (unitOfMeasureConversion) {
            unitOfMeasureConversion.properties = params
            if (!unitOfMeasureConversion.hasErrors() && unitOfMeasureConversionDataService.save(unitOfMeasureConversion)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: messageArgs)}"
                redirect(action: "list")
            } else {
                render(view: "edit", id: unitOfMeasureConversion.id, model: [unitOfMeasureConversion: unitOfMeasureConversion])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: messageArgs)}"
            redirect(action: "list")
        }
    }

    def delete() {
        UnitOfMeasureConversion unitOfMeasureConversion = unitOfMeasureConversionDataService.get(params.id)
        def messageArgs = [warehouse.message(code: 'unitOfMeasureConversion.label', default: 'Unit of Measure conversion'), params.id]
        if (unitOfMeasureConversion) {
            try {
                unitOfMeasureConversionDataService.delete(unitOfMeasureConversion.id)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: messageArgs)}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: messageArgs)}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: messageArgs)}"
            redirect(action: "list")
        }
    }

}
