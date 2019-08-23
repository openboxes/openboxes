
<%@ page import="org.pih.warehouse.reporting.Indicator" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'indicator.label', default: 'Indicator')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${indicatorInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${indicatorInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['indicator']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['indicator']"/></g:link>
			</div>

			<g:form method="post" >
				<g:hiddenField name="id" value="${indicatorInstance?.id}" />
				<g:hiddenField name="version" value="${indicatorInstance?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="name"><warehouse:message code="indicator.name.label" default="Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: indicatorInstance, field: 'name', 'errors')}">
									<g:textField class="text large" size="80" name="name" value="${indicatorInstance?.name}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="description"><warehouse:message code="indicator.description.label" default="Description" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: indicatorInstance, field: 'description', 'errors')}">
									<g:textField class="text large" size="80" name="description" value="${indicatorInstance?.description}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="expression"><warehouse:message code="indicator.expression.label" default="Expression" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: indicatorInstance, field: 'expression', 'errors')}">
									<g:textArea class="text" rows="30" name="expression" value="${indicatorInstance?.expression}" />
								</td>
							</tr>				

							<tr class="prop">
								<td valign="top"></td>
								<td valign="top left">
									<div class="buttons">
										<g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
										<g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
            </g:form>
        </div>
    </body>
</html>
