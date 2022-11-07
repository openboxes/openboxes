<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <meta name="layout" content="print"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'print.css')}" type="text/css"
          media="print, screen, projection"/>
    <script src="${resource(dir: 'js/jquery.nailthumb', file: 'jquery.nailthumb.1.1.js')}"
            type="text/javascript"></script>
    <link rel="stylesheet" href="${resource(dir: 'js/jquery.nailthumb', file: 'jquery.nailthumb.1.1.css')}"
          type="text/css" media="all"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'buttons.css')}" type="text/css" media="all" />
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
                <g:each var="headerItem" status="i" in="${headerItems.keySet()}">
                    <tr class="header">
                        <td class="name right">
                            <label><warehouse:message code="${'order.' + headerItem + '.label'}"/>:</label>
                        </td>
                        <td>
                            ${headerItems[headerItem]}
                        </td>
                    </tr>
                </g:each>
            </table>
        </td>
    </tr>

</table>

<g:set var="zoneNames" value='${itemsMap.keySet()}'/>

<g:each var="zoneName" status="i" in="${zoneNames}">

    <g:set var="lineItems" value='${itemsMap[zoneName].lineItems}'/>
    <g:set var="pickListItems" value='${itemsMap[zoneName].pickListItems}'/>

    <g:set var="showZoneName" value='${zoneName || zoneNames.size() > 1}'/>

    <div class="page" style="page-break-after: ${enablePageBreak && showZoneName && i < zoneNames.size() - 1 ? 'always':'avoid'};">
        <g:if test="${showZoneName}">
            <h1 class="subtitle">
                ${zoneName ?: g.message(code: 'location.noZone.label', default: 'No zone')}
            </h1>
        </g:if>
        <g:each  var="lineItemKey" in="${lineItems.keySet()}">
            <g:if test="${lineItems[lineItemKey].size() > 0}">
                <g:set var="groupName" value="${g.message(code:'product.'+lineItemKey+'.label')}"/>
                <g:set var="pageBreakAfter" value="${enablePageBreak && !showZoneName ?'always':'avoid'}"/>
                <g:render template="printPage" model="[lineItems:lineItems[lineItemKey], groupName: groupName, pickListItems: pickListItems, pageBreakAfter: pageBreakAfter]"/>
            </g:if>
        </g:each>
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
