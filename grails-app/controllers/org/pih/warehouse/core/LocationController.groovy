/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.validation.ValidationException
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.springframework.web.multipart.MultipartFile

class LocationController {

    def inventoryService
    def locationService
    def dataService

    /**
     * Controllers for managing other locations (besides warehouses)
     */

    def index = {
        redirect(action: "list")
    }

    def list = {

        def locationsTotal = 0
        def locationType = LocationType.get(params["locationType.id"])
        def locationGroup = LocationGroup.get(params["locationGroup.id"])

        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.offset = params.offset ? params.int("offset") : 0
        def locations = locationService.getLocations(locationType, locationGroup, params.q, params.max, params.offset as int)

        [locationInstanceList: locations, locationInstanceTotal: locations.totalCount]
    }

    def show = {
        def locationInstance = inventoryService.getLocation(params.id)
        if (!locationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
            redirect(action: "list")
        } else {
            return [locationInstance: locationInstance]
        }
    }

    def edit = {
        def locationInstance = inventoryService.getLocation(params.id)
        if (!locationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
            redirect(action: "list")
        } else {
            return [locationInstance: locationInstance]
        }
    }

    def update = {
        def locationInstance = inventoryService.getLocation(params.id)

        if (locationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (locationInstance.version > version) {

                    locationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
                            warehouse.message(code: 'location.label', default: 'Location')] as Object[], "Another user has updated this Location while you were editing")
                    render(view: "edit", model: [locationInstance: locationInstance])
                    return
                }
            }

            locationInstance.properties = params

            if (locationInstance.validate() && !locationInstance.hasErrors()) {
                try {
                    if (locationInstance?.address?.validate() && !locationInstance?.address?.hasErrors()) {
                        locationInstance.address.save()
                    }

                    boolean useDefault = params.useDefault as boolean
                    if (useDefault && locationInstance?.supportedActivities) {
                        locationInstance.supportedActivities.clear()
                    }

                    inventoryService.saveLocation(locationInstance)
                    if (locationInstance?.id == session?.warehouse?.id) {
                        session.warehouse = locationInstance
                    }
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'location.label', default: 'Location'), locationInstance.id])}"

                } catch (ValidationException e) {
                    flash.message = e.message
                    log.error("error: " + e.message, e)
                    render(view: "edit", model: [locationInstance: locationInstance])
                    return

                } catch (Exception e) {
                    flash.message = e.message
                    log.error("error: " + e.message, e)
                    render(view: "edit", model: [locationInstance: locationInstance])
                    return
                }
            } else {
                render(view: "edit", model: [locationInstance: locationInstance])
                return
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
            redirect(action: "list")
        }

        if (locationInstance.parentLocation) {
            redirect(action: "edit", id: locationInstance.parentLocation.id)
        } else {
            redirect(action: "edit", id: locationInstance.id)
        }


    }

    def delete = {
        def locationInstance = Location.get(params.id)
        if (locationInstance) {
            try {
                locationInstance.delete(flush: true)

                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"

                if (locationInstance.parentLocation) {
                    redirect(action: "edit", id: locationInstance.parentLocation.id)
                } else {
                    redirect(action: "list")
                }
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
                redirect(action: "edit", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
            redirect(action: "edit", id: params.id)
        }
    }

    def resetSupportedActivities = {
        def location = Location.get(params.id)
        location.supportedActivities.clear()
        location.save()
        redirect(action: "edit", id: params.id)
    }

    def showContents = {
        def binLocation = Location.get(params.id)
        if (!binLocation) {
            render "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"

        } else {
            List contents = inventoryService.getQuantityByBinLocation(binLocation.parentLocation, binLocation)
            return [binLocation: binLocation, contents: contents]
        }


    }


    /**
     * Render location logo
     */
    def viewLogo = {
        def warehouseInstance = Location.get(params.id)
        if (warehouseInstance) {
            if (warehouseInstance.logo) {
                response.outputStream << warehouseInstance.logo
            }
        }
    }


    def renderLogo = {
        def location = Location.get(params.id)
        if (location?.logo) {
            response.setContentLength(location.logo.length)
            response.outputStream.write(location.logo)
        } else {
            // Sends 404 error if no photo
            response.sendError(404)
        }
    }


    def uploadLogo = {
        def locationInstance = Location.get(params.id)

        if (request.method == "POST") {
            if (locationInstance) {
                def logo = request.getFile("logo")

                // List of OK mime-types
                def okcontents = [
                        'image/png',
                        'image/jpeg',
                        'image/gif'
                ]

                if (!okcontents.contains(logo.getContentType())) {
                    log.info "Photo is not correct type"
                    flash.message = "Photo must be one of: ${okcontents}"
                    render(view: "uploadLogo", model: [locationInstance: locationInstance])
                    return
                }

                if (!logo?.empty && logo.size < 1024 * 1000) { // not empty AND less than 1MB
                    locationInstance.logo = logo.bytes
                    if (!locationInstance.hasErrors()) {
                        inventoryService.saveLocation(locationInstance)
                        session.warehouse = locationInstance
                        flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), locationInstance.id])}"
                    } else {
                        // there were errors, the logo was not saved
                        flash.message = "${warehouse.message(code: 'default.not.updated.message', args: [warehouse.message(code: 'user.label'), locationInstance.id])}"
                        render(view: "uploadPhoto", model: [locationInstance: locationInstance])
                        return
                    }
                } else {
                    flash.message = "${warehouse.message(code: 'user.photoTooLarge.message', args: [warehouse.message(code: 'location.label'), locationInstance.id])}"
                }

                redirect(action: "show", id: locationInstance.id)
            } else {
                "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
            }
        }
        [locationInstance: locationInstance]
    }

    def deleteLogo = {
        def location = Location.get(params.id)
        if (location) {
            location.logo = []
            location.save(flush: true)
            flash.message = "Logo has been deleted"
        }
        redirect(action: "uploadLogo", id: params.id)
    }

    def deleteTransaction = {
        def transaction = Transaction.get(params.id)
        transaction.delete()
        flash.message = "Transaction deleted"
        redirect(action: "show", id: params.location.id)
    }
    def deleteShipment = {
        def shipment = Shipment.get(params.id)
        shipment.delete()
        flash.message = "Shipment deleted"
        redirect(action: "show", id: params.location.id)
    }
    def deleteOrder = {
        def order = Order.get(params.id)
        order.delete()
        flash.message = "Order deleted"
        redirect(action: "show", id: params.location.id)
    }
    def deleteRequest = {
        def requestInstance = Requisition.get(params.id)
        requestInstance.delete()
        flash.message = "Request deleted"
        redirect(action: "show", id: params.location.id)
    }
    def deleteEvent = {
        def event = Event.get(params.id)
        event.delete()
        flash.message = "Event deleted"
        redirect(action: "show", id: params.location.id)
    }
    def deleteUser = {
        def user = User.get(params.id)
        user.delete()
        flash.message = "User deleted"
        redirect(action: "show", id: params.location.id)
    }

    def showBinLocations = {

        def locationInstance = Location.get(params.id)
        if (!locationInstance) {
            render "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
        } else {
            def binLocations = Location.findAllByParentLocation(locationInstance)
            [locationInstance: locationInstance, binLocations: binLocations]
        }
    }


    def importBinLocations = {
        try {
            MultipartFile multipartFile = request.getFile('fileContents')
            if (multipartFile.empty) {
                flash.message = "File cannot be empty."
                return
            }

            if (locationService.importBinLocations(params.id, multipartFile.inputStream)) {
                flash.message = "Successfully imported all bin locations."

            } else {
                flash.message = "Failed to import bin locations due to an unknown error."
            }
        } catch (Exception e) {
            log.error("Failed to import bin locations due to the following error: " + e.message, e)
            flash.message = "Failed to import bin locations due to the following error: " + e.message
        }
        redirect(action: "edit", id: params.id)
    }


    def exportBinLocations = {

        Location location = Location.get(params.id)

        if (!location) {
            throw new IllegalArgumentException("Must specify location")
        }

        if (location.locations) {
            def date = new Date()
            response.setHeader("Content-disposition",
                    "attachment; filename=\"BinLocations-${location?.name}-${date.format("yyyyMMdd-hhmmss")}.csv\"")
            response.contentType = "text/csv"
            def csvrows = location.locations.collect { binLocation ->
                return [
                        "id"            : binLocation.id ?: "",
                        "locationType"  : binLocation?.locationType?.locationTypeCode ?: "",
                        "locationNumber": binLocation?.locationNumber ?: "",
                        "locationName"  : binLocation?.name ?: ""
                ]
            }

            render dataService.generateCsv(csvrows)
        } else {
            flash.message = "No bin locations for location ${location.name}"
            redirect(action: "edit", id: params.id)
        }
    }


}
