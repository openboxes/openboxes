<table width="100%">
    <thead>
        <tr>
            <th>
                <g:message code="product.label"/>
            </th>
            <th>
                <g:message code="default.quantityOnHand.label"/>
            </th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${products}" var="row">
            <tr>
                <td>
                    ${row.product_code}
                    ${row.product_name}
                </td>
                <td>
                    ${row.current_quantity}
                </td>
            </tr>
        </g:each>
        <g:unless test="${products}">
            <tr>
                <td colspan="2" style="text-align: center">
                    <g:message code="default.noResults.label"/>
                </td>
            </tr>
        </g:unless>
    </tbody>
</table>

