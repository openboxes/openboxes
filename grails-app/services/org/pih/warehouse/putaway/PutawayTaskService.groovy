package org.pih.warehouse.putaway

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.api.PutawayTaskAdapter
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.api.putaway.SearchPutawayTaskCommand
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.PutawayTypeCode
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.putaway.discrepancy.PutawayDiscrepancyEvent
import org.pih.warehouse.shipping.Shipment

@Transactional
class PutawayTaskService {

    GrailsApplication grailsApplication
    def inboundSortationService
    InventoryService inventoryService
    PutawayService putawayService
    UserService userService
    def sessionFactory


    @Transactional(readOnly = true)
    List search(SearchPutawayTaskCommand command, Map params = [:]) {
        log.info "Searching putaway tasks with command=${command?.properties} and params=${params}"

        Integer max = Math.min((params.int('max') ?: 50), 100) as Integer
        Integer offset = params.int('offset') ?: 0 as Integer
        String sort = params.sort ?: 'dateCreated'
        String sortOrder = (params.order ?: 'desc').toLowerCase() in ['asc', 'desc'] ? params.order : 'desc' as Integer

        // Get user-provided statuses
        List<PutawayTaskStatus> statuses = params.list("status").collect { it as PutawayTaskStatus }

        // Resolve the status category to a set of statuses
        List<PutawayTaskStatus> statusesByStatusCategory = PutawayTaskStatus.toSet(command.statusCategory)

        // If both are provided, intersect (AND); otherwise use whichever is available
        if (statuses && statusesByStatusCategory) {
            statuses = statuses.intersect(statusesByStatusCategory)
        } else if (statusesByStatusCategory) {
            statuses = statusesByStatusCategory
        }

        Date createdAfter = params.createdAfter ? Date.parse("MM/dd/yyyy", params.createdAfter) : null
        Date createdBefore = params.createdBefore ? Date.parse("MM/dd/yyyy", params.createdBefore) : null

        // Search for putaway tasks based on user-provided search parameters
        List<PutawayTask> tasks = PutawayTask.createCriteria().list(max: max, offset: offset, sort: sort, order: sortOrder) {
            if (statuses) {
                'in'('status', statuses)
            }
            if (command.product) {
                eq('product', command.product)
            }
            if (command.searchTerm) {
                or {
                    ilike('identifier', "%${command.searchTerm}%")
                    putawayOrder {
                        ilike('orderNumber', "%${command.searchTerm}%")
                    }
                    product {
                        or {
                            ilike('productCode', "${command.searchTerm}%")
                            ilike('name', "%${command.searchTerm}%")
                            ilike('description', "%${command.searchTerm}%")
                        }
                    }
                }
            }
            if (command.facility) {
                eq('facility', command.facility)
            }
            if (command.container) {
                eq('container', command.container)
            }
            if (command.destination) {
                eq('destination', command.destination)
            }
            if (command.order) {
                eq('putawayOrder', command.order)
            }
            if (createdAfter) {
                ge('dateCreated', createdAfter)
            }
            if (createdBefore) {
                le('dateCreated', createdBefore)
            }
        }

        return tasks
    }

    @Transactional(readOnly = true)
    Integer count(SearchPutawayTaskCommand command, Map params = [:]) {
        log.info "Counting putaway tasks with command=${command?.properties} and params=${params}"

        List<PutawayTaskStatus> statuses = params.list("status").collect { it as PutawayTaskStatus }

        List<PutawayTaskStatus> statusesByStatusCategory = PutawayTaskStatus.toSet(command.statusCategory)
        statuses += statusesByStatusCategory

        Integer count = PutawayTask.where {
            if (statuses) {
                status in statuses
            }
            if (command.product) {
                product == command.product
            }
            if (command.facility) {
                facility == command.facility
            }
            if (command.container) {
                container == command.container
            }
            if (command.order) {
                putawayOrder == command.order
            }

        }.count()

        return count
    }

    PutawayTask get(String id) {
        if (!id) return null
        return PutawayTask.get(id)
    }

    PutawayTask read(String id) {
        if (!id) return null
        return PutawayTask.read(id)
    }

    /**
     * The putaway task, if it belongs to the facility (the facility in the request URL); null otherwise.
     */
    PutawayTask read(String id, Location facility) {
        PutawayTask task = read(id)
        return task && facility && task.facility?.id == facility.id ? task : null
    }

    /**
     * Action router for state transitions and side-effects (transaction).
     * Supported actions include: start, scanToContainer, complete, cancel, rollback
     * When a facility is given (the facility in the request URL), a task of another facility is not found.
     * The signed-in user is recorded as the user who completed the task.
     * FIXME - this will eventually be replaced with a Spring Statemachine implementation.
     */
    PutawayTask patch(String id, Map data = [:], Location facility = null) {
        PutawayTask task = get(id)
        if (task && facility && task.facility?.id != facility.id) {
            task.discard()
            task = null
        }

        if (!task) {
            throw new ObjectNotFoundException(id, "Putaway task ${id} was not found")
        }

        switch (data?.action) {

            case 'noop':
                // So we can compare the putaway task and putaway order to make sure status and properties are the same
                break;

            case 'assign':
                assign(task, data.container as String, data.assignee as String, data.override as Boolean)
                break;

            case 'start':
                start(task)
                break

            case 'load':
                task = load(task, data.quantity as BigDecimal, data.container as String, data.override as Boolean)
                break

            case 'complete':
                complete(task, data.destination as String, AuthService.currentUser?.id, data.isCancelRemaining as Boolean,
                        data.reasonCode as ReasonCode, data.isDirectPutaway as Boolean)
                break

            case 'partialComplete':
                task = partialComplete(task, data.quantity as BigDecimal, data.destination as String,
                        data.reasonCode as ReasonCode, data.isDirectPutaway as Boolean)
                break

            case 'rollback':
                rollback(task)
                break

            case 'cancel':
                //executeStatusChange(task, PutawayTaskStatus.CANCELED)
                //putawayService.savePutaway(task.toPutaway())
                break

            case 'rerunStrategy':
                // A pending task split off by a partial putaway may still have its stock sitting in the putaway
                // container, so move it back to the origin before the strategy reassigns the container.
                Integer quantityInContainer = getQuantityRemainingInContainer(task)
                if (quantityInContainer > 0) {
                    transferFromContainerToOrigin(task, quantityInContainer)
                    task = PutawayTask.get(task.id)
                }
                inboundSortationService.rerunPutawayStrategy(task)
                task = PutawayTask.get(task.id)  // refresh from view
                break

            default:
                throw new UnsupportedOperationException("Unsupported action: ${data?.action}")
        }

        // FIXME The putaway task is a view-backed domain, but Grails doesn't know that so any changes to the
        //  task triggers a persistence event. I need to figure out how to make it a read-only domain class.
        if (task) {
            task.discard()
        }

        // Eventually, we want to save any changes made to the putaway task as a putaway.
        // FIXME  For now the PutawayTask will be a wrapper around Putaway. However, we may want to use just the
        //  Putaway object instead of creating a new PutawayTask. I had forgotten about the Putaway abstraction.
        return task
    }

    void save(PutawayTask task) {
        OrderItem existingOrderItem = OrderItem.get(task.putawayOrderItem.id)
        OrderItem orderItem = PutawayTaskAdapter.toOrderItem(task, existingOrderItem)
        log.info "dirty: " + orderItem.dirtyPropertyNames
        log.info "dirty: " + orderItem.order.dirtyPropertyNames

        Order order = orderItem.order
        // start - putaway task has been assigned and has been started
        order.approvedBy = task.assignee
        order.dateApproved = task.dateStarted

        if (task.status == PutawayTaskStatus.COMPLETED) {
            def allSplitTasks = order.orderItems.findAll {
                it.parentOrderItem != null &&
                it.orderItemStatusCode != OrderItemStatusCode.CANCELED
            }

            boolean allItemsCompleted = allSplitTasks.every {
                it.orderItemStatusCode == OrderItemStatusCode.COMPLETED
            }

            // complete - task is fully completed
            if (allItemsCompleted) {
                order.status = OrderStatus.COMPLETED
                order.completedBy = task.completedBy
                order.dateCompleted = task.dateCompleted
            }
        } else {
            order.status = PutawayTaskAdapter.toOrderStatus(task.status)
        }

        orderItem.save(cascade: true, failOnError:true)
        task.discard()
    }


    /**
     * Assign task to a putaway container (if one hasn't already been assigned) and, optionally, to a user.
     * Assigning the task to a user other than the signed-in user needs the admin role at the task's facility.
     *
     * @param task
     * @param containerId
     * @param assigneeId
     */
    void assign(PutawayTask task, String containerId, String assigneeId, Boolean override = false) {
        if (task.status != PutawayTaskStatus.PENDING) {
            throw new IllegalStateException("Putaway container can only be assigned when the task is PENDING (current status: ${task.status}).")
        }
        Location container = containerId ? Location.get(containerId) : null
        if (containerId && !container) {
            throw new IllegalArgumentException("Putaway container ${containerId} was not found")
        }
        if (container && (task.facility != container.parentLocation || !container.internalLocation)) {
            throw new IllegalArgumentException("Putaway container ${container?.name} must be an internal location within facility ${task?.facility?.name}")
        }

        User currentUser = AuthService.currentUser
        if (assigneeId && assigneeId != currentUser?.id &&
                !userService.isUserInRoleAtLocation(currentUser, task.facility, RoleType.ROLE_ADMIN)) {
            String message = "Only an administrator can assign putaway task ${task.identifier} to another user"
            task.errors.reject("assignee", message)
            throw new ValidationException(message, task.errors)
        }
        Person assignee = assigneeId ? Person.get(assigneeId) : null
        if (assigneeId && !assignee) {
            String message = "User ${assigneeId} was not found"
            task.errors.reject("assignee", message)
            throw new ValidationException(message, task.errors)
        }

        if (container && task.container && container != task.container && !override) {
            throw new IllegalArgumentException("Putaway container is already assigned: ${task?.container?.name}. Must override to re-assign it to putaway constainer ${container?.name}")
        }
        if (container && task.container == container) {
            log.warn "Putaway container for task ${task.identifier} has already been set to ${container}"
        }

        if (container) task.container = container

        // Set the assignee if one is provided
        if (assignee) task.assignee = assignee
        save(task)
    }

    /**
     * Start a putaway task.
     *
     * @param task
     */
    void start(PutawayTask task) {
        task.dateStarted = new Date()
        task.assignee = AuthService.currentUser
        executeStateTransition(task, PutawayTaskStatus.STARTED)
        save(task)
    }

    /**
     * Load the putaway item into the assigned putaway container. Also, allows you to force the item into
     * a different putaway container (i.e. as long as it's not the destination location).
     *
     * If the quantity being loaded is less than the task's full quantity, the order item is split the same
     * way {@link #partialComplete} splits it: the loaded quantity becomes its own task moving into the
     * container, and the un-sorted remainder is left behind as a new PENDING task at the origin location.
     *
     * @param task
     * @param containerNumberOrId
     * @param override
     * @return the putaway task representing the quantity that was actually loaded into the container
     */
    PutawayTask load(PutawayTask task, BigDecimal quantity, String containerNumberOrId, Boolean override = false) {
        log.info "Loading item into putaway container ${containerNumberOrId}"

        if (quantity > task.quantity) {
            throw new IllegalArgumentException("Quantity provided is more than requested. Please re-enter quantity")
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity provided must be greater than 0")
        }

        // Validate the container location exists
        Location container = containerNumberOrId ? Location.findByLocationNumberOrId(containerNumberOrId, containerNumberOrId) : null
        if (container && container.parentLocation?.id != task.facility?.id) {
            container = Location.findByLocationNumberAndParentLocation(containerNumberOrId, task.facility)
        }
        if (!container) {
            throw new IllegalStateException("Container does not exist")
        }

        if (!container.supports(ActivityCode.PUTAWAY_CART)) {
            throw new IllegalArgumentException("Container ${container?.name} does not support ${ActivityCode.PUTAWAY_CART} activity")
        }

        // validate that the container matches the task putaway container
        if (container != task.container) {
            if (!override) {
                throw new IllegalStateException("Provided container does not match expected putaway container")
            }

            // Cannot load the item into the destination
            if (container == task.destination) {
                throw new IllegalStateException("Cannot load into this destination")
            }

            // otherwise, allow the container to change if the user has requested a force
            task.container = container
        }

        // Split off the unsorted remainder (if any) into its own PENDING task before loading the rest,
        // instead of just overwriting task.quantity and losing track of what wasn't actually sorted.
        if (quantity < task.quantity) {
            task = splitTaskForLoad(task, quantity)
        } else {
            task.quantity = quantity
        }

        // Execute state transition
        executeStateTransition(task, PutawayTaskStatus.IN_PROGRESS)

        // Transfer stock from origin to putaway container
        // FIXME can't figure out the best way to make this
        transferToContainer(task)

        save(task)

        return task
    }

    /**
     * Splits the task's order item into the piece being loaded into the container (quantity) and a PENDING
     * remainder left behind, unsorted, at the origin location. Mirrors the split performed by
     * {@link #partialComplete}, except neither piece is COMPLETED here since loading into a container is
     * not the end of the putaway.
     */
    private PutawayTask splitTaskForLoad(PutawayTask task, BigDecimal quantity) {
        BigDecimal quantityRemaining = task.quantity - quantity

        task.discard()

        OrderItem currentItem = OrderItem.get(task.putawayOrderItem.id)
        def currentItemParent = currentItem.parentOrderItem
        currentItem.parentOrderItem = null
        Order order = currentItem.order

        Putaway putaway = Putaway.createFromOrder(order)
        if (!putaway.orderedBy) {
            putaway.orderedBy = AuthService.currentUser
        }
        PutawayItem itemToSplit = putaway.putawayItems.find { it.id == (currentItemParent?.id ?: currentItem.id) }

        PutawayItem loadedSplitItem = createSplitPutawayItem(task, quantity, PutawayStatus.PENDING, task.destination)
        PutawayItem remainingSplitItem = createSplitPutawayItem(task, quantityRemaining, PutawayStatus.PENDING, task.destination)
        // The remainder hasn't been sorted into a container yet
        remainingSplitItem.containerLocation = null

        itemToSplit?.splitItems?.addAll([loadedSplitItem, remainingSplitItem])
        putawayService.savePutaway(putaway)

        currentItem.parentOrderItem = currentItemParent
        currentItem.orderItemStatusCode = OrderItemStatusCode.CANCELED
        currentItem.save(flush: true, failOnError: true)

        OrderItem loadedOrderItem = order.orderItems.find {
            it.parentOrderItem?.id == (currentItem.parentOrderItem?.id ?: currentItem.id) &&
                    it.quantity == quantity &&
                    it.containerLocation == task.container &&
                    it.orderItemStatusCode == OrderItemStatusCode.PENDING
        } as OrderItem

        return PutawayTaskAdapter.toPutawayTask(loadedOrderItem)
    }

    void complete(PutawayTask task, String destinationId, String completedById, Boolean isCancelRemaining,
                  ReasonCode reasonCode, Boolean isDirectPutaway) {
        log.info "complete putaway"
        if (!task) {
            throw new ObjectNotFoundException(task.id, "Unable to locate putaway task with id ${task.id}")
        }

        // validate destination or user has forced a destination change
        Location destination = Location.get(destinationId)
        if (!destination) {
            task.errors.reject("destination", "Destination is required")
        }
        validateInFacility(destination, task)

        if (task.hasErrors() || !task.validate()) {
            throw new ValidationException("Validation errors occurred during complete action", task.errors)
        }

        task.destination = destination

        Person completedBy = completedById ? Person.get(completedById) : AuthService.currentUser
        if (!completedBy) {
            task.errors.reject("completedBy", "Must provide a valid person or user who completed the putaway task")
        }

        if (isCancelRemaining) {
            def discrepancyLocation = findDiscrepancyLocation(task)
            if (!discrepancyLocation) {
                throw new IllegalStateException("No discrepancy location found")
            }
            task.destination = discrepancyLocation
        }

        task.discrepancyReasonCode = reasonCode

        task.dateCompleted = new Date()
        task.completedBy = completedBy
        executeStateTransition(task, PutawayTaskStatus.COMPLETED)

        // Transfer stock to destination (either from putaway container or destination)
        transferToDestination(task, isDirectPutaway)

        // Save the task
        save(task)

        if (isCancelRemaining) {
            sendDiscrepancyNotification(task)
        }
    }

    PutawayTask partialComplete(PutawayTask task, BigDecimal quantity, String destinationId, ReasonCode reasonCode,
                                Boolean isDirectPutaway) {
        log.info "partial complete putaway"
        if (quantity > task.quantity) {
            throw new IllegalArgumentException("Quantity provided is more than requested. Please re-enter quantity")
        }

        BigDecimal quantityRemaining = task.quantity - quantity
        if (quantityRemaining <= 0) {
            throw new IllegalArgumentException("Quantity provided is more than requested. Please re-enter quantity")
        }

        Location alternativeDestination = Location.get(destinationId)
        validateInFacility(alternativeDestination, task)
        if (!alternativeDestination) {
            def discrepancyLocation = findDiscrepancyLocation(task)
            if (!discrepancyLocation) {
                throw new IllegalStateException("No discrepancy location found")
            }
            alternativeDestination = discrepancyLocation
        }

        task.discrepancyReasonCode = reasonCode

        if (task.hasErrors() || !task.validate()) {
            throw new ValidationException("Validation errors occurred during complete action", task.errors)
        }

        task.discard()

        OrderItem currentItem = OrderItem.get(task.putawayOrderItem.id)
        def currentItemParent = currentItem.parentOrderItem
        currentItem.parentOrderItem = null
        Order order = currentItem.order

        Putaway putaway = Putaway.createFromOrder(order)
        if (!putaway.orderedBy) {
            putaway.orderedBy = AuthService.currentUser
        }
        PutawayItem itemToSplit = putaway.putawayItems.find { it.id == (currentItemParent?.id ?: currentItem.id) }

        PutawayItem completedSplitItem = createSplitPutawayItem(task, quantity, PutawayStatus.COMPLETED, alternativeDestination)
        PutawayItem remainingSplitItem = createSplitPutawayItem(task, quantityRemaining, PutawayStatus.PENDING, alternativeDestination)

        itemToSplit?.splitItems?.addAll([completedSplitItem, remainingSplitItem])
        putawayService.savePutaway(putaway)

        def taskToTransfer = PutawayTaskAdapter.toPutawayTask(completedSplitItem, order)
        transferToDestination(taskToTransfer, isDirectPutaway)
        save(task)

        currentItem.parentOrderItem = currentItemParent
        currentItem.orderItemStatusCode = OrderItemStatusCode.CANCELED
        currentItem.save(flush: true, failOnError: true)

        OrderItem remainingOrderItem = order.orderItems.find {
            it.parentOrderItem?.id == (currentItem.parentOrderItem?.id ?: currentItem.id) &&
                    it.quantity == quantityRemaining &&
                    it.orderItemStatusCode == OrderItemStatusCode.PENDING
        } as OrderItem

        return PutawayTaskAdapter.toPutawayTask(remainingOrderItem)
    }

    void transferToContainer(PutawayTask task) {
        if (!tracksInternalTransactions(task)) {
            log.warn("Skipping transfer to putaway container: Facility does not support TRACK_INTERNAL_TRANSACTIONS activity")
            return
        }

        TransferStockCommand command = new TransferStockCommand()
        command.location = task.facility
        command.binLocation = task.location
        command.inventoryItem = task.inventoryItem
        command.quantity = task.quantity.toInteger()
        command.otherLocation = task.facility
        command.otherBinLocation = task.container
        command.order = task.putawayOrder
        command.transferOut = Boolean.TRUE
        command.disableRefresh = Boolean.TRUE
        transfer(task, command)
    }

    void transferFromContainerToOrigin(PutawayTask task, Integer quantity) {
        if (!tracksInternalTransactions(task)) {
            log.warn("Skipping transfer from putaway container: Facility does not support TRACK_INTERNAL_TRANSACTIONS activity")
            return
        }

        TransferStockCommand command = new TransferStockCommand()
        command.location = task.facility
        command.binLocation = task.container
        command.inventoryItem = task.inventoryItem
        command.quantity = quantity
        command.otherLocation = task.facility
        command.otherBinLocation = task.location
        command.order = task.putawayOrder
        command.transferOut = Boolean.TRUE
        command.disableRefresh = Boolean.TRUE
        transfer(task, command)
    }

    /**
     * Returns the quantity of a pending task's stock that is still sitting in its putaway container. This happens
     * when a partial putaway splits an in-progress task, leaving the remainder PENDING while its stock remains in
     * the container it was loaded into during sortation.
     */
    Integer getQuantityRemainingInContainer(PutawayTask task) {
        if (!tracksInternalTransactions(task) || task.status != PutawayTaskStatus.PENDING || !task.container
                || !task.putawayOrderItem?.parentOrderItem) {
            return 0
        }

        Integer quantityOnHand = inventoryService.getQuantityFromBinLocation(task.facility, task.container, task.inventoryItem) ?: 0

        // Stock in the container that belongs to other tasks currently loaded into it
        BigDecimal quantityLoadedByOtherTasks = PutawayTask.createCriteria().get {
            projections {
                sum('quantity')
            }
            eq('container', task.container)
            eq('inventoryItem', task.inventoryItem)
            eq('status', PutawayTaskStatus.IN_PROGRESS)
            ne('id', task.id)
        } as BigDecimal ?: 0

        BigDecimal quantityRemaining = [quantityOnHand - quantityLoadedByOtherTasks, task.quantity].min()
        return quantityRemaining > 0 ? quantityRemaining.intValue() : 0
    }

    void transferToDestination(PutawayTask task, Boolean isDirectPutaway) {
        TransferStockCommand command = new TransferStockCommand()
        command.location = task.facility

        // If direct putaway, or stock was never moved into the container because internal transactions are not
        // tracked, then we transfer from origin to destination
        if (isDirectPutaway || !tracksInternalTransactions(task)) {
            command.binLocation = task.location
        } else {
            // otherwise if we're performing a two-step putaway, we'll transfer from container destination
            command.binLocation = task.container
        }

        command.inventoryItem = task.inventoryItem
        command.quantity = task.quantity.toInteger()
        command.otherLocation = task.facility
        command.otherBinLocation = task.destination
        command.order = task.putawayOrder
        command.transferOut = Boolean.TRUE
        command.disableRefresh = Boolean.TRUE
        transfer(task, command)
    }

    private boolean tracksInternalTransactions(PutawayTask task) {
        return task.facility?.supports(ActivityCode.TRACK_INTERNAL_TRANSACTIONS)
    }

    void transfer(PutawayTask task, TransferStockCommand command) {
        // FIXME I would love to get rid of this but haven't figured out a good way to tell GORM that we
        //  have no intentions of saving this. I actually think the problem is with the association between
        //  PutawayTask and Order/OrderItem, so if we can remove those associations we can probably remove
        //  the discard.
        task.discard()

        // Create transaction
        Transaction transaction = inventoryService.transferStock(command)
        if (transaction?.hasErrors()) {
            String errorMessage = transaction.errors.allErrors.collect { it.defaultMessage ?: it.code }.join('; ')
            throw new ValidationException(
                    "Unable to transfer stock for putaway task ${task?.identifier}: ${errorMessage}",
                    transaction.errors)
        }

        // Emit putawayTask.completed event which will refresh the product availability table
        grailsApplication.mainContext.publishEvent(new PutawayTaskCompletedEvent(task))
    }

    /**
     * Rollback the putaway task from any state to pending state.
     *
     * @param task
     * @return
     */
    boolean rollback(PutawayTask task) {
        Order order = task.putawayOrder
        if (!order) {
            throw new ObjectNotFoundException(order.id, "Putaway task ${task.id} not found")
        }

        // FIXME Currently this only handles the direct putaway case. We need to also be able to rollback transactions
        //  in the two step (origin -> container -> destination) once that has been implemented.
        if (order.status == OrderStatus.COMPLETED) {
            // Get transaction we need to rollback
            List<Transaction> transactions = Transaction.where {
                order == order
            }.list()

            for (Transaction transaction : transactions) {
                // If transaction exists, delete the transaction and reset status of putaway
                if (transaction) {
                    if (transaction.localTransfer) {
                        transaction.localTransfer.delete()
                    } else {
                        transaction.delete()
                    }
                }
            }
        }

        executeRollback(task)
        save(task)

        return true
    }

    void executeRollback(PutawayTask task) {
        PutawayTaskStatus toStatus = PutawayTaskStatus.ROLLBACK_STATE_TRANSITIONS.get(task.status)
        log.info "toStatus " + toStatus
        task.status = toStatus
        task.assignee = null
        task.dateStarted = null
        task.dateCompleted = null
        task.completedBy = null
        save(task)
    }

    void executeStateTransition(PutawayTask task, PutawayTaskStatus to, boolean validate = true) {
        PutawayTaskStatus from = task.status ?: PutawayTaskStatus.PENDING
        if (validate && !PutawayTaskStatus.validateTransition(from, to)) {
            throw new IllegalStateException("Invalid transition ${from} -> ${to}")
        }
        task.status = to
    }

    private PutawayItem createSplitPutawayItem(PutawayTask task, BigDecimal quantity, PutawayStatus status, Location destination) {
        return new PutawayItem(
                quantity: quantity,
                putawayStatus: status,
                putawayLocation: destination,
                inventoryItem: task.inventoryItem,
                product: task.product,
                currentFacility: task.facility,
                currentLocation: task.location,
                containerLocation: task.container,
                receiptItem: task.putawayOrderItem?.receiptItem,
                receipt: task.putawayOrderItem?.receipt
        )
    }

    /**
     * A destination named in a request must be a location of the task's facility.
     */
    private static void validateInFacility(Location location, PutawayTask task) {
        if (location && location.parentLocation?.id != task.facility?.id) {
            throw new IllegalArgumentException("Location ${location.name} is not a location of facility ${task.facility?.name}")
        }
    }

    private Location findDiscrepancyLocation(PutawayTask task) {
        def discrepancyLocation = task.facility.internalLocations
                .find { it.supports(ActivityCode.LOST_AND_FOUND) }
        if (!discrepancyLocation) {
            throw new IllegalStateException("No discrepancy location found")
        }

        return discrepancyLocation
    }

    /**
     * Whether the shipment has cross-dock putaways and all of them have been completed. Allocation has to
     * wait until then: before the putaway runs the stock is still in inbound sortation, and before the
     * putaway exists the release would cover the order from ordinary stock instead of its own inbound.
     */
    @Transactional(readOnly = true)
    boolean isCrossDockPutawayCompleted(Shipment shipment) {
        if (!shipment) {
            return false
        }
        List<PutawayTask> tasks = PutawayTask.createCriteria().list {
            ne('status', PutawayTaskStatus.CANCELED)
            putawayOrderItem {
                receiptItem {
                    shipmentItem {
                        eq('shipment', shipment)
                    }
                }
            }
        } as List<PutawayTask>

        List<PutawayTask> crossDockTasks = tasks.findAll { it.putawayTypeCode == PutawayTypeCode.CROSS_DOCK }
        return crossDockTasks && crossDockTasks.every { it.status == PutawayTaskStatus.COMPLETED }
    }

    List<Location> getAlternateDestinations(PutawayTask task) {
        if (task.destination) {
            // We're cross-docking so we want to find similar locations in the same cross-docking zone
            if (task?.destination?.supports(ActivityCode.CROSS_DOCKING)) {
                if (task.destination.zone) {
                    return Location.findAllByParentLocationAndZone(task.facility, task.destination.zone)
                }
                else {
                    // TODO return the locations that share the same DeliveryTypeCode as the destination
                    //  need to wait until we merge the code from OBLS-180, OBLS-187
                }
            }
        }
        // return preferred putaway locations
        return InventoryLevel.getPutawayLocations(task.facility, task.product)
    }

    void sendDiscrepancyNotification(PutawayTask task) {
        try {
            grailsApplication.mainContext.publishEvent(new PutawayDiscrepancyEvent(task))
        } catch (Exception e) {
            log.error("Error while processing PutawayDiscrepancyEvent", e)
        }
    }

    // FIXME Move to PutawayMetricService if we need to calculate more metrics
    BigDecimal getAveragePutawayCycleTime(Location facility, Date startAt, Date endAt) {
        def session = sessionFactory.currentSession
        String sql = '''
            SELECT COALESCE(AVG(TIMESTAMPDIFF(SECOND, putaway_task.date_created, putaway_task.date_completed)), 0)
            FROM putaway_task
            WHERE putaway_task.date_completed IS NOT NULL
              AND putaway_task.status = 'COMPLETED'
              AND putaway_task.facility_id = :facilityId
              AND putaway_task.date_created >= :startAt
              AND putaway_task.date_created <  :endAt
        '''

        Number seconds = (Number) session.createNativeQuery(sql)
                .setParameter('facilityId', facility.id)
                .setParameter('startAt', startAt)
                .setParameter('endAt', endAt)
                .uniqueResult()
        return (seconds?.toBigDecimal() ?: 0) / 60
    }

}
