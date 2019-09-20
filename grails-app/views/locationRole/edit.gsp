
<%@ page import="org.pih.warehouse.core.LocationRole" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'locationRole.label', default: 'LocationRole')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${locationRoleInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${locationRoleInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['locationRole']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['locationRole']"/></g:link>
			</div>

			<g:form method="post">
				<g:hiddenField name="id" value="${locationRoleInstance?.id}" />
				<g:hiddenField name="version" value="${locationRoleInstance?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="user"><warehouse:message code="locationRole.user.label" default="User" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: locationRoleInstance, field: 'user', 'errors')}">
									<g:select class="chzn-select-deselect" name="user.id" from="${org.pih.warehouse.core.User.list()}" optionKey="id" value="${locationRoleInstance?.user?.id}"  />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="location"><warehouse:message code="locationRole.location.label" default="Location" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: locationRoleInstance, field: 'location', 'errors')}">
									<g:select class="chzn-select-deselect" name="location.id" from="${org.pih.warehouse.core.Location.list()}" optionKey="id" value="${locationRoleInstance?.location?.id}"  />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="role"><warehouse:message code="locationRole.role.label" default="Role" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: locationRoleInstance, field: 'role', 'errors')}">
									<g:select class="chzn-select-deselect" name="role.id" from="${org.pih.warehouse.core.Role.list()}" optionKey="id" value="${locationRoleInstance?.role?.id}"  />
								</td>
							</tr>
						</tbody>
                        <tfoot>
							<tr class="prop">
								<td valign="top"></td>
								<td valign="top left">
									<div class="buttons left">
										<g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
										<g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
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
