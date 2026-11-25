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

// Thrown when the staging location given for a drop is not a location of the facility (by id or
// location number). PickTaskApiController.drop answers it with 404 and this message, like a missing
// outbound container; other callers see the IllegalArgumentException they saw before.
class StagingLocationNotFoundException extends IllegalArgumentException {

    StagingLocationNotFoundException(String stagingLocationId) {
        super("Staging location ${stagingLocationId} not found".toString())
    }
}
