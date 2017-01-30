
<%@ page import="org.pih.warehouse.core.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'person.label', default: 'Person')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${personInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${personInstance}" as="list" />
	            </div>
            </g:hasErrors>

            <div class="nav" role="navigation">
                <ul>
                    <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                    <li><g:link class="list" action="index"><warehouse:message code="default.list.label" args="[warehouse.message(code:'persons.label').toLowerCase()]"/></g:link></li>
                    <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
                </ul>
            </div>

            <g:form method="put">
                <g:hiddenField name="id" value="${personInstance?.id}" />
                <g:hiddenField name="version" value="${personInstance?.version}" />
                <div class="dialog box">
                    <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label><warehouse:message code="person.type.label" default="Type" /></label>
                                </td>
                                <td valign="top" class="value">
                                    <g:if test="${personInstance?.class?.simpleName=='Person' }">
                                        <warehouse:message code="person.label"/>
                                    </g:if>
                                    <g:elseif test="${personInstance?.class?.simpleName=='User' }">
                                        <warehouse:message code="user.label"/>
                                    </g:elseif>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="firstName"><warehouse:message code="person.firstName.label" default="First Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'firstName', 'errors')}">
                                    <g:textField name="firstName" value="${personInstance?.firstName}" class="text medium" size="60"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="lastName"><warehouse:message code="person.lastName.label" default="Last Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'lastName', 'errors')}">
                                    <g:textField name="lastName" value="${personInstance?.lastName}" class="text medium" size="60"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="email"><warehouse:message code="person.email.label" default="Email" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'email', 'errors')}">
                                    <g:textField name="email" value="${personInstance?.email}" class="text medium" size="60"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="phoneNumber"><warehouse:message code="person.phoneNumber.label" default="Phone Number" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'phoneNumber', 'errors')}">
                                    <g:textField name="phoneNumber" value="${personInstance?.phoneNumber}" class="text medium" size="60"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name"></td>
                                <td valign="top" class="value">
                                    <div class="left">
                                        <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.save.label', default: 'Update')}" />
                                        &nbsp;
                                        <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                        &nbsp;
                                        <g:link action="convertPersonToUser" id="${personInstance?.id }" class="button"><warehouse:message code="person.convertPersonToUser.label"/></g:link>
                                        &nbsp;
                                        <g:link action="index">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
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
