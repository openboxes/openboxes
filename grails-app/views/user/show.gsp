<%@ page import="org.pih.warehouse.core.User"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName" value="${warehouse.message(code: 'user.label', default: 'User')}" />
<title><warehouse:message code="default.show.label" args="[entityName]" /></title>
<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>

</head>
<body>
	<div class="body">


        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>

        <div class="dialog">

            <g:render template="summary" />


            <div class="box">
				<h2><g:message code="user.label"/></h2>
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message
									code="user.username.label" /></label></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "username")}
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message
									code="default.name.label" /></label></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "name")}
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message
									code="user.email.label" /></label></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "email")}								
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message
									code="default.locale.label" /></label></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "locale.displayName")}
							</td>
						</tr>
                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message
                                    code="default.timezone.label" default="Timezone" /></label></td>
                            <td valign="top" class="value">
                                ${userInstance?.timezone}
                            </td>
                        </tr>
						<tr class="prop">
							<td valign="top" class="name">
                                <label><warehouse:message
									code="user.roles.label" default="Roles" /></label></td>
							<td valign="top" class="value">
								<g:if test="${userInstance?.roles}">
									${fieldValue(bean: userInstance, field: "roles")}
								</g:if>
								<g:else>
									<span class="fade">
										<warehouse:message code="no.access.label" />
									</span>
								</g:else>
							</td>
						</tr>
                        <tr class="prop" id="locationRoles">
                             <td valign="top" class="name">
                                <label><warehouse:message code="user.locationRoles.label" /></label>
                             </td>
                             <td valign="top" class="value">
                                 <g:if test="${userInstance?.locationRoles}">
                                    ${userInstance?.locationRolesDescription()}
                                 </g:if>
                                 <g:else>
                                     <span class="fade">${warehouse.message(code:'default.none.label')}</span>
                                 </g:else>
                             </td>
                         </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message
                                    code="user.defaultLocation.label" /></label></td>
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
							<td valign="top" class="name">
                                <label><warehouse:message code="user.rememberLastLocation.label" /></label>
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
							<td valign="top" class="name">
                                <label><warehouse:message code="user.lastLoginDate.label" /></label>
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
                                <label><warehouse:message code="default.lastUpdated.label" /></label>
                            </td>
                            <td valign="top" class="value">
                                <g:if test="${userInstance.lastUpdated }">
                                    <format:datetime obj="${userInstance.lastUpdated}"></format:datetime>
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
								<label><warehouse:message code="default.dateCreated.label" /></label>
							</td>
							<td valign="top" class="value">
								<g:if test="${userInstance.dateCreated }">
									<format:datetime obj="${userInstance.dateCreated}"></format:datetime>
								</g:if>
								<g:else>
									<span class="fade">
										<warehouse:message code="default.never.label" />
									</span>
								</g:else>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

	</div>
</body>
</html>
