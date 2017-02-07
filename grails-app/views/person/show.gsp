
<%@ page import="org.pih.warehouse.core.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'person.label', default: 'Person')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">

            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>

            <div class="nav" role="navigation">
                <ul>
                    <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                    <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[warehouse.message(code:'persons.label').toLowerCase()]"/></g:link></li>
                    <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
                </ul>
            </div>

            <div class="box">
                <h2><warehouse:message code="default.show.label" args="[entityName]" /></h2>
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="person.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "id")}</td>
                            
                        </tr>
						<tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="person.type.label" default="Type" />
                            </td>
                            <td valign="top" class="value">
								${personInstance?.class?.simpleName} 
                            </td>
                        </tr>                        
                        
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="person.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="person.firstName.label" default="First Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "firstName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="person.lastName.label" default="Last Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "lastName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="person.email.label" default="Email" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "email")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="person.phoneNumber.label" default="Phone Number" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "phoneNumber")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="default.dateCreated.label" default="Date Created" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${personInstance?.dateCreated}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><warehouse:message code="default.lastUpdated.label" default="Last Updated" /></td>
                            
                            <td valign="top" class="value"><format:datetime obj="${personInstance?.lastUpdated}" /></td>
                            
                        </tr>
                    
                    
						<tr class="prop">
                        	<td valign="top"></td>
                        	<td valign="top" colspan="2">
					            <div class="button-bar">
                                    <g:form url="[resource:personInstance, action:'delete']" method="DELETE">
                                        <g:link class="edit button" action="edit" resource="${personInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
                                        <g:actionSubmit class="delete button" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                    </g:form>
					            </div>
							</td>
						</tr>                    
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
