package org.pih.warehouse.core.identification

/**
 * Used to define when randomness should be applied when generating identifiers
 */
enum RandomCondition {
    /**
     * Always apply the ${random} block (if there is one) when generating an identifier.
     */
    ALWAYS,

    /**
     * Apply the ${random} block (if there is one) when generating an identifier only if duplicates of the
     * identifier already exist with no randomness applied.
     */
    ON_DUPLICATE,
}
