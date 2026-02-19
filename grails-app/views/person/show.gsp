
<%@ page import="org.pih.warehouse.core.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'person.label', default: 'Person')}" />
        <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
        <content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
                <div class="message" role="status" aria-label="message">${flash.message}</div>
            </g:if>

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

            <div class="box">
                <h2><warehouse:message code="default.show.label" args="[entityName]" /></h2>
                <table>
                    <tbody>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="user.active.label" default="Active" />
                            </td>
                            <td valign="top" class="value">
                                <g:if test="${personInstance?.active}">
                                    <span class="tag tag-success"><warehouse:message code="default.active.label" default="Active"/></span>
                                </g:if>
                                <g:else>
                                    <span class="tag tag-danger"><warehouse:message code="default.inactive.label" default="Inactive"/></span>
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="person.id.label" default="Id" />
                            </td>
                            <td valign="top" class="value">
                                ${fieldValue(bean: personInstance, field: "id")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="person.type.label" default="Type" />
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
                                <warehouse:message code="person.name.label" default="Name" />
                            </td>
                            <td valign="top" class="value">
                                ${fieldValue(bean: personInstance, field: "name")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="person.firstName.label" default="First Name" />
                            </td>
                            <td valign="top" class="value">
                                ${fieldValue(bean: personInstance, field: "firstName")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="person.lastName.label" default="Last Name" />
                            </td>
                            <td valign="top" class="value">
                                ${fieldValue(bean: personInstance, field: "lastName")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="person.email.label" default="Email" />
                            </td>
                            <td valign="top" class="value">
                                <g:if test="${grailsApplication.config.openboxes.anonymize.enabled}">
                                    ${util.StringUtil.mask(personInstance?.email)}
                                </g:if>
                                <g:else>
                                    ${fieldValue(bean: personInstance, field: "email")}
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="person.phoneNumber.label" default="Phone Number" />
                            </td>
                            <td valign="top" class="value">
                                ${fieldValue(bean: personInstance, field: "phoneNumber")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="person.identifier.label" default="Identifier" />
                            </td>
                            <td valign="top" class="value">
                                ${fieldValue(bean: personInstance, field: "identifier")}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="default.dateCreated.label" default="Date Created" />
                            </td>
                            <td valign="top" class="value">
                                <format:datetime obj="${personInstance?.dateCreated}" />
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <warehouse:message code="default.lastUpdated.label" default="Last Updated" />
                            </td>
                            <td valign="top" class="value">
                                <format:datetime obj="${personInstance?.lastUpdated}" />
                            </td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr class="prop">
                            <td valign="top"></td>
                            <td valign="top left">
                                <g:form>
                                    <g:hiddenField name="id" value="${personInstance?.id}" />
                                    <g:actionSubmit class="button" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
                                    <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                                </g:form>
                            </td>
                        </tr>
                    </tfoot>
                </table>
            </div>
        </div>
    </body>
</html>
