package org.pih.warehouse.product

import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.DocumentType;
import org.springframework.web.multipart.MultipartFile;

class DocumentCommand {
	String name
	Product product
	String documentNumber
	String contentType
	Document document
	DocumentType documentType
	MultipartFile fileContents
 }
