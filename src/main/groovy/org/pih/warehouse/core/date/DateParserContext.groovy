package org.pih.warehouse.core.date

/**
 * Context object containing the configuration fields for parsing in dates.
 * For a majority of cases the default settings can be used and so this context object will not be required.
 */
class DateParserContext {

    /**
     * The epoch date to use when parsing to the given date type.
     *
     * This is required for parsing dates coming from Excel files and can likely be ignored for all other scenarios.
     */
    EpochDate epochDate = EpochDate.UNIX_EPOCH
}
