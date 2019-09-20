<table width="100%" border="1">
    <thead>
        <tr>
            <th>
                <g:message code="location.binLocation.label"/>
            </th>
            <th>
                <g:message code="product.label"/>
            </th>
            <th>
                <g:message code="inventoryItem.lotNumber.label"/>
            </th>
            <th>
                <g:message code="inventoryItem.expirationDate.label"/>
            </th>
            <th>
                <g:message code="inventoryItem.daysUntilExpiry.label"/>
            </th>
            <th>
                <g:message code="default.quantityOnHand.label"/>
            </th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${inventoryItems}" var="row">
            <tr>
                <td>
                    ${row.bin_name}
                </td>
                <td>
                    ${row.product_code}
                    ${row.product_name}
                </td>
                <td>
                    ${row.lot_number}
                </td>
                <td>
                    ${row.expiration_date}
                </td>
                <td style="text-align: center">
                    ${row.days_until_expiry}
                </td>
                <td style="text-align: center">
                    ${row.quantity_on_hand}
                </td>
            </tr>
        </g:each>
        <g:unless test="${inventoryItems}">
            <tr>
                <td colspan="6" style="text-align: center">
                    <g:message code="default.noResults.label"/>
                </td>
            </tr>
        </g:unless>
    </tbody>
</table>

