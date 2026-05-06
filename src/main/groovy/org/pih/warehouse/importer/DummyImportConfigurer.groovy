package org.pih.warehouse.importer

import org.springframework.stereotype.Component

/**
 * TODO: Remove this once we have actual ConfiguresBulkDataBinder implementations. Without this we get startup
 *       errors when autowiring beans in the BulkDataImportComponentResolver constructor.
 */
@Component
class DummyImportConfigurer implements ConfiguresBulkDataBinder<Importable> {

    @Override
    BulkDataBinderConfig getBulkDataBinderConfig() {
        return new BulkDataBinderConfig()
    }

    @Override
    BulkDataType getBulkDataType() {
        return BulkDataType.PERSON
    }
}
