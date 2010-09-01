
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
            <div class="dialog">
                <table>
                    <tbody>
						<tr>
		        			<td valign="top" colspan="2">
	            				<img class="photo" width="25" height="25" 
	            					src="${createLink(controller:'user', action:'viewPhoto', id:userInstance.id)}" style="vertical-align: bottom" />
	            				&nbsp;
		            			<span style="font-weight: bold; font-size: 200%">${fieldValue(bean: userInstance, field: "firstName")} ${fieldValue(bean: userInstance, field: "lastName")}</span>
							</td>            
                    	</tr>                    
                    
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
                            <td valign="top" class="name"><g:message code="user.photo.label" default="Profile Photo" /></td>                            
                            <td valign="top" class="value">
							
								<img class="photo" src="${createLink(controller:'user', action:'viewPhoto', id:userInstance.id)}" />
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
							</td>
							<td>							
								<g:form controller="user" method="post" action="uploadPhoto" enctype="multipart/form-data">
									<input type="hidden" name="id" value="${userInstance.id}" />
									<input type="file" name="photo"/>
									<span class="buttons"><input class="positive" type="submit" value="Upload"/></span>
								</g:form>
								
							</td>                            
                        </tr>

						<tr class="prop">
                            <td valign="top" class="name"></td>
                            <td valign="top" class="value">

								<g:form>
									<div class="buttons">
					
										<g:hiddenField name="id" value="${userInstance?.id}" />

										<g:actionSubmit class="positive" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" />
										<%--
										<g:actionSubmit class="negative" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
										--%>										
												<g:link controller="user" action="list" id="${userInstance.id}">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
													alt="Cancel" /> Cancel</g:link>

										</div>
								</g:form>
							</td>
                        </tr>
 
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
