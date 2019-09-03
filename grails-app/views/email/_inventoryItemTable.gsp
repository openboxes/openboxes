<table>
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
                </td>
                <td>
                    ${row.lot_number}
                </td>
                <td>
                    ${row.expiration_date}
                </td>
                <td>
                    ${row.days_until_expiry}
                </td>
                <td>
                    ${row.quantity_on_hand}
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

