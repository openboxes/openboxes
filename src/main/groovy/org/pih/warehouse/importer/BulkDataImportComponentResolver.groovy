package org.pih.warehouse.importer

import org.apache.commons.collections4.map.MultiKeyMap
import org.springframework.stereotype.Component

import org.pih.warehouse.core.http.ContentType

/**
 * A convenience component for fetching the configurer components associated with the data type being imported.
 *
 * This saves us from needing to manually maintain a collection of all the configurer components and avoids an
 * ugly switch statement when resolving those components during the generic import flow.
 */
@Component
class BulkDataImportComponentResolver {

    private final HashMap<ContentType, BulkDataReader> bulkDataReaderByContentType = [:]
    private final MultiKeyMap<Object, ConfiguresBulkDataReader> bulkDataReaderConfigurersByContentTypeAndBulkDataType =
            new MultiKeyMap<>()
    private final HashMap<BulkDataType, ConfiguresBulkDataBinder> bulkDataBinderConfigurersByDataType = [:]
    private final HashMap<BulkDataType, ConfiguresBulkDataValidator> bulkDataValidatorConfigurersByDataType = [:]

    // Components are wrapped with optional to avoid an error when no implementations are defined.
    BulkDataImportComponentResolver(final Optional<List<BulkDataReader>> bulkDataReaders,
                                    final Optional<List<ConfiguresBulkDataReader>> bulkDataReaderConfigurers,
                                    final Optional<List<ConfiguresBulkDataBinder>> bulkDataBinderConfigurers,
                                    final Optional<List<ConfiguresBulkDataValidator>> bulkDataValidatorConfigurers) {
        populateBulkDataReaderMap(bulkDataReaders.orElse([]))
        populateBulkDataReaderConfigMap(bulkDataReaderConfigurers.orElse([]))
        populateBulkDataBinderConfigMap(bulkDataBinderConfigurers.orElse([]))
        populateBulkDataValidatorConfigMap(bulkDataValidatorConfigurers.orElse([]))
    }

    private void populateBulkDataReaderMap(List<BulkDataReader> bulkDataReaders) {
        for (bulkDataReader in bulkDataReaders) {
            for (contentType in bulkDataReader.supportedContentTypes) {
                if (bulkDataReaderByContentType.containsKey(contentType)) {
                    throw new RuntimeException("Found multiple bulk data readers for content type [${contentType}]: " +
                            "[${bulkDataReader.class}, ${bulkDataReaderByContentType.get(contentType).class}]. " +
                            "Only one is allowed.")
                }
                bulkDataReaderByContentType.put(contentType, bulkDataReader)
            }
        }
    }

    /**
     * @return the bulk data reader associated with the given content type.
     */
    BulkDataReader getBulkDataReader(ContentType contentType) {
        return bulkDataReaderByContentType.get(contentType)
    }

    private void populateBulkDataReaderConfigMap(List<ConfiguresBulkDataReader> bulkDataReaderConfigurers) {
        for (bulkDataReaderConfigurer in bulkDataReaderConfigurers) {
            BulkDataType bulkDataType = bulkDataReaderConfigurer.bulkDataType
            for (contentType in bulkDataReaderConfigurer.supportedContentTypes) {
                if (bulkDataReaderConfigurersByContentTypeAndBulkDataType.containsKey(contentType, bulkDataType)) {
                    throw new RuntimeException("Found multiple bulk data reader configurers for content type " +
                            "[${contentType}] and bulk data type [${bulkDataType}]: " +
                            "[${bulkDataReaderConfigurer.class}, " +
                            "${bulkDataReaderConfigurersByContentTypeAndBulkDataType.get(contentType).class}]. " +
                            "Only one is allowed.")
                }
                bulkDataReaderConfigurersByContentTypeAndBulkDataType.put(
                        contentType, bulkDataType, bulkDataReaderConfigurer)
            }
        }
    }

    /**
     * @return the bulk data reader configurer associated with the given content type.
     */
    ConfiguresBulkDataReader getBulkDataReaderConfigurer(ContentType contentType, BulkDataType bulkDataType) {
        return bulkDataReaderConfigurersByContentTypeAndBulkDataType.get(contentType, bulkDataType)
    }

    private void populateBulkDataBinderConfigMap(List<ConfiguresBulkDataBinder> bulkDataBinderConfigurers) {
        for (bulkDataBinderConfigurer in bulkDataBinderConfigurers) {
            BulkDataType bulkDataType = bulkDataBinderConfigurer.bulkDataType
            if (bulkDataBinderConfigurersByDataType.containsKey(bulkDataType)) {
                throw new RuntimeException("Found multiple bulk data binder configurers for data type " +
                        "[${bulkDataType}]: [${bulkDataBinderConfigurer.class}, " +
                        "${bulkDataBinderConfigurersByDataType.get(bulkDataType).class}]. Only one is allowed.")
            }
            bulkDataBinderConfigurersByDataType.put(bulkDataType, bulkDataBinderConfigurer)
        }
    }

    /**
     * @return the bulk data binder configurer associated with the given data type.
     */
    ConfiguresBulkDataBinder getBulkDataBinderConfigurer(BulkDataType dataImportType) {
        return bulkDataBinderConfigurersByDataType.get(dataImportType)
    }

    private void populateBulkDataValidatorConfigMap(List<ConfiguresBulkDataValidator> bulkDataValidatorConfigurers) {
        for (bulkDataValidatorConfigurer in bulkDataValidatorConfigurers) {
            BulkDataType bulkDataType = bulkDataValidatorConfigurer.bulkDataType
            if (bulkDataValidatorConfigurersByDataType.containsKey(bulkDataType)) {
                throw new RuntimeException("Found multiple bulk data validator configurers for data type " +
                        "[${bulkDataType}]: [${bulkDataValidatorConfigurer.class}, " +
                        "${bulkDataValidatorConfigurersByDataType.get(bulkDataType).class}]. Only one is allowed.")
            }
            bulkDataValidatorConfigurersByDataType.put(bulkDataType, bulkDataValidatorConfigurer)
        }
    }

    /**
     * @return the bulk data validator configurer associated with the given data type.
     */
    ConfiguresBulkDataValidator getBulkDataValidatorConfigurer(BulkDataType dataImportType) {
        return bulkDataValidatorConfigurersByDataType.get(dataImportType)
    }
}
