<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><g:message code="default.controllers.label" default="Settings" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="menuTitle"><g:message code="default.controllers.label" default="Check Email" /></content>		
    </head>
    <body>        
		<div id="settings" role="main" class="yui-gb">
			<!-- the first child of a Grid needs the "first" class -->
			<div class="yui-u first">
					<h2>General Settings</h2>
					<table>
						<tr class="prop">
							<td class="name">
								<label>External config file</label>
							</td>
							<td>
								${grailsApplication.config.grails.config.locations }
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label>Environment</label>
							</td>
							<td>
								${env }
							</td>
						</tr>
					</table>
					
					<h2>Email Settings</h2>
					<table>
						<tr class="prop">
							<td class="name">
								<label>Email enabled</label>
							</td>
							<td>
								${enabled }
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label>Hostname </label>
							</td>
							<td>
								${host }
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label>Port </label>
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
