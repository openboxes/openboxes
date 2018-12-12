<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        
        <title>
            <warehouse:message code="${controllerName}.${actionName}.label"/>
        </title>
    </head>    

	<body>
		<div class="body">

			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>

            <div class="yui-ga">
                <div class="yui-u first">
                    <div class="button-container button-bar">

                        <div class="right">
                            <g:link params="[format:'csv']" controller="${controllerName}" action="${actionName}" class="button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" style="vertical-align: middle"/>
                                Download as CSV
                            </g:link>


                            <div class="button-group">
                                <div id="columnChooser" class="action-menu">
                                    <button class="action-btn button">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'text_columns.png')}" style="vertical-align: middle"/>
                                        Toggle Columns
                                    </button>
                                    <div class="actions left" style="padding: 5px;">
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="0" class="toggle-visibility" checked="true"><warehouse:message code="inventoryLevel.status.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="1" class="toggle-visibility" checked="true"><warehouse:message code="product.productCode.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="2" class="toggle-visibility" checked="true"><warehouse:message code="product.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="3" class="toggle-visibility" checked="true"><warehouse:message code="product.genericProduct.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="4" class="toggle-visibility" checked="true"><warehouse:message code="category.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="5" class="toggle-visibility" checked="true"><warehouse:message code="product.manufacturer.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="6" class="toggle-visibility" checked="true"><warehouse:message code="product.vendor.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="7" class="toggle-visibility" checked="true"><warehouse:message code="inventoryLevel.binLocation.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="8" class="toggle-visibility" checked="true"><warehouse:message code="inventoryLevel.abcClass.label" default="ABC Analysis Class"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="9" class="toggle-visibility" checked="true"><warehouse:message code="product.unitOfMeasure.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="10" class="toggle-visibility" checked="true"><warehouse:message code="inventoryLevel.minimumQuantity.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="11" class="toggle-visibility" checked="true"><warehouse:message code="inventoryLevel.reorderQuantity.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="12" class="toggle-visibility" checked="true"><warehouse:message code="inventoryLevel.maximumQuantity.label"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="13" class="toggle-visibility" checked="true"><warehouse:message code="inventoryLevel.currentQuantity.label" default="Current quantity"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="14" class="toggle-visibility" checked="true"><warehouse:message code="product.pricePerUnit.label" default="Price per unit"/></label></div>
                                        <div class="action-menu-item"><label><input type="checkbox" data-column="15" class="toggle-visibility" checked="true"><warehouse:message code="product.totalValue.label" default="Total Value"/></label></div>

                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="button-group">
                            <g:link class="button" controller="dashboard">
                                <img src="${resource(dir: 'images/icons/silk', file: 'application.png')}" style="vertical-align: middle"/>
                                <warehouse:message code="inventory.backToDashboard.label"/>
                            </g:link>
                        </div>
                        <div class="button-group">
                            <div class="action-menu">
                                <button class="action-btn button">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
                                    <warehouse:message code="inventory.${actionName}.label"/>
                                </button>
                                <div class="actions">
                                    <div class="action-menu-item">
                                        <g:link controller="inventory" action="listTotalStock" class="${'listTotalStock'.equals(actionName)?'active':''}">
                                            <warehouse:message code="inventory.listTotalStock.label"/>
                                        </g:link>
                                    </div>
                                    <div class="action-menu-item">
                                        <g:link controller="inventory" action="listInStock" class="${'listInStock'.equals(actionName)?'active':''}">
                                            <warehouse:message code="inventory.listInStock.label"/>
                                        </g:link>
                                    </div>
                                    <div class="action-menu-item">
                                        <g:link controller="inventory" action="listQuantityOnHandZero" class="${'listQuantityOnHandZero'.equals(actionName)?'active':''}">
                                            <warehouse:message code="inventory.listQuantityOnHandZero.label"/>
                                        </g:link>
                                    </div>
                                    <div class="action-menu-item">
                                        <g:link controller="inventory" action="listOutOfStock" class="${'listOutOfStock'.equals(actionName)?'active':''}">
                                            <warehouse:message code="inventory.listOutOfStock.label"/>
                                        </g:link>
                                    </div>
                                    <div class="action-menu-item">
                                        <g:link controller="inventory" action="listLowStock" class="${'listLowStock'.equals(actionName)?'active':''}">
                                            <warehouse:message code="inventory.listLowStock.label"/>
                                        </g:link>
                                    </div>
                                    <div class="action-menu-item">
                                        <g:link controller="inventory" action="listReorderStock" class="${'listReorderStock'.equals(actionName)?'active':''}">
                                            <warehouse:message code="inventory.listReorderStock.label"/>
                                        </g:link>
                                    </div>
                                    <div class="action-menu-item">
                                        <g:link controller="inventory" action="listOverStock" class="${'listOverStock'.equals(actionName)?'active':''}">
                                            <warehouse:message code="inventory.listOverStock.label"/>
                                        </g:link>
                                    </div>
                                    <div class="action-menu-item">
                                        <g:link controller="inventory" action="listReconditionedStock" class="${'listReconditionedStock'.equals(actionName)?'active':''}">
                                            <warehouse:message code="inventory.listReconditionedStock.label"/>
                                        </g:link>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                    <g:set var="totalStockValue" value="${0.00}"/>



                    <div class="box">
                        <h2>
                            <warehouse:message code="${controllerName}.${actionName}.label"/> -
                            <warehouse:message code="default.showing.message" args="[quantityMap?.keySet()?.size()]"/>
                        </h2>
                        <table id="inventoryTable">
                            <thead>
                                <tr>
                                    <th class="center"><warehouse:message code="inventoryLevel.status.label"/></th>
                                    <th><warehouse:message code="product.productCode.label"/></th>
                                    <th><warehouse:message code="product.label"/></th>
                                    <th><warehouse:message code="product.genericProduct.label"/></th>
                                    <th><warehouse:message code="category.label"/></th>
                                    <th><warehouse:message code="product.manufacturer.label"/></th>
                                    <th><warehouse:message code="product.vendor.label"/></th>
                                    <th class="left"><warehouse:message code="inventoryLevel.binLocation.label"/></th>
                                    <th class="left"><warehouse:message code="inventoryLevel.abcClass.label" default="ABC Analysis Class"/></th>
                                    <th><warehouse:message code="product.unitOfMeasure.label"/></th>
                                    <th class="center"><warehouse:message code="inventoryLevel.minimumQuantity.label"/></th>
                                    <th class="center"><warehouse:message code="inventoryLevel.reorderQuantity.label"/></th>
                                    <th class="center"><warehouse:message code="inventoryLevel.maximumQuantity.label"/></th>
                                    <th class="center border-right"><warehouse:message code="inventoryLevel.currentQuantity.label" default="Current quantity"/></th>
                                    <th><warehouse:message code="product.pricePerUnit.label" default="Price per unit"/></th>
                                    <th class="center"><warehouse:message code="product.totalValue.label" default="Total amount"/></th>
                                </tr>
                            </thead>
                            <tbody>

                                <g:each var="entry" in="${quantityMap.sort()}" status="i">
                                    <g:set var="inventoryLevel" value="${entry?.key?.getInventoryLevel(session.warehouse.id)}"/>
                                    <tr class="prop ${i%2?'odd':'even'}">
                                        <td>
                                            <g:if test="${statusMap}">
                                                <g:set var="status" value="${statusMap[entry?.key]}"/>
                                            </g:if>
                                            <g:else>
                                                <g:set var="status" value="${entry?.key?.getStatus(session.warehouse.id, entry?.value?:0 as int)}"/>
                                            </g:else>
                                            ${warehouse.message(code:'enum.InventoryLevelStatus.'+status)}

                                    </td>
                                    <td>
                                        ${entry.key.productCode}
                                    </td>
                                    <td>
                                        <g:link controller="inventoryItem" action="showStockCard" id="${entry.key.id}">
                                            ${entry?.key?.name}
                                        </g:link>
                                    </td>
                                    <td>
                                        ${entry.key?.genericProduct?.name?:""}
                                    </td>
                                    <td>
                                        ${entry.key?.category?.name}

                                        </td>
                                        <td>
                                            ${entry.key.manufacturer}

                                        </td>
                                        <td>
                                            ${entry.key.vendor}
                                        </td>
                                        <td class="left">
                                            ${inventoryLevel?.binLocation?:""}
                                        </td>
                                        <td class="center">
                                            ${inventoryLevel?.abcClass?:""}
                                        </td>
                                        <td class="center">
                                            ${entry?.key?.unitOfMeasure}
                                        </td>

                                        <td class="center">
                                            ${inventoryLevel?.minQuantity?:""}
                                        </td>
                                        <td class="center">
                                            ${inventoryLevel?.reorderQuantity?:""}
                                        </td>
                                        <td class="center">
                                            ${inventoryLevel?.maxQuantity?:""}
                                        </td>
                                        <td class="center border-right">
                                            ${entry.value}
                                        </td>
                                        <td class="center">
                                            <g:if test="${entry?.key?.pricePerUnit}">
                                                <g:formatNumber number="${entry.key.pricePerUnit}" minFractionDigits="2"/>
                                            </g:if>
                                        </td>
                                        <td class="center">
                                            <g:if test="${entry.key.pricePerUnit && entry.value}">
                                                <g:set var="stockValue" value="${entry.key.pricePerUnit*entry.value}"/>
                                                <g:formatNumber number="${stockValue}" minFractionDigits="2"/>
                                                <g:set var="totalStockValue" value="${totalStockValue + stockValue}"/>
                                            </g:if>
                                            <g:else>
                                                0.00
                                            </g:else>
                                        </td>
                                    </tr>
                                </g:each>

                                <g:unless test="${quantityMap}">
                                    <tr>
                                        <td colspan="12" class="center">
                                            <div class="empty fade">
                                                <warehouse:message code="default.emptyResults.message" default="No results found"/>
                                            </div>
                                        </td>

                                    </tr>

                                </g:unless>
                            </tbody>

                            <tfoot>
                                <tr>
                                    <th colspan="16">
                                        <div class="title right middle">
                                            <warehouse:message code="inventory.totalValue.label" default="Total value"/>
                                            <g:formatNumber number="${totalStockValue}"/>
                                            ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                        </div>

                                    </th>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>
            </div>
		</div>


    <script>
        $(window).load(function(){



            var options = {
                "bProcessing": true,
                //"sServerMethod": "GET",
                "iDisplayLength": 10,
                "bSearch": false,
                "bScrollCollapse": true,
                "bJQueryUI": true,
                "bAutoWidth": true,
                "sPaginationType": "two_button",
                //"sAjaxSource": "${request.contextPath}/json/getFastMovers",
                //"fnServerParams": function ( data ) {
                    //console.log("server data " + data);
                    //var locationId = $("#locationId").val();
                    //var date = $("#date").val();
                    //data.push({ name: "location.id", value: locationId });
                    //data.push({ name: "date", value: date })
                //},
//                "fnServerData": function ( sSource, aoData, fnCallback ) {
//                    $.ajax( {
//                        "dataType": 'json',
//                        "type": "GET",
//                        "url": sSource,
//                        "data": aoData,
//                        "success": fnCallback,
//                        "timeout": 120000,   // optional if you want to handle timeouts (which you should)
//                        "error": handleAjaxError // this sets up jQuery to give me errors
//                    } );
//                },
//            "fnServerData": function ( sSource, aoData, fnCallback ) {
//                $.getJSON( sSource, aoData, function (json) {
//                    console.log(json);
//                    fnCallback(json);
//                });
//            },
                "oLanguage": {
                    "sZeroRecords": "No records found",
                    "sProcessing": "<img alt='spinner' src='${request.contextPath}/images/spinner.gif' /> Loading... "
                },
                //"fnInitComplete": fnInitComplete,
                //"iDisplayLength" : -1,
                "aLengthMenu": [
                    [5, 10, 25, 100, 1000, -1],
                    [5, 10, 25, 100, 1000, "All"]
                ],
                "aoColumns": [

                    //{ "mData": "id", "bVisible":false }, // 0
                    //{ "mData": "rank", "sWidth": "1%" }, // 1
                    { "mData": "status" },
                    { "mData": "productCode" },
                    { "mData": "name" },
                    { "mData": "genericProduct" },
                    { "mData": "category" },
                    { "mData": "manufacturer" },
                    { "mData": "vendor" },
                    { "mData": "binLocation" },
                    { "mData": "abcClassification" },
                    { "mData": "unitOfMeasure" },
                    { "mData": "minQuantity", "sType": 'numeric' },
                    { "mData": "reorderQuantity", "sType": 'numeric' },
                    { "mData": "maxQuantity", "sType": 'numeric'},
                    { "mData": "currentQuantity", "sType": 'numeric' },
                    { "mData": "unitPrice", "sType": 'numeric' },
                    { "mData": "totalValue", "sType": 'numeric' }

                ],
                "bUseRendered": false,
                "dom": '<"top"i>rt<"bottom"flp><"clear">'
                //"aaSorting": [[ 2, "desc" ], [3, "desc"]],

            };

            var dataTable = $('#inventoryTable').dataTable(options);

            $('.toggle-visibility').on( 'click', function (e) {

                var iCol = $(this).attr('data-column')
                var oTable = $('#inventoryTable').dataTable();
                var bVis = oTable.fnSettings().aoColumns[iCol].bVisible;
                oTable.fnSetColumnVis( iCol, bVis ? false : true );
                oTable.fnDraw();
            } );

            $('#column-chooser').on( 'click', function (e) {
                e.preventDefault();
            });

        });

        /*
        function handleAjaxError( xhr, status, error ) {
            if ( status === 'timeout' ) {
                alert( 'The server took too long to send the data.' );
            }
            else {
                // User probably refreshed page or clicked on a link, so this isn't really an error
                if(xhr.readyState == 0 || xhr.status == 0) {
                    return;
                }

                if (xhr.responseText) {
                    var error = eval("(" + xhr.responseText + ")");
                    alert("An error occurred on the server.  Please contact your system administrator.\n\n" + error.errorMessage);
                } else {
                    alert('An unknown error occurred on the server.  Please contact your system administrator.');
                }
            }
            console.log(dataTable);
            dataTable.fnProcessingDisplay( false );
        }
        */

    </script>

	</body>
</html>