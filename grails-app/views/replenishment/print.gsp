<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <meta name="layout" content="print"/>
    <link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'print.css')}" type="text/css"
          media="print, screen, projection"/>
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
            <a href="javascript:window.close();" class="button">
                ${warehouse.message(code: "default.button.close.label", default: 'Close')}
            </a>
        </div>
    </div>
    <h1 class="title"><warehouse:message code="inventory.printStockTransfer.label"/></h1>
    <hr/>
</div>

<g:set var="enablePageBreak" value="${params.pageBreak != 'Disable page break'}"/>

<table border="0">
    <tr>
        <td width="1%">
            <div class="requisition-header cf-header" style="margin-bottom: 20px;">
                <g:displayReportLogo/>
            </div>
        </td>
        <td>
            <div class="header">
                <h1><warehouse:message code="order.transferOrder.label"/></h1>
            </div>
        </td>
        <td class="top">
            <table border="0">
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="order.orderNumber.label"/>:</label>
                    </td>
                    <td>
                        ${headerItems.orderNumber}
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="default.createdBy.label"/>:</label>
                    </td>
                    <td>
                        ${headerItems.createdBy}
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="default.dateCreated.label"/>:</label>
                    </td>
                    <td>
                        <g:formatDate date="${headerItems?.dateCreated}" format="MM/dd/yyyy"/>
                    </td>
                </tr>
            </table>
        </td>
    </tr>

</table>

<g:set var="lineItemsByZone" value='${lineItems?.groupBy { it?.originBinLocation?.zone?.name } ?: [:]}'/>

<g:set var="allPickListItems" value='${lineItems*.retrievePicklistItems()?.flatten()}'/>
<g:set var="zoneNames" value='${allPickListItems?.collect { it?.binLocation?.zone?.name }?.unique()?.sort{ a, b -> !a ? !b ? 0 : 1 : !b ? -1 : a <=> b }}'/>
<g:set var="pickListItemsByZone" value='${allPickListItems?.groupBy { it?.binLocation?.zone?.name } ?: [:]}'/>

<g:each var="zoneName" status="i" in="${zoneNames}">

    <g:set var="zoneLineItems" value='${lineItemsByZone[zoneName] ?: [:]}'/>
    <g:set var="zoneLineItemsColdChain" value='${zoneLineItems.findAll { it?.product?.coldChain }}'/>
    <g:set var="zoneLineItemsControlled" value='${zoneLineItems.findAll {it?.product?.controlledSubstance}}'/>
    <g:set var="zoneLineItemsHazmat" value='${zoneLineItems.findAll {it?.product?.hazardousMaterial}}'/>
    <g:set var="zoneLineItemsOther" value='${zoneLineItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

    <g:set var="showZoneName" value='${zoneName || zoneNames.size() > 1}'/>

    <g:set var="pickListItemsByOrder" value='${pickListItemsByZone[zoneName]?.groupBy { it?.orderItem?.id } ?: [:]}'/>
    <h1 class="subtitle">
        ${zoneName ?: g.message(code: 'location.noZone.label', default: 'No zone')}
    </h1>
    <div class="page" style="page-break-after: ${enablePageBreak && showZoneName && i < zoneNames.size() - 1 ? 'always':'avoid'};">
        <g:if test="${showZoneName}">
            <h1 class="subtitle">
                ${zoneName ?: g.message(code: 'location.noZone.label', default: 'No zone')}
            </h1>
        </g:if>

        <g:if test="${zoneLineItemsColdChain}">`
            <g:set var="groupName" value="${g.message(code:'product.coldChain.label', default:'Cold Chain')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (zoneLineItemsControlled||zoneLineItemsHazmat||zoneLineItemsOther) ? 'always':'avoid'}"/>
            <g:render template="printPage" model="[lineItems:zoneLineItemsColdChain, groupName: groupName, picklist:picklist, pickListItemsByOrder: pickListItemsByOrder, pageBreakAfter: pageBreakAfter]"/>
        </g:if>
        <g:if test="${zoneLineItemsControlled}">
            <g:set var="groupName" value="${g.message(code:'product.controlledSubstance.label', default:'Controlled Substance')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (zoneLineItemsHazmat||zoneLineItemsOther)?'always':'avoid'}"/>
            <g:render template="printPage" model="[lineItems:zoneLineItemsControlled, groupName: groupName, picklist:picklist, pickListItemsByOrder: pickListItemsByOrder, pageBreakAfter: pageBreakAfter]"/>
        </g:if>
        <g:if test="${zoneLineItemsHazmat}">
            <g:set var="groupName" value="${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous Material')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (zoneLineItemsOther)?'always':'avoid'}"/>
            <g:render template="printPage" model="[lineItems:zoneLineItemsHazmat, groupName: groupName, picklist:picklist, pickListItemsByOrder: pickListItemsByOrder, pageBreakAfter: pageBreakAfter]"/>
        </g:if>
        <g:if test="${zoneLineItemsOther}">
            <g:set var="groupName" value="${warehouse.message(code:'product.generalGoods.label', default:'General Goods')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName ?'always':'avoid'}"/>
            <g:render template="printPage" model="[lineItems:zoneLineItemsOther, groupName: groupName, picklist:picklist, pickListItemsByOrder: pickListItemsByOrder, pageBreakAfter:pageBreakAfter]"/>
        </g:if>
    </div>

</g:each>

<table class="signature-table" style="border: 0px solid lightgrey;">
    <tr class="theader">
        <th width="15%"></th>
        <th width="20%"><warehouse:message code="default.name.label"/></th>
        <th width="40%"><warehouse:message code="default.signature.label"/></th>
        <th width="15%" class="center"><warehouse:message code="default.date.label"/></th>
    </tr>
    <tr>
        <td class="middle">
            <label><warehouse:message code="order.completedBy.label"/></label>
        </td>
        <td>
        </td>
        <td>
        </td>
        <td>
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
