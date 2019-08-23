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

class Localization implements Serializable {

    String id
    String code
    String locale
    String text


    // Transient fields
    String translation
    Map translations

    // Audit fields
    Date dateCreated
    Date lastUpdated

    static transients = ['translation', 'translations']

    static mapping = {
        id generator: 'uuid'
        cache true
    }


    static constraints = {
        code(nullable: false)
        locale(nullable: true)
        text(nullable: true)
    }


    Map toJson() {
        [
                id          : id,
                version     : version,
                code        : code,
                locale      : locale,
                text        : text,
                translation : translation,
                translations: translations,
                lastUpdated : lastUpdated?.format("dd/MMM/yyyy hh:mm a"),
                dateCreated : dateCreated?.format("dd/MMM/yyyy hh:mm a")
        ]
    }
}
