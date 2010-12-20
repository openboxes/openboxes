<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /></content>
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
	                            <td valign="top" class="name"><g:message code="warehouse.id.label" default="Id" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "id")}</td>                            
	                        </tr>                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="warehouse.name.label" default="Name" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "name")}</td>                            
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="warehouse.locationType.label" default="Location Type" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "locationType.name")}</td>                            
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="warehouse.parentLocation.label" default="Parent Location" /></td>
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "parentLocation.name")}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="warehouse.city.label" default="City" /></td>
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "address.city")}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="warehouse.country.label" default="Country" /></td>
	                            <td valign="top" class="value">${fieldValue(bean: warehouseInstance, field: "address.country")}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="warehouse.manager.label" default="Manager" /></td>
	                            <td valign="top" class="value">
									<g:link controller="user" action="show" id="${warehouseInstance?.manager?.id}">
										${warehouseInstance?.manager?.firstName?.encodeAsHTML()}
										${warehouseInstance?.manager?.lastName?.encodeAsHTML()}
									</g:link>
								</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="warehouse.inventory.label" default="Inventory" /></td>
	                            <td valign="top" class="value">
				      				<g:link controller="inventory" action="browse" id="${warehouseInstance?.inventory?.id}">
				      				Browse Inventory
				      				</g:link>
							    </td>
	                        </tr>
	                        
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="user.photo.label" default="Profile Photo" /></td>                            
	                            <td valign="top" class="value">		
	                            
	                            	<%--					
									<img class="photo" src="${createLink(controller:'warehouse', action:'viewLogo', id:warehouseInstance.id)}" />
									<br/>
									 --%>
									<g:form controller="warehouse" method="post" action="uploadLogo" enctype="multipart/form-data">
										<input type="hidden" name="id" value="${warehouseInstance.id}" />
										<input type="file" name="logo"/>
										<input class="positive" type="submit" value="Upload"/>
									</g:form>
									
								</td>                            
	                        </tr>
	                    </tbody>
					</table>
	            </div>
	    	</fieldset>
        </div>
    </body>
</html>
