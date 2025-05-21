<%@ page import="org.pih.warehouse.inventory.TransactionScope" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<%@ page import="org.pih.warehouse.inventory.LotStatusCode" %>
<%@ page import="org.pih.warehouse.inventory.TransactionCode" %>

<html>
<head>
    <style>
        .print-history {
            display: inline-block;
        }

        .active {
            color: black;
        }

        .icon::before {
            padding: 10px;
            content: '\25B7'; /* Unicode character for triangle sign pointing to right */
            font-size: 13px;
        }

        .icon.active::before {
            padding: 10px;
            content: "\25BC"; /* Unicode character for triangle sign pointing to down */
        }

        .showAll {
            right: 5px;
            position: absolute;
            top: 8px;
        }

        .showYear {
            float: right;
        }

        /* Main parent row */
        .yearRow {
            cursor: pointer;
        }

        .yearRow:hover {
            background: #f7f7f7;
        }

        /* Hide child rows on initial load (unless its current year row) */
        .monthRow {
            display: none;
            cursor: pointer;
        }

        .dataRow {
            display: none;
        }

        .currentYear {
            display: table-row;
        }

        .monthRow .icon {
            padding-left: 20px;
        }

        .monthRow:hover {
            background: #f7f7f7;
        }
    </style>
    <script>
      $(document).ready(function() {
        function getMonthRows($row) {
          var monthRows = [];
          while($row.next().hasClass('monthRow')) {
            if (!$row.next().hasClass('dataRow')) {
              monthRows.push($row.next());
            }
            $row = $row.next();
          }
          return monthRows;
        }

        function getDataRows($row) {
          var dataRows = [];
          while($row.next().hasClass('dataRow')) {
            dataRows.push($row.next());
            $row = $row.next();
          }
          return dataRows;
        }

        $('.yearRow').on('click', function() {
          $(this).children().toggleClass("active");
          var monthRows = getMonthRows($(this));
          $.each(monthRows, function() {
            $(this).children().removeClass("active");
            var dataRows = getDataRows($(this));
            $.each(dataRows, function() {
              $(this).hide();
            });
            $(this).toggle();
          });
        });

        $('.monthRow:not(.dataRow)').on('click', function() {
          $(this).children().toggleClass("active");
          var dataRows = getDataRows($(this));
          $.each(dataRows, function() {
            $(this).toggle();
          })
        });

        $('.showAll').on('click', function() {
          if (!$(this).hasClass('expanded')) {
            $(this).addClass('expanded');
            $('.yearRow').show().children().addClass("active");
            $('.monthRow').show().children().addClass("active");
            $('.dataRow').show();
          } else {
            $(this).removeClass('expanded');
            $('.yearRow').children().removeClass("active");
            $('.monthRow').hide().children().removeClass("active");
            $('.dataRow').hide();
          }
        });

        $('.showYear').on('click', function(event) {
          var tr = $(this).closest('tr');
          var td = $(this).closest('td');
          var monthRows = getMonthRows(tr);
          var allRowsVisible = true;

          $.each(monthRows, function() {
            if (allRowsVisible) {
              allRowsVisible = $(this).children().hasClass("active");
            }
          });
          $.each(monthRows, function() {
            var dataRows = getDataRows($(this));
            $.each(dataRows, function() {
              if (allRowsVisible) {
                $(this).hide();
              } else {
                $(this).show();
              }
            });
            if (allRowsVisible) {
              td.removeClass('active');
              $(this).hide().children().removeClass("active");
            } else {
              td.addClass('active');
              $(this).show().children().addClass("active");
            }
          });

          event.stopPropagation();
        });

        $("#stockHistoryFilter").keyup(function(event){
          var filterCell = 9 // serial lot number
          var filterValue = $("#stockHistoryFilter").val().toUpperCase();
          filterTable(filterCell, filterValue)
        });

      });
      function filterTable(cellIndex, filterValue) {
        var tableRows = $("#stockHistoryTable tr.dataRow");

        // Loop through all table rows, and hide those who don't match the search query
        $.each(tableRows, function(index, currentRow) {

            // If filter matches text value then we display, otherwise hide
            var txtValue = $(currentRow).find("td").eq(cellIndex).text();
            if (txtValue.toUpperCase().indexOf(filterValue) > -1) {
                $(currentRow).show();
            } else {
                $(currentRow).hide();
            }
        });
      }


    </script>
</head>

<body>
<div class="box">
    <h2>
        <div>
            <warehouse:message code="inventory.stockHistory.label"/>
            <div class="print-history">
                <g:link controller="inventoryItem" action="showStockHistory" params="[print:true]" id="${commandInstance.product.id}" class="button">
                    <img src="${resource(dir:'images/icons',file:'pdf.png')}" />
                    ${warehouse.message(code: 'inventory.exportPdf.label', default: 'Export to PDF')}
                </g:link>
            </div>
            <div class="showAll button">
                ${warehouse.message(code: 'default.showAll.label')}
            </div>
        </div>
    </h2>
    <input
        type="text"
        id="stockHistoryFilter"
        class="text large"
        placeholder="${g.message(code: 'inventory.stockHistory.search.label', default: 'Filter by serial number or lot number')}"
    />
    <table id="stockHistoryTable" class="stockHistory">
        <thead>
            <tr class="odd">
                <th>

                </th>
                <th>
                    ${warehouse.message(code: 'default.date.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'default.time.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'default.createdBy.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'transaction.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'default.origin.label', default: "Origin")}&nbsp;/&nbsp;
                    ${warehouse.message(code: 'order.destination.label', default: "Destination")}
                </th>
                <th class="border-right">
                    ${warehouse.message(code: 'default.reference.label')}
                </th>
                <th class="border-right middle center">
                    ${warehouse.message(code: 'default.comments.label')}
                </th>
                <th class="border-right">
                    ${warehouse.message(code: 'inventoryItem.binLocation.label')}
                </th>
                <th class="border-right">
                    ${warehouse.message(code: 'inventoryItem.lotNumber.label')}
                </th>

                <th class="border-right center" width="7%">
                    ${warehouse.message(code: 'default.count.label', default: 'Count')}
                </th>
                <th class="border-right center" width="7%">
                    ${warehouse.message(code: 'default.credit.label', default: 'Credit')}
                </th>
                <th class="border-right center" width="7%">
                    ${warehouse.message(code: 'default.debit.label', default: 'Debit')}
                </th>
                <th class="center" width="7%">
                    ${warehouse.message(code: 'report.quantityBalance.label', default: 'Balance')}
                </th>
            </tr>
        </thead>
        <tbody>
            <g:set var="count" value="${0}"/>
            <g:each var="year" in="${stockHistoryList}">
                <g:set var="isCurrentYear" value="${(new Date().year + 1900 == year.key.toInteger())}"/>
                <tr class="yearRow border-top">
                    <div>
                    <td colspan="14" class="icon ${isCurrentYear ? 'active' : ''}">
                        ${year.key}
                        <button class="showYear button">
                            ${warehouse.message(code: 'default.showYear.label')}
                        </button>
                    </td>
                    </div>
                </tr>
                <g:each var="month" in="${year.value}">
                    <tr class="monthRow border-top ${isCurrentYear ? 'currentYear' : ''}">
                        <td colspan="14" class="icon  ${isCurrentYear ? 'active' : ''}">
                            <g:message
                                code="month.${(month.key).toInteger() + 1}.label"
                                default="${new java.text.DateFormatSymbols().months[(month.key).toInteger()]}"
                            />
                            ${year.key}
                        </td>
                    </tr>
                    <g:each var="stockHistoryEntry" in="${month.value}">
                        <g:set var="rowClass" value=""/>
                        <g:if test="${!stockHistoryEntry.isSameTransaction}">
                            <g:set var="rowClass" value="${(count++%2==0)?'even':'odd' }"/>
                            <g:if test='${stockHistoryEntry?.isBaseline}'>
                                <g:set var="rowClass" value="${rowClass} border-top"/>
                            </g:if>
                        </g:if>
                        <g:else>
                            <g:set var="rowClass" value="${(count%2==0)?'odd':'even' }"/>
                        </g:else>

                        <tr class="${rowClass} monthRow dataRow ${isCurrentYear ? 'currentYear' : ''} ${stockHistoryEntry?.inventoryItem?.lotStatus == LotStatusCode.RECALLED ? 'recalled' : ''}"
                            title="${stockHistoryEntry?.inventoryItem?.lotStatus == LotStatusCode.RECALLED ? warehouse.message(code: 'inventoryItem.recalledLot.label') : ''}">
                            <td  class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <g:if test="${stockHistoryEntry?.isInternal}">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'arrow_refresh_small_blue.png' )}" title="${format.metadata(obj:TransactionScope.INTERNAL)}"/>
                                    </g:if>
                                    <g:elseif test="${stockHistoryEntry?.transaction?.transactionType?.isAdjustment()}">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'arrow_up_green_down_red.png')}" title="${format.metadata(obj:stockHistoryEntry?.transaction?.transactionType)}"/>
                                    </g:elseif>
                                    <g:elseif test="${stockHistoryEntry?.transaction?.transactionType?.transactionCode== TransactionCode.DEBIT}">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'delete.png' )}" title="${format.metadata(obj:stockHistoryEntry?.transaction?.transactionType)}"/>
                                    </g:elseif>
                                    <g:elseif test="${stockHistoryEntry?.transaction?.transactionType?.transactionCode== TransactionCode.CREDIT}">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'add.png' )}" title="${format.metadata(obj:stockHistoryEntry?.transaction?.transactionType)}" />
                                    </g:elseif>
                                    <g:elseif test="${stockHistoryEntry?.transaction?.transactionType?.transactionCode== TransactionCode.INVENTORY}">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'calculator_edit.png' )}" title="${format.metadata(obj:stockHistoryEntry?.transaction?.transactionType)}" />
                                    </g:elseif>
                                    <g:elseif test="${stockHistoryEntry?.transaction?.transactionType?.transactionCode== TransactionCode.PRODUCT_INVENTORY}">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'calculator.png' )}" title="${format.metadata(obj:stockHistoryEntry?.transaction?.transactionType)}"/>
                                    </g:elseif>
                                </g:if>
                            </td>
                            <td nowrap="nowrap" class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <format:date obj="${stockHistoryEntry?.transaction?.transactionDate}" format="dd/MMM/yyyy"/>
                                </g:if>
                            </td>
                            <td class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <format:date obj="${stockHistoryEntry?.transaction?.transactionDate}" format="hh:mma"/>
                                </g:if>
                            </td>
                            <td class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <div title="${stockHistoryEntry?.transaction?.dateCreated}">
                                        ${stockHistoryEntry?.transaction?.createdBy?.name?:g.message(code:'default.unknown.label')}
                                    </div>
                                </g:if>
                            </td>
                            <td class="middle">
                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <g:if test="${stockHistoryEntry?.isInternal}">
                                        <g:set var="localTransfer" value="${stockHistoryEntry?.transaction?.localTransfer}"/>
                                        <g:link controller="inventory" action="showTransaction" id="${localTransfer.destinationTransaction?.id }">
                                            <format:metadata obj="${localTransfer?.destinationTransaction?.transactionType}"/>
                                        </g:link>
                                        /
                                        <g:link controller="inventory" action="showTransaction" id="${localTransfer.sourceTransaction?.id }">
                                            <format:metadata obj="${localTransfer?.sourceTransaction?.transactionType}"/>
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        <g:link controller="inventory" action="showTransaction" id="${stockHistoryEntry?.transaction?.id }">
                                            <format:metadata obj="${stockHistoryEntry?.transaction?.transactionType}"/>
                                            <g:if test="${stockHistoryEntry?.transaction?.transactionNumber}">
                                                &rsaquo;
                                                ${stockHistoryEntry?.transaction?.transactionNumber }
                                            </g:if>
                                        </g:link>
                                    </g:else>
                                    <g:if test="${stockHistoryEntry?.transaction?.comment}">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'note.png')}" class="middle" title="${stockHistoryEntry?.transaction?.comment}"/>
                                    </g:if>
                                </g:if>
                            </td>
                            <td class="middle">
                                <g:if test="${stockHistoryEntry?.transaction?.source }">
                                    ${stockHistoryEntry?.transaction?.source?.name }
                                </g:if>
                                <g:elseif test="${stockHistoryEntry?.transaction?.destination }">
                                    ${stockHistoryEntry?.transaction?.destination?.name }
                                </g:elseif>

                            </td>

                            <td class="border-right middle">

                                <g:if test="${stockHistoryEntry?.showDetails}">
                                    <g:set var="shipment" value="${stockHistoryEntry?.transaction?.incomingShipment ?: stockHistoryEntry?.transaction?.outgoingShipment}"/>
                                    <div>
                                        %{-- CHECK IF IT IS PURCHASE ORDER RELATED --}%
                                        <g:if test="${shipment?.isFromPurchaseOrder}">
                                            <g:link controller="stockMovement" action="show" id="${shipment?.id }">
                                                <div class="ellipsis" title="${shipment?.shipmentNumber} &rsaquo; ${shipment?.name}">
                                                    <format:metadata obj="${shipment?.purchaseOrder?.orderType?.orderTypeCode }"/>
                                                    &rsaquo;
                                                    ${shipment?.shipmentNumber}
                                                </div>
                                            </g:link>
                                        </g:if>
                                        %{-- CHECK IF IT IS RETURN ORDER RELATED --}%
                                        <g:elseif test="${shipment?.isFromReturnOrder}">
                                            <g:link controller="stockMovement" action="show" id="${shipment?.id}">
                                                <div class="ellipsis" title="${shipment?.shipmentNumber} &rsaquo; ${shipment?.name}">
                                                    <g:message
                                                        code="order.orderType.code.${shipment?.returnOrder?.orderType?.code}"
                                                        default="${shipment?.returnOrder?.orderType?.name}"
                                                    />
                                                    &rsaquo;
                                                    ${shipment?.shipmentNumber}
                                                </div>
                                            </g:link>
                                        </g:elseif>
                                        %{-- CHECK IF IT IS INBOUND OR OUTBOUND STOCK MOVEMENT --}%
                                        <g:elseif test="${stockHistoryEntry?.transaction?.requisition }">
                                            <g:link controller="stockMovement" action="show" id="${stockHistoryEntry?.transaction?.requisition?.id }">
                                                <div title="${stockHistoryEntry?.transaction?.requisition?.requestNumber } &rsaquo; ${stockHistoryEntry?.transaction?.requisition?.name }">
                                                    <g:message code="requisition.label"/>
                                                    &rsaquo;
                                                    ${stockHistoryEntry?.transaction?.requisition?.requestNumber }
                                                </div>
                                            </g:link>
                                        </g:elseif>
                                        %{-- CHECK IF IT IS SHIPMENT WITHOUT BACKING OBJECT (NON REQUISITION, NON ORDER) --}%
                                        <g:elseif test="${shipment}">
                                            <g:link controller="shipment" action="showDetails" id="${shipment?.id }">
                                                <div class="ellipsis" title="${shipment?.shipmentNumber } &rsaquo; ${shipment?.name }">
                                                    <g:message code="shipment.label"/>
                                                    &rsaquo;
                                                    ${shipment?.shipmentNumber }
                                                </div>
                                            </g:link>
                                        </g:elseif>
                                        %{-- CHECK IF IT IS TRANSFER OR PUTAWAY ORDER --}%
                                        <g:elseif test="${stockHistoryEntry?.transaction?.order }">
                                            <g:link controller="order" action="show" id="${stockHistoryEntry?.transaction?.order?.id }">
                                                <div title="${stockHistoryEntry?.transaction?.order?.name }">
                                                    <g:message
                                                        code="order.orderType.code.${stockHistoryEntry?.transaction?.order?.orderType?.code}"
                                                        default="${stockHistoryEntry?.transaction?.order?.orderType?.name}"
                                                    />
                                                    &rsaquo;
                                                    ${stockHistoryEntry?.transaction?.order?.orderNumber }
                                                </div>
                                            </g:link>
                                        </g:elseif>
                                    </div>
                                    %{-- reset shipment value --}%
                                    <g:set var="shipment" value="${null}"/>
                                </g:if>
                            </td>
                            <td class="border-right middle center">
                                <g:if test="${stockHistoryEntry?.comments}">
                                    <g:if test="${params.print}">
                                        ${stockHistoryEntry.comments}
                                    </g:if>
                                    <g:else>
                                        <img src="${resource(dir: 'images/icons/silk', file: 'note.png')}" class="middle" title="${stockHistoryEntry.comments}"/>
                                    </g:else>
                                </g:if>
                            </td>


                            <td class="border-right middle">
                                <div class="line">
                                    <span class="d-flex">
                                        <g:if test="${stockHistoryEntry?.binLocation}">
                                            <g:if test="${stockHistoryEntry?.binLocation?.zone}">
                                                <span class="line-base" title="${stockHistoryEntry?.binLocation?.zone?.name}">
                                                    ${stockHistoryEntry?.binLocation?.zone?.name}
                                                </span>:&nbsp;
                                            </g:if>
                                            <span class="line-extension" title="${stockHistoryEntry?.binLocation?.name}">
                                                ${stockHistoryEntry?.binLocation?.name}
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <div class="fade">${g.message(code: 'default.label')}</div>
                                        </g:else>
                                    </span>
                                    <g:if test="${stockHistoryEntry?.isInternal}">
                                        <span>&nbsp;&rsaquo;&nbsp;</span>
                                        <g:if test="${stockHistoryEntry?.destinationBinLocation}">
                                            <span class="line-base" title="${stockHistoryEntry?.destinationBinLocation?.zone?.name}">
                                                <g:if test="${stockHistoryEntry?.destinationBinLocation?.zone}">
                                                    ${stockHistoryEntry?.destinationBinLocation?.zone?.name}
                                                </g:if>
                                            </span>
                                            <span class="line-extension" title="${stockHistoryEntry?.destinationBinLocation?.name}">
                                                ${stockHistoryEntry?.destinationBinLocation?.name}
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <div class="fade">${g.message(code: 'default.label')}</div>
                                        </g:else>
                                    </g:if>
                                </div>
                            </td>

                            <td class="border-right center middle">
                                <span class="lotNumber">
                                    ${stockHistoryEntry?.inventoryItem?.lotNumber}
                                </span>
                            </td>
                            <td class="border-right center middle">
                                <g:if test="${stockHistoryEntry?.transactionCode in [TransactionCode.INVENTORY, TransactionCode.PRODUCT_INVENTORY] }">
                                    <span class="balance">
                                        <g:formatNumber number="${stockHistoryEntry?.quantity?:0 }" format="###,###.#" maxFractionDigits="1"/>
                                    </span>
                                </g:if>
                            </td>

                            <td class="border-right center middle">
                                <g:if test="${stockHistoryEntry.isCredit || stockHistoryEntry.isInternal }">
                                    <span class="credit">
                                        <g:formatNumber number="${stockHistoryEntry?.quantity?:0 }" format="###,###.#" maxFractionDigits="1"/>
                                    </span>

                                </g:if>
                            </td>
                            <td class="border-right center middle">
                                <g:if test="${stockHistoryEntry.isDebit || stockHistoryEntry.isInternal}">
                                    <span class="debit"><g:formatNumber number="${stockHistoryEntry?.quantity?:0 }" format="###,###.#" maxFractionDigits="1"/></span>
                                </g:if>
                            </td>
                            <td class="center middle">
                                <g:formatNumber number="${stockHistoryEntry?.balance?:0}" format="###,###.#" maxFractionDigits="1"/>
                            </td>
                        </tr>
                    </g:each>
                </g:each>
            </g:each>
            <g:unless test="${stockHistoryList }">
                <tr>
                    <td colspan="11" class="even center">
                        <div class="empty fade">
                            <warehouse:message code="transaction.noTransactions.label"/>
                        </div>
                    </td>
                </tr>
            </g:unless>
        </tbody>
        <tfoot style="display: table-row-group">
            <tr class="odd">
                <th colspan="7" class="left border-right">
                    <warehouse:message code="stockCard.totals.label" default="Totals"/>
                </th>
                <th class="center border-right">

                </th>
                <th class="center border-right">

                </th>
                <th></th>
                <th class="center border-right">
                    <g:formatNumber number="${totalCount?:0}" format="#,###"/>
                </th>
                <th class="center border-right">
                    <g:formatNumber number="${totalCredit?:0}" format="#,###"/>
                </th>
                <th class="center border-right">
                    (<g:formatNumber number="${totalDebit?:0}" format="#,###"/>)
                </th>
                <th class="center">
                    <g:formatNumber number="${totalBalance?:0}" format="#,###"/>
                </th>
            </tr>
        </tfoot>
    </table>
</div>
</body>
</html>
