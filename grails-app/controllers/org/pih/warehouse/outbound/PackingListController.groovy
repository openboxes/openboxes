package org.pih.warehouse.outbound

import grails.converters.JSON
import org.pih.warehouse.core.UploadService
import org.pih.warehouse.importer.DataImporter
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.PackingListExcelImporter
import org.springframework.web.multipart.MultipartFile

class PackingListController {

    UploadService uploadService

    def importPackingList(ImportDataCommand command) {
        MultipartFile importFile = command.importFile
        File localFile = uploadService.createLocalFile(importFile.originalFilename)
        importFile.transferTo(localFile)
        DataImporter packingListImporter = new PackingListExcelImporter(localFile.absolutePath)

        render([data: packingListImporter.toJson() ] as JSON)
    }
}
