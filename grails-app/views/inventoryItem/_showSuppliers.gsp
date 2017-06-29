<div class="box">
    <h2><g:message code="location.suppliers.label"></g:message></h2>
    <table class="table">
        <thead>
            <tr>
                <th><g:message code="location.supplier.label" default="Supplier"/></th>
                <th><g:message code="inventoryItem.lotNumber.label"/></th>
                <th><g:message code="default.quantity.label"/></th>
            </tr>
        </thead>
        <tbody>
            <g:each var="productSupplier" in="${productSuppliers}">
                <tr>
                    <td>${productSupplier?.shipment?.origin?.name}</td>
                    <td>${productSupplier?.inventoryItem?.lotNumber}</td>
                    <td>${productSupplier?.quantity}</td>
                </tr>
            </g:each>
            <g:unless test="${productSuppliers}">
                <tr>
                    <td colspan="3">
                        <p class="empty fade center">
                            <g:message code="default.empty.label"></g:message>
                        </p>
                    </td>
                </tr>
            </g:unless>
        </tbody>
    </table>
</div>
