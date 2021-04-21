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
