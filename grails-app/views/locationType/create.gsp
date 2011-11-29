
<%@ page import="org.pih.warehouse.core.LocationType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'locationType.label', default: 'LocationType')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationTypeInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationTypeInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
            	<fieldset>
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="name"><warehouse:message code="locationType.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationTypeInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${locationTypeInstance?.name}" />
	                                </td>
	                            </tr>
	                        	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="description"><warehouse:message code="locationType.description.label" default="Description" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationTypeInstance, field: 'description', 'errors')}">
	                                    <g:textField name="description" value="${locationTypeInstance?.description}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
										<label for="name"><warehouse:message code="location.supportedActivities.label" /></label>
	                                </td>
	                                <td valign="top" class="value">
	                                	<g:set var="activityList" value="${org.pih.warehouse.core.ActivityCode.list() }"/>
	                                	<g:select name="supportedActivities" multiple="true" from="${activityList }" size="${activityList.size() }" style="width: 150px" 
	                                		optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${warehouseInstance?.supportedActivities}" />
	                                </td>
	                            </tr>	                            
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="sortOrder"><warehouse:message code="locationType.sortOrder.label" default="Sort Order" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: locationTypeInstance, field: 'sortOrder', 'errors')}">
	                                    <g:textField name="sortOrder" value="${fieldValue(bean: locationTypeInstance, field: 'sortOrder')}" />
	                                </td>
	                            </tr>
	                        
	                        
		                        <tr class="prop">
		                        	<td valign="top"></td>
		                        	<td valign="top">
						                <div class="buttons">
						                   <g:submitButton name="create" class="save" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />
						                   
						                   <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
						                   
						                </div>                        	
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
