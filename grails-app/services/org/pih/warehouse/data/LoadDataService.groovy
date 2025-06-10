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

import grails.gorm.transactions.Transactional
import grails.plugins.csv.CSVMapReader
import grails.validation.ValidationException
import org.pih.warehouse.DateUtil
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.*
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.LocationImportDataService
import org.pih.warehouse.importer.ProductCatalogItemImportDataService
import org.pih.warehouse.importer.ProductSupplierImportDataService
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryImportProductInventoryTransactionService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.requisition.ReplenishmentTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemSortByCode

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant

@Transactional
class LoadDataService {

    LocationImportDataService locationImportDataService
    ProductSupplierImportDataService productSupplierImportDataService
    ProductService productService
    InventoryService inventoryService
    OrganizationIdentifierService organizationIdentifierService
    ProductCatalogItemImportDataService productCatalogItemImportDataService
    InventoryImportProductInventoryTransactionService inventoryImportProductInventoryTransactionService
    ProductAvailabilityService productAvailabilityService
    ConfigService configService
    TransactionIdentifierService transactionIdentifierService

    def importLocations(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());

        ImportDataCommand command = new ImportDataCommand();
        command.setData(csvReader.readAll());

        locationImportDataService.validateData(command);
        locationImportDataService.importData(command);

        csvReader.close();
    }


    def importLocationGroups(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys();

        csvReader.eachLine { Map attr ->
            new LocationGroup(name: attr.get("Name")).save(failOnError: true)
        }

        csvReader.close();
    }

    def importOrganizations(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys();

        csvReader.eachLine({ Map attr ->
            String organizationName = attr.get("Organization")
            String partyRole = attr.get("Party Role")

            Organization organization = new Organization(
                    name: organizationName,
                    code: organizationIdentifierService.generate(organizationName),
                    partyType: PartyType.findByCode("ORG") // FIXME: Should party type be provided?
            )

            RoleType roleType = RoleType.valueOf(partyRole)
            organization.addToRoles(new PartyRole(roleType: roleType))

            organization.save(failOnError: true)
        })

        csvReader.close();
    }

    def importCategories(URL csvURL) {
        InputStream csvStream = csvURL.newInputStream();
        String csv = new String(csvStream.getBytes())

        productService.importCategoryCsv(csv)
        csvStream.close();
    }

    def importProducts(URL csvURL) {
        InputStream csvStream = csvURL.newInputStream()
        String csv = new String(csvStream.getBytes())

        def products = productService.validateProducts(csv)
        productService.importProducts(products)
        csvStream.close()
    }

    def importProductCatalog(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys();

        csvReader.eachLine({ Map attr ->
            String id = attr.id;
            String code = attr.code;
            String name = attr.name;
            String description = attr.description;

            ProductCatalog catalog = ProductCatalog.findById(id);

            if (catalog == null) {
                catalog = ProductCatalog.findByCode(code);

                if (catalog != null) {
                    throw new IllegalArgumentException("Duplicate code: " + code);
                }

                catalog = new ProductCatalog(
                        code: code,
                        name: name,
                        description: description
                )
            } else {
                catalog.code = code;
                catalog.name = name;
                catalog.description = description;
            }

            catalog.save(failOnError: true)
        })

        csvReader.close();
    }

    def importProductCatalogItems(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys()

        csvReader.eachLine { Map attr ->
            productCatalogItemImportDataService.bindProductCatalogItem(attr).save(failOnError: true)
        }

        csvReader.close();
    }

    def importProductSuppliers(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());
        csvReader.initFieldKeys()

        List<Map<String, String>> csvItems = csvReader.readAll();
        def emptyStringAsNull = { return it == "" ? null : it }

        for (int i = 0; i < csvItems.size(); i++) {
            Map<String, String> currentItem = csvItems.get(i);
            Map<String, Object> newItem = new HashMap<String, String>();

            newItem.put("active", currentItem.get("active"))
            newItem.put("id", currentItem.get("ID"));
            newItem.put("code", currentItem.get("Product Source Code"));
            newItem.put("name", currentItem.get("Product Source Name"));
            newItem.put("productCode", currentItem.get("Product Code"));
            newItem.put("legacyProductCode", currentItem.get("Legacy Product Code"));
            newItem.put("supplierName", currentItem.get("Supplier Name"));
            newItem.put("supplierCode", currentItem.get("Supplier Item No"));
            newItem.put("manufacturerName", currentItem.get("Manufacturer Name"));
            newItem.put("manufacturerCode", currentItem.get("Manufacturer Item No"));
            newItem.put("minOrderQuantity", currentItem.get("Minimum Order Quantity"));
            newItem.put("contractPricePrice", currentItem.get("Contract Price (Each)"));
            newItem.put("contractPriceValidUntil", emptyStringAsNull(currentItem.get("Contract Price Valid Until")));
            newItem.put("ratingTypeCode", emptyStringAsNull(currentItem.get("Rating Type")));
            newItem.put("globalPreferenceTypeName", currentItem.get("Default Global Preference Type"));
            newItem.put("globalPreferenceTypeValidityStartDate", currentItem.get("Preference Type Validity Start Date"));
            newItem.put("globalPreferenceTypeValidityEndDate", currentItem.get("Preference Type Validity End Date"));
            newItem.put("globalPreferenceTypeComments", currentItem.get("Preference Type Comment"));
            newItem.put("defaultProductPackageUomCode", currentItem.get("Default Package Type"));
            newItem.put("defaultProductPackageQuantity", currentItem.get("Quantity per Package").toInteger());
            newItem.put("defaultProductPackagePrice", currentItem.get("Package Price"));

            csvItems.set(i, newItem);
        }

        ImportDataCommand command = new ImportDataCommand();

        command.setData(csvItems);

        productSupplierImportDataService.validateData(command)
        productSupplierImportDataService.importData(command)

        csvReader.close();
    }

    private Transaction createAdjustmentTransaction(Date transactionDate, Inventory inventory) {
        Transaction transaction =  new Transaction(
                transactionDate: transactionDate,
                transactionType: TransactionType.get(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID),
                inventory: inventory
        )
        transaction.transactionNumber = transactionIdentifierService.generate(transaction)
        return transaction
    }

    private TransactionEntry createAdjustmentTransactionEntry(
            Map<String, String> csvReaderRow,
            Product product,
            Location binLocation,
            Map<String, AvailableItem> availableItems
    ) {
        InventoryItem inventoryItem = inventoryService.findAndUpdateOrCreateInventoryItem(
                product,
                csvReaderRow["Lot number"],
                DateUtil.asDate(csvReaderRow["Expiration date"], Constants.EXPIRATION_DATE_FORMATTER)
        )
        String key = ProductAvailabilityService.constructAvailableItemKey(binLocation, inventoryItem)
        int newQuantity = csvReaderRow["Physical QOH"] as int
        int quantityOnHand = availableItems.get(key)?.quantityOnHand ?: 0
        int adjustmentQuantity = newQuantity - quantityOnHand

        if (adjustmentQuantity == 0) {
            return null
        }

        return new TransactionEntry(
                quantity: adjustmentQuantity,
                comments: csvReaderRow["Comment"],
                inventoryItem: inventoryItem,
                binLocation: binLocation
        )
    }

    def importInventory(URL csvURL, Location targetWarehouse) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader())
        // Storing stream data as list to avoid closing stream after first reading
        List<Map<String, String>> rows = csvReader.toList()

        List<String> productCodes = rows.collect { it["Product code"] }.unique() as List<String>
        List<String> binLocationNames = rows.collect { it["Bin location"] }.unique() as List<String>

        // Get all locations and products at one DB call
        Map<String, Location> locationMap = Location.findAllByParentLocationAndNameInList(targetWarehouse, binLocationNames).collectEntries {
            [it.name, it]
        }
        Map<String, Product> productMap = Product.findAllByProductCodeInList(productCodes).collectEntries {
            [it.productCode, it]
        }

        // Calculate dates for inventory baseline and adjustment transactions
        Date adjustmentTransactionDate = DateUtil.asDate(Instant.now())
        Date inventoryBaselineTransactionDate = DateUtil.asDate(DateUtil.asInstant(adjustmentTransactionDate).minusSeconds(1))

        // 1. Calculate the current available items from product availability - one snapshot transaction for all products
        Map<String, AvailableItem> availableItems = productAvailabilityService.getAvailableItemsAtDateAsMap(
                targetWarehouse,
                productMap.values().toList(),
                inventoryBaselineTransactionDate
        )

        Boolean isInventoryBaselineEnabled = configService.getProperty(
                "openboxes.transactions.inventoryBaseline.loadDemoData.enabled",
                Boolean
        )

        // 2a. If there are available items:
        //   - If inventory snapshot is turned on for load demo data:
        //       - then create an inventory snapshot transaction with entries made from the current stock calculated in 1st point
        // 2b. If there are no available items:
        //   - then no snapshot is being saved
        if (availableItems.size() && isInventoryBaselineEnabled) {
            inventoryImportProductInventoryTransactionService.createInventoryBaselineTransactionForGivenStock(
                    targetWarehouse,
                    null,
                    availableItems.values(),
                    inventoryBaselineTransactionDate
            )
        }

        // 3. Create an adjustment (credit) transaction and take the data imported
        Transaction adjustmentTransaction = createAdjustmentTransaction(adjustmentTransactionDate, targetWarehouse.inventory)

        rows.forEach { Map<String, String> attr ->
            Product product = productMap[attr["Product code"]]
            Location binLocation = locationMap[attr["Bin location"]]

            if (!product) {
                throw new IllegalArgumentException("Product not found: " + attr["Product code"])
            }

            if (!binLocation) {
                throw new IllegalArgumentException("Location not found: " + attr["Bin location"])
            }

            TransactionEntry transactionEntry = createAdjustmentTransactionEntry(
                    attr,
                    product,
                    binLocation,
                    availableItems
            )

            if (transactionEntry) {
                adjustmentTransaction.addToTransactionEntries(transactionEntry)
            }
        }

        if (adjustmentTransaction.transactionEntries && !adjustmentTransaction.save()) {
            throw new ValidationException("Invalid transaction", adjustmentTransaction.errors)
        }

        csvReader.close()
    }

    def importInventoryLevels(URL csvURL, Location targetWarehouse) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());

        csvReader.eachLine { Map<String, String> attr ->
            Product product = Product.findByProductCode(attr["Product code"]);

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

        csvReader.close();
    }

    def importUsers(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());

        csvReader.eachLine { Map<String, String> attr ->
            StringTokenizer tokenizer = new StringTokenizer(attr.roleType, ",");
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
                    username: attr.username,
                    password: attr.username, // FIXME: What is default password?
                    passwordConfirm: attr.username,
                    firstName: attr.firstName,
                    lastName: attr.lastName,
                    email: attr.email,
                    active: false,
                    roles: userRoles,
            )

            user.save(failOnError: true)
        }

        csvReader.close();
    }

    def importPersons(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());

        csvReader.eachLine { Map<String, String> attr ->
            new Person(
                    firstName: attr.firstName,
                    lastName: attr.lastName,
                    email: attr.email,
                    phoneNumber: attr.phoneNumber,
            ).save(failOnError: true)
        }

        csvReader.close();
    }

    Requisition importStocklistTemplate(URL csvURL) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());

        List templates = csvReader.readAll();

        if(templates.size() != 1) {
            throw new IllegalArgumentException("Invalid number of templates != 1");
        }

        def attr = templates.get(0);

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
                replenishmentPeriod: Integer.parseInt(attr['Replenishment Period']),
                replenishmentTypeCode: ReplenishmentTypeCode.valueOf(attr['Replenishment Type']),
                requestedBy: requestedBy,
                isTemplate: true,
                isPublished: true
        )

        requisition.save(failOnError: true);

        csvReader.close();

        return requisition;
    }

    def importStocklistItems(URL csvURL, Requisition requisition) {
        CSVMapReader csvReader = new CSVMapReader(csvURL.newInputStream().newReader());

        csvReader.eachLine { Map<String, String> attr ->
            Product product = Product.findByProductCode(attr["product_code"])

            RequisitionItem requisitionItem = new RequisitionItem(
                    requisition: requisition,
                    product: product,
                    substitutable: false,
                    quantity: Integer.parseInt(attr["quantity"])
            );

            requisitionItem.save(failOnError: true);
        }

        csvReader.close();
    }
}
