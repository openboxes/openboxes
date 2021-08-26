package org.pih.warehouse.integration

import org.pih.warehouse.integration.xml.pod.DocumentUpload
import org.springframework.context.ApplicationEvent

class DocumentUploadEvent extends ApplicationEvent {

    DocumentUpload documentUpload

    DocumentUploadEvent(DocumentUpload documentUpload) {
        super(documentUpload)
        this.documentUpload = documentUpload
    }

}
