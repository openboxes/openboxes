<%@ page import="org.pih.warehouse.requisition.RequisitionItemSortByCode; org.pih.warehouse.core.RoleType" %>
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
</head>

<body>
<div id="print-header" style="line-height: 40px;">
    <span class="title"><warehouse:message code="picklist.print.label"/></span>
    <span style="float: right;">
        <button type="button" id="print-button" onclick="window.print()">
            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}"/>
            ${warehouse.message(code: "default.button.print.label")}
        </button>
        &nbsp;
        <a href="javascript:window.close();">Close</a>
    </span>
    <hr/>
</div>

<div class="clear"></div>

<div class="right">
    <table style="width:auto;" border="0">
        <tr class="header">
            <td>
                <label><warehouse:message code="requisition.origin.label"/>:</label>
            </td>
            <td class="right">
                ${requisition.origin?.name}
            </td>
        </tr>
        <tr class="header">
            <td>
                <label><warehouse:message code="requisition.destination.label"/>:</label>
            </td>
            <td class="right">
                ${requisition.destination?.name}
            </td>
        </tr>

        <tr class="header">
            <td>
                <label><warehouse:message code="requisition.date.label"/>:</label>
            </td>
            <td class="right">
                <g:formatDate
                        date="${requisition?.dateRequested}" format="MMM d, yyyy  hh:mma"/>
            </td>
        </tr>
        <tr class="header">
            <td>
                <label><warehouse:message code="picklist.datePrinted.label" default="Date printed"/>:</label>
            </td>
            <td class="right">
                <g:formatDate
                        date="${new Date()}" format="MMM d, yyyy hh:mma"/>
            </td>
        </tr>

    </table>
</div>

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
                <h3>${requisition.requestNumber} | ${requisition?.name }</h3>
            </div>
            <%--
            <div class="header">
                <h3>${requisition?.destination?.name}</h3>
            </div>
            --%>
            <div class="header">
            </div>

            <div class="header">
                <g:if test="${requisition.requestNumber}">
                    <img src="${createLink(controller: 'product', action: 'barcode', params: [data: requisition?.requestNumber, width: 100, height: 30, format: 'CODE_128'])}"/>
                </g:if>
            </div>
        </td>
    </tr>

</table>


<div class="clear"></div>

<table class="signature-table" border="0">
    <tr class="theader">
        <td width="15%"></td>
        <td width="20%"><warehouse:message code="default.name.label"/></td>
        <td width="40%"><warehouse:message code="default.signature.label"/></td>
        <td width="15%" class="center"><warehouse:message code="default.date.label"/></td>
        <td width="10%" class="center"><warehouse:message code="default.time.label"/></td>
    </tr>
    <tr>
        <td class="right">
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
        <td class="right middle">
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
        <td class="right">
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
        <td class="right">
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
        <td class="middle right">
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

<div class="clear"></div>

<g:set var="sortByCode" value='${requisition?.sortByCode ?: RequisitionItemSortByCode.SORT_INDEX}'/>
<g:set var="requisitionItems" value='${requisition?."$sortByCode.methodName"}'/>
<g:set var="requisitionItems" value='${requisitionItems.findAll { !it.isCanceled()&&!it.isChanged() }}'/>
<g:set var="requisitionItemsColdChain" value='${requisitionItems.findAll { it?.product?.coldChain }}'/>
<g:set var="requisitionItemsControlled" value='${requisitionItems.findAll {it?.product?.controlledSubstance}}'/>
<g:set var="requisitionItemsHazmat" value='${requisitionItems.findAll {it?.product?.hazardousMaterial}}'/>
<g:set var="requisitionItemsOther" value='${requisitionItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

<div>
    <g:if test="${requisitionItemsColdChain}">
        <h2>
            <img src="${resource(dir: 'images/icons/', file: 'coldchain.gif')}" title="Cold chain"/>&nbsp;
            ${warehouse.message(code:'product.coldChain.label', default:'Cold chain')}
        </h2>
        <g:render template="printPage" model="[requisitionItems:requisitionItemsColdChain, pageBreakAfter: (requisitionItemsControlled||requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsControlled}">
        <h2>
            <img src="${resource(dir: 'images/icons/silk', file: 'error.png')}" title="Controlled substance"/>&nbsp;
            ${warehouse.message(code:'product.controlledSubstance.label', default:'Controlled substance')}
        </h2>
        <g:render template="printPage" model="[requisitionItems:requisitionItemsControlled, pageBreakAfter: (requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsHazmat}">
        <h2>
            <img src="${resource(dir: 'images/icons/silk', file: 'exclamation.png')}" title="Hazardous material"/>&nbsp;
            ${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous material')}

        </h2>
        <g:render template="printPage" model="[requisitionItems:requisitionItemsHazmat, pageBreakAfter: (requisitionItemsOther)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsOther}">
        <h2>
            <img src="${resource(dir: 'images/icons/silk', file: 'package.png')}" title="Everything else"/>&nbsp;
            ${warehouse.message(code:'default.everythingElse.label', default:'Everything else')}
        </h2>
        <g:render template="printPage" model="[requisitionItems:requisitionItemsOther, pageBreakAfter: 'avoid']"/>
    </g:if>
</div>

<script>
    $(document).ready(function () {
        $('.nailthumb-container').nailthumb({ width: 100, height: 60 });
        $('.nailthumb-container-100').nailthumb({ width: 100, height: 100 });
    });
</script>

</body>
</html>
