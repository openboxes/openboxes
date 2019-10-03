import org.grails.plugins.web.taglib.ApplicationTagLib
import util.ConfigHelper

class LinkTagLib extends ApplicationTagLib {

    static namespace = "g"
    def userService

    def link = { attrs, body ->

        boolean disabled = attrs.disabled ?: false
        String disabledMessage = attrs.disabledMessage ?: 'Access denied'
        def actionName = attrs.action
        def controllerName = attrs.controller ?: ""
        if (!SecurityFilters.actionsWithAuthUserNotRequired.contains(actionName)) {
            def rule = ConfigHelper.findAccessRule(controllerName as String, actionName as String)
            def missRule = rule ? !userService.isUserInRole(session.user, rule?.accessRules?.minimumRequiredRole) : false
            def missManager = RoleFilters.needManager(controllerName, actionName) && (RoleFilters.needRequestorOrManager(controllerName, actionName) ? !userService.isUserManager(session.user) && !userService.isUserRequestor(session.user) : !userService.isUserManager(session.user))
            def missAdmin = RoleFilters.needAdmin(controllerName, actionName) && !userService.isUserAdmin(session.user)
            def missSuperuser = RoleFilters.needSuperuser(controllerName, actionName) && !userService.isSuperuser(session.user)

            // If user is not authorized to access link we just display the link body (text)
            if (missManager || missAdmin || missSuperuser || disabled || missRule) {
                attrs.onclick = "alert('${disabledMessage}'); return false;"
            }
        }

        def applicationTagLib = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        applicationTagLib.link.call(attrs, body)
    }
}
