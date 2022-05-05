<%@ page import="org.apache.commons.lang.StringEscapeUtils" defaultCodec="html" %>

<div class="page" style="page-break-after: ${pageBreakAfter};">
    <table id="requisition-items" class="fs-repeat-header" border="0">
        <thead style="display: table-row-group">
            <tr class="">
                <td colspan="11">
                    <h4 class="title">${groupName}</h4>
                </td>
            </tr>
            <tr class="theader">
                <th><warehouse:message code="report.number.label"/></th>
                <th>${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.name.label')}</th>
                <th class="center" style="min-width: 150px;">${warehouse.message(code: 'default.lotSerialNo.label')}</th>
                <th class="center">${warehouse.message(code: 'orderItem.expiry.label')}</th>
                <th class="center">${warehouse.message(code: 'orderItem.transferToBin.label')}</th>
                <th class="center">${warehouse.message(code: 'orderItem.qtyToTransfer.label')}</th>
                <th class="center">${warehouse.message(code: 'inventoryLevel.binLocation.label')}</th>
                <th class="center">${warehouse.message(code: 'requisitionItem.suggestedPick.label')}</th>
                <th class="center" style="min-width: 100px">${warehouse.message(code:'default.notes.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:unless test="${lineItems}">
                <tr>
                    <td colspan="10" class="middle center">
                        <span class="fade">
                            <warehouse:message code="default.none.label"/>
                        </span>
                    </td>
                </tr>
            </g:unless>
            <g:each in="${lineItems}" status="i" var="lineItem">

                <g:set var="picklistItems" value="${pickListItemsByOrder[lineItem.id]?.findAll { it.quantity > 0 }}"/>
                <g:set var="numInventoryItem" value="${picklistItems?.size() ?: 1}"/>

                <g:set var="splitItems" value="${lineItem?.orderItems?.sort { a, b ->
                    a.destinationBinLocation?.name <=> b.destinationBinLocation?.name ?:
                            b.quantity <=> a.quantity }}"
                />
                <g:set var="backgroundColor" value="${(i % 2) == 0 ? '#fff' : '#f7f7f7'}"/>
                <g:set var="j" value="${0}"/>

                <g:while test="${j < numInventoryItem}">
                    <tr class="prop" style="background-color: ${backgroundColor}">
                        <g:if test="${j==0}">
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                ${i + 1}
                            </td>
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                ${lineItem?.product?.productCode}
                            </td>
                            <td width="50%" rowspan="${numInventoryItem}">
                                ${lineItem?.product?.name}
                            </td>
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                ${lineItem?.inventoryItem?.lotNumber}
                            </td>
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                <g:formatDate date="${lineItem?.inventoryItem?.expirationDate}" format="MM/dd/yyyy"/>
                            </td>
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                <g:if test="${splitItems}">
                                    ${splitItems[j]?.destinationBinLocation?.name}
                                </g:if>
                                <g:else>
                                    ${lineItem?.destinationBinLocation?.name}
                                </g:else>
                            </td>
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                <g:if test="${splitItems}">
                                    ${splitItems[j]?.quantity}
                                </g:if>
                                <g:else>
                                    ${lineItem?.quantity}
                                </g:else>
                            </td>
                        </g:if>
                        <td class="center middle">
                            <g:if test="${picklistItems}">
                                <div class="binLocation">
                                    ${picklistItems[j]?.binLocation?.name}
                                </div>
                            </g:if>
                        </td>
                        <td class="middle center">
                            <g:if test="${picklistItems}">
                                ${picklistItems[j]?.quantity ?: 0}
                                ${lineItem?.product?.unitOfMeasure ?: "EA"}
                            </g:if>
                        </td>
                        <td class="middle" width=30%">
                            <!-- Notes -->
                        </td>
                        <% j++ %>
                    </tr>
                </g:while>
            </g:each>
        </tbody>
    </table>
</div>
