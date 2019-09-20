<div class="box">
    <h2><g:message code="forecasting.demand.label"/></h2>
    <table id="productDemand" class="dataTable">
        <thead>
        <th>Request</th>
        <th>Status</th>
        <th>Date</th>
        <th>Origin</th>
        <th>Destination</th>
        <th>Requested</th>
        <th>Canceled</th>
        <th>Approved</th>
        <th>Approved Change</th>
        <th>Approved Substitution</th>
        <th>Demand</th>
        <th>Reason Code</th>
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

    $('#productDemand').dataTable( {
        "bProcessing": true,
        "sServerMethod": "GET",
        "iDisplayLength": 25,
        "bSearch": false,
        "bScrollInfinite": true,
        "bScrollCollapse": true,
        "sScrollY": 400,
        "bJQueryUI": true,
        "bAutoWidth": true,
        "sAjaxSource": "${request.contextPath}/json/getProductDemand/${product?.id}",
        "aaSorting": [[2,'desc']],
        "aoColumns": [
            { "mData": "request_number", "sWidth": "0%" },
            { "mData": "request_status", "sWidth": "0%" },
            { "mData": "date_requested", "sType":"date", "sWidth": "0%" },
            { "mData": "origin_name", "sWidth": "0%" },
            { "mData": "destination_name", "sWidth": "0%" },
            { "mData": "quantity_requested", "sWidth": "0%" },
            { "mData": "quantity_canceled", "sWidth": "0%" },
            { "mData": "quantity_approved", "sWidth": "0%" },
            { "mData": "quantity_change_approved", "sWidth": "0%" },
            { "mData": "quantity_substitution_approved", "sWidth": "0%" },
            { "mData": "quantity_demand", "sWidth": "0%" },
            { "mData": "cancel_reason_code", "sWidth": "0%" },
        ]
    });

</script>
