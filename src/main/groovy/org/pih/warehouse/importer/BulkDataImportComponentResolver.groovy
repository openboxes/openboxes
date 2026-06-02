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

    // Components are wrapped with optional to avoid an error when no implementations are defined.
    BulkDataImportComponentResolver(final Optional<List<ConfiguresBulkDataBinder>> bulkDataBinderConfigurers) {
        populateBulkDataBinderConfigMap(bulkDataBinderConfigurers.orElse([]))
    }

    private void populateBulkDataBinderConfigMap(List<ConfiguresBulkDataBinder> bulkDataBinderConfigurers) {
        for (bulkDataBinderConfigurer in bulkDataBinderConfigurers) {
            BulkDataType bulkDataType = bulkDataBinderConfigurer.bulkDataType
            if (bulkDataBinderConfigurersByDataType.containsKey(bulkDataType)) {
                throw new RuntimeException("Found multiple bulk data binder configurers for data type ${bulkDataType}. Only one is allowed.")
            }
            bulkDataBinderConfigurersByDataType.put(bulkDataType, bulkDataBinderConfigurer)
        }
    }

    /**
     * @return the bulk data binder configurer associated with the given data type.
     */
    ConfiguresBulkDataBinder getBulkDataBinderConfigurer(BulkDataType dataImportType) {
        return dataImportType ? bulkDataBinderConfigurersByDataType.get(dataImportType) : null
    }
}
