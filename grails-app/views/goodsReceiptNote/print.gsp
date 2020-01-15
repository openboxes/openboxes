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
    <style>
        .product-name {
            text-overflow: ellipsis;
            overflow: hidden;
            max-width: 350px;
            white-space: nowrap;
        }
    </style>

</head>

<body>

<div id="print-header">
    <span class="title">
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

<table>
    <thead>
       <tr><td>
        <g:render template="header" model="[title: warehouse.message(code:'goodsReceiptNote.label')]"/>
       </td></tr>
    </thead>
    <tbody>
    <tr><td>
        <g:render template="body" model="[pageBreakAter:false]"/>
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
