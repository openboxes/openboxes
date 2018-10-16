
<%@ page import="org.pih.warehouse.core.ReasonCode" %>
<%@ page import="org.pih.warehouse.requisition.RequisitionItem" %>
<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitionItem.label', default: 'Requisition item')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>

    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="yui-gf">
                <div class="yui-u first">
                    <div class="box">
                        <h2><warehouse:message code="default.filters.label"/></h2>
                        <g:form action="list" method="GET">
                            <div class="filter-list">
                                <div class="filter-list-item">
                                    <label><warehouse:message code="requisition.dateRequestedBetween.label" default="Date requested between"/></label>
                                    <div>
                                        <g:jqueryDatePicker name="dateRequestedFrom" value="${params.dateRequestedFrom}"/>
                                        <g:jqueryDatePicker name="dateRequestedTo" value="${params.dateRequestedTo}"/>
                                    </div>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="reasonCode.label" default="Reason code"/></label>
                                    <div class="chzn-field">
                                        <g:select name="cancelReasonCode" from="${ReasonCode.values()}" noSelection="['null':'']"
                                                  class="chzn-select-deselect" keys="${ReasonCode.values()*.name()}"
                                                  multiple="true" value="${params.list('cancelReasonCode')}" data-placeholder=" "/>
                                    </div>
                                </div>
                                <div class="filter-list-item">
                                    <hr/>
                                </div>
                                <div class="filter-list-item">
                                    <button name="search" class="button icon search">${warehouse.message(code:'default.search.label')}</button>
                                    <g:link controller="requisitionItem" action="list"><warehouse:message code="default.clear.label"/></g:link>
                                </div>
                            </div>
                        </g:form>
                    </div>
                </div>
                <div class="yui-u">

                    <div class="button-bar">
                        <g:link class="button icon log" action="list"><warehouse:message code="default.list.label" args="['requisitionItem']"/></g:link>
                        <%--
                        <g:link class="button icon add" action="create"><warehouse:message code="default.add.label" args="['requisitionItem']"/></g:link>
                        --%>
                        <g:link class="button icon arrowdown" action="export" params="${params}"><warehouse:message code="default.export.label" args="['requisitionItem']"/></g:link>
                    </div>
                    <div class="list">

                        <div class="box">
                            <h2>
                                ${title?:warehouse.message(code: "requisitionItem.list.label", default: "List requisition items")}
                                (${requisitionItemInstanceList.totalCount})
                            </h2>
                            <%--
                            <h3>
                                Showing ${(params.offset as int)+1} - ${(params.max as int) + (params.offset as int)} of ${requisitionItemInstanceList.totalCount}
                            </h3>
                            --%>
                            <table>
                                <thead>
                                    <tr>
                                        <%--
                                        <g:sortableColumn property="id" title="${warehouse.message(code: 'requisitionItem.id.label', default: 'Id')}" />
                                        --%>
                                        <th><warehouse:message code="requisition.status.label" /></th>
                                        <th><warehouse:message code="requisition.requestNumber.label" /></th>
                                        <th><warehouse:message code="requisitionItem.requisition.label" default="Requisition" /></th>
                                        <th><warehouse:message code="requisitionItem.dateRequested.label" default="Date requested" /></th>
                                        <th><warehouse:message code="product.productCode.label" /></th>
                                        <th><warehouse:message code="product.label" /></th>
                                        <th><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Cancel reason code" /></th>
                                        <th><warehouse:message code="requisitionItem.cancelComments.label" default="Cancel comments" /></th>
                                        <th><warehouse:message code="requisitionItem.quantityApproved.label" default="Quantity approved" /></th>
                                        <th><warehouse:message code="requisitionItem.quantityCanceled.label" default="Quantity canceled" /></th>
                                        <th><warehouse:message code="requisitionItem.quantity.label" default="Quantity requested" /></th>
                                        <th><warehouse:message code="default.actions.label" default="Actions" /></th>

                                    </tr>
                                </thead>
                                <tbody>
                                <g:each in="${requisitionItemInstanceList}" status="i" var="requisitionItemInstance">
                                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                                        <td>
                                            <g:link controller="requisition" action="show" id="${requisitionItemInstance.requisition.id}">
                                                ${fieldValue(bean: requisitionItemInstance.requisition, field: "status")}
                                            </g:link>
                                        </td>
                                        <td>
                                            <g:link controller="requisition" action="show" id="${requisitionItemInstance.requisition.id}">
                                                ${fieldValue(bean: requisitionItemInstance.requisition, field: "requestNumber")}
                                            </g:link>
                                        </td>
                                        <td>
                                            <g:link controller="requisition" action="show" id="${requisitionItemInstance.requisition.id}">
                                                ${fieldValue(bean: requisitionItemInstance.requisition, field: "name")}
                                            </g:link>
                                        </td>
                                        <td>
                                            ${fieldValue(bean: requisitionItemInstance.requisition, field: "dateRequested")}
                                        </td>
                                        <td>
                                            ${fieldValue(bean: requisitionItemInstance.product, field: "productCode")}
                                        </td>

                                        <td>
                                            <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItemInstance.product.id}">
                                                ${fieldValue(bean: requisitionItemInstance, field: "product")}
                                            </g:link>
                                        </td>

                                        <td>${fieldValue(bean: requisitionItemInstance, field: "cancelReasonCode")}</td>

                                        <td>${fieldValue(bean: requisitionItemInstance, field: "cancelComments")}</td>

                                        <td>${fieldValue(bean: requisitionItemInstance, field: "quantityApproved")}</td>
                                        <td>${fieldValue(bean: requisitionItemInstance, field: "quantityCanceled")}</td>

                                        <td>${fieldValue(bean: requisitionItemInstance, field: "quantity")}</td>
                                        <td>
                                            <g:link action="edit" id="${requisitionItemInstance.id}" class="button">
                                                <warehouse:message code="default.button.edit.label"/>
                                            </g:link>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </div>
                        <div class="paginateButtons">
                            <g:paginate total="${requisitionItemInstanceTotal}" params="${params}" />
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </body>
</html>
