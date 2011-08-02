<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
        	<div class="nav">
				<g:render template="nav"/>        	
        	</div>
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>

			<fieldset>            
	            <div class="dialog">
					<table>
	                    <tbody>                    
	                    	<tr>
			        			<td valign="top" colspan="2">
									<g:render template="summary"/>			            			
								</td>            
	                    	</tr>                   
	                        <tr class="prop">
	                            <td valign="top" class="name"><label for="name"><warehouse:message code="warehouse.name.label" default="Name" /></label></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "name")}</td>                            
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><label for="manager"><warehouse:message code="warehouse.manager.label" default="Manager" /></label></td>
	                            <td valign="top" class="value">
									<g:link controller="user" action="show" id="${warehouseInstance?.manager?.id}">
										${warehouseInstance?.manager?.firstName?.encodeAsHTML()}
										${warehouseInstance?.manager?.lastName?.encodeAsHTML()}
									</g:link>
								</td>
	                        </tr>
	                        <!-- 
	                        <tr class="prop">
	                            <td valign="top" class="name"><label for="parentLocation"><warehouse:message code="warehouse.parentLocation.label" default="Parent Location" /></label></td>
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "parentLocation.name")}</td>
	                        </tr>
	                        -->
	                         <tr class="prop">
	                            <td valign="top" class="name"><label for="managedLocally"><warehouse:message code="warehouse.managedLocally.label" default="Managed Locally" /></label></td>
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "local") ? 'yes' : 'no'}</td>
	                        </tr>
	                         <tr class="prop">
	                            <td valign="top" class="name"><label for="active"><warehouse:message code="warehouse.active.label" default="Active" /></label></td>
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "active") ? 'yes' : 'no'}</td>
	                        </tr>
	                    </tbody>
					</table>
	            </div>
	    	</fieldset>
        </div>
    </body>
</html>
