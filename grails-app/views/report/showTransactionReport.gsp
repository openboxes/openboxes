<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="${params.print?'print':'custom' }" />
    <title><warehouse:message code="report.showTransactionReport.label" /></title>
    <style>
        .title { text-align: center; padding: 15px; }
        .total { border-right: 2px solid lightgrey; }
        .parameters { width:30%; margin-left: auto; margin-right: auto;  }
        .filter { padding-right: 15px; }
    </style>
</head>
<body>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${command}">
        <div class="errors">
            <g:renderErrors bean="${command}" as="list" />
        </div>
    </g:hasErrors>


    <div class="summary">

        <g:if test="${!command?.product && command?.entries}">
            <div class="right">
                <label><warehouse:message code="report.exportAs.label"/></label>
                <g:link target="_blank" controller="report" action="generateTransactionReport" class="button"
                        params="[print:true,'location.id':command.location?.id,'category.id':command?.category?.id,'startDate':params.startDate,'endDate':params.endDate,'showTransferBreakdown':command.showTransferBreakdown,'includeChildren':command?.includeChildren,'hideInactiveProducts':command?.hideInactiveProducts]">
                    <warehouse:message code="report.exportAs.html.label"/>
                </g:link>
                <g:link target="_blank" controller="report" action="downloadTransactionReport" class="button"
                        params="[print:true,'location.id':command.location?.id,'category.id':command?.category?.id,'startDate':params.startDate,'endDate':params.endDate,'showTransferBreakdown':command.showTransferBreakdown,'includeChildren':command?.includeChildren,'hideInactiveProducts':command?.hideInactiveProducts,'insertPageBreakBetweenCategories':command?.insertPageBreakBetweenCategories]">
                    <warehouse:message code="report.exportAs.pdf.label"/>
                </g:link>
            </div>
            <div class="clearfix"></div>
        </g:if>

        <h1><g:message code="report.transactionReport.title"/></h1>
    </div>

    <div class="yui-gf">

        <div class="yui-u first">
            <div id="parameters" class="box">
                <h2><g:message code="report.parameters.label" default="Report Parameters"/></h2>
                <g:form controller="report" action="generateTransactionReport" method="GET">
                    <g:if test="${command?.product }">
                        <div class="prop">
                            <label><warehouse:message code="report.product.label"/></label>
                            <div>
                                <format:product product="${command?.product }"/>
                                <g:hiddenField name="product.id" value="${command?.product?.id }"/>
                            </div>
                        </div>
                    </g:if>
                    <div class="prop">
                        <label><warehouse:message code="report.dateRange.label"/></label>
                        <table>
                            <tr>
                                <td>
                                    From: <g:jqueryDatePicker class="filter" id="startDate" name="startDate" value="${command?.startDate }" format="MM/dd/yyyy"/>
                                </td>
                                <td>
                                    To: <g:jqueryDatePicker class="filter" id="endDate" name="endDate" value="${command?.endDate }" format="MM/dd/yyyy"/>
                                </td>
                            </tr>
                        </table>

                    </div>
                    <div class="prop">
                        <label><warehouse:message code="report.location.label"/></label>
                        <g:selectLocation class="chzn-select-deselect" name="location.id" noSelection="['null':'']"
                                          maxChars="75" groupBy="locationType" value="${command?.location?.id}"/>
                    </div>
                    <div class="prop">
                        <label><warehouse:message code="report.category.label"/></label>
                        <g:selectCategory id="category.id"
                                          name="category.id"
                                          multiple="false"
                                          class="chzn-select-deselect"
                                          noSelection="['null':'']"
                                          value="${params.list('category.id')}"/>

                        <div>
                            <g:checkBox name="includeChildren" value="${command?.includeChildren }"/>
                            <label for="includeChildren"><warehouse:message code="report.includeChildren.label"/></label>
                        </div>

                    </div>

                    <div class="prop">
                        <div>
                            <g:checkBox name="hideInactiveProducts" value="${command?.hideInactiveProducts}" />
                            <label for="hideInactiveProducts"><warehouse:message code="report.hideInactiveProducts.label"/></label>
                        </div>
                        <div>
                            <g:checkBox name="showTransferBreakdown" value="${command?.showTransferBreakdown}" />
                            <label for="showTransferBreakdown"><warehouse:message code="report.showTransferBreakdown.label"/></label>
                        </div>
                        <div>
                            <g:checkBox name="insertPageBreakBetweenCategories" value="${command?.insertPageBreakBetweenCategories }"/>
                            <label for="insertPageBreakBetweenCategories"><warehouse:message code="report.insertPageBreakBetweenCategories.label"/></label>
                        </div>
                    </div>

                    <div class="prop center">
                        <button type="submit" class="button"><warehouse:message code="report.runReport.label"/></button>

                        <g:link controller="report" action="showTransactionReport" class="button"><g:message code="default.button.reset.label" default="Reset"></g:message></g:link>
                    </div>
                </g:form>
            </div>
        </div>

        <div class="yui-u">
            <g:if test="${command.product }">
                <style>
                .debit:before { content: '-'; }
                .debit:after { content: ''; }
                .credit:before { content: '+'; }
                .credit:after { content: ''; }
                .product_inventory { font-weight: bold; }
                .inventory { font-weight: bold; }
                </style>
                <g:set var="i" value='${0 }'/>
                <g:each var="entry" in="${command?.entries }">
                    <g:if test="${command?.product == entry?.value?.product}">
                        <div class="buttonBar">
                            <g:link class="button" controller="report" action="generateTransactionReport" params="['location.id':command?.location?.id,'category.id':command?.category.id,'startDate':format.date(obj:command.startDate,format:'MM/dd/yyyy'),'endDate':format.date(obj:command.endDate,format:'MM/dd/yyyy'),'includeChildren':params.includeChildren]" style="display: inline">
                                <warehouse:message code="report.backToInventoryReport.label"/>
                            </g:link>

                            <g:link class="button" controller="report" action="generateTransactionReport" params="['product.id':command?.product?.id,'category.id':command?.category?.id,'location.id':command?.location?.id,startDate:params.startDate,endDate:params.endDate,showEntireHistory:true,'includeChildren':params.includeChildren]">
                                <warehouse:message code="report.showEntireHistory.label"/></g:link>

                            <g:link class="button" controller="inventoryItem" action="showStockCard" params="['product.id':product?.id]" fragment="inventory">
                                <warehouse:message code="report.showStockCard.label"/></g:link>
                        </div>
                        <g:each var="itemEntry" in="${entry.value.entries}">
                            <div class="box">
                                <h2>
                                    ${entry.key }&nbsp;
                                    ${itemEntry.key.lotNumber ?: 'EMPTY'}&nbsp;
                                    <span class="circle">${itemEntry?.value?.quantityRunning}</span>
                                </h2>

                                <table style="border: 1px solid lightgrey" class="report">
                                    <thead>
                                    <tr class="${i++%2?'odd':'even' }">
                                        <th wdith="1%">
                                            <warehouse:message code="report.transactionDate.label"/>
                                        </th>
                                        <th wdith="1%">
                                            <warehouse:message code="report.transactionTime.label" default="Time"/>
                                        </th>
                                        <th>
                                            <warehouse:message code="report.transactionType.label"/>
                                        </th>
                                        <th class="center">
                                            <warehouse:message code="report.quantityChange.label"/>
                                        </th>
                                        <th class="center">
                                            <warehouse:message code="report.quantityBalance.label"/>
                                        </th>
                                    </tr>
                                    </thead>
                                    <g:if test="${itemEntry.value.transactionEntries}">
                                        <tbody>
                                        <tr>
                                            <td><format:date obj="${command?.startDate}" format="MMM dd yyyy"/></td>
                                            <td><format:date obj="${command?.startDate}" format="hh:mma"/></td>
                                            <td><warehouse:message code="report.initialQuantity.label"/></td>
                                            <td></td>
                                            <td class="center">${itemEntry?.value?.quantityInitial }</td>
                                        </tr>
                                        <g:each var="row" in="${itemEntry.value.transactionEntries}">
                                            <g:set var="transactionTypeCode" value="${row?.transactionEntry?.transaction?.transactionType?.transactionCode?.toString()?.toLowerCase()}"/>
                                            <tr class="${i++%2?'odd':'even' }">
                                                <td>
                                                    <format:date obj="${row.transactionEntry?.transaction?.transactionDate}" format="MMM dd yyyy"/>
                                                </td>
                                                <td>
                                                    <format:date obj="${row.transactionEntry?.transaction?.transactionDate}" format="hh:mma"/>
                                                </td>
                                                <td>
                                                    <g:link controller="inventory" action="showTransaction" id="${row?.transactionEntry?.transaction?.id }">
                                                        <format:metadata obj="${row.transactionEntry?.transaction?.transactionType}"/>
                                                    </g:link>
                                                </td>
                                                <td class="center">
                                                    <span class="${transactionTypeCode}">${row.transactionEntry?.quantity }</span>
                                                </td>
                                                <td class="center">${row.balance }</td>
                                            </tr>
                                        </g:each>

                                        </tbody>
                                        <tfoot>
                                        <tr>
                                            <td><format:date obj="${command?.endDate}" format="MMM dd yyyy"/></td>
                                            <td><format:date obj="${command?.endDate}" format="hh:mma"/></td>

                                            <td><warehouse:message code="report.finalQuantity.label"/></td>
                                            <td></td>
                                            <td class="center">${itemEntry?.value?.quantityFinal }</td>
                                        </tr>
                                        </tfoot>
                                    </g:if>
                                    <g:else>
                                        <tbody>
                                        <tr>
                                            <td colspan="5">
                                                <warehouse:message code="transaction.noTransactions.label"/>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </g:else>
                                </table>
                            </div>
                        </g:each>
                    </g:if>
                </g:each>
            </g:if>
            <g:else>
                <div>
                    <g:set var="status" value="${0 }"/>
                    <g:each var="productEntry" in="${command?.productsByCategory }" status="i">
                        <g:set var="category" value="${productEntry.key }"/>

                        <div class="box">
                            <h2>
                                <format:category category="${category}"/>

                                <g:link controller="report" action="generateTransactionReport" class="button" params="['location.id':command?.location?.id,'category.id':category?.id,'startDate':format.date(obj:command.startDate,format:'MM/dd/yyyy'),'endDate':format.date(obj:command.endDate,format:'MM/dd/yyyy'),'includeChildren':false,'insertPageBreakBetweenCategories':command?.insertPageBreakBetweenCategories,'showTransferBreakdown':command?.showTransferBreakdown,'hideInactiveProducts':command?.hideInactiveProducts]">
                                    <warehouse:message code="report.showThisCategoryOnly.label"/>
                                </g:link>
                            </h2>
                            <div class="dialog">
                                <table class="report">
                                    <thead>
                                    <tr style="border-top: 1px solid lightgrey;">
                                        <th rowspan="2" class="bottom">
                                            <warehouse:message code="report.productDescription.label"/>
                                        </th>
                                        <th rowspan="2" class="center bottom total start">
                                            <warehouse:message code="report.initialQuantity.label"/>
                                        </th>
                                        <td colspan="${(command.showTransferBreakdown) ? 3 + (transferInLocations?.size?:0) : 3}" class="center total">
                                            <label>
                                                <warehouse:message code="report.incomingQuantity.label"/>
                                            </label>
                                        </td>
                                        <td colspan="${(command.showTransferBreakdown) ? 6 + (transferOutLocations?.size?:0) : 6}" class="center total">
                                            <label>
                                                <warehouse:message code="report.outgoingQuantity.label"/>
                                            </label>
                                        </td>
                                        <th rowspan="2" class="center bottom total end">
                                            <warehouse:message code="report.finalQuantity.label"/>
                                        </th>
                                    </tr>

                                    <tr style="border-top: 1px solid lightgrey;">
                                        <th class="right">
                                            <warehouse:message code="report.incomingTransferQuantity.label"/>
                                        </th>
                                        <g:if test="${command.showTransferBreakdown }">
                                            <g:each var="location" in="${transferInLocations }">
                                                <th class="right bottom">${location.name.substring(0,3) }</th>
                                            </g:each>
                                        </g:if>
                                        <th class="right nowrap">
                                            <warehouse:message code="report.adjustedInQuantity.label"/>
                                        </th>
                                        <th class="right total">
                                            <warehouse:message code="report.incomingTotalQuantity.label"/>
                                        </th>
                                        <th class="right">
                                            <warehouse:message code="report.outgoingTransferQuantity.label"/>
                                        </th>
                                        <g:if test="${command.showTransferBreakdown }">
                                            <g:each var="location" in="${transferOutLocations }">
                                                <th class="right">${location.name.substring(0,3) }</th>
                                            </g:each>
                                        </g:if>
                                        <th class="right">
                                            <warehouse:message code="report.expiredQuantity.label"/>
                                        </th>
                                        <th class="right">
                                            <warehouse:message code="report.consumedQuantity.label"/>
                                        </th>
                                        <th class="right">
                                            <warehouse:message code="report.damagedQuantity.label"/>
                                        </th>
                                        <th class="right">
                                            <warehouse:message code="report.adjustedOutQuantity.label"/>
                                        </th>
                                        <th class="right total">
                                            <warehouse:message code="report.outgoingTotalQuantity.label"/>
                                        </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <g:each var="product" in="${productEntry.value }" status="j">
                                        <g:set var="entry" value="${command.entries[product].totals }"/>
                                        <tr class="${j%2 ? 'even' : 'odd' }">
                                            <td class="left" style="width: 35%">
                                                <g:link controller="report" action="generateTransactionReport" params="['product.id':product?.id,'category.id':category?.id,'location.id':command?.location?.id,startDate:params.startDate,endDate:params.endDate,includeChildren:command?.includeChildren,pageBreak:params.pageBreak]">
                                                    <format:product product="${product }"/>
                                                </g:link>
                                            </td>
                                            <td class="right total start nowrap">
                                                <span class="${(entry?.quantityInitial>=0)?'credit':'debit'}">
                                                    ${entry?.quantityInitial ?: 0}
                                                </span>
                                            </td>
                                            <td class="right nowrap">
                                                <span class="${(entry?.quantityTransferredIn>=0)?'credit':'debit'}">
                                                    ${entry?.quantityTransferredIn ?: 0}
                                                </span>
                                                <g:if test="${command.showTransferBreakdown}">
                                                    <img src="${resource(dir:'images/icons/silk',file:'magnifier.png')}" class="show-details middle"/>
                                                    <div class="hidden details">
                                                        <g:if test="${entry.quantityTransferredInByLocation }">
                                                            <h2>
                                                                <table>
                                                                    <tr class="unhighlight">
                                                                        <td class="">
                                                                            <format:product product="${product }"/>
                                                                        </td>
                                                                        <td class="right middle">
                                                                            <img src="${resource(dir:'images/icons/silk',file:'cross.png')}" class="close-details middle"/>
                                                                        </td>
                                                                    </tr>
                                                                </table>
                                                            </h2>
                                                            <table>
                                                                <thead>
                                                                <tr>
                                                                    <th>
                                                                        <warehouse:message code="default.from.label"/>
                                                                    </th>
                                                                    <th class="right">
                                                                        <warehouse:message code="default.qty.label"/>
                                                                    </th>
                                                                </tr>
                                                                </thead>
                                                                <tbody>
                                                                <g:each var="xferInMapEntry" in="${entry.quantityTransferredInByLocation.sort { it.key } }" status="xinStatus">
                                                                    <tr class="${xinStatus%2?'even':'odd' }">
                                                                        <td>
                                                                            ${xferInMapEntry.key }

                                                                        </td>
                                                                        <td class="right">
                                                                            ${xferInMapEntry.value }
                                                                        </td>
                                                                    </tr>
                                                                </g:each>
                                                                </tbody>
                                                                <tfoot>
                                                                <tr>
                                                                    <td>

                                                                    </td>
                                                                    <td class="right">
                                                                        ${entry?.quantityTransferredIn?:0}
                                                                    </td>
                                                                </tr>
                                                                </tfoot>
                                                            </table>
                                                        </g:if>
                                                        <g:unless test="${entry.quantityTransferredInByLocation }">
                                                            <warehouse:message code="default.none.label"/>
                                                        </g:unless>
                                                    </div>
                                                </g:if>

                                            </td>
                                            <td class="right nowrap">
                                                <span class="${(entry?.quantityFound>=0)?'credit':'debit'}">${entry?.quantityFound ?: 0}</span>
                                            </td>
                                            <td class="right total nowrap">
                                                <span class="${(entry?.quantityTotalIn>=0)?'credit':'debit'}">
                                                    ${entry?.quantityTotalIn ?: 0}
                                                </span>
                                            </td>

                                            <g:if test="${command.showTransferBreakdown }">
                                                <g:each var="location" in="${transferOutLocations }">
                                                    <td class="center nowrap">
                                                        <span class="${(entry?.quantityTransferredOutByLocation[location]>0)?'debit':'credit'}">${entry.quantityTransferredOutByLocation[location]?:0}</span>
                                                    </td>
                                                </g:each>
                                            </g:if>
                                            <td class="right nowrap">
                                                <span class="${(entry?.quantityTransferredOut>0)?'debit':'credit'}">${entry?.quantityTransferredOut?:0}</span>
                                                <g:if test="${command.showTransferBreakdown}">
                                                    <img src="${resource(dir:'images/icons/silk',file:'magnifier.png')}" class="show-details middle"/>
                                                    <div class="hidden details">
                                                        <g:if test="${entry.quantityTransferredOutByLocation }">
                                                            <h2>
                                                                <table>
                                                                    <tr class="unhighlight">
                                                                        <td class="left middle">
                                                                            <label>
                                                                                <format:product product="${product }"/>
                                                                            </label>
                                                                        </td>
                                                                        <td class="right middle">
                                                                            <img src="${resource(dir:'images/icons/silk',file:'cross.png')}" class="close-details middle"/>
                                                                        </td>
                                                                    </tr>
                                                                </table>
                                                            </h2>
                                                            <table>
                                                                <thead>
                                                                <tr>
                                                                    <th>
                                                                        <warehouse:message code="default.to.label"/>
                                                                    </th>
                                                                    <th class="right">
                                                                        <warehouse:message code="default.qty.label"/>
                                                                    </th>
                                                                </tr>
                                                                </thead>
                                                                <tbody>
                                                                <g:each var="xferOutMapEntry" in="${entry.quantityTransferredOutByLocation.sort { it.key } }" status="xoutStatus">
                                                                    <tr class="${xoutStatus%2?'even':'odd' }">
                                                                        <td>
                                                                            ${xferOutMapEntry.key }

                                                                        </td>
                                                                        <td class="right">
                                                                            ${xferOutMapEntry.value }
                                                                        </td>
                                                                    </tr>
                                                                </g:each>
                                                                </tbody>
                                                                <tfoot>
                                                                <tr>
                                                                    <td>
                                                                    </td>
                                                                    <td class="right">
                                                                        ${entry?.quantityTransferredOut?:0}
                                                                    </td>
                                                                </tr>
                                                                </tfoot>
                                                            </table>
                                                        </g:if>
                                                        <g:unless test="${entry.quantityTransferredOutByLocation }">
                                                            <warehouse:message code="default.none.label"/>
                                                        </g:unless>
                                                    </div>
                                                </g:if>
                                            </td>
                                            <td class="right nowrap">
                                                <span class="${(entry?.quantityExpired>0)?'debit':'credit'}">${entry?.quantityExpired ?: 0}</span>
                                            </td>
                                            <td class="right nowrap">
                                                <span class="${(entry?.quantityConsumed>0)?'debit':'credit'}">${entry?.quantityConsumed ?: 0}</span>
                                            </td>
                                            <td class="right nowrap">
                                                <span class="${(entry?.quantityDamaged>0)?'debit':'credit'}">${entry?.quantityDamaged ?: 0}</span>
                                            </td>
                                            <td class="right nowrap">
                                                <span class="${(entry?.quantityLost>=0)?'credit':'debit'}">${entry?.quantityLost ?: 0}</span>
                                            </td>
                                            <td class="right total nowrap">
                                                <span class="${(entry?.quantityTotalOut>0)?'debit':'credit'}">${entry?.quantityTotalOut ?: 0 }</span>
                                            </td>
                                            <td class="right total end nowrap">
                                                <span class="${(entry?.quantityFinal>=0)?'credit':'debit'}">${entry?.quantityFinal ?: 0}</span>
                                            </td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </g:each>
                </div>
            </g:else>
        </div>
    </div>
    <script>

        function showDetails() {
            //$(this).children(".actions").show();
        }

        function hideDetails() {
            $(this).parent().children(".details").hide();
        }

        $(document).ready(function() {

            /*
            $(".details").hoverIntent({
                sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
                interval: 5,   // number = milliseconds for onMouseOver polling interval
                over: showActions,     // function = onMouseOver callback (required)
                timeout: 100,   // number = milliseconds delay before onMouseOut
                out: hideActions       // function = onMouseOut callback (required)
            });
            */
            /*
            $(".show-details").mouseout(function() {
                $(this).parent().children(".details").toggle();
            });
            $(".show-details").click(function() {
                $(this).parent().children(".details").show();
            });
            */

            $(".close-details").click(function(event) {
                $(".details").hide();
            });


            $(".show-details").click(function(event) {
                //show the menu directly over the placeholder
                var details = $(this).parent().children(".details");

                // Need to toggle before setting the position
                details.toggle();

                // Set the position for the actions menu
                details.position({
                    my: "left top",
                    at: "left bottom",
                    of: $(this).closest(".show-details"),
                    //offset: "0 0"
                    collision: "flip"
                });

                // To prevent the action button from POST'ing to the server
                event.preventDefault();
            });


            $(".details").hoverIntent({
                sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
                interval: 5,   // number = milliseconds for onMouseOver polling interval
                over: showDetails,     // function = onMouseOver callback (required)
                timeout: 100,   // number = milliseconds delay before onMouseOut
                out: hideDetails       // function = onMouseOut callback (required)
            });

            // Open / Close Panel According to Cookie //
            /*
            if ($.cookie('panel') == 'open'){
                $('#parameters').slideDown('fast'); // Show on Page Load / Refresh with Animation
                $('#parameters').show(); // Show on Page Load / Refresh without Animation
                $("#hideParameters").show();
                $("#showParameters").hide();
            } else {
                $('#parameters').slideUp('fast'); // Hide on Page Load / Refresh with Animation
                $('#parameters').hide(); // Hide on Page Load / Refresh without Animation
                $("#hideParameters").hide();
                $("#showParameters").show();
            }
            */
            function toggleParameters() {
                $('#parameters').slideToggle('fast', function(){
                    if ($(this).is(':hidden')) {
                        $.cookie('panel', 'closed');
                    } else {
                        $.cookie('panel', 'open');
                    }
                });
            }


            // Toggle Panel and Set Cookie //
            $('.toggleParameters').click(function(){
                toggleParameters();
                $(".toggleParameters").toggle();

            });

        });

    </script>

    </body>
</html>