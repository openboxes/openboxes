<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
%{--    <meta name="layout" content="print"/>--}%
%{--    <link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'print.css')}" type="text/css"--}%
%{--          media="print, screen, projection"/>--}%
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}"/>
    <title><warehouse:message code="default.show.label" args="[entityName]"/></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'buttons.css')}" type="text/css" media="all" />

    <style>

    table {
        border-collapse: collapse;
        page-break-inside: auto;
        -fs-table-paginate: paginate;
        border-spacing: 0;
        margin: 5px;
    }
    thead {display: table-header-group;}
    tr {page-break-inside: avoid; page-break-after: auto;}
    td {vertical-align: top; }
    th { background-color: lightgrey; font-weight: bold;}
    body { font-size: 11px; }

    div.header {
        display: block;
        text-align: center;
        position: running(header);
    }
    div.footer {
        display: block;
        text-align: center;
        position: running(footer);
    }

    @page {
        size: letter;
        background: white;
        @top-center { content: element(header) }
        @bottom-center { content: element(footer) }
    }

    .small {font-size: xx-small;}
    .large { font-size: larger; }
    .line{border-bottom: 1px solid black}
    .page-start {
        -fs-page-sequence: start;
        page-break-before: avoid;
    }

    .page-content { page-break-after: avoid; }
    .page-header { page-break-before: avoid; }
    .break {page-break-after:always}
    .page:before { content: counter(page); }
    .pagecount:before { content: counter(pages); }
    body { font: 11px "lucida grande", verdana, arial, helvetica, sans-serif; }

    table {border-collapse: collapse; page-break-inside: auto;}
    thead {display: table-header-group;}

    table td, table th {
        padding: 5px;
        border: 1px solid lightgrey;
        vertical-align: middle;
    }
    .first-line {
        display: flex;
        justify-content: space-between;
    }

    .no-border-table td, .no-border-table th { border: 0 !important; }
    .m-0 { margin: 0 !important; }
    .m-5 { margin: 5px !important }
    .b-0 { border: 0 !important; }
    .b-t0 { border-top: 0 !important; }
    .b-r0 { border-right: 0 !important; }
    .b-b0 { border-bottom: 0 !important; }
    .b-l0 { border-left: 0 !important; }
    .no-padding { padding: 0 !important; }
    .w100 { width: 100% !important; }
    .no-wrap { white-space: nowrap; }
    .gray-background { background-color: #ddd !important; }
    .fixed-layout { table-layout: fixed; }
    .signature-table tr { height: 40px;  }
    .break-word { word-wrap: break-word; }

    .signature-table table {
        width: 100%;
        padding: 5px;
        border: 0;
        margin: auto;
        margin-bottom: 20px;
        margin-top: 20px;
    }

    .signature-table {
        width: 100%;
        margin: auto;
        margin-bottom: 20px;
        margin-top: 100px;
    }
    .signature-table tr, .signature-table td {
        border: 0px solid lightgrey;
        border-top: 1px solid lightgrey;
        height: 60px;
        vertical-align: top;
    }

    .top { vertical-align: top }
    .bottom { vertical-align: bottom }
    .right { text-align: right; }
    .center { text-align: center; }
    .left { text-align: left; }

    @media print {
        .print-button { display:none; }
        .print-header { display:none; }
    }
    .canceled {
        text-decoration: line-through;
    }
    </style>

</head>

<body>

    <div class="print-header">
        <table class="w100 fixed-layout no-border-table">
            <tr>
                <td>
                    <h1 class="m-0">${g.message(code: 'deliveryNote.button.print.label')}</h1>
                </td>
                <td class="right">
                    <div class="button-container" >
                        <a href="#" id="print-button" onclick="window.print()" class="button">
                            ${warehouse.message(code: "default.button.print.label", default:"Print")}
                        </a>

                        <a href="javascript:window.close();" class="button">
                            ${warehouse.message(code: "default.button.close.label")}
                        </a>
                    </div>
                </td>
            </tr>
        </table>
        <hr/>
    </div>

    <div id="header" class="header">
        <table class="w100 fixed-layout no-border-table">
            <tr>
                <%-- Icon and title --%>
                <td colspan="2" class="b-0">
                    <table class=" w100">
                        <tr>
                            <td class="left top" width="5%">
                                <g:displayReportLogo/>
                            </td>
                            <td class="left top">
                                <h1 class="m-5">${warehouse.message(code: 'requisition.deliveryNote.label')}</h1>
                                <div class="m-5 large">${requisition.requestNumber} ${requisition.name}</div>
                                <div class="m-0">
                                    <g:if test="${requisition.requestNumber}">
                                        <img src="${createLink(controller: 'product', action: 'barcode', params: [data: requisition?.requestNumber, height: 30, format: 'CODE_128'])}"/>
                                    </g:if>
                                </div>
                            </td>
                            <td class="right" width="25%">
                                <table class="w100 no-wrap">
                                    <tr>
                                        <td class="name">
                                            <label><warehouse:message code="requisition.origin.label"/>:</label>
                                        </td>
                                        <td>
                                            ${requisition.origin?.name}
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="name">
                                            <label><warehouse:message code="requisition.destination.label"/>:</label>
                                        </td>
                                        <td>
                                            ${requisition.destination?.name}
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="name">
                                            <label><warehouse:message code="requisition.requestedBy.label"/>:</label>
                                        </td>
                                        <td>
                                            ${requisition?.requestedBy?.name}
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="name">
                                            <label><warehouse:message code="requisition.date.label"/>:</label>
                                        </td>
                                        <td>
                                            <g:formatDate date="${requisition?.dateRequested}" format="d MMMMM yyyy  hh:mma"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="name">
                                            <label><warehouse:message code="deliveryNote.shipDate.label" default="Ship date"/>:</label>
                                        </td>
                                        <td>
                                            <g:formatDate date="${requisition?.shipment?.expectedShippingDate}" format="d MMMMM yyyy  hh:mma"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </div>

    <g:if test="${requisition.origin.address && requisition.destination.address}">
        <div id="address" class="page-content">
            <table class="w100 fixed-layout b-0">
                <tr>
                    <td class="b-0">
                        <h2>${g.message(code: 'deliveryNote.receivedFrom.label', default: 'Received From')}</h2>
                        <table class="no-border-table w100">
                            <tbody>
                                <tr>
                                    <td>
                                        <table class="w100 no-wrap left">
                                            <tr>
                                                <td>
                                                    <strong>${requisition.origin?.name}</strong>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    ${requisition.origin?.address?.address}
                                                </td>
                                            </tr>
                                            <g:if test="${requisition.origin?.address?.address2}">
                                                <tr>
                                                    <td>
                                                        ${requisition.origin?.address?.address2}
                                                    </td>
                                                </tr>
                                            </g:if>
                                            <tr>
                                                <td>
                                                    ${requisition?.origin?.address?.city}
                                                    ${requisition?.origin?.address?.stateOrProvince}
                                                    ${requisition?.origin?.address?.postalCode}
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    ${requisition?.origin?.address?.country}
                                                </td>
                                            </tr>

                                        </table>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </td>

                    <td class="b-0" class="top">
                        <h2>${g.message(code: 'deliveryNote.deliveredTo.label', default: 'Delivered To')}</h2>

                        <table class="no-border-table w100">
                            <tbody>
                                <tr>
                                    <td>
                                        <table class="w100 no-wrap">
                                            <tr>
                                                <td>
                                                    <strong>${requisition.destination?.name}</strong>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    ${requisition.destination?.address?.address}
                                                </td>
                                            </tr>
                                            <g:if test="${requisition.destination?.address?.address2}">
                                                <tr>
                                                    <td>
                                                        ${requisition.destination?.address?.address2}
                                                    </td>
                                                </tr>
                                            </g:if>
                                            <tr>
                                                <td>
                                                    ${requisition?.destination?.address?.city}
                                                    ${requisition?.destination?.address?.stateOrProvince}
                                                    ${requisition?.destination?.address?.postalCode}
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    ${requisition?.destination?.address?.country}
                                                </td>
                                            </tr>

                                        </table>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </g:if>

    <g:set var="requisitionItems" value='${requisition.requisitionItems.sort { it.product.name }}'/>
    <g:set var="requisitionItemsCanceled" value='${requisitionItems.findAll { it.isCanceled()}}'/>
    <g:set var="requisitionItems" value='${requisitionItems.findAll { !it.isCanceled()&&!it.isChanged() }}'/>
    <g:set var="requisitionItemsColdChain" value='${requisitionItems.findAll { it?.product?.coldChain }}'/>
    <g:set var="requisitionItemsControlled" value='${requisitionItems.findAll {it?.product?.controlledSubstance}}'/>
    <g:set var="requisitionItemsHazmat" value='${requisitionItems.findAll {it?.product?.hazardousMaterial}}'/>
    <g:set var="requisitionItemsOther" value='${requisitionItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

    <div class="content">
        <g:if test="${requisitionItemsColdChain}">
            <h2>
                ${warehouse.message(code:'product.coldChain.label', default:'Cold chain')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsColdChain,
                                                   pageBreakAfter: (requisitionItemsControlled||requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
        </g:if>
        <g:if test="${requisitionItemsControlled}">
            <h2 class="${requisitionItemsColdChain ? 'mt' : ''}">
                ${warehouse.message(code:'product.controlledSubstance.label', default:'Controlled Substance')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsControlled,
                                                   pageBreakAfter: (requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
        </g:if>
        <g:if test="${requisitionItemsHazmat}">
            <h2 class="${requisitionItemsControlled||requisitionItemsColdChain ? 'mt' : ''}">
                ${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous Material')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsHazmat,
                                                   pageBreakAfter: (requisitionItemsOther)?'always':'avoid']"/>
        </g:if>
        <g:if test="${requisitionItemsOther}">
            <h2 class="${requisitionItemsHazmat||requisitionItemsControlled||requisitionItemsColdChain ? 'mt' : ''}">
                ${warehouse.message(code:'product.generalGoods.label', default:'General Goods')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsOther,
                                                   pageBreakAfter: (requisitionItemsCanceled)?'always':'avoid']"/>
        </g:if>
        <g:if test="${requisitionItemsCanceled}">
            <h2 class="${requisitionItemsOther ? 'mt' : ''}">
                ${warehouse.message(code:'default.canceled.label', default:'Canceled Items')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsCanceled, location:location, pageBreakAfter: 'avoid']"/>
        </g:if>

        <table class="w100 fixed-layout b-0">
            <tr>
                    <h2><warehouse:message code="deliveryNote.notes.label" default="Notes"/></h2>
            </tr>
            <tr>
                <td>
                    <label><warehouse:message code="deliveryNote.trackingNumber.label" default="Tracking number"/>: </label>
                    ${requisition?.shipment?.referenceNumbers ? requisition?.shipment?.referenceNumbers?.first() : ''}
                </td>
            </tr>
            <tr>
                <td>
                    <label><warehouse:message code="deliveryNote.driverName.label" default="Driver name"/>: </label>
                    ${requisition?.shipment?.driverName?:''}
                </td>
            </tr>
            <tr>
                <td>
                    <label><warehouse:message code="deliveryNote.comments.label" default="Comments"/>: </label>
                    ${requisition?.shipment?.additionalInformation?:''}
                </td>
            </tr>
        </table>

        <table class="signature-table w100 fixed-layout">
            <tr>
                <td width="33%" align="left">
                    <warehouse:message code="deliveryNote.sentBy.label"/>
                </td>
                <td width="33%" align="center">
                    <warehouse:message code="deliveryReceipt.signature.label"/>
                </td>
                <td width="33%" align="right">
                    <warehouse:message code="deliveryReceipt.date.label"/>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <warehouse:message code="deliveryNote.approvedBy.label"/>
                </td>
                <td align="center">
                    <warehouse:message code="deliveryReceipt.signature.label"/>
                </td>
                <td align="right">
                    <warehouse:message code="deliveryReceipt.date.label"/>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <warehouse:message code="deliveryNote.deliveredBy.label"/>
                </td>
                <td align="center">
                    <warehouse:message code="deliveryReceipt.signature.label"/>
                </td>
                <td align="right">
                    <warehouse:message code="deliveryReceipt.date.label"/>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <warehouse:message code="deliveryNote.receivedBy.label"/>
                </td>
                <td align="center">
                    <warehouse:message code="deliveryReceipt.signature.label"/>
                </td>
                <td align="right">
                    <warehouse:message code="deliveryReceipt.date.label"/>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <warehouse:message code="deliveryNote.checkedBy.label"/>
                </td>
                <td align="center">
                    <warehouse:message code="deliveryReceipt.signature.label"/>
                </td>
                <td align="right">
                    <warehouse:message code="deliveryReceipt.date.label"/>
                </td>
            </tr>
        </table>

    </div>

</body>
</html>
