<div>
    <div class="filter-list">
        <table>
            <g:hiddenField name="supplierId" value="${supplier?.id}" />
            <tr>
                <td>
                    <button class="download-button button">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}" />
                        <warehouse:message code="default.button.download.label"/>
                    </button>
                </td>
                <td width="25%">
                    <label><warehouse:message code="product.label"/></label>
                    <p>
                        <g:autoSuggest id="product" name="product" styleClass="text"
                                       jsonUrl="${request.contextPath }/json/findProductByName?skipQuantity=true"
                                       valueId="${params?.product?.id}"
                                       valueName="${params?.product?.value}"/>
                    </p>
                </td>
                <td width="25%">
                    <label><warehouse:message code="default.search.label"/></label>
                    <p>
                        <g:textField name="q" style="width:100%" class="text" value="${params.q}" placeholder="Search by product code, supplier code, etc"/>
                    </p>
                </td>
                <td class="right" width="5%">
                    <button class="submit-button button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" />&nbsp;
                        ${warehouse.message(code:'default.search.label')}
                    </button>
                </td>
            </tr>
        </table>
    </div>
    <div>
        <table id="priceHistoryTable" class="dataTable">
            <thead>
                <tr class="prop">
                    <th class="center"><g:message code="purchaseOrder.orderNumber.label"/></th>
                    <th class="center"><g:message code="default.dateCreated.label"/></th>
                    <th class="center"><g:message code="default.description.label"/></th>
                    <th class="center"><g:message code="product.code.label"/></th>
                    <th class="center"><g:message code="product.label"/></th>
                    <th class="center"><g:message code="productSupplier.sourceCode.label"/></th>
                    <th class="center"><g:message code="product.supplierCode.label"/></th>
                    <th class="center"><g:message code="product.manufacturer.label"/></th>
                    <th class="center"><g:message code="product.manufacturerCode.label"/></th>
                    <th class="center"><g:message code="productSupplier.unitPrice.label"/></th>
                </tr>
            </thead>
        </table>
    </div>
</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.25.3/moment.min.js"></script>
<script type="text/javascript" charset="utf8" src="//cdnjs.cloudflare.com/ajax/libs/datatables/1.9.4/jquery.dataTables.js"></script>
<script>
  function initializeDataTable() {
    var dataTable = $('#priceHistoryTable').dataTable( {
      "bJQueryUI": true,
      "bProcessing": true,
      "bDestroy": true,
      "sPaginationType": "full_numbers",
      "iDisplayLength": 10,
      "bAutoWidth": false,
      "bFilter": false,
      "sAjaxSource": "${request.contextPath}/supplier/getPriceHistory",
      "fnServerParams": function ( data ) {
        data.push({ name: "supplierId", value: $("#supplierId").val() });
        data.push({ name: "q", value: $("#q").val() });
        data.push({ name: "productId", value: $("#product-id").val() });
      },
      "fnServerData": function ( sSource, aoData, fnCallback ) {
        $.ajax( {
          "dataType": 'json',
          "type": "GET",
          "url": sSource,
          "data": aoData,
          "success": fnCallback,
          "timeout": 120000,   // optional if you want to handle timeouts (which you should)
        } );
      },
      "oLanguage": {
        "sZeroRecords": "No records found",
        "sProcessing": "Loading ... <img alt='spinner' src='${request.contextPath}/images/spinner.gif' />"
      },
      "aLengthMenu": [
        [5, 10, 25, 100],
        [5, 10, 25, 100]
      ],
      "aoColumns": [
        { "mData": "orderNumber"},
        { "mData": "dateCreated"},
        { "mData": "description", sWidth: "15%"},
        { "mData": "productCode"},
        { "mData": "productName", sWidth: "20%"},
        { "mData": "sourceCode"},
        { "mData": "supplierCode"},
        { "mData": "manufacturerName"},
        { "mData": "manufacturerCode"},
        { "mData": "unitPrice"},
      ],
      "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
        $('td:eq(0)', nRow).html('<a href="${request.contextPath}/order/show/' + aData["orderId"] + '">' + aData["orderNumber"] + '</a>');
        $('td:eq(1)', nRow).html(moment(aData["dateCreated"]).format("DD/MMM/YYYY"));
        $('td:eq(9)', nRow).html((aData["unitPrice"]/aData["quantityPerUom"]).toLocaleString('en-US'));
        return nRow;
      }
    });
  }

  $(document).ready(function() {
    initializeDataTable();

    $(".download-button").click(function(event) {
        event.preventDefault();
        var params = {
          supplierId: $("#supplierId").val(),
          q: $("#q").val(),
          productId: $("#product-id").val(),
          format: "text/csv"
        };
        var queryString = $.param(params, true);
        window.location.href = '${request.contextPath}/supplier/getPriceHistory?' + queryString;
    });

    $(".submit-button").click(function(event){
      event.preventDefault();
      initializeDataTable();
    });

  });
</script>
