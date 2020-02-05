package org.pih.warehouse
/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/


import grails.converters.JSON
import grails.core.GrailsApplication
import grails.util.Environment
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.EditPage
import org.pih.warehouse.api.EditPageItem
import org.pih.warehouse.api.PackPage
import org.pih.warehouse.api.PackPageItem
import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.PartialReceiptContainer
import org.pih.warehouse.api.PartialReceiptItem
import org.pih.warehouse.api.PickPage
import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockAdjustment
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.api.StocklistItem
import org.pih.warehouse.api.SubstitutionItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType
import org.quartz.Scheduler
import util.LiquibaseUtil

import javax.sql.DataSource

class BootStrap {

    def identifierService
    GrailsApplication grailsApplication
    def uploadService
    DataSource dataSource

    def init = { servletContext ->

        log.info("Registering JSON marshallers ...")
        registerJsonMarshallers()

        log.info("Executing database migrations ...")
        executeDatabaseMigrations()

        log.info("Starting Quartz scheduler ...")
        Scheduler scheduler = grailsApplication.mainContext.getBean("quartzScheduler")
        scheduler.start()

        // Create uploads directory if it doesn't already exist
        log.info("Creating uploads directory ...")
        uploadService.findOrCreateUploadsDirectory()
    }


    def destroy = {

    }

    def executeDatabaseMigrations() {
        Liquibase liquibase = null
        try {

            def connection = dataSource.getConnection()
            if (connection == null) {
                throw new RuntimeException("Connection could not be created.")
            }

            def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dataSource.connection))
            database.setDefaultSchemaName(connection.catalog)
            boolean isRunningMigrations = LiquibaseUtil.isRunningMigrations()
            log.info("Liquibase running: " + isRunningMigrations)
            log.info("Setting default schema to " + connection.catalog)
            log.info("Product Version: " + database.databaseProductVersion)
            log.info("Database Version: " + database.databaseMajorVersion + "." + database.databaseMinorVersion)

            // Ensure the databasechangelog up-to-date in order to handle
            // DatabaseException: Error executing SQL SELECT * FROM obnav.DATABASECHANGELOG
            // ORDER BY DATEEXECUTED ASC, ORDEREXECUTED ASC: Unknown column 'ORDEREXECUTED' in 'order clause'
            liquibase = new Liquibase(null as DatabaseChangeLog, new ClassLoaderResourceAccessor(), database)
            liquibase.checkLiquibaseTables(false, null, new Contexts(), new LabelExpression())

            def executedChangelogVersions = LiquibaseUtil.getExecutedChangelogVersions()
            log.info ("executedChangelogVersions: " + executedChangelogVersions)

            // FIXME Not good that we'll need to update this on subsequent versions so this needs some more thought
            List previousChangelogVersions = ["0.5.x", "0.6.x", "0.7.x", "0.8.x"]

            // Check if the executed changelog versions include one of the previous versions
            // and if so, then we need to keep running the old updates to catch up to 0.9.x
            boolean hasExecutedAnyPreviousChangesets =
                    executedChangelogVersions.any { previousChangelogVersions.contains(it.version) }

            // FIXME Remove !hasExecutedAnyPreviousChangesets once this goes to production
            //If nothing has been created yet, let's create all new database objects with the install scripts
            List<ChangeSet> hasExecutedAnyChangesets = database.getRanChangeSetList()
            if (!hasExecutedAnyChangesets || !hasExecutedAnyPreviousChangesets) {
                log.info("Running install changelog ...")
                liquibase = new Liquibase("install/changelog.xml", new ClassLoaderResourceAccessor(), database)
                liquibase.update(null as Contexts, new LabelExpression());
            }

            if (hasExecutedAnyPreviousChangesets) {
                log.info("Running upgrade changelog ...")
                liquibase = new Liquibase("upgrade/changelog.xml", new ClassLoaderResourceAccessor(), database)
                liquibase.update(null as Contexts, new LabelExpression())
            }

            // And now we need to run changelogs from 0.9.x and beyond
            log.info("Running latest updates")
            liquibase = new Liquibase("changelog.groovy", new ClassLoaderResourceAccessor(), database)
            liquibase.update(null as Contexts, new LabelExpression())
        }
        finally {
            log.info("Closing database")
            liquibase?.database?.close()
        }
        log.info("Finished running liquibase changelog(s)!")
    }

    def registerJsonMarshallers() {
        // Static data
        JSON.registerObjectMarshaller(ContainerType) { ContainerType containerType ->
            [
                    id         : containerType.id,
                    name       : containerType.name,
                    description: containerType.description
            ]
        }

        JSON.registerObjectMarshaller(LocationType) { LocationType locationType ->
            [
                    id              : locationType.id,
                    name            : locationType.name,
                    description     : locationType.description,
                    locationTypeCode: locationType?.locationTypeCode?.name()
            ]
        }

        JSON.registerObjectMarshaller(ShipmentType) { ShipmentType shipmentType ->
            [
                    id         : shipmentType.id,
                    name       : shipmentType.name,
                    description: shipmentType.description
            ]
        }


        // Master data

        JSON.registerObjectMarshaller(Category) { Category category ->
            [
                    id            : category.id,
                    name          : category.name,
                    parentCategory: category?.parentCategory
            ]
        }

        JSON.registerObjectMarshaller(Container) { Container container ->
            [
                    id             : container.id,
                    name           : container.name,
                    containerNumber: container.containerNumber,
                    containerType  : container.containerType,
                    recipient      : container.recipient,
                    sortOrder      : container.sortOrder,
                    shipmentItems  : container.shipmentItems
            ]
        }

        JSON.registerObjectMarshaller(LocationGroup) { LocationGroup locationGroup ->
            [
                    id  : locationGroup.id,
                    name: locationGroup.name
            ]
        }

        JSON.registerObjectMarshaller(InventoryItem) { InventoryItem inventoryItem ->
            [
                    id            : inventoryItem.id,
                    product       : [
                            id         : inventoryItem?.product?.id,
                            name       : inventoryItem?.product?.name,
                            productCode: inventoryItem?.product?.productCode
                    ],
                    lotNumber     : inventoryItem.lotNumber,
                    expirationDate: inventoryItem.expirationDate?.format("MM/dd/yyyy")
            ]
        }

        JSON.registerObjectMarshaller(Location) { Location location ->
            [
                    id                   : location.id,
                    name                 : location.name,
                    description          : location.description,
                    locationNumber       : location.locationNumber,
                    locationGroup        : location.locationGroup,
                    parentLocation       : location.parentLocation,
                    locationType         : location.locationType,
                    sortOrder            : location.sortOrder,
                    hasBinLocationSupport: location.hasBinLocationSupport(),
                    hasPackingSupport    : location.supports(ActivityCode.PACK_SHIPMENT)
            ]
        }

        JSON.registerObjectMarshaller(Person) { Person person ->
            return person.toJson()
        }


        JSON.registerObjectMarshaller(Picklist) { Picklist picklist ->
            [
                    id              : picklist.id,
                    name            : picklist.name,
                    description     : picklist.description,
                    picker          : picklist.picker,
                    datePicked      : picklist.datePicked?.format("MM/dd/yyyy"),
                    picklistItems   : picklist.picklistItems,
                    "requisition.id": picklist?.requisition?.id
            ]
        }

        JSON.registerObjectMarshaller(PicklistItem) { PicklistItem picklistItem ->
            [
                    id                  : picklistItem.id,
                    status              : picklistItem.status,
                    "picklist.id"       : picklistItem?.picklist?.id,
                    "requisitionItem.id": picklistItem?.requisitionItem?.id,
                    "inventoryItem.id"  : picklistItem.inventoryItem?.id,
                    "product.name"      : picklistItem?.inventoryItem?.product?.name,
                    "productCode"       : picklistItem?.inventoryItem?.product?.productCode,
                    lotNumber           : picklistItem?.inventoryItem?.lotNumber,
                    expirationDate      : picklistItem?.inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                    "binLocation.id"    : picklistItem?.binLocation?.id,
                    "binLocation.name"  : picklistItem?.binLocation?.name,
                    quantityPicked      : picklistItem.quantity,
                    reasonCode          : picklistItem.reasonCode,
                    comment             : picklistItem.comment
            ]
        }


        JSON.registerObjectMarshaller(Product) { Product product ->
            [
                    id         : product.id,
                    productCode: product.productCode,
                    name       : product.name,
                    description: product.description
            ]
        }

        JSON.registerObjectMarshaller(ProductAssociation) { ProductAssociation productAssociation ->
            [
                    id               : productAssociation.id,
                    type             : productAssociation?.code?.name(),
                    product          : productAssociation.product,
                    associatedProduct: productAssociation.associatedProduct,
                    quantity         : productAssociation.quantity,
                    comments         : productAssociation.comments
            ]
        }


        JSON.registerObjectMarshaller(Receipt) { Receipt receipt ->
            [
                    id                  : receipt.id,
                    expectedDeliveryDate: receipt.expectedDeliveryDate,
                    actualDeliveryDate  : receipt.actualDeliveryDate,
                    recipient           : receipt.recipient,
                    shipment            : receipt.shipment,
                    recipientItems      : receipt.receiptItems
            ]
        }

        JSON.registerObjectMarshaller(ReceiptItem) { ReceiptItem receiptItem ->
            [
                    id              : receiptItem.id,
                    receipt         : receiptItem.receipt,
                    product         : receiptItem.inventoryItem.product,
                    inventoryItem   : receiptItem.inventoryItem,
                    quantityReceived: receiptItem.quantityReceived,
                    quantityShipped : receiptItem.quantityShipped,
                    binLocation     : receiptItem.binLocation,
                    recipient       : receiptItem.recipient
            ]
        }

        JSON.registerObjectMarshaller(Requisition) { Requisition requisition ->
            def defaultName = requisition?.isTemplate ? "Stocklist ${requisition?.id}" : null
            [
                    id               : requisition.id,
                    name             : requisition.name ?: defaultName,
                    requisitionNumber: requisition.requestNumber,
                    description      : requisition.description,
                    isTemplate       : requisition.isTemplate,
                    type             : requisition?.type?.name(),
                    status           : requisition?.status?.name(),
                    commodityClass   : requisition?.commodityClass?.name(),
                    dateRequested    : requisition.dateRequested?.format("MM/dd/yyyy"),
                    dateReviewed     : requisition.dateReviewed?.format("MM/dd/yyyy"),
                    dateVerified     : requisition.dateVerified?.format("MM/dd/yyyy"),
                    dateChecked      : requisition.dateChecked?.format("MM/dd/yyyy"),
                    dateDelivered    : requisition.dateDelivered?.format("MM/dd/yyyy HH:mm XXX"),
                    dateIssued       : requisition.dateIssued?.format("MM/dd/yyyy"),
                    dateReceived     : requisition.dateReceived?.format("MM/dd/yyyy"),
                    origin           : requisition.origin,
                    destination      : requisition.destination,
                    requestedBy      : requisition.requestedBy,
                    reviewedBy       : requisition.reviewedBy,
                    verifiedBy       : requisition.verifiedBy,
                    checkedBy        : requisition.checkedBy,
                    deliveredBy      : requisition.deliveredBy,
                    issuedBy         : requisition.issuedBy,
                    receivedBy       : requisition.receivedBy,
                    recipient        : requisition.recipient,
                    requisitionItems : requisition.requisitionItems
            ]
        }

        JSON.registerObjectMarshaller(RequisitionItem) { RequisitionItem requisitionItem ->
            [
                    id              : requisitionItem.id,
                    status          : requisitionItem.status?.name(),
                    "requisition.id": requisitionItem?.requisition.id,
                    product         : requisitionItem.product,
                    inventoryItem   : requisitionItem.inventoryItem,
                    quantity        : requisitionItem.quantity,
                    quantityApproved: requisitionItem.quantityApproved,
                    quantityCanceled: requisitionItem.quantityCanceled,
                    cancelReasonCode: requisitionItem.cancelReasonCode,
                    cancelComments  : requisitionItem.cancelComments,
                    orderIndex      : requisitionItem.orderIndex,
                    changes         : requisitionItem.change ? [requisitionItem.change] : [],
                    modification    : requisitionItem.modificationItem,
                    substitution    : requisitionItem.substitutionItem,
                    picklistItems   : requisitionItem.picklistItems,
            ]
        }


        JSON.registerObjectMarshaller(Shipment) { Shipment shipment ->
            def containerList = []
            def shipmentItemsByContainer = shipment?.shipmentItems?.groupBy { it.container }
            shipmentItemsByContainer.each { container, shipmentItems ->
                containerList << [id: container?.id, name: container?.name, type: container?.containerType?.name, shipmentItems: shipmentItems]
            }
            return [
                    id                  : shipment.id,
                    name                : shipment.name,
                    status              : shipment?.status?.code?.name(),
                    origin              : [
                            id  : shipment.origin?.id,
                            name: shipment?.origin?.name,
                            type: shipment?.origin?.locationType?.locationTypeCode?.name()
                    ],
                    destination         : [
                            id  : shipment?.destination?.id,
                            name: shipment?.destination?.name,
                            type: shipment?.destination?.locationType?.locationTypeCode?.name()

                    ],
                    expectedShippingDate: shipment.expectedShippingDate?.format("MM/dd/yyyy HH:mm XXX"),
                    actualShippingDate  : shipment.actualShippingDate?.format("MM/dd/yyyy HH:mm XXX"),
                    expectedDeliveryDate: shipment.expectedDeliveryDate?.format("MM/dd/yyyy HH:mm XXX"),
                    actualDeliveryDate  : shipment.actualDeliveryDate?.format("MM/dd/yyyy HH:mm XXX"),
                    shipmentItems       : shipment.shipmentItems,
                    containers          : containerList
            ]
        }

        JSON.registerObjectMarshaller(ShipmentItem) { ShipmentItem shipmentItem ->
            def container = shipmentItem?.container ? [
                    id  : shipmentItem?.container?.id,
                    name: shipmentItem?.container?.name,
                    type: shipmentItem?.container?.containerType?.name] : null
            [
                    id           : shipmentItem.id,
                    inventoryItem: shipmentItem?.inventoryItem,
                    quantity     : shipmentItem.quantity,
                    recipient    : shipmentItem.recipient,
                    shipment     : [id: shipmentItem?.shipment?.id, name: shipmentItem?.shipment?.name],
                    container    : container
            ]
        }


        JSON.registerObjectMarshaller(User) { User user ->
            return user.toJson()
        }

        // Command objects

        JSON.registerObjectMarshaller(AvailableItem) { AvailableItem availableItem ->
            return availableItem.toJson()
        }

        JSON.registerObjectMarshaller(EditPage) { EditPage editPage ->
            return editPage.toJson()
        }

        JSON.registerObjectMarshaller(EditPageItem) { EditPageItem editPageItem ->
            return editPageItem.toJson()
        }

        JSON.registerObjectMarshaller(PartialReceipt) { PartialReceipt partialReceipt ->
            return partialReceipt.toJson()
        }
        JSON.registerObjectMarshaller(PartialReceiptItem) { PartialReceiptItem partialReceiptItem ->
            return partialReceiptItem.toJson()
        }

        JSON.registerObjectMarshaller(PartialReceiptContainer) { PartialReceiptContainer partialReceiptContainer ->
            return partialReceiptContainer.toJson()
        }

        JSON.registerObjectMarshaller(PickPage) { PickPage pickPage ->
            return pickPage.toJson()
        }

        JSON.registerObjectMarshaller(PickPageItem) { PickPageItem pickPageItem ->
            return pickPageItem.toJson()
        }

        JSON.registerObjectMarshaller(PackPage) { PackPage packPage ->
            return packPage.toJson()
        }

        JSON.registerObjectMarshaller(PackPageItem) { PackPageItem packPageItem ->
            return packPageItem.toJson()
        }

        JSON.registerObjectMarshaller(StockAdjustment) { StockAdjustment stockAdjustment ->
            return stockAdjustment.toJson()
        }

        JSON.registerObjectMarshaller(StockMovement) { StockMovement stockMovement ->
            return stockMovement.toJson()
        }

        JSON.registerObjectMarshaller(StockMovementItem) { StockMovementItem stockMovementItem ->
            return stockMovementItem.toJson()
        }

        JSON.registerObjectMarshaller(SubstitutionItem) { SubstitutionItem substitutionItem ->
            return substitutionItem.toJson()
        }

        JSON.registerObjectMarshaller(SuggestedItem) { SuggestedItem suggestedItem ->
            return suggestedItem.toJson()
        }

        JSON.registerObjectMarshaller(Stocklist) { Stocklist stocklist ->
            return stocklist.toJson()
        }

        JSON.registerObjectMarshaller(StocklistItem) { StocklistItem stocklistItem ->
            return stocklistItem.toJson()
        }
    }

}
