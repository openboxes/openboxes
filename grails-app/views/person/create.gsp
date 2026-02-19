
<%@ page import="org.pih.warehouse.core.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'person.label', default: 'Person')}" />
        <title><warehouse:message code="default.add.label" args="[entityName]" /></title>
        <content tag="pageTitle"><warehouse:message code="default.add.label" args="[entityName]" /></content>
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

            <g:form action="save" method="post">
                <div class="box">
                    <h2><warehouse:message code="default.add.label" args="[entityName]" /></h2>
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
                                    <g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />
                                    <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                </td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </g:form>
        </div>
    </body>
</html>
