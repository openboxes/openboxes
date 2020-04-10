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

import grails.validation.ValidationException
import groovy.xml.Namespace
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.core.ApiException
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.importer.ImportDataCommand
import util.ReportUtil

import java.text.SimpleDateFormat

/**
 * @author jmiranda*
 */
class ProductService {

    def sessionFactory
    def grailsApplication
    def identifierService
    def userService

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
        return getProducts(category, [], tags, includeInactive, params)
    }

    /**
     * Get all products that match category, tags, and other search parameters.
     *
     * @param category
     * @param catalogs
     * @param tags
     * @param params
     * @return
     */
    List<Product> getProducts(Category category, List<ProductCatalog> catalogsInput, List<Tag> tagsInput, boolean includeInactive, Map params) {
        log.info "get products where category=" + category + ", catalogs=" + catalogsInput + ", tags=" + tagsInput + ", params=" + params

        int max = params.max ? params.int("max") : 10
        int offset = params.offset ? params.int("offset") : 0
        String sortColumn = params.sort ?: "name"
        String sortOrder = params.order ?: "asc"

        def results = Product.createCriteria().list(max: max, offset: offset) {

            def fields = params.fields ? params.fields.split(",") : null
            log.info "Fields: " + fields
            if (fields) {
                projections {
                    fields.each { field ->
                        property(field)
                    }
                }
            }
            if (!includeInactive) {
                eq("active", true)
            }
            and {
                if (category) {
                    if (params.includeCategoryChildren) {
                        def categories = category.children ?: []
                        categories << category

                        println "Categories to search in " + categories
                        'in'("category", categories)
                    } else {
                        println "Equality search " + category
                        eq("category", category)
                    }
                }


                or {
                    if (tagsInput) {
                        tags {
                            'in'("id", tagsInput.collect { it.id })
                        }
                    }

                    if (catalogsInput) {
                        productCatalogItems {
                            "in"("productCatalog", catalogsInput)
                        }
                    }
                }

                or {
                    if (params.name) ilike("name", "%" + params.name.replaceAll(" ", "%") + "%")
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

                    if (params.unitOfMeasureIsNull) isNull("unitOfMeasure")
                    if (params.productCodeIsNull) isNull("productCode")
                    if (params.brandNameIsNull) isNull("brandName")
                    if (params.manufacturerIsNull) isNull("manufacturer")
                    if (params.manufacturerCodeIsNull) isNull("manufacturerCode")
                    if (params.vendorIsNull) isNull("vendor")
                    if (params.vendorCodeIsNull) isNull("vendorCode")
                }
            }
            if (offset) firstResult(offset)
            if (max) maxResults(max)
            if (sortColumn) order(sortColumn, sortOrder)
        }

        return results.unique()
    }

    /**
     * Get the root category.
     *
     * @return
     */
    Category getRootCategory() {
        def rootCategory = Category.getRootCategory()
        if (!rootCategory) {
            def categories = Category.findAllByParentCategoryIsNull()
            if (categories && categories.size() == 1) {
                rootCategory = categories.get(0)
            } else {
                rootCategory = new Category()
                rootCategory.categories = []
                categories.each { rootCategory.categories << it }
            }
        }
        return rootCategory
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
     * Validate the data in the given import command object.
     *
     * @param command
     */
    void validateData(ImportDataCommand command) {
        log.info "validate data test "
        // Iterate over each row and validate values
        command?.data?.each { Map params ->
            log.info "validate data " + params
            params.prompts = [:]
            params.prompts["product.id"] = Product.findAllByActiveAndNameLike(true, "%" + params.search1 + "%")
        }
    }

    /**
     * Import the data in the given import command object
     * @param command
     */
    void importData(ImportDataCommand command) {
        log.info "import data"

        try {
            // Iterate over each row
            command?.data?.each { Map params ->
                log.info "import data " + params
            }
        } catch (Exception e) {
            log.error("Error importing data ", e)
            throw e
        }

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

        // Iterate over each line and either update an existing product or create a new product
        csv.toCsvReader(['skipLines': 1, 'separatorChar': delimiter]).eachLine { tokens ->

            rowCount++
            println "Processing line: " + tokens
            def productId = tokens[0]
            def productCode = tokens[1]
            def productName = tokens[2]
            def categoryName = tokens[3]
            def description = tokens[4]
            def unitOfMeasure = tokens[5]
            def productTags = tokens[6]?.split(",")
            def pricePerUnit
            try {
                pricePerUnit = tokens[7] ? Float.valueOf(tokens[7]) : null
            } catch (NumberFormatException e) {
                throw new RuntimeException("Unit price for product '${productCode}' at row ${rowCount} must be a valid decimal (value = '${tokens[7]}')", e)
            }
            def manufacturer = tokens[8]
            def brandName = tokens[9]
            def manufacturerCode = tokens[10]
            def manufacturerName = tokens[11]
            def vendor = tokens[12]
            def vendorCode = tokens[13]
            def vendorName = tokens[14]
            def coldChain = Boolean.valueOf(tokens[15])
            def upc = tokens[16]
            def ndc = tokens[17]

            if (!productName) {
                throw new RuntimeException("Product name cannot be empty at row " + rowCount)
            }

            def category = findOrCreateCategory(categoryName)
            def product = Product.findByIdOrProductCode(productId, productCode)

            // If the identifier is incorrect/missing we should display the ID of the product found using the product code instead of the missing/incorrect product identifier
            def productProperties = [
                    id              : product?.id ?: productId,
                    name            : productName,
                    category        : category,
                    description     : description,
                    productCode     : productCode,
                    upc             : upc,
                    ndc             : ndc,
                    coldChain       : coldChain,
                    tags            : productTags,
                    unitOfMeasure   : unitOfMeasure,
                    manufacturer    : manufacturer,
                    manufacturerCode: manufacturerCode,
                    brandName       : brandName,
                    manufacturerName: manufacturerName,
                    vendor          : vendor,
                    vendorCode      : vendorCode,
                    vendorName      : vendorName,
                    product         : product
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

    def importProducts(products, tags) {
        log.info("Importing products " + products + " tags: " + tags)

        // Create all tags that don't already exist
        tags.each { tagName ->
            Tag tag = Tag.findByTag(tagName)
            if (!tag) {
                log.info "Tag ${tagName} does not exist so creating it"
                tag = new Tag(tag: tagName)
                tag.save(flush: true)
            }
        }

        products.each { productProperties ->

            log.info "Import product code = " + productProperties.productCode + ", name = " + productProperties.name
            // Update existing
            def product = Product.findByIdOrProductCode(productProperties.id, productProperties.productCode)
            if (product) {
                product.properties = productProperties
            }
            // ... or create a new product
            else {
                product = new Product(productProperties)
            }

            if (!product.validate()) {
                throw new ValidationException("Product is invalid", product.errors)
            }

            addTagsToProduct(product, tags)
            addTagsToProduct(product, productProperties.tags)

            if (!product.save(flush: true)) {
                throw new ValidationException("Could not save product '" + product.name + "'", product.errors)
            }
        }
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

        def formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        def attributes = Attribute.findAllByExportableAndActive(true, true)
        def formatTagLib = grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
        boolean hasRoleFinance = userService.hasRoleFinance()

        def rows = []
        products.each { product ->
            def row = [
                    Id              : product?.id,
                    ProductCode     : product.productCode ?: '',
                    Name            : product.name,
                    Category        : product?.category?.name,
                    Description     : product?.description ?: '',
                    UnitOfMeasure   : product.unitOfMeasure ?: '',
                    Tags            : product.tagsToString() ?: '',
                    UnitCost        : hasRoleFinance ? (product.pricePerUnit ?: '') : '',
                    Manufacturer    : product.manufacturer ?: '',
                    BrandName       : product.brandName ?: '',
                    ManufacturerCode: product.manufacturerCode ?: '',
                    ManufacturerName: product.manufacturerName ?: '',
                    Vendor          : product.vendor ?: '',
                    VendorCode      : product.vendorCode ?: '',
                    VendorName      : product.vendorName ?: '',
                    ColdChain       : product.coldChain ?: Boolean.FALSE,
                    UPC             : product.upc ?: '',
                    NDC             : product.ndc ?: '',
                    Created         : product.dateCreated ? "${formatDate.format(product.dateCreated)}" : "",
                    Updated         : product.lastUpdated ? "${formatDate.format(product.lastUpdated)}" : "",
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
     * Find or create a category with the given name.
     *
     * @param categoryName
     * @return
     */
    Category findOrCreateCategory(String categoryName) {
        def rootCategory = Category.getRootCategory()

        if (!categoryName)
            return rootCategory

        def category = Category.findByName(categoryName)
        if (!category) {
            category = new Category(parentCategory: rootCategory, name: categoryName)
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
     * Add a list of tags to each of the given products.
     *
     * @param products
     * @param tags
     * @return
     */
    def addTagsToProducts(products, tags) {
        log.info "Add tags ${tags} to products ${products}"
        products.each { product ->
            addTagsToProduct(product, tags)
        }
    }

    /**
     * Add the list of tags to the given product.
     *
     * @param product
     * @param tags
     */
    def addTagsToProduct(product, tags) {
        log.info "Add tags ${tags} to product ${product}"
        if (tags) {
            tags.each { tagName ->
                if (tagName) {
                    addTagToProduct(product, tagName)
                }
            }
        }
    }

    /**
     * Add the given tag to the given product.
     *
     * @param product
     * @param tagName
     * @return
     */
    def addTagToProduct(product, tagName) {
        log.info "Add tags ${tagName} to product ${product}"

        // Check if the product already has the given tag
        def tag = product.tags.find { it.tag == tagName }

        if (!tag) {
            // Otherwise try to find an existing tag that matches the tag
            tag = Tag.findByTag(tagName)
            // Or create a brand new one
            if (!tag) {
                log.info "Tag ${tagName} does not exist so creating it"
                tag = new Tag(tag: tagName)
                tag.save(flush: true)
            }
            product.addToTags(tag)
            product.save(flush: true)
        } else {
            log.info "Product ${product} already contains tag ${tag}"
        }
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
     * Ensure that the given product code does not exist
     *
     * @param productCode
     * @return
     */
    def validateProductIdentifier(productCode) {
        if (!productCode) return false
        def count = Product.executeQuery("select count(p.productCode) from Product p where productCode = :productCode", [productCode: productCode])
        return count ? (count[0] == 0) : false
    }

    /**
     * Generate a product identifier.
     *
     * @return
     */
    def generateProductIdentifier() {
        def productCode

        try {
            productCode = identifierService.generateProductIdentifier()
            if (validateProductIdentifier(productCode)) {
                return productCode
            }

        } catch (Exception e) {
            log.warn("Error generating unique product code " + e.message, e)
        }
        return productCode
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
                product.productCode = generateProductIdentifier()
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

    /**
     * Find or create a tag with the given tag text.
     *
     * @param tagText
     * @return
     */
    def findOrCreateTag(tagText) {
        Tag tag = Tag.findByTagAndIsActive(tagText, true)
        if (!tag) {
            tag = new Tag(tag: tagText)
            tag.save()
        }
        return tag
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
    List<Product> searchProducts(String[] terms, List<Category> categories) {
        def results = Product.createCriteria().list {

            eq("active", true)
            if (categories) {
                inList("category", categories)
            }
            if (terms) {
                terms.each { term ->
                    term = term + "%"
                    or {
                        ilike("name", term)
                        ilike("productCode", term)
                        ilike("description", "%" + term)
                        ilike("brandName", term)
                        ilike("manufacturer", term)
                        ilike("manufacturerCode", term)
                        ilike("manufacturerName", term)
                        ilike("vendor", term)
                        ilike("vendorCode", term)
                        ilike("vendorName", term)
                        ilike("upc", term)
                        ilike("ndc", term)
                        ilike("unitOfMeasure", term)
                        productSuppliers {
                            or {
                                ilike("name", term)
                                ilike("code", term)
                                ilike("productCode", term)
                                ilike("manufacturerCode", term)
                                ilike("manufacturerName", term)
                                ilike("supplierCode", term)
                                ilike("supplierName", term)
                                manufacturer {
                                    ilike("name", term)
                                }
                                supplier {
                                    ilike("name", term)
                                }
                            }
                        }
                        inventoryItems {
                            ilike("lotNumber", term)
                        }
                    }
                }
            }
        }
        return results
    }
}
