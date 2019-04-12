/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.data

import grails.validation.ValidationException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.PreferenceTypeCode
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.springframework.validation.Errors

class MigrationController {

    def migrationService

    def index = { }


    def migrateProductSuppliers = { MigrationCommand command ->
        def startTime = System.currentTimeMillis()
        try {
//            def products = migrationService.getProductsForMigration()
//            if (params.preview) {
//                def message = "You will migrate ${products.size()} products"
//                render(template: "status", model: [message: message])
//                return
//            }
            def migratedList = migrationService.migrateProductSuppliersInParallel()
            render(template: "status", model: [message: "Migrated ${migratedList.size()} products in ${System.currentTimeMillis()-startTime} ms"])

        } catch (Exception e) {
            command.errors.reject("productSupplier.error.message", e.message)
            render(template: "status", model: [command: command])

        } catch (ValidationException e) {
            command.errors = e.errors
            render(template: "status", model: [command: command])
        }
    }


    def migrateOrganizations = { MigrationCommand command ->
        def startTime = System.currentTimeMillis()
        try {
//            def suppliers = migrationService.getSuppliersForMigration()
//            if (params.preview) {
//                render(template: "status", model: [message: "You will migrate ${suppliers.size()} organizations"])
//                return
//            }
//
            def migratedList = migrationService.migrationOrganizationsInParallel()
            render(template: "status", model: [message: "Migrated ${migratedList.size()} organizations in ${System.currentTimeMillis()-startTime} ms"])


        } catch (ValidationException e) {
            command.errors = e.errors
            render(template: "status", model: [command: command])
            return
        }
    }






    def deleteOrganizations = {
        def startTime = System.currentTimeMillis()
        def orgCount = migrationService.deleteOrganizations()
        render(template: "status", model: [message: "Deleted ${orgCount} organizations in ${System.currentTimeMillis()-startTime} ms"])
    }

    def deleteProductSuppliers = {
        def startTime = System.currentTimeMillis()
        def productSupplierCount = migrationService.deleteProductSuppliers()
        render(template: "status", model: [message: "Deleted ${productSupplierCount} product suppliers in ${System.currentTimeMillis()-startTime} ms"])

    }



}

class MigrationCommand {

}