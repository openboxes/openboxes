package org.pih.warehouse.core

import org.apache.commons.lang.StringUtils;
import org.junit.Test

class IdentifierServiceTests extends GroovyTestCase{
  
	def identifierService
		
	@Test
	void generateIdentifier() {
		def identifier = identifierService.generateIdentifier(4)
		assertNotNull identifier
		assertEquals 4, identifier.length()
	}

	@Test
	void generateIdentifier_shouldReturnFormattedIdentifier() {
		def identifier = identifierService.generateIdentifier("LLL-NNN-AAA-NNN")
		assertNotNull identifier
		assertEquals 15, identifier.length()
		assertTrue StringUtils.isAlpha(identifier[0])
		assertTrue StringUtils.isAlpha(identifier[1])
		assertTrue StringUtils.isAlpha(identifier[2])
		assertEquals "-", identifier[3]
		assertTrue StringUtils.isNumeric(identifier[4])
		assertTrue StringUtils.isNumeric(identifier[5])
		assertTrue StringUtils.isNumeric(identifier[6])
		assertEquals "-", identifier[7]
		assertTrue StringUtils.isAlphanumeric(identifier[8])
		assertTrue StringUtils.isAlphanumeric(identifier[9])
		assertTrue StringUtils.isAlphanumeric(identifier[10])
		assertEquals "-", identifier[11]
		assertTrue StringUtils.isNumeric(identifier[12])
		assertTrue StringUtils.isNumeric(identifier[13])
		assertTrue StringUtils.isNumeric(identifier[14])
	}

	@Test
	void generateIdentifier_shouldFailOnEmptyString() {
		def message = shouldFail(IllegalArgumentException) {
			def identifier = identifierService.generateIdentifier("")
		}
	}

	@Test
	void generateIdentifier_shouldFailOnNull() {
		def message = shouldFail(IllegalArgumentException) {
			def identifier = identifierService.generateIdentifier(null)
		}
	}
}
