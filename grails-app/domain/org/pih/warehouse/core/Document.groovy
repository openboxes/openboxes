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
import groovy.util.logging.Slf4j

import org.pih.warehouse.core.http.ContentType

import util.FileUtil


/**
 * A document is a file (e.g. document, image) that can be associated with an
 * entity in the system.  Currently, users can only upload and link documents to
 * shipments.
 */
@Slf4j
class Document implements Serializable {

    private static final List<ContentType> ALLOWED_CONTENT_TYPES = [
            ContentType.CSV,
            ContentType.DOCX,
            ContentType.GIF,
            ContentType.JPEG,
            ContentType.JPG,
            ContentType.PDF,
            ContentType.PNG,
            ContentType.TXT,
            ContentType.WEBP,
            ContentType.XLS,
            ContentType.XLSX,
    ]

    /**
     * Check whether the file extension, MIME type, and (where possible)
     * file content all agree on a file type we allow in our system.
     *
     * First we check the (extension, media type) pair against an allowlist.
     * If an InputStream is provided, we also sniff the file's magic bytes
     * via URLConnection.guessContentTypeFromStream. If the sniffed type
     * contradicts the other type data provided, the file is rejected.
     *
     * Byte-sniffing only works for certain types (images, HTML, XML).
     * For types like PDF, CSV, and XLSX, guessContentTypeFromStream
     * returns null so we can only do the extension/mediaType check.
     */
    static boolean isAllowedFile(String filename, String mediaType, InputStream content = null) {
        if (!filename || !mediaType) {
            return false
        }
        String extension = FileUtil.getExtension(filename)?.toLowerCase()
        boolean matchesAllowlist = ALLOWED_CONTENT_TYPES.any { ContentType ct ->
            ct.fileExtension.extension == extension &&
                    ct.mediaType.toString() == mediaType.toLowerCase()
        }
        if (!matchesAllowlist) {
            return false
        }
        if (content != null) {
            BufferedInputStream buffered = new BufferedInputStream(content)
            try {
                String sniffed = URLConnection.guessContentTypeFromStream(buffered)
                if (sniffed != null && sniffed.toLowerCase() != mediaType.toLowerCase()) {
                    return false
                }
            } catch (IOException e) {
                log.warn("Rejecting upload of ${filename}: could not read stream to verify content type", e)
                return false
            } finally {
                buffered.close()
            }
        }
        return true
    }

    static Set<String> allowedExtensions() {
        return ALLOWED_CONTENT_TYPES.collect { it.fileExtension.extension } as Set
    }

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
        cache true
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
    }
}
