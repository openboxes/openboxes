/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.Stocklist
import org.pih.warehouse.core.Attachment
import org.pih.warehouse.requisition.Requisition
import grails.validation.ValidationException
import org.apache.commons.lang.StringEscapeUtils


@Transactional
class StocklistService {

    def requisitionService
    def locationService
    def mailService
    def pdfRenderingService
    def documentService
    GrailsApplication grailsApplication

    Stocklist getStocklist(String id) {
        Requisition requisition = Requisition.findByIdAndIsTemplate(id, true)

        if (!requisition) {
            return null
        }

        return Stocklist.createFromRequisition(requisition)
    }

    Stocklist createStocklist(Stocklist stocklist) {
        Requisition requisition = new Requisition()
        requisition.isTemplate = true
        requisition.name = stocklist.name
        requisition.destination = stocklist.destination
        requisition.origin = stocklist.origin
        requisition.requestedBy = stocklist.requestedBy

        requisition = requisitionService.saveTemplateRequisition(requisition)

        return Stocklist.createFromRequisition(requisition)
    }

    Stocklist updateStocklist(Stocklist stocklist) {
        Requisition requisition = stocklist.requisition
        requisition.name = stocklist.name
        requisition.destination = stocklist.destination
        requisition.origin = stocklist.origin
        requisition.requestedBy = stocklist.requestedBy

        requisition = requisitionService.saveTemplateRequisition(requisition)

        return Stocklist.createFromRequisition(requisition)
    }

    void deleteStocklist(String id) {
        Requisition requisition = Requisition.findByIdAndIsTemplate(id, true)
        if (!requisition) {
            throw new IllegalArgumentException("No Stock List found with ID ${id}")
        }

        requisitionService.deleteRequisition(requisition)
    }

    void sendMail(String stocklistId, String subject, String body, Collection to, Boolean includePdf, Boolean includeXls) {
        Stocklist stocklist = getStocklist(stocklistId)
        List<Attachment> attachments = []

        if (includeXls) {
            OutputStream os = new ByteArrayOutputStream()
            documentService.generateStocklistCsv(os, stocklist)
            Attachment attachment = new Attachment(name: "Stocklist - " + stocklist?.requisition?.name + ".xls", mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document", bytes: os.toByteArray())
            attachments.add(attachment)
        }
        if (includePdf) {
            Attachment attachment = new Attachment(name: "Stocklist - ${stocklist?.requisition?.name}.pdf", mimeType: "application/pdf", bytes: pdfRenderingService.render(template: "/stocklist/print", model: [stocklist: stocklist]).toByteArray())
            attachments.add(attachment)
        }
        mailService.sendHtmlMailWithAttachment(to, subject, body, attachments)
    }

    void publishStockList(Requisition requisition, Boolean publish) {
        requisition.isPublished = publish
        if (requisition.hasErrors()) {
            String publishLabel = publish ? "publish" : "unpublish";
            throw new ValidationException("Unable to $publishLabel stocklist due to errors", requisition.errors)
        }
        requisition.save(flush: true)
    }

    def exportStocklistItems(List<Requisition> requisitions, Boolean hasRoleFinance) {

        def requisitionItems = []

        requisitionService.getRequisitionTemplatesItems(requisitions)
                .groupBy {it.product}
                .collect { product, value ->
                    def totalCost = 0
                    if (hasRoleFinance) {
                        requisitions.each { requisition ->
                            totalCost = product?.pricePerUnit ? totalCost + (requisition.getQuantityByProduct(product) * product?.pricePerUnit) : ""
                        }
                    }
                    requisitionItems << [
                            product     : product,
                            productCode : product.productCode ?: "",
                            productName : product.name ?: "",
                            category    : product?.category?.name ?: "",
                            pricePerUnit: hasRoleFinance ? product?.pricePerUnit ?: "" : "",
                            totalCost   : hasRoleFinance ? totalCost : "",
                    ]
                }

        def sw = new StringWriter()

        if (requisitionItems) {
            def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')

            sw.append(g.message(code: 'product.code.label')).append(",")
            sw.append(g.message(code: 'product.description.label')).append(",")
            sw.append(g.message(code: 'product.primaryCategory.label')).append(",")

            requisitions.each { requisition ->
                sw.append(StringEscapeUtils.escapeCsv("${requisition?.name} [${requisition?.isPublished ? 'Published' : 'Draft'}]")).append(",")
            }

            if (hasRoleFinance) {
                sw.append(g.message(code: 'product.unitCost.label')).append(",")
                sw.append(g.message(code: 'product.totalValue.label'))
            }

            sw.append("\n")
            requisitionItems.each { requisitionItem ->

                sw.append(StringEscapeUtils.escapeCsv(requisitionItem?.productCode)).append(",")
                sw.append(StringEscapeUtils.escapeCsv(requisitionItem?.productName)).append(",")
                sw.append(StringEscapeUtils.escapeCsv(requisitionItem?.category)).append(",")


                requisitions?.each { requisition ->
                    sw.append((requisition?.getQuantityByProduct(requisitionItem.product) ?: "").toString()).append(",")
                }

                if (hasRoleFinance) {
                    sw.append(requisitionItem?.pricePerUnit.toString()).append(",")
                    sw.append(requisitionItem?.totalCost.toString()).append(",")
                }

                sw.append("\n")
            }
        }
        return sw;
    }

}
