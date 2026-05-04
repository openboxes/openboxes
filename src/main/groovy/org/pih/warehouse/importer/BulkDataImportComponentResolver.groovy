package org.pih.warehouse.importer

import org.springframework.stereotype.Component

/**
 * A convenience component for fetching the configurer components associated with the data type being imported.
 *
 * This saves us from needing to manually maintain a collection of all the configurer components and avoids an
 * ugly switch statement when resolving those components during the generic import flow.
 */
@Component
class BulkDataImportComponentResolver {

    private final HashMap<BulkDataType, ConfiguresBulkDataBinder> bulkDataBinderConfigurersByDataType = [:]

    BulkDataImportComponentResolver(final List<ConfiguresBulkDataBinder> bulkDataBinderConfigurers) {
        bulkDataBinderConfigurers.each { bulkDataBinderConfigurersByDataType.put(it.bulkDataType, it) }
    }

    /**
     * @return the bulk data binder configurer associated with the given data type.
     */
    ConfiguresBulkDataBinder getBulkDataBinderConfigurer(BulkDataType dataImportType) {
        return bulkDataBinderConfigurersByDataType.get(dataImportType)
    }
}
