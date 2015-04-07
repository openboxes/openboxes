
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

				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="name"><warehouse:message code="eventType.name.label" default="Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'name', 'errors')}">
									<g:textField name="name" value="${eventTypeInstance?.name}" class="text" size="100"/>
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="description"><warehouse:message code="eventType.description.label" default="Description" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'description', 'errors')}">
									<g:textField name="description" value="${eventTypeInstance?.description}" class="text" size="100" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="sortOrder"><warehouse:message code="eventType.sortOrder.label" default="Sort Order" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'sortOrder', 'errors')}">
									<g:textField name="sortOrder" value="${fieldValue(bean: eventTypeInstance, field: 'sortOrder')}" class="text" size="100" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="eventCode"><warehouse:message code="eventType.eventCode.label" default="Event Status" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: eventTypeInstance, field: 'eventCode', 'errors')}">
									<g:select name="eventCode" from="${org.pih.warehouse.core.EventCode?.values()}" value="${eventTypeInstance?.eventCode}" noSelection="['': '']" class="chzn-select-deselect"/>
								</td>
							</tr>




						</tbody>
						<tfoot>

							<tr class="prop">
								<td valign="top" colspan="2">
									<div class="buttons">
									   <g:submitButton name="create" class="button icon approve" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />

									   <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>

									</div>
								</td>
							</tr>

						</tfoot>
					</table>
				</div>

            </g:form>
        </div>
    </body>
</html>
