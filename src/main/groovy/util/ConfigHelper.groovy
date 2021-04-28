/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package util
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import grails.util.Holders

// See http://jira.codehaus.org/browse/GRAILS-6515
class ConfigHelper {

    static getContextPath() {
        String contextPath = Holders.grailsApplication.config.server.contextPath
        return (contextPath != '/') ? contextPath : ''
    }


    static booleanValue(def value) {
        if (value.class == java.lang.Boolean) {
            // because 'true.toBoolean() == false' !!!
            return value
        } else {
            return value.toBoolean()
        }
    }

    static listValue(def value) {
        if (value instanceof java.lang.String) {
            return value?.split(",")
        } else {
            return value
        }

    }

    static findAccessRule(String controllerName, String actionName) {
        def rules = ConfigurationHolder.config.openboxes.security.rbac.rules

        ArrayList<Closure> filters = [
            { it.controller == controllerName && it.actions.contains(actionName) },
            { it.controller == controllerName && it.actions.contains("*") },
            { it.controller == "*" && it.actions.contains("*") }
        ]
        // Find the rules for each filter and find the first non empty from the top (filters on the top are higher priority)
        def rule = filters
                    .collect { rules.findAll(it) }
                    .find { it.size() != 0 }

        // If there is more than one rule specified for the same controller and action, determine which Role is "higher" by sortOrder of RoleType enum, and return the "higher" one
        return rule?.sort { it?.accessRules?.minimumRequiredRole?.sortOrder }?.first()
    }

}
