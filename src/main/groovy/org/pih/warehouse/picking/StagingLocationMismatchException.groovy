/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.picking

import grails.validation.ValidationException
import org.springframework.validation.BeanPropertyBindingResult

// Thrown when the scanned/selected staging location's zone doesn't match the zone expected
// for the requisition's delivery type (see PickTaskService.validateStagingLocationZone).
// Handled globally by ErrorsController.handleStagingLocationMismatch (see UrlMappings, where
// this must be registered *after* the plain ValidationException mapping to take precedence),
// which marks the response overridable so the mobile app can offer to stage there anyway.
//
// getMessage() is overridden because grails.validation.ValidationException.getMessage() returns
// a verbose, Spring-formatted dump of the Errors object rather than the raw message passed in.
class StagingLocationMismatchException extends ValidationException {

    private final String simpleMessage

    StagingLocationMismatchException(String message) {
        super(message, new BeanPropertyBindingResult(message, "stagingLocation"))
        this.simpleMessage = message
    }

    @Override
    String getMessage() {
        return simpleMessage
    }
}
