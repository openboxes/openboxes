<table>
    <thead>
        <tr>
            <th><g:message code="location.binLocation.label"/></th>
            <th><g:message code="inventoryItem.lotNumber.label"/></th>
            <th><g:message code="inventoryItem.expirationDate.label"/></th>
            <th><g:message code="default.quantity.label"/></th>
        </tr>
    </thead>
    <tbody>
        <g:each var="entry" in="${binLocations}">
            <tr>
                <td>${entry?.binLocation?.name?:g.message(code:'default.label')}</td>
                <td>${entry?.inventoryItem?.lotNumber}</td>
                <td><g:expirationDate date="${entry?.inventoryItem?.expirationDate}"/></td>
                <td>${entry?.quantity} ${entry?.product?.unitOfMeasure?:"EA"}</td>
            </tr>
        </g:each>
        <g:unless test="${binLocations}">
            <tr>
                <td colspan="4" class="empty fade center">
                    ${g.message(code:'default.empty.label')}
                </td>
            </tr>
        </g:unless>
    </tbody>
</table>