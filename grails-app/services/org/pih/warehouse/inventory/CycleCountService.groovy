package org.pih.warehouse.inventory

import grails.gorm.PagedResultList
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.apache.commons.collections4.keyvalue.MultiKey
import org.apache.commons.collections4.map.MultiKeyMap
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang.StringEscapeUtils
import org.grails.datastore.mapping.query.api.Criteria
import org.hibernate.ObjectNotFoundException
import org.hibernate.criterion.Order
import org.hibernate.sql.JoinType
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.date.DateFormatterManager
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.product.Product
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.report.CycleCountReportCommand

@Transactional
class CycleCountService {

    /**
     * The count index representing the initial count.
     */
    static final int INITIAL_COUNT_INDEX = 0

    CycleCountTransactionService cycleCountTransactionService
    CycleCountProductAvailabilityService cycleCountProductAvailabilityService

    @Autowired
    DateFormatterManager dateFormatter

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

            if (!command.statuses) {
                isNull("status")
            }
            else {
                or {
                    inList("status", command.statuses)
                    if (command.showCycleCountsInProgress) {
                        isNull("status")
                    }
                }
            }

            if (command.countAssignees) {
                createCycleCountRequestAlias(delegate, usedAliases)
                "in"("ccr.countAssignee", command.countAssignees)
            }

            if (command.recountAssignees) {
                createCycleCountRequestAlias(delegate, usedAliases)
                "in"("ccr.recountAssignee", command.recountAssignees)
            }

            if (command.countDeadline) {
                createCycleCountRequestAlias(delegate, usedAliases)
                lte("ccr.countDeadline", command.countDeadline)
            }

            if (command.recountDeadline) {
                createCycleCountRequestAlias(delegate, usedAliases)
                lte("ccr.recountDeadline", command.recountDeadline)
            }

            if (command.negativeQuantity) {
                gt("negativeItemCount", 0)
            }

            // FIXME It's possible we need this to be "ne" rather "gt" because we probably want to include
            //  product/facility pairs where quantity on hand is negative.
            // Moved this from the cycle count session view since it's a requirement of the candidate query,
            // not the cycle count session.
            if (command.includeStockOnHandOrNegativeStock && !command.showCycleCountsInProgress) {
                eq("hasStockOnHandOrNegativeStock", command.includeStockOnHandOrNegativeStock)
            }

            // Products from the "To Count" and "To Resolve" tabs can have negative stock,
            // but we want to include them in the results
            if (command.showCycleCountsInProgress) {
                or {
                    inList("status", command.statuses)
                    eq("hasStockOnHandOrNegativeStock", command.includeStockOnHandOrNegativeStock)
                }
            }

            // FIXME Sort order should allow multiple sort order rules ("columna, -columnb"). We should consider
            //  using a more conventional syntax for the column and direction i.e. "columna" sorts "columna" in
            //  ascending order while "-columnb" sorts "columnb" in descending order.
            // Don't check command.sort because we want the default case to be applied if there's no sort order
            applySortOrderForCandidates(command.sort, command.order, delegate, usedAliases)

        } as List<CycleCountCandidate>
    }

    Integer getInventoryItemsCount(CycleCountRequest cycleCountRequest) {
        if (!cycleCountRequest.cycleCount) {
            return cycleCountProductAvailabilityService.getAvailableItems(
                    cycleCountRequest.facility,
                    cycleCountRequest.product
            )?.size()
        }

        return cycleCountRequest.cycleCount.numberOfItemsOfMostRecentCount
    }

    List<PendingCycleCountRequest> getPendingCycleCountRequests(CycleCountCandidateFilterCommand command, String facilityId) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid params", command.errors)
        }
        Integer max = command.format == "csv" ? null : command.max
        Integer offset = command.format == "csv" ? null : command.offset
        Location facility = Location.read(facilityId)
        // Store added aliases to avoid duplicate alias exceptions for product
        // This could happen when params.searchTerm and e.g. sort by product is applied
        Set<String> usedAliases = new HashSet<>()
        List<PendingCycleCountRequest> pendingCycleCountRequests = PendingCycleCountRequest.createCriteria().list(max: max, offset: offset) {
            eq("facility", facility)
            if(command.requestIds) {
                "in"("cycleCountRequest.id", command.requestIds)
            }
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

            if (command.statuses) {
                inList("status", command.statuses)
            }

            if (command.countAssignees) {
                createCycleCountRequestAlias(delegate, usedAliases)
                "in"("ccr.countAssignee", command.countAssignees)
            }

            if (command.recountAssignees) {
                createCycleCountRequestAlias(delegate, usedAliases)
                "in"("ccr.recountAssignee", command.recountAssignees)
            }

            if (command.countDeadline) {
                createCycleCountRequestAlias(delegate, usedAliases)
                lte("ccr.countDeadline", command.countDeadline)
            }

            if (command.recountDeadline) {
                createCycleCountRequestAlias(delegate, usedAliases)
                lte("ccr.recountDeadline", command.recountDeadline)
            }

            if (command.negativeQuantity) {
                gt("negativeItemCount", 0)
            }

            applySortOrderForCandidates(command.sort, command.order, delegate, usedAliases)

        } as List<PendingCycleCountRequest>

        // Access to the information about available cycle count items before creating the cycle count
        pendingCycleCountRequests.each {
            Integer inventoryItemsCount = getInventoryItemsCount(it.cycleCountRequest)
            it.cycleCountRequest.setInventoryItemsCount(inventoryItemsCount)
        }

        return pendingCycleCountRequests
    }

    private static void applySortOrderForCandidates(String sortBy, String orderDirection, Criteria criteria, Set<String> usedAliases) {
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

    private static void applySortOrderForCycleCounts(String sortBy, String orderDirection, Criteria criteria, Set<String> usedAliases) {
        switch (sortBy) {
            case "productName":
                criteria.createAlias("cycleCountItems", "item", JoinType.LEFT_OUTER_JOIN)
                criteria.createAlias("item.product", "product", JoinType.INNER_JOIN)
                usedAliases.addAll(["item", "product"])
                criteria.addOrder(getOrderDirection("product.name", orderDirection))
                criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
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

    // Prevents duplicate alias 'ccr' creation, which can happen when filtering by assignees and deadlines simultaneously.
    private static void createCycleCountRequestAlias(Criteria criteria, Set<String> usedAliases) {
        if (!usedAliases.contains("ccr")) {
            usedAliases.add("ccr")
            criteria.createAlias("cycleCountRequest", "ccr", JoinType.INNER_JOIN)
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

    /**
     * Bulk updates a given list of cycle count requests.
     */
    List<CycleCountRequest> updateRequests(CycleCountRequestUpdateBulkCommand command) {
        List<CycleCountRequest> updatedRequests = []
        for (CycleCountRequestUpdateCommand requestToUpdate in command.commands) {
            CycleCountRequest updatedRequest = updateRequest(requestToUpdate)
            updatedRequests.add(updatedRequest)
        }
        return updatedRequests
    }

    private CycleCountRequest updateRequest(CycleCountRequestUpdateCommand command) {
        CycleCountRequest cycleCountRequest = command.cycleCountRequest
        CycleCountAssignmentCommand countAssignment = command.getAssignmentByCountIndex(Constants.COUNT_INDEX)
        if (countAssignment) {
            cycleCountRequest.countAssignee = countAssignment.assignee
            cycleCountRequest.countDeadline = countAssignment.deadline
        }
        CycleCountAssignmentCommand recountAssignment = command.getAssignmentByCountIndex(Constants.RECOUNT_INDEX)
        if (recountAssignment) {
            cycleCountRequest.recountAssignee = recountAssignment.assignee
            cycleCountRequest.recountDeadline = recountAssignment.deadline
        }

        if (!cycleCountRequest.save()) {
            throw new ValidationException("Invalid cycle count request", cycleCountRequest.errors)
        }
        return cycleCountRequest
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
                    dateFormatter.formatForExport(candidate?.dateLastCount),
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
                        "Product cycle count id": cycleCount.id,
                        "Cycle count item id": item.id,
                        "Product Code": item.product.productCode,
                        "Product Name": item.product.name,
                        "Lot Number": item.inventoryItem.lotNumber,
                        "Expiration Date": item.inventoryItem.expirationDate
                                ? Constants.EXPIRATION_DATE_FORMATTER.format(item.inventoryItem.expirationDate) : "",
                        "Bin Location": item.binLocation?.name,
                        "Quantity Counted": item.quantityCounted != null ? item.quantityCounted : "",
                        "Comment": item.comment ?: "",
                        "User Counted": item.assignee?.name ?: "",
                        "Date Counted": dateFormatter.formatForExport(item.dateCounted),
                ]
            }
        }

        return data
    }

    List<Map> getRecountFormXls(List<CycleCountDto> cycleCounts) {
        List<Map> data = []
        cycleCounts.each { CycleCountDto cycleCount ->
            data.addAll(getRecountAsXlsMap(cycleCount))
        }

        return data
    }

    /**
     * Converts a CycleCountDto to a list of rows/maps. For use when exporting to XLS.
     */
    private List<Map> getRecountAsXlsMap(CycleCountDto cycleCount) {
        int currentCountIndex = cycleCount.maxCountIndex

        // Build a map to make it easier to group the count and recount items into one row in the XLS.
        // The outer map is keyed on [product code + lot + bin]. The inner map is keyed on count index.
        MultiKeyMap<String, Map<Integer, CycleCountItemDto>> countItemsMap = [:]
        List<CycleCountItemDto> customRecountItems = []
        for (CycleCountItemDto item in cycleCount.cycleCountItems) {
            // Keep custom recount items separate because they can have duplicate keys and don't have a count item.
            if (item.countIndex == currentCountIndex && item.custom) {
                customRecountItems.add(item)
                continue
            }

            MultiKey<String> key = new MultiKey(
                    item.product.productCode,
                    item.inventoryItem?.lotNumber,
                    item.binLocation?.get('name'),
            )
            Map<Integer, CycleCountItemDto> countItemByIndex = countItemsMap.computeIfAbsent(key, { k -> [:] })
            countItemByIndex.put(item.countIndex, item)
        }

        List<Map> data = []
        for (itemsEntry in countItemsMap.entrySet()) {
            CycleCountItemDto countItem = itemsEntry.value.get(INITIAL_COUNT_INDEX)
            CycleCountItemDto recountItem = itemsEntry.value.get(currentCountIndex)
            data << mergeCountAndRecountItemAsXlsMap(countItem, recountItem)
        }

        for (CycleCountItemDto customItem in customRecountItems) {
            data << mergeCountAndRecountItemAsXlsMap(null, customItem)
        }
        return data
    }

    /**
     * Merge two CycleCountItemDtos together into a single map/row. For use when exporting to XLS.
     * @param countItem Represents the item in the original count
     * @param recountItem Represents the item in the current recount
     */
    private Map mergeCountAndRecountItemAsXlsMap(CycleCountItemDto countItem, CycleCountItemDto recountItem) {
        return [
                "Product Code": recountItem.product.productCode,
                "Product Name": recountItem.product.name,
                "Lot Number": recountItem.inventoryItem.lotNumber,
                "Expiration Date": recountItem.inventoryItem.expirationDate
                        ? Constants.EXPIRATION_DATE_FORMATTER.format(recountItem.inventoryItem.expirationDate) : "",
                "Bin Location": recountItem.binLocation?.name,

                // Count-specific fields
                "Quantity Counted": countItem?.quantityCounted != null ? countItem.quantityCounted : "",
                "Difference": countItem?.quantityVariance ?: "",
                "Counted by": countItem?.assignee ?: "",
                "Date Counted": dateFormatter.formatForExport(countItem?.dateCounted),

                // Recount-specific fields
                "Quantity Recounted": recountItem.quantityCounted != null ? recountItem.quantityCounted : "",
                "Root Cause": recountItem.discrepancyReasonCode ?: "",
                "Comment": recountItem.comment ?: "",
                "Recounted By": recountItem.assignee ?: "",
                "Date Recounted": dateFormatter.formatForExport(recountItem.dateCounted),
        ]
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

        updateCountAssigneeData(cycleCount)

        // Otherwise the count has already been started so simply return it as is. We allow this behaviour of
        // "starting" already started counts (instead of throwing an error) because it's simpler for the frontend.
        return CycleCountDto.toDto(cycleCount)
    }

    void updateCountAssigneeData(CycleCount cycleCount) {
        Person assignee = cycleCount.cycleCountRequest.countAssignee
        // We want to override assignee while starting the count
        cycleCount.cycleCountItems.each {
            if (it.countIndex == 0) {
                it.assignee = assignee
            }
        }
    }

    void updateRecountAssigneeData(CycleCount cycleCount) {
        Person assignee = cycleCount.cycleCountRequest.recountAssignee
        // We want to override assignee while starting the recount
        cycleCount.cycleCountItems.each {
            if (it.countIndex > 0) {
                it.assignee = assignee
            }
        }
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
        Person assignee = request.cycleCountRequest.countAssignee
        itemsToSave.each { AvailableItem availableItem ->
            CycleCountItem cycleCountItem = initCycleCountItem(
                    facility,
                    availableItem,
                    newCycleCount,
                    0,  // countIndex is always zero for the initial count
                    CycleCountItemStatus.READY_TO_COUNT,
                    assignee,
            )

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
        Integer countIndex = command.countIndex

        // If there are already items for the requested count index, simply return the count as it is since the recount
        // has already been started. We do this (instead of throwing an error) because it's convenient for the frontend.
        if (cycleCount.maxCountIndex >= countIndex) {
            updateRecountAssigneeData(cycleCount)
            return CycleCountDto.toDto(cycleCount)
        }

        // The items to recount are determined by product availability, just like in a regular count. As such, any
        // new transactions that have occurred on the product since the initial count will be applied when determining
        // the QoH values used for recounts. This includes any new [bin location + lot number] quantities that did not
        // exist at the time of the initial count.
        List<AvailableItem> availableItemsToRecount = cycleCountProductAvailabilityService.getAvailableItems(
                facility, product)
        Person assignee = command.cycleCountRequest.recountAssignee
        for (AvailableItem availableItemToRecount : availableItemsToRecount) {
            CycleCountItem cycleCountItem = initCycleCountItem(
                    facility,
                    availableItemToRecount,
                    cycleCount,
                    countIndex,
                    CycleCountItemStatus.INVESTIGATING,
                    assignee,
            )

            cycleCount.addToCycleCountItems(cycleCountItem)
        }

        // If there are custom items, they won't have been discovered by the above loop (because they won't have
        // a product availability record) so make sure to create new recount items for them as well.
        int countIndexForCustomItems = cycleCount.maxCountIndex == 0 ? 0 : cycleCount.maxCountIndex - 1
        Set<CycleCountItem> customItemsOfLastCount = cycleCount.cycleCountItems.findAll {
            countIndexForCustomItems == it.countIndex && it.custom
        }
        for (CycleCountItem customCycleCountItem : customItemsOfLastCount) {
            // We want to avoid a situation where we create an inventory using a custom row
            // and then someone is creating the same inventory on record stock
            CycleCountItem itemAlreadyExists = cycleCount.getCycleCountItem(customCycleCountItem.product,
                    customCycleCountItem.location, customCycleCountItem.inventoryItem, countIndex)
            if (itemAlreadyExists) {
                continue
            }

            CycleCountItem cycleCountItem = initCycleCountItemFromCustom(
                    facility,
                    customCycleCountItem,
                    cycleCount,
                    countIndex,
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
            CycleCountItemStatus status,
            Person assignee = null
    ) {

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
                assignee: assignee,
                custom: false,
        )
    }

    private CycleCountItem initCycleCountItemFromCustom(
            Location facility,
            CycleCountItem cycleCountItem,
            CycleCount cycleCount,
            int countIndex,
            CycleCountItemStatus status) {

        return new CycleCountItem(
                status: status,
                countIndex: countIndex,
                quantityOnHand: cycleCountItem.quantityOnHand,
                quantityCounted: null,
                cycleCount: cycleCount,
                facility: facility,
                location: cycleCountItem.location,
                inventoryItem: cycleCountItem.inventoryItem,
                product: cycleCountItem.inventoryItem.product,
                createdBy: AuthService.currentUser,
                updatedBy: AuthService.currentUser,
                dateCounted: new Date(),

                // Note that even though the given item is custom added, the resulting item is treated as not
                // custom. This is done to preserve count information. Ex: If the item was custom added during the
                // count, we don't want to be able to remove it from the recount. If on a recount a user decides they
                // don't actually need an item they added during the count, they can set its quantity counted to 0.
                custom: false,
        )
    }

    List<CycleCountDto> getCycleCounts(List<String> ids, String sortBy) {
        Set<String> usedAliases = new HashSet<>()
        List<CycleCount> cycleCounts = CycleCount.createCriteria().list {
            if (ids) {
                'in'("id", ids)
            }
            applySortOrderForCycleCounts(sortBy, "asc", delegate, usedAliases)
        } as List<CycleCount>
        return cycleCounts.collect { CycleCountDto.toDto(it) }
    }

    /**
     * Submits the (re)count as it is in its current state.
     */
    CycleCountDto submitCount(CycleCountSubmitCountCommand command) {
        CycleCount cycleCount = command.cycleCount

        // OBPIH-7525. We need to take a snapshot of the counted products now because the refresh might remove some
        // (if QoH has since become zero) but we still want to create a baseline transaction for all of them.
        List<Product> countedProducts = cycleCount.products

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

        // We've updated the status of the cycle count items so we need to also update the status of the count.
        cycleCount.status = cycleCount.recomputeStatus()

        if (cycleCount.status?.isClosed()) {
            closeCycleCount(cycleCount, countedProducts, command.refreshQuantityOnHand)
        }

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
    private void closeCycleCount(CycleCount cycleCount, List<Product> countedProducts, boolean refreshQuantityOnHand) {
        // If the count was cancelled, there's nothing to do except also cancel the request
        if (cycleCount.status == CycleCountStatus.CANCELED) {
            cycleCount.cycleCountRequest.status = CycleCountRequestStatus.CANCELED
            return
        }

        if (cycleCount.status != CycleCountStatus.COMPLETED) {
            throw new IllegalStateException("Cannot complete cycle count when it's in status ${cycleCount.status}")
        }

        // The count completed successfully, so commit the adjustments and close out the cycle count request.
        cycleCountTransactionService.createTransactions(cycleCount, countedProducts, refreshQuantityOnHand)
        cycleCount.cycleCountRequest.status = CycleCountRequestStatus.COMPLETED
    }

    List<CycleCountItemDto> updateCycleCountItems(List<CycleCountUpdateItemCommand> items) {
        List<CycleCountItemDto> updatedItems = []
        items.each { CycleCountUpdateItemCommand item ->
            CycleCountItemDto cycleCountItem = updateCycleCountItem(item)
            updatedItems.add(cycleCountItem)
        }

        return updatedItems
    }

    CycleCountItemDto updateCycleCountItem(CycleCountUpdateItemCommand command) {
        CycleCountItem cycleCountItem = command.cycleCountItem
        cycleCountItem.properties = command.properties
        cycleCountItem.countIndex = command.recount ? 1 : 0
        cycleCountItem.status = command.recount ? CycleCountItemStatus.INVESTIGATING : CycleCountItemStatus.COUNTING
        // If the dateCounted field is null, set it to today's date
        if (cycleCountItem.dateCounted == null) {
            cycleCountItem.dateCounted = new Date()
        }

        // We've updated the status of a cycle count item so we need to also update the status of the count.
        cycleCountItem.cycleCount.status = cycleCountItem.cycleCount.recomputeStatus()

        return cycleCountItem.toDto()
    }

    List<CycleCountItemDto> createCycleCountItems(List<CycleCountItemCommand> items) {
        List<CycleCountItemDto> createdItems = []
        items.each { CycleCountItemCommand item ->
            CycleCountItemDto cycleCountItem = createCycleCountItem(item)
            createdItems.add(cycleCountItem)
        }

        return createdItems
    }


    CycleCountItemDto createCycleCountItem(CycleCountItemCommand command) {
        if (!command.inventoryItem?.id) {
            // Make sure the inventory item for a particular product and lot has not just been created - this might be a case for a batch operation,
            // where a few products might share the same inventory item. Since the batch list is bound at the beginning, e.g. 5th item might not know that 2nd item
            // has already initialized and potentially created the inventory item that should be used by the 5th item afterwards.
            InventoryItem inventoryItem = InventoryItem.findByProductAndLotNumber(command.inventoryItem.product, command.inventoryItem.lotNumber)
            if (inventoryItem){
                command.inventoryItem = inventoryItem
            }
            if (!command.inventoryItem.validate()) {
                throw new ValidationException("Invalid inventory item", command.inventoryItem.errors)
            }
            // Flush is needed for the batch operation, so that for next iterations we are able to fetch just created inventory item in any previous row (look above for details)
            command.inventoryItem.save(flush: true)
        }
        CycleCount cycleCount = command.cycleCount

        CycleCountItem cycleCountItem = new CycleCountItem(
                facility: command.facility,
                status: command.recount ? CycleCountItemStatus.INVESTIGATING : CycleCountItemStatus.COUNTING,
                countIndex: command.recount ? 1 : 0,
                quantityOnHand: 0,
                quantityCounted: command.quantityCounted,
                cycleCount: cycleCount,
                location: command.binLocation,
                inventoryItem: command.inventoryItem,
                product: command.inventoryItem?.product,
                createdBy: AuthService.currentUser,
                updatedBy: AuthService.currentUser,
                dateCounted: command.dateCounted ?: new Date(),
                comment: command.comment,
                discrepancyReasonCode: command.discrepancyReasonCode,
                assignee: command.assignee,
                custom: true,
        )
        if (!cycleCountItem.validate()) {
            throw new ValidationException("Invalid cycle count item", cycleCountItem.errors)
        }
        cycleCountItem.save()

        // We're adding a new cycle count item to the count so we need to also update the status of the count.
        cycleCount.addToCycleCountItems(cycleCountItem)
        cycleCount.status = cycleCount.recomputeStatus()

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
    CycleCountDto refreshCycleCount(String cycleCountId, boolean removeOutOfStockItemsImplicitly, Integer countIndex) {
        CycleCount cycleCount = CycleCount.get(cycleCountId)
        if (!cycleCount) {
            throw new ObjectNotFoundException(cycleCountId, CycleCount.class.toString())
        }

        if (!cycleCount.status.isCounting() && !cycleCount.status.isRecounting()) {
            throw new IllegalArgumentException("Cycle count cannot be refreshed when in state: ${cycleCount.status}")
        }

        cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount, removeOutOfStockItemsImplicitly, countIndex)

        return CycleCountDto.toDto(cycleCount)
    }

    /**
     * Batch deletes a list of CycleCountRequest as well as the associated CycleCount and CycleCountItems if they exist.
     */
    void deleteCycleCountRequests(List<String> cycleCountRequestIds) {
        for (String cycleCountRequestId : cycleCountRequestIds) {
            deleteCycleCountRequest(cycleCountRequestId)
        }
    }

    private void deleteCycleCountRequest(String cycleCountRequestId) {
        CycleCountRequest cycleCountRequest = CycleCountRequest.get(cycleCountRequestId)
        if (!cycleCountRequest) {
            throw new ObjectNotFoundException(cycleCountRequestId, CycleCountRequest.class.toString())
        }

        CycleCount cycleCount = cycleCountRequest.cycleCount
        if (cycleCount) {
            deleteCycleCount(cycleCount)
        }

        cycleCountRequest.delete()
    }

    private void deleteCycleCount(CycleCount cycleCount) {
        if (!cycleCount) {
            return
        }

        cycleCount.cycleCountItems.each { it.delete() }
        cycleCount.cycleCountItems.clear()

        cycleCount.cycleCountRequest?.cycleCount = null

        cycleCount.delete()
    }


    @Transactional(readOnly=true)
    PagedResultList getCycleCountDetailsReport(CycleCountReportCommand command) {
        return CycleCountDetails.createCriteria().list(command.paginationParams) {
            if (command.facility) {
                eq("facility", command.facility)
            }
            if (command.startDate && command.endDate) {
                between("dateRecorded", command.startDate, command.endDate)
            }
            else if (command.startDate) {
                gte("dateRecorded", command.startDate)
            }
            else if (command.endDate) {
                lte("dateRecorded", command.endDate)
            }
            if (command.products) {
                "in"("product", command.products)
            }
        }
    }

    @Transactional(readOnly=true)
    PagedResultList getCycleCountSummaryReport(CycleCountReportCommand command) {
        return CycleCountSummary.createCriteria().list(command.paginationParams) {
            if (command.facility) {
                eq("facility", command.facility)
            }
            if (command.startDate && command.endDate) {
                between("dateRecorded", command.startDate, command.endDate)
            }
            else if (command.startDate) {
                gte("dateRecorded", command.startDate)
            }
            else if (command.endDate) {
                lte("dateRecorded", command.endDate)
            }
            if (command.products) {
                "in"("product", command.products)
            }
        }
    }
}
