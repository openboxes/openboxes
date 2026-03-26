package org.pih.warehouse.core

import grails.validation.Validateable
import org.pih.warehouse.api.PaginationCommand

class DocumentFilterCommand extends PaginationCommand {
    String q
    DocumentType documentType
    String sort
    String order
}
