package org.pih.warehouse

import org.pih.warehouse.core.RoleType

/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
class RoleInterceptor {
    def userService

    // this interceptor depends on SecurityInterceptor
    int order = LOWEST_PRECEDENCE
    def static changeActions = [
            'delete',
            'create',
            'add',
            'process',
            'save',
            'update',
            'importData',
            'receive',
            'showRecordInventory',
            'withdraw',
            'cancel',
            'change',
            'toggle',
            'exportAsCsv',
            'importOutboundStockMovement'
    ]

    def static changeControllers = ['createProductFromTemplate']

    def static managerActions = [
        'inventory'           : ['createOutboundTransfer'],
        'stockMovementItemApi': ['eraseItem']
    ]

    def static adminControllers = ['createProduct', 'createProductFromTemplate', 'admin']
    def static adminActions = [
        'product'      : ['create'],
        'person'       : ['list'],
        'user'         : ['list'],
        'location'     : ['edit'],
        'shipper'      : ['create'],
        'locationGroup': ['create'],
        'locationType' : ['create']
    ]

    def static superuserControllers = []
    def static superuserActions = [
        'console'                   : ['index', 'execute'],
        'inventory'                 : ['createInboundTransfer', 'createConsumed', 'editTransaction', 'deleteTransaction', 'saveTransaction'],
        'inventoryItem'             : ['adjustStock', 'transferStock'],
        'productCatalog'            : ['create', 'importProductCatalog'],
        'productType'               : ['edit', 'delete', 'save', 'update'],
        'transactionEntry'          : ['edit', 'delete', 'save', 'update'],
        'user'                      : ['impersonate'],
        'productsConfigurationApi'  : ['downloadCategories', 'importCategories'],
        'quartz'                    : ['*'],
        'jobs'                      : ['*']
    ]

    def static invoiceActions = [
        'invoice': ['*']
    ]

    def static requestorOrManagerActions = [
        'api'                 : ['getAppContext', 'getRequestTypes', 'getMenuConfig'],
        'dashboard'           : ['megamenu'],
        'grails'              : ['errors'],
        'localizationApi'     : ['list'],
        'locationApi'         : ['list'],
        'productApi'          : ['list', 'productDemand', 'productAvailabilityAndDemand'],
        'stocklistApi'        : ['list'],
        'stockMovement'       : ['list', 'createRequest'],
        'stockMovementApi'    : ['updateItems', 'create', 'updateStatus', 'read'],
        'stockMovementItemApi': ['getStockMovementItems']
    ]

    def static authenticatedActions = [
        'api'                 : ['getAppContext', 'getRequestTypes', 'getMenuConfig'],
        'dashboard'           : ['megamenu'],
        'grails'              : ['errors'],
        'localizationApi'     : ['list'],
        'locationApi'         : ['list'],
        'productApi'          : ['list', 'productDemand', 'productAvailabilityAndDemand'],
        'stocklistApi'        : ['list'],
        'stockMovement'       : ['list'],
    ]

    public RoleInterceptor() {
        matchAll().except(uri: '/static/**').except(controller: "errors").except(uri: "/info").except(uri: "/health")
    }

    boolean before() {

        def rules = grailsApplication.config.openboxes.security.rbac.rules
        def rule = rules.find { it.controller == controllerName && it.actions.contains(actionName) ||
            it.controller == controllerName && it.actions.contains("*") ||
            it.controller == "*" && it.actions.contains("*")
        }

        if (!rule) {
            log.info "No rule for ${controllerName}:${actionName} -> allow anonymous"
        } else {
            log.info "Found rule matching controller ${controllerName}, action ${actionName}: " + rule
            def minimumRequiredRole = rule.accessRules?.minimumRequiredRole
            def supplementalRoles = rule.accessRules?.supplementalRoles ?: []

            Boolean isAnonymous = minimumRequiredRole == RoleType.ROLE_ANONYMOUS

            Boolean isMinimumRequiredRole = true
            if (session.user && minimumRequiredRole) {
                isMinimumRequiredRole = userService.isUserInRole(session.user, minimumRequiredRole)
            }

            Boolean isUserInRole = true
            if (session.user && supplementalRoles.size() > 0) {
                isUserInRole = userService.isUserInRole(session.user, supplementalRoles)
            }

            if (isAnonymous || (session.user && isMinimumRequiredRole && isUserInRole)) {
                log.info "User has access to ${controllerName}.${actionName}"
                return true
            }
            redirect(controller: "errors", action: "handleForbidden")
            return false
        }

        // Anonymous
        if (SecurityInterceptor.actionsWithAuthUserNotRequired.contains(actionName) || actionName == "chooseLocation" ||
                SecurityInterceptor.controllersWithAuthUserNotRequired.contains(controllerName)) {
            return true
        }

        // Authorized users
        def isNotAuthenticated = !userService.isUserInRole(session.user, RoleType.ROLE_AUTHENTICATED)
        def isNotBrowser = !userService.canUserBrowse(session.user) && !needRequestorOrManager(controllerName, actionName)
        def isNotManager = needManager(controllerName, actionName) && (needRequestorOrManager(controllerName, actionName) ? !userService.isUserManager(session.user) && !userService.isUserRequestor(session.user) : !userService.isUserManager(session.user))
        def isNotAdmin = needAdmin(controllerName, actionName) && !userService.isUserAdmin(session.user)
        def isNotSuperuser = needSuperuser(controllerName, actionName) && !userService.isSuperuser(session.user)
        def hasNoRoleInvoice = needInvoice(controllerName, actionName) && !userService.hasRoleInvoice(session.user)
        def isNotRequestor = needRequestorOrManager(controllerName, actionName) && !userService.isUserRequestor(session.user)
        def isNotRequestorOrManager = needRequestorOrManager(controllerName, actionName) ? !userService.isUserManager(session.user) && !userService.isUserRequestor(session.user) : false

        if (isNotAuthenticated || isNotBrowser || isNotManager || isNotAdmin || isNotSuperuser || hasNoRoleInvoice || (isNotRequestorOrManager && !userService.hasHighestRole(session.user, session?.warehouse?.id, RoleType.ROLE_AUTHENTICATED) && !needAuthenticatedActions(controllerName, actionName)) || (isNotRequestor && userService.hasHighestRole(session.user, session?.warehouse?.id, RoleType.ROLE_AUTHENTICATED) && !needAuthenticatedActions(controllerName, actionName))) {
            log.info("User ${session?.user?.username} does not have access to ${controllerName}/${actionName} in location ${session?.warehouse?.name}")
            redirect(controller: "errors", action: "handleForbidden")
            return false
        }
        return true
    }

    static Boolean needSuperuser(controllerName, actionName) {
        (superuserActions[controllerName]?.contains("*")
            || superuserControllers?.contains(controllerName)
            || superuserActions[controllerName]?.contains(actionName)
            || superuserActions['*'].any {actionName?.startsWith(it)})
    }

    static Boolean needAdmin(controllerName, actionName) {
        adminControllers?.contains(controllerName) || adminActions[controllerName]?.contains(actionName) || adminActions['*'].any {
            actionName?.startsWith(it)
        }
    }

    static Boolean needManager(controllerName, actionName) {
        def isChangeAction = changeActions.any {
            actionName?.startsWith(it)
        }
        def isWorkflow = controllerName?.contains("Workflow")
        def isChangeController = changeControllers?.contains(controllerName)
        def isManagerAction = managerActions[controllerName]?.contains(actionName)
        return isChangeAction || isWorkflow || isChangeController || isManagerAction
    }

    static Boolean needInvoice(controllerName, actionName) {
        invoiceActions[controllerName]?.contains("*") || invoiceActions[controllerName]?.contains(actionName)
    }

    static Boolean needRequestorOrManager(controllerName, actionName) {
        requestorOrManagerActions[controllerName]?.contains(actionName)
    }

    static Boolean needAuthenticatedActions(controllerName, actionName) {
        authenticatedActions[controllerName]?.contains(actionName)
    }


}
