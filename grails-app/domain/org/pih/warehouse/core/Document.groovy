/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.util.Holders


/**
 * A document is a file (e.g. document, image) that can be associated with an
 * entity in the system.  Currently, users can only upload and link documents to
 * shipments.
 */
class Document implements Serializable {

    String id
    String name            // Document name (optional)
    String filename            // Document filename
    String extension        // The extension of the file
    String contentType        // The content type of the file
    byte[] fileContents        // The contents of the file (if stored in database)
    Date dateCreated        // The date the document was created
    Date lastUpdated        // The date the document was last updated

    String fileUri            // Universal Resource Identifier
    String documentNumber        // Document reference number
    DocumentType documentType    // Type of document

    // Documents should exist on their own in case we want to tie them to other objects.
    // Shipment (and other entities) should create a join table for documents.
    static mapping = {
        id generator: 'uuid'
    }

    static transients = ["size", "image", 'link']

    static constraints = {
        name(nullable: true, maxSize: 255)
        filename(nullable: true, maxSize: 255)
        fileContents(nullable: true)
        extension(nullable: true, maxSize: 255)
        contentType(nullable: true, maxSize: 255)
        fileUri(uri: true, nullable: true)
        fileContents(nullable: true, maxSize: 10485760) // 10 MBs
        documentNumber(nullable: true, maxSize: 255)
        documentType(nullable: true)
    }

    String toString() { return "$name" }

    Integer getSize() { return fileContents?.length ?: 0 }

    /**
     *
     * @return
     */
    boolean isImage() {
        return contentType?.startsWith("image/")
    }

    static List findAllByDocumentCode(DocumentCode documentCode) {
        List<DocumentType> documentTypes = DocumentType.findAllByDocumentCode(documentCode)
        return documentTypes ? Document.findAllByDocumentTypeInList(documentTypes) : []
    }

    def getLink() {
        def g = Holders.grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        return fileUri ?: g.createLink(controller: 'document', action: "download", id: id, absolute: true)
    }

    Map toJson() {
        return [
                id          : id,
                name        : name,
                link        : link,
                fileUri     : fileUri,
                filename    : filename,
                documentType: documentType,
                size        : size,
                lastUpdated : lastUpdated,
        ]
    }

    static namedQueries = {
        listAllByDocumentCode { DocumentCode documentCode ->
            documentType {
                eq("documentCode", documentCode)
            }
        }
        listBarcodeTemplates { String documentNumber ->
            documentType {
                eq("documentCode", DocumentCode.ZEBRA_TEMPLATE)
            }
            eq("documentNumber", documentNumber)
        }

    }
}
