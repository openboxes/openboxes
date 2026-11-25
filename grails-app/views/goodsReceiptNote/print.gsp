<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <meta name="layout" content="print"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'print.css')}" type="text/css"
          media="print, screen, projection"/>
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}"/>
    <title><warehouse:message code="default.show.label" args="[entityName]"/></title>
    <script src="${resource(dir: 'js/jquery.nailthumb', file: 'jquery.nailthumb.1.1.js')}"
            type="text/javascript"></script>
    <link rel="stylesheet" href="${resource(dir: 'js/jquery.nailthumb', file: 'jquery.nailthumb.1.1.css')}"
          type="text/css" media="all"/>
    <link rel="stylesheet" href="${resource(dir:'css',file:'buttons.css')}" type="text/css" media="all" />
    <style>
        .product-name {
            white-space: normal;
            overflow-wrap: break-word;
            max-width: 350px;
        }

        .header-title {
            font-size: 20px;
            font-weight: bold;
            line-height: 1.2;
            margin-bottom: 6px;
        }

        td.name {
            white-space: nowrap;
        }

        .print-layout > tbody > tr {
            page-break-inside: auto;
            break-inside: auto;
        }

        .canceled-blank {
            display: inline-block;
            width: 50px;
            border-top: 1px solid lightgrey;
            vertical-align: middle;
        }
        .goods-receipt-note-original-item td {
            color: #222;
            font-weight: 600;
        }

        .goods-receipt-note-original-item td.canceled {
            color: #333;
        }

        .goods-receipt-note-original-item .canceled-blank {
            border-top-color: #333;
        }

        .goods-receipt-note-split-item td {
            color: #888;
        }
    </style>

</head>

<body>

<div id="print-header">
    <span class="title" data-testid="document-title">
        ${warehouse.message(code:'goodsReceiptNote.label')}
    </span>
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

<table class="print-layout">
    <thead>
       <tr><td>
        <g:render template="header" model="[goodsReceiptNote: goodsReceiptNote]"/>
       </td></tr>
    </thead>
    <tbody>
    <tr><td>
        <g:render template="body" model="[pageBreakAfter: false, goodsReceiptNote: goodsReceiptNote]"/>
    </td></tr>
    </tbody>
    <div class="signature-page">
        <g:render template="signature"/>
    </div>

</table>
<script>
    $(document).ready(function () {
        $('.nailthumb-container').nailthumb({ width: 100, height: 100 });
    });
</script>

</body>
</html>
