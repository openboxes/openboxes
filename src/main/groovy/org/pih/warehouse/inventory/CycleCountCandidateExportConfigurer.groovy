package org.pih.warehouse.inventory

import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.DateFieldType
import org.pih.warehouse.core.file.FileNamePart
import org.pih.warehouse.exporter.BulkDataExportConfig
import org.pih.warehouse.exporter.BulkDataExportConfigurer
import org.pih.warehouse.importer.BulkDataType

/**
 * Configuration for exporting a list of {@link CycleCountCandidate}.
 *
 * The rows are produced by {@link CycleCountCandidateMapper#serializeTabular}.
 */
@Component
class CycleCountCandidateExportConfigurer extends BulkDataExportConfigurer {

    @Override
    BulkDataType getBulkDataType() {
        return BulkDataType.CYCLE_COUNT_CANDIDATE
    }

    @Override
    protected BulkDataExportConfig getExportConfig() {
        return configBuilder()
                .fileName("CycleCountReport", FileNamePart.CURRENT_TIME)
                .field("productCode", "cycleCountCandidate.export.productCode.label")
                .field("productName", "cycleCountCandidate.export.productName.label")
                .field("productFamily", "cycleCountCandidate.export.productFamily.label")
                .field("category", "cycleCountCandidate.export.category.label")
                .field("productCatalogs", "cycleCountCandidate.export.productCatalogs.label")
                .field("abcClass", "cycleCountCandidate.export.abcClass.label")
                .field("binLocations", "cycleCountCandidate.export.binLocations.label")
                .field("tags", "cycleCountCandidate.export.tags.label")
                .dateField("lastCounted", "cycleCountCandidate.export.lastCounted.label", DateFieldType.LOCAL_DATE)
                .field("quantityOnHand", "cycleCountCandidate.export.quantityOnHand.label")
                .build()
    }
}
