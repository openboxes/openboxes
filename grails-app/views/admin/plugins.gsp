<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><g:message code="default.admin.label" default="Admin Settings" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.plugins.label" default="Plug-ins" /></content>
		<content tag="menuTitle"><g:message code="default.plugins.label" default="Plug-ins" /></content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global"/></content>
		<content tag="localLinks"><g:render template="local"/></content>		
    </head>
    <body>        
		<div id="settings" role="main" class="yui-gb">
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">
				<h1>Installed Plugins</h1>
				<ul>
					<g:set var="pluginManager" value="${applicationContext.getBean('pluginManager')}"></g:set>
					<g:each var="plugin" in="${pluginManager.allPlugins}">
						<li>${plugin.name} - ${plugin.version}</li>
					</g:each>
				</ul>
			</div>				
		</div>
    </body>
</html>
