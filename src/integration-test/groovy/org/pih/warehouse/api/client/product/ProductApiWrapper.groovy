package org.pih.warehouse.api.client.product

import groovy.transform.InheritConstructors
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability

@TestComponent
@InheritConstructors
class ProductApiWrapper extends ApiWrapper<ProductApi> {

    List<ProductAvailability> getProductAvailabilityOK(Product product) {
        return api.getProductAvailability(product.id, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getList("data", ProductAvailability.class)
    }

    Product saveOK(Product product) {
        String body = new JSONObject()
                .put('productType', jsonObjectUtil.asIdForRequestBody(product.productType))
                .put('category', jsonObjectUtil.asIdForRequestBody(product.category))
                .put('productFamily', jsonObjectUtil.asIdForRequestBody(product.productFamily))
                .put('glAccount', jsonObjectUtil.asIdForRequestBody(product.glAccount))
                .put('attributes', jsonObjectUtil.asIdsForRequestBody(product.attributes))
                .put('tags', jsonObjectUtil.asIdsForRequestBody(product.tags))
                .put('name', product.name)
                .put('description', product.description)
                .put('active', product.active)
                .put('productCode', product.productCode)
                .put('pricePerUnit', product.pricePerUnit)
                .put('costPerUnit', product.costPerUnit)
                .put('abcClass', product.abcClass)
                .put('unitOfMeasure', product.unitOfMeasure)
                .put('upc', product.upc)
                .put('ndc', product.ndc)
                .put('manufacturer', product.manufacturer)
                .put('manufacturerCode', product.manufacturerCode)
                .put('manufacturerName', product.manufacturerName)
                .put('brandName', product.brandName)
                .put('modelNumber', product.modelNumber)
                .put('vendor', product.vendor)
                .put('vendorCode', product.vendorCode)
                .put('vendorName', product.vendorName)
                .put('abcClass', product.abcClass)
                .put('coldChain', product.coldChain)
                .put('controlledSubstance', product.controlledSubstance)
                .put('hazardousMaterial', product.hazardousMaterial)
                .put('reconditioned', product.reconditioned)
                .put('lotAndExpiryControl', product.lotAndExpiryControl)
                .put('color', product.color)
                .toString()

        return api.save(body, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getObject("product", Product.class)
    }
}
