<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
    <title><g:message code="report.onOrderReport.label" default="Order Report"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
</head>
<body>
<div class="body">

    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${reportInstance}">
        <div class="errors">
            <g:renderErrors bean="${reportInstance}" as="list" />
        </div>
    </g:hasErrors>
    <div class="yui-gf">
        <div class="yui-u first">
            <div class="box">
                <h2 class="middle"><g:message code="default.button.download.label" /></h2>
                <g:form name="showOnOrderReportForm" controller="report" action="showOnOrderReport" method="GET">
                    <div class="filters">
                        <div class="buttons">
                            <span class="action-menu" style="margin-left: 15px">
                                <button class="action-btn button">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'play_green.png')}" />
                                    <g:message code="report.runReport.label"/>
                                    <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
                                </button>
                                <div class="actions">
                                    <div class="action-menu-item">
                                        <a href="#" class="run-btn" data-run-action="summaryReport">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'play_green.png')}" />
                                            <g:message code="default.run.label"
                                                       args="[g.message(code: 'report.onOrderReportSummary.label', default: 'On Order Report Summary')]"/>
                                        </a>
                                    </div>
                                    <div class="action-menu-item">
                                        <a href="#" class="run-btn" data-run-action="detailedReport">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'play_green.png')}" />
                                            <g:message code="default.run.label"
                                                       args="[g.message(code: 'report.onOrderReportDetails.label', default: 'On Order Report Details')]"/>
                                        </a>
                                    </div>
                                </div>
                            </span>
                            <span class="action-menu" style="margin-left: 15px">
                                <button class="action-btn button">
                                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_white_excel.png')}" />&nbsp;
                                    ${warehouse.message(code: 'default.button.download.label')}
                                    <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
                                </button>
                                <div class="actions">
                                    <div class="action-menu-item">
                                        <a href="#" class="download-btn" data-download-action="downloadSummaryOnOrderReport">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'page_white_excel.png')}" />
                                            <g:message code="default.download.label"
                                                       args="[g.message(code: 'report.onOrderReportSummary.label', default: 'On Order Report Summary')]"/>
                                        </a>
                                    </div>
                                    <div class="action-menu-item">
                                        <a href="#" class="download-btn" data-download-action="downloadOnOrderReport">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'page_white_excel.png')}" />
                                            <g:message code="default.download.label"
                                                       args="[g.message(code: 'report.onOrderReportDetails.label', default: 'On Order Report Details')]"/>
                                        </a>
                                    </div>
                                </div>
                            </span>
                        </div>
                    </div>
                </g:form>
            </div>
        </div>
        <div class="yui-u">
            <g:hasErrors bean="${command}">
                <div class="errors">
                    <g:renderErrors bean="${command}" as="list" />
                </div>
            </g:hasErrors>
            <div class="box">
                <h2 class="middle">
                    <g:message code="${params?.report == 'summaryReport' ? 'report.onOrderReportSummary.label' : 'report.onOrderReportDetails.label'}"
                               default="${params?.report == 'summaryReport' ? 'On Order Report Summary' : 'On Order Report Details'}"/>
                </h2>
                <div class="dialog">
                    <table id="orderReportTable">
                        <thead>
                            <tr class="prop">
                                <g:if test="${params?.report == 'summaryReport'}">
                                    <th class="center"><g:message code="product.productCode.label"/></th>
                                    <th class="center"><g:message code="product.label"/></th>
                                    <th class="center"><g:message code="product.qtyOrderedNotShipped.label" default="Qty Ordered Not Shipped"/></th>
                                    <th class="center"><g:message code="product.qtyShippedNotReceived.label" default="Qty Shipped Not Received"/></th>
                                    <th class="center"><g:message code="product.totalOnOrder.label" default="Total On Order"/></th>
                                    <th class="center"><g:message code="product.totalOnHand.label" default="Total On Hand"/></th>
                                    <th class="center"><g:message code="product.totalOnHandAndOnOrder.label" default="Total On Hand and On Order"/></th>
                                </g:if>
                                <g:else>
                                    <th class="center"><g:message code="product.productCode.label"/></th>
                                    <th class="center"><g:message code="product.label"/></th>
                                    <th class="center"><g:message code="product.qtyOrderedNotShipped.label" default="Qty Ordered Not Shipped"/></th>
                                    <th class="center"><g:message code="product.qtyShippedNotReceived.label" default="Qty Shipped Not Received"/></th>
                                    <th class="center"><g:message code="purchaseOrder.orderNumber.label" default="PO #"/></th>
                                    <th class="center"><g:message code="order.purchaseOrderDescription.label" default="PO Description"/></th>
                                    <th class="center"><g:message code="productSupplier.supplierOrganization.label" default="Supplier Organization"/></th>
                                    <th class="center"><g:message code="productSupplier.supplierLocation.label" default="Supplier Location"/></th>
                                    <th class="center"><g:message code="productSupplier.supplierLocationGroup.label" default="Supplier Location Group"/></th>
                                    <th class="center"><g:message code="orderItem.estimatedGoodsReadyDate.label" default="Estimated Goods Ready Date"/></th>
                                    <th class="center"><g:message code="shipping.shipmentNumber.label"/></th>
                                    <th class="center"><g:message code="shipping.shipDate.label"/></th>
                                    <th class="center"><g:message code="shipping.shipmentType.label"/></th>
                                </g:else>
                            </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="loading">Loading...</div>
<script type="text/javascript" charset="utf8" src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js"></script>
<script>

    $(document).ready(function() {
        $('#orderReportTable').dataTable( {
            "bProcessing": false,
            "sServerMethod": "GET",
            "iDisplayLength": 100,
            "bSearch": false,
            "bScrollCollapse": true,
            "bScrollInfinite": true,
            "bJQueryUI": true,
            "sScrollY": 450,
            "bAutoWidth": true,
            "sPaginationType": "full_numbers",
            "sAjaxSource": ${params.report == 'summaryReport'} ?
              "${request.contextPath}/json/getSummaryOrderReport" :
              "${request.contextPath}/json/getDetailedOrderReport",
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                $.ajax( {
                    "dataType": 'json',
                    "type": "GET",
                    "url": sSource,
                    "data": aoData,
                    "success": fnCallback,
                    "timeout": 120000,   // optional if you want to handle timeouts (which you should)
                    "error": handleAjaxError, // this sets up jQuery to give me errors
                    beforeSend : function(){
                        $(".loading").show();
                    },
                    complete: function(){
                       $(".loading").hide();
                    },
                } );
            },
            "oLanguage": {
                //"sZeroRecords": "No records found",
                "sProcessing": "Loading <img alt='spinner' src='${request.contextPath}/images/spinner.gif' /> Loading... "
            },
            "aLengthMenu": [
                [5, 15, 25, 100, 1000, -1],
                [5, 15, 25, 100, 1000, "All"]
            ],
            "aoColumns": ${params.report == 'summaryReport'} ?
              [
                { "mData": "productCode" },
                { "mData": "productName", "sWidth": "20%" },
                { "mData": "qtyOrderedNotShipped", "sClass": "center" },
                { "mData": "qtyShippedNotReceived", "sClass": "center" },
                { "mData": "totalOnOrder" },
                { "mData": "totalOnHand" },
                { "mData": "totalOnHandAndOnOrder"}
            ] :
              [
                { "mData": "productCode" },
                { "mData": "productName", "sWidth": "20%" },
                { "mData": "qtyOrderedNotShipped", "sClass": "center" },
                { "mData": "qtyShippedNotReceived", "sClass": "center" },
                { "mData": "orderNumber" },
                { "mData": "orderDescription" },
                { "mData": "supplierOrganization"},
                { "mData": "supplierLocation" },
                { "mData": "supplierLocationGroup"},
                { "mData": "estimatedGoodsReadyDate" },
                { "mData": "shipmentNumber"},
                { "mData": "shipDate" },
                { "mData": "shipmentType" }
              ]
          ,
            "bUseRendered": false,
            "aaSorting": [[ 0, "asc" ]],
        });
    });

    function handleAjaxError( xhr, status, error ) {
        if ( status === 'timeout' ) {
            alert( 'The server took too long to send the data.' );
        }
        else {
            // User probably refreshed page or clicked on a link, so this isn't really an error
            if(xhr.readyState == 0 || xhr.status == 0) {
                return;
            }
            alert("An error occurred on the server.  Please contact your system administrator.");
        }
    }

    $(".download-btn").click(function(event){
      event.preventDefault();
      $(".loading").show();
      try {
        var action = $(this).data("download-action");
        var formField = $("<input>").attr({id: "downloadActionInput", "type": "hidden", name: "downloadAction", value: action});
        $("form[name='showOnOrderReportForm']").append(formField).submit();
        $("#downloadActionInput").remove();

      } finally {
        $(".loading").hide();
      }
    });

    $(".run-btn").click(function(event){
      event.preventDefault();
      $(".loading").show();
      try {
        var action = $(this).data("run-action");
        var formField = $("<input>").attr({id: "runActionInput", "type": "hidden", name: "report", value: action});
        $("form[name='showOnOrderReportForm']").append(formField).submit();
        $("#runActionInput").remove();

      } finally {
        $(".loading").hide();
      }
    });

</script>
</body>
</html>
