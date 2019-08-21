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

/**
 * TODO Use enum 
 */
class DocumentType implements Serializable {

    String id
    String name
    String description
    Integer sortOrder = 0
    Date dateCreated
    Date lastUpdated

    DocumentCode documentCode

    static constraints = {
        name(nullable: false, maxSize: 255)
        description(nullable: true, maxSize: 255)
        sortOrder(nullable: true)
        documentCode(nullable: true)
    }

    static mapping = {
        id generator: 'uuid'
        sort "sortOrder"
    }

    String toString() {
        return name
    }


}
