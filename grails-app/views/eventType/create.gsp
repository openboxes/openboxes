
<%@ page import="org.pih.warehouse.core.EventType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'eventType.label', default: 'EventType')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.create.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${eventTypeInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${eventTypeInstance}" as="list" />
	            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
            	<fieldset>
	                <div class="dialog">
	                    <table>
	                        <tbody>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="name"><warehouse:message code="eventType.name.label" default="Name" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'name', 'errors')}">
	                                    <g:textField name="name" value="${eventTypeInstance?.name}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="description"><warehouse:message code="eventType.description.label" default="Description" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'description', 'errors')}">
	                                    <g:textField name="description" value="${eventTypeInstance?.description}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="sortOrder"><warehouse:message code="eventType.sortOrder.label" default="Sort Order" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'sortOrder', 'errors')}">
	                                    <g:textField name="sortOrder" value="${fieldValue(bean: eventTypeInstance, field: 'sortOrder')}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="eventCode"><warehouse:message code="eventType.eventCode.label" default="Event Status" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'eventCode', 'errors')}">
	                                    <g:select name="eventCode" from="${org.pih.warehouse.core.EventCode?.values()}" value="${eventTypeInstance?.eventCode}" noSelection="['': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="activityType"><warehouse:message code="eventType.activityType.label" default="Activity Type" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'activityType', 'errors')}">
	                                    <g:select name="activityType" from="${org.pih.warehouse.core.ActivityType?.values()}" value="${eventTypeInstance?.activityType}" noSelection="['': '']" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="eventType"><warehouse:message code="eventType.eventType.label" default="Event Type" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'eventType', 'errors')}">
	                                    <g:select name="eventType" from="${org.pih.warehouse.core.EventTypes?.values()}" value="${eventTypeInstance?.eventType}"  />
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
