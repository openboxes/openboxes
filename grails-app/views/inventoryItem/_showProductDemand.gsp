<div class="box">
    <h2><g:message code="demand.summary.label" default="Summary"/></h2>
    <table id="productDemandSummary" class="dataTable">
        <thead>
        <th>Year</th>
        <th>Month</th>
        <th>Demand</th>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>
<div class="box">
    <h2><g:message code="demand.details.label" default="Details"/></h2>
    <table id="productDemandDetails" class="dataTable">
        <thead>
        <th>Request</th>
        <th>Status</th>
        <th>Month</th>
        <th>Requested</th>
        <th>Issued</th>
        <th>Origin</th>
        <th>Destination</th>
        <th>Requested</th>
        <th>Canceled</th>
        <th>Approved</th>
        <th>Modified</th>
        <th>Substituted</th>
        <th>Picked</th>
        <th>Demand</th>
        <th>Reason Code Classification</th>
        </thead>
        <tbody>
        </tbody>
    </table>
    <div class="buttons center">
        <g:link controller="inventoryItem" action="showProductDemand"
                id="${product?.id}" class="button" params="['format':'csv']">
            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}" />&nbsp;
            <g:message code="default.button.download.label"/>
        </g:link>
    </div>
</div>
<script>

    $('#productDemandSummary').dataTable( {
        "bProcessing": true,
        "bSort": false,
        "sServerMethod": "GET",
        "iDisplayLength": 25,
        "bSearch": false,
        "bScrollInfinite": true,
        "bScrollCollapse": true,
        "sScrollY": 400,
        "bJQueryUI": true,
        "bAutoWidth": true,
        "sAjaxSource": "${request.contextPath}/json/getProductDemandSummary/${product?.id}",
        //"aaSorting": [[2,'desc']],
        "aoColumns": [
            { "mData": "year", "sWidth": "1%" },
            { "mData": "monthName", "sWidth": "1%" },
            { "mData": "quantityDemand", "sWidth": "1%", "sClass": "right" },
        ]
    });

    $('#productDemandDetails').dataTable( {
        "bProcessing": true,
        "bSort": false,
        "sServerMethod": "GET",
        "iDisplayLength": 25,
        "bSearch": false,
        "bScrollInfinite": true,
        "bScrollCollapse": true,
        "sScrollY": 400,
        "bJQueryUI": true,
        "bAutoWidth": true,
        "sAjaxSource": "${request.contextPath}/json/getProductDemandDetails/${product?.id}",
        "aaSorting": [[3,'desc']],
        "aoColumns": [
            { "mData": "request_number", "sWidth": "0%" },
            { "mData": "request_status", "sWidth": "0%" },
            { "mData": "month_year", "sWidth": "0%" },
            { "mData": "date_requested_formatted", "sType":"date", "sWidth": "0%" },
            { "mData": "date_issued_formatted", "sType":"date", "sWidth": "0%" },
            { "mData": "origin_name", "sWidth": "0%" },
            { "mData": "destination_name", "sWidth": "0%" },
            { "mData": "quantity_requested", "sWidth": "0%" },
            { "mData": "quantity_canceled", "sWidth": "0%" },
            { "mData": "quantity_approved", "sWidth": "0%" },
            { "mData": "quantity_modified", "sWidth": "0%" },
            { "mData": "quantity_substituted", "sWidth": "0%" },
            { "mData": "quantity_picked", "sWidth": "0%" },
            { "mData": "quantity_demand", "sWidth": "0%" },
            { "mData": "reason_code_classification", "sWidth": "0%" }
        ]
    });

</script>
