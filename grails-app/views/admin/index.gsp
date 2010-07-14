<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><g:message code="default.controllers.label" default="Controllers" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="menuTitle"><g:message code="default.controllers.label" default="Controllers" /></content>		
		<content tag="pageTitle"><g:message code="default.controllers.label" default="Controllers" /></content>
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global"/></content>
		<content tag="localLinks"><g:render template="local"/></content>
		<content tag="breadcrumb"><g:render template="breadcrumb" model=""/></content>

    </head>
    <body>        
		<div id="settings" role="main" class="yui-gb">
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">
				<ul>
					<g:each var="c" in="${grailsApplication.controllerClasses}">
						<g:set var="controllerName"><%= c.getName().toLowerCase() %></g:set>
						<span class="menuButton">							
							<li class="controller">
								<a class="${controllerName}" href="${createLink(uri: '/' + controllerName)}"">${controllerName}</a>
							</li>
						</span>
					</g:each>
				</ul>									  
			</div>	
		</div>
    </body>
</html>
