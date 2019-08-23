<div class="box dialog list">
    <h2>
        <g:message code="inventory.currentStock.label" default="Current Stock"/>
        <small><g:message code="default.allLocations.label" default="All Locations"/></small>
    </h2>
    <table>
        <thead>
        <tr>
            <th>${warehouse.message(code:'location.label')}</th>
            <th>${warehouse.message(code:'location.locationType.label')}</th>
            <th class="right">
                ${warehouse.message(code:'default.quantity.label')}
                <small>${commandInstance?.product?.unitOfMeasure}</small>
            </th>
            <th class="right">
                ${warehouse.message(code:'product.totalValue.label')}
                <small>${grailsApplication.config.openboxes.locale.defaultCurrencyCode}</small>
            </th>
        </tr>
        </thead>
        <g:if test="${quantityMap}">
            <g:set var="totalValue" value="${0 }"/>
            <g:set var="totalQuantity" value="${0 }"/>

            <tbody>
                <g:each in="${quantityMap}" var="entry">
                    <g:each in="${entry}" var="locationGroupEntry">
                        <g:set var="locations" value="${locationGroupEntry.value.locations}"/>
                        <g:set var="totalQuantity" value="${totalQuantity + locationGroupEntry.value.totalQuantity}"/>
                        <g:set var="totalValue" value="${totalValue + locationGroupEntry.value.totalValue}"/>

                        <tr class="prop header">
                            <td>
                                ${locationGroupEntry.key?:g.message(code:'default.none.label', default: "No location group")}
                                <small>(${locations.size()} locations)</small>
                            </td>
                            <td>

                            </td>
                            <td class="right">
                                <g:formatNumber number="${locationGroupEntry?.value?.totalQuantity}" format="###,###.#" maxFractionDigits="1"/>
                            </td>
                            <td class="right">
                                <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: [g.message(code:'access.accessDenied.label')])}">
                                    ${g.formatNumber(number: locationGroupEntry?.value?.totalValue, format: '###,###,##0.00', maxFractionDigits: 2) }
                                    ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                </g:hasRoleFinance>
                            </td>
                        </tr>
                        <g:each in="${locations}" var="locationEntry">
                            <tr class="prop">
                                <td class="middle indent">
                                    <g:link controller="dashboard" action="chooseLocation" id="${locationEntry?.location?.id}"
                                            params="['targetUri':targetUri]">
                                        ${locationEntry?.location?.name}
                                    </g:link>
                                </td>
                                <td class="middle">
                                    <format:metadata obj="${locationEntry?.location?.locationType?.name}"/>
                                </td>
                                <td class="middle right">
                                    <g:formatNumber number="${locationEntry?.quantity}" format="###,###.#" maxFractionDigits="1"/>
                                </td>
                                <td class="middle right">
                                    <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message',
                                            args: [g.message(code:'access.accessDenied.label')])}">
                                        ${g.formatNumber(number: locationEntry?.value, format: '###,###,##0.00', maxFractionDigits: 2) }
                                        ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </g:hasRoleFinance>
                                </td>
                            </tr>
                        </g:each>
                    </g:each>

                </g:each>
            </tbody>
            <tfoot>
                <tr class="summary">

                    <th><warehouse:message code="default.total.label"/></th>
                    <th></th>
                    <th class="right">
                        <g:formatNumber number="${totalQuantity?:0}" format="###,###.#" maxFractionDigits="1"/>
                    </th>
                    <th class="right">
                        <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.blurred.message', args: [g.message(code:'access.accessDenied.label')])}">
                            <g:formatNumber number="${totalValue?:0}" format="###,##0.00" maxFractionDigits="2"/>
                            ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                        </g:hasRoleFinance>
                    </th>
                </tr>
            </tfoot>
        </g:if>
        <g:unless test="${quantityMap}">
            <tr>
                <td colspan="5">
                    <div class="empty center fade">
                        <warehouse:message code="inventory.quantityOnHand.unavailable.label" default="No quantity on hand at any locations."/>
                    </div>
                </td>

            </tr>
        </g:unless>
    </table>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jqgrid/4.6.0/js/jquery.jqGrid.min.js" type="text/javascript" ></script>

