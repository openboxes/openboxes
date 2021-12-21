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

import grails.util.Holders
import util.StringUtil
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema

class Person implements Comparable, Serializable {

    @Schema(description="database identifier, may be uuid or numeric string", format="uuid", readOnly=true, required=true)
    String id

    @Schema(description="given name or forename", maxLength=255, required=true)
    String firstName

    @Schema(description="surname or family name", maxLength=255, required=true)
    String lastName

    @Schema(description="email address", format="email", maxLength=255, nullable=true)
    String email

    @Hidden
    @Schema(maxLength=255, nullable=true, pattern="^[0-9()+-]+\$")  // FIXME https://www.twilio.com/docs/glossary/what-e164
    String phoneNumber
    @Hidden
    Date dateCreated
    @Hidden
    Date lastUpdated

    static mapping = {
        tablePerHierarchy false
        table 'person'
        id generator: 'uuid'
    }

    static transients = ["name", "lastInitial"]

    static constraints = {
        firstName(blank: false, maxSize: 255)
        lastName(blank: false, maxSize: 255)
        phoneNumber(nullable: true, maxSize: 255)
        email(nullable: true, email: true, maxSize: 255)
    }

    int compareTo(Object obj) {

        def sortOrder =
                lastName <=> obj.lastName ?:
                        firstName <=> obj.firstName ?:
                                email <=> obj.email ?:
                                        id <=> obj?.id
        return sortOrder

    }

    @Hidden
    String getLastInitial() {
        lastName?.substring(0, 1)
    }

    String toString() {
        return "${name}"
    }

    @Schema(description="full personal name", readOnly=true)
    String getName() {
        Boolean anonymize = Holders.config.getProperty("openboxes.anonymize.enabled", Boolean.class, Boolean.FALSE)
        return "$firstName ${anonymize ? lastInitial : lastName}"
    }

    Map toJson() {
        Boolean anonymize = Holders.config.getProperty("openboxes.anonymize.enabled", Boolean.class, Boolean.FALSE)
        return [
                "id"       : id,
                "name"     : name,
                "firstName": firstName,
                "lastName" : (anonymize) ? lastInitial : lastName,
                "email"    : anonymize ? StringUtil.mask(email) : email,
                "username" : null
        ]
    }
}
