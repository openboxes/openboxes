<%@ page defaultCodec="none" %>
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

            <%-- FIXME Need to figure out a better way of doing this --%>
            <g:set var="hasRoleFinance" value="${value}"/>
            <g:hasRoleFinance>
                <g:set var="hasRoleFinance" value="${true}"/>
            </g:hasRoleFinance>
            <div class="button-container button-bar">

                <div class="button-group">
                    <g:link class="button" controller="dashboard">
                        <img src="${resource(dir: 'images/icons/silk', file: 'application.png')}" style="vertical-align: middle"/>
                        <warehouse:message code="inventory.backToDashboard.label"/>
                    </g:link>
                </div>
            </div>

            <div class="yui-gf">
                <div class="box p-2">
                    <g:message
                            code="report.inventorySummary.instructions.label"
                            default="In the Inventory Summary, you find a full list of products entered into OpenBoxes along with the current quantity at your location. If you have set stock levels, you will see how the inventory compares to your desired  stock levels. Use this summary and filters to find information about a specific product or group of products, their details, and their current quantity in your location."
                    />
                </div>
                <div class="yui-u first">
                    <div class="box">
                        <h2 class="middle"><g:message code="default.filters.label"/></h2>
                        <g:form controller="${controllerName}" action="${actionName}" method="GET">
                            <div class="filters">
                                <div class="prop">
                                    <div class="filter-list-item">
                                        <label><warehouse:message code="category.label"/></label>
                                        <p>
                                            <g:selectCategory
                                                    id="category" multiple="true"
                                                    class="chzn-select-deselect filter"
                                                    data-placeholder="${g.message(code: 'category.selectCategory.label', default: 'Select a category')}"
                                                    name="categories"
                                                    noSelection="['':'']"
                                                    value="${params?.list('categories')}"
                                            />
                                        </p>
                                        <p>
                                            <label>
                                                <g:checkBox name="includeSubcategories" value="${params?.includeSubcategories}" />
                                                ${warehouse.message(
                                                        code:'report.filter.includeAllSubcategories',
                                                        default: 'Include all subcategories',
                                                )}
                                            </label>
                                        </p>
                                    </div>
                                </div>
                                <div class="buttons">
                                    <button name="button" value="run" class="button">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'play_green.png')}" />&nbsp;
                                    <g:message code="report.runReport.label"/>
                                    </button>
                                    <button name="button" value="download" class="button">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'page_white_excel.png')}" />
                                        <g:message code="default.button.downloadAsCSV.label" default="Download as CSVs"/>
                                    </button>
                                </div>
                            </div>
                        </g:form>
                    </div>

                </div>
                <div class="yui-u">

                    <g:set var="totalStockValue" value="${0.00}"/>
                    <div class="box">
                        <h2>
                            <warehouse:message code="${controllerName}.${actionName}.label"/> -
                            <warehouse:message code="default.showing.message" args="[availableItems.size()]"/>
                        </h2>
                        <table id="inventoryTable">
                            <thead>
                            <tr>
                                <th class="center"><warehouse:message code="inventoryLevel.status.label" default="Status"/></th>
                                <th><warehouse:message code="product.productCode.label" default="Code"/></th>
                                <th><warehouse:message code="product.label" default="Product"/></th>
                                <th class="left"><warehouse:message code="product.productFamily.label" default="Product Family"/></th>
                                <th><warehouse:message code="category.label" default="Category"/></th>
                                <th class="left"><warehouse:message code="inventoryLevel.abcClass.label" default="ABC Class"/></th>
                                <th><warehouse:message code="product.unitOfMeasure.label" default="Unit of measure"/></th>
                                <th class="center"><warehouse:message code="inventoryLevel.minimumQuantity.label" default="Minimum quantity"/></th>
                                <th class="center"><warehouse:message code="inventoryLevel.reorderQuantity.label" default="Reorder quantity"/></th>
                                <th class="center"><warehouse:message code="inventoryLevel.maximumQuantity.label" default="inventoryLevel.maximumQuantity.label"/></th>
                                <th class="center border-right"><warehouse:message code="inventoryLevel.currentQuantity.label" default="Current quantity"/></th>
                                <th class="center border-right"><warehouse:message code="default.quantityAvailableToPromise.label" default="Quantity ATP"/></th>
                                <th title="${g.message(code: "product.lastPaid.label", default: "Last Paid")}">
                                    <g:message code="product.unitPrice.label" default="Unit Price"/>
                                </th>
                                <th class="center"><warehouse:message code="product.totalValue.label" default="Total amount"/></th>
                            </tr>
                            </thead>
                            <tbody>

                                <g:each var="availableItem" in="${availableItems}" status="i">
                                    <g:set var="product" value="${availableItem.product}"/>
                                    <g:set var="quantity" value="${availableItem.quantity}"/>
                                    <g:set var="quantityAvailableToPromise" value="${availableItem.quantityAvailableToPromise}"/>
                                    <g:set var="inventoryLevel" value="${availableItem.inventoryLevel}"/>
                                    <tr>
                                        <td>
                                            <g:set var="status" value="${availableItem.status}"/>
                                            ${warehouse.message(code:'enum.InventoryLevelStatus.'+status)}
                                        </td>
                                        <td>
                                            ${product?.productCode}
                                        </td>
                                        <td>
                                            <g:link controller="inventoryItem" action="showStockCard" id="${product?.id}">
                                                <format:displayName product="${product}" showTooltip="${true}" />
                                                <g:renderHandlingIcons product="${product}" />
                                            </g:link>
                                        </td>
                                        <td class="left">
                                            ${product?.productFamily?.name?:""}
                                        </td>
                                        <td>
                                            ${product?.category?.name}
                                        </td>
                                        <td class="center">
                                            ${inventoryLevel?.abcClass?:""}
                                        </td>
                                        <td class="center">
                                            ${product?.unitOfMeasure}
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
                                            ${quantity}
                                        </td>
                                        <td class="center border-right">
                                            ${quantityAvailableToPromise}
                                        </td>
                                        <td class="center">
                                            <g:if test="${hasRoleFinance}">
                                                <g:formatNumber number="${product?.pricePerUnit?:0.0}" minFractionDigits="2"/>
                                                ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                            </g:if>
                                        </td>
                                        <td class="center">
                                            <g:if test="${hasRoleFinance}">
                                                <g:if test="${product?.pricePerUnit && quantity}">
                                                    <g:set var="stockValue" value="${product?.pricePerUnit*quantity}"/>
                                                    <g:formatNumber number="${stockValue}" minFractionDigits="2"/>
                                                    <g:set var="totalStockValue" value="${totalStockValue + stockValue}"/>
                                                </g:if>
                                                <g:else>
                                                    0.00
                                                </g:else>
                                                ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                            </g:if>
                                        </td>
                                    </tr>
                                </g:each>

                            </tbody>
                            <g:if test="${hasRoleFinance}">
                                <tfoot>
                                <tr>
                                    <th colspan="14">
                                        <div class="title right middle">
                                            <warehouse:message code="inventory.totalValue.label" default="Total value"/>
                                            <g:formatNumber number="${totalStockValue}"/>
                                            ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                        </div>
                                    </th>
                                </tr>
                                </tfoot>
                            </g:if>
                        </table>
                    </div>
                </div>
            </div>
        </div>


    <script>
        $(window).load(function(){
            var options = {
                "bProcessing": true,
                "iDisplayLength": 100,
                "bSearch": false,
                "bScrollCollapse": true,
                "bJQueryUI": true,
                "bAutoWidth": true,
                "bScrollInfinite": true,
                "sScrollY": 500,
                "sPaginationType": "two_button",
                "oLanguage": {
                  "sEmptyTable": "${g.message(code: 'default.dataTable.noData.label', default: 'No data available in table')}",
                  "sInfoEmpty": "${g.message(code: 'default.dataTable.showingZeroEntries.label', default: 'Showing 0 to 0 of 0 entries')}",
                  "sInfo": "${g.message(code: 'default.dataTable.showing.label', 'Showing')} " +
                    "_START_" +
                    " ${g.message(code: 'default.dataTable.to.label', default: 'to')} " +
                    "_END_" +
                    " ${g.message(code: 'default.dataTable.of.label', default: 'of')} " +
                    "_TOTAL_" +
                    " ${g.message(code: 'default.dataTable.entries.label', default: 'entries')}",
                  "sSearch": "${g.message(code: 'default.dataTable.search.label', default: 'Search:')}",
                  "sZeroRecords": "${g.message(code: 'default.dataTable.noRecordsFound.label', default: 'No records found')}",
                  "sProcessing": "<img alt='spinner' src=\"${resource(dir: 'images', file: 'spinner.gif')}\" /> ${g.message(code: 'default.loading.label', default: 'Loading...')}",
                  "sInfoFiltered": "(${g.message(code: 'default.dataTable.filteredFrom.label', default: 'filtered from')} " +
                    "_MAX_" +
                    " ${g.message(code: 'default.dataTable.totalEntries.label', default: 'total entries')})"
                },
                "aLengthMenu": [
                    [5, 15, 25, 50, 100, 500, 1000, -1],
                    [5, 15, 25, 50, 100, 500, 1000, "All"]
                ],
                "aoColumns": [
                    { "mData": "status" },
                    { "mData": "productCode" },
                    { "mData": "name" },
                    { "mData": "category" },
                    { "mData": "binLocation" },
                    { "mData": "abcClassification" },
                    { "mData": "unitOfMeasure" },
                    { "mData": "minQuantity", "sType": 'numeric' },
                    { "mData": "reorderQuantity", "sType": 'numeric' },
                    { "mData": "maxQuantity", "sType": 'numeric'},
                    { "mData": "currentQuantity", "sType": 'numeric' },
                    { "mData": "quantityAvailableToPromise", "sType": 'numeric' },
                    { "mData": "unitPrice", "sType": 'numeric' },
                    { "mData": "totalValue", "sType": 'numeric' }

                ],
                "bUseRendered": false,
                "dom": '<"top"i>rt<"bottom"flp><"clear">'

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
    </script>

	</body>
</html>
