
<%@ page import="org.pih.warehouse.core.Organization" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'organization.label', default: 'Organization')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${organizationInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${organizationInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['organization']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['organization']"/></g:link>
			</div>

			<g:form method="post" >
				<g:hiddenField name="id" value="${organizationInstance?.id}" />
				<g:hiddenField name="version" value="${organizationInstance?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="partyType"><warehouse:message code="organization.partyType.label" default="Party Type" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'partyType', 'errors')}">
									<g:select class="chzn-select-deselect" name="partyType.id" from="${org.pih.warehouse.core.PartyType.list()}" optionKey="id" value="${organizationInstance?.partyType?.id}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="name"><warehouse:message code="organization.name.label" default="Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'name', 'errors')}">
									<g:textField class="text" size="80" name="name" maxlength="255" value="${organizationInstance?.name}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="description"><warehouse:message code="organization.description.label" default="Description" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'description', 'errors')}">
									<g:textArea class="text" name="description" maxlength="255" value="${organizationInstance?.description}" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
								  <label for="dateCreated"><warehouse:message code="organization.dateCreated.label" default="Date Created" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'dateCreated', 'errors')}">
									<g:datePicker name="dateCreated" precision="minute" value="${organizationInstance?.dateCreated}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="lastUpdated"><warehouse:message code="organization.lastUpdated.label" default="Last Updated" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'lastUpdated', 'errors')}">
									<g:datePicker name="lastUpdated" precision="minute" value="${organizationInstance?.lastUpdated}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="roles"><warehouse:message code="organization.roles.label" default="Roles" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'roles', 'errors')}">
									
									<ul>
									<g:each in="${organizationInstance?.roles?}" var="r">
										<li><g:link controller="partyRole" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
									</g:each>
									</ul>
									<g:link controller="partyRole" action="create" params="['organization.id': organizationInstance?.id]">${warehouse.message(code: 'default.add.label', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole')])}</g:link>

								</td>
							</tr>
						</tbody>
						<tfoot>
							<tr class="prop">
								<td valign="top"></td>
								<td valign="top left">
                                    <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
                                    <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
								</td>
							</tr>
					</tfoot>
					</table>
				</div>
            </g:form>
        </div>
    </body>
</html>
