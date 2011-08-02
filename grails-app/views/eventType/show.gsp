
<%@ page import="org.pih.warehouse.core.EventType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'eventType.label', default: 'EventType')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: eventTypeInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: eventTypeInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.description.label" default="Description" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: eventTypeInstance, field: "description")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.sortOrder.label" default="Sort Order" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: eventTypeInstance, field: "sortOrder")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.eventCode.label" default="Event Status" /></td>
                            
                            <td valign="top" class="value">${eventTypeInstance?.eventCode?.encodeAsHTML()}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.activityType.label" default="Activity Type" /></td>
                            
                            <td valign="top" class="value">${eventTypeInstance?.activityType?.encodeAsHTML()}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.dateCreated.label" default="Date Created" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${eventTypeInstance?.dateCreated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.lastUpdated.label" default="Last Updated" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${eventTypeInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.status.label" default="Status" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: eventTypeInstance, field: "status")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.optionValue.label" default="Option Value" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: eventTypeInstance, field: "optionValue")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="eventType.eventType.label" default="Event Type" /></td>
                            
                            <td valign="top" class="value">${eventTypeInstance?.eventType?.encodeAsHTML()}</td>
                            
                        </tr>
                    
                    
						<tr class="prop">
                        	<td valign="top"></td>
                        	<td valign="top">                         
					            <div class="buttons">
					                <g:form>
					                    <g:hiddenField name="id" value="${eventTypeInstance?.id}" />
					                    <g:actionSubmit class="edit" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
					                    <g:actionSubmit class="delete" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					                </g:form>
					            </div>
							</td>
						</tr>                    
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
