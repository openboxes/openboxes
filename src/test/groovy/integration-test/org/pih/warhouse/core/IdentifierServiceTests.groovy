package org.pih.warehouse.core

import grails.core.DefaultGrailsApplication
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import org.apache.commons.lang.StringUtils
import org.junit.Test
import spock.lang.Specification
import static org.junit.Assert.*;

//@Ignore
@SuppressWarnings('MethodName')
class IdentifierServiceTests  extends Specification implements ServiceUnitTest<IdentifierService>, DataTest {
  
	def setup() {
		Holders.grailsApplication = new DefaultGrailsApplication()
		Holders.grailsApplication.config.openboxes.identifier.alphanumeric = Constants.RANDOM_IDENTIFIER_NUMERIC_CHARACTERS
	}
		
	@Test
	void test_generateIdentifier() {
		when:
		def identifier = service.generateIdentifier(4)
		then:
		assert identifier != null
		assert  4 == identifier.length()
	}

	@Test
	void generateIdentifier_shouldReturnFormattedIdentifier() {
		when:
		def identifier = service.generateIdentifier("LLL-NNN-AAA-NNN")
		then:
		assert identifier != null
		assert 15 == identifier.length()
		assert StringUtils.isAlpha(identifier[0])
		assert StringUtils.isAlpha(identifier[1])
		assert StringUtils.isAlpha(identifier[2])
		assert "-" == identifier[3]
		assert StringUtils.isNumeric(identifier[4])
		assert StringUtils.isNumeric(identifier[5])
		assert StringUtils.isNumeric(identifier[6])
		assert "-" == identifier[7]
		assert StringUtils.isAlphanumeric(identifier[8])
		assert StringUtils.isAlphanumeric(identifier[9])
		assert StringUtils.isAlphanumeric(identifier[10])
		assert "-" ==  identifier[11]
		assert StringUtils.isNumeric(identifier[12])
		assert StringUtils.isNumeric(identifier[13])
		assert StringUtils.isNumeric(identifier[14])
	}

	@Test
	void generateIdentifier_shouldFailOnEmptyString() {
		when:
		def identifier = service.generateIdentifier("")
		then:
		thrown(IllegalArgumentException)
	}

	@Test
	void generateIdentifier_shouldFailOnNull() {
		when:
		def identifier = service.generateIdentifier(null)
		then:
		thrown(IllegalArgumentException)
	}
}
