package org.pih.warehouse.allocation;

/**
 * Identifier for the allocation SOURCE strategy — which bins are eligible and in what zone order.
 * Per-order value, persisted on the requisition and set by the integration. Carries no behavior;
 * the ordering logic lives in the {@link AllocationSourceStrategyHandler} implementations.
 * Rotation (FEFO/FIFO/...) is a separate axis, see {@link RotationRule}.
 *
 * Named to leave room for a future {@code ReplenishmentSourceStrategy}, etc.
 */
public enum AllocationSourceStrategy {
    STORAGE_FIRST, DISPLAY_FIRST, STORAGE_ONLY
}
