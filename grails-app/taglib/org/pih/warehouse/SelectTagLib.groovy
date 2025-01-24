/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

import org.grails.core.artefact.DomainClassArtefactHandler
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.GlAccountType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PaymentMethodType
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.core.UnitOfMeasureType
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustmentType
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.requisition.CommodityClass
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.Shipper
import org.springframework.beans.SimpleTypeConverter
import org.springframework.web.servlet.support.RequestContextUtils as RCU

class SelectTagLib {

    def locationService
    def shipmentService
    def requisitionService
    def organizationService

    //@Cacheable("selectCategoryCache")
    def selectCategory = { attrs, body ->
        attrs.from = Category.list().sort() // { it.name }
        attrs.optionKey = "id"
        attrs.optionValue = {
            it.getHierarchyAsString(" > ")
        }
        out << g.select(attrs)
    }

    def selectRequestReasonCode = { attrs, body ->
        attrs.from = ReasonCode.listRequestReasonCodes()
        attrs.optionValue = { format.metadata(obj: it) }
        out << g.select(attrs)
    }

    def selectChangeQuantityReasonCode = { attrs, body ->
        attrs.from = ReasonCode.listRequisitionQuantityChangeReasonCodes()
        attrs.optionValue = { format.metadata(obj: it) }
        out << g.select(attrs)
    }

    def selectSubstitutionReasonCode = { attrs, body ->
        attrs.from = ReasonCode.listRequisitionSubstitutionReasonCodes()
        attrs.optionValue = { format.metadata(obj: it) }
        out << g.select(attrs)
    }

    def selectInventoryAdjustmentReasonCode = { attrs, body ->
        attrs.from = ReasonCode.listInventoryAdjustmentReasonCodes()
        attrs.optionValue = { format.metadata(obj: it) }
        out << g.select(attrs)
    }

    def selectCancelReasonCode = { attrs, body ->
        attrs.from = ReasonCode.list()
        attrs.optionValue = { format.metadata(obj: it) }
        out << g.select(attrs)
    }

    def selectEntityTypeCode = { attrs, body ->
        attrs.from = EntityTypeCode.values()
        attrs.optionValue = { format.metadata(obj: it) }
        out << g.select(attrs)
    }

    //@Cacheable("selectTagCache")
    def selectTag = { attrs, body ->
        def tags = Tag.list(sort: "tag").collect {
            [id: it.id, name: it.tag, productCount: it?.products?.size()]
        }
        attrs.from = tags
        attrs.value = attrs.value
        attrs.optionKey = "id"
        attrs.optionValue = { it.name + " (" + it.productCount + ")" }
        out << g.select(attrs)
    }

    //@Cacheable("selectProductTypeCache")
    def selectProductType = { attrs, body ->
        def productTypes = ProductType.list(sort: "name").collect {
            [id: it.id, name: it.name]
        }
        attrs.from = productTypes
        attrs.multiple = true
        attrs.value = attrs.value
        attrs.optionKey = "id"
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }

    //@Cacheable("selectTagsCache")
    def selectTags = { attrs, body ->
        def tags = Tag.list(sort: "tag", fetch: [products: "eager"]).collect {
            [id: it.id, name: it.tag, productCount: it?.products?.size()]
        }
        attrs.from = tags
        attrs.multiple = true
        attrs.value = attrs.value
        attrs.optionKey = "id"
        attrs.optionValue = { it.name + " (" + it?.productCount + ")" }
        out << g.select(attrs)
    }

    //@Cacheable("selectCatalogsCache")
    def selectCatalogs = { attrs, body ->
        def catalogs = ProductCatalog.list(sort: "name").collect {
            [id: it.id, name: it.name, productCount: it?.productCatalogItems?.size()]
        }
        attrs.from = catalogs
        attrs.multiple = true
        attrs.value = attrs.value
        attrs.optionKey = "id"
        attrs.optionValue = { it.name + " (" + it?.productCount + ")" }
        out << g.select(attrs)
    }

    //@Cacheable("selectProductFamilyCache")
    def selectProductFamily = { attrs, body ->
        def productGroups = ProductGroup.list(sort: "name").collect {
            [id: it.id, name: it.name]
        }
        attrs.from = productGroups
        attrs.value = attrs.value
        attrs.optionKey = "id"
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }


    def selectRequisitionStatus = { attrs, body ->
        attrs.from = RequisitionStatus.list()
        attrs.optionValue = { it?.name() }
        out << g.select(attrs)
    }


    def selectRequisitionTemplate = { attrs, body ->
        def requisitionCriteria = new Requisition(isTemplate: true)
        requisitionCriteria.origin = session.warehouse
        def requisitionTemplates = requisitionService.getAllRequisitionTemplates(requisitionCriteria, [max: -1, offset: 0])
        requisitionTemplates.sort { it.destination.name }
        attrs.from = requisitionTemplates
        attrs.optionKey = "id"
        attrs.optionValue = {
            it.name + " - ${it.origin.name} - ${it.destination.name} (" + format.metadata(obj: it?.commodityClass) + ")"
        }
        out << g.select(attrs)

    }

    def selectUnitOfMeasure = { attrs, body ->
        UnitOfMeasureType unitOfMeasureType = attrs.unitOfMeasureType ?
                attrs.unitOfMeasureType as UnitOfMeasureType :
                UnitOfMeasureType.QUANTITY
        UnitOfMeasureClass uomClass = UnitOfMeasureClass.findByType(unitOfMeasureType)
        if (uomClass) {
            attrs.from = UnitOfMeasure.findAllByUomClass(uomClass)
        }
        attrs.optionKey = 'id'
        out << g.select(attrs)
    }

    def selectProduct = { attrs, body ->
        attrs.from = Product.findAllByActive(true)
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }

    def selectProductSupplier = { attrs, body ->
        Product product = Product.get(attrs?.product?.id)
        Organization supplier = Organization.get(attrs?.supplier?.id)
        log.info ("product: ${product}, supplier ${supplier}")
        attrs.from = ProductSupplier.findAllByProductAndSupplier(product, supplier).findAll { it.active } ?: []
        attrs.optionKey = 'id'
        attrs.optionValue = { it.code + " - " + it.supplierCode + " - " + (it.manufacturer?.name?:"") + " - " + (it.manufacturerCode?:"") }
        out << g.select(attrs)
    }

    def selectProductPackage = { attrs, body ->
        def product = Product.get(attrs?.product?.id)
        if (product.packages) {
            attrs.noSelection = ["null": "EA/1"]
            attrs.from = product?.packages?.sort()
            attrs.optionKey = "id"
            attrs.optionValue = { it?.uom?.code + "/" + it.quantity + " -- " + it?.uom?.name }
            out << g.select(attrs)
        } else {
            attrs.noSelection = ["null": "EA/1"]
            out << g.select(attrs)

        }
    }

    def selectPreferenceType = { attrs, body ->
        attrs.from = PreferenceType.list()
        attrs.optionKey = "id"
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }

    def selectRatingType = { attrs, body ->
        attrs.from = RatingTypeCode.list()
        out << g.select(attrs)
    }

    def selectOrganization = { attrs, body ->
        attrs.from = organizationService.selectOrganizations(attrs.roleTypes, attrs.active ?: false, attrs.currentOrganizationId)
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }

    def selectPaymentMethodType = { attrs, body ->
        attrs.from = PaymentMethodType.list().sort { it?.name?.toLowerCase() }
        attrs.optionKey = 'id'
        attrs.value = attrs.value
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }

    def selectPaymentTerm = { attrs, body ->
        attrs.from = PaymentTerm.list().sort { it?.name?.toLowerCase() }
        attrs.optionKey = 'id'
        attrs.value = attrs.value
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }

    def selectBudgetCode = { attrs, body ->
        if (attrs.active == "true") {
            attrs.from = BudgetCode.findAllByActive(true)
        } else {
            attrs.from = BudgetCode.list()
        }
        attrs.optionKey = 'id'
        attrs.optionValue = { it.code }
        out << g.select(attrs)
    }

    def selectGlAccountType = { attrs, body ->
        attrs.from = GlAccountType.list()
        attrs.optionKey = 'id'
        attrs.optionValue = { it.code }
        out << g.select(attrs)
    }

    def selectGlAccount = { attrs, body ->
        attrs.from = GlAccount.findAllByActive(true)
        attrs.optionKey = 'id'
        attrs.optionValue = { it.code + " " + it.description }
        out << g.select(attrs)
    }

    def selectOrderAdjustmentTypes = { attrs, body ->
        attrs.from = OrderAdjustmentType.list()
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name }
        out << g.select(attrs)

    }


    def selectOrderItems = { attrs, body ->
        def order = Order.get(attrs.orderId)
        if (!order) {
            throw new IllegalArgumentException("Order items drop down requires a valid order")
        }
        attrs.from = order.listOrderItems()
        attrs.optionKey = 'id'
        attrs.optionValue = { it.product?.displayNameOrDefaultName }
        out << g.select(attrs)
    }

    def selectCurrency = { attrs, body ->
        println "attrs: ${attrs}"
        UnitOfMeasureClass currencyClass = UnitOfMeasureClass.findByType(UnitOfMeasureType.CURRENCY)
        attrs.from = currencyClass ? UnitOfMeasure.findAllByUomClass(currencyClass) : []
        attrs.optionKey = 'code'
        attrs.value = attrs.value ?: currencyClass?.baseUom?.code
        attrs.optionValue = { it.name + " " + it.code }
        out << g.select(attrs)
    }

    def selectShipper = { attrs, body ->
        attrs.from = Shipper.list().sort { it?.name?.toLowerCase() }
        attrs.optionKey = 'id'
        attrs.value = attrs.value
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }

    def selectShipment = { attrs, body ->

        ShipmentStatusCode shipmentStatusCode = attrs.statusCode as ShipmentStatusCode
        def currentLocation = Location.get(session?.warehouse?.id)
        attrs.from = shipmentService.getShipmentsByLocation(null, currentLocation, shipmentStatusCode).sort {
            it?.name?.toLowerCase()
        }
        attrs.optionKey = 'id'
        attrs.optionValue = { it.shipmentNumber + " " + it.name + " - " + it.shipmentItemCount + " items" + " (" + it.origin.name + " to " + it.destination.name + ")" }
        out << g.select(attrs)
    }

    def selectContainer = { attrs, body ->
        def currentLocation = Location.get(session?.warehouse?.id)
        attrs.from = shipmentService.getPendingShipments(currentLocation)
        attrs['data-placeholder'] = attrs['data-placeholder'] ?: g.message(code: 'default.selectAnOption.label', default: 'Select an Option')
        out << render(template: '/taglib/selectContainer', model: [attrs: attrs])

    }


    def selectDepot = { attrs, body ->
        def currentLocation = Location.get(session?.warehouse?.id)
        def locations = Location.findAllByActive(true).findAll { location -> location.isDepot() }.sort {
            it.name
        }
        attrs.from = locations
        attrs.optionKey = 'id'

        attrs.groupBy = 'locationType'
        attrs.value = attrs.value ?: currentLocation?.id
        if (attrs.groupBy) {
            attrs.optionValue = { it.name }
        } else {
            attrs.optionValue = { "" + format.metadata(obj: it?.locationType) + " - " + it.name }
        }
        out << g.select(attrs)
    }


    def selectUser = { attrs, body ->
        attrs.from = User.findAllByActive(true).sort { it.firstName }
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name + " (" + it.username + ")" }
        out << g.select(attrs)
    }

    def selectPersonViaAjax = { attrs, body ->
        attrs.from = attrs.value ? [Person.get(attrs.value)] : []
        attrs.optionKey = 'id'
        out << g.select(attrs)
    }

    def selectPerson = { attrs, body ->
        attrs.from = Person.list().findAll { it.active }.sort { it.firstName }
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }

    def selectRecipient = { attrs, body ->
        attrs.from = Person.findAllByEmailIsNotNull().findAll { it.active }.sort { it.firstName }
        attrs.optionKey = 'email'
        attrs.optionValue = { it.name }
        out << g.select(attrs)
    }

    def selectBinLocation = { attrs, body ->
        def currentLocation = Location.get(session?.warehouse?.id)
        if (currentLocation.hasBinLocationSupport()) {
            attrs.from = Location.findAllByParentLocationAndActive(currentLocation, true).sort {
                it?.name?.toLowerCase()
            }
            attrs.optionKey = 'id'
            attrs.optionValue = 'name'
            out << g.select(attrs)
        } else {
            out << g.message(code: "default.notSupported.label")
            out << g.hiddenField(id: attrs.id, name: attrs.name, value: attrs.value)
        }
    }

    def selectBinLocationWithOptGroup = { attrs, body ->
        def currentLocation = Location.get(session?.warehouse?.id)
        if (currentLocation.hasBinLocationSupport()) {
            attrs.from = locationService.getBinLocations(currentLocation).sort {
                it?.name?.toLowerCase()
            }
            attrs.optionKey = 'id'
            attrs.optionValue = { it.zone ? it.zone.name + ": " + it.name : it.name }
            attrs.groupBy = 'zone'

            out << g.selectWithOptGroup(attrs)
        } else {
            out << g.message(code: "default.notSupported.label")
            out << g.hiddenField(id: attrs.id, name: attrs.name, value: attrs.value)
        }
    }

    def selectBinLocationByLocation = { attrs, body ->
        Location location = Location.get(attrs.id)

        attrs.from = []
        if (location?.hasBinLocationSupport()) {
            attrs.from = locationService.getBinLocations(location).sort {
                it?.name?.toLowerCase()
            }
        }

        attrs["class"] = "chzn-select-deselect"
        attrs["noSelection"] = ["": ""]
        attrs.optionKey = 'id'
        attrs.optionValue = 'name'

        out << g.select(attrs)
    }

    def selectZoneLocationByLocation = { attrs, body ->
        Location location = Location.get(attrs.id)

        attrs.from = location ? locationService.getZones(location) : []
        attrs["class"] = "chzn-select-deselect"
        attrs["noSelection"] = ["null": ""]
        attrs.optionKey = 'id'
        attrs.optionValue = 'name'

        out << g.select(attrs)
    }

    def selectLocationWithOptGroup = { attrs, body ->

        if (!attrs.from) {
            attrs.from = locationService.getAllLocations().sort { it?.name?.toLowerCase() }
        }
        attrs.groupBy = 'locationType'
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name }

        out << g.selectWithOptGroup(attrs)
    }


    /**
     * For select lists that use Ajax, we just need to load the selected location(s) so that it
     * will be selected.
     */
    def selectLocationViaAjax = { attrs, body ->
        attrs.from = attrs.value ? [Location.get(attrs.value)] : []
        attrs.optionKey = 'id'
        out << g.select(attrs)
    }

    def selectLocation = { attrs, body ->

        // If attrs.from is populated use that by default even if it's empty
        if (!attrs.containsKey("from")) {
            ActivityCode activityCode = attrs.activityCode ?: null
            Map locationListAttributes = [withOrganization: attrs.withOrganization]
            attrs.from = locationService.getLocations(null, locationListAttributes).sort { it?.name?.toLowerCase() }

            // use sparingly - this is expensive since it requires multiple database queries
            if (activityCode) {
                attrs.from = attrs.from.findAll { it.supports(activityCode) }
            }
        }

        attrs.optionKey = 'id'
        attrs.groupBy = 'locationType'
        if (attrs.groupBy) {
            attrs.optionValue = { it.name }
        } else {
            attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]" }
        }

        out << g.select(attrs)
    }

    def selectTransactionType = { attrs, body ->

        List transactionTypes = (attrs.transactionCode) ?
                TransactionType.findAllByTransactionCode(attrs.transactionCode) : TransactionType.list()

        // FIXME Once we tie the activity code to the transaction type I think this will be more elegant
        Location location = Location.get(session.warehouse.id)
        def disabledTransactionTypes = []
        if (!location.supports(ActivityCode.CONSUME_STOCK)) {
            disabledTransactionTypes.add(Constants.CONSUMPTION_TRANSACTION_TYPE_ID)
        }
        if (!location.supports(ActivityCode.ADJUST_INVENTORY)) {
            disabledTransactionTypes.add(Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID)
            disabledTransactionTypes.add(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
        }
        if (!location.supports(ActivityCode.SEND_STOCK)) {
            disabledTransactionTypes.add(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
        }
        if (!disabledTransactionTypes.empty) {
            disabledTransactionTypes = transactionTypes.findAll { it.id in disabledTransactionTypes }
            transactionTypes.removeAll(disabledTransactionTypes)
        }

        attrs.from = transactionTypes
        attrs.optionKey = 'id'
        attrs.optionValue = { format.metadata(obj: it?.name) }
        out << g.select(attrs)
    }


    def selectTransactionDestination = { attrs, body ->
        def currentLocation = Location.get(session?.warehouse?.id)
        attrs.from = locationService.getTransactionDestinations(currentLocation).sort {
            it?.name?.toLowerCase()
        }
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]" }
        out << g.select(attrs)
    }

    def selectTransactionSource = { attrs, body ->
        def currentLocation = Location.get(session?.warehouse?.id)
        attrs.from = locationService.getTransactionSources(currentLocation).sort {
            it?.name?.toLowerCase()
        }
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]" }
        out << g.select(attrs)
    }

    def selectOrderSupplier = { attrs, body ->
        def currentLocation = Location.get(session?.warehouse?.id)
        attrs.from = locationService.getOrderSuppliers(currentLocation)
        attrs.optionKey = 'id'
        attrs.optionValue = { "${it.organization?.code ? it.organization?.code + ' - ' :''}${it.name}" }
        out << g.select(attrs)
    }

    def selectShipmentOrigin = { attrs, body ->
        attrs.from = locationService.getShipmentOrigins().sort { it?.name?.toLowerCase() }
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]" }
        out << g.select(attrs)
    }

    def selectShipmentDestination = { attrs, body ->
        attrs.from = locationService.getShipmentDestinations().sort { it?.name?.toLowerCase() }
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]" }
        out << g.select(attrs)
    }

    def selectCommodityClass = { attrs, body ->
        attrs.from = CommodityClass.list()
        attrs.optionValue = { format.metadata(obj: it) }
        out << g.select(attrs)
    }

    def selectRequisitionType = { attrs, body ->
        attrs.from = RequisitionType.list()
        attrs.optionValue = { it }
        out << g.select(attrs)
    }

    def selectTimezone = { attrs, body ->
        def timezones = getTimezones()
        if (timezones) {
            attrs.from = timezones
            attrs["class"] = "chzn-select-deselect"
            out << g.select(attrs)
        } else {
            attrs["class"] = "text large readonly"
            attrs["disabled"] = "disabled"
            out << g.textField(attrs)
        }
    }

    def getTimezones() {
        def timezones
        try {
            timezones = TimeZone?.getAvailableIDs()?.sort()
        } catch (Exception e) {
            log.warn("No timezones available: " + e.message)
        }
        return timezones
    }

    def selectLocale = { attrs, body ->
        attrs.from = grailsApplication.config.openboxes.locale.supportedLocales?.sort()
        attrs.optionValue = { LocalizationUtil.getLocale(it).getDisplayName(LocalizationUtil.currentLocale) }
        out << g.select(attrs)
    }

    def selectProductAssociationTypeCode = { attrs, body ->
        attrs.from = ProductAssociationTypeCode.list()
        attrs.optionValue = { format.metadata(obj: it) }
        out << g.select(attrs)
    }


    /**
     * Generic select widget using optgroup.
     */
    def selectWithOptGroup = { attrs ->
        def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
        def locale = RCU.getLocale(request)
        def writer = out
        def from = attrs.remove('from')
        def keys = attrs.remove('keys')
        def optionKey = attrs.remove('optionKey')
        def optionValue = attrs.remove('optionValue')
        def groupBy = attrs.remove('groupBy')
        def value = attrs.remove('value')
        def valueMessagePrefix = attrs.remove('valueMessagePrefix')
        def noSelection = attrs.remove('noSelection')
        def disabled = attrs.remove('disabled')
        Set optGroupSet = new TreeSet()
        def noGroup = []
        attrs.id = attrs.id ? attrs.id : attrs.name

        if (value instanceof Collection && attrs.multiple == null) {
            attrs.multiple = 'multiple'
        }

        if (noSelection != null) {
            noSelection = noSelection.entrySet().iterator().next()
        }

        if (disabled && Boolean.valueOf(disabled)) {
            attrs.disabled = 'disabled'
        }

        // figure out the groups
        from.each {
            it.properties[groupBy] ? optGroupSet.add(it.properties[groupBy]) : noGroup.add(it)
        }

        writer << "<select name=\"${attrs.remove('name')}\" "
        // process remaining attributes
        outputAttributes(attrs)
        writer << '>'
        writer.println()

        if (noSelection) {
            renderNoSelectionOption(noSelection.key, noSelection.value, value)
            writer.println()
        }

        // create options from list
        if (from) {
            //iterate through group set
            for (optGroup in optGroupSet) {

                def optGroupFormatted = "${format.metadata(obj: optGroup)}"
                writer << " <optgroup label=\"${optGroupFormatted ?: optGroup.encodeAsHTML()}\">"
                writer.println()

                from.eachWithIndex { el, i ->
                    if (el.properties[groupBy]?.id == optGroup?.id) {

                        def keyValue = null
                        writer << '<option '

                        if (keys) {
                            keyValue = keys[i]
                            writeValueAndCheckIfSelected(keyValue, value, writer)
                        } else if (optionKey) {
                            if (optionKey instanceof Closure) {
                                keyValue = optionKey(el)
                            } else if (el != null && optionKey == 'id' && grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, el.getClass().name)) {
                                keyValue = el.ident()
                            } else {
                                keyValue = el[optionKey]
                            }

                            writeValueAndCheckIfSelected(keyValue, value, writer)
                        } else {
                            keyValue = el
                            writeValueAndCheckIfSelected(keyValue, value, writer)
                        }

                        writer << '>'

                        if (optionValue) {
                            if (optionValue instanceof Closure) {
                                writer << optionValue(el)?.toString()?.encodeAsHTML()
                            } else {
                                writer << el[optionValue]?.toString()?.encodeAsHTML()
                            }

                        } else if (valueMessagePrefix) {
                            def message = messageSource.getMessage("${valueMessagePrefix}.${keyValue}", null, null, locale)

                            if (message != null) {
                                writer << message.encodeAsHTML()
                            } else if (keyValue) {
                                writer << keyValue.toString().encodeAsHTML()
                            } else {
                                writer << el?.toString()?.encodeAsHTML()
                            }
                        } else {
                            writer << el?.toString()?.encodeAsHTML()
                        }

                        writer << '</option>'
                        writer.println()
                    }
                }

                writer << '</optgroup>'
                writer.println()
            }
            // iterate through items with no group
            noGroup.each {
                writer << '<option '
                def keyValue = null
                // check type of optionKey, retrieve value from iterated object and assign it to keyValue
                if (optionKey instanceof Closure) {
                    keyValue = optionKey(it)
                } else if (it != null && optionKey == 'id' && grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, it.getClass().name)) {
                    keyValue = it.ident()
                } else {
                    keyValue = it[optionKey]
                }
                // add value to the option and mark if it's selected
                writeValueAndCheckIfSelected(keyValue, value, writer)
                writer << '>'
                writer << optionValue(it)?.toString()?.encodeAsHTML()
                writer << '</option>'
                writer.println()
            }
        }
        // close tag
        writer << '</select>'
    }

    void outputAttributes(attrs) {
        attrs.remove('tagName') // Just in case one is left
        attrs.each { k, v ->
            out << "${k?.encodeAsHTML()}=\"${v?.encodeAsHTML()}\""
        }
    }

    def typeConverter = new SimpleTypeConverter()

    private writeValueAndCheckIfSelected(keyValue, value, writer) {
        boolean selected = false
        def keyClass = keyValue?.getClass()
        if (keyClass.isInstance(value)) {
            selected = (keyValue == value)
        } else if (value instanceof Collection) {
            selected = value.contains(keyValue)
        } else if (keyClass && value) {
            try {
                value = typeConverter.convertIfNecessary(value, keyClass)
                selected = (keyValue == value)
            } catch (Exception) {
                // ignore
            }
        }
        writer << "value=\"${keyValue}\" "
        if (selected) {
            writer << 'selected="selected" '
        }
    }

    def renderNoSelectionOption = { noSelectionKey, noSelectionValue, value ->
        // If a label for the '--Please choose--' first item is supplied, write it out
        out << "<option value=\"${noSelectionKey == null ? '' : noSelectionKey}\""
        if (noSelectionKey == value) {
            out << ' selected="selected"'
        }
        out << ">${noSelectionValue.encodeAsHTML()}</option>"
    }
}
