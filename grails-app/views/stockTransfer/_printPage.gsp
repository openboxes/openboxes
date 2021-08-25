<%@ page import="org.apache.commons.lang.StringEscapeUtils" defaultCodec="html" %>

<div class="page" style="page-break-after: ${pageBreakAfter};">
    <table id="requisition-items" class="fs-repeat-header" border="0">
        <thead style="display: table-row-group">
            <tr class="">
                <td colspan="10">
                    <h4 class="title">${groupName}</h4>
                </td>
            </tr>
            <tr class="theader">
                <th><warehouse:message code="report.number.label"/></th>
                <th class="center">${warehouse.message(code: 'orderItem.currentBin.label')}</th>
                <th>${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.name.label')}</th>
                <th class="center" style="min-width: 150px;">${warehouse.message(code: 'default.lotSerialNo.label')}</th>
                <th class="center">${warehouse.message(code: 'orderItem.expiry.label')}</th>
                <th class="center">${warehouse.message(code: 'orderItem.transferToBin.label')}</th>
                <th class="center">${warehouse.message(code: 'orderItem.qtyToTransfer.label')}</th>
                <th class="center" style="min-width: 100px">${warehouse.message(code:'default.notes.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:unless test="${stockTransferItems}">
                <tr>
                    <td colspan="10" class="middle center">
                        <span class="fade">
                            <warehouse:message code="default.none.label"/>
                        </span>
                    </td>
                </tr>
            </g:unless>
            <g:each in="${stockTransferItems}" status="i" var="stockTransferItem">
                <g:set var="splitItems" value="${stockTransferItem?.orderItems?.sort { a, b ->
                    a.destinationBinLocation?.name <=> b.destinationBinLocation?.name ?:
                        b.quantity <=> a.quantity }}"
                />
                <g:set var="splitItemsSize" value="${splitItems?.size() ?: 1}"/>
                <g:set var="backgroundColor" value="${(i % 2) == 0 ? '#fff' : '#f7f7f7'}"/>
                <g:set var="j" value="${0}"/>

                <g:while test="${j < splitItemsSize}">
                    <tr class="prop" style="background-color: ${backgroundColor}">
                        <g:if test="${j==0}">
                            <td class="center" width="1%" rowspan="${splitItemsSize}">
                                ${i + 1}
                            </td>
                            <td class="center" width="1%" rowspan="${splitItemsSize}">
                                ${stockTransferItem?.originBinLocation?.name}
                            </td>
                            <td class="center" width="1%" rowspan="${splitItemsSize}">
                                ${stockTransferItem?.product?.productCode}
                            </td>
                            <td width="50%" rowspan="${splitItemsSize}">
                                ${stockTransferItem?.product?.name}
                            </td>
                            <td class="center" width="1%" rowspan="${splitItemsSize}">
                                ${stockTransferItem?.inventoryItem?.lotNumber}
                            </td>
                            <td class="center" width="1%" rowspan="${splitItemsSize}">
                                <g:formatDate date="${stockTransferItem?.inventoryItem?.expirationDate}" format="MM/dd/yyyy"/>
                            </td>
                        </g:if>
                        <td class="center" width="1%">
                            <g:if test="${splitItems}">
                                ${splitItems[j]?.destinationBinLocation?.name}
                            </g:if>
                            <g:else>
                                ${stockTransferItem?.destinationBinLocation?.name}
                            </g:else>
                        </td>
                        <td class="center" width="1%">
                            <g:if test="${splitItems}">
                                ${splitItems[j]?.quantity}
                            </g:if>
                            <g:else>
                                ${stockTransferItem?.quantity}
                            </g:else>
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
