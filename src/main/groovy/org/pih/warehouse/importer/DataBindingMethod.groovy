package org.pih.warehouse.importer

/**
 * The approach to use when binding a field during the bulk data binding flow.
 */
enum DataBindingMethod {
    /**
     * The field will be automatically bound via the common data binder logic.
     */
    AUTO,

    /**
     * The field will be ignored by the data binder. As such, it is expected that the field will be
     * manually bound by the configurer during the customBindData step.
     *
     * Note that if the goal is to ignore a field entirely, simply leave it out of the config.
     */
    MANUAL,
}
