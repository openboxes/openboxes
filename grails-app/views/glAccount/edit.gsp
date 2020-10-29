
<%@ page import="org.pih.warehouse.core.GlAccount" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'glAccount.label', default: 'GL Account')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${glAccount}">
            <div class="errors">
                <g:renderErrors bean="${glAccount}" as="list" />
            </div>
        </g:hasErrors>
        <g:hasErrors bean="${command}">
            <div class="errors">
                <g:renderErrors bean="${command}" as="list" />
            </div>
        </g:hasErrors>

        <div class="button-bar">
            <g:link class="button" action="list">
                <img src="${resource(dir: 'images/icons/silk', file: 'table.png')}" />&nbsp;
                <warehouse:message code="default.list.label" args="[entityName]"/>
            </g:link>
            <g:link class="button" action="create">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.create.label" args="[entityName]"/>
            </g:link>
        </div>

        <div id="edit-gl-account">
            <g:form method="post" action="update" >
                <g:hiddenField name="id" value="${glAccount?.id}" />
                <div class="box">
                    <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="code"><warehouse:message code="glAccount.code.label" default="Code" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: glAccount, field: 'code', 'errors')}">
                                    <g:textField class="text large" size="80" name="code" value="${glAccount?.code}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="glAccount.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: glAccount, field: 'name', 'errors')}">
                                    <g:textField class="text large" size="80" name="name" value="${glAccount?.name}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description"><warehouse:message code="glAccount.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: glAccount, field: 'description', 'errors')}">
                                    <g:textArea class="text large" name="description" value="${glAccount?.description}" />
                                </td>
                            </tr>

                        <tr class="prop">
                            <td valign="middle" class="name">
                                <label for="glAccountType.id"><warehouse:message code="glAccount.glAccountType.label" default="GL Account Type" /></label>
                            </td>
                            <td valign="middle" class="value">
                                <g:selectGlAccountType name="glAccountType.id"
                                                       id="glAccountType.id"
                                                       noSelection="['null':'']"
                                                       class="chzn-select-deselect"
                                                       value="${glAccountTypeId}"/>
                            </td>
                        </tr>
                        </tbody>
                        <tfoot>
                        <tr class="prop">
                            <td valign="top"></td>
                            <td valign="top left">
                                <div class="buttons left">
                                    <g:actionSubmit
                                        class="button"
                                        action="update"
                                        value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}"
                                    />
                                    <g:actionSubmit
                                        class="button"
                                        action="delete"
                                        value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}"
                                        onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                    />
                                    <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                </div>
                            </td>
                        </tr>
                        </tfoot>
                    </table>
                </div>
            </g:form>
        </div>
    </div>
    </body>
</html>
