<%@ page import="org.pih.warehouse.requisition.RequisitionItemSortByCode; grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
    <script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
    <style>
    .sortable { list-style-type: none; margin: 0; padding: 0; width: 100%; }
    .sortable tr { margin: 0 5px 5px 5px; padding: 5px; font-size: 1.2em; height: 1.5em; }
    html>body .sortable li { height: 1.5em; line-height: 1.2em; }
    .ui-state-highlight { height: 1.5em; line-height: 1.2em; }
    </style>
</head>
<body>

<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<g:if test="${flash.error}">
    <div class="errors">${flash.error}</div>
</g:if>
<g:hasErrors bean="${requisition}">
    <div class="errors">
        <g:renderErrors bean="${requisition}" as="list" />
    </div>
</g:hasErrors>

<g:render template="summary" model="[requisition:requisition]"/>
<div class="yui-gf">
    <div class="yui-u first">
        <g:render template="header" model="[requisition:requisition]"/>

    </div>
    <div class="yui-u">

        <div class="box">
            <h2>${warehouse.message(code:'requisitionTemplate.requisitionItems.label')}</h2>

            <g:form name="requisitionItemForm" method="post" controller="requisitionTemplate" action="update">


                <g:hiddenField name="id" value="${requisition.id}"/>
                <g:hiddenField name="version" value="${requisition.version}"/>

                <div class="dialog list">
                    <table class="sortable" data-update-url="${createLink(controller:'json', action:'sortRequisitionItems')}">
                        <thead>
                        <tr>
                            <th><warehouse:message code="product.productCode.label" default="#"/></th>
                            <th><warehouse:message code="product.label"/></th>
                            <th><warehouse:message code="category.label"/></th>
                            <th><warehouse:message code="default.quantity.label"/></th>
                            <th><warehouse:message code="unitOfMeasure.label"/></th>
                            <g:hasRoleFinance>
                                <th><warehouse:message code="requisitionTemplate.unitCost.label"/></th>
                                <th><warehouse:message code="requisitionTemplate.totalCost.label"/></th>
                            </g:hasRoleFinance>
                        </tr>
                        </thead>
                        <tbody>
                        <g:set var="sortByCode" value='${requisition?.sortByCode ?: RequisitionItemSortByCode.SORT_INDEX}'/>
                        <g:set var="requisitionItems" value='${requisition?."$sortByCode.methodName"}'/>
                        <g:each var="requisitionItem" in="${requisitionItems}" status="i">
                            <tr class="prop ${i%2?'even':'odd'}" id="requisitionItem_${requisitionItem?.id }" requisitionItem="${requisitionItem?.id}">
                                <td>
                                    ${requisitionItem?.product?.productCode}
                                </td>
                                <td>
                                    <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                                        ${requisitionItem?.product?.name}
                                    </g:link>
                                </td>
                                <td>
                                    <format:metadata obj="${requisitionItem?.product?.category}"/>
                                </td>
                                <td>
                                    ${requisitionItem?.quantity}
                                </td>
                                <td>
                                    EA/1
                                </td>
                                <g:hasRoleFinance>
                                    <td>
                                        ${g.formatNumber(number: (requisitionItem?.product?.pricePerUnit?:0), format: '###,###,##0.00##')}
                                        ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                    <td>
                                        ${g.formatNumber(number: (requisitionItem?.totalCost?:0), format: '###,###,##0.00##')}
                                        ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </g:hasRoleFinance>
                            </tr>
                        </g:each>
                        <g:unless test="${requisition?.requisitionItems}">
                            <tr>
                                <td colspan="4" class="center">
                                    <span class="fade empty">${warehouse.message(code: "requisition.noRequisitionItems.message")}</span>
                                </td>

                            </tr>
                        </g:unless>
                        </tbody>
                        <tfoot>


                        <tr>
                            <td colspan="7">
                                <div class="buttons">
                                </div>
                            </td>
                        </tr>


                        </tfoot>
                    </table>
                </div>
            </g:form>
        </div>
    </div>
</div>
</body>
</html>
