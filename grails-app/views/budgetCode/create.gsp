
<%@ page import="org.pih.warehouse.core.BudgetCode" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'budgetCode.label', default: 'Budget Code')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
		<style>
			.chosen-container > a, .chosen-drop {
				max-width: 500px;
			}
		</style>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${budgetCode}">
	            <div class="errors">
	                <g:renderErrors bean="${budgetCode}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list">
                	<img src="${resource(dir: 'images/icons/silk', file: 'table.png')}" />&nbsp;
					<warehouse:message code="default.list.label" args="['Budget Code']"/>
				</g:link>
				<g:link class="button" action="create">
                	<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
					<warehouse:message code="default.add.label" args="['Budget Code']"/>
				</g:link>
			</div>

			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>
						
							<tr class="prop">
								<td valign="middle" class="name">
									<label for="code"><warehouse:message code="budgetCode.code.label" default="Code" /></label>
								</td>
								<td valign="middle" class="value ${hasErrors(bean: budgetCode, field: 'code', 'errors')}">
									<g:textField class="text" size="80" name="code" value="${budgetCode?.code}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="middle" class="name">
									<label for="name"><warehouse:message code="budgetCode.name.label" default="Name" /></label>
								</td>
								<td valign="middle" class="value">
									<g:textField class="text" size="80" name="name" value="${budgetCode?.name}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="middle" class="name">
									<label for="description"><warehouse:message code="budgetCode.description.label" default="Description" /></label>
								</td>
								<td valign="middle" class="value">
									<g:textField class="text" size="80" name="description" value="${budgetCode?.description}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="middle" class="name">
									<label for="organization"><warehouse:message code="budgetCode.organization.label" default="Organization" /></label>
								</td>
								<td valign="middle" class="value">
									<g:selectOrganization name="organization"
														  id="organization"
                                                          roleTypes="[RoleType.ROLE_ORGANIZATION, RoleType.ROLE_SUPPLIER, RoleType.ROLE_MANUFACTURER]"
														  noSelection="['':'']"
														  class="chzn-select-deselect" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="middle"></td>
								<td valign="middle">
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
