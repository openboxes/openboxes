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

import groovy.util.logging.Slf4j

@Slf4j
class RotationStrategyResolver {

    private final Map<RotationRule, RotationStrategy> strategiesByRule
    private final RotationStrategy fallback

    RotationStrategyResolver() {
        this([
                new FefoRotationStrategy(),
                new NoneRotationStrategy(),
        ])
    }

    RotationStrategyResolver(List<RotationStrategy> strategies) {
        strategiesByRule = strategies.collectEntries { [(it.rule): it] }
        fallback = strategiesByRule[RotationRule.NONE] ?: new NoneRotationStrategy()
    }

    RotationStrategy forRule(RotationRule rule) {
        RotationStrategy strategy = strategiesByRule[rule]
        if (strategy) {
            return strategy
        }
        log.warn("No rotation strategy registered for ${rule}, defaulting to NONE (no rotation)")
        return fallback
    }
}
