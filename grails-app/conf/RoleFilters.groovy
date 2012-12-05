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
    def static changeActions = ['edit', 'delete', 'create', 'add', 'process','save', 'update', 'editTransaction','importData', 'showRecordInventory', 'saveRecordInventory']
    def static changeControllers = ['createProductFromTemplate', 'createProduct']
    def filters = {
        readonlyCheck(controller:'*', action:'*') {
            before = { 
                if(SecurityFilters.actionsWithAuthUserNotRequired.contains(actionName)) return true
                if(!userService.canUserBrowse(session.user)){
                  response.sendError(401)
                  return false
                }
                def willChange = changeActions.contains(actionName) || controllerName.contains("Workflow") || changeControllers.contains(controllerName)
                if(willChange && !userService.isUserManager(session.user)){
                  response.sendError(401)
                  return false
                 }
                return true
            }
        }
    }
}
