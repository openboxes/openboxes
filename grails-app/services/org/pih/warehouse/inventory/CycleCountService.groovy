package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang.StringEscapeUtils
import org.grails.datastore.mapping.query.api.Criteria
import org.hibernate.ObjectNotFoundException
import org.hibernate.criterion.Order
import org.hibernate.sql.JoinType
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.product.Product

@Transactional
class CycleCountService {

    CycleCountTransactionService cycleCountTransactionService
    CycleCountProductAvailabilityService cycleCountProductAvailabilityService
    ProductAvailabilityService productAvailabilityService

    List<CycleCountCandidate> getCandidates(CycleCountCandidateFilterCommand command, String facilityId) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid params", command.errors)
        }
        Integer max = command.format == "csv" ? null : command.max
        Integer offset = command.format == "csv" ? null : command.offset
        Location facility = Location.read(facilityId)
        // Store added aliases to avoid duplicate alias exceptions for product
        // This could happen when params.searchTerm and e.g. sort by product is applied
        Set<String> usedAliases = new HashSet<>()
        return CycleCountCandidate.createCriteria().list(max: max, offset: offset) {
            eq("facility", facility)
            if (command.searchTerm) {
                createProductAlias(delegate, usedAliases)
                or {
                    ilike("product.productCode", "%${command.searchTerm}%")
                    ilike("product.name", "%${command.searchTerm}%")
                }
            }
            if (command.categories) {
                usedAliases.add("product")
                createAlias("product", "product", JoinType.INNER_JOIN)
                "in"("product.category", command.categories)
            }
            if (command.internalLocations) {
                or {
                    command.internalLocations.each {
                        ilike("internalLocations", "%${it}%")
                    }
                }
            }
            if (command.dateLastCount) {
                lte("dateLastCount", command.dateLastCount)
            }
            if (command.catalogs) {
                createProductAlias(delegate, usedAliases)
                createAlias("product.productCatalogItems", "productCatalogItems")
                usedAliases.add("productCatalogItems")
                "in"("productCatalogItems.productCatalog", command.catalogs)
            }
            if (command.tags) {
                createProductAlias(delegate, usedAliases)
                createAlias("product.tags", "tags")
                usedAliases.add("tags")
                "in"("tags.id", command.tags.collect { it.id })
            }
            if (command.abcClasses) {
                "in"("abcClass", command.abcClasses)
            }
            if (command.sort) {
                getCandidatesSortOrder(command.sort, command.order, delegate, usedAliases)
            }
            if (!command.statuses) {
                isNull("status")
            }
            else {
                inList("status", command.statuses)
            }
            if (command.negativeQuantity) {
                gt("negativeItemCount", 0)
            }
        } as List<CycleCountCandidate>
    }

    private static void getCandidatesSortOrder(String sortBy, String orderDirection, Criteria criteria, Set<String> usedAliases) {
        switch (sortBy) {
            case "product":
                createProductAlias(criteria, usedAliases)
                criteria.addOrder(getOrderDirection("product.productCode", orderDirection))
                break
            case "dateLastCount":
                criteria.addOrder(getOrderDirection("dateLastCount", orderDirection))
                break
            case "category":
                createProductAlias(criteria, usedAliases)
                criteria.createAlias("product.category", "category", JoinType.INNER_JOIN)
                usedAliases.add("category")
                criteria.addOrder(getOrderDirection("category.name", orderDirection))
                break
            case "abcClass":
                criteria.addOrder(getOrderDirection("abcClass", orderDirection))
                break
            case "quantityOnHand":
                criteria.addOrder(getOrderDirection("quantityOnHand", orderDirection))
                break
            default:
                break
        }
    }

    private static Order getOrderDirection(String sort, String order) {
        if (order == "desc") {
            return Order.desc(sort)
        }
        return Order.asc(sort)
    }

    private static void createProductAlias(Criteria criteria, Set<String> usedAliases) {
        if (!usedAliases.contains("product")) {
            usedAliases.add("product")
            criteria.createAlias("product", "product", JoinType.INNER_JOIN)
        }
    }

    List<CycleCountRequest> createRequests(CycleCountRequestBatchCommand command) {
        List<CycleCountRequest> cycleCountsRequests = []
        command.requests.each { CycleCountRequestCommand request ->
            CycleCountRequest cycleCountRequest = new CycleCountRequest(
                    facility: request.facility,
                    product: request.product,
                    status: CycleCountRequestStatus.CREATED,
                    requestType: CycleCountRequestType.MANUAL_REQUEST,
                    blindCount: request.blindCount,
                    createdBy: AuthService.currentUser,
                    updatedBy: AuthService.currentUser
            )
            cycleCountsRequests.add(cycleCountRequest)
        }
        cycleCountsRequests.each { CycleCountRequest cycleCountRequest ->
            if (!cycleCountRequest.validate()) {
                throw new ValidationException("Invalid cycle count request", cycleCountRequest.errors)
            }
            cycleCountRequest.save()
        }
        return cycleCountsRequests
    }

    CSVPrinter getCycleCountCsv(List<CycleCountCandidate> candidates) {
        CSVPrinter csv = CSVUtils.getCSVPrinter()

        csv.printRecord(
                "Code",
                "Product",
                "Product Family",
                "Category",
                "Formularies",
                "ABC Classification",
                "Bin Location",
                "Tag",
                "Last Counted",
                "QoH",
        )

        candidates?.each { CycleCountCandidate candidate ->
            csv.printRecord(
                    StringEscapeUtils.escapeCsv(candidate?.product?.productCode),
                    candidate?.product?.name ?: "",
                    candidate?.product?.productFamily ?: "",
                    StringEscapeUtils.escapeCsv(candidate?.product?.category?.name ?: ""),
                    candidate?.product?.productCatalogs?.join(", ") ?: "",
                    StringEscapeUtils.escapeCsv(candidate?.abcClass),
                    StringEscapeUtils.escapeCsv(candidate?.internalLocations ?: ""),
                    StringEscapeUtils.escapeCsv(candidate?.product?.tags?.tag?.join(", ")),
                    candidate?.dateLastCount?.format(Constants.EUROPEAN_DATE_FORMAT) ?: "",
                    candidate?.quantityOnHand ?: 0,
            )
        }

        return csv
    }

    List<Map> getCountFormXls(List<CycleCountDto> cycleCounts) {
        List<Map> data = []
        cycleCounts.each { CycleCountDto cycleCount ->
            cycleCount.cycleCountItems.each { CycleCountItemDto item ->
                data << [
                        "Product Code": item.product.productCode,
                        "Product Name": item.product.name,
                        "Lot Number": item.inventoryItem.lotNumber,
                        "Expiration Date": item.inventoryItem.expirationDate
                                ? Constants.EXPIRATION_DATE_FORMATTER.format(item.inventoryItem.expirationDate) : "",
                        "Bin Location": item.binLocation?.locationNumber,
                        "Quantity Counted": "",
                        "Comment": "",
                        "User Counted": "",
                        "Date Counted": ""
                ]
            }
        }

        return data
    }

    List<Map> getRecountFormXls(List<CycleCountDto> cycleCounts) {
        List<Map> data = []
        cycleCounts.each { CycleCountDto cycleCount ->
            cycleCount.cycleCountItems?.findAll { it.countIndex == 0 }?.each { CycleCountItemDto item ->
                data << [
                        "Product Code": item.product.productCode,
                        "Product Name": item.product.name,
                        "Lot Number": item.inventoryItem.lotNumber,
                        "Expiration Date": item.inventoryItem.expirationDate
                                ? Constants.EXPIRATION_DATE_FORMATTER.format(item.inventoryItem.expirationDate) : "",
                        "Bin Location": item.binLocation?.locationNumber,
                        "Quantity Counted": item.quantityCounted,
                        "Difference": item.quantityVariance,
                        "Counted by": item.assignee,
                        "Date Counted": item.dateCounted ? Constants.EXPIRATION_DATE_FORMATTER.format(item.dateCounted) : "",
                        "Quantity Recounted": "",
                        "Comment": "",
                        "Recounted By": "",
                        "Date Recounted": ""
                ]
            }
        }

        return data
    }

    /**
     * Batch start multiple cycle counts.
     */
    List<CycleCountDto> startCycleCount(CycleCountStartBatchCommand command) {
        List<CycleCountDto> cycleCounts = []
        command.requests.each { CycleCountStartCommand request ->
            CycleCountDto cycleCountDto = startCycleCount(request, command.facility)
            cycleCounts.add(cycleCountDto)
        }
        return cycleCounts
    }

    /**
     * Starts a cycle count. If the count is already started for the given cycle count request, we simply
     * return it as is.
     */
    CycleCountDto startCycleCount(CycleCountStartCommand command, Location facility) {
        CycleCount cycleCount = command.cycleCountRequest.cycleCount

        // When first starting a count, the cycle count object won't exist yet, so create it now.
        if (!cycleCount) {
            return createCycleCount(command, facility)
        }

        // Otherwise the count has already been started so simply return it as is. We allow this behaviour of
        // "starting" already started counts (instead of throwing an error) because it's simpler for the frontend.
        return CycleCountDto.toDto(cycleCount)
    }

    /**
     * Initializes a new cycle count given a request to start the count.
     */
    private CycleCountDto createCycleCount(CycleCountStartCommand request, Location facility) {
        CycleCount newCycleCount = new CycleCount(
                facility: facility,
                // Set an initial status here so that validation passes. It gets automatically recomputed on save.
                status: CycleCountStatus.REQUESTED,
                dateLastRefreshed: new Date()
        )

        List<AvailableItem> itemsToSave = cycleCountProductAvailabilityService.getAvailableItems(
                facility, request.cycleCountRequest.product)
        // 1:1 association between cycle count and cycle count request
        request.cycleCountRequest.cycleCount = newCycleCount
        request.cycleCountRequest.status = CycleCountRequestStatus.IN_PROGRESS
        itemsToSave.each { AvailableItem availableItem ->
            CycleCountItem cycleCountItem = initCycleCountItem(
                    facility,
                    availableItem,
                    newCycleCount,
                    0,  // countIndex is always zero for the initial count
                    CycleCountItemStatus.READY_TO_COUNT)

            newCycleCount.addToCycleCountItems(cycleCountItem)
        }

        if (!newCycleCount.save()) {
            throw new ValidationException("Invalid cycle count", newCycleCount.errors)
        }

        return CycleCountDto.toDto(newCycleCount)
    }

    /**
     * Inserts a list of cycle count items representing the recount for each request in the given batch.
     */
    List<CycleCountDto> startRecount(CycleCountStartRecountBatchCommand command) {
        Location facility = command.facility

        List<CycleCountDto> cycleCounts = []
        for (CycleCountStartRecountCommand request : command.requests) {
            CycleCountDto cycleCountDto = startRecount(facility, request)
            cycleCounts.add(cycleCountDto)
        }

        return cycleCounts
    }

    /**
     * Inserts a list of cycle count items representing the recount for the given request.
     */
    CycleCountDto startRecount(Location facility, CycleCountStartRecountCommand command) {
        CycleCount cycleCount = command.cycleCountRequest.cycleCount
        Product product = command.cycleCountRequest.product

        // If there are already items for the requested count index, simply return the count as it is since the recount
        // has already been started. We do this (instead of throwing an error) because it's convenient for the frontend.
        if (cycleCount.cycleCountItems.any{ it.countIndex == command.countIndex }) {
            return CycleCountDto.toDto(cycleCount)
        }

        // The items to recount are determined by product availability, just like in a regular count. As such, any
        // new transactions that have occurred on the product since the initial count will be applied when determining
        // the QoH values used for recounts. This includes any new [bin location + lot number] quantities that did not
        // exist at the time of the initial count.
        List<AvailableItem> availableItemsToRecount = cycleCountProductAvailabilityService.getAvailableItems(
                facility, product)
        for (AvailableItem availableItemToRecount : availableItemsToRecount) {
            CycleCountItem cycleCountItem = initCycleCountItem(
                    facility,
                    availableItemToRecount,
                    cycleCount,
                    command.countIndex,
                    CycleCountItemStatus.INVESTIGATING)

            cycleCount.addToCycleCountItems(cycleCountItem)
        }

        if (!cycleCount.save()) {
            throw new ValidationException("Invalid cycle count", cycleCount.errors)
        }
        return CycleCountDto.toDto(cycleCount)
    }

    private CycleCountItem initCycleCountItem(
            Location facility,
            AvailableItem availableItem,
            CycleCount cycleCount,
            int countIndex,
            CycleCountItemStatus status) {

        return new CycleCountItem(
                status: status,
                countIndex: countIndex,
                quantityOnHand: availableItem.quantityOnHand,
                quantityCounted: availableItem.quantityOnHand == 0 ? 0 : null,
                cycleCount: cycleCount,
                facility: facility,
                location: availableItem.binLocation,
                inventoryItem: availableItem.inventoryItem,
                product: availableItem.inventoryItem.product,
                createdBy: AuthService.currentUser,
                updatedBy: AuthService.currentUser,
                dateCounted: new Date(),
                custom: false,
        )
    }

    List<CycleCountDto> getCycleCounts(List<String> ids) {
        List<CycleCount> cycleCounts = CycleCount.createCriteria().list {
            if (ids) {
                'in'("id", ids)
            }
        } as List<CycleCount>
        return cycleCounts.collect { CycleCountDto.toDto(it) }
    }

    /**
     * Submits the (re)count as it is in its current state.
     */
    CycleCountDto submitCount(CycleCountSubmitCountCommand command) {
        CycleCount cycleCount = command.cycleCount

        if (command.refreshQuantityOnHand) {
            CycleCountProductAvailabilityService.CycleCountItemsForRefresh refreshedItems =
                    cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

            if (refreshedItems.itemsHaveChanged() && command.failOnOutdatedQuantity) {
                throw new IllegalArgumentException("Quantity on hand for a cycle count item is no longer up to date")
            }
        }

        cycleCount.cycleCountItems.each { CycleCountItem cycleCountItem ->
            determineCycleCountItemStatusForSubmit(cycleCountItem, command.requireRecountOnDiscrepancy)
        }

        cycleCount.status = cycleCount.recomputeStatus()
        if (cycleCount.status.isClosed()) {
            closeCycleCount(cycleCount, command.refreshQuantityOnHand)
        }

        // TODO: The beforeUpdate() on CycleCount class is not triggered without
        // the line below, so without it status is not correct in the DB.
        // Investigate why this line is needed.
        cycleCount.save()
        return CycleCountDto.toDto(cycleCount)
    }

    private void determineCycleCountItemStatusForSubmit(CycleCountItem cycleCountItem, boolean requireRecountOnDiscrepancy) {
        if ((cycleCountItem.quantityOnHand == cycleCountItem.quantityCounted) || !requireRecountOnDiscrepancy) {
            // TODO: Once we add support for the "to review" tab, this should be changed to assign status =
            //       CycleCountItemStatus.READY_TO_REVIEW. For now we simply complete the count for the item.
            cycleCountItem.status = CycleCountItemStatus.APPROVED
            return
        }
        cycleCountItem.status = CycleCountItemStatus.COUNTED
    }

    /**
     * Given a resolved cycle count, close it out by bringing it to the completion state and committing any
     * required quantity adjustments.
     */
    private void closeCycleCount(CycleCount cycleCount, boolean refreshQuantityOnHand) {
        // If the count was cancelled, there's nothing to do except also cancel the request
        if (cycleCount.status == CycleCountStatus.CANCELED) {
            cycleCount.cycleCountRequest.status = CycleCountRequestStatus.CANCELED
            return
        }

        if (cycleCount.status != CycleCountStatus.COMPLETED) {
            throw new IllegalStateException("Cannot complete cycle count when it's in status ${cycleCount.status}")
        }

        // The count completed successfully, so commit the adjustments and close out the cycle count request.
        cycleCountTransactionService.createTransactions(cycleCount, refreshQuantityOnHand)
        cycleCount.cycleCountRequest.status = CycleCountRequestStatus.COMPLETED
    }

    CycleCountItemDto updateCycleCountItem(CycleCountUpdateItemCommand command) {
        CycleCountItem cycleCountItem = command.cycleCountItem
        cycleCountItem.properties = command.properties
        cycleCountItem.countIndex = command.recount ? 1 : 0
        cycleCountItem.status = command.recount ? CycleCountItemStatus.INVESTIGATING : CycleCountItemStatus.COUNTING
        cycleCountItem.dateCounted = new Date()

        return cycleCountItem.toDto()
    }

    CycleCountItemDto createCycleCountItem(CycleCountItemCommand command) {
        Integer currentQuantityOnHand = productAvailabilityService.getQuantityOnHandInBinLocation(command.inventoryItem, command.facility) ?: 0
        CycleCountItem cycleCountItem = new CycleCountItem(
                facility: command.facility,
                status: command.recount ? CycleCountItemStatus.INVESTIGATING : CycleCountItemStatus.COUNTING,
                countIndex: command.recount ? 1 : 0,
                quantityOnHand: currentQuantityOnHand,
                quantityCounted: command.quantityCounted,
                cycleCount: command.cycleCount,
                location: command.binLocation,
                inventoryItem: command.inventoryItem,
                product: command.inventoryItem?.product,
                createdBy: AuthService.currentUser,
                updatedBy: AuthService.currentUser,
                dateCounted: new Date(),
                comment: command.comment,
                discrepancyReasonCode: command.discrepancyReasonCode,
                assignee: command.assignee,
                custom: true,
        )
        if (!cycleCountItem.validate()) {
            throw new ValidationException("Invalid cycle count item", cycleCountItem.errors)
        }
        cycleCountItem.save()

        return cycleCountItem.toDto()
    }

    void deleteCycleCountItem(String cycleCountItemId) {
        CycleCountItem cycleCountItem = CycleCountItem.get(cycleCountItemId)
        if (cycleCountItem && !cycleCountItem?.custom) {
            throw new IllegalArgumentException("Only custom cycle count items can be deleted")
        }
        cycleCountItem?.delete()
    }

    /**
     * Refreshes the most recent count items of a given cycle count.
     *
     * A "refresh" means fetching the product availability for the products associated with the count and updating
     * the QoH for each of the items in the most recent count.
     */
    CycleCountDto refreshCycleCount(String cycleCountId) {
        CycleCount cycleCount = CycleCount.get(cycleCountId)
        if (!cycleCount) {
            throw new ObjectNotFoundException(cycleCountId, CycleCount.class.toString())
        }

        if (!cycleCount.status.isCounting() && !cycleCount.status.isRecounting()) {
            throw new IllegalArgumentException("Cycle count cannot be refreshed when in state: ${cycleCount.status}")
        }

        cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

        return CycleCountDto.toDto(cycleCount)
    }
}
