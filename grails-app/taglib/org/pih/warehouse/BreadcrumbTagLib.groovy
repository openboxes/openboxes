package org.pih.warehouse

import org.junit.runner.Request;

class BreadcrumbTagLib {

	/**
	 * def get_bread_crumb(url)
		  begin
		    breadcrumb = ''
		    sofar = '/'
		    elements = url.split('/')
		    for i in 1...elements.size
		      sofar += elements[i] + '/'
		      if i%2 == 0
		        begin
		          breadcrumb += "<a href='#{sofar}'>"  + eval("#{elements[i - 1].singularize.camelize}.find(#{elements[i]}).name").to_s + '</a>'
		        rescue
		          breadcrumb += elements[i]
		        end
		      else
		        breadcrumb += "<a href='#{sofar}'>#{elements[i].pluralize}</a>"
		      end
		      breadcrumb += ' -> ' if i != elements.size - 1
		    end
		    breadcrumb
		  rescue
		    'Not available'
		  end
		end
	 */
	
	def breadcrumb = { attrs, body -> 
	
		def breadcrumb = "<a class=\"home\" href=\"${createLink(uri: '/home/index')}\">Home</a>";
		
		if (session.user) { 
			breadcrumb += "&nbsp; &raquo; &nbsp;";
			breadcrumb += "<a class=\"building\" href=\"${createLink(uri: '/warehouse/show/' + session.warehouse?.id)}\">${session.warehouse?.name}</a>"			
			def baseUrl = "/";
			def currentUrl = baseUrl;
			request.getServletPath().split("/").each {
				if (it != '') { 
					currentUrl += it;
					breadcrumb += "&nbsp; &raquo; &nbsp;";
					// TODO figure out the correct URL to match with each part of the servlet path 
					// It's a bit tricky so we're just going to display the breadcrumb i18n message
					breadcrumb += message(code: "breadcrumb." + it + ".label");	
				}
			}

		
		}
		else {
			breadcrumb += "&nbsp; &raquo; &nbsp;";
			breadcrumb += "(unknown warehouse)"			
		}
		// "http://" + request.getServerName() + ":" + request.getServerPort() + 
		out << "" + breadcrumb;
			
			
			
	}
	
}
