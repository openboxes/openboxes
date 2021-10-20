<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <meta name="layout" content="print"/>
    <link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'print.css')}" type="text/css"
          media="print, screen, projection"/>
    <g:set var="entityName" value="${warehouse.message(code: 'order.label', default: 'Order')}"/>
    <title><warehouse:message code="default.show.label" args="[entityName]"/></title>
    <script src="${createLinkTo(dir: 'js/jquery.nailthumb', file: 'jquery.nailthumb.1.1.js')}"
            type="text/javascript"></script>
    <link rel="stylesheet" href="${createLinkTo(dir: 'js/jquery.nailthumb', file: 'jquery.nailthumb.1.1.css')}"
          type="text/css" media="all"/>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'buttons.css')}" type="text/css" media="all" />

</head>
<body>
<div id="print-header">
    <div class="right button-container">
        <g:select id="select-page-break" name="pageBreak" from="['', 'Disable page break', 'Enable page break']" value="${params.pageBreak}" />
        <div class="button-group">
            <a href="javascript:window.print()" type="button" id="print-button" onclick="window.print();"  class="button">
                ${warehouse.message(code: "default.button.print.label", default: 'Print')}
            </a>
            <g:link controller="picklist" action="renderReturnPdf" id="${order?.id}" class="button" params="[sorted: sorted]">
                ${warehouse.message(code: "default.button.download.label", default: 'Download')}
            </g:link>
            <a href="javascript:window.close();" class="button">
                ${warehouse.message(code: "default.button.close.label", default: 'Close')}
            </a>
        </div>
    </div>
    <h1 class="title"><warehouse:message code="picklist.print.label"/></h1>
    <hr/>
</div>

<g:set var="enablePageBreak" value="${params.pageBreak != 'Disable page break'}"/>

<table border="0">
    <tr>
        <td width="1%">
            <div class="order-header cf-header" style="margin-bottom: 20px;">
                <g:displayReportLogo/>
            </div>
        </td>
        <td>
            <div class="header">
                <h1><warehouse:message code="picklist.label"/></h1>
            </div>
            <div class="header">
                ${order.orderNumber} - ${order?.name }
            </div>
            <div class="header">
            </div>

            <div class="header">
                <g:if test="${order.orderNumber}">
                    <img src="${createLink(controller: 'product', action: 'barcode', params: [data: order?.orderNumber, width: 100, height: 30, format: 'CODE_128'])}"/>
                </g:if>
            </div>
        </td>
        <td class="top">
            <table border="0">
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="order.origin.label"/>:</label>
                    </td>
                    <td>
                        ${order.origin?.name}
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="order.destination.label"/>:</label>
                    </td>
                    <td>
                        ${order.destination?.name}
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="picklist.datePrinted.label" default="Date printed"/>:</label>
                    </td>
                    <td>
                        <g:formatDate
                                date="${new Date()}" format="MMM d, yyyy hh:mma"/>
                    </td>
                </tr>

            </table>
        </td>
    </tr>

</table>

<g:set var="allOrderItems" value='${order.orderItems.sort { it.product.name }}'/>
<g:set var="allPickListItems" value='${allOrderItems*.retrievePicklistItems()?.flatten()}'/>
<g:set var="zoneNames" value='${allPickListItems?.collect { it?.binLocation?.zone?.name }?.unique()?.sort{ a, b -> !a ? !b ? 0 : 1 : !b ? -1 : a <=> b }}'/>
<g:set var="pickListItemsByZone" value='${allPickListItems?.groupBy { it?.binLocation?.zone?.name } ?: [:]}'/>

<g:each var="zoneName" status="i" in="${zoneNames}">

    <g:set var="pickListItemsByOrder" value='${pickListItemsByZone[zoneName]?.groupBy { it?.orderItem?.id } ?: [:]}'/>

    <g:if test="${!zoneName}">
        <g:set var="orderItems" value='${allOrderItems.findAll { !it.picklistItems?.size() || pickListItemsByOrder[it.id]?.size() }}'/>
    </g:if>
    <g:else>
        <g:set var="orderItems" value='${allOrderItems.findAll { pickListItemsByOrder[it.id]?.size() }}'/>
    </g:else>

    <g:set var="orderItemsColdChain" value='${orderItems.findAll { it?.product?.coldChain }}'/>
    <g:set var="orderItemsControlled" value='${orderItems.findAll {it?.product?.controlledSubstance}}'/>
    <g:set var="orderItemsHazmat" value='${orderItems.findAll {it?.product?.hazardousMaterial}}'/>
    <g:set var="orderItemsOther" value='${orderItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

    <g:set var="showZoneName" value='${zoneName || zoneNames.size() > 1}'/>

    <div class="page" style="page-break-after: ${enablePageBreak && showZoneName && i < zoneNames.size() - 1 ? 'always':'avoid'};">
        <g:if test="${showZoneName}">
            <h1 class="subtitle">
                ${zoneName ?: g.message(code: 'location.noZone.label', default: 'No zone')}
            </h1>
        </g:if>

        <g:if test="${orderItemsColdChain}">
            <g:set var="groupName" value="${g.message(code:'product.coldChain.label', default:'Cold Chain')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (orderItemsControlled||orderItemsHazmat||orderItemsOther) ? 'always':'avoid'}"/>
            <g:render template="returnPrintPage" model="[orderItems:orderItemsColdChain, pickListItemsByOrder: pickListItemsByOrder, groupName: groupName, location:location, pageBreakAfter: pageBreakAfter, sorted:sorted]"/>
        </g:if>
        <g:if test="${orderItemsControlled}">
            <g:set var="groupName" value="${g.message(code:'product.controlledSubstance.label', default:'Controlled Substance')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (orderItemsHazmat||orderItemsOther)?'always':'avoid'}"/>
            <g:render template="returnPrintPage" model="[orderItems:orderItemsControlled, pickListItemsByOrder: pickListItemsByOrder, groupName: groupName, location:location, pageBreakAfter: pageBreakAfter, sorted:sorted]"/>
        </g:if>
        <g:if test="${orderItemsHazmat}">
            <g:set var="groupName" value="${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous Material')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (orderItemsOther)?'always':'avoid'}"/>
            <g:render template="returnPrintPage" model="[orderItems:orderItemsHazmat, pickListItemsByOrder: pickListItemsByOrder, groupName: groupName, location:location, pageBreakAfter: pageBreakAfter, sorted:sorted]"/>
        </g:if>
        <g:if test="${orderItemsOther}">
            <g:set var="groupName" value="${warehouse.message(code:'product.generalGoods.label', default:'General Goods')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName?'always':'avoid'}"/>
            <g:render template="returnPrintPage" model="[orderItems:orderItemsOther, pickListItemsByOrder: pickListItemsByOrder, groupName: groupName, location:location, pageBreakAfter:pageBreakAfter, sorted:sorted]"/>
        </g:if>
    </div>

</g:each>

<table class="signature-table" style="border: 0px solid lightgrey;">
    <tr class="theader">
        <th width="15%"></th>
        <th width="20%"><warehouse:message code="default.name.label"/></th>
        <th width="40%"><warehouse:message code="default.signature.label"/></th>
        <th width="15%" class="center"><warehouse:message code="default.date.label"/></th>
        <th width="10%" class="center"><warehouse:message code="default.time.label"/></th>
    </tr>
    <tr>
        <td class="middle">
            <label><warehouse:message code="order.orderedBy.label"/></label>
        </td>
        <td class="middle">
            ${order?.orderedBy?.name}
        </td>
        <td>

        </td>
        <td class="middle center">
            <g:formatDate date="${order?.dateOrdered}" format="MMM d, yyyy"/>
        </td>
        <td class="middle center">
            <g:formatDate date="${order?.dateOrdered}" format="hh:mma"/>
        </td>
    </tr>
    <tr>
        <td class="middle">
            <label><warehouse:message code="order.createdBy.label"/></label>
        </td>
        <td class="middle">
            ${order?.createdBy?.name}
        </td>
        <td>

        </td>
        <td class="middle center">
            <g:formatDate date="${order?.dateCreated}" format="MMM d, yyyy"/>
        </td>
        <td class="middle center">
            <g:formatDate date="${order?.dateCreated}" format="hh:mma"/>
        </td>
    </tr>
</table>
<script>
    $(document).ready(function () {
        $('.nailthumb').nailthumb({ width: 100, height: 60 });
        $('.nailthumb-100').nailthumb({ width: 100, height: 100 });

        $("#select-page-break").change(function() {
            var selected = this.value;
            if ('URLSearchParams' in window) {
              var searchParams = new URLSearchParams(window.location.search);
              searchParams.set("pageBreak", selected);
              window.location.search = searchParams.toString();
            }
        });
    });
</script>

</body>
</html>
