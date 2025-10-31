package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.data.DataService
import org.pih.warehouse.data.ProductSupplierService
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierFilterCommand
import org.pih.warehouse.product.ProductSupplierDetailsCommand
import org.springframework.http.HttpStatus

class ProductSupplierApiController {

    ProductSupplierService productSupplierService
    DocumentService documentService
    DataService dataService

    def list(ProductSupplierFilterCommand filterParams) {
        List<ProductSupplier> productSuppliers = productSupplierService.getProductSuppliers(filterParams)
        render([data: productSuppliers, totalCount: productSuppliers.totalCount] as JSON)
    }

    def read() {
        ProductSupplier productSupplier = ProductSupplier.get(params.id)
        if (!productSupplier) {
            throw new ObjectNotFoundException(params.id, ProductSupplier.class.toString())
        }

        render([data: productSupplier.toJson()] as JSON)
    }

    def delete() {
        productSupplierService.delete(params.id)
        render status: 204
    }

    def create(ProductSupplierDetailsCommand productSupplierDetailsCommand) {
        if (productSupplierDetailsCommand.hasErrors()) {
            throw new ValidationException("Invalid product source", productSupplierDetailsCommand.errors)
        }
        ProductSupplier productSupplier = productSupplierService.saveProductSupplier(productSupplierDetailsCommand)

        response.status = HttpStatus.CREATED.value()
        render([data: productSupplier] as JSON)
    }

    def update(ProductSupplierDetailsCommand productSupplierDetailsCommand) {
        if (productSupplierDetailsCommand.hasErrors()) {
            throw new ValidationException("Invalid product source", productSupplierDetailsCommand.errors)
        }
        ProductSupplier updatedProductSupplier = productSupplierService.updateProductSupplier(productSupplierDetailsCommand, params.id)

        render([data: updatedProductSupplier] as JSON)
    }

    def export(ProductSupplierFilterCommand filterParams) {
        def data = productSupplierService.getExportData(filterParams)
        def fileName = "ProductSuppliers-${new Date().format('yyyyMMdd-hhmmss')}"
        withFormat {
            json {
                render([data: data, totalCount: data.size()] as JSON)
            }
            csv {
                exportToCsv(data, fileName)
            }
            xls {
                exportToXls(data, fileName)
            }
        }
    }

    def exportToXls(List data, fileName) {
        response.contentType = "application/vnd.ms-excel"
        response.setHeader 'Content-disposition',
                "attachment; filename=\"${fileName}.xls\""
        documentService.generateExcel(response.outputStream, data)
        response.outputStream.flush()
    }

    def exportToCsv(List data, fileName) {
        response.contentType = "text/csv"
        response.setHeader("Content-disposition",
                "attachment; filename=\"${fileName}.csv\"")
        render(contentType: "text/csv", text: dataService.generateCsv(data))
        response.outputStream.flush()
    }
}
