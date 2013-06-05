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

import org.apache.commons.lang.RandomStringUtils
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment;

class IdentifierService {

    boolean transactional = true

   
	/**
	 * A: alphabetic
	 * L: letter
	 * N: numeric
	 * D: digit
	 * 0-9: digit
	 *
	 * @param format
	 * @return
	 */
	def generateIdentifier(String format) {
		if (!format || format.isEmpty()) {
			println "format must be specified"
			throw new IllegalArgumentException("Format pattern string must be specified")
		}
		
		String identifier = ""
		for (int i = 0; i < format.length(); i++) {
			switch(format[i]) {
				case 'N':
					identifier += RandomStringUtils.random(1, Constants.RANDOM_IDENTIFIER_NUMERIC_CHARACTERS)
					break;
				case 'D':
					identifier += RandomStringUtils.random(1, Constants.RANDOM_IDENTIFIER_NUMERIC_CHARACTERS)
					break;
				case 'L':
					identifier += RandomStringUtils.random(1, Constants.RANDOM_IDENTIFIER_ALPHABETIC_CHARACTERS)
					break;
				case 'A':
					identifier += RandomStringUtils.random(1, Constants.RANDOM_IDENTIFIER_ALPHANUMERIC_CHARACTERS)
					break;
				default:
					identifier += format[i]
					//throw new IllegalArgumentException("Unsupported format symbol: " + format[i])
				
			}
		}
		
		return identifier
	}
	
	/**
	 * Generate a random identifier of given length using alphanumeric characters.
	 *
	 * @param length
	 */
	def generateIdentifier(int length) {
		return RandomStringUtils.random(length, Constants.RANDOM_IDENTIFIER_ALPHANUMERIC_CHARACTERS)
	}

	
	/**
	 * @return
	 */
	def generateOrderIdentifier() {
		return generateIdentifier(Constants.DEFAULT_PRODUCT_NUMBER_FORMAT)
	}

	/**
	 * @return
	 */
	def generateProductIdentifier() { 
		return generateIdentifier(Constants.DEFAULT_PRODUCT_NUMBER_FORMAT)
	}
	
	/**
	 * @return
	 */
	def generateRequisitionIdentifier() {
		return generateIdentifier(Constants.DEFAULT_REQUISITION_NUMBER_FORMAT)
	}

	/**
	 * @return
	 */
	def generateShipmentIdentifier() {
		return generateIdentifier(Constants.DEFAULT_SHIPMENT_NUMBER_FORMAT)
	}

	/**
	 * @return
	 */
	def generateTransactionIdentifier() {
		return generateIdentifier(Constants.DEFAULT_TRANSACTION_NUMBER_FORMAT)
	}


    void assignTransactionIdentifiers() {
        def transactions = Transaction.findAll("from Transaction as t where transactionNumber is null or transactionNumber = ''")
        transactions.each { transaction ->
            try {
                println "Assigning identifier to transaction " + transaction.id + " " + transaction.dateCreated + " " + transaction.lastUpdated
                Transaction.withTransaction {
                    transaction.transactionNumber = generateTransactionIdentifier()
                    if (!transaction.save(flush: true, validate: false)) {
                        println transaction.errors
                    }
                }
                println "Assigned identifier to transaction " + transaction.id + " " + transaction.dateCreated + " " + transaction.lastUpdated
            } catch (ObjectNotFoundException e) {
                println("Unable to assign identifier to transaction with ID " + transaction?.id + ": " + e.message)

            } catch (Exception e) {
                println("Unable to assign identifier to transaction with ID " + transaction?.id + ": " + e.message)
            }
        }
    }


    void assignProductIdentifiers() {
        def products = Product.findAll("from Product as p where productCode is null or productCode = ''")
        products.each { product ->
            try {
                println "Assigning identifier to product " + product.id + " " + product.name
                product.productCode = generateProductIdentifier()
                if (!product.save(flush: true, validate: false)) {
                    println product.errors
                }
            } catch (Exception e) {
                println("Unable to assign identifier to product with ID " + product?.id + ": " + e.message)
            }
        }
    }

    void assignShipmentIdentifiers() {
        def shipments = Shipment.findAll("from Shipment as s where shipmentNumber is null or shipmentNumber = ''")
        shipments.each { shipment ->
            println "Assigning identifier to shipment " + shipment.id + " " + shipment.name
            try {
                shipment.shipmentNumber = generateShipmentIdentifier()
                if (!shipment.save(flush: true, validate: false)) {
                    println shipment.errors
                }
            } catch (Exception e) {
                println("Unable to assign identifier to shipment with ID " + shipment?.id + ": " + e.message)
            }
        }
    }

    void assignRequisitionIdentifiers() {
        def requisitions = Requisition.findAll("from Requisition as r where requestNumber is null or requestNumber = ''")
        requisitions.each { requisition ->
            try {
                println "Assigning identifier to requisition " + requisition.id + " " + requisition.name
                requisition.requestNumber = generateRequisitionIdentifier()
                if (!requisition.save(flush: true, validate: false)) {
                    println requisition.errors
                }
            } catch (Exception e) {
                println("Unable to assign identifier to requisition with ID " + requisition?.id + ": " + e.message)
            }
        }
    }

    void assignOrderIdentifiers() {
        def orders = Order.findAll("from Order as o where orderNumber is null or orderNumber = ''")
        orders.each { order ->
            try {
                println "Assigning identifier to order " + order.id + " " + order.name
                order.orderNumber = generateOrderIdentifier()
                if (!order.save(flush: true, validate: false)) {
                    println order.errors
                }
            } catch (Exception e) {
                println("Unable to assign identifier to order with ID " + order?.id + ": " + e.message)
            }
        }
    }



}
