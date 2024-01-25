/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.data

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.grails.datastore.mapping.query.api.Criteria
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Order
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Subqueries
import org.hibernate.sql.JoinType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierDataService

import org.pih.warehouse.product.ProductSupplierListParams
import org.pih.warehouse.product.ProductSupplierPreference

import java.text.SimpleDateFormat

@Transactional
class ProductSupplierService {

    public static final PREFERENCE_TYPE_NONE = "NONE"

    public static final PREFERENCE_TYPE_MULTIPLE = "MULTIPLE"

    def identifierService
    def dataSource
    ProductSupplierDataService productSupplierGormService


    List<ProductSupplier> getProductSuppliers(ProductSupplierListParams params) {
        // Store added aliases to avoid duplicate alias exceptions for product and supplier
        // This could happen when params.searchTerm and e.g. sort by productCode/productName is applied
        Set<String> usedAliases = new HashSet<>()

        return ProductSupplier.createCriteria().list(max: params.max, offset: params.offset) {
            if (params.searchTerm) {
                createAlias("product", "p", JoinType.LEFT_OUTER_JOIN)
                createAlias("supplier", "s", JoinType.LEFT_OUTER_JOIN)
                createAlias("manufacturer", "m", JoinType.LEFT_OUTER_JOIN)
                usedAliases.addAll(["product", "supplier", "manufacturer"])
                or {
                    ilike("p.productCode", "%" + params.searchTerm + "%")
                    ilike("code", "%" + params.searchTerm + "%")
                    ilike("s.code", "%" + params.searchTerm + "%")
                    ilike("name", "%" + params.searchTerm + "%")
                    ilike("supplierCode", "%" + params.searchTerm + "%")
                    ilike("supplierName", "%" + params.searchTerm + "%")
                    ilike("manufacturerCode", "%" + params.searchTerm + "%")
                    ilike("manufacturerName", "%" + params.searchTerm + "%")
                    ilike("m.name", "%" + params.searchTerm + "%")
                    ilike("productCode", "%" + params.searchTerm + "%")
                }
            }
            if (params.product) {
                eq("product.id", params.product)
            }
            if (!params.includeInactive) {
                eq("active", true)
            }
            if (params.supplier) {
                eq("supplier.id", params.supplier)
            }
            if (params.preferenceType) {
                add(getPreferenceTypeCriteria(params.preferenceType))
            }
            if (params.createdFrom) {
                ge("dateCreated", params.createdFrom)
            }
            if (params.createdTo) {
                lte("dateCreated", params.createdTo)
            }
            if (params.sort) {
                String orderDirection = params.order ?: "asc"
                getSortOrder(params.sort, orderDirection, delegate, usedAliases)
            }
        }
    }

    private static void getSortOrder(String sort, String orderDirection, Criteria criteria, Set<String> usedAliases) {
        switch (sort) {
            case "product.productCode":
                if (!usedAliases.contains("product")) {
                    criteria.createAlias("product", "p", JoinType.LEFT_OUTER_JOIN)
                    usedAliases.add("product")
                }
                criteria.addOrder(getOrderDirection("p.productCode", orderDirection))
                break
            case "product.name":
                if (!usedAliases.contains("product")) {
                    criteria.createAlias("product", "p", JoinType.LEFT_OUTER_JOIN)
                    usedAliases.add("product")
                }
                criteria.addOrder(getOrderDirection("p.name", orderDirection))
                break
            case "code":
                criteria.addOrder(getOrderDirection("code", orderDirection))
                break
            case "supplier.displayName":
                if (!usedAliases.contains("supplier")) {
                    criteria.createAlias("supplier", "s", JoinType.LEFT_OUTER_JOIN)
                    usedAliases.add("supplier")
                }
                criteria.addOrder(getOrderDirection("s.name", orderDirection))
                break
            case "dateCreated":
                criteria.addOrder(getOrderDirection("dateCreated", orderDirection))
                break
            case "active":
                criteria.addOrder(getOrderDirection("active", orderDirection))
                break
            case "name":
                criteria.addOrder(getOrderDirection("name", orderDirection))
                break
            default:
                break

        }
    }

    private static Order getOrderDirection(String sort, String order) {
        if (order == "desc") {
            return Order.desc(sort)
        }
        return Order.asc(sort)
    }

    private static Criterion getPreferenceTypeCriteria(String preferenceType) {
        if (preferenceType == PREFERENCE_TYPE_NONE) {
            return Restrictions.isEmpty("productSupplierPreferences")
        }
        if (preferenceType == PREFERENCE_TYPE_MULTIPLE) {
            return Restrictions.sizeGt("productSupplierPreferences", 1)
        }
        // If we are searching by a specific preference type, we want to search for a product supplier
        // that has only one preference (first statement) and its id is equal to the provided id (second statement)
        return Restrictions
                .and(Restrictions.sizeEq("productSupplierPreferences", 1),
                        Subqueries.exists(DetachedCriteria.forClass(ProductSupplierPreference, 'pp')
                            .createAlias("pp.preferenceType", 'pt')
                            .setProjection(Projections.property("pp.id"))
                            .add(Restrictions.and(
                                    Restrictions.eq("pt.id", preferenceType),
                                    // Join the product supplier preferences table by productSupplier.id = productPreference.supplierId
                                    Restrictions.eqProperty("pp.productSupplier.id", "this.id")))))


    }

    ProductSupplier createOrUpdate(Map params) {
        log.info("params: ${params}")

        def productCode = params.productCode
        def supplierName = params.supplierName
        def manufacturerName = params.manufacturerName
        def ratingTypeCode = params?.ratingTypeCode ? params?.ratingTypeCode?.toUpperCase() as RatingTypeCode : null
        def supplierCode = params.supplierCode
        def manufacturerCode = params.manufacturerCode

        Product product = productCode ? Product.findByProductCode(productCode) : null
        UnitOfMeasure unitOfMeasure = params.defaultProductPackageUomCode ?
                UnitOfMeasure.findByCode(params.defaultProductPackageUomCode) : null
        BigDecimal price = params.defaultProductPackagePrice ?
                new BigDecimal(params.defaultProductPackagePrice) : null
        Integer quantity = params.defaultProductPackageQuantity as Integer

        ProductSupplier productSupplier = ProductSupplier.findByIdOrCode(params["id"], params["code"])
        if (!productSupplier) {
            productSupplier = new ProductSupplier(params)
        } else {
            productSupplier.properties = params
        }

        productSupplier.ratingTypeCode = ratingTypeCode
        productSupplier.productCode = params["legacyProductCode"]
        productSupplier.product = product
        productSupplier.supplier = supplierName ? Organization.findByName(supplierName) : null
        productSupplier.manufacturer = manufacturerName ? Organization.findByName(manufacturerName) : null
        productSupplier.supplierCode = supplierCode ? supplierCode : null
        productSupplier.manufacturerCode = manufacturerCode ? manufacturerCode : null

        if (unitOfMeasure && quantity) {
            ProductPackage defaultProductPackage =
                    productSupplier.productPackages.find { it.uom == unitOfMeasure && it.quantity == quantity }

            if (!defaultProductPackage) {
                defaultProductPackage = new ProductPackage()
                defaultProductPackage.name = "${unitOfMeasure.code}/${quantity}"
                defaultProductPackage.description = "${unitOfMeasure.name} of ${quantity}"
                defaultProductPackage.product = productSupplier.product
                defaultProductPackage.uom = unitOfMeasure
                defaultProductPackage.quantity = quantity
                if (price != null) {
                    ProductPrice productPrice = new ProductPrice()
                    productPrice.price = price
                    defaultProductPackage.productPrice = productPrice
                }
                productSupplier.addToProductPackages(defaultProductPackage)
            } else if (price != null && !defaultProductPackage.productPrice) {
                ProductPrice productPrice = new ProductPrice()
                productPrice.price = price
                defaultProductPackage.productPrice = productPrice
            } else if (price != null && defaultProductPackage.productPrice) {
                defaultProductPackage.productPrice.price = price
                defaultProductPackage.lastUpdated = new Date()
            }
        }

        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")

        def contractPriceValidUntil = params.contractPriceValidUntil ? dateFormat.parse(params.contractPriceValidUntil) : null
        BigDecimal contractPricePrice = params.contractPricePrice ? new BigDecimal(params.contractPricePrice) : null

        if (contractPricePrice) {
            if (!productSupplier.contractPrice) {
                productSupplier.contractPrice = new ProductPrice()
            }

            productSupplier.contractPrice.price = contractPricePrice

            if (contractPriceValidUntil) {
                productSupplier.contractPrice.toDate = contractPriceValidUntil
            }
        }

        PreferenceType preferenceType = params.globalPreferenceTypeName ? PreferenceType.findByName(params.globalPreferenceTypeName) : null

        if (preferenceType) {
            ProductSupplierPreference productSupplierPreference = productSupplier.getGlobalProductSupplierPreference()

            if (!productSupplierPreference) {
                productSupplierPreference = new ProductSupplierPreference()
                productSupplier.addToProductSupplierPreferences(productSupplierPreference)
            }

            productSupplierPreference.preferenceType = preferenceType
            productSupplierPreference.comments = params.globalPreferenceTypeComments

            def globalPreferenceTypeValidityStartDate = params.globalPreferenceTypeValidityStartDate ? dateFormat.parse(params.globalPreferenceTypeValidityStartDate) : null

            if (globalPreferenceTypeValidityStartDate) {
                productSupplierPreference.validityStartDate = globalPreferenceTypeValidityStartDate
            }

            def globalPreferenceTypeValidityEndDate = params.globalPreferenceTypeValidityEndDate ? dateFormat.parse(params.globalPreferenceTypeValidityEndDate) : null

            if (globalPreferenceTypeValidityEndDate) {
                productSupplierPreference.validityEndDate = globalPreferenceTypeValidityEndDate
            }
        }

        if (!productSupplier.code) {
            String prefix = productSupplier?.product?.productCode
            productSupplier.code = identifierService.generateProductSupplierIdentifier(prefix)
        }
        return productSupplier
    }

    def getOrCreateNew(Map params, boolean forceCreate) {
        def productSupplier
        if (params.productSupplier) {
            productSupplier = params.productSupplier ? ProductSupplier.get(params.productSupplier) : null
        } else {
            productSupplier = getProductSupplier(params)
        }

        if (!productSupplier && (params.supplierCode || params.manufacturer || params.manufacturerCode || forceCreate)) {
            return createProductSupplierWithoutPackage(params)
        }

        return productSupplier
    }

    def getProductSupplier(Map params) {
        String supplierCode = params.supplierCode ? params.supplierCode.replaceAll('[ .,-]','') : null
        String manufacturerCode = params.manufacturerCode ? params.manufacturerCode.replaceAll('[ .,-]','') : null

        String query = """
                select 
                    id
                FROM product_supplier_clean
                WHERE product_id = :productId
                AND supplier_id = :supplierId 
                """
        if (params.supplierCode) {
            query += " AND supplier_code = IFNULL(:supplierCode, supplier_code) "
        } else {
            query += " AND (supplier_code is null OR supplier_code = '') "
            if (params.manufacturer && params.manufacturerCode) {
                query += " AND manufacturer_id = :manufacturerId AND manufacturer_code = :manufacturerCode "
            } else if (params.manufacturer) {
                query += " AND manufacturer_id = :manufacturerId AND (manufacturer_code is null OR manufacturer_code = '')"
            } else if (params.manufacturerCode) {
                query += " AND manufacturer_code = :manufacturerCode AND (manufacturer_id is null or manufacturer_id = '')"
            } else {
                query += " AND (manufacturer_code is null OR manufacturer_code = '') AND (manufacturer_id is null or manufacturer_id = '')"
            }
        }
        Sql sql = new Sql(dataSource)
        def data = sql.rows(query, [
                'productId': params.product?.id,
                'supplierId': params.supplier?.id,
                'manufacturerId': params.manufacturer,
                'manufacturerCode': manufacturerCode,
                'supplierCode': supplierCode,
        ])
        // Sort productSuppliers by active field, so we make sure that if there is more than one and one of then could be inactive, that the active ones are "moved" to the top of list
        ArrayList<ProductSupplier> productSuppliers = data?.collect{ it -> ProductSupplier.get(it?.id) }?.sort{ !it?.active }
        // Double negation below is equivalent to productSuppliers.size() > 0 ? productSuppliers.first() : null (empty list is treated as true, so we can't do "productSuppliers ?")
        def productSupplier = !!productSuppliers ? productSuppliers?.first() : null
        return productSupplier
    }

    def createProductSupplierWithoutPackage(Map params) {
        Product product = Product.get(params.product.id)
        Organization organization = Organization.get(params.supplier.id)
        Organization manufacturer = Organization.get(params.manufacturer)
        ProductSupplier productSupplier = new ProductSupplier()
        productSupplier.code = params.sourceCode ?: identifierService.generateProductSupplierIdentifier(product?.productCode, organization?.code)
        productSupplier.name = params.sourceName ?: product?.name
        productSupplier.supplier = organization
        productSupplier.supplierCode = params.supplierCode
        productSupplier.product = product
        productSupplier.manufacturer = manufacturer
        productSupplier.manufacturerCode = params.manufacturerCode

        if (productSupplier.validate()) {
            productSupplier.save(failOnError: true)
        }
        return productSupplier
    }

    void delete(String productSupplierId) {
        productSupplierGormService.delete(productSupplierId)
    }
}
