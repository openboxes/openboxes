<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page import="org.pih.warehouse.core.Location" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'warehouse.label', default: 'Location')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
        
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationGroupInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationGroupInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<table>
				<tbody>
					<tr class="prop">
						<td class="name">
							${warehouse.message(code: 'locationGroup.name.label') }
						</td>
						<td class="value">
							${locationGroupInstance?.name }
						</td>
					</tr>
					<tr class="prop">
						<td class="name">
							${warehouse.message(code: 'locationGroup.locations.label') }
						</td>
						<td class="value">
							<table>
								<g:each in="${locationGroupInstance?.locations }" var="location">
									<tr>
										<td>									
											${location?.name }
										</td>
										<td>
											<g:link controller="stocklist" action="show" id="${location?.id }">
												${warehouse.message(code: 'stocklist.manage.label', default: 'Manage stock list')} 
											</g:link>
										</td>
									</tr>
								</g:each>
							</table>
						</td>
					</tr>
				</tbody>
			</table>
                        
<script type="text/javascript">

	        $(document).ready(function() {
			
	        });
</script>        


    </body>
</html>
