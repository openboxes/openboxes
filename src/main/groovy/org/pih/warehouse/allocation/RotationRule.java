package org.pih.warehouse.allocation;

/**
 * Identifier for the stock ROTATION rule — which lot to consume first within an eligible set of bins.
 * A policy axis (not per-order): resolved from config today, eventually from a facility/product
 * cascade. Carries no behavior — the ordering is applied in AllocationService for now, and will move
 * to a RotationStrategy implementation (parallel to AllocationSourceStrategyHandler) when FIFO/LIFO
 * and friends are added.
 *
 * NONE and FEFO are honored today; FIFO and LIFO are declared extension points and currently fall
 * back to natural (source) order.
 */
public enum RotationRule {
    NONE, FEFO, FIFO, LIFO
}
