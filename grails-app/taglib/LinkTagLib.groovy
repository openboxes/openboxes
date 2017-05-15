import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

class LinkTagLib extends ApplicationTagLib {

    static namespace = "g"
    def userService

    def link = { attrs, body ->

        def actionName = attrs.action
        def controllerName = attrs.controller ?: ""
        if (!SecurityFilters.actionsWithAuthUserNotRequired.contains(actionName)) {
            def missManager = RoleFilters.needManager(controllerName, actionName) && !userService.isUserManager(session.user)
            def missAdmin = RoleFilters.needAdmin(controllerName, actionName) && !userService.isUserAdmin(session.user)
            if (missManager || missAdmin) {
                out << body()
                return;
            }
        }

        def applicationTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        applicationTagLib.link.call(attrs, body)
    }
}
