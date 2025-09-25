<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <title><g:message code="report.lostAndFound.label" default="Lost & Found Report" /></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
</head>
<body>
<div class="body">

    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>

    <div class="button-bar">
        <g:link controller="dashboard" action="index" class="button">
            <g:message code="default.button.backTo.label" args="['Dashboard']"/>
        </g:link>
    </div>

    <div class="yui-gf">

        <!-- Instructions -->
        <div class="box p-2">
            <g:message code="report.lostAndFound.instructions.label"
                       default="The Lost & Found report lists products that are currently stored in Lost & Found (Putaway Discrepancy) locations. Use this report to identify misplaced stock and take corrective actions (adjust, transfer, putaway)." />
        </div>

        <!-- Left side: Filters -->
        <div class="yui-u first">
            <div class="box">
                <h2 class="middle"><g:message code="default.filters.label" default="Filters"/></h2>
                <g:form name="showLostAndFoundReportForm" controller="report" action="showLostAndFoundReport" method="GET">
                    <div class="filters">
                        <div class="filter-list-item">
                            <label><g:message code="location.label" default="Location"/></label>
                            <g:selectLocation
                                    id="location"
                                    name="location.id"
                                    class="chzn-select-deselect"
                                    value="${params?.location?.id ?: session?.warehouse?.id}"
                                    noSelection="['':'']"
                                    data-placeholder=" " />
                        </div>
                        <div class="buttons">
                            <button name="button" value="run" class="button">
                                <img src="${resource(dir:'images/icons/silk',file:'play_green.png')}" />&nbsp;
                                <g:message code="report.runReport.label" default="Run Report"/>
                            </button>
                        </div>
                    </div>
                </g:form>
            </div>
        </div>

        <!-- Right side: Table -->
        <div class="yui-u">
            <div class="box">
                <h2 class="middle">
                    <g:message code="report.lostAndFound.label" default="Lost & Found Report"/>
                </h2>

                <div class="dialog">
                    <table id="lostAndFoundReportTable" class="display">
                        <thead>
                        <tr>
                            <th><g:message code="product.productCode.label" default="Product Code"/></th>
                            <th><g:message code="product.label" default="Product"/></th>
                            <th><g:message code="inventoryItem.lotNumber.label" default="Lot Number"/></th>
                            <th><g:message code="inventoryItem.expirationDate.label" default="Expiration Date"/></th>
                            <th><g:message code="location.binLocation.label" default="Bin Location"/></th>
                            <th><g:message code="default.quantityOnHand.label" default="Quantity On Hand"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${entries}" var="entry">
                            <tr>
                                <td>${entry.product?.productCode}</td>
                                <td>${entry.product?.name}</td>
                                <td>${entry.inventoryItem?.lotNumber ?: '-'}</td>
                                <td><g:formatDate date="${entry.inventoryItem?.expirationDate}" format="MM/dd/yyyy"/></td>
                                <td>${entry.binLocation?.name ?: 'Default'}</td>
                                <td>${entry.quantityOnHand}</td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

</div>

<script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js"></script>
<script>
    $(document).ready(function() {
        $('#lostAndFoundReportTable').dataTable({
            "iDisplayLength": 50,
            "bLengthChange": true,
            "sPaginationType": "full_numbers",
            "bJQueryUI": true,
            "oLanguage": {
                "sEmptyTable": "${g.message(code: 'default.dataTable.noData.label', default: 'No data available')}",
                "sInfo": "${g.message(code: 'default.dataTable.showing.label', default: 'Showing')} _START_ ${g.message(code: 'default.dataTable.to.label', default: 'to')} _END_ ${g.message(code: 'default.dataTable.of.label', default: 'of')} _TOTAL_",
                "sInfoEmpty": "${g.message(code: 'default.dataTable.showingZeroEntries.label', default: 'Showing 0 entries')}"
            }
        });
    });
</script>
</body>
</html>