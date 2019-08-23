<%@ page import="org.pih.warehouse.requisition.RequisitionItemSortByCode; grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
    <script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
    <style>
    .sortable { list-style-type: none; margin: 0; padding: 0; width: 100%; }
    .sortable tr { margin: 0 5px 5px 5px; padding: 5px; font-size: 1.2em; height: 1.5em; }
    html>body .sortable li { height: 1.5em; line-height: 1.2em; }
    .ui-state-highlight { height: 1.5em; line-height: 1.2em; }
    </style>
</head>
<body>

<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<g:if test="${flash.error}">
    <div class="errors">${flash.error}</div>
</g:if>
<g:hasErrors bean="${requisition}">
    <div class="errors">
        <g:renderErrors bean="${requisition}" as="list" />
    </div>
</g:hasErrors>

<div id="success-messages"></div>
<div id="error-messages" ></div>

<g:render template="summary" model="[requisition:requisition]"/>

<div class="yui-gf">
    <div class="yui-u first">
        <g:render template="header" model="[requisition:requisition]"/>

    </div>
    <div class="yui-u">
        <div class="box">
            <h2>
                ${warehouse.message(code:'requisitionTemplate.requisitionItems.label')}
            </h2>

                <g:hiddenField name="id" value="${requisition.id}"/>
                <g:hiddenField name="version" value="${requisition.version}"/>

                <div>
                    <table class="sortable dataTable" data-update-url="${createLink(controller:'json', action:'sortRequisitionItems')}">
                        <thead>
                        <tr>
                            %{--<th></th>--}%
                            <th class="center"><warehouse:message code="product.productCode.label" default="#"/></th>
                            <th><warehouse:message code="product.label"/></th>
                            <th><warehouse:message code="category.label"/></th>
                            <th class="center"><warehouse:message code="requisitionTemplate.maxQuantity.label"/></th>
                            <th class="center"><warehouse:message code="unitOfMeasure.label"/></th>
                            <th class="center"><warehouse:message code="requisitionTemplate.monthlyQuantity.label"/></th>
                            <g:hasRoleFinance>
                                <th id="finance" class="center"><warehouse:message code="requisitionTemplate.unitCost.label"/></th>
                                <th class="center"><warehouse:message code="requisitionTemplate.totalCost.label"/></th>
                            </g:hasRoleFinance>
                            <g:isUserAdmin>
                                <th id="actions"><warehouse:message code="default.actions.label"/></th>
                            </g:isUserAdmin>
                        </tr>
                        </thead>
                        <tbody>

                        </tbody>
                        <tfoot>
                            <g:isUserAdmin>
                                <tr class="prop">
                                    <td></td>
                                    <td>
                                        <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName?skipQuantity=true"
                                                       width="100%" styleClass="text"/>
                                    </td>
                                    <td></td>
                                    <td class="center">
                                        <g:textField name="quantity" value="" class="text" size="6"/>
                                    </td>
                                    <td class="center">
                                        <g:select name="unitOfMeasure"
                                                  class="chzn-select-deselect"
                                                  from="['EA/1']"/>
                                    </td>
                                    <td></td>
                                    <g:hasRoleFinance>
                                        <td></td>
                                        <td></td>
                                    </g:hasRoleFinance>
                                    <td>
                                        <button class="button icon add" id="add-requisition-item">
                                            <warehouse:message code="default.button.add.label"/>
                                        </button>
                                    </td>
                                </tr>
                            </g:isUserAdmin>
                            <tr>
                                <td colspan="9">
                                    <div class="buttons">
                                        <button id="update-requisition" class="button" name="save">
                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'accept.png')}" />&nbsp;
                                            ${warehouse.message(code:'default.button.save.label', default: 'Save') }
                                        </button>
                                        %{--<g:link controller="requisitionTemplate" action="show" id="${requisition?.id}" class="button">--}%
                                            %{--<warehouse:message code="default.button.done.label" default="Done"/>--}%
                                        %{--</g:link>--}%
                                    </div>
                                </td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
        </div>
    </div>
</div>
<script>
    $(document).ready(function() {
        var columns = [
          { "mData": "product.productCode" },
          { "mData": "product.name" },
          { "mData": "product.category" },
          { "mData": "quantity" },
          { "mData": "productPackageId" },
          { "mData": "monthlyDemand" }
        ];

        if ($("#finance").length) {
          columns.push({ "mData": "product.pricePerUnit" });
          columns.push({ "mData": "totalCost" });
        }

        if ($("#actions").length) {
          columns.push({ "mData": "id", "bSortable": false });
        }

        var table = $(".dataTable").dataTable({
          "bProcessing": true,
          "sServerMethod": "GET",
          "bSearch": false,
          "bScrollCollapse": true,
          "bJQueryUI": true,
          "bAutoWidth": true,
          "aaSorting": [],
          "sPaginationType": "full_numbers",
          "sAjaxSource": "${request.contextPath}/json/getRequisitionItems/" + $("#id").val(),
          "fnServerData": function ( sSource, aoData, fnCallback ) {
            $.ajax( {
              "dataType": 'json',
              "type": "GET",
              "url": sSource,
              "data": aoData,
              "success": fnCallback,
              "timeout": 10000,   // optional if you want to handle timeouts (which you should)
              "error": handleAjaxError // this sets up jQuery to give me errors
            } );
          },
          "oLanguage": {
            "sZeroRecords": "No records found",
            "sProcessing": "Loading ... <img alt='spinner' src='${request.contextPath}/images/spinner.gif' />"
          },
          "iDisplayLength" : -1,
          "aLengthMenu": [
            [5, 10, 25, 100, 1000, -1],
            [5, 10, 25, 100, 1000, "All"]
          ],
          "aoColumns": columns,
          "bUseRendered": false,
          "fnRowCallback": function( nRow, aData) {

              $('td:eq(0)', nRow).addClass('center middle');

              $('td:eq(1)', nRow).addClass('middle');
              $('td:eq(1)', nRow).html('<a href="${request.contextPath}/inventoryItem/showStockCard/' + aData["product"].id + '" target="_blank">' + aData["product"].name + '</a>');

              $('td:eq(2)', nRow).addClass('middle dont-break-out');

              $('td:eq(3)', nRow).addClass('center');
              $('td:eq(3)', nRow).html('<input class="text" id="quantity-' + aData["id"] + '" size="6" value=' + aData["quantity"] + ' />');

              $('td:eq(4)', nRow).addClass('center middle');
              var selectPackage = $('<select/>', {
                id: 'productPackage-' + aData["id"]
              });
              selectPackage.css('width', '100%');

              selectPackage.append($("<option>").attr('value', null).text('EA/1'));
              $(aData["product"].packages).each(function() {
                selectPackage.append($("<option>", { value: this.id, selected: this.id === aData["productPackageId"] }).text(this.uom.code + "/" + this.quantity + " -- " + this.uom.name));
              });

              $('td:eq(4)', nRow).html(selectPackage);

              $('td:eq(5)', nRow).addClass('center middle');

              if (aData["monthlyDemand"] === null) {
                $('td:eq(5)', nRow).html("${warehouse.message(code: 'requisitionTemplate.noReplenishmentPeriod.message')}");
              } else {
                $('td:eq(5)', nRow).html(aData["monthlyDemand"] + aData["product"].unitOfMeasure ? aData["product"].unitOfMeasure : "${warehouse.message(code:'default.each.label')}");
              }

              if ($("#finance").length) {
                $('td:eq(6)', nRow).html(Number(aData["product"].pricePerUnit).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2}) + " ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}");
                $('td:eq(7)', nRow).html(Number(aData["totalCost"]).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2}) + " ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}");
              }

              if ($("#actions").length) {
                var deleteButton = $('<button/>', {
                  text: "${warehouse.message(code:'default.button.delete.label')}",
                  id: 'delete-' + aData["id"],
                  type: 'button',
                  class: 'button icon trash',
                  style: 'min-width: 70px',
                  click: function (event) {
                    event.preventDefault();

                    $.ajax({
                      url: "${request.contextPath}/json/removeRequisitionItem/" + aData["id"],
                      type: "delete",
                      contentType: 'text/json',
                      dataType: "json",
                      success: function() {
                        table.fnDeleteRow(nRow);
                      },
                      error: handleAjaxError
                    });
                  }
                });

                if ($("#finance").length) {
                  $('td:eq(8)', nRow).html(deleteButton);
                } else {
                  $('td:eq(6)', nRow).html(deleteButton);
                }
              }

              return nRow;
          }

        });

        $("#product-suggest").focus();
        $("#add-requisition-item").click(function(event) {
            event.preventDefault();
            var productId = $("#product-id").val();
            var requisitionId = $("#id").val();
            var quantity = $("#quantity").val();
            var orderIndex = table.fnGetData().length;

            if (productId && quantity) {
              var params = { "product.id": productId, "requisition.id": requisitionId, "quantity": quantity, "orderIndex": orderIndex };
              console.log('params: ', params);

              $.ajax({
                url: "${request.contextPath}/json/addToRequisitionItems",
                type: "get",
                contentType: 'text/json',
                dataType: "json",
                data: params,
                success: function(data) {
                  table.fnAddData(data.data);
                  $("#product-id").val('');
                  $("#product-suggest").val('');
                  $("#quantity").val('');
                },
                error: handleAjaxError
              });
            }
        });

        $("#update-requisition").click(function(event) {
          event.preventDefault();
          var requisitionId = $("#id").val();
          var data = [];
          $(table.fnGetData()).each(function() {
            var id = this.id;

            var quantityInput = $('#quantity-' + id);
            var packageSelect = $('#productPackage-' + id);

            if (quantityInput.length && packageSelect.length) {
              data.push({ id: id, quantity: quantityInput.val(), productPackageId: packageSelect.val() || '' })
            }
          });

          $.ajax({
            url: "${request.contextPath}/json/updateRequisitionItems/" + requisitionId,
            type: "post",
            contentType: 'application/json',
            dataType: "json",
            data: JSON.stringify({ items: data }),
            success: function() {
              $("#success-messages").html('<div class="message">${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition')])}</div>');
            },
            error: handleAjaxError
          });

        });

        $("#selectAllProducts").click(function(event) {
            var checked = ($(this).attr("checked") == 'checked');
            $("input.select-product[type='checkbox']").attr("checked", checked);
        });

        $('#productCodesInput').tagsInput({
            'autocomplete_url':'${createLink(controller: 'json', action: 'findProductCodes')}',
            'defaultText': '...',
            'width': 'auto',
            'height': 'auto',
            'removeWithBackspace' : true
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

        var errorMessage = "<p class='error'>An unexpected error has occurred on the server.  Please contact your system administrator.";

        if (xhr.responseText) {
          var errors = JSON.parse(xhr.responseText).errors;
          if (errors && errors.length) {
            $(errors).each(function () {
              errorMessage += "</br><code>" + this + "</code>"
            });
          }
        }
        $("#error-messages").html(errorMessage + "</p>");
      }
    }

</script>
</body>
</html>
