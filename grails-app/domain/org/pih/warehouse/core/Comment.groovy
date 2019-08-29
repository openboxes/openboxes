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

class Comment implements Serializable {

    String id
    String comment
    User sender
    User recipient
    Date dateSent
    Date dateRead
    Date dateCreated
    Date lastUpdated

    // Comments should exist on their own in case we want to tie them to other objects.
    // Shipment (and other entities) should create a join table for comments.

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        comment(nullable: false, maxSize: 255)
        sender(nullable: true)
        recipient(nullable: true)
        dateSent(nullable: true)
        dateRead(nullable: true)
    }

    String toString() { return "$comment" }
}
