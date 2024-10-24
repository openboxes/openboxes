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

import grails.test.GrailsUnitTestCase

class IdentifierServiceUnitTests extends GrailsUnitTestCase {

    IdentifierService identifierService = new IdentifierService()

    protected void setUp() {
        super.setUp()

        identifierService.grailsApplication = new org.codehaus.groovy.grails.commons.DefaultGrailsApplication()
        identifierService.grailsApplication.config.openboxes.identifier.alphanumeric = Constants.RANDOM_IDENTIFIER_ALPHANUMERIC_CHARACTERS
    }

    void testGenerateIdentifierWithLength() {
        def identifier = identifierService.generateIdentifier(2)
        assertEquals(2, identifier.length())
    }

    void testGenerateIdentifierFromOrderNumberFormat() {
        def identifier = identifierService.generateIdentifier(Constants.DEFAULT_ORDER_NUMBER_FORMAT)
        def pattern = /^[0-9]{3}[A-Z]{3}$/
        assertEquals(6, identifier.length())

        def matcher = (identifier =~ pattern)
        assertTrue(matcher.matches())
    }

    void testGenerateIdentifierFromProductNumberFormat() {
        def identifier = identifierService.generateIdentifier(Constants.DEFAULT_PRODUCT_NUMBER_FORMAT)
        def pattern = /^[A-Z]{2}[0-9]{2}$/
        assertEquals(4, identifier.length())

        def matcher = (identifier =~ pattern)
        assertTrue(matcher.matches())
    }

    void testGenerateIdentifierFromTransactionNumberFormat() {
        def identifier = identifierService.generateIdentifier(Constants.DEFAULT_TRANSACTION_NUMBER_FORMAT)
        def pattern = /^[0-9A-Z]{3}-[0-9A-Z]{3}-[0-9A-Z]{3}$/
        assertEquals(11, identifier.length())

        def matcher = (identifier =~ pattern)
        assertTrue(matcher.matches())
    }
}
