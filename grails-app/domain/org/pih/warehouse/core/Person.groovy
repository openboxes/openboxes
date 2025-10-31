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


class Person implements Comparable, Serializable {

    String id
    String firstName
    String lastName
    String email
    String phoneNumber
    Date dateCreated
    Date lastUpdated
    Boolean active = true

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
        active(nullable: true)
    }

    int compareTo(Object obj) {

        def sortOrder =
                lastName <=> obj.lastName ?:
                        firstName <=> obj.firstName ?:
                                email <=> obj.email ?:
                                        id <=> obj?.id
        return sortOrder

    }

    String getLastInitial() {
        lastName?.substring(0, 1)
    }

    String toString() {
        return "${name}"
    }

    String getName() {
        Boolean anonymize = Holders.config.getProperty("openboxes.anonymize.enabled", Boolean.class, Boolean.FALSE)
        return "$firstName ${anonymize ? lastInitial : lastName}"
    }

    static Person findByNameOrEmail(String searchTerm) {
        String[] searchTerms = searchTerm?.split(Constants.SPACE_SEPARATOR)
        // If search term contains two words, try to search by first name or last name first
        if (searchTerms?.length == 2) {
            return findByFirstNameAndLastName(searchTerms[0], searchTerms[1])
        }
        if (searchTerms?.length == 1) {
            return findByEmail(searchTerms[0])
        }
        return null
    }

    static List<Person> findAllByNameOrEmail(List<String> searchTerms) {
        return searchTerms.collect { findByNameOrEmail(it) }.findAll { it }
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
