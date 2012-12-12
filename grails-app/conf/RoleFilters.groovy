/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 

import org.pih.warehouse.core.User
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.UserService


class RoleFilters {
  def userService
  def dependsOn = [SecurityFilters]
    def static changeActions = ['edit', 'delete', 'create', 'add', 'process','save',
    'update','importData', 'receive','showRecordInventory','withdraw', 'cancel', 'change','toggle']
    def static changeControllers = ['createProductFromTemplate']
    def static adminControllers=['createProduct', 'createProductFromTemplate', 'admin']
    def static adminActions = ['product':['create'], 'person': ['list'], 'user':['list'], 'location':['edit'], 'shipper':['create'], 'locationGroup':['create'],'locationType':['create']]
    def filters = {
        readonlyCheck(controller:'*', action:'*') {
            before = { 
                if(SecurityFilters.actionsWithAuthUserNotRequired.contains(actionName)  || actionName == "chooseLocation") return true
                def missBrowser = !userService.canUserBrowse(session.user) 
                def missManager = needManager(controllerName, actionName) && !userService.isUserManager(session.user)
                def missAdmin = needAdmin(controllerName, actionName) && !userService.isUserAdmin(session.user)
                if(missBrowser || missManager || missAdmin){
                   response.sendError(401)
                  return false
                }
                return true
            }
        }
    }

    def static Boolean needAdmin(controllerName, actionName){
       adminControllers.contains(controllerName) || adminActions[controllerName]?.contains(actionName)
    }

     def static Boolean needManager(controllerName, actionName){
       changeActions.any{ actionName.startsWith(it)} || controllerName.contains("Workflow") || changeControllers.contains(controllerName)
    }


   }
