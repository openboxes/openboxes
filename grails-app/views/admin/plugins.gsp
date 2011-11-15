<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="default.plugins.label" default="Installed Plug-ins" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.plugins.label" default="Installed Plug-ins" /></content>
    </head>
    <body>        
		<div id="settings" role="main" class="yui-gb">
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">
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
