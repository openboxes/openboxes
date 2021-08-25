package org.pih.warehouse.integration

import org.pih.warehouse.xml.acceptancestatus.AcceptanceStatus
import org.pih.warehouse.xml.pod.DocumentUpload
import org.springframework.context.ApplicationEvent

class DocumentUploadEvent extends ApplicationEvent {

    DocumentUpload documentUpload

    DocumentUploadEvent(DocumentUpload documentUpload) {
        super(documentUpload)
        this.documentUpload = documentUpload
    }

}
