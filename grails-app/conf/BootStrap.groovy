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
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.EditPageItem
import org.pih.warehouse.api.PackPageItem
import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.PartialReceiptContainer
import org.pih.warehouse.api.PartialReceiptItem
import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockAdjustment
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.api.StocklistItem
import org.pih.warehouse.api.SubstitutionItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.invoice.InvoiceItemCandidate
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.jobs.RefreshDemandDataJob
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.jobs.RefreshStockoutDataJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductSearchDto
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType
import util.LiquibaseUtil

import javax.sql.DataSource

class BootStrap {

    def identifierService
    def grailsApplication
    def uploadService
    DataSource dataSource

    def init = { servletContext ->

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
                    hasPackingSupport    : location.supports(ActivityCode.PACK_SHIPMENT),
                    hasPartialReceivingSupport : location.supports(ActivityCode.PARTIAL_RECEIVING),
                    hasCentralPurchasingEnabled : location.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING),
                    organizationName     : location?.organization?.name,
                    organizationCode     : location?.organization?.code,
                    backgroundColor : location?.bgColor,
                    zoneName : location?.zone?.name,
                    zoneId : location?.zone?.id
            ]
        }

        JSON.registerObjectMarshaller(Order) { Order order ->
            return order.toJson()
        }

        JSON.registerObjectMarshaller(OrderItem) { OrderItem orderItem ->
            return orderItem.toJson()
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
                    id                    : picklistItem.id,
                    status                : picklistItem.status,
                    "picklist.id"         : picklistItem?.picklist?.id,
                    "requisitionItem.id"  : picklistItem?.requisitionItem?.id,
                    "inventoryItem.id"    : picklistItem.inventoryItem?.id,
                    "product.name"        : picklistItem?.inventoryItem?.product?.name,
                    "productCode"         : picklistItem?.inventoryItem?.product?.productCode,
                    lotNumber             : picklistItem?.inventoryItem?.lotNumber,
                    expirationDate        : picklistItem?.inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                    "binLocation.id"      : picklistItem?.binLocation?.id,
                    "binLocation.name"    : picklistItem?.binLocation?.name,
                    "binLocation.zoneId"  : picklistItem?.binLocation?.zone?.id,
                    "binLocation.zoneName": picklistItem?.binLocation?.zone?.name,
                    quantityPicked        : picklistItem.quantity,
                    reasonCode            : picklistItem.reasonCode,
                    comment               : picklistItem.comment
            ]
        }


        JSON.registerObjectMarshaller(Product) { Product product ->
            return product.toJson()
        }

        JSON.registerObjectMarshaller(ProductSearchDto) { ProductSearchDto productSearchDto ->
            return productSearchDto.toJson()
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

        JSON.registerObjectMarshaller(PickPageItem) { PickPageItem pickPageItem ->
            return pickPageItem.toJson()
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

        JSON.registerObjectMarshaller(InvoiceItem) { InvoiceItem invoiceItem ->
            return invoiceItem.toJson()
        }

        JSON.registerObjectMarshaller(InvoiceItemCandidate) { InvoiceItemCandidate invoiceItemCandidate ->
            return invoiceItemCandidate.toJson()
        }



        // ================================    Static Data    ============================================
        //
        // Use the 'demo' environment to create a database with 'static' and 'demo' data.  Then
        // run the following:
        //
        // 		$ grails -Dgrails.env=demo run-app
        //
        // In another terminal, run through these commands to generate the appropriate
        // changelog files for a new version of the data model
        //
        // 		$ grails db-diff-schema > grails-app/migrations/x.x.x/changelog-initial-schema.xml
        // 		$ grails db-diff-index > grails-app/migrations/x.x.x/changelog-initial-indexes.xml
        // 		$ grails db-diff-data > grails-app/migrations/x.x.x/changelog-initial-data.xml
        //
        // Migrating existing data to the new data model is still a work in progress, but you can
        // use the previous versions changelogs.
        //
        log.info("Running liquibase changelog(s) ...")
        Liquibase liquibase = null
        try {

            def connection = dataSource.getConnection()
            if (connection == null) {
                throw new RuntimeException("Connection could not be created.")
            }
            def classLoader = getClass().classLoader
            def fileOpener = classLoader.loadClass("org.liquibase.grails.GrailsFileOpener").getConstructor().newInstance()

            def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
            boolean isRunningMigrations = LiquibaseUtil.isRunningMigrations()
            log.info("Liquibase running: " + isRunningMigrations)
            log.info("Setting default schema to " + connection.catalog)
            log.info("Product Version: " + database.databaseProductVersion)
            log.info("Database Version: " + database.databaseMajorVersion + "." + database.databaseMinorVersion)
            def ranChangeSets = database.getRanChangeSetList()
            database.setDefaultSchemaName(connection.catalog)

            //If nothing has been created yet, let's create all new database objects with the install scripts
            if (!ranChangeSets) {
                liquibase = new Liquibase("install/install.xml", fileOpener, database)
                liquibase.update(null)
            }

            // Run through the updates in the master changelog
            liquibase = new Liquibase("changelog.xml", fileOpener, database)
            liquibase.update(null)
        }
        finally {
            if (liquibase && liquibase.database) {
                liquibase.database.close()
            }
        }
        log.info("Finished running liquibase changelog(s)!")

        // Create uploads directory if it doesn't already exist
        uploadService.findOrCreateUploadsDirectory()

        Boolean refreshAnalyticsDataOnStartup = grailsApplication.config.openboxes.refreshAnalyticsDataOnStartup.enabled
        if (refreshAnalyticsDataOnStartup) {
            // Refresh stock out data on startup to make sure the fact table is created
            RefreshStockoutDataJob.triggerNow()

            // Refresh demand data on startup to make sure the materialized views are created
            RefreshDemandDataJob.triggerNow()

            // Refresh inventory snapshot data
            RefreshProductAvailabilityJob.triggerNow([forceRefresh: Boolean.TRUE]);
        }
    }


    def destroy = {

    }

}
