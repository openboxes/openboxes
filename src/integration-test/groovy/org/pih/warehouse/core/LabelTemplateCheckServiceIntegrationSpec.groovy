package org.pih.warehouse.core

import grails.gorm.transactions.Rollback
import org.pih.warehouse.common.base.IntegrationSpec

/**
 * The startup check finds the stored Zebra templates on a real database.
 */
@Rollback
class LabelTemplateCheckServiceIntegrationSpec extends IntegrationSpec {

    LabelTemplateCheckService labelTemplateCheckService

    void 'checkStoredTemplates reports a stored Zebra template that uses ?. and ignores other documents'() {
        given:
        DocumentType zebra = DocumentType.findByDocumentCode(DocumentCode.ZEBRA_TEMPLATE) ?:
                new DocumentType(name: "Zebra template", documentCode: DocumentCode.ZEBRA_TEMPLATE).save(failOnError: true)
        DocumentType other = DocumentType.findByDocumentCode(DocumentCode.SHIPPING_TEMPLATE) ?:
                new DocumentType(name: "Shipping template", documentCode: DocumentCode.SHIPPING_TEMPLATE).save(failOnError: true)
        new Document(name: "it-good-label", documentNumber: "it:good", filename: "good.zpl", contentType: "text/plain",
                fileContents: '^XA^FD${product.productCode}^FS^XZ'.bytes, documentType: zebra).save(failOnError: true, flush: true)
        new Document(name: "it-old-label", documentNumber: "it:old", filename: "old.zpl", contentType: "text/plain",
                fileContents: '^XA^FD${container?.name}^FS^XZ'.bytes, documentType: zebra).save(failOnError: true, flush: true)
        new Document(name: "it-other", documentNumber: "it:other", filename: "other.txt", contentType: "text/plain",
                fileContents: '${a?.b}'.bytes, documentType: other).save(failOnError: true, flush: true)

        when:
        List<String> warnings = labelTemplateCheckService.checkStoredTemplates()

        then:
        warnings.any { it.contains("Label template 'it-old-label' (it:old)") && it.contains("uses ?.") }
        !warnings.any { it.contains("it-good-label") || it.contains("it-other") }
    }
}
