
<%@ page import="org.pih.warehouse.core.User" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'user.label', default: 'User')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
      
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${userInstance}">
				<div class="errors">
					<g:renderErrors bean="${userInstance}" as="list" />
				</div>
            </g:hasErrors>
            
            
            
            <g:form method="post" >
                <g:hiddenField name="id" value="${userInstance?.id}" />
                <g:hiddenField name="version" value="${userInstance?.version}" />
                
                
				
				<fieldset>                
	                <div class="dialog">
	                    <table>
							<thead>
								<tr>
				        			<td valign="top" colspan="2">
										<g:render template="summary"/>			            			
									</td>            
		                    	</tr>	                    	
	                    	</thead>	                    
	                    
	                        <tbody>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="username"><warehouse:message code="user.username.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
	                                    <g:textField name="username" value="${userInstance?.username}" />
	                                </td>
	                            </tr>
	                        
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="firstName"><warehouse:message code="user.firstName.label"/></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
	                                    <g:textField name="firstName" value="${userInstance?.firstName}" />
	                                </td>
	                            </tr>
	
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="lastName"><warehouse:message code="user.lastName.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
	                                    <g:textField name="lastName" value="${userInstance?.lastName}" />
	                                </td>
	                            </tr>
	
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="password"><warehouse:message code="user.password.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'password', 'errors')}">
	                                    <g:passwordField name="password" value="${userInstance?.password}" />
	                                </td>
	                            </tr>
	
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                    <label for="password"><warehouse:message code="user.confirmPassword.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'passwordConfirm', 'errors')}">
	                                    <g:passwordField name="passwordConfirm" value="" />
	                                </td>
	                            </tr>
	
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="email"><warehouse:message code="user.email.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
	                                    <g:textField name="email" value="${userInstance?.email}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="locale"><warehouse:message code="default.locale.label"/></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'locale', 'errors')}">
	                                    <g:select name="locale" from="${ grailsApplication.config.locale.supportedLocales.collect{ new Locale(it) } }" optionValue="displayName" value="${userInstance?.locale}" noSelection="['':'']"/>
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="email"><warehouse:message code="user.active.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'active', 'errors')}">
	                                    <g:checkBox name="active" value="${userInstance?.active}" />
	                                </td>
	                            </tr>
	                            <tr class="prop">
	                                <td valign="top" class="name">
	                                  <label for="email"><warehouse:message code="warehouse.label" /></label>
	                                </td>
	                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'warehouse', 'errors')}">
										<g:select name="warehouse.id" from="${org.pih.warehouse.inventory.Warehouse.list()?.sort()}" optionKey="id" value="${userInstance?.warehouse?.id}" noSelection="['':'']"/>
	                                </td>
	                            </tr>	                            	                            
								<tr class="prop">
									<td valign="top" class="name">
				
									</td>
									<td valign="top">
					                    <g:actionSubmit class="save" action="update" value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" /></span>
										&nbsp;
										<g:link class="cancel" action="show" id="${warehouseInstance?.id }">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
									</td>
								</tr>
	                        </tbody>
	                    </table>
	                </div>
				</fieldset>
            </g:form>
        </div>
    </body>
</html>
