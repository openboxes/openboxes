/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import org.pih.warehouse.core.Document

class ShipmentWorkflow implements Serializable {

    String id
    String name                    // user-defined name of the workflow
    ShipmentType shipmentType    // the shipment type this workflow is associated with
    String excludedFields
    // comma-delimited (with no spaces) list of Shipment fields to exclude in this workflow
    String documentTemplate
    // the template to use when auto-generating documents for this workflow

    // Audit fields
    Date dateCreated
    Date lastUpdated

    // one-to-many associations
    List referenceNumberTypes
    List containerTypes

    // Core association mappings
    static hasMany = [referenceNumberTypes: ReferenceNumberType,
                      containerTypes      : ContainerType,
                      documentTemplates   : Document]

    static mapping = {
        id generator: 'uuid'
        documentTemplates joinTable: [name: 'shipment_workflow_document', column: 'document_id', key: 'shipment_workflow_id']
    }

    static constraints = {
        name(nullable: false, blank: false, maxSize: 255)
        shipmentType(nullable: false, unique: true)
        // for now, we are only allowing one workflow per shipment type, though we may want to change this
        excludedFields(nullable: true, maxSize: 255)
        documentTemplate(nullable: true, maxSize: 255)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)

        // a shipment workflow can't have two identical reference number types
        referenceNumberTypes(validator: { referenceNumberTypes ->
            referenceNumberTypes?.unique({ a, b -> a.id <=> b.id })?.size() == referenceNumberTypes?.size()
        })

        // a shipment workflow can't have two identical container types
        containerTypes(validator: { containerTypes ->
            containerTypes?.unique({ a, b -> a.id <=> b.id })?.size() == containerTypes?.size()
        })

    }

    String toString() { name }

    Boolean isExcluded(String field) {
        // ?i: -> sets case insensitive
        // (^|,) -> matches start-of-line or comma
        // (,|$) -> matches comma or end-of-line
        return excludedFields =~ (/(?i:(^|,)$field(,|$))/)
    }
}
