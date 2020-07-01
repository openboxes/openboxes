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

    URI fileUri            // Universal Resource Identifier
    String documentNumber        // Document reference number
    DocumentType documentType    // Type of document

    // Documents should exist on their own in case we want to tie them to other objects.
    // Shipment (and other entities) should create a join table for documents.
    static mapping = {
        id generator: 'uuid'
        cache true
    }

    static transients = ["size", "image"]

    static constraints = {
        name(nullable: true, maxSize: 255)
        filename(nullable: true, maxSize: 255)
        fileContents(nullable: true)
        extension(nullable: true, maxSize: 255)
        contentType(nullable: true, maxSize: 255)
        fileUri(nullable: true)
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
        return Document.findAllByDocumentTypeInList(documentTypes)
    }

}
