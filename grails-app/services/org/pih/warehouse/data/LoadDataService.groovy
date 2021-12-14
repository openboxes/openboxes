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

import org.apache.commons.csv.CSVRecord
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.requisition.ReplenishmentTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemSortByCode

import java.text.DateFormat
import java.text.SimpleDateFormat

class LoadDataService {

    def locationService
    def productService
    def productSupplierDataService
    def productCatalogService
    def inventoryService

    def importLocations(URL csvURL) {
        locationService.importLocationCsv(new ImportDataCommand(), csvURL)
    }

    def importLocationGroups(URL csvURL) {
        final records = CSVUtils.parseRecords(csvURL, ['Name'])
        records.each {
            new LocationGroup(name: it.get('Name')).save(failOnError: true)
        }
    }

    def importOrganizations(URL csvURL) {
        final records = CSVUtils.parseRecords(csvURL, ['Organization', 'Party Role'])
        records.each {
            String organizationName = it.get('Organization')
            String partyRole = it.get('Party Role')

            Organization organization = new Organization(
                name: organizationName,
                code: partyRole.substring(0, 3), // FIXME: Should code be provided or generated?
                partyType: PartyType.findByCode("ORG") // FIXME: Should party type be provided?
            )

            RoleType roleType = RoleType.valueOf(partyRole)
            organization.addToRoles(new PartyRole(roleType: roleType))

            organization.save(failOnError: true)
        }
    }

    def importCategories(URL csvURL) {
        productService.importCategoryCsv(csvURL)
    }

    def importProducts(URL csvURL) {
        def products = productService.validateProducts(csvURL)
        productService.importProducts(products)
    }

    def importProductCatalog(URL csvURL) {
        final records = CSVUtils.parseRecords(csvURL, ['code', 'description', 'id', 'name'])

        records.each {
            ProductCatalog catalog = ProductCatalog.findById(it['id'])
            if (catalog == null) {
                if (ProductCatalog.findByCode(it['code']) != null) {
                    throw new IllegalArgumentException("Code ${it['code']} already exists under a different ID")
                }
                catalog = new ProductCatalog()
            }

            catalog.code = it['code']
            catalog.description = it['description']
            catalog.name = it['name']
            catalog.save(failOnError: true)
        }
    }

    def importProductCatalogItems(URL csvURL) {
        final records = CSVUtils.parseRecords(csvURL, ['productCatalogCode', 'productCode'])
        records.each {
            productCatalogService.createOrUpdateProductCatalogItem(it.toMap()).save(failOnError: true)
        }
    }

    def importProductSuppliers(URL csvURL) {
        final fieldMappings = [
            code: 'Product Source Code',
            contractPricePrice: 'Contract Price (Each)',
            contractPriceValidUntil: 'Contract Price Valid Until',
            defaultProductPackagePrice: 'Package Price',
            defaultProductPackageQuantity: 'Quantity per Package',
            defaultProductPackageUomCode: 'Default Package Type',
            globalPreferenceTypeComments: 'Preference Type Comment',
            globalPreferenceTypeName: 'Default Global Preference Type',
            globalPreferenceTypeValidityEndDate: 'Preference Type Validity End Date',
            globalPreferenceTypeValidityStartDate: 'Preference Type Validity Start Date',
            id: 'ID',
            legacyProductCode: 'Legacy Product Code',
            manufacturerCode: 'Manufacturer Item No',
            manufacturerName: 'Manufacturer Name',
            minOrderQuantity: 'Minimum Order Quantity',
            name: 'Product Source Name',
            productCode: 'Product Code',
            ratingTypeCode: 'Rating Type',
            supplierCode: 'Supplier Item No',
            supplierName: 'Supplier Name',
        ]

        final records = CSVUtils.parseRecords(csvURL, fieldMappings.values())
        ImportDataCommand command = new ImportDataCommand()
        command.data = records.collect {
            Map<String, String> newItem = [:]
            fieldMappings.each { k, v ->
                newItem[k] = it.get(v)
            }
            return newItem
        }

        productSupplierDataService.validate(command)
        productSupplierDataService.process(command)
    }


    def importInventory(URL csvURL, Location targetWarehouse) {
        final records = CSVUtils.parseRecords(csvURL,
            [
                'Bin location',
                'Comment',
                'Expiration date',
                'Lot number',
                'Physical QOH',
                'Product code',
            ]
        )

        Transaction transaction = new Transaction();
        transaction.transactionDate = new Date();
        transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID);
        transaction.inventory = targetWarehouse.inventory
        DateFormat dateFormat = new SimpleDateFormat('DD/mm/yyyy');

        records.each { CSVRecord it ->
            Map attr = it.toMap()
            Product product = Product.findByProductCode(attr["Product code"]);
            if (!product) {
                throw new IllegalArgumentException("Product not found: ${attr['Product code']}")
            }

            Location binLocation = Location.findByName(attr["Bin location"])
            if (!binLocation) {
                throw new IllegalArgumentException("Location not found: ${attr['Bin location']}")
            }

            TransactionEntry transactionEntry = new TransactionEntry();
            transactionEntry.quantity = attr["Physical QOH"] as Integer
            transactionEntry.comments = attr["Comment"]

            def expirationDate = attr["Expiration date"]

            InventoryItem inventoryItem = inventoryService.findAndUpdateOrCreateInventoryItem(
                    product,
                    attr["Lot number"],
                    expirationDate == ""
                            ? null
                            : dateFormat.parse(expirationDate)
            )

            transactionEntry.inventoryItem = inventoryItem
            transactionEntry.binLocation = binLocation

            transaction.addToTransactionEntries(transactionEntry)
        }

        transaction.forceRefresh = Boolean.TRUE
        transaction.save(flush: true, failOnError: true)
    }

    def importInventoryLevels(URL csvURL, Location targetWarehouse) {
        final records = CSVUtils.parseRecords(csvURL,
            [
                'Expected Lead Time Days',
                'Min quantity',
                'Max quantity',
                'Preferred bin location',
                'Product code',
                'Reorder quantity',
                'Replenishment Period Days',
            ]
        )

        records.each { CSVRecord it ->
            attr = it.toMap()
            Product product = Product.findByProductCode(attr["Product code"])
            Location preferredBinLocation = Location.findByName(attr["Preferred bin location"])

            InventoryLevel inventoryLevel = new InventoryLevel(
                inventory: targetWarehouse.inventory,
                product: product,
                status: attr["status"],
                preferredBinLocation: preferredBinLocation,
                minQuantity: attr["Min quantity"],
                maxQuantity: attr["Max quantity"],
                reorderQuantity: attr["Reorder quantity"],
                expectedLeadTimeDays: attr["Expected Lead Time Days"],
                replenishmentPeriodDays: attr["Replenishment Period Days"]
            )

            inventoryLevel.save(failOnError: true);
        }
    }

    def importUsers(URL csvURL) {
        final records = CSVUtils.parseRecords(csvURL,
            [
                'email',
                'firstName',
                'lastName',
                'roleType',
                'username',
            ]
        )

        records.each {
            StringTokenizer tokenizer = new StringTokenizer(it.get('roleType'), ',')
            Set<RoleType> roleTypes = new HashSet<RoleType>();

            while (tokenizer.hasMoreElements()) {
                roleTypes.add(RoleType.valueOf(tokenizer.nextElement() as String));
            }

            Set<Role> userRoles = new HashSet<Role>();

            for (RoleType roleType in roleTypes) {
                Role role = Role.findByRoleType(roleType)

                if (!role) {
                    throw new IllegalArgumentException("Role for role type ${roleType.name()} does not exist")
                }

                userRoles.add(role)
            }

            User user = new User(
                username: it.get('username'),
                password: it.get('username'), // FIXME: What is default password?
                passwordConfirm: it.get('username'),
                firstName: it.get('firstName'),
                lastName: it.get('lastName'),
                email: it.get('email'),
                active: false,
                roles: userRoles,
            )

            user.save(failOnError: true)
        }
    }

    def importPersons(URL csvURL) {
        final records = CSVUtils.parseRecords(csvURL, ['firstName', 'lastName', 'email', 'phoneNumber'])
        records.each {
            new Person(
                email: it.get('email'),
                firstName: it.get('firstName'),
                lastName: it.get('lastName'),
                phoneNumber: it.get('phoneNumber'),
            ).save(failOnError: true)
        }
    }

    Requisition importStocklistTemplate(URL csvURL) {
        final templates = CSVUtils.parseRecords(csvURL,
            [
                'Destination Code',
                'Name',
                'Origin Code',
                'Replenishment Period',
                'Replenishment Type',
                'Sort By'
            ]
        )

        if (templates.size() != 1) {
            throw new IllegalArgumentException("Invalid number of templates != 1");
        }

        final attr = templates.get(0).toMap()

        String originCode = attr['Origin Code'];
        Location origin = Location.findByLocationNumber(originCode)

        if(!origin) {
            throw new IllegalArgumentException("Origin not found: " + originCode);
        }

        String destinationCode = attr["Destination Code"]
        Location destination = Location.findByLocationNumber(destinationCode)

        if(!destination) {
            throw new IllegalArgumentException("Destination not found: " + destinationCode);
        }

        Person requestedBy = Person.createCriteria().list {
            maxResults(1)
            order("id", "asc")
        }[0] // FIXME: Who is default person?

        Requisition requisition = new Requisition(
            name: attr["Name"],
            origin: origin,
            destination: destination,
            dateRequested: new Date(),
            requestedDeliveryDate: new Date(),
            sortByCode: RequisitionItemSortByCode.valueOf(attr['Sort by']),
            replenishmentPeriod: CSVUtils.parseInteger(attr['Replenishment Period'], 'Replenishment Period'),
            replenishmentTypeCode: ReplenishmentTypeCode.valueOf(attr['Replenishment Type']),
            requestedBy: requestedBy,
            isTemplate: true,
            isPublished: true
        )

        requisition.save(failOnError: true);
        return requisition;
    }

    def importStocklistItems(URL csvURL, Requisition requisition) {
        final records = CSVUtils.parseRecords(csvURL, ['product_name', 'quantity'])
        records.each {
            RequisitionItem requisitionItem = new RequisitionItem(
                requisition: requisition,
                product: Product.findByProductCode(it.get('product_code')),
                substitutable: false,
                quantity: CSVUtils.parseInteger(it.get('quantity'), 'quantity'),
            )

            requisitionItem.save(failOnError: true)
        }
    }
}
