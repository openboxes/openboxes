
<%@ page import="org.pih.warehouse.inventory.Warehouse" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'warehouse.label', default: 'Warehouse')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.create.label" args="[entityName]" /></content>
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
            <g:form action="save" method="post" >
                <div class="dialog">
					<fieldset>

	                    <table>
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
	                                  <label for="locationType.id"><g:message code="warehouse.locationType.label" default="Location Type" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'locationType', 'errors')}">
	                                    <g:select name="locationType.id" from="${org.pih.warehouse.core.LocationType.list()}" optionKey="id" value="${warehouseInstance?.locationType?.id}"  />
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

	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="address"><g:message code="warehouse.address.label" default="Address" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'address.address', 'errors')}">
	                                    <g:textField name="address" value="${warehouseInstance?.address?.address}" /><br/>
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="address2"><g:message code="warehouse.address2.label" default="Address 2" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'address.address2', 'errors')}">
	                                    <g:textField name="address2" value="${warehouseInstance?.address?.address2}" /><br/>
	                                </td>
	                            </tr>

	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="city"><g:message code="warehouse.city.label" default="City" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'address.city', 'errors')}">
	                                    <g:textField name="city" value="${warehouseInstance?.address?.city}" />
	                                </td>
	                            </tr>
	
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="stateOrProvince"><g:message code="warehouse.stateOrProvince.label" default="State/Province" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'address.stateOrProvince', 'errors')}">
	                                    <g:textField name="stateOrProvince" value="${warehouseInstance?.address?.stateOrProvince}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="country"><g:message code="warehouse.country.label" default="Country" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'address.country', 'errors')}">
	                                    <g:textField name="country" value="${warehouseInstance?.address?.country}" />
	                                </td>
	                            </tr>
	                            
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="postalCode"><g:message code="warehouse.postalCode.label" default="Postal Code" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: warehouseInstance, field: 'address.postalCode', 'errors')}">
	                                    <g:textField name="postalCode" value="${warehouseInstance?.address?.postalCode}" size="5" /><br/>
	                                </td>
	                            </tr>
	                            
	                        </tbody>
	                    </table>
		                <div class="buttonBar">
		                    <g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" />
		                </div>
                    
                    </fieldset>
                    
                </div>
            </g:form>
        </div>
    </body>
</html>
