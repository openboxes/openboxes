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
      if(!SecurityFilters.actionsWithAuthUserNotRequired.contains(actionName)){  
        if(RoleFilters.changeActions.contains(actionName) && !userService.isUserInRole(session.user.id, [RoleType.ROLE_MANAGER, RoleType.ROLE_ADMIN])){
          return 
         }
      }

      def applicationTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
      
      applicationTagLib.link.call(attrs, body)
      
    }
}
