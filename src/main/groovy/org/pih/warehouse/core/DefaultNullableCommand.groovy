package org.pih.warehouse.core

import grails.validation.Validateable

class DefaultNullableCommand implements Validateable {

    static boolean defaultNullable() {
        return true
    }

}
