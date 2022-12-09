<%@ page import="org.pih.warehouse.core.UnitOfMeasure" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'unitOfMeasureConversion.label', default: 'Unit of Measure Conversion')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <g:hasErrors bean="${unitOfMeasureConversion}">
            <div class="errors">
                <g:renderErrors bean="${unitOfMeasureConversion}" as="list" />
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
                <g:hiddenField name="id" value="${unitOfMeasureConversion?.id}" />
                <div class="box">
                    <h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="middle" class="name">
                                    <label for="fromUnitOfMeasure.id"><warehouse:message code="unitOfMeasureConversion.fromUnitOfMeasure.label" default="From Unit Of Measure" /></label>
                                </td>
                                <td valign="middle" class="value">
                                    <g:select name="fromUnitOfMeasure.id"
                                              id="fromUnitOfMeasure.id"
                                              value="${unitOfMeasureConversion.fromUnitOfMeasure?.id}"
                                              from="${UnitOfMeasure.list() }"
                                              optionKey="id" optionValue="name"
                                              data-placeholder="Choose a unit of measure"
                                              class="chzn-select-deselect"
                                              noSelection="['null':'']" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="middle" class="name">
                                    <label for="toUnitOfMeasure.id"><warehouse:message code="unitOfMeasureConversion.toUnitOfMeasure.label" default="To Unit Of Measure" /></label>
                                </td>
                                <td valign="middle" class="value">
                                    <g:select name="toUnitOfMeasure.id"
                                              id="toUnitOfMeasure.id"
                                              value="${unitOfMeasureConversion.toUnitOfMeasure?.id}"
                                              from="${UnitOfMeasure.list() }"
                                              optionKey="id" optionValue="name"
                                              data-placeholder="Choose a unit of measure"
                                              class="chzn-select-deselect"
                                              noSelection="['null':'']" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="middle" class="name">
                                    <label for="conversionRate"><warehouse:message code="unitOfMeasureConversion.conversionRate.label" default="Conversion Rate" /></label>
                                </td>
                                <td valign="middle" class="value ${hasErrors(bean: unitOfMeasureConversion, field: 'conversionRate', 'errors')}">
                                    <g:textField class="text" size="80" name="conversionRate" value="${unitOfMeasureConversion?.conversionRate?.stripTrailingZeros()}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active"><g:message code="default.active.label" default="Active" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: unitOfMeasureConversion, field: 'active', 'errors')}">
                                    <g:checkBox name="active" value="${unitOfMeasureConversion?.active}" />
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
