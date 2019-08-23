/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse.picklist

class PicklistItemController {

    def scaffold = PicklistItem

    def picklistService

    def delete = {
        def picklistItem = PicklistItem.get(params.id)
        if (picklistItem) {

            try {
                picklistItem.delete()
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'picklistItem.label', default: 'Picklist item'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'picklistItem.label', default: 'Picklist item'), params.id])}"
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'picklistItem.label', default: 'Picklist item'), params.id])}"
        }
        redirect(controller: "requisition", action: "pick", id: picklistItem?.picklist?.requisition?.id, params: ['requisitionItem.id': picklistItem?.requisitionItem?.id])
    }
}
