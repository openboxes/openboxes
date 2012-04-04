package org.pih.warehouse.report;

import static org.junit.Assert.*;

import grails.test.*
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;

class ReportServiceTests extends GroovyTestCase {

	def reportService;

	protected void setUp() {
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	/**
	 * 
	 */
	void testGenerateTransactionReport() {
		println("Running 'Transaction Report' ... ")
		def command = new InventoryReportCommand();		
		command.startDate = new Date() - 180;
		command.endDate = new Date()
		command.category = Category.get(123)
		command.categoryOnly = true
		command.location = Location.get(3)
		assertEquals(command.category.name, "INFECTION CONTROL SUPPLIES")
		
		
		reportService.generateTransactionReport(command);
		
		println command
		
		
	}
}

