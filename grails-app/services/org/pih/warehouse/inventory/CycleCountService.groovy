package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
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
        // We don't set the status on the cycle count here because it will be done automatically upon save.
        CycleCount newCycleCount = new CycleCount(
                facility: facility,
                dateLastRefreshed: new Date()
        )

        List<AvailableItem> itemsToSave = determineCycleCountItemsToSave(facility, request.cycleCountRequest.product)
        // 1:1 association between cycle count and cycle count request
        request.cycleCountRequest.cycleCount = newCycleCount
        request.cycleCountRequest.status = CycleCountRequestStatus.IN_PROGRESS
        itemsToSave.each { AvailableItem availableItem ->
            CycleCountItem cycleCountItem = new CycleCountItem(
                    status: CycleCountItemStatus.READY_TO_COUNT,
                    countIndex: request.countIndex,
                    quantityOnHand: availableItem.quantityOnHand,
                    quantityCounted: availableItem.quantityOnHand == 0 ? 0 : null,
                    cycleCount: newCycleCount,
                    facility: facility,
                    inventoryItem: availableItem.inventoryItem,
                    product: availableItem.inventoryItem.product,
                    createdBy: AuthService.currentUser,
                    updatedBy: AuthService.currentUser,
                    custom: false,
            )
            newCycleCount.addToCycleCountItems(cycleCountItem)
        }

        if(!newCycleCount.save()) {
            throw new ValidationException("Invalid cycle count", newCycleCount.errors)
        }

        return CycleCountDto.asDto(newCycleCount)
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
        return CycleCountDto.asDto(cycleCount)
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
        return cycleCounts.collect { CycleCountDto.asDto(it) }
    }
}
