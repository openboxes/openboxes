<div class="box dialog">
    <h2>
        <g:message code="inventory.currentStock.label" default="Current Stock"/>
        <small><g:message code="default.allLocations.label" default="All Locations"/></small>
    </h2>
    <table>
        <thead>
        <tr class="odd">
            <th>${warehouse.message(code:'location.label')}</th>
            <th>${warehouse.message(code:'locationGroup.label')}</th>
            <th>${warehouse.message(code:'location.locationType.label')}</th>
            <th>${warehouse.message(code:'default.quantity.label')}</th>
        </tr>
        </thead>
        <g:if test="${quantityMap}">
            <tbody>
                <g:set var="totalQuantity" value="${0 }"/>
                <g:set var="quantityMap" value="${quantityMap?.sort {it.value}}"/>
                <g:each in="${quantityMap}" var="entry" status="i">
                    <g:set var="totalQuantity" value="${totalQuantity + (entry?.value?:0) }"/>
                    <tr class="prop ${i%2?'even':'odd'} ">
                        <td>
                            ${entry?.key}
                        </td>
                        <td>
                            ${entry?.key?.locationGroup?.name}
                        </td>
                        <td>
                            <format:metadata obj="${entry?.key?.locationType?.name}"/>
                        </td>
                        <td>
                            <g:formatNumber number="${entry.value?:0}" format="###,###.#" maxFractionDigits="1"/>
                             ${commandInstance?.product?.unitOfMeasure}
                        </td>
                    </tr>
                </g:each>
            </tbody>
            <tfoot>
                <tr>
                    <th><warehouse:message code="default.total.label"/></th>
                    <th></th>
                    <th></th>
                    <th>
                        <g:formatNumber number="${totalQuantity?:0}" format="###,###.#" maxFractionDigits="1"/>
                        ${commandInstance?.product?.unitOfMeasure}
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

