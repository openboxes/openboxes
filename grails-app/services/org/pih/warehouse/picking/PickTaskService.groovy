package org.pih.warehouse.picking

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.picking.SearchPickTaskCommand
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionAction
import org.pih.warehouse.inventory.TransactionSource
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.picklist.PicklistService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment

@Transactional
class PickTaskService {

    GrailsApplication grailsApplication
    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService
    PicklistService picklistService
    StockMovementService stockMovementService

    @Transactional(readOnly = true)
    List<PickTask> search(SearchPickTaskCommand command, Map params = [:]) {
        log.info "Searching pick tasks for facility=${command.facility?.name}, deliveryTypeCode=${command.deliveryTypeCode}, ordersCount=${command.ordersCount}"

        Integer max = params.int('max') ?: 100
        Integer offset = params.int('offset') ?: 0

        List<String> requisitionIds = findRequisitionIdsForPicking(command)

        List<PickTaskStatus> statusesToSearch = command.status
        if (!statusesToSearch  && !command.outboundContainerId && !command.requisitionId) {
            statusesToSearch = [PickTaskStatus.PENDING, PickTaskStatus.PICKING]
        }

        List<PickTask> tasks = PickTask.createCriteria().list(max: max, offset: offset) {
            if (command.facility) {
                eq("facility", command.facility)
            }

            if (!requisitionIds.isEmpty()) {
                'in'("requisition.id", requisitionIds)
            }

            if (command.deliveryTypeCode) {
                eq("deliveryTypeCode", command.deliveryTypeCode)
            }

            if (command.assigneeId) {
                eq("assignee.id", command.assigneeId)
            }

            if (statusesToSearch) {
                'in'("status", statusesToSearch)
            }

            if (command.priority) {
                eq("priority", command.priority)
            }

            if (command.outboundContainerId) {
                createAlias("outboundContainer", "oc")

                or {
                    eq("oc.id", command.outboundContainerId)
                    eq("oc.locationNumber", command.outboundContainerId)
                }
            }

            if (command.requisitionId) {
                createAlias("requisition", "r")

                or {
                    eq("r.id", command.requisitionId)
                    eq("r.requestNumber", command.requisitionId)
                }
            }

            order("priority", "asc")
            order("dateCreated", "asc")
            createAlias("location", "l")
            order("l.name", "asc")
        }

        return tasks
    }

    @Transactional(readOnly = true)
    PickTask get(String id) {
        if (!id) {
            return null
        }

        return PickTask.get(id)
    }

    PickTask patch(String id, Map data = [:]) {
        PickTask task = get(id)

        if (!task) {
            throw new ObjectNotFoundException(id, "Pick task ${id} was not found")
        }

        switch (data?.action) {

            case 'start':
                start(task, data.assigneeId as String)
                break

            case 'pick':
                pick(task, data.outboundContainerId as String, data.pickedById as String)
                break

            case 'short-pick':
                shortPick(task, data.outboundContainerId as String, data.quantityPicked as Integer,
                        data.pickedById as String, data.reasonCode as String)
                break

            default:
                throw new UnsupportedOperationException("Unsupported action: ${data?.action}")
        }

        if (task) {
            task.discard()
        }

        return task
    }

    void start(PickTask task, String assigneeId) {
        executeStateTransition(task, PickTaskStatus.PICKING)

        PicklistItem existingPickItem = PicklistItem.get(task.id)
        existingPickItem.assignee = Person.get(assigneeId)
        existingPickItem.dateAssigned = new Date()
        existingPickItem.dateStarted = new Date()

        save(task)
    }

    void pick(PickTask task, String outboundContainerId, String pickedById) {
        Location outboundContainer = Location.findByLocationNumberOrId(outboundContainerId, outboundContainerId)
        validateOutboundContainer(outboundContainer, task)

        executeStateTransition(task, PickTaskStatus.PICKED)
        transferToContainer(task, outboundContainer, task.quantityRequired.toInteger())

        PicklistItem existingPickItem = PicklistItem.get(task.id)
        existingPickItem.pickedBy = Person.get(pickedById)
        existingPickItem.datePicked = new Date()
        existingPickItem.outboundContainer = outboundContainer
        existingPickItem.quantityPicked = task.quantityRequired.toInteger()

        save(task)
    }

    void shortPick(PickTask task, String outboundContainerId, Integer quantityPicked, String pickedById, String reasonCode) {
        Location outboundContainer = null
        if (outboundContainerId != null) {
            outboundContainer = Location.findByLocationNumberOrId(outboundContainerId, outboundContainerId)
            validateOutboundContainer(outboundContainer, task)
        }

        PicklistItem existingPickItem = PicklistItem.get(task.id)
        if (quantityPicked > 0) {
            picklistService.updatePicklistItem(existingPickItem.id, task.product?.id, quantityPicked.toBigDecimal(), pickedById, reasonCode)
            transferToContainer(task, outboundContainer, quantityPicked)
            existingPickItem.outboundContainer = outboundContainer
        }

        def isFullyPicked = task.quantityRequired.toInteger() == existingPickItem.quantityPicked

        if (quantityPicked == 0 && reasonCode) { // 0-quantity short pick, we skip transfer to container
            existingPickItem.reasonCode = reasonCode
            // Only use STAGED if nothing was ever picked (pure cancellation)
            if (!existingPickItem.quantityPicked || existingPickItem.quantityPicked == 0) {
                // STAGED is used as a terminal status for items that were never picked - there's nothing to physically stage
                executeStateTransition(task, PickTaskStatus.STAGED)
            } else {
                executeStateTransition(task, PickTaskStatus.PICKED)
            }
        } else if (reasonCode || isFullyPicked) {
            executeStateTransition(task, PickTaskStatus.PICKED)
        }

        save(task)
    }

    List<PickTask> reallocate(PickTask task, List picklistItems) {
        if (!picklistItems) {
            throw new IllegalArgumentException("Must specify picklistItems")
        }

        RequisitionItem requisitionItem = task.requisitionItem
        if (!requisitionItem) {
            throw new IllegalStateException("Pick task ${task.id} has no associated requisition item")
        }

        // Delete only the specific picklist item being reallocated (task.id === picklistItem.id)
        PicklistItem currentPicklistItem = PicklistItem.get(task.id)
        if (currentPicklistItem) {
            Picklist picklist = currentPicklistItem.picklist
            currentPicklistItem.disableRefresh = Boolean.TRUE
            picklist?.removeFromPicklistItems(currentPicklistItem)
            requisitionItem.removeFromPicklistItems(currentPicklistItem)
            currentPicklistItem.delete(flush: true)
        }

        // Create new picklist items for each selected bin location
        picklistItems.each { item ->
            InventoryItem inventoryItem = item.inventoryItem?.id ?
                    InventoryItem.get(item.inventoryItem.id) : null

            Location binLocation = item.binLocation?.id ?
                    Location.get(item.binLocation.id) : null

            Integer quantityToPick = item.quantity ? new Integer(item.quantity) : null

            stockMovementService.createOrUpdatePicklistItem(
                    requisitionItem,
                    null,
                    inventoryItem,
                    binLocation,
                    0,
                    null,
                    null,
                    false,
                    quantityToPick
            )
        }

        // Return newly created pick tasks for this requisition item
        return PickTask.findAllByRequisitionItem(requisitionItem)
    }

    def drop(String outboundContainerId, Map data = [:]) {
        Location outboundContainer = Location.findByLocationNumberOrId(outboundContainerId, outboundContainerId)
        if (!outboundContainer) {
            throw new IllegalArgumentException("Outbound container with identifier ${outboundContainerId} does not exist")
        }

        switch (data?.action) {

            case 'drop':
                dropToStaging(outboundContainer, data.stagingLocationId as String, data.stagedById as String)
                break

            default:
                throw new UnsupportedOperationException("Unsupported action: ${data?.action}")
        }
    }

    void save(PickTask task) {
        PicklistItem existingPickItem = PicklistItem.get(task.id)
        existingPickItem.status = task.status.toString()
        Requisition requisition = existingPickItem?.requisitionItem?.requisition

        if (!requisition) {
            task.discard()
            return
        }

        def allOrderTasks = requisition?.picklist?.picklistItems ?: []

        if (task.status == PickTaskStatus.PICKED) {
            boolean allTasksPicked = allOrderTasks.every {
                it.status == PickTaskStatus.PICKED.name()
            }
            boolean partialAllocation = requisition.partialAllocationAllowed
            boolean backordered = requisition.requisitionItems?.any { it.isBackordered() }
            if (allTasksPicked && (!partialAllocation || !backordered)) {
                requisition.status = RequisitionStatus.PICKED
            }
        } else if (task.status == PickTaskStatus.STAGED) {
            boolean allTasksStaged = allOrderTasks.every {
                it.status == PickTaskStatus.STAGED.name()
            }
            boolean partialAllocation = requisition.partialAllocationAllowed
            boolean backordered = requisition.requisitionItems?.any { it.isBackordered() }
            if (allTasksStaged && (!partialAllocation || !backordered)) {
                requisition.status = RequisitionStatus.STAGED

                if (!requisition.shipment) {
                    StockMovement stockMovement = StockMovement.createFromRequisition(requisition)
                    Shipment shipment = stockMovementService.createShipment(stockMovement)
                    stockMovementService.createMissingShipmentItems(requisition, shipment)
                }
            }
        }

        requisition.save(cascade: true, failOnError: true)
        task.discard()
    }

    void executeStateTransition(PickTask task, PickTaskStatus to, boolean validate = true) {
        PickTaskStatus from = task.status ?: PickTaskStatus.PENDING
        if (validate && !PickTaskStatus.validateTransition(from, to)) {
            throw new IllegalStateException("Invalid transition ${from} -> ${to}")
        }
        task.status = to
    }

    void dropToStaging(Location outboundContainer, String stagingLocationId, String stagedById) {
        Location stagingLocation = Location.findByLocationNumberOrId(stagingLocationId, stagingLocationId)
        validateStagingLocation(stagingLocation)

        List<AvailableItem> itemsToMove = productAvailabilityService.getAvailableItems(outboundContainer)

        if (!itemsToMove) {
            throw new IllegalStateException("Outbound container with number ${outboundContainer.locationNumber} is empty")
        }

        Person stagedBy = Person.get(stagedById)
        itemsToMove.each { item ->
            List<PickTask> tasks = PickTask.findAllByOutboundContainerAndInventoryItemAndStatus(
                    outboundContainer, item.inventoryItem, PickTaskStatus.PICKED)
            tasks.each { task ->
                executeStateTransition(task, PickTaskStatus.STAGED)
                transferToStaging(task, item, stagingLocation)

                PicklistItem existingPickItem = PicklistItem.get(task.id)
                existingPickItem.stagingLocation = stagingLocation
                existingPickItem.dateStaged = new Date()
                existingPickItem.stagedBy = stagedBy

                save(task)
            }
        }
    }

    void transferToContainer(PickTask task, Location container, Integer quantity) {
        if (!task.facility.supports(ActivityCode.TRACK_INTERNAL_TRANSACTIONS)) {
            log.warn("Skipping transfer to container: Facility does not support TRACK_INTERNAL_TRANSACTIONS activity")
            return
        }

        TransferStockCommand command = new TransferStockCommand()
        command.location = task.facility
        command.binLocation = task.location
        command.inventoryItem = task.inventoryItem
        command.quantity = quantity
        command.otherLocation = task.facility
        command.otherBinLocation = container
        command.transferOut = Boolean.TRUE
        command.disableRefresh = Boolean.TRUE
        transfer(task, command)
    }

    void transferToStaging(PickTask task, AvailableItem item, Location stagingLocation) {
        if (!task.facility.supports(ActivityCode.TRACK_INTERNAL_TRANSACTIONS)) {
            log.warn("Skipping transfer to staging: Facility does not support TRACK_INTERNAL_TRANSACTIONS activity")
            return
        }

        TransferStockCommand command = new TransferStockCommand()
        command.location = item.binLocation?.parentLocation
        command.binLocation = item.binLocation
        command.inventoryItem = item.inventoryItem
        command.quantity = item.quantityOnHand.toInteger()
        command.otherLocation = item.binLocation?.parentLocation
        command.otherBinLocation = stagingLocation
        command.transferOut = Boolean.TRUE
        command.disableRefresh = Boolean.TRUE
        transfer(task, command)
    }

    void transfer(PickTask task, TransferStockCommand command) {
        task.discard()

        Transaction transaction = inventoryService.transferStock(command)
        transaction.transactionSource = createPickTaskTransactionSource(task)

        grailsApplication.mainContext.publishEvent(new PickTaskUpdateEvent(task))
    }


    TransactionSource createPickTaskTransactionSource(PickTask pickTask) {
        PicklistItem picklistItem = PicklistItem.get(pickTask.id)
        if (!picklistItem) {
            throw new ValidationException("PicklistItem not found for PickTask id: ${pickTask.id}", null)
        }

        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.PICK_TASK,
                picklist: picklistItem.picklist,
                origin: pickTask.facility
        )

        if (!transactionSource.validate()) {
            throw new ValidationException("Invalid transaction source", transactionSource.errors)
        }

        return transactionSource.save()
    }

    /**
     * Rollback the pick task from any state before ISSUED to pending state.
     *
     * @param requisition Requisition
     * @return boolean
     */
    boolean rollbackPickTasks(Requisition requisition) {
        if (requisition.status >= RequisitionStatus.ISSUED) {
            throw new IllegalStateException("Cannot rollback pick tasks for requisition with status: ${requisition.status}")
        }

        if (requisition.picklist) {
            // First delete local transfers and transaction sources
            List<TransactionSource> transactionSources = TransactionSource.findAllByPicklist(requisition.picklist)
            List<Transaction> transactions =  transactionSources?.associatedTransactions?.flatten()
            transactions?.each {
                inventoryService.deleteLocalTransfer(it)
            }
            transactionSources?.each {
                it.delete()
            }

            // Then delete shipment items associated with the requisition (if any)
            requisition.requisitionItems?.each { RequisitionItem requisitionItem ->
                stockMovementService.removeShipmentItemsForModifiedRequisitionItem(requisitionItem)
            }

            // Once we are done with deleting transactions and shipment items, we can clear the picklist
            // (delete picklist items)
            picklistService.clearPicklist(requisition.picklist.id)
        }

        // Finally set requisition status to VERIFYING
        requisition.status = RequisitionStatus.VERIFYING
        requisition.save()

        return true
    }

    private List<String> findRequisitionIdsForPicking(SearchPickTaskCommand command) {
        Integer ordersCount = command.ordersCount
        DeliveryTypeCode deliveryTypeCode = command.deliveryTypeCode
        if (ordersCount) {
            boolean searchForAllOrders = deliveryTypeCode == null

            def criteria = Requisition.createCriteria()
            List<Requisition> allCandidates = criteria.list() {
                eq("origin", command.facility)
                eq("status", RequisitionStatus.PICKING)

                if (!searchForAllOrders) {
                    eq("deliveryTypeCode", deliveryTypeCode)
                    order("dateCreated", "asc")
                }
            }

            if (!allCandidates) {
                return []
            }

            List<Requisition> selectedRequisitions
            if (searchForAllOrders) {
                def sortedCandidates = allCandidates.sort { a, b ->
                    Integer p1 = a.deliveryTypeCode?.priority ?: 99
                    Integer p2 = b.deliveryTypeCode?.priority ?: 99

                    return p1 <=> p2 ?: a.dateCreated <=> b.dateCreated
                }
                selectedRequisitions = sortedCandidates.take(ordersCount)
            } else {
                selectedRequisitions = allCandidates.take(ordersCount)
            }

            return selectedRequisitions.collect { it.id }
        }

        return []
    }

    private void validateOutboundContainer(Location outboundContainer, PickTask pickTask) {
        if (!outboundContainer) {
            throw new IllegalArgumentException("Outbound container does not exist")
        }

        if (!outboundContainer.supports(ActivityCode.OUTBOUND_CONTAINER)) {
            throw new IllegalArgumentException("Container ${outboundContainer.name} does not support ${ActivityCode.OUTBOUND_CONTAINER} activity")
        }

        if (!outboundContainer.supports(ActivityCode.HOLD_STOCK)) {
            throw new IllegalArgumentException("Container ${outboundContainer.name} does not support ${ActivityCode.HOLD_STOCK} activity")
        }

        ActivityCode taskDeliveryTypeActivity = pickTask.getDeliveryTypeCode().getActivityCode()
        if (!outboundContainer.supports(taskDeliveryTypeActivity)) {
            throw new IllegalArgumentException("Container ${outboundContainer.name} does not support ${taskDeliveryTypeActivity} activity")
        }
    }

    private void validateStagingLocation(Location stagingLocation) {
        if (!stagingLocation) {
            throw new IllegalArgumentException("Staging location does not exist")
        }

        if (!stagingLocation.supports(ActivityCode.STAGING_LOCATION)) {
            throw new IllegalArgumentException("Staging location ${stagingLocation.name} does not support ${ActivityCode.STAGING_LOCATION} activity")
        }

        if (!stagingLocation.supports(ActivityCode.HOLD_STOCK)) {
            throw new IllegalArgumentException("Staging location ${stagingLocation.name} does not support ${ActivityCode.HOLD_STOCK} activity")
        }
    }
}
