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
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.IdentifierGeneratorTypeCode
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UpdateUnitPriceMethodCode
import org.pih.warehouse.order.OrderStatus

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
grails.views.default.codec = "html" // none, html, base64
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
openboxes.logo.label = ""
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
    dashboard {
        yearTypes {
            fiscalYear {
                start = "07/01" // format: MM/DD, For PIH and the Govt of Dominica fiscal year start July 1
                end = "06/30" // format: MM/DD
                labelYearPrefix = "FY "
                yearFormat = "yy"
            }
            calendarYear {
                start = "01/01"
                end = "12/31"
                labelYearPrefix = ""
                yearFormat = "yyyy"
            }
        }
    }
}

openboxes {
    dashboardConfig {
        dashboards {
            personal {
                name = "My Dashboard"
                filters {}
                widgets = [
                        [
                            widgetId: "inventoryByLotAndBin",
                            order: 1
                        ],
                        [
                            widgetId: "receivingBin",
                            order: 2
                        ],
                        [
                            widgetId: "inProgressShipments",
                            order: 3
                        ],
                        [
                            widgetId: "inProgressPutaways",
                            order: 4
                        ],

                        [
                            widgetId: "inventorySummary",
                            order: 1
                        ],
                        [
                            widgetId: "expirationSummary",
                            order: 2
                        ],
                        [
                            widgetId: "incomingStock",
                            order: 3
                        ],
                        [
                            widgetId: "outgoingStock",
                            order: 4
                        ],
                        [
                            widgetId: "delayedShipments",
                            order: 5
                        ],
                        [
                            widgetId: "discrepancy",
                            order: 6
                        ]
                ]
            }
            warehouse {
                name = "Warehouse Management"
                filters {}
                widgets = [
                        [
                            widgetId: "inventoryByLotAndBin",
                            order: 1
                        ],
                        [
                            widgetId: "receivingBin",
                            order: 2
                        ],
                        [
                            widgetId: "inProgressShipments",
                            order: 3
                        ],
                        [
                            widgetId: "inProgressPutaways",
                            order: 4
                        ],
                        [
                            widgetId: "itemsInventoried",
                            order: 5
                        ],

                        [
                            widgetId: "inventorySummary",
                            order: 1
                        ],
                        [
                            widgetId: "expirationSummary",
                            order: 2
                        ],
                        [
                            widgetId: "incomingStock",
                            order: 3
                        ],
                        [
                            widgetId: "outgoingStock",
                            order: 4
                        ],
                        [
                            widgetId: "delayedShipments",
                            order: 5
                        ],
                        [
                            widgetId: "discrepancy",
                            order: 6
                        ]
                ]
            }
            inventory {
                name = "Inventory Management"
                filters {}
                widgets = [
                        [
                            widgetId: "receivingBin",
                            order: 1
                        ],
                        [
                            widgetId: "defaultBin",
                            order: 2
                        ],
                        [
                            widgetId: "negativeInventory",
                            order: 3
                        ],
                        [
                            widgetId: "expiredStock",
                            order: 4
                        ],
                        [
                            widgetId: "openStockRequests",
                            order: 5
                        ],

                        [
                            widgetId: "delayedShipments",
                            order: 1
                        ],
                        [
                            widgetId: "productsInventoried",
                            order: 2
                        ]
                ]
            }
            transaction {
                name = "Transaction Management"
                filters {}
                widgets = [
                        [
                            widgetId: "fillRateSnapshot",
                            order: 1
                        ],

                        [
                            widgetId: "receivedStockMovements",
                            order: 1
                        ],
                        [
                            widgetId: "sentStockMovements",
                            order: 2
                        ],
                        [
                            widgetId: "lossCausedByExpiry",
                            order: 3
                        ],
                        [
                            widgetId: "percentageAdHoc",
                            order: 4
                        ],
                        [
                            widgetId: "fillRate",
                            order: 5
                        ],
                        [
                            widgetId: "stockOutLastMonth",
                            order: 6
                        ]
                ]
            }
            fillRate {
                name = "Fill Rate"
                filters {
                    category {
                        endpoint = "/${appName}/categoryApi/list"
                    }
                }
                widgets = [
                        [
                            widgetId: "fillRateSnapshot",
                            order: 1
                        ],

                        [
                            widgetId: "fillRate",
                            order: 1
                        ]
                ]
            }
            supplier {
                name = "Supplier Dashboard"
                filters {
                    supplier {
                        endpoint = "/${appName}/api/locations?direction=INBOUND"
                    }
                }
                widgets = [
                    [
                        widgetId: "numberOfOpenPurchaseOrders",
                        order: 1
                    ]
                ]
            }
        }
        // TODO: OBPIH-4384 Refactor indicator filters to be more generic (currently filters are hardcoded on the frontend, these should be defined here and rendered there basing on config)
        dashboardWidgets {
            inProgressPutaways {
                enabled = true
                title = "react.dashboard.inProgressPutaways.title.label"
                info = "react.dashboard.inProgressPutaways.info.label"
                subtitle = "react.dashboard.subtitle.putaways.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/inProgressPutaways"
            }
            inventoryByLotAndBin {
                enabled = true
                title = "react.dashboard.inventoryByLotAndBin.title.label"
                info = "react.dashboard.inventoryByLotAndBin.info.label"
                subtitle = "react.dashboard.subtitle.inStock.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/inventoryByLotAndBin"
            }
            inProgressShipments {
                enabled = true
                title = "react.dashboard.inProgressShipments.title.label"
                info = "react.dashboard.inProgressShipments.info.label"
                subtitle = "react.dashboard.subtitle.shipments.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/inProgressShipments"
            }
            receivingBin {
                enabled = true
                title = "react.dashboard.receivingBin.title.label"
                info = "react.dashboard.receivingBin.info.label"
                subtitle = "react.dashboard.subtitle.products.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/receivingBin"
            }
            itemsInventoried {
                enabled = true
                title = "react.dashboard.itemsInventoried.title.label"
                info = "react.dashboard.itemsInventoried.info.label"
                subtitle = "react.dashboard.subtitle.items.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/itemsInventoried"
            }
            defaultBin {
                enabled = true
                title = "react.dashboard.defaultBin.title.label"
                info = "react.dashboard.defaultBin.info.label"
                subtitle = "react.dashboard.subtitle.products.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/defaultBin"
            }
            negativeInventory {
                enabled = true
                title = "react.dashboard.productWithNegativeInventory.title.label"
                info = "react.dashboard.productWithNegativeInventory.info.label"
                subtitle = "react.dashboard.subtitle.products.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/productWithNegativeInventory"
            }
            expiredStock {
                enabled = true
                title = "react.dashboard.expiredProductsInStock.title.label"
                info = "react.dashboard.expiredProductsInStock.info.label"
                subtitle = "react.dashboard.subtitle.products.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/expiredProductsInStock"
            }
            openStockRequests {
                enabled = true
                title = "react.dashboard.openStockRequests.title.label"
                info = "react.dashboard.openStockRequests.info.label"
                subtitle = "react.dashboard.requests.subtitle.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/openStockRequests"
            }
            inventoryValue {
                enabled = true
                title = "react.dashboard.inventoryValue.title.label"
                info = ''
                subtitle = "react.dashboard.subtitle.inStock.label"
                numberType = 'dollars'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/inventoryValue"
            }

            fillRateSnapshot {
                enabled = true
                title = "react.dashboard.fillRateSnapshot.title.label"
                info = "react.dashboard.fillRateSnapshot.info.label"
                graphType = 'sparkline'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/fillRateSnapshot"
            }

            requisitionCountByYear {
                enabled = true
                title = "react.dashboard.requisitionCountByYear.title.label"
                info = "react.dashboard.requisitionCountByYear.info.label"
                graphType = "bar"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/requisitionsByYear"
                yearTypeFilter {
                    parameter = "yearType"
                    defaultValue = "fiscalYear"
                    options = [
                        [label: "react.dashboard.fiscalYear.label", value: "fiscalYear"],
                        [label: "react.dashboard.calendarYear.label", value: "calendarYear"]
                    ]
                }
            }
            inventorySummary {
                enabled = true
                title = "react.dashboard.inventorySummaryData.title.label"
                info = "react.dashboard.inventorySummaryData.info.label"
                graphType = "horizontalBar"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/inventorySummary"
                datalabel = true
                colors {
                    labels {
                        success = ["In stock"]
                        warning = ["Above maximum", "Below reorder", "Below minimum"]
                        error = ["No longer in stock"]
                    }
                }
            }
            expirationSummary {
                enabled = true
                title = "react.dashboard.expirationSummaryData.title.label"
                info = "react.dashboard.expirationSummaryData.info.label"
                graphType = "line"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/expirationSummary"
                timeFilter = true
                colors {
                    datasets {
                        state6 = ["Expiration(s)"]
                    }
                    labels {
                        state5 = [
                                [code : "react.dashboard.timeline.today.label", message : "today"],
                                [code : "react.dashboard.timeline.within30Days.label", message : "within 30 days"],
                                [code : "react.dashboard.timeline.within90Days.label", message : "within 90 days"],
                                [code : "react.dashboard.timeline.within180Days.label", message : "within 180 days"],
                                [code : "react.dashboard.timeline.within360Days.label", message : "within 360 days"]
                        ]
                    }
                }
            }
            incomingStock {
                enabled = true
                title = "react.dashboard.incomingStock.title.label"
                info = "react.dashboard.incomingStock.info.label"
                graphType = "numbers"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/incomingStock"
                colors {
                    datasets {
                        state6 = ["first"]
                        state7 = ["second"]
                        state8 = ["third"]
                    }
                }
            }
            outgoingStock {
                enabled = true
                title = "react.dashboard.outgoingStock.title.label"
                info = "react.dashboard.outgoingStock.info.label"
                graphType = "numbers"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/outgoingStock"
                colors {
                    datasets {
                        success = ["first"]
                        warning = ["second"]
                        error = ["third"]
                    }
                }
            }
            receivedStockMovements {
                enabled = true
                title = "react.dashboard.receivedStockData.title.label"
                info = "react.dashboard.receivedStockData.info.label"
                graphType = "bar"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/receivedStockMovements"
                timeFilter = true
                stacked = true
                datalabel = true
            }
            discrepancy {
                enabled = true
                title = "react.dashboard.discrepancy.title.label"
                info = "react.dashboard.discrepancy.info.label"
                graphType = "table"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/discrepancy"
                timeFilter = true
            }
            delayedShipments {
                enabled = true
                title = "react.dashboard.delayedShipments.title.label"
                info = "react.dashboard.delayedShipments.info.label"
                graphType = "numberTable"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/delayedShipments"
                colors {
                    datasets {
                        state5 = ["first"]
                        state4 = ["second"]
                        state3 = ["third"]
                    }
                }
            }
            sentStockMovements {
                enabled = true
                title = "react.dashboard.sentStockMovements.title.label"
                info = "react.dashboard.sentStockMovements.info.label"
                graphType = "bar"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/sentStockMovements"
                timeFilter = true
                stacked = true
                datalabel = true
            }
            lossCausedByExpiry {
                enabled = false
                title = "react.dashboard.lossCausedByExpiry.title.label"
                info = ""
                graphType = "bar"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/lossCausedByExpiry"
                timeFilter = true
                stacked = true
                colors {
                    datasets {
                        success = ["Inventory value not expired last day of month"]
                        warning = ["Inventory value expired last day of month"]
                        error = ["Inventory value removed due to expiry"]
                    }
                }
            }
            productsInventoried {
                enabled = false
                title = "react.dashboard.productsInventoried.title.label"
                info = ""
                graphType = "numbersCustomColors"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/productsInventoried"
                colors {
                    datasets {
                        state6 = ["first"]
                        state7 = ["second"]
                        state8 = ["third"]
                    }
                }
            }
            percentageAdHoc {
                enabled = true
                title = "react.dashboard.percentageAdHoc.title.label"
                info = "react.dashboard.percentageAdHoc.info.label"
                graphType = "doughnut"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/percentageAdHoc"
                legend = true
                datalabel = true
                colors {
                    labels {
                        state5 = ["STOCK"]
                        state4 = ["ADHOC"]
                    }
                }
            }
            fillRate {
                enabled = true
                title = "react.dashboard.fillRate.title.label"
                info = "react.dashboard.fillRate.info.label"
                graphType = "bar"
                type = 'graph'
                legend = true
                endpoint = "/${appName}/api/dashboard/fillRate"
                timeFilter = true
                locationFilter = true
                timeLimit = 12
                doubleAxeY = true
                datalabel = false
                size = 'big'
                colors {
                    datasets {
                        state3 = ["Request lines submitted"]
                        state6 = ["Lines cancelled stock out"]
                        state2 = ["Average Fill Rate"]
                        state8 = ["Average of target Fill Rate"]
                    }
                }
            }
            stockOutLastMonth {
                enabled = true
                title = "react.dashboard.stockOutLastMonth.title.label"
                info = "react.dashboard.stockOutLastMonth.info.label"
                graphType = "doughnut"
                type = 'graph'
                endpoint = "/${appName}/api/dashboard/stockOutLastMonth"
                legend = true
                datalabel = true
                colors {
                    labels {
                        success = ["Never"]
                        warning = ["Stocked out <1 week"]
                        state2  = ["Stocked out 1-2 weeks"]
                        state1  = ["Stocked out 2-3 weeks"]
                        error   = ["Stocked out 3-4 weeks"]
                    }
                }
            }
            numberOfOpenPurchaseOrders {
                enabled = true
                title = "react.dashboard.numberOfOpenPurchaseOrders.title.label"
                info = "react.dashboard.numberOfOpenPurchaseOrders.info.label"
                subtitle = "react.dashboard.subtitle.purchaseOrders.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/${appName}/api/dashboard/openPurchaseOrdersCount"
            }
        }
    }
}

//Breadcrumbs configuration
breadcrumbsConfig {
        inbound {
            actionLabel = "react.stockMovement.inbound.create.label"
            defaultActionLabel = "Create Inbound"
            listLabel = "react.stockMovement.label"
            defaultListLabel = "Stock Movement"
            actionUrl = "/${appName}/stockMovement/createInbound/"
            listUrl   = "/${appName}/stockMovement/list?direction=INBOUND"
        }
        outbound {
            actionLabel = "react.stockMovement.outbound.create.label"
            defaultActionLabel = "Create Outbound"
            listLabel = "react.stockMovement.label"
            defaultListLabel = "Stock Movement"
            actionUrl = "/${appName}/stockMovement/createOutbound/"
            listUrl = "/${appName}/stockMovement/list?direction=OUTBOUND"
        }
        request {
            actionLabel = "react.stockMovement.request.create.label"
            defaultActionLabel = "Create Request"
            listLabel = "react.stockMovement.label"
            defaultListLabel = "Stock Movement"
            actionUrl = "/${appName}/stockMovement/createRequest/"
            listUrl = "/${appName}/stockMovement/list?direction=INBOUND"
        }
        verifyRequest {
            actionLabel = "react.stockMovement.request.verify.label"
            defaultActionLabel = "Verify Request"
            listLabel = "react.stockMovement.label"
            defaultListLabel = "Stock Movement"
            actionUrl = "/${appName}/stockMovement/list"
            listUrl = "/${appName}/stockMovement/list"
        }
        putAway {
            actionLabel = "react.putAway.createPutAway.label"
            defaultActionLabel = "Create Putaway"
            listLabel = "react.breadcrumbs.order.label"
            defaultListLabel = "Order"
            actionUrl = "/${appName}/putAway/create/"
            listUrl = "/${appName}/order/list?orderType=PUTAWAY_ORDER&status=PENDING"
        }
        combinedShipments {
            actionLabel = "shipmentFromPO.label"
            defaultActionLabel = "Ship from PO"
            listLabel = "react.stockMovement.label"
            defaultListLabel = "Stock Movement"
            actionUrl = "/${appName}/stockMovement/createCombinedShipments/"
            listUrl   = "/${appName}/stockMovement/list?direction=INBOUND"
        }
        invoice {
            actionLabel = "react.invoice.create.label"
            defaultActionLabel = "Create"
            listLabel = "react.invoice.label"
            defaultListLabel = "Invoice"
            actionUrl = "/${appName}/invoice/create/"
            listUrl   = "/${appName}/invoice/list/"
        }
        stockTransfer {
            actionLabel = "react.stockTransfer.createStockTransfer.label"
            defaultActionLabel = "Create Stock Transfer"
            listLabel = "react.stockTransfer.label"
            defaultListLabel = "Stock Transfer"
            actionUrl = "/${appName}/stockTransfer/create/"
            listUrl = "/"
        }
        replenishment {
            actionLabel = "react.replenishment.createReplenishment.label"
            defaultActionLabel = "Create Replenishment"
            listLabel = "react.replenishment.label"
            defaultListLabel = "Replenishment"
            actionUrl = "/${appName}/replenishment/create/"
            listUrl = "/"
        }
        outboundReturns {
            actionLabel = "react.outboundReturns.createReturn.label"
            defaultActionLabel = "Create Outbound Return"
            listLabel = "react.outboundReturns.label"
            defaultListLabel = "Outbound Returns"
            actionUrl = "/${appName}/stockTransfer/createOutboundReturn/"
            listUrl = "/"
        }
        inboundReturns {
            actionLabel = "react.inboundReturns.createReturn.label"
            defaultActionLabel = "Create Inbound Return"
            listLabel = "react.inboundReturns.label"
            defaultListLabel = "Inbound Returns"
            actionUrl = "/${appName}/stockTransfer/createInboundReturn/"
            listUrl = "/"
        }
        productsConfiguration {
            actionLabel = "productsConfiguration.label"
            defaultActionLabel = "Categories and Products Configuration"
            actionUrl = "/${appName}/productsConfiguration/index"
        }
        locationsConfiguration {
            actionLabel = "locationsConfiguration.label"
            defaultActionLabel = "Locations Configuration"
            actionUrl = "/${appName}/locationsConfiguration/create"
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
openboxes.identifier.invoice.format = Constants.DEFAULT_INVOICE_NUMBER_FORMAT
openboxes.identifier.location.format = Constants.DEFAULT_LOCATION_NUMBER_FORMAT

openboxes.identifier.organization.format = Constants.DEFAULT_ORGANIZATION_NUMBER_FORMAT
openboxes.identifier.organization.minSize = 2
openboxes.identifier.organization.maxSize = 4

openboxes.identifier.purchaseOrder.generatorType = IdentifierGeneratorTypeCode.SEQUENCE
openboxes.identifier.purchaseOrder.sequenceNumber.format = Constants.DEFAULT_PO_SEQUENCE_NUMBER_FORMAT
openboxes.identifier.purchaseOrder.format = "PO-\${destinationPartyCode}-\${sequenceNumber}"
openboxes.identifier.purchaseOrder.properties = ["destinationPartyCode": "destinationParty.code"]

openboxes.identifier.productCode.generatorType = IdentifierGeneratorTypeCode.SEQUENCE
openboxes.identifier.productCode.delimiter = Constants.DEFAULT_IDENTIFIER_SEPARATOR
openboxes.identifier.productCode.format = "\${productTypeCode}\${delimiter}\${sequenceNumber}"
openboxes.identifier.productCode.properties = ["productTypeCode": "code"]

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

// Order status property map
openboxes.order.orderStatusPropertyMap = [
        (OrderStatus.PLACED) : ["productCode", "sourceName", "supplierCode", "manufacturer", "manufacturerCode", "quantity", "unitPrice", "unitOfMeasure", "budgetCode"],
]
openboxes.purchaseOrder.editableProperties = [
        [
                status: OrderStatus.PLACED,
                deny: ["productCode", "sourceName", "supplierCode", "manufacturer", "manufacturerCode", "quantity", "unitPrice", "unitOfMeasure", "budgetCode"]
        ]
]

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
openboxes.forecasting.enabled = true
openboxes.forecasting.demandPeriod = 365

// Bill of Materials feature
openboxes.bom.enabled = false

// User Signup
openboxes.signup.enabled = true
openboxes.signup.recaptcha.enabled = false
openboxes.signup.recaptcha.v2.siteKey = ""
openboxes.signup.recaptcha.v2.secretKey = ""

openboxes {
    signup {
        additionalQuestions {
            enabled = false
//            content = [
//                id: "human",
//                label: "Are you human?",
//                options:
//                [
//                        [key:"", value: ""],
//                        [key:"yes", value: "Yes"],
//                        [key:"no", value: "No"]
//                        [key:"sorta", value: "Sorta"],
//                ]
//
//            ]
        }
    }
}



// UserVoice widget
openboxes.uservoice.widget.enabled = false
openboxes.uservoice.widget.position = "right"

// Zopim widget
openboxes.zopim.widget.enabled = false
openboxes.zopim.widget.url = "//v2.zopim.com/?2T7RMi7ERqr3s8N20KQ3wOBRudcwosBA"

// HelpScout beacon
openboxes.helpscout.widget.color = "#3AB4B1"
openboxes.helpscout.widget.enabled = true
openboxes.helpscout.widget.key = "44ee4f01-5334-4b93-ad25-03037903eb80"

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

// Print barcode labels via USB
openboxes.barcode.printer.name = "printer-thermalprinter"

// Print barcode labels via RAW
openboxes.barcode.printer.ipAddress = "127.0.0.1"
openboxes.barcode.printer.port = 9100

// Default delay and min length for typeahead components
openboxes.typeahead.delay = 300
openboxes.typeahead.minLength = 3

// Allow system administrators to disable refresh on startup
openboxes.refreshAnalyticsDataOnStartup.enabled = true

// Send stock alerts
openboxes.jobs.sendStockAlertsJob.enabled = true
openboxes.jobs.sendStockAlertsJob.skipOnEmpty = true
openboxes.jobs.sendStockAlertsJob.daysUntilExpiry = 60
openboxes.jobs.sendStockAlertsJob.cronExpression = "0 0 0 * * ?" // every day at midnight

// Refresh inventory snapshots
openboxes.jobs.refreshInventorySnapshotJob.enabled = true
openboxes.jobs.refreshInventorySnapshotJob.retryOnError = false
openboxes.jobs.refreshInventorySnapshotJob.maxRetryAttempts = 3

// Refresh inventory snapshots after transaction (only for transaction entries)
openboxes.jobs.refreshInventorySnapshotAfterTransactionJob.enabled = true
openboxes.jobs.refreshInventorySnapshotAfterTransactionJob.retryOnError = false
openboxes.jobs.refreshInventorySnapshotAfterTransactionJob.maxRetryAttempts = 3

// Refresh product availability materialized view
openboxes.jobs.refreshProductAvailabilityJob.enabled = true
openboxes.jobs.refreshProductAvailabilityJob.cronExpression = "0 0 0/2 * * ?" // every two hours starting at midnight

// Use delay when transactions are persisted to avoid missing data
openboxes.jobs.refreshProductAvailabilityJob.delayStart = true
openboxes.jobs.refreshProductAvailabilityJob.delayInMilliseconds = 5000

// Refresh transaction fact table
openboxes.jobs.refreshTransactionFactJob.enabled = true
openboxes.jobs.refreshTransactionFactJob.cronExpression = "0 0 0 * * ?" // every day at midnight

// Refresh stockout data for yesterday
openboxes.jobs.refreshStockoutDataJob.enabled = true
openboxes.jobs.refreshStockoutDataJob.cronExpression = "0 0 1 * * ?" // at 01:00:00am every day

// Refresh demand data snapshots
openboxes.jobs.refreshDemandDataJob.enabled = true
openboxes.jobs.refreshDemandDataJob.cronExpression = "0 0 1 * * ?" // at 01:00:00am every day

// Assign identifier job
openboxes.jobs.assignIdentifierJob.enabled = true
openboxes.jobs.assignIdentifierJob.cronExpression = "0 * * * * ?" // every minute

// Calculate current quantity on hand
openboxes.jobs.calculateQuantityJob.enabled = true
openboxes.jobs.calculateQuantityJob.cronExpression = "0 0 0/12 * * ?" // every twelve hours starting at midnight
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
openboxes.locale.supportedLocales = ['ar','de','en','es','fr','it','pt','fi','zh']

// Currency configuration
openboxes.locale.defaultCurrencyCode = "USD"
openboxes.locale.defaultCurrencySymbol = "\$"
openboxes.locale.supportedCurrencyCodes = ["USD", "CAD", "EUR", "GBP"]

// Currency API configuration
openboxes.locale.currencyApi.url = "https://api.exchangeratesapi.io/latest?base=%s"
openboxes.locale.currencyApi.apiKey = ""
openboxes.locale.currencyApi.password = ""

// Translation API configuration (https://tech.yandex.com/translate/)
openboxes.locale.translationApi.url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=%s&text=%s&lang=%s&format=%s"
openboxes.locale.translationApi.apiKey = ""
openboxes.locale.translationApi.format = "plain"

// Inventory snapshot configuration
openboxes.inventorySnapshot.batchSize = 100

// Minimum date for expiration date
openboxes.expirationDate.minValue = new Date("01/01/2000")
openboxes.expirationDate.format = Constants.EXPIRATION_DATE_FORMAT

// Global megamenu configuration
// TODO: Clean up and add all missing message.properties
openboxes {
    megamenu {
        dashboard {
            enabled = true
            label = "dashboard.label"
            defaultLabel = "Dashboard"
            href = "/${appName}/dashboard/index"
        }
        analytics {
            enabled = true
            requiredRole = [RoleType.ROLE_ADMIN, RoleType.ROLE_SUPERUSER]
            label = "analytics.label"
            defaultLabel = "Analytics"
            menuItems = [
                // TODO: Add option to include label 'beta'
                [label: "inventory.browse.label", defaultLabel: "Browse Inventory", href: "/${appName}/inventoryBrowser/index"],
                [label: "inventory.snapshot.label", defaultLabel: "Inventory Snapshots", href: "/${appName}/snapshot/list"],
                [label: "consumption.report.label", defaultLabel: "Consumption Report", href: "/${appName}/consumption/list"]
            ]
        }
        inventory {
            enabled = true
            label = "inventory.label"
            defaultLabel = "Inventory"
            requiredActivitiesAny = [ActivityCode.MANAGE_INVENTORY]
            subsections = [
                [
                    label: "inventory.browse.label",
                    defaultLabel: "Browse Inventory",
                    menuItems: [
                        [label: "inventory.browse.label", defaultLabel: "Browse Inventory", href: "/${appName}/inventory/browse?resetSearch=true"],
                        // TODO: (Future improvement) Probably further options should be generated dynamicaly (with item count in bracket)...
                    ],
                ],
                [
                    label: "inventory.manage.label",
                    defaultLabel: "Manage Inventory",
                    menuItems: [
                        [label: "inventory.manage.label", defaultLabel: "Manage Inventory", href: "/${appName}/inventory/manage"],
                        [label: "inventory.import.label", defaultLabel: "Import Inventory", href: "/${appName}/batch/importData?type=inventory&execution=e1s1"],
                        [label: "inventory.createStockTransfer.label", defaultLabel: "Create Stock Transfer", requiredActivitiesAll: ActivityCode.binTrackingList(), href: "/${appName}/stockTransfer/create"],
                        [label: "inventory.listStockTransfers.label", defaultLabel: "List Stock Transfers", requiredActivitiesAll: ActivityCode.binTrackingList(), href: "/${appName}/stockTransfer/list"],
                        [label: "inventory.createReplenishment.label", defaultLabel: "Create Replenishment", requiredActivitiesAll: ActivityCode.binTrackingList(), href: "/${appName}/replenishment/index"]
                    ]
                ]
            ]
        }
        purchasing {
            enabled = true
            label = "order.purchasing.label"
            defaultLabel = "Purchasing"
            subsections = [
                [
                    label: "",
                    defaultLabel: "Purchasing",
                    menuItems: [
                            [label: "order.createPurchase.label", defaultLabel: "Create Purchase Order", href: "/${appName}/purchaseOrder/index", requiredActivitiesAny: [ActivityCode.PLACE_ORDER]],
                            [label: "order.listPurchase.label", defaultLabel: "List Purchase Orders", href: "/${appName}/order/list?orderType=PURCHASE_ORDER"],
                            [label: "location.listSuppliers.label", defaultLabel: "List Suppliers", href: "/${appName}/supplier/list"],
                            [label: "shipment.shipfromPO.label", defaultLabel: "Ship from Purchase Order", href: "/${appName}/stockMovement/createCombinedShipments?direction=INBOUND"],
                            [label: "dashboard.supplierDashboard.label", defaultLabel: "Supplier Dashboard", href: "/${appName}/dashboard/supplier"]
                    ]
                ]
            ]
        }
        invoicing {
            enabled = true
            requiredRole = [RoleType.ROLE_INVOICE]
            label = "react.invoicing.label"
            defaultLabel = "Invoicing"
            subsections = [
                [
                    label: "react.invoicing.label",
                    defaultLabel: "Invoicing",
                    menuItems: [
                            [label: "react.invoice.createInvoice.label", defaultLabel: "Create Invoice", href: "/${appName}/invoice/create"],
                            [label: "react.invoice.list.label", defaultLabel: "List Invoices", href: "/${appName}/invoice/list"],
                    ]
                ]
            ]
        }
        inbound {
            enabled = true
            label = "default.inbound.label"
            defaultLabel = "Inbound"
            requiredActivitiesAny = [ActivityCode.RECEIVE_STOCK]
            subsections = [
                    [
                            label: "stockMovements.label",
                            defaultLabel: "Stock Movements",
                            menuItems: [
                                    [label: "inbound.create.label", defaultLabel: "Create Inbound Movement", href: "/${appName}/stockMovement/createInbound?direction=INBOUND"],
                                    [label: "stockRequest.create.label", defaultLabel: "Create Stock Request", href: "/${appName}/stockMovement/createRequest"],
                                    [label: "inbound.list.label", defaultLabel: "List Inbound Movements", href: "/${appName}/stockMovement/list?direction=INBOUND"],
                                    [label: "inboundReturns.create.label", defaultLabel: "Create Inbound Return", href: "/${appName}/stockTransfer/createInboundReturn"]
                            ]
                    ],
                    [
                            label: "putAways.label",
                            defaultLabel: "Putaways",
                            requiredActivitiesAll: ActivityCode.binTrackingList(),
                            menuItems: [
                                    [label: "react.putAway.createPutAway.label", defaultLabel: "Create Putaway", href: "/${appName}/putAway/index"],
                                    [label: "react.putAway.list.label", defaultLabel: "List Putaways", href: "/${appName}/order/list?orderType=PUTAWAY_ORDER&status=PENDING"]
                            ]
                    ]
            ]
        }
        outbound {
            enabled = true
            label = "outbound.label"
            defaultLabel = "Outbound"
            requiredActivitiesAny = [ActivityCode.SEND_STOCK]
            subsections = [
                [
                    label: "",
                    defaultLabel: "Stock Movement",
                    menuItems: [
                        [label: "outbound.create.label", defaultLabel: "Create Outbound Movements", href: "/${appName}/stockMovement/createOutbound?direction=OUTBOUND"],
                        [label: "outbound.list.label", defaultLabel: "List Outbound Movements", href: "/${appName}/stockMovement/list?direction=OUTBOUND"],
                        [label: "outboundReturns.create.label", defaultLabel: "Create Outbound Return", href: "/${appName}/stockTransfer/createOutboundReturn"]
                    ]
                ]
            ]
        }
        reporting {
            enabled = true
            label = "reporting.label"
            defaultLabel = "Reporting"
            subsections = [
                [
                    label: "report.inventoryReports.label",
                    defaultLabel: "Inventory Reports",
                    menuItems: [
                        [label: "report.inStockReport.label", defaultLabel: "In Stock Report", href: "/${appName}/inventory/listInStock"],
                        [label: "report.binLocationReport.label", defaultLabel: "Bin Location Report", href: "/${appName}/report/showBinLocationReport"],
                        [label: "report.expiredStockReport.label", defaultLabel: "Expired Stock Report", href: "/${appName}/inventory/listExpiredStock"],
                        [label: "report.expiringStockReport.label", defaultLabel: "Expiring Stock Report", href: "/${appName}/inventory/listExpiringStock"],
                        [label: "report.inventoryByLocationReport.label", defaultLabel: "Inventory By Location Report", href: "/${appName}/report/showInventoryByLocationReport"],
                        [label: "report.cycleCount.label", defaultLabel: "Cycle Count Report", href: "/${appName}/report/showCycleCountReport"],
                        [label: "report.baselineQohReport.label", defaultLabel: "Baseline QoH Report", href: "/${appName}/inventory/show"],
                        [label: "report.onOrderReport.label", defaultLabel: "Order Report", href: "/${appName}/report/showOnOrderReport"]
                    ]
                ],
                [
                        label: "report.orderReports.label",
                        defaultLabel: "Order Reports",
                        menuItems: [
                                [label: "report.forecastReport.label", defaultLabel: "Forecast Report", href: "/${appName}/report/showForecastReport"],
                                [label: "report.reorderReport.label", defaultLabel: "Reorder Report", href: "/${appName}/inventory/reorderReport"],
                        ]
                ],
                [
                    label: "report.transactionReports.label",
                    defaultLabel: "Transaction Reports",
                    menuItems: [
                        [label: "report.showTransactionReport.label", defaultLabel: "Transaction Report", href: "/${appName}/report/showTransactionReport"],
                        [label: "report.consumption.label", defaultLabel: "Consumption Report", href: "/${appName}/consumption/show"],
                        [label: "report.requestDetailReport.label", defaultLabel: "Request Detail Report", href: "/${appName}/report/showRequestDetailReport"],
                    ]
                ],
                [
                    label: "dataExports.label",
                    defaultLabel: "Data Exports",
                    menuItems: [
                        [label: "product.exportAsCsv.label", defaultLabel: "Export products", href: "/${appName}/product/exportAsCsv"],
                        [label: "export.productSources.label", defaultLabel: "Export product sources", href: "/${appName}/productSupplier/export"],
                        [label: "export.latestInventory.label", defaultLabel: "Export latest inventory date", href: "/${appName}/inventory/exportLatestInventoryDate"],
                        [label: "export.inventoryLevels.label", defaultLabel: "Export inventory levels", href: "/${appName}/inventoryLevel/export"],
                        [label: "export.requisitions.label", defaultLabel: "Export requisitions", href: "/${appName}/requisition/export"],
                        [label: "export.binLocations.label", defaultLabel: "Export bin locations", href: "/${appName}/report/exportBinLocation?downloadFormat=csv"],
                        [label: "export.productDemand.label", defaultLabel: "Export product demand", href: "/${appName}/report/exportDemandReport?downloadFormat=csv"],
                        [label: "export.custom.label", defaultLabel: "Custom data exports", href: "/${appName}/dataExport/index"]
                    ]
                ]
            ]
        }
        products {
            enabled = true
            label = "products.label"
            defaultLabel = "Products"
            requiredActivitiesAny = [ActivityCode.MANAGE_INVENTORY]
            subsections = [
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "product.create.label", defaultLabel: "Create product", href: "/${appName}/product/create"],
                        [label: "products.list.label", defaultLabel: "List Products", href: "/${appName}/product/list"],
                        [label: "product.batchEdit.label", defaultLabel: "Batch edit product", href: "/${appName}/product/batchEdit"],
                        [label: "product.importAsCsv.label", defaultLabel: "Import products", href: "/${appName}/product/importAsCsv"],
                        [label: "product.exportAsCsv.label", defaultLabel: "Export products", href: "/${appName}/product/exportAsCsv"],
                        [label: "productType.label", defaultLabel: "Product Type", href: "/${appName}/productType/list", requiredRole: [RoleType.ROLE_SUPERUSER]]
                    ]
                ],
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "categories.label", defaultLabel: "Categories", href: "/${appName}/category/tree"],
                        [label: "product.catalogs.label", defaultLabel: "Catalogs", href: "/${appName}/productCatalog/list"],
                        [label: "product.tags.label", defaultLabel: "Tags", href: "/${appName}/tag/list"],
                        [label: "attributes.label", defaultLabel: "Attributes", href: "/${appName}/attribute/list"],
                        [label: "product.associations.label", defaultLabel: "Associations", href: "/${appName}/productAssociation/list"],
                    ]
                ],
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "productSuppliers.label", defaultLabel: "Products Sources", href: "/${appName}/productSupplier/list"],
                        [label: "product.components.label", defaultLabel: "Components", href: "/${appName}/productComponent/list"],
                        [label: "productGroups.label", defaultLabel: "Generic Products", href: "/${appName}/productGroup/list"],
                        [label: "unitOfMeasure.label", defaultLabel: "Unit of Measure", href: "/${appName}/unitOfMeasure/list"],
                        [label: "unitOfMeasureClass.label", defaultLabel: "Uom Class", href: "/${appName}/unitOfMeasureClass/list"],
                        [label: "unitOfMeasureConversion.label", defaultLabel: "Uom Conversion", href: "/${appName}/unitOfMeasureConversion/list"]
                    ]
                ]
            ]
        }
        requisitionTemplate {
            enabled = true
            label = "requisitionTemplates.label"
            defaultLabel = "Stock Lists"
            menuItems = [
                [label: "requisitionTemplates.list.label", defaultLabel: "List stock lists", href: "/${appName}/requisitionTemplate/list"],
                [label: "requisitionTemplates.create.label", defaultLabel: "Create stock list", href: "/${appName}/requisitionTemplate/create"],
            ]
        }
        configuration {
            enabled = true
            requiredRole = [RoleType.ROLE_ADMIN, RoleType.ROLE_SUPERUSER]
            label = "configuration.label"
            defaultLabel = "Configuration"
            subsections = [
                [
                    label: "admin.label",
                    defaultLabel: "Administration",
                    menuItems: [
                        [label: "default.settings.label", defaultLabel: "Settings", href: "/${appName}/admin/showSettings"],
                        [label: "cache.label", defaultLabel: "Cache", href: "/${appName}/admin/cache"],
                        [label: "console.label", defaultLabel: "Console", href: "/${appName}/console/index"],
                        [label: "dataImport.label", defaultLabel: "Data Import", href: "/${appName}/batch/importData"],
                        [label: "dataMigration.label", defaultLabel: "Data Migration", href: "/${appName}/migration/index"],
                        [label: "email.label", defaultLabel: "Email", href: "/${appName}/admin/sendMail"],
                        [label: "localization.label", defaultLabel: "Localization", href: "/${appName}/localization/list"]
                    ]
                ],
                [
                    label: "parties.label",
                    defaultLabel: "Locations",
                    menuItems: [
                        [label: "locations.label", defaultLabel: "Locations", href: "/${appName}/location/list"],
                        [label: "locationGroups.label", defaultLabel: "Location groups", href: "/${appName}/locationGroup/list"],
                        [label: "locationTypes.label", defaultLabel: "Location types", href: "/${appName}/locationType/list"],
                        [label: "organizations.label", defaultLabel: "Organizations", href: "/${appName}/organization/list"],
                        [label: "partyRoles.label", defaultLabel: "Party roles", href: "/${appName}/partyRole/list"],
                        [label: "partyTypes.label", defaultLabel: "Party types", href: "/${appName}/partyType/list"],
                        [label: "person.list.label", defaultLabel: "People", href: "/${appName}/person/list"],
                        [label: "roles.label", defaultLabel: "Roles", href: "/${appName}/role/list"],
                        [label: "users.label", defaultLabel: "Users", href: "/${appName}/user/list"],
                    ]
                ],
                [
                    label: "transactions.label",
                    defaultLabel: "Transactions",
                    menuItems: [
                        [label: "transactionsTypes.label", defaultLabel: "Transactions Types", href: "/${appName}/transactionType"],
                        [label: "transactions.label", defaultLabel: "Transactions", href: "/${appName}/inventory/listAllTransactions"],
                        [label: "transaction.add.label", defaultLabel: "Add transaction", href: "/${appName}/inventory/editTransaction"],
                        [label: "import.inventory.label", defaultLabel: "Import Inventory", href: "/${appName}/batch/importData?type=inventory"],
                        [label: "import.inventoryLevel.label", defaultLabel: "Import Inventory Level", href: "/${appName}/batch/importData?type=inventoryLevel"]
                    ]
                ],
                [
                    label: "default.other.label",
                    defaultLabel: "Other",
                    menuItems: [
                        [label: "budgetCode.label", defaultLabel: "Budget Code", href: "/${appName}/budgetCode/list", requiredRole: [RoleType.ROLE_ADMIN, RoleType.ROLE_SUPERUSER]],
                        [label: "containerTypes.label", defaultLabel: "Container Types", href: "/${appName}/containerType/list"],
                        [label: "documents.label", defaultLabel: "Documents", href: "/${appName}/document/list"],
                        [label: "documentTypes.label", defaultLabel: "Document Types", href: "/${appName}/documentType/list"],
                        [label: "eventTypes.label", defaultLabel: "Event Types", href: "/${appName}/eventType/list"],
                        [label: "glAccountType.label", defaultLabel: "GL Account Type", href: "/${appName}/glAccountType/list", requiredRole: [RoleType.ROLE_ADMIN, RoleType.ROLE_SUPERUSER]],
                        [label: "glAccount.label", defaultLabel: "GL Account", href: "/${appName}/glAccount/list", requiredRole: [RoleType.ROLE_ADMIN, RoleType.ROLE_SUPERUSER]],
                        [label: "orderAdjustmentType.label", defaultLabel: "Order Adjustment Type", href: "/${appName}/orderAdjustmentType/list", requiredRole: [RoleType.ROLE_ADMIN, RoleType.ROLE_SUPERUSER]],
                        [label: "paymentMethodTypes.label", defaultLabel: "Payment Method Types", href: "/${appName}/paymentMethodType/list"],
                        [label: "paymentTerms.label", defaultLabel: "Payment Terms", href: "/${appName}/paymentTerm/list"],
                        [label: "preferenceType.label", defaultLabel: "Preference Type", href: "/${appName}/preferenceType/list"],
                        [label: "shippers.label", defaultLabel: "Shippers", href: "/${appName}/shipper/list"],
                        [label: "shipmentWorkflows.label", defaultLabel: "Shipment Workflows", href: "/${appName}/shipmentWorkflow/list"],
                        [label: "productsConfiguration.label", defaultLabel: "Categories and Products Configuration", href: "/${appName}/productsConfiguration/index"],
                        [label: "locationsConfiguration.label", defaultLabel: "Locations Configuration", href: "/${appName}/locationsConfiguration/index"],
                        [label: "loadData.label", defaultLabel: "Load Data", href: "/${appName}/loadData/index"],
                        [label: "resetInstance.label", defaultLabel: "Reset your instance", href: "/${appName}/resettingInstanceInfo/index"]
                    ]
                ]
            ]
        }
        customLinks {
            enabled = true
            label = "customLinks.label"
            defaultLabel = "Custom Links"
            menuItems = [
                    //[label: "requestItemCreation.label", defaultLabel: "Request Item Creation", href: "", target: "_blank"],
            ]
        }

        orders {
            enabled = true
            label = "orders.label"
            defaultLabel = "Orders"
        }
        stockRequest {
            enabled = true
            label = "stockRequests.label"
            defaultLabel = "Stock Requests"
        }
        stockMovement {
            enabled = true
            label = "stockMovements.label"
            defaultLabel = "Stock Movements"
        }
        putaways {
            enabled = true
            label = "putaways.label"
            defaultLabel = "Putaways"
        }

        // deprecated megamenu configuration
        requisitions {
            enabled = false
            label = "requisitions.label"
            defaultLabel = "Requisitions"
        }
        shipping {
            enabled = false
            label = "shipping.label"
            defaultLabel = "Shipping"
        }
        receiving {
            enabled = false
            label = "receiving.label"
            defaultLabel = "Receiving"
        }
    }
    requestorMegamenu {
        request {
            enabled = true
            requiredRole = [RoleType.ROLE_REQUESTOR]
            label = "default.inbound.label"
            defaultLabel = "Inbound"
            subsections = [
                    [
                            label       : "stockMovements.label",
                            defaultLabel: "Stock Movements",
                            menuItems   : [
                                    [label: "stockRequest.create.label", defaultLabel: "Create Stock Request", href: "/${appName}/stockMovement/createRequest"],
                                    [label: "inbound.list.label", defaultLabel: "List Inbound Movements", href: "/${appName}/stockMovement/list?direction=INBOUND"],
                            ]
                    ]
            ]
        }
    }
}

openboxes.generateName.separator = " - "


// Disable feature during development
openboxes.shipping.splitPickItems.enabled = true

// Add item to shipment search
openboxes.shipping.search.maxResults = 1000

// Automatically create temporary receiving locations for shipments
openboxes.receiving.createReceivingLocation.enabled = true
openboxes.receiving.receivingLocation.prefix = Constants.DEFAULT_RECEIVING_LOCATION_PREFIX


openboxes.supportLinks = [
    configureOrganizationsAndLocations: 'https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1291452471/Configure+Organizations+and+Locations',
    manageBinLocations: 'https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1311572061/Manage+Bin+Locations',
    discussionForum: 'https://discuss.openboxes.com/',
    knowledgeBase: 'https://openboxes.helpscoutdocs.com/',
]

// Reset an instance

openboxes.resettingInstance.command = "wget https://raw.githubusercontent.com/openboxes/openboxes/develop/reset-database.sh | sh"

// Product configuration wizard
openboxes.configurationWizard.enabled = true
openboxes.configurationWizard.categoryOptions = [
    defaultCategories: [
        enabled: true,
        fileUrl: "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/default_OB_categories.csv",
        rootCategoryName: "ROOT",
        categoryNameColumnIndex: 0,
        parentCategoryNameColumnIndex: 1,
        title: "OpenBoxes Default Category List",
        description: "<div>A simple and flexible category tree with 25 categories organized into Equipment, Medicine, Supplies, Perishables, and Other. A good place to start for users  who aren’t sure exactly what they want. Can be edited after import. See a sample of the category tree below.</div>" +
                "<div class='category-list'>" +
                "  <ul>" +
                "    <li>Supplies" +
                "      <ul>" +
                "        <li>Office Supplies</li>" +
                "        <li>Medical Supplies" +
                "          <ul>" +
                "            <li>Dental</li>" +
                "            <li>Lab</li>" +
                "            <li>Surgical</li>" +
                "          </ul>" +
                "        </li>" +
                "      </ul>" +
                "    </li>" +
                "    <li>Equipment</li>" +
                "  </ul>" +
                "</div>",
    ],
    unspscCategories: [
        enabled: true,
        // TODO: add option to support 'classpath:'
        fileUrl: "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/UNSPSC_categories.csv",
        rootCategoryName: "ROOT", // needs to match the category from file
        categoryNameColumnIndex: 0,
        parentCategoryNameColumnIndex: 1,
        title: "UNSPSC Category List",
        description: "<div>A tree of 201 categories based on the <a target='_blank' rel='noopener noreferrer' href='https://www.unspsc.org'>United Nations Standard Products and Services Code</a>. This list takes some of the most commonly used sections and classes from the UNSPSC list, using the sections as parent categories for the classes. This is a good option for organizations who already use UNSPSC classifications or who want a very detailed tree. See a sample section of the tree below.</div>" +
                "<div class='category-list'>" +
                "  <ul>" +
                "    <li>Paper Materials and Products" +
                "      <ul>" +
                "        <li>Paper materials</li>" +
                "        <li>Paper Products</li>" +
                "        <li>Industrial use papers</li>" +
                "      </ul>" +
                "    </li>" +
                "    <li>Office Equipment and Accessories and Supplies" +
                "      <ul>" +
                "        <li>Office machines and their supplies and accessories</li>" +
                "      </ul>" +
                "    </li>" +
                "  </ul>" +
                "</div>",
    ],
    whoCategories: [
            enabled: true,
            // TODO: add option to support 'classpath:'
            fileUrl: "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/WHO_categories.csv",
            rootCategoryName: "ROOT", // needs to match the category from file
            categoryNameColumnIndex: 0,
            parentCategoryNameColumnIndex: 1,
            title: "WHO Category List",
            description: "<div>A system of medical categorization used by the <a target='_blank' rel='noopener noreferrer' href='https://www.who.int/groups/expert-committee-on-selection-and-use-of-essential-medicines/essential-medicines-lists'>WHO in their Essential Medicines List</a>. This categorization system is focused entirely on medication, and is best suited to healthcare organizations. Public health facilities that use the WHO list as the basis of their product catalogue will find that this is a good starting point to which medical items and other categories can be added. Users that want to import the WHO Essential Medicines List as their product list must select this category tree. See a sample of the tree below.</div>" +
                    "<div class='category-list'>" +
                    "  <ul>" +
                    "    <li>Antileprosy Medicines</li>" +
                    "    <li>Antimalarial Medicines" +
                    "      <ul>" +
                    "        <li>For Chemoprevention</li>" +
                    "        <li>For Curative Treatment</li>" +
                    "        <li>For Treatment of Acute Attack</li>" +
                    "      </ul>" +
                    "    </li>" +
                    "    <li>Antimigraine Medicines</li>" +
                    "  </ul>" +
                    "</div>",
    ]
]

openboxes.configurationWizard.productOptions = [
        whoProducts: [
                enabled: true,
                // TODO: add option to support 'classpath:'
                fileUrl: "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/WHO_products.csv",
                title: "WHO product list",
                description: "<div>This selection will import the WHO Essential Medicines List (Sept 2021 version) into your instance as products.  Public health facilities that use the WHO list as the basis of their product catalogue will find that this is a good starting point to building out their products in OpenBoxes.</div>" +
                        "<div class='my-3'>In order to import this product list, you must have selected the corresponding WHO category tree in the previous step. This product list will not work with any other category tree. Go to <a target='_blank' rel='noopener noreferrer' href='https://list.essentialmeds.org/'>list.essentialmeds.org</a> to view the full WHO list that will be imported.</div>",
        ]
]

openboxes.configurationWizard.listOfDemoData = [
    title: "Summary of data to be loaded",
    description: "<ul>" +
                 "  <li>57 products across 21 categories</li>" +
                 "  <li>18 locations including 3 depots, 5 suppliers, and 10 dispensaries</li>" +
                 "  <li>Inventory for 3 depots</li>" +
                 "  <li>12 sample people and users</li>" +
                 "  <li>2 sample stock lists</li>" +
                 "<ul>",
]

openboxes {
    configurationWizard {
        dataInit {
            locations {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/locations.csv"
            }
            locationGroups {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/locationGroups.csv"
            }
            organizations {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/organizations.csv"
            }
            binLocations {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/binLocations.csv"
            }
            categories {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/default_OB_categories.csv"
            }
            products {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/products.csv"
            }
            productCatalog {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/productCatalog.csv"
            }
            productCatalogItems {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/productCatalogItems.csv"
            }
            productSuppliers {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/productSuppliers.csv"
            }
            mainWarehouseInventory {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/mainWarehouseInventory.csv"
                warehouseName = "Main Warehouse"
            }
            bostonWarehouseInventory {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/bostonWarehouseInventory.csv"
                warehouseName = "Boston Warehouse"
            }
            chicagoWarehouseInventory {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/chicagoWarehouseInventory.csv"
                warehouseName = "Chicago Warehouse"
            }
            inventoryLevels {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/inventoryLevels.csv"
                warehouseName = "Main Warehouse"
            }
            users {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/users.csv"
            }
            persons {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/persons.csv"
            }
            chicagoStocklist {
                enabled = true
                templateUrl = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/chicagoStocklistTemplate.csv"
                itemsUrl = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/chicagoStocklistItems.csv"
            }
            bostonStocklist {
                enabled = true
                templateUrl = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/bostonStocklistTemplate.csv"
                itemsUrl = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/bostonStocklistItems.csv"
            }
        }
    }
}


// Pagination
openboxes.api.pagination.enabled = true
openboxes.api.pagination.pageSize = 10

// Accounting (Budget Code, GL Account)
openboxes.accounting.enabled = true

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
    expirationDateConstraint(nullable: true, min: openboxes.expirationDate.minValue)
}

// Order number prefix for bin replenishment case
openboxes.stockTransfer.binReplenishment.prefix = Constants.REPLENISHMENT_PREFIX
