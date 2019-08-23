/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

class BaseDomainApiController {

    def list = {
        log.debug "list: " + params
        forward(controller: "genericApi", action: "list")
    }

    def read = {
        log.debug "read: " + params
        forward(controller: "genericApi", action: "read")
    }

    def create = {
        log.debug "create: " + params
        forward(controller: "genericApi", action: "create")
    }

    def update = {
        log.debug "update: " + params
        forward(controller: "genericApi", action: "update")
    }

    def delete = {
        log.debug "delete: " + params
        forward(controller: "genericApi", action: "delete")
    }

}
