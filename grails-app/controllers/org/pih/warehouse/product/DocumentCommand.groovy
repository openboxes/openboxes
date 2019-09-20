/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentType
import org.springframework.web.multipart.MultipartFile

class DocumentCommand {
    String name
    Product product
    String documentNumber
    String contentType
    Document document
    DocumentType documentType
    MultipartFile fileContents
}
