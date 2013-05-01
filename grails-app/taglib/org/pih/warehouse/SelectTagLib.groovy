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

import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionStatus;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.Shipper;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.requisition.CommodityClass
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipper
import org.springframework.beans.SimpleTypeConverter
import org.springframework.web.servlet.support.RequestContextUtils as RCU

class SelectTagLib {
	
	def locationService
	def shipmentService


    def selectReasonCode = { attrs, body ->
        attrs.from = ReasonCode.listReasonCodesForRequisitionChange()
        attrs.optionValue = { format.metadata(obj: it) }
        out << g.select(attrs)
    }


    def selectTag = { attrs, body ->
        attrs.from = Tag.list()
        attrs.multiple = true
        attrs.optionKey = "id"
        attrs.optionValue = { it?.tag }
        out << g.select(attrs)
    }

    def selectRequisitionStatus = { attrs, body ->
        attrs.from = RequisitionStatus.list()
        attrs.optionValue = { it?.name() }
        out << g.select(attrs)

    }


    def selectUnitOfMeasure = { attrs, body ->
        def product = Product.get(attrs?.product?.id)
        if (product.packages) {
            attrs.noSelection = ["null":"EA/1"]
            //attrs.value = attrs.value
            attrs.from = product?.packages?.sort()
            attrs.optionKey = "id"
            attrs.optionValue = { it?.uom?.code + "/" + it.quantity + " -- " + it?.uom?.name }
            out << g.select(attrs)
        }
        else {
            attrs.noSelection = ["null":"EA/1"]
            out << g.select(attrs)
            //out << product.unitOfMeasure?:"EA/1"

        }
    }
	
	def selectShipper = { attrs, body ->
		attrs.from = Shipper.list().sort { it?.name?.toLowerCase() } 
		attrs.optionKey = 'id'
		attrs.value = attrs.value
		attrs.optionValue = { it.name }
		out << g.select(attrs)
	}
	
	def selectShipment = { attrs,body ->
		def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = shipmentService.getShipmentsByLocation(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		//attrs.value = attrs.value 
		attrs.optionValue = { it.name + " (" + it.origin.name + " to " + it.destination.name + ")"}
		out << g.select(attrs)
	}
	
	def selectContainer = { attrs, body ->
		def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = shipmentService.getPendingShipments(currentLocation)
		out << render(template: '/taglib/selectContainer', model: [attrs:attrs])
		
	}


    def selectDepot = { attrs, body ->
        def currentLocation = Location.get(session?.warehouse?.id)
        def locations = []

        locations = Location.list().findAll {location -> location.id != session.warehouse.id && location.isWarehouse()}.sort{ it.name }
        attrs.from = locations
        attrs.optionKey = 'id'
        //attrs.optionValue = 'name'

        //attrs.groupBy = 'locationType'
        attrs.value = attrs.value ?: currentLocation?.id
        if (attrs.groupBy) {
            attrs.optionValue = { it.name }
        }
        else {
            attrs.optionValue = { "" + format.metadata(obj: it?.locationType) + " - " + it.name }
        }
        out << (attrs.groupBy ? g.selectWithOptGroup(attrs) : g.select(attrs))

    }


    def selectUser = { attrs, body ->
        attrs.from = User.list().sort()
        attrs.optionKey = 'id'
        attrs.optionValue = { it.name + " (" + it.username + ")"}
        out << g.select(attrs)
    }

    def selectPerson = { attrs, body ->
        def person = Person.get(attrs?.value?.id)
        attrs.selectedPerson = person
        out << render(template: "/taglib/selectPerson", model: [attrs:attrs])
    }


	def selectWardOrPharmacy = { attrs, body ->
        println "select ward or pharmacy"
        def currentLocation = Location.get(session.warehouse.id)
        def locations = []
        if (currentLocation) {

            // If the current location is in a location group, then we want to pull from the locations from that group
            if(currentLocation?.locationGroup != null) {
                locations = Location.list().findAll { location -> location.locationGroup?.id == currentLocation.locationGroup?.id }.findAll {location -> location.isWardOrPharmacy()}.sort { it.name }
            }

            // But if there are no other locations in the location group, then we just want to get all locations that are wards or phaamracies
            if (!locations) {
                locations = Location.list().findAll { location -> location.isWardOrPharmacy() }.sort { it.name }
            }
        }

        if (!locations) {
            out << render(template: "/taglib/createLocation", model: [location: currentLocation])
            return;
        }

        /*
        def currentLocation = Location.get(session?.warehouse?.id)
		def locations = locationService.getAllLocations().sort { it?.name?.toLowerCase() }
        println locations
		if (attrs.locationGroup) {
            println "filter by location group " + attrs.locationGroup
			locations = locations.findAll { it.locationGroup == attrs.locationGroup } 
		}
		if (attrs.locationType) {
            println "filter by location type " + attrs.locationType
			locations = locations.findAll { it.locationType == attrs.locationType }
		}
        println locations
        */
		attrs.from = locations
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		
		attrs.groupBy = 'locationType'
		attrs.value = attrs.value ?: currentLocation?.id
		if (attrs.groupBy) {
			attrs.optionValue = { it.name }
		}
		else {
			attrs.optionValue = { "" + format.metadata(obj: it?.locationType) + " - " + it.name }
		}
		out << (attrs.groupBy ? g.selectWithOptGroup(attrs) : g.select(attrs))
	}

	
	def selectLocation = { attrs,body ->
		def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = locationService.getAllLocations().sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.groupBy = 'locationType'
		attrs.value = attrs.value ?: currentLocation?.id
		if (attrs.groupBy) { 
			attrs.optionValue = { it.name }
		}
		else { 
			attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		}
		out << (attrs.groupBy ? g.selectWithOptGroup(attrs) : g.select(attrs))
	}

		
	def selectTransactionDestination = { attrs,body ->		
		def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = locationService.getTransactionDestinations(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'		
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectTransactionSource = { attrs,body ->
		def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = locationService.getTransactionSources(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectOrderSupplier = { attrs,body ->
		def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = locationService.getOrderSuppliers(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectRequestOrigin = { attrs,body ->
		def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = locationService.getRequestOrigins(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		attrs.placeholder = attrs.placeholder
		
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectRequestDestination = { attrs,body ->
		def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = locationService.getRequestDestinations(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}
	
	def selectCustomer = { attrs,body ->
		def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = locationService.getCustomers(currentLocation).sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectShipmentOrigin = { attrs,body ->
		// def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = locationService.getShipmentOrigins().sort { it?.name?.toLowerCase() };
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

	def selectShipmentDestination = { attrs,body ->
		// def currentLocation = Location.get(session?.warehouse?.id)
		attrs.from = locationService.getShipmentDestinations().sort { it?.name?.toLowerCase() } ;
		attrs.optionKey = 'id'
		//attrs.optionValue = 'name'
		attrs.optionValue = { it.name + " [" + format.metadata(obj: it?.locationType) + "]"}
		out << g.select(attrs)
	}

    def selectCommodityClass = { attrs, body ->
        attrs.from = CommodityClass.list()
        //attrs.optionKey = 'id'
        //attrs.optionValue = 'name'
        attrs.optionValue = { format.metadata(obj: it)  }
        out << g.select(attrs)
    }

    def selectRequisitionType = { attrs, body ->
        attrs.from = RequisitionType.list()
        //attrs.optionKey = 'id'
        //attrs.optionValue = 'name'
        attrs.optionValue = { format.metadata(obj: it)  }
        out << g.select(attrs)
    }

	/**
	 * Generic select widget using optgroup.
	 */
    def selectWithOptGroup = {attrs ->
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
        Set optGroupSet = new TreeSet();
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
            optGroupSet.add(it.properties[groupBy])
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
            for(optGroup in optGroupSet) {
				
				def optGroupFormatted = "${format.metadata(obj: optGroup)}"				
                writer << " <optgroup label=\"${optGroupFormatted ?: optGroup.encodeAsHTML()}\">"
                writer.println()

                from.eachWithIndex {el, i ->
                    if(el.properties[groupBy].equals(optGroup)) {

                        def keyValue = null
                        writer << '<option '

                        if (keys) {
                            keyValue = keys[i]
                            writeValueAndCheckIfSelected(keyValue, value, writer)
                        }

                        else if (optionKey) {
                            if (optionKey instanceof Closure) {
                                keyValue = optionKey(el)
                            }

                            else if (el != null && optionKey == 'id' && grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, el.getClass().name)) {
                                keyValue = el.ident()
                            }

                            else {
                                keyValue = el[optionKey]
                            }

                            writeValueAndCheckIfSelected(keyValue, value, writer)
                        }

                        else {
                            keyValue = el
                            writeValueAndCheckIfSelected(keyValue, value, writer)
                        }

                        writer << '>'

                        if (optionValue) {
                            if (optionValue instanceof Closure) {
                                writer << optionValue(el).toString().encodeAsHTML()
                            }

                            else {
                                writer << el[optionValue].toString().encodeAsHTML()
                            }

                        }

                        else if (valueMessagePrefix) {
                            def message = messageSource.getMessage("${valueMessagePrefix}.${keyValue}", null, null, locale)

                            if (message != null) {
                                writer << message.encodeAsHTML()
                            }

                            else if (keyValue) {
                                writer << keyValue.encodeAsHTML()
                            }

                            else {
                                def s = el.toString()
                                if (s) writer << s.encodeAsHTML()
                            }
                        }

                        else {
                            def s = el.toString()
                            if (s) writer << s.encodeAsHTML()
                        }

                        writer << '</option>'
                        writer.println()
                    }
                }

                writer << '</optgroup>'
                writer.println()
            }
        }
        // close tag
        writer << '</select>'
    }

    void outputAttributes(attrs) {
        attrs.remove('tagName') // Just in case one is left
        attrs.each {k, v ->
            out << k << "=\"" << v.encodeAsHTML() << "\" "
        }
    }

    def typeConverter = new SimpleTypeConverter()
    private writeValueAndCheckIfSelected(keyValue, value, writer) {
        boolean selected = false
        def keyClass = keyValue?.getClass()
        if (keyClass.isInstance(value)) {
            selected = (keyValue == value)
        }
        else if (value instanceof Collection) {
            selected = value.contains(keyValue)
        }
        else if (keyClass && value) {
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

    def renderNoSelectionOption = {noSelectionKey, noSelectionValue, value ->
        // If a label for the '--Please choose--' first item is supplied, write it out
        out << '<option value="' << (noSelectionKey == null ? "" : noSelectionKey) << '"'
        if (noSelectionKey.equals(value)) {
            out << ' selected="selected" '
        }
        out << '>' << noSelectionValue.encodeAsHTML() << '</option>'
    }

    private String optionValueToString(def el, def optionValue) {
        if (optionValue instanceof Closure) {
            return optionValue(el).toString().encodeAsHTML()
        }

        el[optionValue].toString().encodeAsHTML()
    }
		
}