/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.admin

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

class MigrationController {

    def index = { }

    def migrateLocalLocations = {
        def locations = Location.createCriteria().list {
            isNull("inventory")
        }

        if (params.preview) {
            render(template: "status", model: [message: "Migrating ${locations.size()} locations"])
            return

        }

        render(template: "status", model: [message: "Migrated ${locations.size()} locations"])
    }


    def migrateProductSuppliers = {

        def now = new Date()

        def products = Product.createCriteria().list {
            or {
                isNotNull("manufacturer")
                isNotNull("manufacturerName")
                isNotNull("manufacturerCode")
                isNotNull("vendor")
                isNotNull("vendorName")
                isNotNull("vendorCode")
                isNotNull("brandName")
                isNotNull("modelNumber")
                isNotNull("upc")
                isNotNull("ndc")
            }
        }


        if (params.preview) {
            def message = "You will migrate ${products.size()} products"
            render(template: "status", model: [message: message])
            return
        }

        def migratedList = []
        Product target
        PartyType orgType = PartyType.findByCode("ORG")

        try {
            products.each { product ->

                target = product

                def productSupplier = new ProductSupplier()
                productSupplier.productCode = product.productCode
                productSupplier.name = product.name
                productSupplier.product = product
                productSupplier.description = product.description

                if (product.manufacturer) {
                    def manufacturer = findOrCreateOrganization(product.manufacturer, null, orgType, [RoleType.ROLE_MANUFACTURER])
                    productSupplier.manufacturer = manufacturer
                    if (!productSupplier.manufacturer) {
                        productSupplier.errors.rejectValue("manufacturer", "productSupplier.invalid.manufacturer","Manufacturer ${product?.manufacturer} does not exist.")
                    }
                }

                if (product.vendor) {
                    def supplier = findOrCreateOrganization(product.vendor, null, orgType, [RoleType.ROLE_SUPPLIER])
                    productSupplier.supplier = supplier
                    if (!productSupplier.supplier) {
                        productSupplier.errors.rejectValue("supplier", "productSupplier.invalid.supplier", "Supplier ${product?.vendor} does not exist.")
                    }
                }

                productSupplier.manufacturerCode = product.manufacturerCode
                productSupplier.manufacturerName = product.manufacturerName
                productSupplier.supplierCode = product.vendorCode
                productSupplier.supplierName = product.vendorName
                productSupplier.upc = product.upc
                productSupplier.ndc = product.ndc
                productSupplier.brandName = product.brandName
                productSupplier.modelNumber = product.modelNumber
                productSupplier.unitPrice = product.pricePerUnit
                productSupplier.ratingTypeCode = RatingTypeCode.NOT_RATED
                productSupplier.preferenceTypeCode = PreferenceTypeCode.NOT_PREFERRED

                if (!ProductSupplier.find(productSupplier)) {

                    // To ensure that we can find the product supplier using the finder above we needed to
                    // postponse setting of these two fields
                    def index = (product.productSuppliers?.size() ?: 0) + 1
                    productSupplier.code = product.productCode + "-${index}"
                    productSupplier.comments = "Migrated on ${now}"

                    if (productSupplier.hasErrors() || !productSupplier.save(flush: true)) {
                        log.info("Product supplier " + productSupplier.errors)
                        throw new ValidationException("Cannot migrate supplier ${productSupplier?.name}: ", productSupplier?.errors)
                    }
                    migratedList.add(productSupplier)
                }
            }

        } catch (Exception e) {
            target.errors.reject("productSupplier.error.message", e.message)
            render(template: "status", model: [target: target])
            return

        } catch (ValidationException e) {
            target.errors = e.errors
            render(template: "status", model: [target: target])
            return

        }
        render(template: "status", model: [message: "Migrated ${migratedList.size()} products"])
    }


    def migrateOrganizations = {
        LocationType supplierType = LocationType.findByLocationTypeCode(LocationTypeCode.SUPPLIER)
        def suppliers = Location.createCriteria().list {
            eq("active", true)
            eq("locationType", supplierType)
            isNull("organization")
        }


        if (params.preview) {
            render(template: "status", model: [message: "You will migrate ${suppliers.size()} organizations"])
            return

        }

        def target
        try {

            PartyType partyType = PartyType.findByCode("ORG")
            suppliers.each { supplier ->
                target = supplier
                if (!supplier.organization) {
                    def organization = findOrCreateOrganization(supplier.name, supplier.description, partyType, [RoleType.ROLE_SUPPLIER])

                    if (!organization.save(flush:true)) {
                        log.info("errors: " + organization.errors)
                        throw new ValidationException("Cannot create organization ${organization?.name}: ", organization.errors)
                    }

                    supplier.organization = organization
                    if (supplier.hasErrors() || !supplier.save(flush: true)) {
                        log.info("errors: " + supplier.errors)
                        throw new ValidationException("Cannot migrate supplier ${supplier?.name}: ", supplier.errors)
                    }
                }
            }
        } catch (ValidationException e) {
            target.errors = e.errors
            render(template: "status", model: [target: target])
            return
        }
        //render ([suppliers:suppliers] as JSON)
        render(template: "status", model: [message: "Migrated ${suppliers.size()} organizations"])
    }


    def findOrCreateOrganization(String name, String description, PartyType partyType, List roleTypes) {

        Organization organization = Organization.findByName(name)
        if (!organization) {
            organization = new Organization()
            organization.name = name
            organization.description = description
            organization.partyType = partyType
        }

        if (roleTypes) {
            roleTypes.each { roleType ->
                if (!organization.roles.find { it.roleType == roleType }) {
                    organization.addToRoles(new PartyRole(roleType: roleType))
                }
            }
        }

        if (organization.hasErrors() || !organization.save(flush:true)) {
            throw new ValidationException("Validation error", organization.errors)
        }


        return organization
    }



    def deleteOrganizations = {

        LocationType supplierType = LocationType.findByLocationTypeCode(LocationTypeCode.SUPPLIER)
        def suppliers = Location.createCriteria().list {
            eq("active", true)
            eq("locationType", supplierType)
            isNotNull("organization")
            ilike("comments", "Migrated%s")
            //maxResults(10)
        }

        suppliers.each { supplier ->
            supplier.organization.delete();
            supplier.organization = null
        }

        render(template: "status", model: [message: "Deleted ${suppliers.size()} organizations"])
    }

    def deleteProductSuppliers = {

        def productSuppliers = ProductSupplier.createCriteria().list {
            ilike("comments", "Migrated%")
        }
        productSuppliers.each { productSupplier ->
            productSupplier.product?.removeFromProductSuppliers(productSupplier)
            productSupplier.delete();
        }
        render(template: "status", model: [message: "Deleted ${productSuppliers?.size()} product suppliers"])

    }



}
