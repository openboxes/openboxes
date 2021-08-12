<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <meta name="layout" content="print"/>
    <link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'print.css')}" type="text/css"
          media="print, screen, projection"/>
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}"/>
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
            <g:link controller="picklist" action="renderPdf" id="${requisition?.id}" class="button" params="[sorted: sorted]">
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
            <div class="requisition-header cf-header" style="margin-bottom: 20px;">
                <g:displayReportLogo/>
            </div>
        </td>
        <td>
            <div class="header">
                <h1><warehouse:message code="picklist.label"/></h1>
            </div>
            <div class="header">
                ${requisition.requestNumber} - ${requisition?.name }
            </div>
            <div class="header">
            </div>

            <div class="header">
                <g:if test="${requisition.requestNumber}">
                    <img src="${createLink(controller: 'product', action: 'barcode', params: [data: requisition?.requestNumber, width: 100, height: 30, format: 'CODE_128'])}"/>
                </g:if>
            </div>
        </td>
        <td class="top">
            <table border="0">
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="requisition.requisitionNumber.label"/>:</label>
                    </td>
                    <td>
                        ${requisition.requestNumber}
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="requisition.requisitionType.label"/>:</label>
                    </td>
                    <td>
                        <format:metadata obj="${requisition.type}"/>
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="requisition.origin.label"/>:</label>
                    </td>
                    <td>
                        ${requisition.origin?.name}
                    </td>
                </tr>
                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="requisition.destination.label"/>:</label>
                    </td>
                    <td>
                        ${requisition.destination?.name}
                    </td>
                </tr>

                <tr class="header">
                    <td class="name right">
                        <label><warehouse:message code="requisition.date.label"/>:</label>
                    </td>
                    <td>
                        <g:formatDate
                                date="${requisition?.dateRequested}" format="MMM d, yyyy  hh:mma"/>
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

<g:set var="allRequisitionItems" value='${requisition.requisitionItems.sort { it.product.name }}'/>
<g:set var="allPickListItems" value='${allRequisitionItems*.retrievePicklistItems()?.flatten()}'/>
<g:set var="zoneNames" value='${allPickListItems?.collect { it?.binLocation?.zone?.name }?.unique()?.sort{ a, b -> !a ? !b ? 0 : 1 : !b ? -1 : a <=> b }}'/>
<g:set var="pickListItemsByZone" value='${allPickListItems?.groupBy { it?.binLocation?.zone?.name } ?: [:]}'/>

<g:each var="zoneName" status="i" in="${zoneNames}">

    <g:set var="pickListItemsByRequisition" value='${pickListItemsByZone[zoneName]?.groupBy { it?.requisitionItem?.id } ?: [:]}'/>

    <g:if test="${!zoneName}">
        <g:set var="requisitionItems" value='${allRequisitionItems.findAll { !it.picklistItems?.size() || pickListItemsByRequisition[it.id]?.size() }}'/>
    </g:if>
    <g:else>
        <g:set var="requisitionItems" value='${allRequisitionItems.findAll { pickListItemsByRequisition[it.id]?.size() }}'/>
    </g:else>

    <g:set var="requisitionItemsCanceled" value='${requisitionItems.findAll { it.isCanceled()}}'/>
    <g:set var="requisitionItems" value='${requisitionItems.findAll { !it.isCanceled()&&!it.isChanged() }}'/>
    <g:set var="requisitionItemsColdChain" value='${requisitionItems.findAll { it?.product?.coldChain }}'/>
    <g:set var="requisitionItemsControlled" value='${requisitionItems.findAll {it?.product?.controlledSubstance}}'/>
    <g:set var="requisitionItemsHazmat" value='${requisitionItems.findAll {it?.product?.hazardousMaterial}}'/>
    <g:set var="requisitionItemsOther" value='${requisitionItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

    <g:set var="showZoneName" value='${zoneName || zoneNames.size() > 1}'/>

    <div class="page" style="page-break-after: ${enablePageBreak && showZoneName && i < zoneNames.size() - 1 ? 'always':'avoid'};">
        <g:if test="${showZoneName}">
            <h1 class="subtitle">
                ${zoneName ?: g.message(code: 'location.noZone.label', default: 'No zone')}
            </h1>
        </g:if>

        <g:if test="${requisitionItemsColdChain}">
            <g:set var="groupName" value="${g.message(code:'product.coldChain.label', default:'Cold Chain')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (requisitionItemsControlled||requisitionItemsHazmat||requisitionItemsOther) ? 'always':'avoid'}"/>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsColdChain, pickListItemsByRequisition: pickListItemsByRequisition, groupName: groupName, location:location, pageBreakAfter: pageBreakAfter, sorted:sorted]"/>
        </g:if>
        <g:if test="${requisitionItemsControlled}">
            <g:set var="groupName" value="${g.message(code:'product.controlledSubstance.label', default:'Controlled Substance')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid'}"/>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsControlled, pickListItemsByRequisition: pickListItemsByRequisition, groupName: groupName, location:location, pageBreakAfter: pageBreakAfter, sorted:sorted]"/>
        </g:if>
        <g:if test="${requisitionItemsHazmat}">
            <g:set var="groupName" value="${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous Material')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (requisitionItemsOther)?'always':'avoid'}"/>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsHazmat, pickListItemsByRequisition: pickListItemsByRequisition, groupName: groupName, location:location, pageBreakAfter: pageBreakAfter, sorted:sorted]"/>
        </g:if>
        <g:if test="${requisitionItemsOther}">
            <g:set var="groupName" value="${warehouse.message(code:'product.generalGoods.label', default:'General Goods')}"/>
            <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName && (requisitionItemsCanceled)?'always':'avoid'}"/>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsOther, pickListItemsByRequisition: pickListItemsByRequisition, groupName: groupName, location:location, pageBreakAfter:pageBreakAfter, sorted:sorted]"/>
        </g:if>
        <g:if test="${requisitionItemsCanceled}">
            <g:set var="groupName" value="${warehouse.message(code:'canceled.canceled.label', default:'Canceled')}"/>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsCanceled, pickListItemsByRequisition: pickListItemsByRequisition, groupName: groupName, location:location, pageBreakAfter: 'avoid', sorted:sorted]"/>
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
            <label><warehouse:message code="requisition.requestedBy.label"/></label>
        </td>
        <td class="middle">
            ${requisition?.requestedBy?.name}
        </td>
        <td>

        </td>
        <td class="middle center">
            <g:formatDate date="${requisition?.dateRequested}" format="MMM d, yyyy"/>
        </td>
        <td class="middle center">
            <g:formatDate date="${requisition?.dateRequested}" format="hh:mma"/>
        </td>
    </tr>
    <tr>
        <td class="middle">
            <label><warehouse:message code="requisition.createdBy.label"/></label>
        </td>
        <td class="middle">
            ${requisition?.createdBy?.name}
        </td>
        <td>

        </td>
        <td class="middle center">
            <g:formatDate date="${requisition?.dateCreated}" format="MMM d, yyyy"/>
        </td>
        <td class="middle center">
            <g:formatDate date="${requisition?.dateCreated}" format="hh:mma"/>
        </td>
    </tr>
    <tr>
        <td class="middle">
            <label><warehouse:message code="requisition.verifiedBy.label"/></label>
        </td>
        <td class="middle">
            ${requisition?.verifiedBy?.name}
        </td>
        <td>

        </td>
        <td class="middle center">
            <g:formatDate date="${requisition?.dateVerified}" format="MMM d, yyyy"/>
        </td>
        <td class="middle center">
            <g:formatDate date="${requisition?.dateVerified}" format="hh:mma"/>
        </td>
    </tr>
    <tr>
        <td class="middle">
            <label><warehouse:message code="requisition.pickedBy.label"/></label>
        </td>
        <td class="middle">
            ${picklist?.picker?.name}
        </td>
        <td>

        </td>
        <td class="middle center">
            <g:formatDate date="${picklist?.datePicked}" format="MMM d, yyyy"/>
        </td>
        <td class="middle center">
            <g:formatDate date="${picklist?.datePicked}" format="hh:mma"/>
        </td>
    </tr>
    <tr>
        <td class="middle">
            <label><warehouse:message code="requisition.reviewedBy.label" default="Checked by"/></label>
        </td>
        <td class="middle">
            ${requisition?.checkedBy?.name}
        </td>
        <td>

        </td>
        <td class="middle center">
            <g:formatDate date="${requisition?.dateChecked}" format="MMM d, yyyy"/>
        </td>
        <td class="middle center">
            <g:formatDate date="${requisition?.dateChecked}" format="hh:mma"/>
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
