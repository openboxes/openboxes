/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.integration

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.RoleType
import org.springframework.context.ApplicationListener

class DocumentUploadEventService implements ApplicationListener<DocumentUploadEvent>  {

    boolean transactional = true

    def stockMovementService
    def notificationService

    void onApplicationEvent(DocumentUploadEvent documentUploadEvent) {
        log.info "Document upload: " + documentUploadEvent.documentUpload

        String documentType = documentUploadEvent.documentUpload.documentType
        String fileName = documentUploadEvent.documentUpload.uploadDetails.documentName
        String fileContents = documentUploadEvent.documentUpload.uploadDetails.documentFile

        String trackingNumber = documentUploadEvent.documentUpload.orderId
        if (trackingNumber) {
            log.info "Looking up stock movement by tracking number ${trackingNumber}"
            StockMovement stockMovement = stockMovementService.findByTrackingNumber(trackingNumber)
            if (stockMovement) {
                log.info "Attaching document ${fileName} to ${stockMovement.identifier}"
                stockMovementService.attachDocument(stockMovement, fileName, fileContents)
            }
        }
    }
}
