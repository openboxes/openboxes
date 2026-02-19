
<%@ page import="org.pih.warehouse.core.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'person.label', default: 'Person')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
                <div class="message" role="status" aria-label="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${personInstance}">
                <div class="errors" role="alert" aria-label="error-message">
                    <g:renderErrors bean="${personInstance}" as="list" />
                </div>
            </g:hasErrors>

            <div class="button-bar">
                <g:link class="button" action="list">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[warehouse.message(code:'persons.label').toLowerCase()]"/>
                </g:link>
                <g:link class="button" action="create">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.add.label" args="[warehouse.message(code:'person.label').toLowerCase()]"/>
                </g:link>
            </div>

            <g:form method="post">
                <g:hiddenField name="id" value="${personInstance?.id}" />
                <g:hiddenField name="version" value="${personInstance?.version}" />
                <div class="box">
                    <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active"><warehouse:message code="user.active.label" default="Active" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'active', 'errors')}">
                                    <g:checkBox name="active" value="${personInstance?.active}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="type"><warehouse:message code="person.type.label" default="Type" /></label>
                                </td>
                                <td valign="top" class="value">
                                    <g:if test="${personInstance?.class?.simpleName == 'User'}">
                                        <span class="tag tag-info"><warehouse:message code="user.label"/></span>
                                    </g:if>
                                    <g:else>
                                        <span class="tag"><warehouse:message code="person.label"/></span>
                                    </g:else>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="firstName"><warehouse:message code="person.firstName.label" default="First Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'firstName', 'errors')}">
                                    <g:textField name="firstName" value="${personInstance?.firstName}" class="text" size="40" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastName"><warehouse:message code="person.lastName.label" default="Last Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'lastName', 'errors')}">
                                    <g:textField name="lastName" value="${personInstance?.lastName}" class="text" size="40" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="email"><warehouse:message code="person.email.label" default="Email" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'email', 'errors')}">
                                    <g:textField name="email" value="${personInstance?.email}" class="text" size="40" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="phoneNumber"><warehouse:message code="person.phoneNumber.label" default="Phone Number" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'phoneNumber', 'errors')}">
                                    <g:textField name="phoneNumber" value="${personInstance?.phoneNumber}" class="text" size="40" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="identifier"><warehouse:message code="person.identifier.label" default="Identifier" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'identifier', 'errors')}">
                                    <g:textField name="identifier" value="${personInstance?.identifier}" class="text" size="40" />
                                </td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr class="prop">
                                <td valign="top"></td>
                                <td valign="top left">
                                    <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}" />
                                    <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                    <g:if test="${personInstance?.class?.simpleName == 'Person'}">
                                        <g:link class="button" action="convertPersonToUser" id="${personInstance?.id}">
                                            <warehouse:message code="person.convertPersonToUser.label" default="Convert to User"/>
                                        </g:link>
                                    </g:if>
                                    <g:elseif test="${personInstance?.class?.simpleName == 'User'}">
                                        <g:link class="button" action="convertUserToPerson" id="${personInstance?.id}">
                                            <warehouse:message code="person.convertUserToPerson.label" default="Convert to Person"/>
                                        </g:link>
                                    </g:elseif>
                                </td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </g:form>
        </div>
    </body>
</html>
