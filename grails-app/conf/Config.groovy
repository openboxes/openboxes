/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/

import grails.util.GrailsUtil
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.IdentifierGeneratorTypeCode
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UpdateUnitPriceMethodCode

// Locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts
grails.config.locations = [
        "classpath:${appName}-config.properties",
        "classpath:${appName}-config.groovy",
        "file:${userHome}/.grails/${appName}-config.properties",
        "file:${userHome}/.grails/${appName}-config.groovy"
]

// Allow admin to override the config location using command line argument
configLocation = System.properties["${appName}.config.location"]
if (configLocation) {
    grails.config.locations << "file:" + configLocation
}

// Allow admin to override the config location using environment variable
configLocation = System.env["${appName.toString().toUpperCase()}_CONFIG_LOCATION"]
if (configLocation) {
    grails.config.locations << "file:" + configLocation
}


println "Using configuration locations ${grails.config.locations} [${GrailsUtil.environment}]"

//grails.plugins.reloadConfig.files = []
//grails.plugins.reloadConfig.includeConfigLocations = true
//grails.plugins.reloadConfig.interval = 5000
//grails.plugins.reloadConfig.enabled = true
//grails.plugins.reloadConfig.notifyPlugins = []
//grails.plugins.reloadConfig.automerge = true
//grails.plugins.reloadConfig.notifyWithConfig = true

grails.exceptionresolver.params.exclude = ['password', 'passwordConfirm']

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

// Default mail settings
grails {
    mail {
        // By default we enable email.  You can enable/disable email using environment settings below or in your
        // ${user.home}/openboxes-config.properties file
        enabled = false
        from = "info@openboxes.com"
        prefix = "[OpenBoxes]"
        host = "localhost"
        port = "25"

        // Authentication disabled by default
        username = null
        password = null

        // Disable debug mode by default
        debug = false
    }
}

/* Indicates which activities are required for a location to allow logins */
openboxes.chooseLocation.requiredActivities = ["MANAGE_INVENTORY"]

/* Grails resources plugin */
grails.resources.adhoc.includes = []
grails.resources.adhoc.excludes = ["*"]

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html         : ['text/html', 'application/xhtml+xml'],
                     xml          : ['text/xml', 'application/xml'],
                     text         : 'text/plain',
                     js           : 'text/javascript',
                     rss          : 'application/rss+xml',
                     atom         : 'application/atom+xml',
                     css          : 'text/css',
                     csv          : 'text/csv',
                     all          : '*/*',
                     json         : ['application/json', 'text/json'],
                     form         : 'application/x-www-form-urlencoded',
                     multipartForm: 'multipart/form-data']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
//grails.views.gsp.keepgenerateddir="/home/jmiranda/git/openboxes/target/generated"
grails.converters.encoding = "UTF-8"
grails.views.enable.jsessionid = true
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// not sure what this does
grails.views.javascript.library = "jquery"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'
// Set to true if BootStrap.groovy is failing to add all sample data
grails.gorm.failOnError = false
// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable fo AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

grails.validateable.packages = [
        'org.pih.warehouse.api',
        'org.pih.warehouse.fulfillment',
        'org.pih.warehouse.inventory',
        'org.pih.warehouse.order',
        'org.pih.warehouse.request',
        'org.pih.warehouse.shipment',
]

// Default URL
grails.serverURL = "http://localhost:8080/${appName}"

// UI performance
uiperformance.enabled = false

/* Default settings for emails sent through the SMTP appender  */
//mail.error.server = 'localhost'
//mail.error.port = 25
//mail.error.from = 'justin@openboxes.com'
//mail.error.to = 'errors@openboxes.com'
//mail.error.subject = '[OpenBoxes '+GrailsUtil.environment+']'
//mail.error.debug = true
mail.error.enabled = false
mail.error.debug = false
mail.error.to = 'errors@openboxes.com'
mail.error.server = grails.mail.host
mail.error.port = grails.mail.port
mail.error.from = grails.mail.from
mail.error.username = grails.mail.username
mail.error.password = grails.mail.password
mail.error.prefix = grails.mail.prefix


// set per-environment serverURL stem for creating absolute links
environments {
    development {
    }
    test {
        quartz {
            autoStartup = false
        }
    }
    loadtest {
    }
    production {
    }
}


// log4j configuration
log4j = {


    root {
        error 'stdout'
        additivity = false
    }

    fatal 'com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter',
            'org.grails.plugin.resource.ResourceMeta',
            'org.codehaus.groovy.grails.web.converters.JSONParsingParameterCreationListener'

    // We get some annoying stack trace when cleaning this class up after functional tests
    error 'org.hibernate.engine.StatefulPersistenceContext.ProxyWarnLog',
            'org.hibernate.impl.SessionFactoryObjectFactory',
            'com.gargoylesoftware.htmlunit.DefaultCssErrorHandler',
            'com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl'
    //'org.jumpmind.symmetric.config.PropertiesFactoryBean'

    warn 'org.mortbay.log',
            'org.codehaus.groovy.grails.web.servlet',        // controllers
            'org.codehaus.groovy.grails.web.sitemesh',        // layouts
            'org.codehaus.groovy.grails.web.mapping.filter',    // URL mapping
            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
            'org.codehaus.groovy.grails.orm.hibernate',
            'org.codehaus.groovy.grails.commons',            // core / classloading
            'org.codehaus.groovy.grails.plugins',            // plugins
            'org.codehaus.groovy.grails.orm.hibernate',        // hibernate integration
            'org.docx4j',
            'org.apache.http.headers',
            'org.apache.ddlutils',
            //'org.apache.http.wire',
            'net.sf.ehcache.hibernate',
            //'org.hibernate.SQL',
            //'org.hibernate.type',
            //'org.jumpmind.symmetric.service.impl.PurgeService',
            'org.hibernate.cache',
            'org.apache.ddlutils'

    info 'org.liquibase',
            'org.codehaus.groovy.grails.web.pages',        // GSP			'com.mchange',
            'org.springframework',
            'org.hibernate',
            'com.mchange',
            'org.pih.warehouse',
            'grails.app',
            'grails.app.controller',
            'grails.app.bootstrap',
            'grails.app.service',
            'grails.app.task',
            'grails.plugin.springcache',
            'BootStrap',
            'liquibase',
            'grails.quartz2',
            'org.quartz',
            'com.gargoylesoftware.htmlunit'

    debug 'org.apache.cxf',
            'grails.plugin.rendering',
            'org.apache.commons.mail',
            'grails.plugins.raven',
            'net.kencochrane.raven',
            //'com.unboundid'
            //'org.hibernate.transaction',
            //'org.jumpmind',
            //'org.hibernate.jdbc',
            //'org.hibernate.SQL',
            //'com.gargoylesoftware.htmlunit',
            'org.apache.http.wire'        // shows traffic between htmlunit and server

    //trace    'org.hibernate.type.descriptor.sql.BasicBinder',
    //         'org.hibernate.type'


}

// Added by the JQuery Validation plugin:
jqueryValidation.packed = true
jqueryValidation.cdn = false  // false or "microsoft"
jqueryValidation.additionalMethods = false


// Added by the JQuery Validation UI plugin:
jqueryValidationUi {
    errorClass = 'error'
    validClass = 'valid'
    onsubmit = true
    renderErrorsOnTop = true

    qTip {
        packed = true
        classes = 'ui-tooltip-red ui-tooltip-shadow ui-tooltip-rounded'
    }

    /*
      Grails constraints to JQuery Validation rules mapping for client side validation.
      Constraint not found in the ConstraintsMap will trigger remote AJAX validation.
    */
    StringConstraintsMap = [
            blank     : 'required', // inverse: blank=false, required=true
            creditCard: 'creditcard',
            email     : 'email',
            inList    : 'inList',
            minSize   : 'minlength',
            maxSize   : 'maxlength',
            size      : 'rangelength',
            matches   : 'matches',
            notEqual  : 'notEqual',
            url       : 'url',
            nullable  : 'required',
            unique    : 'unique',
            validator : 'validator'
    ]

    // Long, Integer, Short, Float, Double, BigInteger, BigDecimal
    NumberConstraintsMap = [
            min      : 'min',
            max      : 'max',
            range    : 'range',
            notEqual : 'notEqual',
            nullable : 'required',
            inList   : 'inList',
            unique   : 'unique',
            validator: 'validator'
    ]

    CollectionConstraintsMap = [
            minSize  : 'minlength',
            maxSize  : 'maxlength',
            size     : 'rangelength',
            nullable : 'required',
            validator: 'validator'
    ]

    DateConstraintsMap = [
            min      : 'minDate',
            max      : 'maxDate',
            range    : 'rangeDate',
            notEqual : 'notEqual',
            nullable : 'required',
            inList   : 'inList',
            unique   : 'unique',
            validator: 'validator'
    ]

    ObjectConstraintsMap = [
            nullable : 'required',
            validator: 'validator'
    ]

    CustomConstraintsMap = [
            phone  : 'true', // International phone number validation
            phoneUS: 'true'
    ]
}


// Allow users to customize logo image url as well as label
openboxes.logo.url = "https://openboxes.com/img/logo_30.png"
openboxes.report.logo.url = "https://openboxes.com/img/logo_100.png"

// Allow system to anonymize user data to prevent it from being accessed by unauthorized users
openboxes.anonymize.enabled = false

// Grails Sentry/Raven plugin
// NOTE: You'll need to enable the plugin and set a DSN using an external config properties file
// (namely, openboxes-config.properties or openboxes-config.groovy)
grails.plugins.raven.active = false
grails.plugins.raven.dsn = "https://{PUBLIC_KEY}:{SECRET_KEY}@app.getsentry.com/{PROJECT_ID}"

// Additional columns for cycle count report
openboxes.cycleCount.additionalColumns = [:]

// Acceptable values - MUST be added to openboxes-config.groovy!
//openboxes.cycleCount.additionalColumns = [
//    "Column1": { obj -> return "string literal" },
//    "Column2": { obj -> return "${obj.product.productCode}" },
//    "Column3": { obj -> return "" },
//    "Column4": { obj -> return null },
//    "Column5": null,
//]

// Dashboard configuration to allow specific ordering of widgets (overrides enabled/disabled config)
openboxes.dashboard.column1.widgets = ["requisitionItemSummary", "requisitionSummary", "receiptSummary", "shipmentSummary", "indicatorSummary"]
openboxes.dashboard.column2.widgets = ["binLocationSummary", "expiringSummary", "productSummary", "genericProductSummary",]
openboxes.dashboard.column3.widgets = ["newsSummary", "activitySummary", "valueSummary", "tagSummary", "catalogsSummary"]

// Column 1
openboxes.dashboard.requisitionItemSummary.enabled = true
openboxes.dashboard.requisitionSummary.enabled = false
openboxes.dashboard.receiptSummary.enabled = true
openboxes.dashboard.shipmentSummary.enabled = true
openboxes.dashboard.indicatorSummary.enabled = false

// Column 2
openboxes.dashboard.binLocationSummary.enabled = true
openboxes.dashboard.productSummary.enabled = true
openboxes.dashboard.genericProductSummary.enabled = false
openboxes.dashboard.expiringSummary.enabled = true

// Column 3
openboxes.dashboard.newsSummary.enabled = true
openboxes.dashboard.activitySummary.enabled = true
openboxes.dashboard.valueSummary.enabled = false
openboxes.dashboard.tagSummary.enabled = true
openboxes.dashboard.catalogsSummary.enabled = true

// Default value for news summary
openboxes.dashboard.newsSummary.newsItems = []
openboxes.dashboard.newsSummary.rssUrl = "https://openboxes.com/blog/index.xml"
openboxes.dashboard.newsSummary.limit = 25

openboxes {
    tablero {
        enabled = true
        configurations {
            personal = "My Dashboard"
            warehouse = "Warehouse Management"
        }
        endpoints {
            number {
                 inProgressPutaways {
                    endpoint = "/${appName}/apitablero/getInProgressPutaways"
                    archived = ['warehouse']
                    order = 4
                }
                inventoryByLotAndBin {
                    endpoint = "/${appName}/apitablero/getInventoryByLotAndBin"
                    archived = ['warehouse']
                    order = 1
                }
                inProgressShipments {
                    endpoint = "/${appName}/apitablero/getInProgressShipments"
                    archived = []
                    order = 3
                }
                receivingBin {
                    endpoint = "/${appName}/apitablero/getReceivingBin"
                    archived = ['warehouse']
                    order = 2
                }
                itemsInventoried {
                    endpoint = "/${appName}/apitablero/getItemsInventoried"
                    archived = ['personal', 'warehouse']
                    order = 5
                }
                defaultBin {
                    endpoint = "/${appName}/apitablero/getDefaultBin"
                    archived = ['personal']
                    order = 6
                }
                negativeInventory {
                    endpoint = "/${appName}/apitablero/getProductWithNegativeInventory"
                    archived = ['personal']
                    order = 7
                }
                expiredStock {
                    endpoint = "/${appName}/apitablero/getExpiredProductsInStock"
                    archived = ['personal', 'warehouse']
                    order = 8 
                }
            }
            graph {
                inventorySummary {
                    endpoint = "/${appName}/apitablero/getInventorySummary"
                    archived = []
                    datalabel = true
                    order = 1
                    colors {
                        labels {
                            success = ["In stock"]
                            warning = ["Above maximum", "Below reorder", "Below minimum"]
                            error = ["No longer in stock"]
                        }
                    }
                }
                expirationSummary {
                    endpoint = "/${appName}/apitablero/getExpirationSummary"
                    archived = ['warehouse']
                    order = 2
                    colors {
                        datasets {
                            state6 = ["Expiration(s)"]
                        }
                        labels {
                            state5 = ["today", "within 30 days", "within 90 days", "within 180 days", "within 360 days"]
                        }
                    }
                }
                sentStockMovements {
                    endpoint = "/${appName}/apitablero/getSentStockMovements"
                    archived = ['personal']
                    stacked = true
                    datalabel = true
                    order = 9
                }
                outgoingStock {
                    endpoint = "/${appName}/apitablero/getOutgoingStock"
                    archived = ['warehouse']
                    order = 4
                    colors {
                        datasets {
                            success = ["first"]
                            warning = ["second"]
                            error = ["third"]
                        }
                    }
                }
                receivedStockMovements {
                    endpoint = "/${appName}/apitablero/getReceivedStockMovements"
                    archived = ['personal']
                    stacked = true
                    datalabel = true
                    order = 7
                }
                discrepancy {
                    endpoint = "/${appName}/apitablero/getDiscrepancy"
                    archived = []
                    order = 6
                }
                delayedShipments {
                    endpoint = "/${appName}/apitablero/getDelayedShipments"
                    archived = ['warehouse']
                    order = 5
                     colors {
                        datasets {
                            state5 = ["first"]
                            state4 = ["second"]
                            state3 = ["third"]
                        }
                    }
                }
                incomingStock {
                    endpoint = "/${appName}/apitablero/getIncomingStock"
                    archived = ['warehouse']
                    order = 3
                    colors {
                        datasets {
                            state6 = ["first"]
                            state7 = ["second"]
                            state8 = ["third"]
                        }
                    }
                }
            }
        }
    }
}

// OpenBoxes identifier config
openboxes.identifier.separator = Constants.DEFAULT_IDENTIFIER_SEPARATOR
openboxes.identifier.numeric = Constants.RANDOM_IDENTIFIER_NUMERIC_CHARACTERS
openboxes.identifier.alphabetic = Constants.RANDOM_IDENTIFIER_ALPHABETIC_CHARACTERS
openboxes.identifier.alphanumeric = Constants.RANDOM_IDENTIFIER_ALPHANUMERIC_CHARACTERS
openboxes.identifier.transaction.format = Constants.DEFAULT_TRANSACTION_NUMBER_FORMAT
openboxes.identifier.order.format = Constants.DEFAULT_ORDER_NUMBER_FORMAT
openboxes.identifier.product.format = Constants.DEFAULT_PRODUCT_NUMBER_FORMAT
openboxes.identifier.productSupplier.prefix.enabled = true
openboxes.identifier.productSupplier.format = Constants.DEFAULT_PRODUCT_SUPPLIER_NUMBER_FORMAT
openboxes.identifier.receipt.format = Constants.DEFAULT_RECEIPT_NUMBER_FORMAT
openboxes.identifier.requisition.format = Constants.DEFAULT_REQUISITION_NUMBER_FORMAT
openboxes.identifier.shipment.format = Constants.DEFAULT_SHIPMENT_NUMBER_FORMAT
openboxes.identifier.sequenceNumber.format = Constants.DEFAULT_SEQUENCE_NUMBER_FORMAT

openboxes.identifier.organization.format = Constants.DEFAULT_ORGANIZATION_NUMBER_FORMAT
openboxes.identifier.organization.minSize = 2
openboxes.identifier.organization.maxSize = 3

openboxes.identifier.purchaseOrder.generatorType = IdentifierGeneratorTypeCode.SEQUENCE
openboxes.identifier.purchaseOrder.format = "PO-\${destinationPartyCode}-\${sequenceNumber}"
openboxes.identifier.purchaseOrder.properties = ["destinationPartyCode":"destinationParty.code"]

// OpenBoxes default line printer port
openboxes.linePrinterTerminal.port = "LPT1"

// Require approval on purchase orders
openboxes.purchasing.approval.enabled = false
openboxes.purchasing.approval.minimumAmount = 0.00
openboxes.purchasing.approval.defaultRoleTypes = [RoleType.ROLE_APPROVER]

// Experimental feature that approximates a costing method to provide a crude unit price used
// for inventory valuation.
//
// Possible values:
//  * UpdateUnitPriceMethodCode.USER_DEFINED_PRICE (default)
//  * UpdateUnitPriceMethodCode.AVERAGE_PURCHASE_PRICE
//  * UpdateUnitPriceMethodCode.FIRST_PURCHASE_PRICE
//  * UpdateUnitPriceMethodCode.LAST_PURCHASE_PRICE
openboxes.purchasing.updateUnitPrice.enabled = false
openboxes.purchasing.updateUnitPrice.method = UpdateUnitPriceMethodCode.USER_DEFINED_PRICE

// OpenBoxes default uploads directory location
openboxes.uploads.location = "uploads"

// Cache configuration
springcache {
    defaults {
        // set default cache properties that will apply to all caches that do not override them
        eternal = false
        diskPersistent = false
        memoryStoreEvictionPolicy = "LRU"
        timeToLive = 3600       // 1 hour = 60 * 60 * 1
        timeToIdle = 1800       // 30 minutes = 60 * 60 * 0.5
    }
    caches {
        binLocationReportCache {}
        binLocationSummaryCache {}
        dashboardCache {}
        dashboardTotalStockValueCache {}
        dashboardProductSummaryCache {}
        dashboardGenericProductSummaryCache {}
        fastMoversCache {}
        inventoryBrowserCache {}
        inventorySnapshotCache {}
        megamenuCache {}
        messageCache {}
        quantityOnHandCache {}
        selectTagCache {}
        selectTagsCache {}
        selectCategoryCache {}
        selectCatalogsCache {}
    }
}


// Grails Sentry/Raven plugin
// NOTE: You'll need to enable the plugin and set a DSN using an external config properties file
// (namely, openboxes-config.properties or openboxes-config.groovy)
grails.plugins.raven.active = false
grails.plugin.raven.dsn = "https://{PUBLIC_KEY}:{SECRET_KEY}@app.getsentry.com/{PROJECT_ID}"

// Default Ajax request timeout
openboxes.ajaxRequest.timeout = 120000

// Google analytics and feedback have been removed until I can improve performance.
//google.analytics.enabled = false
//google.analytics.webPropertyID = "UA-xxxxxx-x"

// Fullstory integration
openboxes.fullstory.enabled = false
openboxes.fullstory.debug = false
openboxes.fullstory.host = "fullstory.com"
openboxes.fullstory.org = ""
openboxes.fullstory.namespace = "FS"

// Hotjar integration
openboxes.hotjar.enabled = false
openboxes.hotjar.hjid = 0
openboxes.hotjar.hjsv = 6

// Feedback mechanism that allows screenshots
//openboxes.feedback.enabled = false

// Forecasting feature
openboxes.forecasting.enabled = false
openboxes.forecasting.demandPeriod = 180

// Bill of Materials feature
openboxes.bom.enabled = false

// User Signup
openboxes.signup.enabled = true

// UserVoice widget
openboxes.uservoice.widget.enabled = true
openboxes.uservoice.widget.position = "right"

// UserVoice widget
openboxes.zopim.widget.enabled = false
openboxes.zopim.widget.url = "//v2.zopim.com/?2T7RMi7ERqr3s8N20KQ3wOBRudcwosBA"

// JIRA Issue Collector
openboxes.jira.issue.collector.enabled = false
openboxes.jira.issue.collector.url = "https://openboxes.atlassian.net/s/d41d8cd98f00b204e9800998ecf8427e/en_USgc5zl3-1988229788/6318/12/1.4.10/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=fb813fdb"

// OpenBoxes Feedback
openboxes.mail.feedback.enabled = false
openboxes.mail.feedback.recipients = ["feedback@openboxes.com"]

// OpenBoxes Error Emails (bug reports)
openboxes.mail.errors.enabled = true
openboxes.mail.errors.recipients = ["errors@openboxes.com"]

// Barcode scanner (disabled by default)
openboxes.scannerDetection.enabled = false

// Default delay and min length for typeahead components
openboxes.typeahead.delay = 300
openboxes.typeahead.minLength = 3

// Send stock alerts
openboxes.jobs.sendStockAlertsJob.enabled = true
openboxes.jobs.sendStockAlertsJob.skipOnEmpty = true
openboxes.jobs.sendStockAlertsJob.daysUntilExpiry = 60
openboxes.jobs.sendStockAlertsJob.cronExpression = "0 0 0 * * ?" // every day at midnight

// Refresh inventory snapshots
openboxes.jobs.refreshInventorySnapshotJob.enabled = true
openboxes.jobs.refreshInventorySnapshotJob.retryOnError = false
openboxes.jobs.refreshInventorySnapshotJob.maxRetryAttempts = 3

// Refresh transaction fact table
openboxes.jobs.refreshTransactionFactJob.enabled = true
openboxes.jobs.refreshTransactionFactJob.cronExpression = "0 0 0 * * ?" // every day at midnight

// Refresh demand data snapshots
openboxes.jobs.refreshDemandDataJob.enabled = true
openboxes.jobs.refreshDemandDataJob.cronExpression = "0 0 1 * * ?" // at 01:00:00am every day

// Assign identifier job
openboxes.jobs.assignIdentifierJob.enabled = true
openboxes.jobs.assignIdentifierJob.cronExpression = "0 * * * * ?" // every minute

// Calculate current quantity on hand
openboxes.jobs.calculateQuantityJob.enabled = true
openboxes.jobs.calculateQuantityJob.cronExpression = "0 0 0/2 * * ?" // every two hours starting at midnight
openboxes.jobs.calculateQuantityJob.enableOptimization = false

// Calculate historical quantity on hand
openboxes.jobs.calculateHistoricalQuantityJob.enabled = false
openboxes.jobs.calculateHistoricalQuantityJob.cronExpression = "0 0 0 * * ?" // every day at midnight
openboxes.jobs.calculateHistoricalQuantityJob.daysToProcess = 540   // 18 months

// Data Cleaning Job
openboxes.jobs.dataCleaningJob.enabled = true
openboxes.jobs.dataCleaningJob.cronExpression = "0 */5 * * * ?" // every five minutes

// Data Migration Job (enabled, but needs to be triggered manually)
openboxes.jobs.dataMigrationJob.enabled = true

// Update exchange rates job
openboxes.jobs.updateExchangeRatesJob.enabled = false
openboxes.jobs.updateExchangeRatesJob.cronExpression = "0 0 * * * ?" // every hour

// LDAP configuration
openboxes.ldap.enabled = false
openboxes.ldap.context.managerDn = "cn=read-only-admin,dc=example,dc=com"
openboxes.ldap.context.managerPassword = "password"
//openboxes.ldap.context.server = "ldap://ldap.forumsys.com:389"
openboxes.ldap.context.server.host = "ldap.forumsys.com"
openboxes.ldap.context.server.port = 389

// LDAP Search
openboxes.ldap.search.base = "dc=example,dc=com"
openboxes.ldap.search.filter = "(uid={0})"
openboxes.ldap.search.searchSubtree = true
openboxes.ldap.search.attributesToReturn = ['mail', 'givenName']

//openboxes.ldap.authorities.retrieveGroupRoles = false
//openboxes.ldap.authorities.groupSearchBase ='DC=example,DC=com'
//openboxes.ldap.authorities.groupSearchFilter = 'member={0}'
//openboxes.ldap.authorities.role.ROLE_ADMIN = "ou=mathematicians,dc=example,dc=com"
//openboxes.ldap.authorities.role.ROLE_MANAGER = "ou=scientists,dc=example,dc=com"
//openboxes.ldap.authorities.role.ROLE_ASSISTANT = "ou=assistants,dc=example,dc=com"
//openboxes.ldap.authorities.role.ROLE_BROWSER = "ou=browsers,dc-example,dc=com"

// Stock Card > Consumption > Reason codes
// Examples: Stock out, Low stock, Expired, Damaged, Could not locate, Insufficient quantity reconditioned
openboxes.stockCard.consumption.reasonCodes = [ReasonCode.STOCKOUT, ReasonCode.LOW_STOCK, ReasonCode.EXPIRED, ReasonCode.DAMAGED, ReasonCode.COULD_NOT_LOCATE, ReasonCode.INSUFFICIENT_QUANTITY_RECONDITIONED]

// Localization configuration - default and supported locales
openboxes.locale.custom.enabled = false
openboxes.locale.defaultLocale = 'en'
openboxes.locale.supportedLocales = ['ar', 'en', 'fr', 'de', 'it', 'es', 'pt', 'fi']

// Currency configuration
openboxes.locale.defaultCurrencyCode = "USD"
openboxes.locale.defaultCurrencySymbol = "\$"
openboxes.locale.supportedCurrencyCodes = ["USD","CAD","EUR","GBP"]

// Currency API configuration
openboxes.locale.currencyApi.url = "https://api.exchangeratesapi.io/latest?base=%s"
openboxes.locale.currencyApi.apiKey = ""
openboxes.locale.currencyApi.password = ""

// Translation API configuration (https://tech.yandex.com/translate/)
openboxes.locale.translationApi.url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=%s&text=%s&lang=%s&format=%s"
openboxes.locale.translationApi.apiKey = ""
openboxes.locale.translationApi.format = "plain"

// Inventory snapshot configuration
openboxes.inventorySnapshot.batchSize = 1500

// Minimum date for expiration date
openboxes.expirationDate.minValue = new Date("01/01/2000")

// Global megamenu configuration
openboxes.megamenu.dashboard.enabled = true
openboxes.megamenu.analytics.enabled = true
openboxes.megamenu.inventory.enabled = true
openboxes.megamenu.orders.enabled = true
openboxes.megamenu.stockRequest.enabled = true
openboxes.megamenu.stockMovement.enabled = true
openboxes.megamenu.putaways.enabled = true
openboxes.megamenu.reporting.enabled = true
openboxes.megamenu.products.enabled = true
openboxes.megamenu.configuration.enabled = true
openboxes.megamenu.customLinks.enabled = false
openboxes.megamenu.inbound.enabled = true
openboxes.megamenu.outbound.enabled = true
openboxes.megamenu.requisitionTemplate.enabled = true
// Global megamenu configuration (deprecated)
openboxes.megamenu.requisitions.enabled = false
openboxes.megamenu.shipping.enabled = false
openboxes.megamenu.receiving.enabled = false


// Custom links example
//openboxes {
//	megamenu {
//		customLinks {
//			content = [
//					[label: "Search Google", href: "https://www.google.com", target: "_blank"]
//			]
//		}
//	}
//}

openboxes.generateName.separator = " - "


// Disable feature during development
openboxes.shipping.splitPickItems.enabled = true

// Add item to shipment search
openboxes.shipping.search.maxResults = 1000

// Automatically create temporary receiving locations for shipments
openboxes.receiving.createReceivingLocation.enabled = true
openboxes.receiving.receivingLocation.prefix = Constants.DEFAULT_RECEIVING_LOCATION_PREFIX

// Pagination
openboxes.api.pagination.enabled = true

// Grails doc configuration
grails.doc.title = "OpenBoxes"
grails.doc.subtitle = ""
grails.doc.authors = "Justin Miranda"
grails.doc.license = "Eclipse Public License - Version 1.0"
grails.doc.copyright = ""
grails.doc.footer = ""

// Added by the Joda-Time plugin:
grails.gorm.default.mapping = {
    id generator: 'uuid'
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDateMidnight, class: org.joda.time.DateMidnight
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDateTime, class: org.joda.time.DateTime
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDateTimeZoneAsString, class: org.joda.time.DateTimeZone
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDurationAsString, class: org.joda.time.Duration
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentInstantAsMillisLong, class: org.joda.time.Instant
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentInterval, class: org.joda.time.Interval
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentLocalDate, class: org.joda.time.LocalDate
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime, class: org.joda.time.LocalDateTime
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentLocalTime, class: org.joda.time.LocalTime
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentPeriodAsString, class: org.joda.time.Period
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentTimeOfDay, class: org.joda.time.TimeOfDay
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentYearMonthDay, class: org.joda.time.YearMonthDay
    "user-type" type: org.jadira.usertype.dateandtime.joda.PersistentYears, class: org.joda.time.Years
}

grails.gorm.default.constraints = {
    expirationDateConstraint(nullable:true, min: openboxes.expirationDate.minValue)
}
