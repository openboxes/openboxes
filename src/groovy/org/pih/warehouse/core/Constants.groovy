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

import java.text.DateFormat
import java.text.SimpleDateFormat

class Constants {


    static final controllersWithAuthUserNotRequired = ['api', 'test']
    static final actionsWithAuthUserNotRequired = ['test', 'login', 'logout', 'handleLogin', 'signup', 'handleSignup', 'json', 'updateAuthUserLocale']
    static final actionsWithLocationNotRequired = ['test', 'login', 'logout', 'handleLogin', 'signup', 'handleSignup', 'chooseLocation', 'json', 'updateAuthUserLocale']
    static final changeActions = ['edit', 'delete', 'create', 'add', 'process', 'save', 'update', 'importData', 'receive', 'showRecordInventory', 'withdraw', 'cancel', 'change', 'toggle']
    static final changeControllers = ['createProductFromTemplate']
    static final adminControllers = ['createProduct', 'createProductFromTemplate', 'admin']
    static final adminActions = ['product': ['create'], 'person': ['list'], 'user': ['list'], 'location': ['edit'], 'shipper': ['create'], 'locationGroup': ['create'], 'locationType': ['create'], '*': ['delete']]

    static final String DEFAULT_YEAR_FORMAT = "yyyy"
    static final String DEFAULT_DATE_FORMAT = "dd/MMM/yyyy"
    static final String DEFAULT_DATE_TIME_FORMAT = "dd/MMM/yyyy HH:mm:ss"
    static final String DEFAULT_TIME_FORMAT = "HH:mm:ss z"
    static final String DEFAULT_MONTH_YEAR_DATE_FORMAT = "dd/MMM/yyyy"
    static final String SHORT_MONTH_YEAR_DATE_FORMAT = "MM/yyyy"
    static final String EXPIRATION_DATE_FORMAT = "MM/dd/yyyy"
    static final String DELIVERY_DATE_FORMAT = "MM/dd/yyyy HH:mm XXX"


    static final DateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat(DEFAULT_DATE_FORMAT)
    static final DateFormat EXPIRATION_DATE_FORMATTER = new SimpleDateFormat(EXPIRATION_DATE_FORMAT)
    static final DateFormat DELIVERY_DATE_FORMATTER = new SimpleDateFormat(DELIVERY_DATE_FORMAT)

    // Dimension date formats
    static DateFormat weekFormat = new SimpleDateFormat("w")
    static DateFormat dayFormat = new SimpleDateFormat("dd")
    static DateFormat weekdayAbbrFormat = new SimpleDateFormat("EEE")
    static DateFormat weekdayNameFormat = new SimpleDateFormat("EEEEE")
    static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    static DateFormat monthFormat = new SimpleDateFormat("MM")
    static DateFormat monthAbbrFormat = new SimpleDateFormat("MMM")
    static DateFormat monthNameFormat = new SimpleDateFormat("MMMMM")
    static DateFormat yearFormat = new SimpleDateFormat("yyyy")
    static DateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM")

    static final String DEFAULT_WEIGHT_UNITS = "lbs"
    static final String DEFAULT_VOLUME_UNITS = "ft"

    static final ArrayList WEIGHT_UNITS = ["lbs", "kg"]
    static final ArrayList VOLUME_UNITS = ["ft", "m"]

    static final Float POUNDS_PER_KILOGRAM = 0.45359237
    static final Float KILOGRAMS_PER_POUND = 2.20462262

    static final ArrayList COLORS = ['FFFFFF', 'FFDFDF', 'FFBFBF', 'FF9F9F', 'FF7F7F', 'FF5F5F', 'FF3F3F', 'FF1F1F', 'FF0000', 'DF1F00', 'C33B00', 'A75700', '8B7300', '6F8F00', '53AB00', '37C700', '1BE300', '00FF00', '00DF1F', '00C33B', '00A757', '008B73', '006F8F', '0053AB', '0037C7', '001BE3', '0000FF', '0000df', '0000c3', '0000a7', '00008b', '00006f', '000053', '000037', '00001b', '000000']

    static final ArrayList EXPORT_PRODUCT_COLUMNS = ["Id", "ProductCode", "Name", "Category", "Description", "UnitOfMeasure", "Tags", "UnitCost", "Manufacturer", "BrandName", "ManufacturerCode", "ManufacturerName", "Vendor", "VendorCode", "VendorName", "ColdChain", "UPC", "NDC", "Created", "Updated"]

    // these are direct references to transaction types by primary key
    static final String CONSUMPTION_TRANSACTION_TYPE_ID = "2"
    static final String ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID = "3"
    static final String EXPIRATION_TRANSACTION_TYPE_ID = "4"
    static final String DAMAGE_TRANSACTION_TYPE_ID = "5"
    static final String INVENTORY_TRANSACTION_TYPE_ID = "7"
    static final String TRANSFER_IN_TRANSACTION_TYPE_ID = "8"
    static final String TRANSFER_OUT_TRANSACTION_TYPE_ID = "9"
    static final String ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID = "10"
    static final String PRODUCT_INVENTORY_TRANSACTION_TYPE_ID = "11"

    // direct references to locations by primary key
    static final String WAREHOUSE_LOCATION_TYPE_ID = "2"
    static final String SUPPLIER_LOCATION_TYPE_ID = "4"
    static final String RECEIVING_LOCATION_TYPE_ID = "ff8081816482352b01648249e8cc0001"

    // direct references to containers by primary key
    static final String DEFAULT_CONTAINER_TYPE_ID = "1"
    static final String PALLET_CONTAINER_TYPE_ID = "2"
    static final String SUITCASE_CONTAINER_TYPE_ID = "3"
    static final String BOX_CONTAINER_TYPE_ID = "4"
    static final String TRUNK_CONTAINER_TYPE_ID = "5"
    static final String ITEM_CONTAINER_TYPE_ID = "6"
    static final String OTHER_CONTAINER_TYPE_ID = "7"
    static final String CRATE_CONTAINER_TYPE_ID = "8"

    // direct reference to stock movement shipment type
    static final String DEFAULT_SHIPMENT_TYPE_ID = "5"

    // Default character separator for generated names
    static final String DEFAULT_NAME_SEPARATOR = "-"
    static final String DEFAULT_IDENTIFIER_SEPARATOR = "-"

    // Default character used to separate columns and newlines in exports
    static final String DEFAULT_COLUMN_SEPARATOR = ","
    static final String DEFAULT_LINE_SEPARATOR = "\n"

    // Characters that should be included with each of the random number types
    static final String RANDOM_IDENTIFIER_ALPHABETIC_CHARACTERS = "ABCDEFGHJKMNPQRSTUVXYZ"
    static final String RANDOM_IDENTIFIER_ALPHANUMERIC_CHARACTERS = "0123456789ABCDEFGHJKMNPQRSTUVWXYZ"
    static final String RANDOM_IDENTIFIER_NUMERIC_CHARACTERS = "0123456789"

    // Default random number formats
    static final String DEFAULT_ORDER_NUMBER_FORMAT = "NNNLLL"
    static final String DEFAULT_PRODUCT_NUMBER_FORMAT = "LLNN"
    static final String DEFAULT_PRODUCT_SUPPLIER_NUMBER_FORMAT = "NNNN"
    static final String DEFAULT_REQUISITION_NUMBER_FORMAT = "NNNLLL"
    static final String DEFAULT_SHIPMENT_NUMBER_FORMAT = "NNNLLL"
    static final String DEFAULT_RECEIPT_NUMBER_FORMAT = "NNNLLL"
    static final String DEFAULT_TRANSACTION_NUMBER_FORMAT = "AAA-AAA-AAA"
    static final String DEFAULT_ORGANIZATION_NUMBER_FORMAT = "AAA"
    static final String DEFAULT_SEQUENCE_NUMBER_FORMAT = "000000"

    static final String TRACKING_NUMBER_TYPE_ID = "10"

    static final String DEFAULT_RECEIVING_LOCATION_PREFIX = "R"

    static final String DEFAULT_DOCUMENT_TYPE_ID = "9"
}
