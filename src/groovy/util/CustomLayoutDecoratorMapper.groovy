package util

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.web.sitemesh.GrailsLayoutDecoratorMapper
import com.opensymphony.module.sitemesh.*
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethods

class CustomLayoutDecoratorMapper extends GrailsLayoutDecoratorMapper {

	public Decorator getDecorator(HttpServletRequest request, Page page) {
		def layoutName = (request.session.layout) ?: request.getParameter("layout")		
		Decorator decorator = getNamedDecorator(request, layoutName)
		if (!decorator) { 
			decorator = super.getDecorator(request, page)
			if (decorator == null) {
				decorator = getNamedDecorator(request, "custom")
			}
		}
		return decorator
	}
}