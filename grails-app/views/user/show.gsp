
<%@ page import="org.pih.warehouse.core.User" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
	<content tag="pageTitle"><g:message code="default.show.label" args="[entityName]" /></content>

    </head>
    <body>   
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
			<fieldset>
				<g:render template="summary"/>
	            <div class="dialog">
	                <table>
	                    <tbody>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="user.id.label" default="ID" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: userInstance, field: "id")}</td>                            
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="user.username.label" default="Username" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: userInstance, field: "username")}</td>                            
	                        </tr>
	                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="user.name.label" default="Name" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: userInstance, field: "lastName")}, ${fieldValue(bean: userInstance, field: "firstName")}</td>                            
	                        </tr>
	                    
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="user.password.label" default="Password" /></td>                            
	                            <td valign="top" class="value">${fieldValue(bean: userInstance, field: "password")}</td>                            
	                        </tr>
	
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="user.email.label" default="Email" /></td>
	                            <td valign="top" class="value">${fieldValue(bean: userInstance, field: "email")}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="user.roles.label" default="Roles" /></td>
	                            <td valign="top" class="value">${fieldValue(bean: userInstance, field: "roles")}</td>
	                        </tr>
	                        <tr class="prop">
	                            <td valign="top" class="name"><g:message code="user.photo.label" default="Profile Photo" /></td>                            
	                            <td valign="top" class="value">
	                            
	                            	<table>
	                            		<tr>
	                            			<td>
												<g:if test="${userInstance.photo}">
													<img class="photo" src="${createLink(controller:'user', action:'viewPhoto', id:userInstance.id)}" />
												</g:if>
	                            			</td>
	                            			<td>
												<g:form controller="user" method="post" action="uploadPhoto" enctype="multipart/form-data">
													<input type="hidden" name="id" value="${userInstance.id}" />
													<input type="file" name="photo"/>
													<span class="buttons"><input class="positive" type="submit" value="Upload"/></span>
												</g:form>
	                            			</td>
	                            		</tr>
	                            	</table>
								</td>
							</tr>
							<tr class="prop">
	                            <td valign="top" class="name"></td>
	                            <td valign="top" class="value">
									<g:form>
										<g:hiddenField name="id" value="${userInstance?.id}" />
										<div class="buttons">
											<g:if test="${userInstance?.active}">
												<g:actionSubmit class="positive" action="toggleActivation" value="${message(code: 'default.button.deactivate.label', default: 'De-activate')}" />
											</g:if>
											<g:else>
												<g:actionSubmit class="negative" action="toggleActivation" value="${message(code: 'default.button.activate.label', default: 'Activate')}" />
											</g:else>
											<g:actionSubmit class="positive" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" />
											<g:actionSubmit class="negative" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
										</div>						
									</g:form>
								</td>
	                        </tr>
	                    </tbody>
	                </table>
	            </div>
			</fieldset>        	
        </div>
    </body>
</html>
