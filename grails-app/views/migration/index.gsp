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
        <div class="box">
            <h2><g:message code="data.integrity.label" default="Data Integrity"/></h2>
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
                        Receipts that have been received but don't have an inbound transaction associated with them
                    </td>
                    <td class="value">${receiptsWithoutTransactionCount}</td>
                    <td>
                        <a href="javascript:void(0);" class="button btn-show-dialog" data-title="${g.message(code:'default.dialog.label', default: 'Dialog')}"
                           data-url="${request.contextPath}/migration/receiptsWithoutTransaction">
                            <g:message code="default.button.list.label"/>
                        </a>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        Shipments that have been shipped but don't have an outbound transaction associated with them
                    </td>
                    <td class="value">${shipmentsWithoutTransactionCount}</td>
                    <td>
                        <a href="javascript:void(0);" class="button btn-show-dialog" data-title="${g.message(code:'default.dialog.label', default: 'Dialog')}"
                           data-url="${request.contextPath}/migration/shipmentsWithoutTransaction">
                            <g:message code="default.button.list.label"/>
                        </a>
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
                </tbody>
            </table>
        </div>
        <div class="box">
            <h2><g:message code="data.warehouse.label" default="Data Warehouse"/></h2>
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
                    <td class="name">Date Dimension</td>
                    <td class="value">${dateDimensionCount}</td>
                    <td rowspan="4">
                        <div class="button-group">
                            <g:link controller="report" action="buildDimensions" class="button">Build</g:link>
                            <g:link controller="report" action="truncateDimensions" class="button">Truncate</g:link>
                        </div>
                    </td>
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
                <tr class="prop">
                    <td class="name">Transaction Fact</td>
                    <td class="value">${transactionFactCount}</td>
                    <td rowspan="2">
                        <div class="button-group">
                            <g:link controller="report" action="buildFacts" class="button">Build</g:link>
                            <g:link controller="report" action="truncateFacts" class="button">Truncate</g:link>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">Consumption Fact</td>
                    <td class="value">${consumptionFactCount}</td>
                </tr>


                </tbody>
            </table>
        </div>


    </div>

<g:javascript>

    function onLoading() {
        $("#status").hide();
        $("#migration-status").text("Starting migration...")

    }

    function onComplete() {
        $("#status").show();
        $("#migration-status").text("Completed migration! ")
    }

</g:javascript>

</body>

</html>

