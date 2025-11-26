package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class CycleCountItem implements Comparable {

    String id

    Location facility

    CycleCount cycleCount

    InventoryItem inventoryItem

    // Bin location
    Location location

    Product product

    Person assignee

    Integer countIndex

    CycleCountItemStatus status

    Integer quantityOnHand

    Integer quantityCounted

    ReasonCode discrepancyReasonCode

    String comment

    Boolean custom

    Date dateCounted

    Date dateCreated

    Date lastUpdated

    User createdBy

    User updatedBy

    CycleCountItemDto toDto() {
        return new CycleCountItemDto(
                id: id,
                facility: facility.toBaseJson(),
                product: product,
                inventoryItem: inventoryItem,
                binLocation: Location.toJson(location),
                countIndex: countIndex,
                status: status,
                quantityOnHand: quantityOnHand,
                quantityCounted: quantityCounted,
                quantityVariance: quantityVariance,
                discrepancyReasonCode: discrepancyReasonCode,
                dateCounted: dateCounted,
                dateCreated: dateCreated,
                comment: comment,
                custom: custom,
                assignee: assignee,
        )
    }

    static constraints = {
        dateCounted(nullable: true)
        comment(nullable: true)
        assignee(nullable: true)
        quantityCounted(nullable: true)
        discrepancyReasonCode(nullable: true, validator: { ReasonCode discrepancyReasonCode ->
            if (!discrepancyReasonCode) {
                return true
            }

            return ReasonCode.listCycleCountReasonCodes().contains(discrepancyReasonCode) ? true : ['invalid']
        })
        location(nullable: true)
    }

    static namedQueries = {
        countCompletedCycleCounts { Location facility, Product product, Date startDate, Date endDate ->
            createAlias "cycleCount", "cycleCount"
            projections {
                countDistinct('cycleCount.id', 'countCycleCounts')
            }
            eq 'product', product
            eq 'cycleCount.facility', facility
            eq 'cycleCount.status', CycleCountStatus.COMPLETED
            between 'dateCounted', startDate, endDate
        }

        dateLastCounted { Location facility, Product product ->
            createAlias 'cycleCount', 'cycleCount'
            projections {
                max('dateCounted', 'dateLastCounted')
            }
            eq 'product', product
            eq 'cycleCount.facility', facility
            eq 'cycleCount.status', CycleCountStatus.COMPLETED
        }
    }

    Integer getQuantityVariance() {
        if (quantityCounted != null && quantityOnHand != null) {
            return quantityCounted - quantityOnHand
        }
        return null
    }

    @Override
    int compareTo(Object that) {
        int diff = inventoryItem?.expirationDate <=> that.inventoryItem?.expirationDate
                ?: location?.name <=> that.location?.name
                    // This id comparison is required due to a quirk in SortedSet (CycleCount has a SortedSet of
                    // CycleCountItem) where it uses compareTo as an equals method when adding/removing from the set,
                    // and so we need to make sure this always resolves to a unique value for each item. (OBPIH-7128)
                    ?: id <=> that.id
                        // The identityHashCode is needed in case of adding a new item that doesn't have the id yet.
                        // There is a case where you put the same expirationDate and location, and due to the SortedSet behavior
                        // this would assume the items are the same and it wouldn't add more than one with the same expirationDate and location
                        ?: System.identityHashCode(this) <=> System.identityHashCode(that)
        return diff
    }

    Map toJson() {
        return [
                id: id,
                facility: facility.toBaseJson(),
                product: product,
                inventoryItem: inventoryItem,
                binLocation: location?.toBaseJson(),
                countIndex: countIndex,
                status: status,
                quantityOnHand: quantityOnHand,
                quantityCounted: quantityCounted,
                quantityVariance: quantityVariance,
                discrepancyReasonCode: discrepancyReasonCode,
                dateCounted: dateCounted,
                dateCreated: dateCreated,
                comment: comment,
                custom: custom,
                assignee: assignee,
        ]
    }
}
