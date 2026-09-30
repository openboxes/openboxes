package org.pih.warehouse.allocation

import org.pih.warehouse.api.SuggestedItem

class AllocationResult {
    AllocationRequest allocationRequest
    List<SuggestedItem> suggestedItems

    /**
     * Stock found in a pre-pick bin. It is consumed without a pick for the picker to perform, so it is
     * kept apart from the ordinary suggestions
     */
    List<SuggestedItem> prepickItems = []
}
