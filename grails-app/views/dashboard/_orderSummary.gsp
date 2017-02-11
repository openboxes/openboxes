<div class="box">
    <h2>
        <warehouse:message code="order.summary.label" args="[session.warehouse.name]"/>
        <small><warehouse:message code="order.ordersInto.label" args="[session.warehouse.name]"/></small>
    </h2>
    <div class="widget-content" style="padding:0; margin:0">
        <div id="receivingsummary">
            <g:if test="${!incomingOrdersByStatus}">
                <p class="fade empty center">
                    <warehouse:message code="order.noRecent.label"/>
                </p>
            </g:if>
            <g:else>

                <table class="table">
                    <tbody>
                        <tr>
                            <g:each var="entry" in="${incomingOrdersByStatus}" status="i">
                                <g:set var="incomingOrdersValue" value="${incomingOrdersByStatus[entry.key]}"/>
                                <td class="center">
                                    <g:link controller="order" action="list" params="['status':entry?.key]">
                                        ${format.metadata(obj:entry.key)}
                                    </g:link>
                                    <g:link controller="order" action="list"
                                            params="[type:'incoming', status:entry.key, dateCreatedFrom:dateCreatedFrom, dateCreatedTo:dateCreatedTo]">
                                    <div class="indicator">
                                            ${incomingOrdersValue.objectList.size()}
                                    </div>
                                        </g:link>
                                    <g:set var="totalAmount" value="${incomingOrdersValue.objectList.sum { it.totalPrice() }}"/>
                                    <div class="">
                                        <g:if test="${totalAmount}">
                                            <g:formatNumber number="${totalAmount}" type="currency"/>
                                        </g:if>
                                        <g:else>
                                            $0.00
                                        </g:else>
                                    </div>

                                </td>
                            </g:each>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr class="odd">
                            <th>
                                <warehouse:message code="shipping.total.label"/>
                            </th>
                            <th colspan="${incomingOrdersByStatus.size()-1}" class="right">
                                <g:link controller="order" action="list" params="[dateCreatedFrom:dateCreatedFrom, dateCreatedTo:dateCreatedTo]">
                                    ${incomingOrders.size() }
                                </g:link>
                            </th>
                        </tr>
                    </tfoot>
                </table>
            </g:else>
        </div>
    </div>
</div>
