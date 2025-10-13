package org.pih.warehouse.inventory

import java.time.Instant
import java.time.LocalDate

import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.product.Product

class CycleCountItemDto {

    String id

    Map facility

    Map binLocation

    Product product

    InventoryItem inventoryItem

    Integer countIndex

    CycleCountItemStatus status

    Integer quantityOnHand

    Integer quantityCounted

    Integer quantityVariance

    ReasonCode discrepancyReasonCode

    String comment

    Boolean custom

    LocalDate dateCounted

    Instant dateCreated

    Person assignee
}
