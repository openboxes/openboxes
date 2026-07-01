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

import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.date.InstantParser
import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.shipping.ShipmentItem

import static groovy.json.JsonOutput.toJson
import static groovy.json.JsonOutput.prettyPrint

@Transactional(readOnly = true)
class WebhookPublisherService {

    ApiClientService apiClientService
    ProductAvailabilityService productAvailabilityService

    // @deprecated TODO: This is old version of shipment.shipped event publishing, this should be migrated into publishShipmentEvent
    def publishShippedEvent(Shipment shipment) {

        boolean webhooksEnabled = shipment.origin.supports(ActivityCode.ENABLE_WEBHOOKS)
        if (!webhooksEnabled) {
            log.info "Location ${shipment.origin} does not support activity code ${ActivityCode.ENABLE_WEBHOOKS}"
            return
        }

        Map payload = [
                id       : shipment.id,
                type     : "shipment.shipped",
                timestamp: new Date().time,
                user     : AuthService.currentUser?.id,
                location : AuthService.currentLocation?.id,
                data     : [
                        id            : shipment.id,
                        shipmentNumber: shipment.shipmentNumber,
                        origin        : shipment.origin.id,
                        destination   : shipment.destination.id,
                        shipmentType  : shipment.shipmentType,
                        shipmentItems : shipment.shipmentItems.collect {
                            [
                                    id            : it.id,
                                    productName   : it.inventoryItem.product?.name,
                                    productCode   : it.inventoryItem?.product.productCode,
                                    lotNumber     : it?.inventoryItem?.lotNumber,
                                    expirationDate: it?.inventoryItem?.expirationDate ?
                                            it.inventoryItem.expirationDate.format(Constants.EXPIRATION_DATE_FORMAT) :
                                            null,
                                    quantity      : it.quantity
                            ]
                        }
                ]
        ]
        publishEvent(payload)
    }

    def publishRequisitionEvent(Requisition requisition, WebhookEventType eventType) {
        if (!requisition) {
            log.warn "Cannot publish order confirmation webhook event without a requisition"
            return
        }

        boolean webhooksEnabled = requisition.origin.supports(ActivityCode.ENABLE_WEBHOOKS)
        if (!webhooksEnabled) {
            log.info "Location ${requisition.origin} does not support activity code ${ActivityCode.ENABLE_WEBHOOKS}"
            return
        }

        String eventId = UUID.randomUUID().toString()
        String webhookId = UUID.randomUUID().toString()
        Date dateTriggered = new Date()
        User triggeredBy = AuthService.currentUser
        Location facility = requisition.origin

        Map payload = [
                eventId: eventId,
                eventType: eventType?.name,
                eventDate: dateTriggered.toString(),
                triggeredBy: triggeredBy?.name,
                requisitionNumber: requisition.requestNumber,
                requisitionType: requisition.type,
                deliveryTypeCode: requisition.deliveryTypeCode.toString(),
                lines: requisition.requisitionItems?.collect { RequisitionItem item ->
                    [
                            productId: item.product.id,
                            productCode: item.product.productCode,
                            requisitionItemType: item.requisitionItemType.name(),
                            quantityRequested: item.quantity,
                            quantityCanceled: item.quantityCanceled ?: 0,
                            quantityIssued: item.quantityIssued,
                            outboundContainers: item?.picklistItems?.outboundContainer?.findAll { it }?.unique()?.collect { it ->
                                [
                                        id: it.id,
                                        locationNumber: it.locationNumber,
                                        name: it.name
                                ]
                            } ?: [],
                            stagingLocations: item?.picklistItems?.stagingLocation?.findAll { it }?.unique()?.collect { it ->
                                [
                                        id: it.id,
                                        locationNumber: it.locationNumber,
                                        name: it.name
                                ]
                            } ?: [],
                            stagedBy: item?.picklistItems?.stagedBy?.findAll { it }?.unique()?.name ?: [],
                            dateStaged: item?.picklistItems?.dateStaged?.findAll { it }?.unique() ?: []
                    ]
                },
                metadata: [
                        facilityId: facility.id,
                        facilityCode: facility.locationNumber,
                        facilityName: facility.name,
                        webhookId: webhookId,
                        attemptNumber: 1
                ]
        ]

        publishEvent(payload, "openboxes.n8n")
    }

    /**
     * Publishes an inventory adjustment notification based on the baseline and/or adjustment transactions
     * for a product after an inventory adjustment is made (or a product inventory record is created).
     */
    void publishInventoryAdjustmentEvent(Product product, Location facility, Transaction baselineTransaction,
                                         Transaction adjustmentTransaction) {
        if (!product || !facility) {
            log.warn("Missing required product and/or facility. Skipping sending the webhook notification.")
            return
        }

        if (!baselineTransaction && !adjustmentTransaction) {
            log.warn("Missing baseline and adjustment transaction. Skipping sending the webhook notification.")
            return
        }

        boolean webhooksEnabled = facility.supports(ActivityCode.ENABLE_WEBHOOKS)
        if (!webhooksEnabled) {
            log.info "Location ${facility} does not support activity code ${ActivityCode.ENABLE_WEBHOOKS}"
            return
        }

        String eventId = UUID.randomUUID().toString()
        String webhookId = UUID.randomUUID().toString()
        Date dateTriggered = new Date()

        List<AvailableItem> baselineAvailableItems = []
        Integer quantityOnHandFromAvailableItems = 0
        if (!baselineTransaction) {
            // This will be a case of a single row adjustment without baseline, and we need to find adjustment date
            Date baselineDate = JavaUtilDateParser
                    .asDate(InstantParser.asInstant(adjustmentTransaction.transactionDate).minusSeconds(1))
            baselineAvailableItems = productAvailabilityService.getAvailableItemsAtDate(facility, [product], baselineDate)
            quantityOnHandFromAvailableItems = (Integer) baselineAvailableItems?.sum { it.quantityOnHand } ?: 0
        }

        User adjustedBy = adjustmentTransaction?.createdBy ?: baselineTransaction?.createdBy

        Integer quantityBeforeAdjustment = (Integer) (
                baselineTransaction ? baselineTransaction.calculateQuantityByProduct(product) : quantityOnHandFromAvailableItems
        ) ?: 0
        Integer quantityVariance = (Integer) adjustmentTransaction?.calculateQuantityVarianceByProduct(product) ?: 0
        Integer quantityAfterAdjustment = quantityBeforeAdjustment + quantityVariance

        // Since we are sending a notification for single product, we need to filter the baseline and adjustment
        // transaction entries for that product only
        List<TransactionEntry> baselineEntries = baselineTransaction?.getTransactionEntriesByProduct(product)
        List<TransactionEntry> adjustmentEntries = adjustmentTransaction?.getTransactionEntriesByProduct(product)
        Map payload = [
                eventId: eventId,
                eventType: WebhookEventType.ADJUSTMENT_CREATED.name,
                eventDate: dateTriggered.format(Constants.ISO_DATE_TIME_WITH_TIMEZONE_OFFSET_FORMAT),
                triggeredBy: adjustedBy?.name,
                adjustment: [
                        id: adjustmentTransaction?.id ?: baselineTransaction?.id,
                        comment: adjustmentTransaction?.comment,
                        adjustedBy: adjustedBy?.name,
                        dateAdjusted: (adjustmentTransaction ?: baselineTransaction).transactionDate?.format(
                                Constants.ISO_DATE_TIME_WITH_TIMEZONE_OFFSET_FORMAT
                        ),
                        product: product.productCode,
                        totals: [
                                quantityBeforeAdjustment: quantityBeforeAdjustment,
                                quantityAfterAdjustment: quantityAfterAdjustment,
                                quantityVariance: quantityVariance
                        ],
                        adjustments: adjustmentEntries?.collect { TransactionEntry entry ->
                            Integer quantityBefore
                            if (baselineTransaction) {
                                quantityBefore = baselineEntries?.find { TransactionEntry it ->
                                    it.inventoryItem?.id == entry.inventoryItem?.id && it.binLocation?.id == entry.binLocation?.id
                                }?.quantity ?: 0
                            } else {
                                quantityBefore = baselineAvailableItems?.find { AvailableItem it ->
                                    it.inventoryItem.id == entry.inventoryItem.id && it.binLocation?.id == entry.binLocation?.id
                                }?.quantityOnHand ?: 0
                            }

                            [
                                    location: entry.binLocation?.locationNumber,
                                    inventoryItem: entry.inventoryItem?.id,
                                    quantityBeforeAdjustment: quantityBefore,
                                    quantityAfterAdjustment: quantityBefore + entry.quantityVariance,
                                    quantityVariance: entry.quantityVariance,
                                    reasonCode: entry.reasonCode,
                                    comment: entry.comments,
                            ]
                        } ?: []
                ],
                metadata: [
                        facilityId: facility.id,
                        facilityCode: facility.locationNumber,
                        facilityName: facility.name,
                        webhookId: webhookId,
                        attemptNumber: 1
                ]
        ]

        log.info("Publishing inventory adjustment webhook event for product ${product?.productCode} at facility ${facility?.name}")
        log.debug(prettyPrint(toJson(payload).toString()))
        publishEvent(payload, "openboxes.n8n")
    }

    /**
     * Publishes a cycle count completed notification based on the baseline and/or adjustment transactions for the
     * specific product and facility.
     */
    void publishCycleCountCompletedEvent(Product product, Location facility, Transaction baselineTransaction,
                                         Transaction adjustmentTransaction) {
        if (!product || !facility) {
            log.warn("Missing required product and/or facility. Skipping sending the webhook notification.")
            return
        }

        // Each cycle count has a baseline, it might not have an adjustment transaction
        if (!baselineTransaction) {
            log.warn("Missing required baseline transaction. Skipping sending the webhook notification.")
            return
        }

        boolean webhooksEnabled = facility.supports(ActivityCode.ENABLE_WEBHOOKS)
        if (!webhooksEnabled) {
            log.info "Location ${facility} does not support activity code ${ActivityCode.ENABLE_WEBHOOKS}"
            return
        }

        String eventId = UUID.randomUUID().toString()
        String webhookId = UUID.randomUUID().toString()
        Date dateTriggered = new Date()

        User countedBy = adjustmentTransaction?.createdBy ?: baselineTransaction?.createdBy

        Integer quantityBeforeAdjustment = (Integer) baselineTransaction.calculateQuantityByProduct(product) ?: 0
        Integer quantityVariance = (Integer) adjustmentTransaction?.calculateQuantityVarianceByProduct(product) ?: 0
        Integer quantityAfterAdjustment = quantityBeforeAdjustment + quantityVariance

        // Since we are sending a notification for single product, we need to filter the baseline and adjustment
        // transaction entries for that product only
        List<TransactionEntry> baselineEntries = baselineTransaction?.getTransactionEntriesByProduct(product)
        List<TransactionEntry> adjustmentEntries = adjustmentTransaction?.getTransactionEntriesByProduct(product)
        Map payload = [
                eventId: eventId,
                eventType: WebhookEventType.CYCLE_COUNT_COMPLETED.name,
                eventDate: dateTriggered.format(Constants.ISO_DATE_TIME_WITH_TIMEZONE_OFFSET_FORMAT),
                triggeredBy: countedBy?.name,
                count: [
                        id: adjustmentTransaction?.id ?: baselineTransaction?.id,
                        comment: adjustmentTransaction?.comment,
                        countedBy: countedBy?.name,
                        dateCounted: (adjustmentTransaction ?: baselineTransaction).transactionDate?.format(
                                Constants.ISO_DATE_TIME_WITH_TIMEZONE_OFFSET_FORMAT
                        ),
                        product: product.productCode,
                        totals: [
                                quantityBeforeAdjustment: quantityBeforeAdjustment,
                                quantityAfterAdjustment: quantityAfterAdjustment,
                                quantityVariance: quantityVariance
                        ],
                        adjustments: adjustmentEntries?.collect { TransactionEntry entry ->
                            Integer quantityBefore = baselineEntries?.find { TransactionEntry it ->
                                it.inventoryItem?.id == entry.inventoryItem?.id && it.binLocation?.id == entry.binLocation?.id
                            }?.quantity ?: 0

                            [
                                    location: entry.binLocation?.locationNumber,
                                    inventoryItem: entry.inventoryItem?.id,
                                    quantityBeforeAdjustment: quantityBefore,
                                    quantityAfterAdjustment: quantityBefore + entry.quantityVariance,
                                    quantityVariance: entry.quantityVariance,
                                    reasonCode: entry.reasonCode,
                                    comment: entry.comments,
                            ]
                        } ?: []
                ],
                metadata: [
                        facilityId: facility.id,
                        facilityCode: facility.locationNumber,
                        facilityName: facility.name,
                        webhookId: webhookId,
                        attemptNumber: 1
                ]
        ]

        log.info("Publishing cycle count event notification for product ${product?.productCode} at facility ${facility?.name}")
        log.debug(prettyPrint(toJson(payload).toString()))
        publishEvent(payload, "openboxes.n8n")
    }

    def publishEvent(Map payload, String configPath = "openboxes.webhook") {
        try {
            boolean webhooksEnabled = Holders.config.get("${configPath}.enabled")
            if (!webhooksEnabled) {
                log.info "Webhooks for config path ${configPath} are disabled"
                return
            }

            String webhookUrl = Holders.config.get("${configPath}.endpoint.url")
            Map headers = Holders.config.get("${configPath}.endpoint.headers")
            apiClientService.post(webhookUrl, payload, headers)
        } catch (Exception e) {
            log.error("Failed to publish webhook event due to error: " + e.message, e)
        }
    }
}
