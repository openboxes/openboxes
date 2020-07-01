
<%@ page import="org.pih.warehouse.core.PartyRole" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'partyRole.label', default: 'PartyRole')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${partyRoleInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${partyRoleInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list">
					<img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
					<warehouse:message code="default.list.label" args="['partyRole']"/>
				</g:link>
				<g:link class="button" action="create">
					<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
					<warehouse:message code="default.add.label" args="['partyRole']"/>
				</g:link>
			</div>


			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="party.id"><warehouse:message code="partyRole.party.label" default="Party" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: partyRoleInstance, field: 'party', 'errors')}">
									<g:select class="chzn-select-deselect"
											  name="party.id" from="${org.pih.warehouse.core.Party.list()}"
											  optionKey="id"
											  value="${partyRoleInstance?.party?.id?:params?.party?.id}"  />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="roleType"><warehouse:message code="partyRole.roleType.label" default="Role Type" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: partyRoleInstance, field: 'roleType', 'errors')}">
									<g:select class="chzn-select-deselect" multiple="true"
											  name="roleType"
											  from="${org.pih.warehouse.core.RoleType?.values()}"
											  value="${partyRoleInstance?.roleType}"  />
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
