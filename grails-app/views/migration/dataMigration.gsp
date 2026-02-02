<g:set
        var="recordStockTransactionSourcesMigrationEnabled"
        value="${(amountOfMissingInventoryImportTransactionSources + amountOfMissingCycleCountTransactionSources) == 0}"
/>
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
            <tr class="prop">
                <td class="name">Product Inventory transactions that should be replaced by Inventory Baseline and Adjustment pair</td>
                <td class="value">
                    <h1>${productInventoryTransactionCount} (total), ${productInventoryTransactionInCurrentLocationCount} (current location)</h1>
                    <br>
                    <h1>Products that have a product inventory transaction overlapping with other type of transaction <b>PLEASE REVIEW THESE BEFORE (OR AFTER MIGRATION)</b>:</h1>
                    <g:if test="${overlappingTransactions?.size()}">
                        <g:each var="product" in="${overlappingTransactions}">
                            <h1>${product}</h1>
                        </g:each>
                    </g:if>
                    <g:else>
                        <h1>None</h1>
                    </g:else>
                    <br>
                    <h1>Products with old transaction: ${productsWithProductInventoryTransactionInCurrentLocation.join(', ') ?: 'None'}</h1>
                </td>
                <td>
                    <div class="button-group">
                        <g:link controller="migration" action="locationsWithProductInventoryTransactions" class="button" target="_blank">
                            View All Locations with deprecated Product Inventory transaction
                        </g:link>
                        <g:link controller="migration" action="downloadCurrentInventory" params="[format: 'csv']" class="button" target="_blank">
                            Download Inventory (.csv)
                        </g:link>
                        <g:link controller="migration" action="migrateProductInventoryTransactions" params="[performMigration:false]" class="button mt-3" target="_blank">
                            <b>Preview</b> Migration for Current Location
                        </g:link>
                        <g:link controller="migration" action="migrateProductInventoryTransactions" params="[performMigration:true]" class="button my-3" target="_blank">
                            <b>Migrate</b> Current Location
                        </g:link>
                    </div>
                    <h1><b>Warning!</b> Currently it takes about couple of minutes to migrate about ~1000 transactions. Results will
                    be visible in the new tab after everything is processed (for your convenience do not close it).
                    Do not trigger migration for the same location twice (ideally each location should be processed one by one).
                    <br/>
                    Preview displays all transaction entries within this location grouped by product.
                    </h1>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">Missing transaction sources for inventory import based transactions</td>
                <td>
                    Maximum amount of inventory import transactions without transaction source: <b>${amountOfMissingInventoryImportTransactionSources}</b>
                </td>
                <td>
                    <g:if test="${productInventoryTransactionInCurrentLocationCount}">
                        <h1>
                            <span class="font-weight-bold">Important:</span> trigger the product inventory transactions migration first, before proceeding with creating the missing transaction sources.
                        </h1>

                    </g:if>
                    <g:elseif test="${amountOfMissingInventoryImportTransactionSources}">
                        <div class="button-group">
                            <g:link controller="migration" action="createMissingInventoryImportTransactionSourcesForCurrentLocation" class="button" target="_blank">Migrate inventory import transactions for current location</g:link>
                        </div>
                    </g:elseif>
                    <g:else>
                        <h1>All missing inventory import transaction sources have been created.</h1>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">Missing transaction sources for cycle count based transactions</td>
                <td>
                    Maximum amount of cycle count transactions without transaction source: <b>${amountOfMissingCycleCountTransactionSources}</b>
                </td>
                <td>
                    <g:if test="${productInventoryTransactionInCurrentLocationCount}">
                        <h1>
                            <span class="font-weight-bold">Important:</span> trigger the product inventory transactions migration first, before proceeding with creating the missing transaction sources.
                        </h1>
                    </g:if>
                    <g:elseif test="${amountOfMissingCycleCountTransactionSources}">
                        <div class="button-group">
                            <g:link
                                    controller="migration"
                                    action="createMissingCycleCountTransactionSourcesForCurrentLocation"
                                    class="button"
                                    target="_blank"
                            >
                                Migrate cycle count transactions for current location
                            </g:link>
                        </div>
                    </g:elseif>
                    <g:else>
                        <h1>All missing cycle count transaction sources have been created.</h1>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">Missing transaction sources for record stock and adjust inventory based transactions</td>
                <td>
                    The amount of missing transaction sources for record stock and adjust inventory based transactions: <b>${amountOfMissingRecordStockTransactionSources}</b>
                </td>
                <td>
                    <div class="button-group">
                        <g:if test="${!recordStockTransactionSourcesMigrationEnabled}">
                            <h1>Please migrate all missing inventory import and cycle count transaction sources first.</h1>
                        </g:if>
                        <g:elseif test="${amountOfMissingRecordStockTransactionSources}">
                            <g:link
                                    controller="migration"
                                    action="createMissingRecordStockTransactionSourcesForCurrentLocation"
                                    class="button"
                                    target="_blank"
                            >
                                Migrate record stock and adjust inventory transactions for current location
                            </g:link>
                        </g:elseif>
                        <g:else>
                            <h1>All missing record stock and adjust inventory transaction sources have been created.</h1>
                        </g:else>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
