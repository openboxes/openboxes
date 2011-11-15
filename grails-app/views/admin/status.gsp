<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="default.appstatus.label" default="App Status" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.appstatus.label" default="App Status" /></content>
		<content tag="menuTitle"><warehouse:message code="default.appstatus.label" default="App Status" /></content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global"/></content>
		<content tag="localLinks"><g:render template="local"/></content>
    </head>
    <body>        
		<div id="settings" role="main" class="yui-gb">
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">
				<h1>Application Status</h1>
				<ul>
					<li>App version: <g:meta name="app.version"></g:meta></li>
					<li>Grails version: <g:meta name="app.grails.version"></g:meta></li>
					<li>JVM version: ${System.getProperty('java.version')}</li>
					<li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
					<li>Domains: ${grailsApplication.domainClasses.size()}</li>
					<li>Services: ${grailsApplication.serviceClasses.size()}</li>
					<li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
				</ul>
			</div>				
			<div class="yui-u">
				<h1>Installed Plugins</h1>
				<ul>
					<g:set var="pluginManager" value="${applicationContext.getBean('pluginManager')}"></g:set>
					<g:each var="plugin" in="${pluginManager.allPlugins}">
						<li>${plugin.name} - ${plugin.version}</li>
					</g:each>
				</ul>
			</div>
			<div class="yui-u">
				<h1>Available Controllers</h1>
				<ul>
					<g:each var="c" in="${grailsApplication.controllerClasses}">
					<li class="controller"><g:link controller="${c.logicalPropertyName}">${c.name}</g:link></li>
					</g:each>
				</ul>									  
			</div>	
		</div>
    </body>
</html>
