
<%@ page import="org.pih.warehouse.core.User"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName"
	value="${warehouse.message(code: 'user.label', default: 'User')}" />
<title><warehouse:message code="default.show.label"
		args="[entityName]" /></title>
<content tag="pageTitle">
<warehouse:message code="default.show.label" args="[entityName]" /></content>

</head>
<body>
	<div class="body">

		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>


		<fieldset>

			<div class="dialog">
				<table>
					<thead>
						<tr>
							<td colspan="2"><g:render template="summary" /></td>
						</tr>
					</thead>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name"><warehouse:message
									code="user.username.label" /></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "username")}
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><warehouse:message
									code="default.name.label" /></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "name")}
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><warehouse:message
									code="user.email.label" /></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "email")}
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><warehouse:message
									code="default.locale.label" /></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "locale.displayName")}
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><warehouse:message
									code="user.warehouse.label" /></td>
							<td valign="top" class="value">
								<g:if test="${userInstance?.warehouse}">
									${fieldValue(bean: userInstance, field: "warehouse")}
								</g:if> 
								<g:else>
									<span class="fade">
										<warehouse:message code="default.none.label" />
									</span>
								</g:else>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><warehouse:message
									code="user.roles.label" default="Roles" /></td>
							<td valign="top" class="value">
								<g:if test="${userInstance?.roles}">
									${fieldValue(bean: userInstance, field: "roles")}
								</g:if> 
								<g:else>
									<span class="fade">
										<warehouse:message code="default.none.label" />
									</span>
								</g:else>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">
								<warehouse:message code="user.lastLoginDate.label" />
							</td>
							<td valign="top" class="value">
								<g:if test="${userInstance.lastLoginDate }">
									<format:datetime obj="${userInstance.lastLoginDate}"></format:datetime>
								</g:if>
								<g:else>
									<span class="fade">
										<warehouse:message code="default.never.label" />
									</span>
								</g:else>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">
								<warehouse:message code="user.rememberLastLocation.label" />
							</td>
							<td valign="top" class="value">
								<g:if test="${userInstance.rememberLastLocation }">
									${userInstance.rememberLastLocation}
								</g:if>
								<g:else>
									<span class="fade">
										<warehouse:message code="default.none.label" />
									</span>
								</g:else>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><warehouse:message
									code="user.photo.label" /></td>
							<td valign="top" class="value">

								<table>
									<tr>
										<td><g:if test="${userInstance.photo}">
												<img class="photo"
													src="${createLink(controller:'user', action:'viewPhoto', id:userInstance.id)}" />
											</g:if></td>
										<td><g:form controller="user" method="post"
												action="uploadPhoto" enctype="multipart/form-data">
												<input type="hidden" name="id" value="${userInstance.id}" />
												<input type="file" name="photo" />
												<span class="buttons"><input class="positive"
													type="submit"
													value="${warehouse.message(code:'default.button.upload.label')}" /></span>
											</g:form></td>
									</tr>
								</table>
							</td>
						</tr>
						<tr class="prop">
							<td class="name"></td>
							<td class="value">
								<div class="buttons left">
									<g:link class="edit" action="edit" id="${userInstance?.id}">
										${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}
									</g:link>
									<g:link class="delete" action="delete" id="${userInstance?.id}"
										onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
										${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}
									</g:link>
									<g:link action="toggleActivation" id="${userInstance?.id}">
										<g:if test="${userInstance?.active}">
											${warehouse.message(code: 'user.deactivate.label')}
										</g:if>
										<g:else>
											${warehouse.message(code: 'user.activate.label')}
										</g:else>
									</g:link>
								</div>

							</td>
						</tr>

						<!-- 
							<tr class="prop">
	                            <td valign="top" class="prop name"></td>
	                            <td valign="top" class="prop value">
									<g:form>
										<g:hiddenField name="id" value="${userInstance?.id}" />
										<div class="buttons">
											<g:if test="${userInstance?.active}">
												<g:actionSubmit class="positive" action="toggleActivation" value="${warehouse.message(code: 'default.button.deactivate.label', default: 'De-activate')}" />
											</g:if>
											<g:else>
												<g:actionSubmit class="negative" action="toggleActivation" value="${warehouse.message(code: 'default.button.activate.label', default: 'Activate')}" />
											</g:else>
											<g:actionSubmit class="positive" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
											<g:actionSubmit class="negative" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
										</div>						
									</g:form>
								</td>
	                        </tr>
	                        -->
					</tbody>
				</table>
			</div>
		</fieldset>
	</div>
</body>
</html>
