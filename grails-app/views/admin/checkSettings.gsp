<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="admin.title" default="Settings" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="menuTitle"><warehouse:message code="admin.title" default="Settings" /></content>		
    </head>
    <body>        
		<div id="settings" role="main" class="yui-gb">
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">
					<h2><warehouse:message code="admin.generalSettings.header"/></h2>
					<table>
						<tr class="prop">
							<td class="name">
								<label><warehouse:message code="admin.externalConfigFile.label"/></label>
							</td>
							<td>
								${grailsApplication.config.grails.config.locations }
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label><warehouse:message code="admin.environment.label"/></label>
							</td>
							<td>
								${env }
							</td>
						</tr>
					</table>
					
					<h2><warehouse:message code="admin.emailSettings.header"/></h2>
					<table>
						<tr class="prop">
							<td class="name">
								<label><warehouse:message code="admin.emailEnabled.label"/></label>
							</td>
							<td>
								${enabled }
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label><warehouse:message code="admin.hostname.label"/> </label>
							</td>
							<td>
								${host }
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label><warehouse:message code="admin.port.label"/> </label>
							</td>
							<td>
								${port}
							</td>
						</tr>						
					</table>
				
				
			</div>	
		</div>
    </body>
</html>
