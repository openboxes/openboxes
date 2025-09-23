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
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.product.Product

@Transactional
class PutawayTaskService {

    GrailsApplication grailsApplication
    InventoryService inventoryService
    PutawayService putawayService

    @Transactional(readOnly = true)
    List search(Location facility, Product product, Location container, StatusCategory statusCategory, Map params) {
        log.info "search putaway tasks " + params + " product=" + product?.toJson() + " facility " + facility
        Integer max = Math.min((params.int('max') ?: 50), 100) as Integer
        Integer offset = params.int('offset') ?: 0 as Integer
        String sort = params.sort ?: 'dateCreated'
        String order = (params.order ?: 'desc').toLowerCase() in ['asc', 'desc'] ? params.order : 'desc' as Integer

        // Get user-provided statuses
        List<PutawayTaskStatus> statuses = params.list("status").collect { it as PutawayTaskStatus }

        // Resolve the status category to a set of statuses and added to user-provided
        //StatusCategory statusCategory = params.statusCategory as StatusCategory
        List<PutawayTaskStatus> statusesByStatusCategory = PutawayTaskStatus.toSet(statusCategory)
        statuses += statusesByStatusCategory

        // Search for putaway tasks based on user-provided search parameters
        List<PutawayTask> tasks = PutawayTask.where {
            if (statuses) {
                status in statuses
            }
            if (product) {
                product == product
            }
            if (facility) {
                facility == facility
            }
            if (container) {
                container == container
            }

        }.list(max: max, offset: offset, sort: sort, order: order)

        return tasks
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
     * Action router for state transitions and side-effects (transaction).
     * Supported actions include: start, scanToContainer, complete, cancel, rollback
     * FIXME - this will eventually be replaced with a Spring Statemachine implementation.
     */
    PutawayTask patch(String id, Map data = [:]) {
        PutawayTask task = get(id)

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
                load(task, data.container as String, data.override as Boolean)
                break

            case 'complete':
                complete(task, data.destination as String, data.completedBy as String, data.force as Boolean)
                break

            case 'partialComplete':
                task = partialComplete(task, data.quantity as BigDecimal, data.destination as String, data.force as Boolean, data.reasonCode as ReasonCode)
                break

            case 'rollback':
                rollback(task)
                break

            case 'cancel':
                //executeStatusChange(task, PutawayTaskStatus.CANCELED)
                //putawayService.savePutaway(task.toPutaway())
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
     * Assign task to a putaway container (if one hasn't already been assigned)
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
        if (task.facility != container.parentLocation || !container.internalLocation) {
            throw new IllegalArgumentException("Putaway container ${container?.name} must be an internal location within facility ${task?.facility?.name}")
        }

        if (task.container && container != task.container && !override) {
            throw new IllegalArgumentException("Putaway container is already assigned: ${task?.container?.name}. Must override to re-assign it to putaway constainer ${container?.name}")
        }
        if (task.container == container) {
            log.warn "Putaway container for task ${task.identifier} has already been set to ${container}"
        }

        if (container) task.container = container

        // Set the assignee if one is provided
        Person assignee = assigneeId ? Person.get(assigneeId) : null
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
        executeStateTransition(task, PutawayTaskStatus.IN_PROGRESS)
        save(task)
    }

    /**
     * Load the putaway item into the assigned putaway container. Also, allows you to force the item into
     * a different putaway container (i.e. as long as it's not the destination location).
     *
     * @param task
     * @param containerNumberOrId
     * @param override
     */
    void load(PutawayTask task, String containerNumberOrId, Boolean override = false) {

        log.info "Loading item into putaway container ${containerNumberOrId}"

        // Validate the container location exists
        Location container = Location.findByLocationNumberOrId(containerNumberOrId, containerNumberOrId)
        if (!container) {
            throw new IllegalStateException("Container does not exist")
        }

        def allowedActivityCodes = [
                ActivityCode.PUTAWAY_CART,
                ActivityCode.PICK_CART,
                ActivityCode.OUTBOUND_STAGING
        ]
        if (!container.supportsAny(allowedActivityCodes as ActivityCode[])) {
            throw new IllegalArgumentException("Container ${container?.name} must support at least one of the following activities: " +
                    "PUTAWAY_CART, PICK_CART, or OUTBOUND_STAGING.")
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

        // Execute state transition
        executeStateTransition(task, PutawayTaskStatus.IN_TRANSIT)

        // Transfer stock from origin to putaway container
        // FIXME can't figure out the best way to make this
        transferToContainer(task)

        save(task)
    }

    void complete(PutawayTask task, String destinationId, String completedById, Boolean force = false) {
        log.info "complete putaway"
        if (!task) {
            throw new ObjectNotFoundException(task.id, "Unable to locate putaway task with id ${task.id}")
        }

        // validate destination or user has forced a destination change
        Location destination = Location.get(destinationId)
        if (!destination) {
            task.errors.reject("destination", "Destination is required")
        }

        // validate destination
        if (task.destination != destination && !force) {
            task.errors.reject("destination", "Destination provided does not match expected destination")
        }

        if (task.hasErrors() || !task.validate()) {
            throw new ValidationException("Validation errors occurred during complete action", task.errors)
        }

        // Update the task destination if the destinations do not match, but user forced change
        if (task.destination != destination && force) {
            task.destination = destination
        }

        Person completedBy = completedById ? Person.get(completedById) : AuthService.currentUser
        if (!completedBy) {
            task.errors.reject("completedBy", "Must provide a valid person or user who completed the putaway task")
        }

        task.dateCompleted = new Date()
        task.completedBy = completedBy
        executeStateTransition(task, PutawayTaskStatus.COMPLETED)

        // Transfer stock to destination (either from putaway container or destination)
        transferToDestination(task)

        // Save the task
        save(task)
    }

    PutawayTask partialComplete(PutawayTask task, BigDecimal quantity, String destinationId, Boolean force = false, ReasonCode reasonCode = null) {
        if (quantity > task.quantity) {
            throw new IllegalArgumentException("Quantity provided is more than requested. Please re-enter quantity")
        }

        BigDecimal quantityRemaining = task.quantity - quantity
        if (quantityRemaining <= 0) {
            throw new IllegalArgumentException("Quantity provided is more than requested. Please re-enter quantity")
        }

        // validate destination or user has forced a destination change
        Location destination = Location.get(destinationId)
        if (!destination) {
            task.errors.reject("destination", "Destination is required")
        }

        // validate destination
        if (task.destination != destination && !force) {
            task.errors.reject("destination", "Destination provided does not match expected destination")
        }

        // Update the task destination if the destinations do not match, but user forced change
        if (task.destination != destination && force) {
            task.destination = destination
        }

        if (reasonCode) {
            task.discrepancyReasonCode = reasonCode
            def discrepancyLocation = task.facility.internalLocations
                    .find { it.locationType.name == Constants.DISCREPANCY_LOCATION_TYPE}
            if (!discrepancyLocation) {
                throw new IllegalStateException("No discrepancy location found")
            }
            task.destination = discrepancyLocation
        }

        if (task.hasErrors() || !task.validate()) {
            throw new ValidationException("Validation errors occurred during complete action", task.errors)
        }

        task.discard()

        OrderItem currentItem = OrderItem.get(task.putawayOrderItem.id)
        def currentItemParent = currentItem.parentOrderItem
        currentItem.parentOrderItem = null
        Order order = currentItem.order

        Putaway putaway = Putaway.createFromOrder(order)
        if (!putaway.putawayAssignee) {
            putaway.putawayAssignee = AuthService.currentUser
        }
        PutawayItem itemToSplit = putaway.putawayItems.find { it.id == currentItem.id }

        PutawayItem completedSplitItem = createSplitPutawayItem(task, quantity, PutawayStatus.COMPLETED)
        PutawayItem remainingSplitItem = createSplitPutawayItem(task, quantityRemaining, PutawayStatus.PENDING)

        if (itemToSplit) {
            itemToSplit.splitItems = [completedSplitItem, remainingSplitItem]
        }
        putawayService.savePutaway(putaway)

        transferToDestination(task)
        save(task)

        currentItem.parentOrderItem = currentItemParent
        currentItem.orderItemStatusCode = OrderItemStatusCode.CANCELED
        currentItem.save(flush: true, failOnError: true)

        OrderItem remainingOrderItem = order.orderItems.find {
            it.parentOrderItem?.id == currentItem.id &&
                    it.quantity == quantityRemaining &&
                    it.orderItemStatusCode == OrderItemStatusCode.PENDING
        } as OrderItem

        return PutawayTaskAdapter.toPutawayTask(remainingOrderItem)
    }

    void transferToContainer(PutawayTask task) {
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

    void transferToDestination(PutawayTask task) {
        TransferStockCommand command = new TransferStockCommand()
        command.location = task.facility

        // If direct putaway (null container) then we transfer from origin to destination
        if (!task.container) command.binLocation = task.location
        // otherwise if we're performing a two-step putaway, we'll transfer from container destination
        else command.binLocation = task.container

        command.inventoryItem = task.inventoryItem
        command.quantity = task.quantity.toInteger()
        command.otherLocation = task.facility
        command.otherBinLocation = task.destination
        command.order = task.putawayOrder
        command.transferOut = Boolean.TRUE
        command.disableRefresh = Boolean.TRUE
        transfer(task, command)
    }

    void transfer(PutawayTask task, TransferStockCommand command) {
        // FIXME I would love to get rid of this but haven't figured out a good way to tell GORM that we
        //  have no intentions of saving this. I actually think the problem is with the association between
        //  PutawayTask and Order/OrderItem, so if we can remove those associations we can probably remove
        //  the discard.
        task.discard()

        // Create transaction
        inventoryService.transferStock(command)

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

    private PutawayItem createSplitPutawayItem(PutawayTask task, BigDecimal quantity, PutawayStatus status) {
        return new PutawayItem(
                quantity: quantity,
                putawayStatus: status,
                putawayLocation: task.destination,
                inventoryItem: task.inventoryItem,
                product: task.product,
                currentFacility: task.facility,
                currentLocation: task.location,
                containerLocation: task.container
        )
    }
}