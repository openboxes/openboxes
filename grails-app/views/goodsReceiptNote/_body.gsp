<div class="page dialog" style="page-break-after: ${pageBreakAfter};">
    <table>
        <thead>
            <tr>
                <th></th>
                <th>${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th>${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
                <th>${warehouse.message(code: 'default.uom.label')}</th>
                <th>${warehouse.message(code: 'shipmentItem.quantityShipped.label')}</th>
                <g:each in="${binLocations}" var="binLocation">
                    <th>
                        ${binLocation}
                    </th>
                </g:each>
                <th class="center">${warehouse.message(code: 'shipmentItem.discrepancy.label')}</th>
                <th>${warehouse.message(code: 'default.comment.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:unless test="${shipment.shipmentItems}">
                <tr>
                    <td colspan="8" class="middle center">
                        <span class="fade">
                            <warehouse:message code="default.none.label"/>
                        </span>
                    </td>
                </tr>
            </g:unless>
            <g:each in="${shipment?.shipmentItems?.findAll { it.receiptItems }?.sort()}" status="i" var="shipmentItem">
                <g:each in="${shipmentItem.receiptItems.sort { !it.isSplitItem }}" var="receiptItem">
                    <g:if test="${previousReceiptItem?.shipmentItem != receiptItem.shipmentItem}">
                        <tr class="prop">
                            <td>
                                ${i+1}
                            </td>
                            <td>
                                <g:if test="${receiptItem.isSplitItem}">
                                    <div class="canceled">
                                        ${shipmentItem?.product?.productCode}
                                    </div>
                                    <g:each in="${shipmentItem.receiptItems.sort()}" var="item">
                                        <div>
                                            ${item?.product?.productCode}
                                        </div>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <div>
                                        ${receiptItem?.product?.productCode}
                                    </div>
                                </g:else>
                            </td>
                            <td>
                                <g:if test="${receiptItem?.isSplitItem}">
                                    <div class="canceled product-name">
                                        ${shipmentItem?.product?.name}
                                    </div>
                                    <g:each in="${shipmentItem.receiptItems.sort()}" var="item">
                                        <div class="product-name">
                                            ${item?.product?.name}
                                        </div>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <div class="product-name">
                                        ${receiptItem?.product?.name}
                                    </div>
                                </g:else>
                            </td>
                            <td>
                                <g:if test="${receiptItem?.lotNumber != receiptItem?.shipmentItem?.lotNumber || receiptItem?.isSplitItem}">
                                    <g:if test="${receiptItem?.shipmentItem?.lotNumber}">
                                        <div class="canceled">
                                            ${receiptItem?.shipmentItem?.inventoryItem?.lotNumber}
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div>
                                            &nbsp
                                        </div>
                                    </g:else>
                                    <g:if test="${receiptItem?.isSplitItem}">
                                        <g:each in="${shipmentItem.receiptItems.sort()}" var="item">
                                            <div>
                                                ${item?.inventoryItem?.lotNumber}
                                            </div>
                                        </g:each>
                                    </g:if>
                                </g:if>
                                <g:if test="${!receiptItem?.isSplitItem}">
                                    <div>
                                        ${receiptItem?.inventoryItem?.lotNumber}
                                    </div>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${receiptItem?.inventoryItem?.expirationDate != receiptItem.shipmentItem.inventoryItem?.expirationDate || receiptItem?.isSplitItem}">
                                    <g:if test="${receiptItem?.shipmentItem?.lotNumber}">
                                        <div class="canceled">
                                            <g:formatDate date="${receiptItem?.shipmentItem?.inventoryItem?.expirationDate}" format="dd/MMM/yyyy"/>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div>
                                            &nbsp
                                        </div>
                                    </g:else>
                                    <g:if test="${receiptItem?.isSplitItem}">
                                        <g:each in="${shipmentItem.receiptItems.sort()}" var="item">
                                            <div>
                                                <g:formatDate date="${item?.expirationDate}" format="dd/MMM/yyyy"/>
                                            </div>
                                        </g:each>
                                    </g:if>
                                </g:if>
                                <g:if test="${!receiptItem?.isSplitItem}">
                                    <div>
                                    <g:formatDate date="${receiptItem?.expirationDate}" format="dd/MMM/yyyy"/>
                                    </div>
                                </g:if>
                            </td>
                            <td>
                                ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:"default.each.label")}
                            </td>
                            <td>
                                <g:if test="${receiptItem?.isSplitItem}">
                                    <div class="canceled">
                                        ${shipmentItem?.quantity}
                                    </div>
                                    <g:each in="${shipmentItem.receiptItems.sort()}" var="item">
                                        <div>
                                            ${item?.quantityShipped}
                                        </div>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    ${receiptItem?.quantityShipped}
                                </g:else>
                            </td>
                            <g:each in="${binLocations}" var="binLocation">
                                <td>
                                    <g:if test="${receiptItem?.isSplitItem}">
                                        <div>
                                            &nbsp
                                        </div>
                                    </g:if>
                                    <g:each in="${shipmentItem.receiptItems.sort()}" status="j" var="item">
                                        <g:set value="${shipmentItem.receiptItems.any { it.isSplitItem }}" var="isSplitItem"/>
                                        <g:set value="${!isSplitItem && !shipmentItem.receiptItems.any { it.binLocation == binLocation} && j == 0}" var="noReceivedItems"/>
                                        <g:if test="${item.binLocation == binLocation}">
                                            <div>
                                                ${item?.quantityReceived}
                                            </div>
                                        </g:if>
                                        <g:elseif test="${isSplitItem || noReceivedItems}">
                                            <div>
                                                0
                                            </div>
                                        </g:elseif>
                                    </g:each>
                                </td>
                            </g:each>
                            <td>
                                <g:if test="${receiptItem?.isSplitItem}">
                                    <div>
                                        &nbsp
                                    </div>
                                    <g:each in="${shipmentItem.receiptItems.sort()}" var="item">
                                        <g:set var="discrepancy" value="${item.quantityShipped - item.quantityReceived }" />
                                        <div class="center">
                                            ${discrepancy}
                                        </div>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <g:set var="discrepancy" value="${shipmentItem.quantity - shipmentItem.receiptItems.sum { it.quantityReceived }}" />
                                    <div class="center">
                                        ${discrepancy}
                                    </div>
                                </g:else>
                            </td>
                            <td>
                                <g:if test="${receiptItem?.isSplitItem}">
                                    <g:each in="${shipmentItem.receiptItems.sort()}" var="item">
                                        <div>
                                            ${item?.comment}
                                        </div>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <g:each in="${shipmentItem.receiptItems.find { it.comment }}" var="item">
                                            ${item?.comment}
                                    </g:each>
                                </g:else>
                            </td>
                        </tr>
                    </g:if>
                <g:set var="previousReceiptItem" value="${receiptItem}"/>
                </g:each>
            </g:each>
        </tbody>
    </table>
</div>

