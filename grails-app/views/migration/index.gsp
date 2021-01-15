<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom">
</head>
<body>

    <div class="dialog">
        <div class="button-bar">
            <div id="migration-status" class="right tag tag-info">None</div>
            <g:link class="button" action="index"><g:message code="default.list.label" args="[g.message(code:'migrations.label', default: 'Migrations')]"/></g:link>
        </div>
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>
        <div id="status">
            <div id="message"></div>
        </div>

        <div class="tabs">
            <ul>
                <li><a href="#tabs-1"><warehouse:message code="data.quality.label" default="Quality"/></a></li>
                <li><a href="#tabs-2"><warehouse:message code="data.migration.label" default="Migration"/></a></li>
                <li><a href="#tabs-3"><warehouse:message code="data.dimensions.label" default="Dimensions"/></a></li>
                <li><a href="#tabs-4"><warehouse:message code="data.facts.label" default="Facts"/></a></li>
                <li><a href="#tabs-5"><warehouse:message code="data.materializedViews.label" default="Materialized Views"/></a></li>
            </ul>
            <div id="tabs-1">
                <div class="box">
                    <h2><g:message code="data.dataQuality.label" default="Data Quality"/></h2>
                    <table>
                        <thead>
                        <tr>
                            <th>Indicator</th>
                            <th>Count</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="prop">
                            <td class="name">
                                Receipts that have been received but don't have an inbound
                                transaction associated with them
                            </td>
                            <td class="value">
                                <div id="receiptsWithoutTransaction" class="indicator">
                                    <img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/>
                                </div>
                            </td>
                            <td>
                                <a href="javascript:void(0);" class="button btn-show-dialog" data-reload="true"
                                   data-title="${g.message(code:'default.dialog.label', default: 'Dialog')}"
                                   data-url="${request.contextPath}/migration/receiptsWithoutTransaction">
                                    <g:message code="default.button.list.label"/>
                                </a>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                Shipments with status shipped but don't have an outbound
                                transaction associated with them
                            </td>
                            <td class="value">
                                <div id="shipmentsWithoutTransactions" class="indicator">
                                    <img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/>
                                </div>
                            </td>
                            <td>
                                <a href="javascript:void(0);" class="button btn-show-dialog" data-reload="true"
                                   data-title="${g.message(code:'default.dialog.label', default: 'Dialog')}"
                                   data-url="${request.contextPath}/migration/shipmentsWithoutTransactions">
                                    <g:message code="default.button.list.label"/>
                                </a>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                Stock movements with status issued without shipment items
                            </td>
                            <td class="value">
                                <div id="stockMovementsWithoutShipmentItems" class="indicator">
                                    <img class="spinner" src="${resource(dir:'images/spinner.gif')}" class="middle"/>
                                </div>
                            </td>
                            <td>
                                <a href="javascript:void(0);" class="button btn-show-dialog" data-reload="true"
                                    data-height="300"
                                   data-title="${g.message(code:'default.dialog.label', default: 'Dialog')}"
                                   data-url="${request.contextPath}/migration/stockMovementsWithoutShipmentItems">
                                    <g:message code="default.button.list.label"/>
                                </a>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div id="tabs-2">
                <div class="box">
                    <h2><g:message code="data.migration.label" default="Data Migration"/></h2>
                    <table>
                        <thead>
                        <tr>
                            <th>Data</th>
                            <th>Count</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>

                        <tr class="prop">
                            <td class="name">
                                Organizations
                            </td>
                            <td class="value">${organizationCount}</td>
                            <td>
                                <div class="button-group">
                                    <g:link controller="organization" action="index" class="button">List</g:link>
                                    <g:remoteLink action="migrateOrganizations" class="button" update="status"
                                        onLoading="onLoading()" onComplete="onComplete()">Migrate</g:remoteLink>
                                    <g:remoteLink action="deleteOrganizations" class="button" update="status">Delete</g:remoteLink>
                                </div>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                Product Suppliers
                            </td>
                            <td class="value">${productSupplierCount}</td>
                            <td>
                                <div class="button-group">
                                <g:link controller="productSupplier" action="index" class="button">List</g:link>
                                <g:remoteLink action="migrateProductSuppliers" class="button" update="status"
                                              onLoading="onLoading()" onComplete="onComplete()">Migrate</g:remoteLink>
                                <g:remoteLink action="deleteProductSuppliers" class="button" update="status"
                                              onLoading="onLoading()" onComplete="onComplete()">Delete</g:remoteLink>
                                </div>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Inventory transactions should be replaced by adjustments</td>
                            <td class="value">${inventoryTransactionCount}</td>
                            <td>

                                <div class="button-group">
                                    <g:link controller="migration" action="nextInventoryTransaction" params="[max:1]" class="button" target="_blank">Next Product</g:link>
                                    <g:link controller="migration" action="locationsWithInventoryTransactions" class="button" target="_blank">View All Locations</g:link>

                                    <g:link controller="migration" action="downloadCurrentInventory" params="[format: 'csv']" class="button" target="_blank">Download Inventory (.csv)</g:link>

                                    <g:link controller="migration" action="migrateInventoryTransactions" params="[max:1, performMigration:false]" class="button" target="_blank">Preview Migration</g:link>
                                    <g:link controller="migration" action="migrateInventoryTransactions" params="[performMigration:true, format: 'json']" class="button" target="_blank">Migrate Current Location</g:link>
                                    <g:link controller="migration" action="migrateAllInventoryTransactions" class="button">Migrate All Locations</g:link>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div id="tabs-3">
                <div class="box">
                    <h2><g:message code="data.dimensions.label" default="Dimension"/></h2>
                    <table>
                        <thead>
                        <tr>
                            <th>Table</th>
                            <th>Count</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="prop">
                            <td class="name">Date Dimension</td>
                            <td class="value">${dateDimensionCount}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Location Dimension</td>
                            <td class="value">${locationDimensionCount}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Lot Dimension</td>
                            <td class="value">${lotDimensionCount}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Product Dimension</td>
                            <td class="value">${productDimensionCount}</td>
                        </tr>
                        </tbody>
                        <tfoot>
                        <tr>
                            <td></td>
                            <td>
                                <div class="button-container">
                                    <g:link controller="report" action="truncateDimensions" class="button">Truncate</g:link>
                                    <g:link controller="report" action="buildDimensions" class="button">Build</g:link>
                                </div>
                            </td>
                        </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
            <div id="tabs-4">
                <div class="box">
                    <h2><g:message code="data.facts.label" default="Facts"/></h2>
                    <table>
                        <thead>
                        <tr>
                            <th>Table</th>
                            <th>Count</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="prop">
                            <td class="name">Transaction Facts</td>
                            <td class="value">${transactionFactCount}</td>
                            <td>
                                <g:remoteLink controller="report" action="refreshTransactionFact" class="button"
                                        onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Consumption Facts</td>
                            <td class="value">${consumptionFactCount}</td>
                            <td>
                                <g:remoteLink controller="report" action="refreshConsumptionFact" class="button"
                                        onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Stockout Facts</td>
                            <td class="value">${stockoutFactCount}</td>
                            <td>
                                <g:remoteLink controller="report" action="refreshStockoutFact" class="button"
                                        onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                            </td>
                        </tr>
                        </tbody>
                        <tfoot>
                        <tr>
                            <td></td>
                            <td>
                                <div class="button-container">
                                    <g:link controller="report" action="truncateFacts" class="button">Truncate</g:link>
                                    <g:link controller="report" action="buildFacts" class="button">Build</g:link>
                                </div>
                            </td>
                            <td></td>
                        </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
            <div id="tabs-5">
                <div class="box">
                    <h2><g:message code="data.materializedViews.label" default="Materialized Views"/></h2>
                    <table>
                        <thead>
                        <tr>
                            <th>Table</th>
                            <th>Count</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="prop">
                            <td class="name">Product Demand</td>
                            <td class="value">
                                ${productDemandCount}
                            </td>
                            <td>
                                <g:remoteLink controller="report" action="refreshProductDemand" class="button"
                                              onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">Product Availability</td>
                            <td class="value">
                                ${productAvailabilityCount}
                            </td>
                            <td>
                                <g:remoteLink controller="report" action="refreshProductAvailability" class="button"
                                        onLoading="onLoading()" onComplete="onComplete()">Refresh</g:remoteLink>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
<div class="loading">Loading...</div>
<g:javascript>
    $(document).ready(function() {
        $(".loading").hide();
        $(".tabs").tabs({
            cookie : {
                expires : 1
            }
        });
        $(".indicator").each(function(index, object){
          $("#" + object.id).load('${request.contextPath}/migration/' + object.id + '?format=count');
        });
    });

    function onLoading() {
        $(".loading").show();
        $("#status").hide();
        $("#migration-status").text("Starting migration...")
    }

    function onComplete() {
        $(".loading").hide();
        $("#status").show();
        $("#migration-status").text("Completed migration! ")
    }
</g:javascript>

</body>

</html>

