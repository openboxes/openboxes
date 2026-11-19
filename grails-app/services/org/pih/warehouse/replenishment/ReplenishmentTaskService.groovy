/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.replenishment

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.api.replenishment.SearchReplenishmentTaskCommand
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.RefreshProductAvailabilityEvent
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionAction
import org.pih.warehouse.inventory.TransactionSource
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.picking.StagingLocationNotFoundException
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.picklist.PicklistService

/**
 * Picking and staging of replenishment lists (transfer orders with destination bins) on the mobile app, modelled
 * on PickTaskService: a task is picked from its bin into an outbound container, and a container is dropped at a
 * staging location. Moving the stock into the destination bins (putaway) is not part of it.
 */
@Transactional
class ReplenishmentTaskService {

    static final List<PickTaskStatus> OPEN_STATUSES = [PickTaskStatus.PENDING, PickTaskStatus.PICKING]

    GrailsApplication grailsApplication
    InventoryService inventoryService
    PicklistService picklistService
    UserService userService

    @Transactional(readOnly = true)
    List<ReplenishmentTask> search(SearchReplenishmentTaskCommand command, Map params = [:]) {
        log.info "Searching replenishment tasks for facility=${command.facility?.name}, orderId=${command.orderId}"

        Integer max = params?.max ? params.max as Integer : 100
        Integer offset = params?.offset ? params.offset as Integer : 0

        List<PickTaskStatus> statusesToSearch = command.status
        if (!statusesToSearch && !command.outboundContainerId && !command.orderId) {
            statusesToSearch = OPEN_STATUSES
        }

        return ReplenishmentTask.createCriteria().list(max: max, offset: offset) {
            eq("facility", command.facility)

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

            createAlias("location", "l", CriteriaSpecification.LEFT_JOIN)
            order("l.name", "asc")
            order("dateCreated", "asc")
        } as List<ReplenishmentTask>
    }

    /**
     * The replenishment lists of the facility still to work on, oldest first: with tasks to pick (status PENDING
     * when none is started yet, otherwise PICKING), or with all tasks picked and picks in a container still to stage
     * (PICKED). A list with neither is staged and ready for putaway, and is left out.
     *
     * @param statuses the list statuses to return; all of them when empty
     */
    @Transactional(readOnly = true)
    List<Map> listReplenishments(Location facility, List<String> statuses = []) {
        List<Object[]> rows = ReplenishmentTask.executeQuery("""
            select t.order.id, t.orderNumber, t.dateRequested, t.status, oc.id, t.quantityPicked
            from ReplenishmentTask t
            left join t.outboundContainer oc
            where t.facility = :facility
        """, [facility: facility]) as List<Object[]>

        List<Map> lists = rows.groupBy { it[0] }.collect { orderId, List<Object[]> tasks ->
            List<PickTaskStatus> taskStatuses = tasks.collect { it[3] as PickTaskStatus }
            int openTasksCount = taskStatuses.count { it in OPEN_STATUSES } as int
            boolean hasPicksToStage = tasks.any { Object[] task ->
                task[3] == PickTaskStatus.PICKED && task[4] && (task[5] ?: 0) > 0
            }
            String status = null
            if (openTasksCount > 0) {
                boolean started = taskStatuses.any { it != PickTaskStatus.PENDING }
                status = started ? PickTaskStatus.PICKING.name() : PickTaskStatus.PENDING.name()
            } else if (hasPicksToStage) {
                status = PickTaskStatus.PICKED.name()
            }
            return [id: orderId, orderNumber: tasks[0][1], status: status, dateCreated: tasks[0][2],
                    tasksCount: openTasksCount]
        }

        return lists.findAll { Map list -> list.status && (!statuses || list.status in statuses) }
                .sort { it.dateCreated }
    }

    @Transactional(readOnly = true)
    ReplenishmentTask get(String id) {
        return id ? ReplenishmentTask.get(id) : null
    }

    /**
     * The replenishment task, if it belongs to the facility (the facility in the request URL); null otherwise.
     */
    @Transactional(readOnly = true)
    ReplenishmentTask get(String id, Location facility) {
        ReplenishmentTask task = get(id)
        return task && facility && task.facility?.id == facility.id ? task : null
    }

    /**
     * Applies an action to a replenishment task: start, pick or short-pick. The signed-in user is recorded as the
     * picker; the assignee is the signed-in user unless assigneeId names someone else, which needs the admin role at
     * the task's facility.
     */
    ReplenishmentTask patch(String id, Map data = [:], Location facility = null) {
        ReplenishmentTask task = facility ? get(id, facility) : get(id)
        if (!task) {
            throw new ObjectNotFoundException(id, "Replenishment task ${id} was not found")
        }
        // A row of a view: detached, so its status changes are never flushed to the view (changes go to the picklist item)
        task.discard()

        String currentUserId = AuthService.currentUser?.id

        switch (data?.action) {
            case 'start':
                start(task, assigneeFor(task, data.assigneeId as String))
                break

            case 'pick':
                pick(task, data.outboundContainerId as String, currentUserId)
                break

            case 'short-pick':
                shortPick(task, data.outboundContainerId as String, data.quantityPicked as Integer,
                        currentUserId, reasonCodeOf(data.reasonCode))
                break

            default:
                throw new UnsupportedOperationException("Unsupported action: ${data?.action}")
        }

        return get(task.id)
    }

    /**
     * The reason code given as a name ("DAMAGED") or as a reason code object ({"name": "DAMAGED"}).
     */
    private static String reasonCodeOf(Object reasonCode) {
        if (reasonCode instanceof Map) {
            return (reasonCode as Map).name as String ?: null
        }
        return reasonCode ? reasonCode.toString() : null
    }

    private String assigneeFor(ReplenishmentTask task, String requestedAssigneeId) {
        User currentUser = AuthService.currentUser
        if (!requestedAssigneeId || requestedAssigneeId == currentUser?.id) {
            return currentUser?.id
        }
        String message = null
        if (!userService.isUserInRoleAtLocation(currentUser, task.facility, RoleType.ROLE_ADMIN)) {
            message = "Only an administrator can assign replenishment task ${task.id} to another user"
        } else if (!User.get(requestedAssigneeId)) {
            message = "User ${requestedAssigneeId} was not found"
        }
        if (message) {
            task.errors.reject("assignee", message)
            throw new ValidationException(message, task.errors)
        }
        return requestedAssigneeId
    }

    void start(ReplenishmentTask task, String assigneeId) {
        if (task.assignee && task.assignee.id != assigneeId) {
            String message = "Replenishment task ${task.id} is already assigned to another user"
            task.errors.reject("assignee", message)
            throw new ValidationException(message, task.errors)
        }

        executeStateTransition(task, PickTaskStatus.PICKING)

        PicklistItem picklistItem = PicklistItem.get(task.id)
        picklistItem.assignee = Person.get(assigneeId)
        picklistItem.dateAssigned = new Date()
        picklistItem.dateStarted = new Date()

        save(task)
    }

    void pick(ReplenishmentTask task, String outboundContainerId, String pickedById) {
        Location outboundContainer = findInFacility(outboundContainerId, task.facility)
        validateOutboundContainer(outboundContainer)

        Integer quantity = (task.quantityRequired - (task.quantityPicked ?: 0)).toInteger()
        transferToContainer(task, outboundContainer, quantity)
        executeStateTransition(task, PickTaskStatus.PICKED)

        PicklistItem picklistItem = PicklistItem.get(task.id)
        picklistItem.pickedBy = Person.get(pickedById)
        picklistItem.datePicked = new Date()
        picklistItem.outboundContainer = outboundContainer
        picklistItem.quantityPicked = task.quantityRequired.toInteger()

        save(task)
    }

    /**
     * Picks less than required. With a reason code the task ends: PICKED when anything was picked, otherwise STAGED
     * (nothing to stage). Without one, the task stays open for the rest.
     */
    void shortPick(ReplenishmentTask task, String outboundContainerId, Integer quantityPicked, String pickedById,
                   String reasonCode) {
        if (quantityPicked == null || quantityPicked < 0) {
            throw new IllegalArgumentException("Quantity picked must be zero or more")
        }

        Location outboundContainer = null
        if (quantityPicked > 0) {
            outboundContainer = findInFacility(outboundContainerId, task.facility)
            validateOutboundContainer(outboundContainer)
        }

        PicklistItem picklistItem = PicklistItem.get(task.id)
        if (quantityPicked > 0) {
            // Records the pick first: it refuses more than the quantity remaining, before any stock moves
            picklistService.updatePicklistItem(picklistItem.id, task.product?.id, quantityPicked.toBigDecimal(),
                    pickedById, reasonCode)
            transferToContainer(task, outboundContainer, quantityPicked)
            picklistItem.outboundContainer = outboundContainer
        } else if (reasonCode) {
            picklistItem.reasonCode = reasonCode
            picklistItem.pickedBy = Person.get(pickedById)
            picklistItem.datePicked = new Date()
        }

        boolean anythingPicked = (picklistItem.quantityPicked ?: 0) > 0
        boolean isFullyPicked = picklistItem.quantityPicked == task.quantityRequired.toInteger()
        if (quantityPicked == 0 && reasonCode && !anythingPicked) {
            // STAGED is the terminal status of a task that was never picked: there is nothing to stage
            executeStateTransition(task, PickTaskStatus.STAGED)
        } else if (reasonCode || isFullyPicked) {
            executeStateTransition(task, PickTaskStatus.PICKED)
        }

        save(task)
    }

    /**
     * Applies an action to an outbound container of the facility. The signed-in user is recorded as the user who
     * staged it. Returns the tasks staged.
     */
    List<ReplenishmentTask> drop(Location outboundContainer, Map data = [:]) {
        switch (data?.action) {
            case 'drop':
                return dropToStaging(outboundContainer, data.stagingLocationId as String, AuthService.currentUser?.id)

            default:
                throw new UnsupportedOperationException("Unsupported action: ${data?.action}")
        }
    }

    /**
     * Moves the picks in the container to the staging location and marks their tasks STAGED. Everything is checked
     * before any stock moves, so a refused drop changes nothing.
     */
    List<ReplenishmentTask> dropToStaging(Location outboundContainer, String stagingLocationId, String stagedById) {
        if (!stagingLocationId?.trim()) {
            throw new IllegalArgumentException("Staging location is required")
        }
        Location stagingLocation = findInFacility(stagingLocationId, outboundContainer.parentLocation)
        if (!stagingLocation) {
            throw new StagingLocationNotFoundException(stagingLocationId)
        }
        validateStagingLocation(stagingLocation)

        List<ReplenishmentTask> tasks = ReplenishmentTask.findAllByOutboundContainerAndStatus(
                outboundContainer, PickTaskStatus.PICKED).findAll { (it.quantityPicked ?: 0) > 0 }
        if (!tasks) {
            throw new IllegalStateException("Outbound container with number ${outboundContainer.locationNumber} is empty")
        }

        Person stagedBy = Person.get(stagedById)
        List<String> taskIds = tasks*.id
        tasks*.discard()
        tasks.each { ReplenishmentTask task ->
            // Each task moves its own picked quantity, so tasks sharing a lot in one container move it once overall
            transferToStaging(task, outboundContainer, stagingLocation)
            executeStateTransition(task, PickTaskStatus.STAGED)

            PicklistItem picklistItem = PicklistItem.get(task.id)
            picklistItem.stagingLocation = stagingLocation
            picklistItem.dateStaged = new Date()
            picklistItem.stagedBy = stagedBy

            save(task)
        }
        return taskIds.collect { get(it) }
    }

    void save(ReplenishmentTask task) {
        PicklistItem picklistItem = PicklistItem.get(task.id)
        picklistItem.status = task.status.toString()
        picklistItem.save(failOnError: true, flush: true)
        task.discard()
    }

    void executeStateTransition(ReplenishmentTask task, PickTaskStatus to) {
        PickTaskStatus from = task.status ?: PickTaskStatus.PENDING
        if (!PickTaskStatus.validateTransition(from, to)) {
            throw new IllegalStateException("Invalid transition ${from} -> ${to}")
        }
        task.status = to
    }

    void transferToContainer(ReplenishmentTask task, Location container, Integer quantity) {
        transfer(task, task.location, container, quantity)
    }

    void transferToStaging(ReplenishmentTask task, Location container, Location stagingLocation) {
        transfer(task, container, stagingLocation, task.quantityPicked.toInteger())
    }

    /**
     * Moves the task's lot between two bins of its facility, when the facility tracks internal transactions.
     */
    private void transfer(ReplenishmentTask task, Location fromBin, Location toBin, Integer quantity) {
        if (!task.facility.supports(ActivityCode.TRACK_INTERNAL_TRANSACTIONS)) {
            log.warn("Skipping transfer of replenishment task ${task.identifier}: facility does not support " +
                    "${ActivityCode.TRACK_INTERNAL_TRANSACTIONS} activity")
            return
        }

        TransferStockCommand command = new TransferStockCommand()
        command.location = task.facility
        command.binLocation = fromBin
        command.inventoryItem = task.inventoryItem
        command.quantity = quantity
        command.otherLocation = task.facility
        command.otherBinLocation = toBin
        command.transferOut = Boolean.TRUE
        command.disableRefresh = Boolean.TRUE

        Transaction transaction = inventoryService.transferStock(command)
        if (transaction.hasErrors()) {
            String errorMessage = transaction.errors.allErrors.collect { it.defaultMessage ?: it.code }.join('; ')
            throw new ValidationException("Unable to transfer stock for replenishment task ${task.identifier} " +
                    "(order ${task.orderNumber}): ${errorMessage}", transaction.errors)
        }
        transaction.transactionSource = createTransactionSource(task)

        grailsApplication.mainContext.publishEvent(new RefreshProductAvailabilityEvent(
                task.facility, [task.product?.id], false))
    }

    private TransactionSource createTransactionSource(ReplenishmentTask task) {
        PicklistItem picklistItem = PicklistItem.get(task.id)
        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.PICK_TASK,
                picklist: picklistItem.picklist,
                origin: task.facility
        )
        if (!transactionSource.validate()) {
            throw new ValidationException("Invalid transaction source", transactionSource.errors)
        }
        return transactionSource.save()
    }

    Location findOutboundContainer(String outboundContainerId, Location facility) {
        return findInFacility(outboundContainerId, facility)
    }

    /**
     * The internal location of the facility with this id or location number, or null.
     */
    private Location findInFacility(String idOrNumber, Location facility) {
        if (!idOrNumber || !facility) {
            return null
        }
        Location location = Location.get(idOrNumber)
        if (location?.parentLocation?.id == facility.id) {
            return location
        }
        return Location.findByLocationNumberAndParentLocation(idOrNumber, facility)
    }

    private void validateOutboundContainer(Location outboundContainer) {
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
        if (!stagingLocation.supports(ActivityCode.STAGING_LOCATION)) {
            throw new IllegalArgumentException("Staging location ${stagingLocation.name} does not support ${ActivityCode.STAGING_LOCATION} activity")
        }
        if (!stagingLocation.supports(ActivityCode.HOLD_STOCK)) {
            throw new IllegalArgumentException("Staging location ${stagingLocation.name} does not support ${ActivityCode.HOLD_STOCK} activity")
        }
    }
}
