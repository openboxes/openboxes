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
		<div id="settings" role="main" class="yui-gf">
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">

			</div>
			<div class="yui-u">
				<table class="box">
					<tr>
						<g:set var="status" value="${1 }"/>
						<g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
							<td>
								<g:set var="controllerName"><%= c.getName().toLowerCase() %></g:set>
								<span class="linkButton">						
									<li class="controller">
										<a class="${c.name}" href="${createLink(uri: '/' + c.logicalPropertyName)}">${c.fullName}</a>
									</li>
								</span>

							</td>
							<g:if test="${status++ % 2 == 0}"></tr><tr></g:if>
						</g:each>
					</tr>
					<tr>
						<td></td>
					</tr>
				</table>								  
			</div>	
		</div>
    </body>
</html>
