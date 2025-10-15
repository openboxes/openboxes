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

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.xml.Namespace
import java.sql.Timestamp
import org.apache.commons.lang.StringUtils
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ApiException
import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.date.DateFormatterManager
import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.TransactionEntry
import org.springframework.beans.factory.annotation.Autowired
import util.ReportUtil

/**
 * @author jmiranda*
 */
@Transactional
class ProductService {

    def sessionFactory
    GrailsApplication grailsApplication
    def authService
    ProductIdentifierService productIdentifierService
    def userService
    def dataService
    ProductGroupService productGroupService
    ConfigService configService

    @Autowired
    DateFormatterManager dateFormatter

    def getNdcResults(operation, q) {
        def hipaaspaceApiKey = grailsApplication.config.hipaaspace.api.key
        if (!hipaaspaceApiKey) {
            throw new ApiException(message: "Your administrator must specify Hipaaspace.com API key (hipaaspace.api.key) in configuration file (openboxes-config.properties).  Sign up at <a href='http://www.hipaaspace.com/myaccount/login.aspx?ReturnUrl=%2fmyaccount%2fdefault.aspx' target='_blank'>hipaaspace.com</a>.")
        }
        try {
            def url = new URL("http://www.HIPAASpace.com/api/ndc/search?q=${q?.encodeAsURL()}&rt=xml&key=${hipaaspaceApiKey}")
            def connection = url.openConnection()
            if (connection.responseCode == 200) {
                def xml = connection.content.text
                //println xml

                return processNdcProducts(xml)

                //search.results << product
            }
        } catch (Exception e) {
            log.error("Error trying to get products from NDC API ", e)
            throw new ApiException(message: "Unable to query NDC database: " + e.message)
        }
    }

    def processNdcProducts(xml) {
        def results = []
        def ndcList = new XmlParser(false, true).parseText(xml)
        ndcList.NDC.each {
            ndc ->

                if (ndc.NDCCode) {
                    println "NDC: " + ndc
                    def product = new ProductDetailsCommand()
                    product.title = ndc.PackageDescription.text()
                    product.ndcCode = ndc.NDCCode
                    product.productType = ndc.ProductTypeName.text()
                    product.packageDescription = ndc.PackageDescription.text()
                    product.ndcCode = ndc.NDCCode.text()
                    product.productNdcCode = ndc.ProductNDC.text()
                    product.labelerName = ndc.LabelerName.text()
                    product.strengthNumber = ndc.StrengthNumber.text()
                    product.strengthUnit = ndc.StrengthUnit.text()
                    product.pharmClasses = ndc.PharmClasses.text()
                    product.dosageForm = ndc.DosageFormName.text()
                    product.route = ndc.RouteName.text()
                    product.proprietaryName = ndc.ProprietaryName.text()
                    product.nonProprietaryName = ndc.NonProprietaryName.text()
                    results << product
                }
        }
        return results
    }

    def processXml(urlString, itemName) {
        try {
            def results = []
            println "URL " + urlString
            def url = new URL(urlString)
            def connection = url.openConnection()
            if (connection.responseCode == 200) {
                def xml = connection.content.text

                def list = new XmlParser(false, true).parseText(xml)
                for (item in list.displayTermsList.term) {
                    println "item: " + item.class.name
                    results << item.text()
                }
                return results
            }
        } catch (Exception e) {
            log.error("Error trying to get products from NDC API ", e)
            throw e
        }
    }

    /**
     * @param q
     * @return
     */
    def findGoogleProducts(search) {

        def googleProductSearchKey = grailsApplication.config.google.api.key
        if (!googleProductSearchKey) {
            throw new ApiException(message: "Your administrator must specify Google API key (google.api.key) in configuration file (openboxes-config.properties).  For more information, see Google's <a href='https://developers.google.com/shopping-search/v1/getting_started#getting-started' target='_blank'>Getting Started</a> guide")
        }
        def products = new ArrayList()
        int startIndex = search.startIndex
        String q = search.searchTerms
        boolean spellingEnabled = search.spellingEnabled

        def urlString = "https://www.googleapis.com/shopping/search/v1/public/products?" +
                "key=${googleProductSearchKey}&country=US&q=${q.encodeAsURL()}&alt=atom&crowdBy=brand:1"
        if (startIndex > 0) {
            urlString += "&startIndex=" + startIndex
        }
        if (spellingEnabled) {
            urlString += "&spelling.enabled=true"
        }
        def url = new URL(urlString)
        def connection = url.openConnection()
        if (connection.responseCode == 200) {
            def xml = connection.content.text
            def feed = new XmlParser(false, true).parseText(xml)

            def ns = new Namespace("http://www.google.com/shopping/api/schemas/2010", "s")
            def openSearch = new Namespace("http://a9.com/-/spec/opensearchrss/1.0/", "openSearch")

            search.totalResults = Integer.valueOf(feed[openSearch.totalResults].text())
            search.startIndex = Integer.valueOf(feed[openSearch.startIndex].text())
            search.itemsPerPage = Integer.valueOf(feed[openSearch.itemsPerPage].text())

            feed.entry.each {
                entry ->

                    def product = new ProductDetailsCommand()

                    product.link = entry[ns.product][ns.link].text()
                    product.author = entry.author.name.text()


                    entry.link.each {
                        link ->
                            product.links[link.'@rel'] = link.'@href'
                    }

                    product.id = entry.id.text()
                    product.googleId = entry[ns.product][ns.googleId].text()
                    product.title = entry[ns.product][ns.title].text()
                    product.description = entry[ns.product][ns.description].text()
                    product.brand = entry[ns.product][ns.brand].text()
                    product.gtin = entry[ns.product][ns.gtin].text()
                    entry[ns.product][ns.gtins][ns.gtin].each { gtin ->
                        product.gtins << gtin.text()
                    }
                    entry[ns.product][ns.images][ns.image].each { image ->
                        product.images << image.'@link'
                    }
                    search.results << product
            }
        } else {
            log.info("URL: " + url)
            log.info("Response Code: " + connection.responseCode)
            log.info("Response Message: " + connection.responseMessage)
            throw new ApiException("Unable to connect to Google Product Search API using connection URL [" + urlString + "]: " + connection.responseMessage)
        }

    }


    /**
     * @param searchTerms
     * @param categories
     * @return
     */
    List<Product> findProducts(List searchTerms) {
        // Get all products, including hidden ones
        def products = Product.list()
        def searchResults = Product.createCriteria().list() {
            eq("active", true)
            or {
                or {
                    searchTerms.each {
                        ilike("name", "%" + it + "%")
                    }
                }
                or {
                    searchTerms.each {
                        ilike("manufacturer", "%" + it + "%")
                    }
                }
                or {
                    searchTerms.each {
                        ilike("manufacturerCode", "%" + it + "%")
                    }
                }
                or {
                    searchTerms.each {
                        ilike("productCode", "%" + it + "%")
                    }
                }
            }
        }
        searchResults = products.intersect(searchResults)

        // now localize to only match products for the current locale
        // TODO: this would also have to handle the category filtering
        //  products = products.findAll { product ->
        //  def localizedProductName = getLocalizationService().getLocalizedString(product.name);  // TODO: obviously, this would have to use the actual locale
        // return productFilters.any {
        //   localizedProductName.contains(it)  // TODO: this would also have to be case insensitive
        // }
        // }
        return searchResults
    }

    /**
     * @deprecated
     * @return
     */
    List<Product> getProducts(String query, Category category, List<Tag> tags, boolean includeInactive, params) {
        return getProducts(category, tags, includeInactive, params)
    }

    /**
     * Get all products that match the given product identifiers.
     *
     * @param ids
     * @return
     */
    List<Product> getProducts(String[] ids) {
        def products = []
        if (ids) {
            products = Product.createCriteria().list() {
                eq("active", true)
                'in'("id", ids)
            }
        }
        return products
    }

    /**
     * Get all products that match category, tags, and search parameters.
     * @param category
     * @param tags
     * @param params
     * @return
     */
    List<Product> getProducts(Category category, List<Tag> tags, Map params) {
        return getProducts(category, tags, false, params)
    }

    /**
     * Get all products that match category, tags, and search parameters.
     * @param category
     * @param tags
     * @param params
     * @return
     */
    List<Product> getProducts(Category category, List<Tag> tags, boolean includeInactive, Map params) {
        return getProducts([category], [], tags, [], [], includeInactive, params)
    }

    /**
     * Get all products that match category, tags, and other search parameters.
     *
     * @param category
     * @param catalogs
     * @param tags
     * @param glAccounts
     * @param productGroups
     * @param includeInactive
     * @param params
     * @return
     */
    List<Product> getProducts(
            List<Category> categories,
            List<ProductCatalog> catalogsInput,
            List<Tag> tagsInput,
            List<GlAccount> glAccounts,
            List<ProductGroup> productFamilies,
            boolean includeInactive,
            Map params
    ) {
        int max = params.max ? params.int("max") : 10
        int offset = params.offset ? params.int("offset") : 0
        String sortColumn = params.sort ?: "name"
        String sortOrder = params.order ?: "asc"
        Date dateCreatedAfter = params.createdAfter ? Date.parse("MM/dd/yyyy", params.createdAfter) : null
        Date dateCreatedBefore = params.createdBefore ? Date.parse("MM/dd/yyyy", params.createdBefore) : null
        List<ProductField> handlingRequirements = params.list("handlingRequirementId").collect { ProductField.valueOf(it) }

        def query = { isCountQuery ->

            if (isCountQuery) {
                projections {
                    countDistinct "id"
                }
            }
            else {
                setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                def fields = params.fields ? params.fields.split(",") : null
                log.info "Fields: " + fields
                if (fields) {
                    projections {
                        fields.each { field ->
                            property(field)
                        }
                    }
                }
            }


            if (!includeInactive) {
                eq("active", true)
            }

            if (categories) {
                if (params.includeCategoryChildren) {
                    'in'("category", categories + categories*.children?.flatten())
                } else {
                    'in'("category", categories)
                }
            }

            if (glAccounts) {
                glAccount {
                    'in'("id", glAccounts.collect { it.id })
                }
            }

            if (productFamilies) {
                productFamily {
                    'in'("id", productFamilies.collect { it.id })
                }
            }

            if (tagsInput) {
                tags {
                    'in'("id", tagsInput.collect { it.id })
                }
            }

            if (!handlingRequirements.empty) {
                or {
                    if (handlingRequirements.contains(ProductField.COLD_CHAIN)) {
                        eq("coldChain", true)
                    }
                    if (handlingRequirements.contains(ProductField.CONTROLLED_SUBSTANCE)) {
                        eq("controlledSubstance", true)
                    }
                    if (handlingRequirements.contains(ProductField.HAZARDOUS_MATERIAL)) {
                        eq("hazardousMaterial", true)
                    }
                    if (handlingRequirements.contains(ProductField.RECONDITIONED)) {
                        eq("reconditioned", true)
                    }
                }
            }

            if (catalogsInput) {
                productCatalogItems {
                    "in"("productCatalog", catalogsInput)
                }
            }
            or {
                if (params.name) {
                    createAlias("synonyms", "synonym", JoinType.LEFT_OUTER_JOIN, Restrictions.like("synonymTypeCode", SynonymTypeCode.DISPLAY_NAME) )
                    ilike("name", "%" + params.name.replaceAll(" ", "%") + "%")
                    and {
                        ilike("synonym.name", "%" + params.name.replaceAll(" ", "%") + "%")
                        eq("synonym.synonymTypeCode", SynonymTypeCode.DISPLAY_NAME)
                    }
                }
                if (params.description) ilike("description", params.description + "%")
                if (params.brandName) ilike("brandName", "%" + params?.brandName?.trim() + "%")
                if (params.manufacturer) ilike("manufacturer", "%" + params?.manufacturer?.trim() + "%")
                if (params.manufacturerCode) ilike("manufacturerCode", "%" + params?.manufacturerCode?.trim() + "%")
                if (params.vendor) ilike("vendor", "%" + params?.vendor?.trim() + "%")
                if (params.vendorCode) ilike("vendorCode", "%" + params?.vendorCode?.trim() + "%")
                if (params.productCode) ilike("productCode", "%" + params.productCode + "%")
                if (params.unitOfMeasure) ilike("unitOfMeasure", "%" + params.unitOfMeasure + "%")
                if (params.createdById) eq("createdBy.id", params.createdById)
                if (params.updatedById) eq("updatedBy.id", params.updatedById)
                if (params.createdAfter || params.createdBefore) {
                    and {
                        if (params.createdAfter) ge("dateCreated", dateCreatedAfter)
                        if (params.createdBefore) le("dateCreated", dateCreatedBefore)
                    }
                }

                if (params.unitOfMeasureIsNull) isNull("unitOfMeasure")
                if (params.productCodeIsNull) isNull("productCode")
                if (params.brandNameIsNull) isNull("brandName")
                if (params.manufacturerIsNull) isNull("manufacturer")
                if (params.manufacturerCodeIsNull) isNull("manufacturerCode")
                if (params.vendorIsNull) isNull("vendor")
                if (params.vendorCodeIsNull) isNull("vendorCode")
            }

            // Sort order
            if (sortColumn) {
                if (sortColumn == "category") {
                    category {
                        order("name", sortOrder)
                    }
                } else if (sortColumn == "updatedBy") {
                    updatedBy(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                        order("firstName", sortOrder)
                        order("lastName", sortOrder)
                    }
                } else {
                    order(sortColumn, sortOrder)
                }
            }
        }

        // Get products
        def products = Product.createCriteria().list(params) {
            query.delegate = delegate
            query(false)
        }

        // Get result count
        def productCount = Product.createCriteria().get() {
            query.delegate = delegate
            query(true)
        }
        products.totalCount = productCount

        return products
    }

    /**
     * Get the root category.
     *
     * @return
     */
    Category getRootCategory() {
        def rootCategory = Category.getRootCategory()
        if (rootCategory) {
            return rootCategory
        }
        def categories = Category.findAllByParentCategoryIsNull()
        if (categories && categories.size() == 1) {
           return categories.get(0)
        }
        def category = new Category()
        category.categories = []
        if (categories && categories.size() > 0) {
            categories.each { category.categories << it }
        }
        return category;
    }

    List<Category> getCategoriesWithoutParent() {
        return Category.findAllByParentCategoryIsNull().sort { a, b ->
            b?.isRoot <=> a?.isRoot ?:
                a?.sortOrder <=> b?.sortOrder ?:
                    a?.name <=> b?.name
        }
    }

    List getCategoryTree() {
        return Category.list()
    }

    List getQuickCategories() {
        List quickCategories = new ArrayList()
        String quickCategoryConfig = grailsApplication.config.inventoryBrowser.quickCategories
        if (quickCategoryConfig) {
            quickCategoryConfig.split(",").each {
                Category c = Category.findByName(it)
                if (c != null) {
                    quickCategories.add(c)
                }
            }
        }
        return quickCategories
    }

    /**
     * Search product and product groups by name.
     *
     * @param term
     * @return
     */
    def searchProductAndProductGroup(String term) {
        return searchProductAndProductGroup(term, false)
    }

    /**
     * Search product and product groups by name using wildcard search.
     *
     * @param term
     * @return
     */
    def searchProductAndProductGroup(String term, Boolean wildcards) {
        long startTime = System.currentTimeMillis()
        def text = (wildcards) ? "%${term.toLowerCase()}%" : "${term.toLowerCase()}%"
        def products = Product.executeQuery(
                """select p.id, p.name, p.productCode 
				from Product as p 				
				where lower(p.name) like ? 
				or lower(p.productCode) like ?""", [text, text])
        println " * Search product and product group: " + (System.currentTimeMillis() - startTime) + " ms"

        return products
    }

    /**
     * Get the column delimiter used in the given data set.
     *
     * @param data
     * @return
     */
    String getDelimiter(String data) {
        // Check to make sure the format is comma-separated
        def lines = data.split("\n")
        def delimiters = [",", "\t", ";"]
        for (def delimiter : delimiters) {
            def headerColumns = lines[0].split(delimiter)
            println "*** Actual:   " + headerColumns + " [" + headerColumns.size() + "]"
            if (headerColumns.size() == Constants.EXPORT_PRODUCT_COLUMNS.size()) {
                return delimiter
            }
        }
        throw new RuntimeException("""Invalid file format: File must contain the following columns: ${
            Constants.EXPORT_PRODUCT_COLUMNS
        };
            Columns must be separated by a comma (,) or tab (\\t);
            lines must be separated by a linefeed (\\n); If you're using Mac Excel, save the file as Windows Comma Separated (.csv) and upload again.""")
    }

    /**
     * Get a list of columns for the data set using the default column delimiter.
     *
     * @param csv
     * @return
     */
    List<String> getColumns(String csv) {
        def delimiter = getDelimiter(csv)
        return getColumns(csv, delimiter)
    }

    /**
     * Get a list of columns for the data set using the given column delimiter.
     *
     * @param csv
     * @param delimiter
     * @return
     */
    List<String> getColumns(String csv, String delimiter) {
        // Check to make sure the format is comma-separated
        def lines = csv.split("\n")
        def columns = lines[0].split(delimiter)
        if (columns.size() != Constants.EXPORT_PRODUCT_COLUMNS.size()) {
            throw new RuntimeException("File must contain the following columns:" + Constants.EXPORT_PRODUCT_COLUMNS)
        }
        return columns
    }


    /**
     * Gets a list of products that already exist in the database
     * @param csv
     * @return
     */
    List<Product> getExistingProducts(String csv) {
        def delimiter = getDelimiter(csv)
        return getExistingProducts(csv, delimiter)
    }

    /**
     * Gets a list of products that already exist in the database.
     *
     * @param csv
     * @param delimiter
     * @return
     */
    List<Product> getExistingProducts(String csv, String delimiter) {
        def products = new ArrayList<Product>()

        // Iterate over each line and either update an existing product or create a new product
        csv.toCsvReader(['skipLines': 1, 'separatorChar': delimiter]).eachLine { tokens ->
            def product = Product.findByIdOrProductCode(tokens[0], tokens[1])
            println "GET EXISTING PRODUCT " + tokens[0] + " OR " + tokens[1]
            if (product) {
                product = Product.get(product.id)
                println "FOUND EXISTING PRODUCT " + product?.id + " " + product?.name
                products << product
            }
        }
        return products

    }

    Boolean parseCsvBooleanField(String value, int rowCount) {
        // If the value is empty we treat it as true
        if (!value) {
            return true
        }
        String parsedValue = value.toLowerCase()
        Set<String> validValues = ['true', 'false', '1', '0']
        if (!(parsedValue in validValues)) {
            throw new RuntimeException("Active field has to be either empty or a boolean value (true/false/1/0) at row " + rowCount)
        }
        return parsedValue in ['true', '1']
    }


    /**
     * Import products from csv
     *
     * ID,Name,Category,Description,Product Code,Unit of Measure,Manufacturer,Manufacturer Code,Cold Chain,UPC,NDC,Date Created,Date Updated
     *
     * @param csv
     */
    List validateProducts(String csv) {
        return validateProducts(csv, getDelimiter(csv))
    }

    /**
     *
     * @param csv
     * @param delimiter
     * @param saveToDatabase
     * @return
     */
    List validateProducts(String csv, String delimiter) {

        def products = []
        if (!csv) {
            throw new RuntimeException("CSV cannot be empty")
        }

        // Check to make sure the format is comma-separated
        def lines = csv.split("\n")
        def columns = lines[0].split(delimiter)
        if (columns.size() != Constants.EXPORT_PRODUCT_COLUMNS.size()) {
            throw new RuntimeException("Invalid data format")
        }

        int rowCount = 1

        Location currentLocation = authService.currentLocation
        ProductType defaultProductType = ProductType.defaultProductType.list()?.first();

        // Iterate over each line and either update an existing product or create a new product
        csv.toCsvReader(['skipLines': 1, 'separatorChar': delimiter]).eachLine { tokens ->

            rowCount++
            println "Processing line: " + tokens
            def productId = tokens[0]
            def active = parseCsvBooleanField(tokens[1], rowCount)
            def productCode = tokens[2]
            def productTypeName = tokens[3]
            def productName = tokens[4]
            def productFamilyName = tokens[5]
            def categoryName = tokens[6]
            def glAccountCode = tokens[7]
            def description = tokens[8]
            def unitOfMeasure = tokens[9]
            def productTags = tokens[10]?.split(",")
            def pricePerUnit
            try {
                pricePerUnit = tokens[11] ? Float.valueOf(tokens[11]) : null
            } catch (NumberFormatException e) {
                throw new RuntimeException("Unit price for product '${productCode}' at row ${rowCount} must be a valid decimal (value = '${tokens[10]}')", e)
            }
            def lotAndExpiryControl = Boolean.valueOf(tokens[12])
            def coldChain = Boolean.valueOf(tokens[13])
            def controlledSubstance = Boolean.valueOf(tokens[14])
            def hazardousMaterial = Boolean.valueOf(tokens[15])
            def reconditioned = Boolean.valueOf(tokens[16])
            def manufacturer = tokens[17]
            def brandName = tokens[18]
            def manufacturerCode = tokens[19]
            def manufacturerName = tokens[20]
            def vendor = tokens[21]
            def vendorCode = tokens[22]
            def vendorName = tokens[23]
            def upc = tokens[24]
            def ndc = tokens[25]

            if (!productName) {
                throw new RuntimeException("Product name cannot be empty at row " + rowCount)
            }
            ProductType productType = null
            if (productTypeName) {
                productType = ProductType.findByName(productTypeName)
                if (!productType) {
                    throw new RuntimeException("Product type with name ${productTypeName} does not exist at row " + rowCount)
                }
                // Throw an error for product type with empty code and product identifier that is not a default product type
                if (productType?.id != defaultProductType?.id && !productType?.code && !productType?.productIdentifierFormat) {
                    throw new RuntimeException("Product type '${productTypeName}' at row ${rowCount} has empty code and empty product identifier format")
                }
            } else {
                throw new RuntimeException("Product type name cannot be empty at row " + rowCount)
            }

            if (currentLocation?.accountingRequired && !glAccountCode) {
                throw new RuntimeException("GL Account code cannot be empty at row " + rowCount)
            }

            GlAccount glAccount = GlAccount.findByCode(glAccountCode)
            if (glAccountCode && !glAccount) {
                throw new RuntimeException("GL Account with code ${glAccountCode} does not exist at row " + rowCount)
            }

            ProductGroup productFamily = productFamilyName ? productGroupService.findOrCreateProductGroup(productFamilyName) : null

            def product = Product.findByIdOrProductCode(productId, productCode)

            // If the identifier is incorrect/missing we should display the ID of the product found using the product code instead of the missing/incorrect product identifier
            def productProperties = [
                id                  : product?.id ?: productId,
                active              : active,
                name                : productName,
                productType         : productType,
                productFamily       : productFamily,
                category            : categoryName,
                glAccount           : glAccount,
                description         : description,
                productCode         : productCode,
                upc                 : upc,
                ndc                 : ndc,
                lotAndExpiryControl : lotAndExpiryControl,
                coldChain           : coldChain,
                controlledSubstance : controlledSubstance,
                hazardousMaterial   : hazardousMaterial,
                reconditioned       : reconditioned,
                tags                : productTags,
                unitOfMeasure       : unitOfMeasure,
                manufacturer        : manufacturer,
                manufacturerCode    : manufacturerCode,
                brandName           : brandName,
                manufacturerName    : manufacturerName,
                vendor              : vendor,
                vendorCode          : vendorCode,
                vendorName          : vendorName,
                product             : product
            ]

            // If the user-entered unit price is different from the current unit price validate the user is allowed to make the change
            if (pricePerUnit) {
                if (pricePerUnit != product?.pricePerUnit) {
                    userService.assertCurrentUserHasRoleFinance()
                    productProperties.pricePerUnit = pricePerUnit
                }
            }

            products << productProperties
        }

        return products
    }


    def importProducts(products) {
        return importProducts(products, null)
    }

    /**
     * Creates or updates the given list of imported products.
     *
     * @param products The products (as key value maps) to import
     * @param tagNamesForAllProducts A list of tags to assign to all products being imported
     */
    List<Product> importProducts(List<Map<String, Object>> products, List<String> tagNamesForAllProducts) {
        log.info("Importing products " + products + " tags: " + tagNamesForAllProducts)

        // The imported categories and tags are just names at this point. Get or create the actual entities now
        // so that we can assign them to the products in the below loop.
        List<String> allCategoryNames = products.category as List<String>
        CategoriesByDesensitizedName importedCategories = findOrCreateCategoriesAsMap(allCategoryNames)

        List<String> allTagNames = products.tags.flatten() as List<String>
        if (tagNamesForAllProducts) {
            allTagNames.addAll(tagNamesForAllProducts)
        }
        Map<String, Tag> tagsMap = getOrCreateTagsAsMap(allTagNames)

        List<Product> importedProducts = []
        products.each { productProperties ->
            log.info "Import product code = " + productProperties.productCode + ", name = " + productProperties.name

            // The product category is still just a name at this point so assign the product a proper Category instance
            // now that they've been created. If the product wasn't assigned a category, assign it the root category.
            productProperties.category = productProperties.category ?
                    importedCategories.get((productProperties.category as String)) :
                    Category.getRootCategory()

            // Assign both the product-specific tags and the globally defined tags to the product.
            List<String> tagNames = []
            List<String> productTagNames = productProperties.tags as List<String>
            if (productTagNames) {
                tagNames.addAll(productTagNames)
            }
            if (tagNamesForAllProducts) {
                tagNames.addAll(tagNamesForAllProducts)
            }
            tagNames = removeBlanksAndDuplicates(tagNames)
            productProperties.tags = tagNames.collect{ tagsMap.get(it) }

            // If the product already exists, update it
            def product = Product.findByIdOrProductCode(productProperties.id, productProperties.productCode)
            if (product) {
                product.properties = productProperties
            }
            // ... else create a new product
            else {
                product = new Product(productProperties)
            }

            if (!product.validate()) {
                throw new ValidationException("Product is invalid", product.errors)
            }

            if (!product?.id || product.validate()) {
                if (!product.productCode) {
                    product.productCode = generateProductIdentifier(product)
                }
            }

            if (!product.save(flush: true)) {
                throw new ValidationException("Could not save product '" + product.name + "'", product.errors)
            }
            importedProducts.add(product)
        }

        return importedProducts
    }


    /**
     * Export all products in the database.
     *
     * @return
     */
    String exportProducts() {
        def products = Product.list()
        return exportProducts(products, false)
    }

    String exportProducts(List<Product> products) {
        return exportProducts(products, false)
    }

    /**
     * Export given products.
     *
     * @param products
     * @return
     */
    String exportProducts(List<Product> products, boolean includeAttributes) {

        def attributes = Attribute.findAllByExportableAndActive(true, true)
        def formatTagLib = grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
        boolean hasRoleFinance = userService.hasRoleFinance()

        def rows = []
        products.each { product ->
            // FIXME make relation to Constants.EXPORT_PRODUCT_COLUMNS explicit
            def row = [
                Id                  : product?.id,
                Active              : product.active ?: Boolean.FALSE,
                ProductCode         : product.productCode ?: '',
                ProductType         : product.productType?.name ?: '',
                Name                : product.name,
                ProductFamily       : product?.productFamily ?: '',
                Category            : product?.category?.name,
                GLAccount           : product?.glAccount?.code ?: '',
                Description         : product?.description ?: '',
                UnitOfMeasure       : product.unitOfMeasure ?: '',
                Tags                : product.tagsToString() ?: '',
                UnitCost            : hasRoleFinance ? (product.pricePerUnit ?: '') : '',
                LotAndExpiryControl : product.lotAndExpiryControl ?: Boolean.FALSE,
                ColdChain           : product.coldChain ?: Boolean.FALSE,
                ControlledSubstance : product.controlledSubstance ?: Boolean.FALSE,
                HazardousMaterial   : product.hazardousMaterial ?: Boolean.FALSE,
                Reconditioned       : product.reconditioned ?: Boolean.FALSE,
                Manufacturer        : product.manufacturer ?: '',
                BrandName           : product.brandName ?: '',
                ManufacturerCode    : product.manufacturerCode ?: '',
                ManufacturerName    : product.manufacturerName ?: '',
                Vendor              : product.vendor ?: '',
                VendorCode          : product.vendorCode ?: '',
                VendorName          : product.vendorName ?: '',
                UPC                 : product.upc ?: '',
                NDC                 : product.ndc ?: '',
                Created             : dateFormatter.formatForExport(product.dateCreated),
                Updated             : dateFormatter.formatForExport(product.lastUpdated),
            ]

            if (includeAttributes) {
                attributes.eachWithIndex { attribute, index ->
                    def productAttribute = product.getProductAttribute(attribute)
                    def attributeName = formatTagLib.metadata(obj: attribute)
                    row << ["${attributeName}": productAttribute?.value ?: '']
                }
            }
            rows << row
        }
        return ReportUtil.getCsvForListOfMapEntries(rows)
    }

    /**
     * A map of Category keyed on the "desensitized" (ie trimmed and lower-cased) category name.
     *
     * We desensitize the name because we want to treat names like "NAME" and "name  " as equivalent, which is useful
     * when importing data since user input errors are common. In other scenarios this equivalency might not be useful
     * (we might want to treat "name" and "NAME" as different), so make sure to be intentional when using this class.
     */
    private class CategoriesByDesensitizedName {
        Map<String, Category> categoryByDesensitizedName = [:]

        CategoriesByDesensitizedName(List<Category> categories) {
            putAll(categories)
        }

        List<Category> putAll(List<Category> categories) {
            categories.each { put(it) }
        }

        Category put(Category category) {
            String desensitizedName = desensitizeName(category.name)
            return categoryByDesensitizedName.put(desensitizedName, category)
        }

        Category get(String categoryName) {
            String desensitizedName = desensitizeName(categoryName)
            return categoryByDesensitizedName.get(desensitizedName)
        }

        private String desensitizeName(String categoryName) {
            if (StringUtils.isBlank(categoryName)) {
                return categoryName
            }
            return categoryName.trim().toLowerCase()
        }
    }

    /**
     * Find or create a list of categories with the given names as a map keyed on "desensitized" (ie trimmed
     * and lower-cased) category name. Nulls, duplicates, and whitespace will be ignored.
     */
    private CategoriesByDesensitizedName findOrCreateCategoriesAsMap(List<String> categoryNames) {
        return new CategoriesByDesensitizedName(findOrCreateCategories(categoryNames))
    }

    /**
     * Find or create a list of categories with the given names. Nulls, duplicates, and whitespace will be ignored.
     */
    private List<Category> findOrCreateCategories(List<String> categoryNames) {
        List<String> categoryNamesSanitized = removeBlanksAndDuplicates(categoryNames)
        if (!categoryNamesSanitized) {
            return Collections.emptyList()
        }

        List<Category> categories = []
        for (categoryName in categoryNamesSanitized) {
            Category category = findOrCreateCategory(categoryName)
            categories.add(category)
        }
        return categories
    }

    private static List<String> removeBlanksAndDuplicates(List<String> strings){
        if (!strings) {
            return Collections.emptyList()
        }

        return strings.findAll{ StringUtils.isNotBlank(it) }
                .collect{ it.trim() }
                .unique()
    }

    /**
     * Find or create a category with the given name. If the category doesn't exist, its parent category will be root.
     */
    private Category findOrCreateCategory(String categoryName) {
        Category category = Category.findByName(categoryName)
        if (category) {
            return category
        }

        Category rootCategory = Category.getRootCategory()
        category = new Category(parentCategory: rootCategory, name: categoryName)

        if (!category.save()) {
            throw new ValidationException("Could not save category: '${categoryName}'", category.errors)
        }
        return category
    }

    /**
     * Find or create a root category with the given name.
     *
     * @param rootCategoryName
     * @return
     */
    Category findOrCreateRootCategory(String rootCategoryName) {
        def rootCategory = Category.findByNameAndIsRoot(rootCategoryName, true)

        if (rootCategory) {
            return rootCategory
        }

        rootCategory = new Category(parentCategory: null, name: rootCategoryName, isRoot: true)
        rootCategory.save(failOnError: true, flush: true)

        return rootCategory
    }

    /**
     * Find or create a category with the given name and parent category name.
     *
     * @param categoryName
     * @param parentCategory
     * @return
     */
    Category findOrCreateCategoryWithParentCategory(String categoryName, Category parentCategory) {
        def category = Category.findByName(categoryName)
        if (!category) {
            category = new Category(parentCategory: parentCategory, name: categoryName)
            category.save(failOnError: true)
        }
        return category
    }

    /**
     * Find all top-level categories (e.g. children of the root category)
     *
     * @return
     */
    def getTopLevelCategories() {
        def rootCategory = Category.getRootCategory()
        return rootCategory ? Category.findAllByParentCategory(rootCategory) : []
    }

    /**
     *
     * Get all unique and active tags in the database.
     *
     * @return all tag labels
     */
    def getAllTagLabels() {
        return Tag.findAllByIsActive(true).collect { it.tag }.unique()
    }

    /**
     * Get all active tags in the database.
     *
     * @return all tags
     */
    def getAllTags() {
        def tags = Tag.findAllByIsActive(true)
        return tags
    }

    /**
     * Get all active catalogs in the database.
     *
     * @return all tags
     */
    def getAllCatalogs() {
        return ProductCatalog.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            createAlias('productCatalogItems','productCatalogItems')
            projections {
                groupProperty("id", "id")
                groupProperty("name", "name")
                count("productCatalogItems.id", "count")
            }
            eq("active", Boolean.TRUE)
        }
    }

    /**
     * Get all popular tags
     *
     * @return all tags that have a product
     */
    def getPopularTags(Integer limit) {
        def popularTags = [:]
        String sql = """
            select tag.id, tag.tag, count(*) as count
            from product_tag
            join tag on tag.id = product_tag.tag_id
            where tag.is_active = true
            group by tag.id, tag.tag
            order by count(*) desc
            """


        // FIXME Convert the query above to HQL so we don't have to worry about N+1 query below
        def sqlQuery = sessionFactory.currentSession.createSQLQuery(sql)
        if (limit > 0) {
            sqlQuery.setMaxResults(limit)
        }
        def list = sqlQuery.list()
        list.each {
            Tag tag = Tag.load(it[0])
            popularTags[tag] = it[2]
        }
        return popularTags
    }

    def getPopularTags() {
        return getPopularTags(0)
    }

    /**
     * Find or create a list of product tags with the given names as a map keyed on tag name. Nulls, duplicates,
     * and whitespace will be ignored.
     */
    private Map<String, Tag> getOrCreateTagsAsMap(List<String> tagNames) {
        return getOrCreateTags(tagNames).collectEntries{ [ it.tag, it ] }
    }

    /**
     * Find or create a list of product tags with the given names. Nulls, duplicates, and whitespace will be ignored.
     */
    private List<Tag> getOrCreateTags(List<String> tagNames) {
        List<String> tagNamesSanitized = removeBlanksAndDuplicates(tagNames)
        if (!tagNamesSanitized) {
            return Collections.emptyList()
        }

        List<Tag> tags = []
        for (tagName in tagNamesSanitized) {
            Tag tag = findOrCreateTag(tagName)
            tags.add(tag)
        }
        return tags
    }

    /**
     * Find or create a tag with the given name.
     */
    private Tag findOrCreateTag(String tagName) {
        Tag tag = Tag.findByTag(tagName)
        if (tag) {
            return tag
        }

        tag = new Tag(tag: tagName)
        if (!tag.save()) {
            throw new ValidationException("Could not save tag: '${tagName}'", tag.errors)
        }
        return tag
    }

    /**
     * Delete a tag from the given product and delete the tag.
     *
     * @param product
     * @param tag
     * @return
     */
    def deleteTag(product, tag) {
        product.removeFromTags(tag)
        tag.delete()
    }

    /**
     * @return A generated identifier for the given product.
     */
    String generateProductIdentifier(Product product) {
        return productIdentifierService.generate(product)
    }

    /**
     * Save the given product
     *
     * @param product
     * @return
     */
    def saveProduct(Product product) {
        return saveProduct(product, null)
    }

    /**
     * Saves the given product
     * @param product
     * @param tags
     *
     * @return
     */
    def saveProduct(Product product, String tags) {
        if (product) {
            // Generate product code if it doesn't already exist
            if (!product.productCode) {
                product.productCode = generateProductIdentifier(product)
            }
            // Handle tags
            try {
                if (tags) {
                    tags.split(",").each { tagText ->
                        def tag = findOrCreateTag(tagText)
                        if (tag) {
                            product.addToTags(tag)
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error occurred: " + e.message)
                throw new ValidationException(e.message, product?.errors)
            }

            return product.save(flush: true)
        }
    }

    Product addProductComponent(String assemblyProductId, String componentProductId, BigDecimal quantity, String unitOfMeasureId) {
        def assemblyProduct = Product.get(assemblyProductId)
        if (assemblyProduct) {
            def componentProduct = Product.get(componentProductId)
            if (componentProduct) {
                def unitOfMeasure = UnitOfMeasure.get(unitOfMeasureId)
                log.info "Adding " + componentProduct.name + " to " + assemblyProduct.name

                ProductComponent productComponent = new ProductComponent(componentProduct: componentProduct,
                        quantity: quantity, unitOfMeasure: unitOfMeasure, assemblyProduct: assemblyProduct)
                assemblyProduct.addToProductComponents(productComponent)
                assemblyProduct.save(flush: true, failOnError: true)
            }
        }
        return assemblyProduct
    }

    List parseProductCatalogItems(def csv) {
        List rows = []

        // Iterate over each line and either update an existing product or create a new product
        csv.toCsvReader(['skipLines': 1]).eachLine { tokens ->

            if (tokens.length != 4) {
                throw new IllegalArgumentException("Expected columns: Catalog Code, Category, Product Code, Product Name")
            }

            def productCatalogCode = tokens[0]
            def productCatalog = ProductCatalog.findByCode(productCatalogCode)

            def productCode = tokens[1]
            def product = Product.findByIdOrProductCode(productCode, productCode)

            rows << [productCatalog: productCatalog, product: product]
        }

        return rows
    }

    List<ProductAssociation> getProductAssociations(Product product, List<ProductAssociationTypeCode> types) {
        return ProductAssociation.createCriteria().list {
            eq("product", product)
            if (types) {
                'in'("code", types)
            }
            associatedProduct {
                eq("active", true)
            }
        }
    }

    /**
     * Get all products matching the given terms and categories.
     *
     * @param terms
     * @param categories
     * @return
     */
    def searchProducts(String[] terms, List<Category> categories, boolean returnIds = false) {
        def results = Product.createCriteria().list {
            if (returnIds) {
                projections {
                    property("id")
                }
            }
            createAlias('productSuppliers', 'ps', JoinType.LEFT_OUTER_JOIN)
            createAlias('productSuppliers.manufacturer', 'psm', JoinType.LEFT_OUTER_JOIN)
            createAlias('productSuppliers.supplier', 'pss', JoinType.LEFT_OUTER_JOIN)
            createAlias('inventoryItems', 'ii', JoinType.LEFT_OUTER_JOIN)
            createAlias('synonyms', 'synonym', JoinType.LEFT_OUTER_JOIN)

            eq("active", true)
            if (categories) {
                inList("category", categories)
            }
            if (terms) {
                terms.each { term ->
                    term = term + "%"
                    or {
                        ilike("name", "%" + term)
                        ilike("productCode", term)
                        and {
                            ilike("synonym.name", "%" + term)
                            eq("synonym.synonymTypeCode", SynonymTypeCode.DISPLAY_NAME)
                        }
                        ilike("description", "%" + term)
                        ilike("brandName", term)
                        ilike("manufacturerCode", term)
                        ilike("vendorCode", term)
                        ilike("upc", term)
                        ilike("ndc", term)
                        ilike("unitOfMeasure", term)
                        ilike("ps.name", "%" + term)
                        ilike("ps.code", term)
                        ilike("ps.productCode", term)
                        ilike("ps.brandName", term)
                        ilike("ps.manufacturerCode", term)
                        ilike("ps.manufacturerName", term)
                        ilike("ps.supplierCode", term)
                        ilike("ps.supplierName", term)
                        ilike("psm.name", term)
                        ilike("pss.name", term)
                        ilike("ii.lotNumber", term)
                    }
                }
            }
        }

        return results
    }

    def searchProductDtos(String[] terms) {
        String locale = LocalizationUtil.localizationService.getCurrentLocale().toString()

        def query = """
            select distinct
            product.id, 
            product.name,
            product.active,
            product.product_code as productCode, 
            product.cold_chain as coldChain, 
            product.controlled_substance as controlledSubstance, 
            product.hazardous_material as hazardousMaterial, 
            product.reconditioned,
            product.unit_of_measure as unitOfMeasure,
            product.lot_and_expiry_control as lotAndExpiryControl,
            # Return whether search term returns an exact match
            ifnull(
                product.product_code = '${terms.join(" ")}' or 
                product.upc = '${terms.join(" ")}' or 
                product.ndc = '${terms.join(" ")}' or 
                product_supplier.supplier_code = '${terms.join(" ")}' or
                product_supplier.manufacturer_code = '${terms.join(" ")}', false
            ) as exactMatch,
            (
                select max(pc.color) 
                from product_catalog_item pci 
                left outer join product_catalog pc on pci.product_catalog_id = pc.id 
                where pci.product_id = product.id 
                group by pci.product_id
            ) as productColor,
            (
                select s.name from synonym s
                where s.product_id = product.id
                and s.synonym_type_code = '${SynonymTypeCode.DISPLAY_NAME}'
                and s.locale = '${locale}'
                limit 1
            ) as displayName
            from product """

        if (terms && terms.size() > 0) {
            query += """
            left outer join product_supplier 
                on product.id = product_supplier.product_id
            left outer join synonym 
                on product.id = synonym.product_id
            left outer join party manufacturer 
                on product_supplier.manufacturer_id = manufacturer.id 
                and (${terms.collect { "lower(manufacturer.name) like '${it}%'" }.join(" or ")}) # adding the conditions to join will allow MySQL to optimize the query
            left outer join party supplier 
                on product_supplier.supplier_id = supplier.id 
                and (${terms.collect { "lower(supplier.name) like '${it}%'" }.join(" or ")})
            left outer join inventory_item 
                on product.id = inventory_item.product_id 
                and (${terms.collect { "lower(inventory_item.lot_number) like '${it}%'" }.join(" or ")})
            where product.active = 1 and (${terms.collect {"""
                lower(product.name) like '%${it}%' 
                or lower(product.product_code) like '${it}%' 
                or (synonym.synonym_type_code = '${SynonymTypeCode.DISPLAY_NAME}' and synonym.name like '%${it}%')
                or lower(product.description) like '%${it}%'
                or lower(product.brand_name) like '${it}%' 
                or lower(product.manufacturer_code) like '${it}%' 
                or lower(product.vendor_code) like '${it}%'
                or lower(product.upc) like '${it}%' 
                or lower(product.ndc) like '${it}%'
                or lower(product.unit_of_measure) like '${it}%' 
                or lower(product_supplier.name) like '%${it}%' 
                or lower(product_supplier.code) like '${it}%'
                or lower(product_supplier.product_code) like '${it}%' 
                or lower(product_supplier.brand_name) like '${it}%'
                or lower(product_supplier.manufacturer_code) like '${it}%'
                or lower(product_supplier.manufacturer_name) like '${it}%' 
                or lower(product_supplier.supplier_code) like '${it}%'
                or lower(product_supplier.supplier_name) like '${it}%'""" }.join(" or ")}
                # when the condition is added to the join, we still need to check if there were any results
                or manufacturer.id is not null  
                or supplier.id is not null
                or inventory_item.id is not null)
            order by productCode"""
        } else {
            query += " where product.active = 1 "
        }

        def results = dataService.executeQuery(query)

        return results.collect { new ProductSearchDto(it) }
    }

    def importCategories(String categoryOption) {
        def enabled = grailsApplication.config.openboxes.configurationWizard.enabled

        if (enabled && categoryOption) {
            def categoryOptionConfig = grailsApplication.config.openboxes.configurationWizard.categoryOptions[categoryOption]

            if (!categoryOptionConfig) {
                throw new Exception("There is no category option with the code: ${categoryOption}")
            }

            if (!categoryOptionConfig.enabled) {
                return
            }

            // TODO: get this part working with [classpath:, file://, https://] (currently it does not support classpath)
            def fileContent = new URL(categoryOptionConfig.fileUrl).getBytes()
            String csv = new String(fileContent)

            // Find existing root category or create one with the configured root name or the Constants.ROOT_CATEGORY_NAME
            def rootCategoryName = categoryOptionConfig.rootCategoryName ?: Constants.ROOT_CATEGORY_NAME

            def categoryNameColumnIndex = categoryOptionConfig.categoryNameColumnIndex ?: 0
            def parentCategoryNameColumnIndex = categoryOptionConfig.parentCategoryNameColumnIndex ?: 1

            importCategoryCsv(csv, rootCategoryName, categoryNameColumnIndex, parentCategoryNameColumnIndex)
        } else if (enabled && !categoryOption) {
            // TODO OBDS-86: This is for excel import done by user
        }
    }

    def importCategoryCsv(String csv) {
        importCategoryCsv(csv, Constants.ROOT_CATEGORY_NAME, 0, 1)
    }

    def importCategoryCsv(String csv, def rootCategoryName, def categoryNameColumnIndex, def parentCategoryNameColumnIndex) {
        def rootCategory = findOrCreateRootCategory(rootCategoryName)

        def settings = [separatorChar: ',', skipLines: 1]
        csv.toCsvReader(settings).eachLine { tokens ->
            def categoryName = tokens[categoryNameColumnIndex]
            def parentCategoryName = tokens[parentCategoryNameColumnIndex]

            def category
            if (parentCategoryName.toUpperCase() == rootCategoryName) {
                category = findOrCreateCategoryWithParentCategory(categoryName, rootCategory)
            } else {
                def parentCategory = Category.findByName(parentCategoryName)
                category = findOrCreateCategoryWithParentCategory(categoryName, parentCategory)
            }
        }
    }

    def importProductsFromConfig(String productOption) {
        def enabled = grailsApplication.config.openboxes.configurationWizard.enabled

        if (enabled && productOption) {
            def productOptionConfig = grailsApplication.config.openboxes.configurationWizard.productOptions[productOption]

            if (!productOptionConfig) {
                throw new Exception("There is no product option with the code: ${productOption}")
            }

            if (!productOptionConfig.enabled) {
                return
            }

            // TODO: get this part working with [classpath:, file://, https://] (currently it does not support classpath)
            def fileContent = new URL(productOptionConfig.fileUrl).getBytes()
            String csv = new String(fileContent)

            def products = validateProducts(csv)

            importProducts(products)
        }
    }

    Product addSynonymToProduct(String productId, String synonymTypeCodeName, String synonymValue, String localeName) {
        Product product = Product.get(productId)
        Locale locale = localeName ? LocalizationUtil.getLocale(localeName) : null
        SynonymTypeCode synonymTypeCode = synonymTypeCodeName ? SynonymTypeCode.valueOf(synonymTypeCodeName) : SynonymTypeCode.ALTERNATE_NAME
        Synonym synonym = new Synonym(name: synonymValue, locale: locale, synonymTypeCode: synonymTypeCode)
        product.addToSynonyms(synonym)
        if (!synonym.validate() || !product.save(flush: true, failOnError: true)) {
            throw new ValidationException("Invalid synonym", synonym.errors)
        }
        return product
    }

    Synonym editProductSynonym(String synonymId, String synonymTypeCodeName, String synonymValue, String localeName) {
        Synonym synonym = Synonym.get(synonymId)
        Locale locale = localeName ? LocalizationUtil.getLocale(localeName) : null
        SynonymTypeCode synonymTypeCode = null
        try {
            if (synonymTypeCodeName) {
                synonymTypeCode = SynonymTypeCode.valueOf(synonymTypeCodeName)
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("classification of type '${synonymTypeCodeName}' does not exist")
        }

        synonym.properties = [ locale: locale, synonymTypeCode: synonymTypeCode, name: synonymValue ]
        if (!synonym.validate() || !synonym.save(flush: true)) {
            throw new ValidationException("Invalid synonym", synonym.errors)
        }
        return synonym
    }

    Map<String, Timestamp> latestInventoryDateForProducts(List<String> productIds) {
        return latestInventoryDateForProducts(AuthService.currentLocation, productIds)
    }

    Map<String, Timestamp> latestInventoryDateForProducts(Location location, List<String> productIds) {
        List<String> transactionTypeIds = configService.getProperty('openboxes.inventoryCount.transactionTypes', List) as List<String>

        Inventory inventory = location.inventory

        String hql = """
            select ii.product.id, max(t.transactionDate)
            from TransactionEntry te
            join te.transaction t
            join te.inventoryItem ii
            where ii.product.id in (:productIds)
              and t.inventory = :inventory
              and t.transactionType.id in (:transactionTypeIds)
              and (t.comment <> :commentToFilter or t.comment IS NULL)
            group by ii.product.id
        """

        List<Object[]> results = TransactionEntry.executeQuery(hql, [
                productIds: productIds,
                inventory: inventory,
                transactionTypeIds: transactionTypeIds,
                commentToFilter: Constants.INVENTORY_BASELINE_MIGRATION_TRANSACTION_COMMENT
        ])

        // Convert list to a map for O(1) accessibility further
        return results.collectEntries { [ (it[0]): it[1] ] }
    }

    Map<String, List<Map<String, Object>>> getLotNumbersWithExpirationDate(List<String> productIds) {
        List<Object[]> productLotRows = Product.createCriteria().list {
            'in'('id', productIds)
            inventoryItems {
                isNotNull('lotNumber')
                ne('lotNumber', '')
                projections {
                    property('product.id', 'productId')
                    property('lotNumber', 'lotNumber')
                    property('expirationDate', 'expirationDate')
                }
            }
        }

        Map<String, List<Map<String, Object>>> result = productLotRows.groupBy { it[0] as String }
            .collectEntries {String productId, List<Object[]> lots ->
                [
                        productId,
                        lots.collect { Object[] row ->
                            [
                                    lotNumber     : row[1],
                                    expirationDate: row[2]
                            ]
                        }
                        .unique { it.lotNumber }
                ]
            }

        return result
    }
}
