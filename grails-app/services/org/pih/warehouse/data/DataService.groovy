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

import org.apache.commons.lang.StringEscapeUtils
import org.grails.plugins.csv.CSVWriter
import org.pih.warehouse.core.Constants

import java.text.SimpleDateFormat

class DataService {
	
	static transactional = true
	
	def importData() { 
		def sql = Sql.newInstance("jdbc:mysql://localhost:3306/mydb", "user", "pswd", "com.mysql.jdbc.Driver")
		def people = sql.dataSet("PERSON")
		new File("users.csv").splitEachLine(",") {fields ->
			people.add(
				first_name: fields[0],
				last_name: fields[1],
				email: fields[2]
			)
		}
	}

	
	def exportData() { 
		def sql = Sql.newInstance("jdbc:mysql://localhost:3306/mydb", "user", "pswd", "com.mysql.jdbc.Driver")
		def people = sql.dataSet("PERSON")
		
		people.each { 
			log.info it;
		}
	}


    String exportProducts(products) {
        def formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        def sw = new StringWriter()

        def csvWriter = new CSVWriter(sw, {
            "ID" { it.id }
            "SKU" { it.productCode }
            "Name" { it.name }
            org.pih.warehouse.product.Category { it.category }
            "Description" { it.description }
            "Unit of Measure" { it.unitOfMeasure }
            "Manufacturer" { it.manufacturer }
            "Brand" { it.brandName }
            "Manufacturer Code" { it.manufacturerCode }
            "Manufacturer Name" { it.manufacturerName }
            "Vendor" { it.vendor }
            "Vendor Code" { it.vendorCode }
            "Vendor Name" { it.vendorName }
            "Cold Chain" { it.coldChain }
            "UPC" { it.upc }
            "NDC" { it.ndc }
            "Date Created" { it.dateCreated }
            "Date Updated" { it.lastUpdated }
        })

        products.each { product ->
            def row =  [
                    id: product?.id,
                    productCode: product.productCode?:'',
                    name: product.name,
                    category: product?.category?.name,
                    description: product?.description?:'',
                    unitOfMeasure: product.unitOfMeasure?:'',
                    manufacturer: product.manufacturer?:'',
                    brandName: product.brandName?:'',
                    manufacturerCode: product.manufacturerCode?:'',
                    manufacturerName: product.manufacturerName?:'',
                    vendor: product.vendor?:'',
                    vendorCode: product.vendorCode?:'',
                    vendorName: product.vendorName?:'',
                    coldChain: product.coldChain?:Boolean.FALSE,
                    upc: product.upc?:'',
                    ndc: product.ndc?:'',
                    dateCreated: product.dateCreated?"${formatDate.format(product.dateCreated)}":"",
                    lastUpdated: product.lastUpdated?"${formatDate.format(product.lastUpdated)}":"",
            ]
            // We just want to make sure that these match because we use the same format to
            // FIXME It would be better if we could drive the export off of this array of columns,
            // but I'm not sure how.  It's possible that the constant could be a map of column
            // names to closures (that might work)
            assert row.keySet().size() == Constants.EXPORT_PRODUCT_COLUMNS.size()
            csvWriter << row
        }
        return sw.toString()
    }


    String exportRequisitions(requisitions) {
        def formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        def sw = new StringWriter()

        def csvWriter = new CSVWriter(sw, {
            "ID" { it.id }
            "Requisition Number" { it.requisitionNumber }
            "Status" { it.status }
            "Type" { it.type }
            "Class" { it.commodityClass }
            "Name" { it.name }
            "Requesting ward" { it.origin }
            "Processing depot" { it.destination }

            "Requested by" { it?.requestedBy?.name }
            "Date Requested" { it.dateRequested }

            "Verified" { it?.verifiedBy?.name }
            "Date Verified" { it.dateVerified }

            "Picked" { it?.pickedBy?.name }
            "Date Picked" { it.datePicked }

            "Checked" { it?.checkedBy?.name }
            "Date Checked" { it.dateChecked }

            "Issued" { it?.issuedBy?.name }
            "Date Issued" { it.dateIssued }

            "Created" { it?.createdBy?.name }
            "Date Created" { it.dateCreated }

            "Updated" { it?.updatedBy?.name }
            "Date Updated" { it.lastUpdated }
        })

        requisitions.each { requisition ->
            def row =  [
                    id: requisition?.id,
                    requisitionNumber: requisition.requestNumber,
                    type: requisition?.type,
                    commodityClass: requisition?.commodityClass,
                    status: requisition.status,
                    name: requisition.name,
                    origin: requisition.origin,
                    destination: requisition.destination,

                    requestedBy: requisition.requestedBy,
                    dateRequested: requisition.dateRequested,

                    reviewedBy: requisition.reviewedBy,
                    dateReviewed: requisition.dateReviewed,

                    verifiedBy: requisition.verifiedBy,
                    dateVerified: requisition.dateVerified,

                    checkedBy: requisition.checkedBy,
                    dateChecked: requisition.dateChecked,

                    deliveredBy: requisition.deliveredBy,
                    dateDelivered: requisition.dateDelivered,

                    pickedBy: requisition?.picklist?.picker,
                    datePicked: requisition?.picklist?.datePicked,

                    issuedBy: requisition.issuedBy,
                    dateIssued: requisition.dateIssued,

                    receivedBy: requisition.receivedBy,
                    dateReceived: requisition.dateReceived,

                    createdBy: requisition.createdBy,
                    dateCreated: requisition.dateCreated?"${formatDate.format(requisition.dateCreated)}":"",

                    updatedBy: requisition.updatedBy,
                    lastUpdated: requisition.lastUpdated?"${formatDate.format(requisition.lastUpdated)}":"",
            ]
            csvWriter << row
        }
        return sw.toString()
    }


    String generateCsv(csvrows) {
        def sw = new StringWriter()
        if (csvrows) {
            def columnHeaders = csvrows[0].keySet().collect { value -> StringEscapeUtils.escapeCsv(value) }
            sw.append(columnHeaders.join(",")).append("\n")
            csvrows.each { row ->
                def values = row.values().collect { value ->
                    if (value?.toString()?.isNumber()) {
                        value
                    }
                    else {
                        //'"' + value.toString().replace('"','""') + '"'
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values.join(","))
                sw.append("\n")
            }
        }
        return sw.toString()
    }
	
}