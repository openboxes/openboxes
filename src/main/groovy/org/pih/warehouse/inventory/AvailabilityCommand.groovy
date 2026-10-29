package org.pih.warehouse.inventory

import org.pih.warehouse.core.validation.ObjectValidatable

/**
 * The product lots whose availability is being read.
 */
class AvailabilityCommand implements ObjectValidatable<AvailabilityCommandValidator> {

    List<ProductLotRequest> productLots = []
}
