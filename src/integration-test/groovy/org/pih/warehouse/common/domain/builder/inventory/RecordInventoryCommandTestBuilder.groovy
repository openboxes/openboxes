package org.pih.warehouse.common.domain.builder.inventory

import java.time.Instant

import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.RecordInventoryCommand
import org.pih.warehouse.inventory.RecordInventoryRowCommand
import org.pih.warehouse.product.Product

/**
 * A convenience builder on RecordInventoryCommand to make it easier for tests to perform record stock operations.
 */
class RecordInventoryCommandTestBuilder {

    RecordInventoryCommand cmd

    RecordInventoryCommandTestBuilder() {
        cmd = new RecordInventoryCommand()

        // RecordInventoryCommand defaults transactionDate to now, but for tests we want to be explicit about
        // the date of the record stock operation to avoid any confusion. Plus if we use the current datetime,
        // we'll hit errors about the adjustment transaction (which is transactionDate + 1) being in the future.
        cmd.transactionDate = null
    }

    RecordInventoryCommandTestBuilder product(Product product) {
        cmd.product = product
        return this
    }

    RecordInventoryCommandTestBuilder inventory(Inventory inventory) {
        cmd.inventory = inventory
        return this
    }

    RecordInventoryCommandTestBuilder transactionDate(Date transactionDate) {
        cmd.transactionDate = transactionDate
        return this
    }

    RecordInventoryCommandTestBuilder transactionDate(Instant transactionDate) {
        cmd.transactionDate = DateUtil.asDate(transactionDate)
        return this
    }

    RecordInventoryCommandTestBuilder transactionDateNow() {
        // "Now" is actually one second ago. We do this because the resulting adjustment transaction will be
        // +1 second from this date, so if we used the real "now" it'd throw errors about being in the future.
        return transactionDate(Instant.now().minusSeconds(1))
    }

    RecordInventoryCommandTestBuilder comment(String comment) {
        cmd.comment = comment
        return this
    }

    RecordInventoryCommandTestBuilder row(String lotNumber, Date expirationDate, Location binLocation, int quantity) {
        RecordInventoryRowCommand rowCommand = new RecordInventoryRowCommand()
        rowCommand.lotNumber = lotNumber
        rowCommand.expirationDate = expirationDate
        rowCommand.binLocation = binLocation
        rowCommand.oldQuantity = 0  // This field is mandatory but unused so don't bother setting an accurate value
        rowCommand.newQuantity = quantity
        return row(rowCommand)
    }

    RecordInventoryCommandTestBuilder row(RecordInventoryRowCommand rowCommand) {
        cmd.recordInventoryRows.add(rowCommand)
        return this
    }

    RecordInventoryCommand build() {
        return cmd
    }
}
