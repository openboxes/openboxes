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
</head>

<body>
<div id="print-header" style="line-height: 40px;">
    <%--<img id="logo" src="${createLinkTo(dir: 'images/', file: 'hands.jpg')}"/>--%>
    <span class="title"><warehouse:message code="picklist.print.label"/></span>
    <span style="float: right;">
        <button type="button" id="print-button" onclick="window.print()">
            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}"/>
            ${warehouse.message(code: "default.print.label")}
        </button>
        &nbsp;
        <a href="javascript:window.close();">Close</a>
    </span>
    <hr/>
</div>

<div class="clear"></div>

<table border="0">
    <tr>
        <td width="1%">
            <div class="requisition-header cf-header" style="margin-bottom: 20px;">
                <div class="print-logo nailthumb-container-100">
                    <img src="${createLinkTo(dir: 'images/', file: 'hands.jpg')}"/>
                </div>
            </div>
        </td>
        <td>
            <div class="header">
                <h1><warehouse:message code="picklist.label"/> (${requisition.requestNumber})</h1>
            </div>
            <div class="header">
                <h3>${requisition?.name }</h3>
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
        <td class="right">
            <div>
                <div class="header">
                    <label><warehouse:message code="requisition.depot.label"/>:</label> ${requisition.destination?.name}
                </div>
                <div class="header">
                    <label><warehouse:message code="requisition.ward.label"/>:</label> ${requisition.origin?.name}
                </div>

                <div class="header">
                    <label><warehouse:message code="requisition.date.label"/>:</label> <g:formatDate
                        date="${requisition?.dateRequested}" format="MMMMM dd, yyyy  hh:mm a"/>
                </div>
                <div class="header">
                    <label><warehouse:message code="picklist.datePrinted.label" default="Date printed"/>:</label> <g:formatDate
                        date="${new Date()}" format="MMMMM dd, yyyy hh:mm a"/>
                </div>

            </div>
        </td>
    </tr>

</table>


<div class="clear"></div>





<div class="clear"></div>

<table class="signature-table" border="0">
    <tr class="theader">
        <td width="15%"></td>
        <td width="35%"><warehouse:message code="default.name.label"/></td>
        <td width="35%"><warehouse:message code="default.signature.label"/></td>
        <td width="15%" class="center"><warehouse:message code="default.date.label"/></td>
    </tr>
    <tr>
        <td class="right"><label><warehouse:message code="requisition.requestedBy.label"/></label></td>
        <td>${requisition?.requestedBy?.name}</td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td class="right"><label><warehouse:message code="requisition.fulfilledBy.label"/></label></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td class="right"><label><warehouse:message code="requisition.verifiedBy.label"/></label></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
</table>

<div class="clear"></div>

<div>
    <g:render template="printPage" model="[requisitionItems:requisition?.requisitionItems]"/>
</div>

<script>
    $(document).ready(function () {
        $('.nailthumb-container').nailthumb({ width: 100, height: 60 });
        $('.nailthumb-container-100').nailthumb({ width: 100, height: 100 });
    });
</script>

</body>
</html>
