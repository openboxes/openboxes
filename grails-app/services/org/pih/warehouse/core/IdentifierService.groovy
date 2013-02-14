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

import org.apache.commons.lang.RandomStringUtils;

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

}
