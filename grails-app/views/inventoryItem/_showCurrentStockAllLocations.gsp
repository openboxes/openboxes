<div class="box">
    <h2><warehouse:message code="inventory.currentStockEverywhere.label" default="Current stock everywhere"/></h2>
    <table>
        <thead>
        <tr class="odd">
            <th>${warehouse.message(code:'locationGroup.label')}</th>
            <th>${warehouse.message(code:'location.label')}</th>
            <th>${warehouse.message(code:'location.locationType.label')}</th>
            <th>${warehouse.message(code:'default.quantity.label')}</th>
        </tr>
        </thead>
        <g:if test="${quantityMap}">
            <tbody>
            <g:set var="quantityMap" value="${quantityMap?.sort {it.value}}"/>
            <g:each in="${quantityMap}" var="entry" status="i">
                <tr class="prop ${i%2?'even':'odd'} ${entry?.key?.isWarehouse()?'':'canceled'}">
                    <td>
                        ${entry?.key?.locationGroup?.name}
                    </td>
                    <td>
                        ${entry?.key}
                    </td>
                    <td>
                        <format:metadata obj="${entry?.key?.locationType?.name}"/>
                    </td>
                    <td>
                        ${entry.value} ${commandInstance?.productInstance?.unitOfMeasure}
                    </td>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <th colspan="4">
                    <div class="fade">*Cannot guarantee quantities at locations that are not managed inventories (like Wards and Pharmacies).</div>

                </th>
            </tr>
            </tfoot>
        </g:if>
        <g:unless test="${quantityMap}">
            <tr>
                <td colspan="4">
                    <div class="empty center fade">
                        <warehouse:message code="inventory.quantityOnHand.unavailable.label" default="No quantity on hand at any locations."/>
                    </div>
                </td>

            </tr>
        </g:unless>
    </table>
</div>

