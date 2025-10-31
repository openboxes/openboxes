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
import grails.validation.ValidationException
import groovy.sql.Sql
import org.grails.datastore.mapping.query.api.Criteria
import org.hibernate.ObjectNotFoundException
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Order
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.hibernate.criterion.Subqueries
import org.hibernate.sql.JoinType
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierDataService

import org.pih.warehouse.product.ProductSupplierFilterCommand
import org.pih.warehouse.product.ProductSupplierDetailsCommand
import org.pih.warehouse.product.ProductSupplierPreference

import java.text.SimpleDateFormat

@Transactional
class ProductSupplierService {

    public static final PREFERENCE_TYPE_NONE = "NONE"

    public static final PREFERENCE_TYPE_MULTIPLE = "MULTIPLE"

    ProductSupplierIdentifierService productSupplierIdentifierService
    def dataSource
    ProductSupplierDataService productSupplierGormService
    DataService dataService

    List<ProductSupplier> getProductSuppliers(ProductSupplierFilterCommand command, boolean forExport = false) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid params", command.errors)
        }
        // Store added aliases to avoid duplicate alias exceptions for product and supplier
        // This could happen when params.searchTerm and e.g. sort by productCode/productName is applied
        Set<String> usedAliases = new HashSet<>()
        return ProductSupplier.createCriteria().list(command.paginationParams) {
            if (forExport) {
                createAlias("product", "p", JoinType.INNER_JOIN)
                createAlias("p.synonyms", "ps", JoinType.LEFT_OUTER_JOIN)
                createAlias("supplier", "s", JoinType.LEFT_OUTER_JOIN)
                createAlias("manufacturer", "m", JoinType.LEFT_OUTER_JOIN)
                createAlias("contractPrice", "cp", JoinType.LEFT_OUTER_JOIN)
                createAlias("defaultProductPackage", "dpp", JoinType.LEFT_OUTER_JOIN)
                createAlias("defaultProductPackage.uom", "dppu", JoinType.LEFT_OUTER_JOIN)
                createAlias("defaultProductPackage.productPrice", "dppp", JoinType.LEFT_OUTER_JOIN)
                createAlias("productSupplierPreferences", "psp", JoinType.LEFT_OUTER_JOIN)
                createAlias("productPackages", "pp", JoinType.LEFT_OUTER_JOIN)
                createAlias("productPackages.uom", "ppu", JoinType.LEFT_OUTER_JOIN)
                createAlias("productPackages.productPrice", "ppp", JoinType.LEFT_OUTER_JOIN)
                usedAliases.addAll(["product", "supplier", "manufacturer", "contractPrice",
                                    "defaultProductPackage", "defaultProductPackage.uom",
                                    "defaultProductPackage.productPrice", "productSupplierPreferences",
                                    "productPackages", "productPackages.uom", "productPackages.productPrice"])
            }

            if (command.searchTerm && !forExport) {
                createAlias("product", "p", JoinType.LEFT_OUTER_JOIN)
                createAlias("supplier", "s", JoinType.LEFT_OUTER_JOIN)
                createAlias("manufacturer", "m", JoinType.LEFT_OUTER_JOIN)
                usedAliases.addAll(["product", "supplier", "manufacturer"])
            }

            if (command.searchTerm) {
                or {
                    ilike("p.productCode", "%" + command.searchTerm + "%")
                    ilike("code", "%" + command.searchTerm + "%")
                    ilike("s.code", "%" + command.searchTerm + "%")
                    ilike("name", "%" + command.searchTerm + "%")
                    ilike("supplierCode", "%" + command.searchTerm + "%")
                    ilike("supplierName", "%" + command.searchTerm + "%")
                    ilike("manufacturerCode", "%" + command.searchTerm + "%")
                    ilike("manufacturerName", "%" + command.searchTerm + "%")
                    ilike("m.name", "%" + command.searchTerm + "%")
                    ilike("productCode", "%" + command.searchTerm + "%")
                }
            }
            if (command.product) {
                eq((forExport || command.searchTerm) ? "p.id" : "product.id", command.product)
            }
            if (!command.includeInactive) {
                eq("active", true)
            }
            if (command.supplier) {
                eq((forExport || command.searchTerm) ? "s.id" : "supplier.id", command.supplier)
            }
            if (command.defaultPreferenceTypes) {
                add(getPreferenceTypeCriteria(command.defaultPreferenceTypes))
            }
            if (command.createdFrom) {
                ge("dateCreated", command.createdFrom)
            }
            if (command.createdTo) {
                lte("dateCreated", command.createdTo)
            }
            if (command.sort) {
                String orderDirection = command.order ?: "asc"
                getSortOrder(command.sort, orderDirection, delegate, usedAliases)
            }
            setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
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

    private static Criterion getPreferenceTypeCriteria(List<String> preferenceTypes) {
        if (preferenceTypes.contains(PREFERENCE_TYPE_NONE)) {
            return Restrictions.or(
                    getDefaultPreferenceTypeCriteria(preferenceTypes),
                    getEmptyDefaultPreferenceTypeCriteria(),
            )
        }

        return getDefaultPreferenceTypeCriteria(preferenceTypes)
    }

    private static Criterion getDefaultPreferenceTypeCriteria(List<String> preferenceTypes) {
        // If we are searching by a list of preference types, we want to search for a product supplier
        // that has default preference type within the provided list (first statement). We are considering
        // preference type as a default when the destination party is null (second statement)
        return Restrictions
                .and(Subqueries.exists(DetachedCriteria.forClass(ProductSupplierPreference, 'pp')
                                .createAlias("pp.preferenceType", 'pt', JoinType.LEFT_OUTER_JOIN)
                                .setProjection(Projections.property("pp.id"))
                                .add(Restrictions.and(
                                        Restrictions.in("pt.id", preferenceTypes),
                                        Restrictions.isNull("pp.destinationParty"),
                                        // Join the product supplier preferences table by productSupplier.id = productPreference.supplierId
                                        Restrictions.eqProperty("pp.productSupplier.id", "this.id")))))
    }

    private static Criterion getEmptyDefaultPreferenceTypeCriteria() {
        //  If we are searching for product suppliers without a default preference type, we are
        //  checking whether exists preference type without a destination party for the exact supplier
        return Restrictions
                .and(Subqueries.notExists(DetachedCriteria.forClass(ProductSupplierPreference, 'pp')
                        .createAlias("pp.preferenceType", 'pt', JoinType.LEFT_OUTER_JOIN)
                        .setProjection(Projections.property("pp.id"))
                        .add(Restrictions.and(
                                Restrictions.isNull("pp.destinationParty"),
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
            String sourceCode = productSupplier.code
            productSupplier.properties = params
            // Do not allow to bind an empty source code, which caused the code to be re-generated for an existing product supplier (OBPIH-6288)
            // The code could be overwritten by the binding, hence we'd like to bring back the existing code, if the one in params is null/empty
            if (!params.code) {
                productSupplier.code = sourceCode
            }
        }

        productSupplier.ratingTypeCode = ratingTypeCode
        productSupplier.productCode = params["legacyProductCode"]
        productSupplier.product = product
        Organization supplier = supplierName ? Organization.findByName(supplierName) : null
        productSupplier.supplier = supplier
        productSupplier.manufacturer = manufacturerName ? Organization.findByName(manufacturerName) : null
        productSupplier.supplierCode = supplierCode ? supplierCode : null
        productSupplier.manufacturerCode = manufacturerCode ? manufacturerCode : null
        if (!productSupplier.code && !productSupplier.id) {
            assignSourceCode(productSupplier, supplier)
        }

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
                /**
                    Product supplier needs to be saved here to receive an ID in order to be able to assign it to the product package
                    otherwise the transient property value exception would be thrown
                 */
                productSupplier.save()
                defaultProductPackage.productSupplier = productSupplier
                /**
                    The flush is needed because of the order Hibernate uses to save the entities in this operation -
                    for productSupplier.defaultProductPackage to be stored properly and not be cleared after transaction commit, the flush is needed
                    Check OBPIH-6757 for more details (#4904 PR)
                 */
                defaultProductPackage.save(flush: true)
                productSupplier.defaultProductPackage = defaultProductPackage
                productSupplier.addToProductPackages(defaultProductPackage)

                if (price != null) {
                    ProductPrice productPrice = new ProductPrice()
                    productPrice.price = price
                    defaultProductPackage.productPrice = productPrice
                }
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

        assignDefaultPreferenceType(productSupplier,
                preferenceType,
                params.globalPreferenceTypeComments,
                params.globalPreferenceTypeValidityStartDate,
                params.globalPreferenceTypeValidityEndDate)

        return productSupplier
    }

    void assignDefaultPreferenceType(ProductSupplier productSupplier,
                 PreferenceType preferenceType,
                 String comments,
                 String validityStartDate,
                 String validityEndDate) {
        ProductSupplierPreference productSupplierPreference = productSupplier.getGlobalProductSupplierPreference()
        if (!preferenceType && productSupplierPreference) {
            // If preference type is not provided, delete it
            productSupplier.removeFromProductSupplierPreferences(productSupplierPreference)
            productSupplierPreference.delete()
            return
        }

        if (!productSupplierPreference) {
            productSupplierPreference = new ProductSupplierPreference()
            productSupplier.addToProductSupplierPreferences(productSupplierPreference)
        }

        productSupplierPreference.preferenceType = preferenceType
        productSupplierPreference.comments = comments

        Date globalPreferenceTypeValidityStartDate = validityStartDate ? Constants.MONTH_DAY_YEAR_DATE_FORMATTER.parse(validityStartDate) : null

        if (globalPreferenceTypeValidityStartDate) {
            productSupplierPreference.validityStartDate = globalPreferenceTypeValidityStartDate
        }

        Date globalPreferenceTypeValidityEndDate = validityEndDate ? Constants.MONTH_DAY_YEAR_DATE_FORMATTER.parse(validityEndDate) : null

        if (globalPreferenceTypeValidityEndDate) {
            productSupplierPreference.validityEndDate = globalPreferenceTypeValidityEndDate
        }
    }

    void assignSourceCode(ProductSupplier productSupplier, Organization organization) {
        productSupplier.code = productSupplierIdentifierService.generate(
                productSupplier,
                productSupplier?.product?.productCode,
                organization?.code)
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
        productSupplier.code = params.sourceCode ?: productSupplierIdentifierService.generate(
                productSupplier,
                product?.productCode,
                organization?.code)
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

    ProductSupplier saveProductSupplier(ProductSupplierDetailsCommand command) {
        ProductSupplier productSupplier = new ProductSupplier(command.properties)
        if (!productSupplier.code) {
            productSupplier.code = productSupplierIdentifierService.generate(
                    productSupplier,
                    command?.product?.productCode,
                    command?.supplier?.code)
        }
        return productSupplierGormService.save(productSupplier)
    }

    ProductSupplier updateProductSupplier(ProductSupplierDetailsCommand command, String productSupplierId) {
        ProductSupplier productSupplier = productSupplierGormService.get(productSupplierId)
        if (!productSupplier) {
            throw new ObjectNotFoundException(productSupplierId, ProductSupplier.class.toString())
        }
        productSupplier.properties = command.properties
        if (!productSupplier.code) {
            productSupplier.code = productSupplierIdentifierService.generate(
                    productSupplier,
                    command?.product?.productCode,
                    command?.supplier?.code)
        }
        return productSupplier
    }

    List<Map> getExportData(ProductSupplierFilterCommand filterParams) {
        List<ProductSupplier> productSuppliers = getProductSuppliers(filterParams, true)

        productSuppliers = productSuppliers.collect { ProductSupplier p ->
            ProductPackage productPackage = p.defaultProductPackage ?: p.getDefaultProductPackageDerived()
            ProductSupplierPreference globalPreference = p.productSupplierPreferences?.find { it.destinationParty == null }
            [
                    active                                    : p.active,
                    id                                        : p.id,
                    name                                      : p.name,
                    code                                      : p.code,
                    productCode                               : p.product?.productCode,
                    productName                               : p.product?.name,
                    legacyProductCode                         : p.productCode,
                    "supplier.name"                           : p.supplier?.name,
                    supplierCode                              : p.supplierCode,
                    "manufacturer.name"                       : p.manufacturer?.name,
                    manufacturerCode                          : p.manufacturerCode,
                    minOrderQuantity                          : p.minOrderQuantity,
                    "contractPrice.price"                     : p.contractPrice?.price,
                    "contractPrice.toDate"                    : p.contractPrice?.toDate,
                    "defaultProductPackage.uom.code"          : productPackage?.uom?.code,
                    "defaultProductPackage.quantity"          : productPackage?.quantity,
                    "defaultProductPackage.productPrice.price": productPackage?.productPrice?.price,
                    ratingTypeCode                            : p.ratingTypeCode,
                    dateCreated                               : p.dateCreated,
                    lastUpdated                               : p.lastUpdated,
                    "globalProductSupplierPreference"         : globalPreference ? [
                            "preferenceType"   : globalPreference.preferenceType,
                            "validityStartDate": globalPreference.validityStartDate,
                            "validityEndDate"  : globalPreference.validityEndDate,
                            "comments"         : globalPreference.comments
                    ] : null,
                    "product"                                 : [
                            "productCode": p.product?.productCode,
                            "name"       : p.product?.displayNameWithLocaleCode ?: p.product?.name
                    ]
            ]
        } as List<Map>

        return productSuppliers ? dataService.transformObjects(productSuppliers, ProductSupplier.PROPERTIES) : [[:]]
    }
}
