package org.pih.warehouse.replenishment

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionAction
import org.pih.warehouse.inventory.TransactionSource
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.PicklistItem

@Transactional
class ReplenishmentTaskService {

    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService
//    StockMovementService stockMovementService

    @Transactional(readOnly = true)
    List<ReplenishmentTask> search(SearchReplenishmentTaskCommand command, Map params = [:]) {
        log.info "Searching replenishment tasks for facility=${command.facility?.name}, orderId=${command.orderId}"

        Integer max = params.int('max') ?: 100
        Integer offset = params.int('offset') ?: 0

        List<PickTaskStatus> statusesToSearch = command.status
        if (!statusesToSearch && !command.outboundContainerId && !command.orderId) {
            statusesToSearch = [PickTaskStatus.PENDING, PickTaskStatus.PICKING]
        }

        List<ReplenishmentTask> tasks = ReplenishmentTask.createCriteria().list(max: max, offset: offset) {
            if (command.facility) {
                eq("facility", command.facility)
            }

            if (command.orderId) {
                createAlias("order", "o")

                or {
                    eq("o.id", command.orderId)
                    eq("o.orderNumber", command.orderId)
                }
            }

            if (command.assigneeId) {
                eq("assignee.id", command.assigneeId)
            }

            if (statusesToSearch) {
                'in'("status", statusesToSearch)
            }

            if (command.outboundContainerId) {
                createAlias("outboundContainer", "oc")

                or {
                    eq("oc.id", command.outboundContainerId)
                    eq("oc.locationNumber", command.outboundContainerId)
                }
            }

            order("dateCreated", "asc")
            createAlias("location", "l")
            order("l.name", "asc")
        }

        return tasks
    }

    @Transactional(readOnly = true)
    ReplenishmentTask get(String id) {
        if (!id) {
            return null
        }

        return ReplenishmentTask.get(id)
    }

    ReplenishmentTask patch(String id, Map data = [:]) {
        ReplenishmentTask task = get(id)

        if (!task) {
            throw new ObjectNotFoundException(id, "Replenishment task ${id} was not found")
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

    void start(ReplenishmentTask task, String assigneeId) {
        executeStateTransition(task, PickTaskStatus.PICKING)

        PicklistItem existingPickItem = PicklistItem.get(task.id)
        existingPickItem.assignee = Person.get(assigneeId)
        existingPickItem.dateAssigned = new Date()
        existingPickItem.dateStarted = new Date()

        save(task)
    }

    void pick(ReplenishmentTask task, String outboundContainerId, String pickedById) {
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

    void shortPick(ReplenishmentTask task, String outboundContainerId, Integer quantityPicked, String pickedById, String reasonCode) {
        Location outboundContainer = null
        if (outboundContainerId != null) {
            outboundContainer = Location.findByLocationNumberOrId(outboundContainerId, outboundContainerId)
            validateOutboundContainer(outboundContainer, task)
        }

        PicklistItem existingPickItem = PicklistItem.get(task.id)
        if (quantityPicked > 0) {
            transferToContainer(task, outboundContainer, quantityPicked)
            existingPickItem.outboundContainer = outboundContainer
        }

        def isFullyPicked = task.quantityRequired.toInteger() == existingPickItem.quantityPicked

        if (quantityPicked == 0 && reasonCode) {
            existingPickItem.reasonCode = reasonCode
            if (!existingPickItem.quantityPicked || existingPickItem.quantityPicked == 0) {
                executeStateTransition(task, PickTaskStatus.STAGED)
            } else {
                executeStateTransition(task, PickTaskStatus.PICKED)
            }
        } else if (reasonCode || isFullyPicked) {
            executeStateTransition(task, PickTaskStatus.PICKED)
        }

        save(task)
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

    void save(ReplenishmentTask task) {
        PicklistItem existingPickItem = PicklistItem.get(task.id)
        existingPickItem.status = task.status.toString()
        existingPickItem.save(failOnError: true)
        task.discard()
    }

    void executeStateTransition(ReplenishmentTask task, PickTaskStatus to, boolean validate = true) {
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
            List<ReplenishmentTask> tasks = ReplenishmentTask.findAllByOutboundContainerAndInventoryItemAndStatus(
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

    void transferToContainer(ReplenishmentTask task, Location container, Integer quantity) {
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

    void transferToStaging(ReplenishmentTask task, AvailableItem item, Location stagingLocation) {
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

    void transfer(ReplenishmentTask task, TransferStockCommand command) {
        task.discard()

        Transaction transaction = inventoryService.transferStock(command)
        transaction.transactionSource = createReplenishmentTaskTransactionSource(task)

        inventoryService.saveTransaction(transaction)
    }


    TransactionSource createReplenishmentTaskTransactionSource(ReplenishmentTask replenishmentTask) {
        PicklistItem picklistItem = PicklistItem.get(replenishmentTask.id)
        if (!picklistItem) {
            throw new ValidationException("PicklistItem not found for ReplenishmentTask id: ${replenishmentTask.id}", null)
        }

        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.PICK_TASK,
                picklist: picklistItem.picklist,
                origin: replenishmentTask.facility
        )

        if (!transactionSource.validate()) {
            throw new ValidationException("Invalid transaction source", transactionSource.errors)
        }

        return transactionSource.save()
    }

    private void validateOutboundContainer(Location outboundContainer, ReplenishmentTask replenishmentTask) {
        if (!outboundContainer) {
            throw new IllegalArgumentException("Outbound container does not exist")
        }

        if (!outboundContainer.supports(ActivityCode.OUTBOUND_CONTAINER)) {
            throw new IllegalArgumentException("Container ${outboundContainer.name} does not support ${ActivityCode.OUTBOUND_CONTAINER} activity")
        }

        if (!outboundContainer.supports(ActivityCode.HOLD_STOCK)) {
            throw new IllegalArgumentException("Container ${outboundContainer.name} does not support ${ActivityCode.HOLD_STOCK} activity")
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
