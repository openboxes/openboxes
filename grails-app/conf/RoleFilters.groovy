/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
class RoleFilters {
    def userService
    def dependsOn = [SecurityFilters]
    def static changeActions = ['delete', 'create', 'add', 'process', 'save',
                                'update', 'importData', 'receive', 'showRecordInventory', 'withdraw', 'cancel', 'change', 'toggle', 'exportAsCsv']
    def static changeControllers = ['createProductFromTemplate']

    def static adminControllers = ['createProduct', 'createProductFromTemplate', 'admin']
    def static adminActions = [
            'product'      : ['create'],
            'person'       : ['list'],
            'user'         : ['list'],
            'location'     : ['edit'],
            'shipper'      : ['create'],
            'locationGroup': ['create'],
            'locationType' : ['create'],
            '*'            : ['remove']
    ]

    def static superuserControllers = []
    def static superuserActions = [
            '*'               : ['delete'],
            'console'         : ['index', 'execute'],
            'inventory'       : ['createInboundTransfer', 'createOutboundTransfer', 'createConsumed', 'editTransaction', 'deleteTransaction', 'saveTransaction'],
            'inventoryItem'   : ['adjustStock', 'transferStock'],
            'productCatalog'  : ['create', 'importProductCatalog'],
            'transactionEntry': ['edit', 'delete', 'save', 'update'],
            'user'            : ['impersonate']
    ]

    def filters = {
        readonlyCheck(controller: '*', action: '*') {
            before = {

                // Anonymous
                if (SecurityFilters.actionsWithAuthUserNotRequired.contains(actionName) || actionName == "chooseLocation" ||
                        SecurityFilters.controllersWithAuthUserNotRequired.contains(controllerName)) {
                    return true
                }

                // Authorized users
                def missBrowser = !userService.canUserBrowse(session.user)
                def missManager = needManager(controllerName, actionName) && !userService.isUserManager(session.user)
                def missAdmin = needAdmin(controllerName, actionName) && !userService.isUserAdmin(session.user)
                def missSuperuser = needSuperuser(controllerName, actionName) && !userService.isSuperuser(session.user)

                if (missBrowser || missManager || missAdmin || missSuperuser) {
                    log.info("User ${session?.user?.username} does not have access to ${controllerName}/${actionName} in location ${session?.warehouse?.name}")
                    redirect(controller: "errors", action: "handleForbidden")
                    return false
                }
                return true
            }
        }
    }

    static Boolean needSuperuser(controllerName, actionName) {
        superuserControllers?.contains(controllerName) || superuserActions[controllerName]?.contains(actionName) || superuserActions['*'].any {
            actionName?.startsWith(it)
        }
    }

    static Boolean needAdmin(controllerName, actionName) {
        adminControllers?.contains(controllerName) || adminActions[controllerName]?.contains(actionName) || adminActions['*'].any {
            actionName?.startsWith(it)
        }
    }

    static Boolean needManager(controllerName, actionName) {
        changeActions.any {
            actionName?.startsWith(it)
        } || controllerName?.contains("Workflow") || changeControllers?.contains(controllerName)
    }


}
