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
                    <p>${productInventoryTransactionCount} (total), ${productInventoryTransactionInCurrentLocationCount} (current location)</p>
                    <br>
                    <p>Products that have a product inventory transaction overlapping with other type of transaction:</p>
                    <g:each var="product" in="${overlappingTransactions}">
                        <p>${product}</p>
                    </g:each>
                    <br>
                    <p>Products with old transaction: ${productsWithProductInventoryTransactionInCurrentLocation.join(', ') ?: 'None'}</p>
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
                    <h1 class=""><b>Warning!</b> Currently it takes about 1-2 minutes to migrate about ~100 transactions. Results will
                    be visible in the new tab after everything is processed (for your convenience do not close it).
                    Do not trigger migration for the same location twice (ideally each location should be processed one by one).
                    <br/>
                    Preview displays all transaction entries within this location grouped by product.
                    </h1>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
