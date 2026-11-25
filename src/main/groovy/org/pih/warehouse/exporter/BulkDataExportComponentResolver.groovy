package org.pih.warehouse.exporter

import org.apache.commons.collections4.map.MultiKeyMap
import org.springframework.stereotype.Component

import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.importer.BulkDataType

/**
 * Holds a map of configurer components relating to bulk data exports.
 *
 * This makes it easy to fetch the configuration to use for a given feature and content type.
 *
 * It also saves us from needing to manually maintain a collection of all the configurer components and avoids an
 * ugly switch statement when resolving those components during the generic export flow.
 */
@Component
class BulkDataExportComponentResolver {

    private final MultiKeyMap<Object, ConfiguresBulkDataWriter> writerConfigsByDataAndContentType = [:]
    private final HashMap<ContentType, BulkDataWriter> writerByContentType = [:]

    // Components are wrapped with optional to avoid an error when no implementations are defined.
    BulkDataExportComponentResolver(final Optional<List<ConfiguresBulkDataWriter>> writerConfigs,
                                    final Optional<List<BulkDataWriter>> writers) {
        populateWriterConfigMap(writerConfigs.orElse([]))
        populateWriterMap(writers.orElse([]))
    }

    private void populateWriterConfigMap(List<ConfiguresBulkDataWriter> writerConfigs) {
        for (writerConfig in writerConfigs) {
            BulkDataType bulkDataType = writerConfig.bulkDataType
            for (contentType in writerConfig.supportedContentTypes) {
                if (writerConfigsByDataAndContentType.containsKey(bulkDataType, contentType)) {
                    throw new RuntimeException("Found multiple bulk data writer configurers for data type " +
                            "${bulkDataType} and content type ${contentType}. Only one is allowed.")
                }
                writerConfigsByDataAndContentType.put(bulkDataType, contentType, writerConfig)
            }
        }
    }

    private void populateWriterMap(List<BulkDataWriter> writers) {
        for (writer in writers) {
            for (contentType in writer.supportedContentTypes) {
                if (writerByContentType.containsKey(contentType)) {
                    throw new RuntimeException("Found multiple bulk data writers for content type ${contentType}. " +
                            "Only one is allowed.")
                }
                writerByContentType.put(contentType, writer)
            }
        }
    }

    /**
     * @return The writer configuration associated with the given feature and content type.
     */
    BulkDataWriterConfig getBulkDataWriterConfig(BulkDataType bulkDataType, ContentType contentType) {
        writerConfigsByDataAndContentType.get(bulkDataType, contentType)?.getBulkDataWriterConfig(contentType)
    }

    /**
     * @return The writer associated with the given content type.
     */
    BulkDataWriter getBulkDataWriter(ContentType contentType) {
        writerByContentType.get(contentType)
    }
}
