/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

/**
 * Simple implementation of entity-attribute-value model that allows for 
 * a Product to be extended to contain custom attribute values
 * TODO: This should really be named ProductAttribute
 */
class Attribute {

    String id
    String code            // Unique code to identify the
    String name            // The name of the attribute (e.g. 'vitality')
    String description
    Boolean active = Boolean.TRUE
    Boolean exportable = Boolean.TRUE

    List options            // Valid coded option values for this attribute

    String defaultValue
    Boolean required = Boolean.FALSE
    Boolean allowOther        // If true, supports a free-text entry for value
    Boolean allowMultiple = Boolean.FALSE

    Date dateCreated
    Date lastUpdated

    static hasMany = [options: String]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        code(nullable: false, unique: true)
        name(nullable: false, maxSize: 255)
        description(nullable: true)
        defaultValue(nullable: true)

        dateCreated(display: false)
        lastUpdated(display: false)
    }

    String toString() { return "$name" }
}
