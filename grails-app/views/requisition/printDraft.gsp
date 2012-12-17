<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="print" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'print.css')}" type="text/css" media="print, screen, projection" />
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
    <content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
</head>
<body>
    <button type="button" id="print-button" onclick="window.print()">
        <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
        ${warehouse.message(code:"default.print.label")}
    </button>
    <div class="header">
        <warehouse:message code="requisition.ward.label"/>: ${requisition.origin?.name}
    </div>
    <div class="header">
        <warehouse:message code="requisition.date.label"/>: <g:formatDate date="${requisition?.dateRequested}" format="dd/MMM/yyyy"/>
    </div>
    <div class="title">
        <g:if test="${location?.logo }">
            <img id="logo" src="${createLink(controller:'location', action:'viewLogo', id:location.id)}" />                                    
        </g:if>
        <g:else>
            <img id="logo" src="${createLinkTo(dir:'images/', file:'hands.jpg')}" />
        </g:else>
            ${requisition?.name}
    </div>
    <table id="signature-table">
        <tr class="theader">
            <td> </td>
            <td><warehouse:message code="default.name.label"/></td>
            <td><warehouse:message code="default.signature.label"/></td>
            <td><warehouse:message code="default.date.label"/></td>
        </tr>
        <tr>
            <td><warehouse:message code="requisition.requestedBy.label"/></td>
            <td>${requisition?.requestedBy?.name}</td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td><warehouse:message code="requisition.fulfilledBy.label"/></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
    </table>
    <table id="requisition-items">
            <tr class="theader">
                <th><warehouse:message code="report.number.label"/></th>
                <th>${warehouse.message(code: 'product.label')}</th>
                <th>${warehouse.message(code: 'requisitionItem.quantityRequested.label')}?</th>
                <th>${warehouse.message(code: 'requisitionItem.quantityPicked.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
            </tr>

            <g:each in="${requisition?.requisitionItems}" status="i" var="requisitionItem">
                <g:if test="${picklist}">
                    <g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItems()}" />
                    <g:set var="numInventoryItem" value="${picklistItems?.size() ?: 1}" />
                </g:if>
                <g:else>
                    <g:set var="numInventoryItem" value="${requisitionItem?.calculateNumInventoryItem() ?: 1}" />
                </g:else>
                <g:set var="j" value="${0}"/>
                <g:while test="${j < numInventoryItem}">
                <tr>
                    <g:if test="${j == 0}">
                        <td rowspan="${numInventoryItem}" class="center">${i+1}</td>
                        <td rowspan="${numInventoryItem}">${requisitionItem?.product?.name}</td>
                        <td rowspan="${numInventoryItem}">${requisitionItem?.quantity}</td>
                    </g:if>
                    <td>${picklistItems ? picklistItems[j].quantity:""}</td>
                    <td>${picklistItems ? picklistItems[j].inventoryItem?.lotNumber:""}</td>
                    <td>${picklistItems ? picklistItems[j].inventoryItem?.expirationDate:""}</td>
                    <%j++%>
                </tr>
                </g:while>
            </g:each>

    </table>
    <p><warehouse:message code="requisitionItem.comment.label"/>:</p>
    <div id="comment-box">

    </div>
</body>
</html>
