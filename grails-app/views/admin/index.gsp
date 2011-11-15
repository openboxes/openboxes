<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="default.controllers.label" default="Controllers" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="menuTitle"><warehouse:message code="default.controllers.label" default="Controllers" /></content>		
    </head>
    <body>        
		<div id="settings" role="main" class="yui-gb">
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">
				<table>
					<tr>
						<g:set var="status" value="${1 }"/>
						<g:each var="c" in="${grailsApplication.controllerClasses}">
							<td>
								<g:set var="controllerName"><%= c.getName().toLowerCase() %></g:set>
								<span class="menuButton">						
									<li class="controller">
										<a class="${c.name}" href="${createLink(uri: '/' + c.name)}"">${controllerName}</a>
									</li>
								</span>
							</td>
							<g:if test="${status++ % 6 == 0}"></tr><tr></g:if>
						</g:each>
					</tr>
				</table>								  
			</div>	
		</div>
    </body>
</html>
