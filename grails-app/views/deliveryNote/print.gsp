<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" %>
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

    <style>
    .cf-header {
        overflow: auto;
        width: 100%
    }
        .signature-table tr { height: 40px;  }
    </style>
</head>

<body>
<div id="print-header" style="line-height: 40px">
    <span class="title"><warehouse:message code="deliveryNote.label" default="Delivery note"/></span>
    <div style="float: right;">

        <div class="button-group">
            <a href="" id="print-button" onclick="window.print()" class="button">
                ${warehouse.message(code: "default.button.print.label", default:"Print")}
            </a>

            <a href="javascript:window.close();" class="button">
                ${warehouse.message(code: "default.button.close.label")}
            </a>
        </div>
    </div>
    <hr/>
</div>

<table border="0">
    <tr>
        <td width="1%">
            <div class="requisition-header cf-header" style="margin-bottom: 20px;">
                <div class="print-logo nailthumb-container" style="float: left;">
                    <img src="${createLinkTo(dir: 'images/', file: 'hands.jpg')}"/>
                </div>
            </div>
        </td>
        <td>
            <div class="header">
                <h1><warehouse:message code="requisition.deliveryNote.label"/></h1>
                <h3>${requisition?.requestNumber} - ${requisition?.name }</h3>

            </div>

            <div class="header">
                <g:if test="${requisition.requestNumber}">
                    <img src="${createLink(controller: 'product', action: 'barcode', params: [data: requisition?.requestNumber, width: 100, height: 30, format: 'CODE_128'])}"/>
                </g:if>
            </div>
        </td>
        <td>
            <table>

                <tr>
                    <td class="name right">
                        <label><warehouse:message code="requisition.origin.label"/>:</label>
                    </td>
                    <td>
                        ${requisition.origin?.name}
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="requisition.destination.label"/>:</label>
                    </td>
                    <td>
                        ${requisition.destination?.name}
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="requisition.date.label"/>:</label>
                    </td>
                    <td>
                        <g:formatDate date="${requisition?.dateRequested}" format="d MMMMM yyyy  hh:mma"/>
                    </td>
                </tr>
                <tr>
                    <td class="name right">
                        <label><warehouse:message code="picklist.datePrinted.label" default="Date printed"/>:</label>
                    </td>
                    <td>
                        <g:formatDate date="${new Date()}" format="d MMMMM yyyy hh:mma"/>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>

<hr/>
<br/>
<div class="clear"></div>

<g:set var="requisitionItems" value='${requisition.requisitionItems.sort { it.product.name }}'/>
<g:set var="requisitionItemsCanceled" value='${requisitionItems.findAll { it.isCanceled()}}'/>
<g:set var="requisitionItems" value='${requisitionItems.findAll { !it.isCanceled()&&!it.isChanged() }}'/>
<g:set var="requisitionItemsColdChain" value='${requisitionItems.findAll { it?.product?.coldChain }}'/>
<g:set var="requisitionItemsControlled" value='${requisitionItems.findAll {it?.product?.controlledSubstance}}'/>
<g:set var="requisitionItemsHazmat" value='${requisitionItems.findAll {it?.product?.hazardousMaterial}}'/>
<g:set var="requisitionItemsOther" value='${requisitionItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

<div>
    <g:if test="${requisitionItemsColdChain}">
        <h2>
            <img src="${resource(dir: 'images/icons/', file: 'coldchain.gif')}" title="Cold chain"/>
            ${warehouse.message(code:'product.coldChain.label', default:'Cold chain')}
        </h2>
        <g:render template="printPage" model="[requisitionItems:requisitionItemsColdChain,
                                               pageBreakAfter: (requisitionItemsControlled||requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsControlled}">
        <h2>
            <img src="${resource(dir: 'images/icons/silk', file: 'error.png')}" title="Controlled Substance"/>
            ${warehouse.message(code:'product.controlledSubstance.label', default:'Controlled Substance')}
        </h2>
        <g:render template="printPage" model="[requisitionItems:requisitionItemsControlled,
                                               pageBreakAfter: (requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsHazmat}">
        <h2>
            <img src="${resource(dir: 'images/icons/silk', file: 'exclamation.png')}" title="Hazardous Material"/>
            ${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous Material')}
        </h2>
        <g:render template="printPage" model="[requisitionItems:requisitionItemsHazmat,
                                               pageBreakAfter: (requisitionItemsOther)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsOther}">
        <h2>
            <img src="${resource(dir: 'images/icons/silk', file: 'package.png')}" title="General Goods"/>&nbsp;
            ${warehouse.message(code:'product.generalGoods.label', default:'General Goods')}
        </h2>
        <g:render template="printPage" model="[requisitionItems:requisitionItemsOther,
                                               pageBreakAfter: (requisitionItemsCanceled)?'always':'avoid']"/>
    </g:if>
    <g:if test="${requisitionItemsCanceled}">
        <h2>
            <img src="${resource(dir: 'images/icons/silk', file: 'decline.png')}" title="Canceled"/>&nbsp;
        ${warehouse.message(code:'default.canceled.label', default:'Canceled Items')}
        </h2>
        <g:render template="printPage" model="[requisitionItems:requisitionItemsCanceled, location:location, pageBreakAfter: 'avoid']"/>
    </g:if>
    <table class="signature-table">
        <tr>
            <td width="33%" class="left">
                <warehouse:message code="deliveryNote.sentBy.label"/>
            </td>
            <td width="33%" class="center">
                <warehouse:message code="deliveryReceipt.signature.label"/>
            </td>
            <td width="33%" class="right">
                <warehouse:message code="deliveryReceipt.date.label"/>
            </td>
        </tr>
        <tr>
            <td class="left">
                <warehouse:message code="deliveryNote.approvedBy.label"/>
            </td>
            <td class="center">
                <warehouse:message code="deliveryReceipt.signature.label"/>
            </td>
            <td class="right">
                <warehouse:message code="deliveryReceipt.date.label"/>
            </td>
        </tr>
        <tr>
            <td class="left">
                <warehouse:message code="deliveryNote.deliveredBy.label"/>
            </td>
            <td class="center">
                <warehouse:message code="deliveryReceipt.signature.label"/>
            </td>
            <td class="right">
                <warehouse:message code="deliveryReceipt.date.label"/>
            </td>
        </tr>
        <tr>
            <td class="left">
                <warehouse:message code="deliveryNote.receivedBy.label"/>
            </td>
            <td class="center">
                <warehouse:message code="deliveryReceipt.signature.label"/>
            </td>
            <td class="right">
                <warehouse:message code="deliveryReceipt.date.label"/>
            </td>
        </tr>
        <tr>
            <td class="left">
                <warehouse:message code="deliveryNote.checkedBy.label"/>
            </td>
            <td class="center">
                <warehouse:message code="deliveryReceipt.signature.label"/>
            </td>
            <td class="right">
                <warehouse:message code="deliveryReceipt.date.label"/>
            </td>
        </tr>
    </table>

</div>


<script>
    $(document).ready(function () {
        $('.nailthumb-container').nailthumb({ width: 100, height: 100 });
        //window.print();
    });
</script>

</body>
</html>
