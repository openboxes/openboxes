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
    private final HashMap<BulkDataType, ConfiguresBulkDataValidator> bulkDataValidatorConfigurersByDataType = [:]

    // Components are wrapped with optional to avoid an error when no implementations are defined.
    BulkDataImportComponentResolver(final Optional<List<ConfiguresBulkDataBinder>> bulkDataBinderConfigurers,
                                    final Optional<List<ConfiguresBulkDataValidator>> bulkDataValidatorConfigurers) {
        populateBulkDataBinderConfigMap(bulkDataBinderConfigurers.orElse([]))
        populateBulkDataValidatorConfigMap(bulkDataValidatorConfigurers.orElse([]))
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

    private void populateBulkDataValidatorConfigMap(List<ConfiguresBulkDataValidator> bulkDataValidatorConfigurers) {
        for (bulkDataValidatorConfigurer in bulkDataValidatorConfigurers) {
            BulkDataType bulkDataType = bulkDataValidatorConfigurer.bulkDataType
            if (bulkDataValidatorConfigurersByDataType.containsKey(bulkDataType)) {
                throw new RuntimeException("Found multiple bulk data validator configurers for data type ${bulkDataType}. Only one is allowed.")
            }
            bulkDataValidatorConfigurersByDataType.put(bulkDataType, bulkDataValidatorConfigurer)
        }
    }

    /**
     * @return the bulk data validator configurer associated with the given data type.
     */
    ConfiguresBulkDataValidator getBulkDataValidatorConfigurer(BulkDataType dataImportType) {
        return dataImportType ? bulkDataValidatorConfigurersByDataType.get(dataImportType) : null
    }
}
