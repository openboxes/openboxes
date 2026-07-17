/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.allocation

/**
 * Collects the available {@link AllocationStrategyHandler}s and keys them by {@link AllocationStrategyHandler#getStrategy()}.
 */
class AllocationStrategyHandlerResolver {

    private final Map<AllocationStrategy, AllocationStrategyHandler> handlersByStrategy

    AllocationStrategyHandlerResolver() {
        this([
                new WarehouseFirstHandler(),
                new DisplayFirstHandler(),
                new WarehouseOnlyHandler(),
                new FefoHandler(),
        ])
    }

    AllocationStrategyHandlerResolver(List<AllocationStrategyHandler> handlers) {
        handlersByStrategy = handlers.collectEntries { [(it.strategy): it] }
    }

    AllocationStrategyHandler handlerFor(AllocationStrategy strategy) {
        return handlersByStrategy[strategy]
    }
}
