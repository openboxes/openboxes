import org.codehaus.groovy.grails.plugins.web.taglib.*
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Role;
import org.pih.warehouse.core.User;
import org.pih.warehouse.core.RoleType;


class LinkTagLib extends ApplicationTagLib {

    static namespace = "g"
    def userService

    def link = { attrs, body ->

      def actionName = attrs.action
      def controllerName = attrs.controller ?: ""
      if(!SecurityFilters.actionsWithAuthUserNotRequired.contains(actionName)){  
        def missManager = RoleFilters.needManager(controllerName, actionName) && !userService.isUserManager(session.user)
        def missAdmin = RoleFilters.needAdmin(controllerName, actionName) && !userService.isUserAdmin(session.user)
        if(missManager || missAdmin) return 
      }

      def applicationTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
      
      applicationTagLib.link.call(attrs, body)
      
    }
}
