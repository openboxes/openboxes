package org.pih.warehouse.product

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.junit.Test

class ProductNotificationTests  extends GroovyTestCase {

    def productService

    @Test
    void testProductFindWithExternalId(){


    }

    @Test
    void testSaveProductFromJson(){

        String externalId = "142"
        Product productInstance = productService.findProductByExternalId(externalId)
        assert productInstance == null

        String productJsonString = """{
            "locationNumber": "SOF00001",
            "products": [{
                "id": "${externalId}",
                "productCode": "SOF00001-SKU02",
                "name": "Test name",
                "pricePerUnit": 150.0,
                "description": "Test description",
                "unitOfMeasure": null,
                "brandName": null,
                "category": "FMCG",
                "attributes": [{
                    "code": "vat",
                    "value": "20.0"
                }, {
                    "code": "country",
                    "value": "Bulgaria"
                }, {
                    "code": "principalCompany",
                    "value": "Test principal company"
                }, {
                    "code": "strength",
                    "value": ""
                }, {
                    "code": "packSize",
                    "value": "Pack size"
                }, {
                    "code": "productForm",
                    "value": "Form of product"
                }, {
                    "code": "size",
                    "value": "Size"
                }, {
                    "code": "color",
                    "value": "Color"
                }, {
                    "code": "weight",
                    "value": "Weight"
                }, {
                    "code": "packageInformation",
                    "value": "Package information"
                }, {
                    "code": "dimensions",
                    "value": "Dimensions"
                }, {
                    "code": "barcode",
                    "value": "Test barcode"
                }, {
                    "code": "category",
                    "value": "Acne & Sensitive Skin"
                }, {
                    "code": "subcategory",
                    "value": "Subcategory 1"
                }, {
                    "code": "ingredients",
                    "value": "ingredients 1"
                }],
                "documents": [{
                    "fileUri": "link2.com"
                }, {
                    "fileUri": "link1.com"
                }, {
                    "fileUri": "videolink.com"
                }],
                "components": [
                    {
                        "productCode":"SOF00001-SKU2_08",
                        "quantity":1
                    },
                    {
                        "productCode":"SOF00001-SKU2_06",
                        "quantity":1
                    }
                ]
            }]
        }"""

        def category = productService.findOrCreateCategory("FMCG")
        JSONObject productsJson = new JSONObject(productJsonString)
        JSONArray products = productsJson.getJSONArray("products")
        Product product = null
        for (int i = 0; i < products.length(); i++) {
            JSONObject productJson = products.getJSONObject(i)
            product = productService.createFromJson(productJson)
            assert product.name == "Test name"
            assert product.attributes?.size() == 15
            assert product.documents?.size() == 3
            assert product.productComponents?.size() == 2
        }

        productInstance = productService.findProductByExternalId(externalId)
        assert productInstance == product
    }
}
