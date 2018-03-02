
<%@ page import="org.pih.warehouse.core.Party" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'party.label', default: 'Party')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${partyInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${partyInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['party']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['party']"/></g:link>
			</div>

			<g:form method="post" >
				<g:hiddenField name="id" value="${partyInstance?.id}" />
				<g:hiddenField name="version" value="${partyInstance?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="partyType"><warehouse:message code="party.partyType.label" default="Party Type" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: partyInstance, field: 'partyType', 'errors')}">
									<g:select class="chzn-select-deselect" name="partyType.id" from="${org.pih.warehouse.core.PartyType.list()}" optionKey="id" value="${partyInstance?.partyType?.id}"  />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="roles"><warehouse:message code="party.roles.label" default="Roles" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: partyInstance, field: 'roles', 'errors')}">
									
<ul>
<g:each in="${partyInstance?.roles?}" var="r">
    <li><g:link controller="partyRole" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="partyRole" action="create" params="['party.id': partyInstance?.id]">${warehouse.message(code: 'default.add.label', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole')])}</g:link>

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
