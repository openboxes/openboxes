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
import grails.util.Holders
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
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
import org.pih.warehouse.core.Address
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.GlAccountType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Party
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UploadService
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.OutboundStockMovementListItem
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceItemCandidate
import org.pih.warehouse.invoice.InvoiceList
import org.pih.warehouse.jobs.RefreshDemandDataJob
import org.pih.warehouse.jobs.RefreshOrderSummaryJob
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.jobs.RefreshStockoutDataJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderSummary
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.ProductListItem
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSearchDto
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.quartz.Scheduler
import util.LiquibaseUtil

import javax.sql.DataSource
import java.math.RoundingMode

class BootStrap {

    UploadService uploadService
    DataSource dataSource

    def init = { servletContext ->
        log.info("Registering JSON marshallers ...")
        registerJsonMarshallers()

        log.info("Executing database migrations ...")
        executeDatabaseMigrations()

        log.info("Starting Quartz scheduler ...")
        Scheduler scheduler = Holders.grailsApplication.mainContext.getBean( 'quartzScheduler' )
        scheduler.start()

        // Create uploads directory if it doesn't already exist
        log.info("Creating uploads directory ...")
        uploadService.findOrCreateUploadsDirectory()

        refreshAnalyticsData()
    }

    void refreshAnalyticsData() {
        Boolean isRefreshAnalyticsDataOnStartupEnabled = Holders.config.openboxes.refreshAnalyticsDataOnStartup.enabled
        if (isRefreshAnalyticsDataOnStartupEnabled) {
            log.info("Refresh analytics data on startup ...")

            // Refresh stock out data on startup to make sure the fact table is created
            RefreshStockoutDataJob.triggerNow()

            // Refresh demand data on startup to make sure the materialized views are created
            RefreshDemandDataJob.triggerNow()

            // Refresh inventory snapshot data
            RefreshProductAvailabilityJob.triggerNow([forceRefresh: Boolean.TRUE])

            // Refresh order summary materialized view
            RefreshOrderSummaryJob.triggerNow()
        }
    }

    void registerJsonMarshallers() {

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
                description: shipmentType.description,
                displayName: shipmentType.displayName,
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

        JSON.registerObjectMarshaller(Address) { Address address ->
            [
                address             : address.address,
                address2            : address.address2,
                city                : address.city,
                country             : address.country,
                description         : address.description,
                postalCode          : address.postalCode,
                stateOrProvince     : address.stateOrProvince,
            ]
        }

        JSON.registerObjectMarshaller(Location) { Location location ->
            return location.toJson()
        }

        JSON.registerObjectMarshaller(Order) { Order order ->
            return order.toJson()
        }

        JSON.registerObjectMarshaller(OrderSummary) { OrderSummary orderSummary ->
            def defaultCurrencyCode = Holders.config.openboxes.locale.defaultCurrencyCode
            def origOrgCode = orderSummary?.order?.origin?.organization?.code
            def destOrgCode = orderSummary?.order?.destination?.organization?.code
            return [
                status: orderSummary?.derivedStatus,
                id: orderSummary?.order?.id,
                orderNumber: orderSummary?.order?.orderNumber,
                name: orderSummary?.order?.name,
                paymentTerm: orderSummary?.order?.paymentTerm,
                origin: orderSummary?.order?.origin?.name + (origOrgCode ? " (${origOrgCode})" : ""),
                destination: orderSummary?.order?.destination?.name + (destOrgCode ? " (${destOrgCode})" : ""),
                dateOrdered: orderSummary?.order?.dateOrdered,
                orderedBy: orderSummary?.order?.orderedBy?.name,
                createdBy: orderSummary?.order?.createdBy?.name,
                orderItemsCount: orderSummary?.order?.activeOrderItems?.size()?:0,
                orderedOrderItemsCount: orderSummary?.itemsOrdered?:0,
                shippedItemsCount: orderSummary?.itemsShipped?:0,
                receivedItemsCount: orderSummary?.itemsReceived?:0,
                invoicedItemsCount: orderSummary?.itemsInvoiced?:0,
                total: "${orderSummary?.order?.total?.setScale(2, RoundingMode.HALF_UP)} ${orderSummary?.order?.currencyCode ?: defaultCurrencyCode}",
                totalNormalized: "${orderSummary?.order?.totalNormalized?.setScale(2, RoundingMode.HALF_UP)} ${defaultCurrencyCode}",
                shipmentsCount: orderSummary?.order?.shipments?.size(),
            ]
        }

        JSON.registerObjectMarshaller(PaymentTerm) { PaymentTerm paymentTerm ->
            return paymentTerm.toJson()
        }

        JSON.registerObjectMarshaller(OrderItem) { OrderItem orderItem ->
            return orderItem.toJson()
        }

        JSON.registerObjectMarshaller(Organization) { Organization organization ->
            return organization.toJson()
        }

        JSON.registerObjectMarshaller(Party) { Party party ->
            return party.toJson()
        }

        JSON.registerObjectMarshaller(PartyRole) { PartyRole partyRole ->
            return partyRole.toJson()
        }

        JSON.registerObjectMarshaller(PartyType) { PartyType partyType ->
            return partyType.toJson()
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
                id                      : picklistItem.id,
                status                  : picklistItem.status,
                "picklist.id"           : picklistItem?.picklist?.id,
                "requisitionItem.id"    : picklistItem?.requisitionItem?.id,
                "inventoryItem.id"      : picklistItem.inventoryItem?.id,
                "product.name"          : picklistItem?.inventoryItem?.product?.name,
                "product.displayName"   : picklistItem?.inventoryItem?.product?.displayName,
                "product.color"         : picklistItem?.inventoryItem?.product?.color,
                "productCode"           : picklistItem?.inventoryItem?.product?.productCode,
                lotNumber               : picklistItem?.inventoryItem?.lotNumber,
                expirationDate          : picklistItem?.inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                "binLocation.id"        : picklistItem?.binLocation?.id,
                "binLocation.name"      : picklistItem?.binLocation?.name,
                "binLocation.zoneId"    : picklistItem?.binLocation?.zone?.id,
                "binLocation.zoneName"  : picklistItem?.binLocation?.zone?.name,
                quantityPicked          : picklistItem.quantity,
                reasonCode              : picklistItem.reasonCode,
                comment                 : picklistItem.comment
            ]
        }

        JSON.registerObjectMarshaller(Product) { Product product ->
            [
                id                 : product.id,
                productCode        : product.productCode,
                name               : product.name,
                description        : product.description,
                category           : product.category?.name,
                unitOfMeasure      : product.unitOfMeasure,
                pricePerUnit       : product.pricePerUnit,
                dateCreated        : product.dateCreated,
                lastUpdated        : product.lastUpdated,
                updatedBy          : product.updatedBy?.name,
                color              : product.color,
                handlingIcons      : product.handlingIcons,
                lotAndExpiryControl: product.lotAndExpiryControl,
                active             : product.active,
                // Introduced new object (decided not to use productNames or synonyms).
                // that includes the display name for all locales. This gives us a little more
                // flexibility in case we don't like it or it performs poorly. We can also
                // convert it into a DTO class if we end up liking it.
                displayNames        : product.displayNames,
            ]
        }

        JSON.registerObjectMarshaller(ProductCatalog) { ProductCatalog productCatalog ->
            [
                id          : productCatalog.id,
                code        : productCatalog.code,
                name        : productCatalog.name,
                description : productCatalog.description,
            ]
        }

        JSON.registerObjectMarshaller(ProductGroup) { ProductGroup productGroup ->
            [
                id          : productGroup.id,
                name        : productGroup.name,
                description : productGroup.description,
            ]
        }

        JSON.registerObjectMarshaller(GlAccount) { GlAccount glAccount ->
            [
                id              : glAccount.id,
                name            : glAccount.name,
                code            : glAccount.code,
                description     : glAccount.description,
                glAccountType   : glAccount.glAccountType,
            ]
        }

        JSON.registerObjectMarshaller(GlAccountType) { GlAccountType glAccountType ->
            return [
                id : glAccountType.id,
                name : glAccountType.name,
                code : glAccountType.code,
                accountTypeCode : glAccountType.glAccountTypeCode.name(),
            ]
        }

        JSON.registerObjectMarshaller(ProductListItem) { ProductListItem productListItem ->
            return productListItem.toJson()
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
            return shipment?.toJson()
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

        JSON.registerObjectMarshaller(OutboundStockMovementListItem) { OutboundStockMovementListItem outboundStockMovementListItem ->
            return outboundStockMovementListItem.toJson()
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

        JSON.registerObjectMarshaller(InvoiceList) { InvoiceList invoiceListItem ->
            [
                id: invoiceListItem?.invoice?.id,
                invoiceNumber: invoiceListItem?.invoiceNumber,
                invoiceTypeCode: invoiceListItem?.invoiceTypeCode?.name(),
                status: invoiceListItem?.status?.name(),
                partyCode: "${invoiceListItem?.partyCode} ${invoiceListItem?.partyName}",
                vendorInvoiceNumber: invoiceListItem?.vendorInvoiceNumber,
                totalValue: invoiceListItem?.invoice?.totalValue,
                currency: invoiceListItem?.currency,
                itemCount: invoiceListItem?.itemCount,
            ]
        }

        JSON.registerObjectMarshaller(InvoiceItem) { InvoiceItem invoiceItem ->
            return invoiceItem.toJson()
        }

        JSON.registerObjectMarshaller(InvoiceItemCandidate) { InvoiceItemCandidate invoiceItemCandidate ->
            return invoiceItemCandidate.toJson()
        }

        JSON.registerObjectMarshaller(Event) { Event event ->
            return event.toJson()
        }

        JSON.registerObjectMarshaller(ProductSupplier) { ProductSupplier productSupplier ->
            return productSupplier.toJson()
        }

        JSON.registerObjectMarshaller(ProductPackage) { ProductPackage productPackage ->
            return productPackage.toJson()
        }
    }

    def destroy = {

    }

    void executeDatabaseMigrations() {
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

            def connection = new JdbcConnection(dataSource.getConnection())
            if (connection == null) {
                throw new RuntimeException("Connection could not be created.")
            }

            def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
            database.setDefaultSchemaName(connection.catalog)
            boolean isRunningMigrations = LiquibaseUtil.isRunningMigrations()
            log.info("Liquibase running: ${isRunningMigrations}")
            log.info("Setting default schema to ${connection.catalog}")
            log.info("Product Version: ${database.databaseProductVersion}")
            log.info("Database Version: ${database.databaseMajorVersion}.${database.databaseMinorVersion} (${database.databaseProductName} ${database.databaseProductVersion})")

            // Ensure the databasechangelog up-to-date in order to handle
            // DatabaseException: Error executing SQL SELECT * FROM obnav.DATABASECHANGELOG
            // ORDER BY DATEEXECUTED ASC, ORDEREXECUTED ASC: Unknown column 'ORDEREXECUTED' in 'order clause'
            liquibase = new Liquibase(null as DatabaseChangeLog, new ClassLoaderResourceAccessor(), database)
            liquibase.checkLiquibaseTables(false, null, new Contexts(), new LabelExpression())

            // This needs to happen before we execute any changelogs in order to ensure that we
            // FIXME could we ever be in a state where the install changelog has not been executed
            //  but we have records in DATABASECHANGELOG
            // FIXME, we should probably add some logic to look for an "install" changelog
            List executedChangeLogVersions = LiquibaseUtil.getExecutedChangelogVersions()
            log.info("executedChangelogVersions: " + executedChangeLogVersions)

            // The process should always start with us dropping views (they are recreated at the end)
            log.info 'Dropping all views (will rebuild after migrations complete)...'
            LiquibaseUtil.executeChangeLog("views/drop-all-views.xml")


            // If nothing has been created yet, let's create all new database objects with the install scripts
            if (!executedChangeLogVersions) {
                log.info("Running install changelog ...")
                LiquibaseUtil.executeChangeLog("install/changelog.xml")
            }

            // Get all possible "upgrade to latest" versions. Currently, 0.9.x is the "install",
            // version while 0.8.x, 0.7.x, 0.6.x, 0.5.x are considered "upgrade" versions.
            Set<String> upgradeChangeLogVersions = LiquibaseUtil.upgradeChangeLogVersions
            log.info("upgradeChangeLogVersions: " + upgradeChangeLogVersions)

            // This should be removed if we ever reset changelogs so there are no "upgrade" versions
            // in the source code (although this will probably never happen)
            if (upgradeChangeLogVersions?.empty) {
                throw new RuntimeException("Unable to determine whether there are database migrations to be executed")
            }

            // Check if any of the executed changelog versions include one of the "upgrade" versions
            // and if so, then we need to keep running the upgrade changelogs to catch up to 0.9.x
            boolean hasExecutedAnyUpgradeChangeLog =
                executedChangeLogVersions.any { upgradeChangeLogVersions.contains(it.version) }

            // Run through the upgrade changelog
            // note: Liquibase does the hard work of determining what changesets need to be applied
            if (hasExecutedAnyUpgradeChangeLog) {
                log.info("Running upgrade changelog ...")
                LiquibaseUtil.executeChangeLog("upgrade/changelog.xml")
            }

            // And now we need to run changelogs from 0.9.x and beyond
            log.info("Running latest updates")
            LiquibaseUtil.executeChangeLog("changelog.groovy")

            log.info 'Rebuilding views ...'
            LiquibaseUtil.executeChangeLog("views/changelog.xml")

        } finally {
            log.info('Safely closing database')
            liquibase?.database?.close()
        }
        log.info("Finished running liquibase changelog(s)!")
    }

}
