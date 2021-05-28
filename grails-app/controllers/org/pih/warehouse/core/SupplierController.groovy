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

class SupplierController {

    def locationService
    def documentService

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.offset = params.offset ? params.int("offset") : 0

        def suppliers = locationService.getSuppliers(params.q, params.max, params.offset as int)

        [suppliers: suppliers, suppliersTotal: suppliers.totalCount]
    }

    def show = {
        Organization supplier = Organization.get(params.id)
        List<Document> documents = documentService.getAllDocumentsBySupplierOrganization(supplier)

        [supplier: supplier, documents: documents]
    }

}
