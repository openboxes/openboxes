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

class PersonController {
    def userService

    def scaffold = Person

    def redirect = {
        redirect(action: "show", id: params.id)
    }


    def list = {
        def personInstanceList = []
        def personInstanceTotal = 0

        params.max = Math.min(params.max ? params.int('max') : 15, 100)

        if (params.q) {
            String[] terms = ["%" + params.q + "%"]
            personInstanceList = userService.findPersons(terms, params)
            personInstanceTotal = personInstanceList.totalCount

        } else {
            personInstanceList = Person.list(params)
            personInstanceTotal = Person.count()
        }

        [personInstanceList: personInstanceList, personInstanceTotal: personInstanceTotal]
    }

    def convertPersonToUser = {
        userService.convertPersonToUser(params.id)
        redirect(controller: "user", action: "edit", id: params.id)
    }

    def convertUserToPerson = {
        userService.convertUserToPerson(params.id)
        redirect(controller: "person", action: "show", id: params.id)
    }

}
