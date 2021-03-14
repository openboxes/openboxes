
<%@ page import="org.pih.warehouse.core.PreferenceType" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'preferenceType.label', default: 'Preference Type')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${preferenceType}">
            <div class="errors">
                <g:renderErrors bean="${preferenceType}" as="list" />
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

        <div id="edit-preference-type">
            <g:form method="post" action="update" >
                <g:hiddenField name="id" value="${preferenceType?.id}" />
                <div class="box">
                    <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><warehouse:message code="preferenceType.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: preferenceType, field: 'name', 'errors')}">
                                    <g:textField class="text large" size="80" name="name" value="${preferenceType?.name}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="validationCode"><warehouse:message code="preferenceType.validationCode.label" default="Event Status" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: preferenceType, field: 'validationCode', 'errors')}">
                                    <g:select name="validationCode" from="${org.pih.warehouse.core.ValidationCode?.values()}"
                                              value="${preferenceType?.validationCode}" noSelection="['': '']" class="chzn-select-deselect"/>
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
