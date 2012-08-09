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
				<div class="box">
					<ul>							
						<g:each in="${actionNames }" var="action">
							<li>${action }</li>
						</g:each>
					</ul>
				</div>
		</div>
    </body>
</html>
