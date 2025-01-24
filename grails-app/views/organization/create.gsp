
<%@ page import="org.pih.warehouse.core.PartyType; org.pih.warehouse.core.Organization" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'organization.label', default: 'Organization')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message" role="status" aria-label="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${organizationInstance}">
	            <div class="errors" role="alert" aria-label="error-message">
	                <g:renderErrors bean="${organizationInstance}" as="list" />
	            </div>
            </g:hasErrors>

            <div class="button-bar">
                <g:link class="button" action="list">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[g.message(code:'organizations.label')]"/>
                </g:link>
                <g:link class="button" action="create" params="[partyType:'ORG']">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.add.label" args="[g.message(code:'organization.label')]"/>
                </g:link>
            </div>

			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="partyType"><warehouse:message code="organization.partyType.label" default="Party Type" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'partyType', 'errors')}">
									<div data-test-id="party-type-select">
										<g:select class="chzn-select-deselect" name="partyType.id" from="${org.pih.warehouse.core.PartyType.list()}" optionKey="id" value="${organizationInstance?.partyType?.id}"  />
									</div>
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
								<td valign="top"></td>
								<td valign="top">
									<div class="buttons left">
									   <g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />

									   <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>

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
