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

enum UpsertStatus {
    OK, ERROR
}

enum UpsertAction {
    CREATED, UPDATED
}

class UpsertResult {

    Integer index
    UpsertStatus status
    UpsertAction action
    String entityId
    String productId
    String errorMessage
    List<Map> errors

    UpsertResult withIndex(int index) {
        this.index = index
        return this
    }

    static UpsertResult ok(boolean isNew, String entityId, String productId) {
        return new UpsertResult(
                status: UpsertStatus.OK,
                action: isNew ? UpsertAction.CREATED : UpsertAction.UPDATED,
                entityId: entityId,
                productId: productId)
    }

    static UpsertResult error(String errorMessage, List<Map> errors = null) {
        return new UpsertResult(status: UpsertStatus.ERROR, errorMessage: errorMessage, errors: errors)
    }

    Map toJson() {
        return [
                index       : index,
                status      : status?.name(),
                action      : action?.name(),
                entityId    : entityId,
                errorMessage: errorMessage,
                errors      : errors,
        ].findAll { k, v -> v != null }
    }
}
