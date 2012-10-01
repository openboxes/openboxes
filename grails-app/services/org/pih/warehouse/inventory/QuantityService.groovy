/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.inventory;

import grails.validation.ValidationException;

import java.util.Map;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.grails.plugins.excelimport.ExcelImportUtils;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.ProductAttribute;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.core.Constants 
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType;
import org.springframework.validation.Errors;

/**
 * Stateful session bean
 * 
 * @author jmiranda
 */
class QuantityService {


	// Indicates that the service is session-scoped (it stores state) and proxied (so it can be used injected/used by 
	// prototype-scoped objects like taglibs).
	//static scope = "session"
	//static proxy = true
	
	//Location warehouse
	//InventoryService inventoryService
	


}
