package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.NullPrecedence
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang.StringEscapeUtils
import org.grails.datastore.mapping.query.api.Criteria
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

            if (!command.statuses) {
                isNull("status")
            }
            else {
                inList("status", command.statuses)
            }

            if (command.negativeQuantity) {
                gt("negativeItemCount", 0)
            }

            // FIXME It's possible we need this to be "ne" rather "gt" because we probably want to include
            //  product/facility pairs where quantity on hand is negative.
            // Moved this from the cycle count session view since it's a requirement of the candidate query,
            // not the cycle count session.
            gt("quantityOnHand", 0)

            // FIXME This should only be used when querying for candidates, but right now we only have a single
            //  candidates query for all of the subsets of the session (counted, ready to be counted, counting).
            // This will filter out cycle count session records where a count has been completed in the required
            // frequency internal (i.e. this helps reduce the results returned in the All Products tab).
            lte("daysUntilNextCount", 0)

            // FIXME Sort order should allow multiple sort order rules ("columna, -columnb"). We should consider
            //  using a more conventional syntax for the column and direction i.e. "columna" sorts "columna" in
            //  ascending order while "-columnb" sorts "columnb" in descending order.
            // Don't check command.sort because we want the default case to be applied if there's no sort order
            applySortOrder(command.sort, command.order, delegate, usedAliases)

        } as List<CycleCountCandidate>
    }

    private static void applySortOrder(String sortBy, String orderDirection, Criteria criteria, Set<String> usedAliases) {
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
                // FIXME We could potentially handle this in the future by making ABC class a first-class citizen
                //  and allowing it to be configured with a sort value, the default sort value in cases of NULL would
                //  be some large integer value (999).
                // The default sort order for the cycle count session (i.e. at least for the All Products tab) should
                // be by ABC class, then by days until next count. The first order guarantees that NULL abc classes
                // are sorted last.
                criteria.order(Order.asc("abcClass").nulls(NullPrecedence.LAST))
                        .order("daysUntilNextCount", "asc")
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
     * Batch method to start a cycle count.
     * Fetch associated Cycle count with the CycleCountRequest and do the following:
     * if a cycle count doesn't exist, create it, fetch all bin locations + inventory items for the product in a request,
     * get the product availability for each item, filter out items that have qoh = 0 (but keep those that have qoh = 0 and were included in the previous count)
     *
     * */
    List<CycleCountDto> startCycleCount(CycleCountStartBatchCommand command) {
        List<CycleCountDto> cycleCounts = []
        command.requests.each { CycleCountStartCommand request ->
            CycleCount cycleCount = request.cycleCountRequest.cycleCount
            if (!cycleCount) {
                CycleCountDto cycleCountDto = createCycleCount(request, command.facility)
                cycleCounts.add(cycleCountDto)
                return
            }
            List<CycleCountItem> cycleCountItems = CycleCountItem.findAllByCycleCount(cycleCount)
            CycleCountDto cycleCountDto = updateCycleCount(cycleCount, cycleCountItems, request)
            cycleCounts.add(cycleCountDto)
        }
        return cycleCounts
    }

    CycleCountDto createCycleCount(CycleCountStartCommand request, Location facility) {
        CycleCount newCycleCount = new CycleCount(
                facility: facility,
                // Set an initial status here so that validation passes. It gets automatically recomputed on save.
                status: CycleCountStatus.REQUESTED,
                dateLastRefreshed: new Date()
        )

        List<AvailableItem> itemsToSave = determineCycleCountItemsToSave(facility, request.cycleCountRequest.product)
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
        List<AvailableItem> availableItemsToRecount = determineCycleCountItemsToSave(facility, product)
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

    CycleCountDto updateCycleCount(CycleCount cycleCount, List<CycleCountItem> cycleCountItems, CycleCountStartCommand request) {
        if (cycleCount.status in [CycleCountStatus.READY_TO_REVIEW, CycleCountStatus.CANCELED]) {
            throw new IllegalArgumentException("Cycle count is already in review or canceled")
        }
        if (cycleCount.status in [CycleCountStatus.INVESTIGATING, CycleCountStatus.COUNTED]) {
            throw new UnsupportedOperationException("Support will be added later")
        }
        if (cycleCount.status in CycleCountStatus.listInProgress()) {
            CycleCountItem itemWithHighestCountIndex = cycleCountItems.max { it.countIndex }
            if (itemWithHighestCountIndex?.countIndex != request.countIndex) {
                throw new IllegalArgumentException("Count index can't be higher than the highest count index of the items")
            }
        }
        return CycleCountDto.toDto(cycleCount)
    }

    /**
     * Get product availability for a product and for facility.
     * Filter out items with QOH = 0 (but keep those that were counted in the previous count)
     * */
    List<AvailableItem> determineCycleCountItemsToSave(Location facility, Product product) {
        List<AvailableItem> availableItems =
                productAvailabilityService.getAvailableItems(facility, [product.id], false, true)

        return availableItems
    }

    List<CycleCountDto> getCycleCounts(List<String> ids) {
        List<CycleCount> cycleCounts = CycleCount.createCriteria().list {
            if (ids) {
                'in'("id", ids)
            }
        } as List<CycleCount>
        return cycleCounts.collect { CycleCountDto.toDto(it) }
    }

    CycleCountDto submitCount(CycleCountSubmitCountCommand command) {
        command.cycleCount.cycleCountItems.each { CycleCountItem cycleCountItem ->
            updateCycleCountItemForSubmit(cycleCountItem, command.refreshQuantityOnHand, command.failOnOutdatedQuantity)
            determineCycleCountItemStatusForSubmit(cycleCountItem, command.requireRecountOnDiscrepancy)
        }
        command.cycleCount.status = command.cycleCount.recomputeStatus()
        if (command.cycleCount.status == CycleCountStatus.READY_TO_REVIEW) {
            cycleCountTransactionService.createTransactions(command.cycleCount, command.refreshQuantityOnHand)
        }
        // TODO: The beforeUpdate() on CycleCount class is not triggered without
        // the line below, so without it status is not correct in the DB.
        // Investigate why this line is needed.
        command.cycleCount.save()
        return CycleCountDto.toDto(command.cycleCount)
    }

    private void updateCycleCountItemForSubmit(CycleCountItem cycleCountItem, boolean refreshQuantityOnHand, boolean failOnOutdatedQuantity) {
        Integer currentQuantityOnHand =
                productAvailabilityService.getQuantityOnHandInBinLocation(cycleCountItem.inventoryItem, cycleCountItem.location)
        if (failOnOutdatedQuantity && cycleCountItem.quantityOnHand != currentQuantityOnHand) {
            throw new IllegalArgumentException("Quantity on hand for a cycle count item is no longer up to date")
        }
        if (refreshQuantityOnHand) {
            // TODO: This doesn't account for any new bins/lots that have been created since the count started! We need
            //       a full QoH fetch on the product, and to create new cycle count items for any new bins/lot numbers.
            cycleCountItem.quantityOnHand = currentQuantityOnHand
        }
    }

    private void determineCycleCountItemStatusForSubmit(CycleCountItem cycleCountItem, boolean requireRecountOnDiscrepancy) {
        if ((cycleCountItem.quantityOnHand == cycleCountItem.quantityCounted) || !requireRecountOnDiscrepancy) {
            cycleCountItem.status = CycleCountItemStatus.READY_TO_REVIEW
            return
        }
        cycleCountItem.status = CycleCountItemStatus.COUNTED
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
}
