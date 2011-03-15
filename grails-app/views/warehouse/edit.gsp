
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
        
        	<div class="nav">
				<g:render template="nav"/>        	
        	</div>
        
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${warehouseInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${warehouseInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form method="post" >
            	<fieldset>
            		
	                <g:hiddenField name="id" value="${warehouseInstance?.id}" />
	                <g:hiddenField name="version" value="${warehouseInstance?.version}" />
	                <div class="dialog">
	                    <table>
	                    	<thead>
								<tr>
				        			<td valign="top" colspan="2">
										<g:render template="summary"/>			            			
									</td>            
		                    	</tr>	                    	
	                    	</thead>
	                        <tbody>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="name"><g:message code="warehouse.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${warehouseInstance?.name}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="manager"><g:message code="warehouse.manager.label" default="Manager" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'manager', 'errors')}">
	                                    <g:select name="manager.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${warehouseInstance?.manager?.id}"  />
	                                </td>
	                            </tr>
	                            
	                            <!--  
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="parentLocation"><g:message code="warehouse.parentWarehouse.label" default="Parent Location" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'parentLocation', 'errors')}">
										<g:select name="parentLocation.id" from="${org.pih.warehouse.core.Location.list()}" 
											optionKey="id" optionValue="name" value="" noSelection="['null': '']" />							
	                                </td>
	                            </tr>
	                            -->
	                            
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="manager"><g:message code="warehouse.manager.label" default="Managed Locally" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'local', 'errors')}">
	                                    <g:checkBox name="local" value="${warehouseInstance?.local}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="manager"><g:message code="warehouse.manager.label" default="Active" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'local', 'errors')}">
	                                    <g:checkBox name="active" value="${warehouseInstance?.active}" />
	                                </td>
	                            </tr>
	                            <tr>
	                            	<td valign="top"></td>
	                            	<td valign="top">
						                    <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.save.label', default: 'Save')}" /></span>
											&nbsp;
											<g:actionSubmit class="cancel" action="list" value="${message(code: 'default.button.cancel.label', default: 'Cancel')}"/>
	                            	</td>
	                            </tr>
	                        </tbody>
	                    </table>
	                </div>
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
