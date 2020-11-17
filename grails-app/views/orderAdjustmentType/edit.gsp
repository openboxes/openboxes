
<%@ page import="org.pih.warehouse.order.OrderAdjustmentType" %>
<%@ page import="org.pih.warehouse.core.RoleType" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'orderAdjustmentType.label', default: 'Order Adjustment Type')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${orderAdjustmentType}">
            <div class="errors">
                <g:renderErrors bean="${orderAdjustmentType}" as="list" />
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

        <g:hiddenField id="isAccountingRequired" name="isAccountingRequired" value="${locationInstance?.isAccountingRequired()}"/>

        <div id="edit-order-adjustment-type">
            <g:form action="update" onsubmit="return validateForm();">
                <g:hiddenField name="id" value="${orderAdjustmentType?.id}" />
                <div class="box">
                    <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">
                                        <warehouse:message code="orderAdjustmentType.name.label" default="Name" />
                                    </label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: orderAdjustmentType, field: 'name', 'errors')}">
                                    <g:textField class="text large" size="80" name="name" value="${orderAdjustmentType?.name}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">
                                        <warehouse:message code="orderAdjustmentType.description.label" default="Description" />
                                    </label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: orderAdjustmentType, field: 'description', 'errors')}">
                                    <g:textField class="text large" size="80" name="description" value="${orderAdjustmentType?.description}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="code">
                                        <warehouse:message code="orderAdjustmentType.code.label" default="Event Status" />
                                    </label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: orderAdjustmentType, field: 'code', 'errors')}">
                                    <g:select name="code" from="${org.pih.warehouse.order.OrderAdjustmentTypeCode?.values()}"
                                              value="${orderAdjustmentType?.code}" noSelection="['': '']" class="chzn-select-deselect"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td class="name middle">
                                    <label id="glAccountLabel" for="glAccount.id">
                                        <warehouse:message code="orderAdjustmentType.glAccount.label"/>
                                    </label>
                                </td>
                                <td class="value middle ${hasErrors(bean: orderAdjustmentType, field: 'glAccount', 'errors')}">
                                    <g:selectGlAccount name="glAccount.id"
                                                       id="glAccount"
                                                       value="${orderAdjustmentType?.glAccount?.id}"
                                                       noSelection="['null':'']"
                                                       class="chzn-select-deselect" />
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
                                    <g:link
                                        class="button"
                                        controller="orderAdjustmentType"
                                        action="delete"
                                        params="[id: orderAdjustmentType?.id]"
                                        onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
                                    >
                                        ${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}
                                    </g:link>
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
    <script type="text/javascript">
        function validateForm()  {
            var glAccount = $("#glAccount").val();
            var isAccountingRequired = ($("#isAccountingRequired").val() === "true");
            if (isAccountingRequired && (!glAccount || glAccount === "null")) {
                $("#glAccountLabel").notify("Required");
                return false;
            } else {
                return true;
            }
        }
    </script>
    </body>
</html>
