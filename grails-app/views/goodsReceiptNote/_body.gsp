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

            <g:each in="${shipment?.receipts}" var="receipt">
                <th>
                    ${warehouse.message(code: 'shipping.receipt.label')} ${receipt?.receiptNumber}
                </th>
            </g:each>

            <th class="center">
                ${warehouse.message(code: 'shipmentItem.discrepancy.label')}
            </th>
            <th>${warehouse.message(code: 'default.comment.label')}</th>
        </tr>
        </thead>

        <tbody>

        <!-- Iterating through every shipment items, lines in the pdf are receipt items related to that shipment item -->
        <g:each in="${shipment?.sortShipmentItemsBySortOrder()?.findAll { it.receiptItems }}"
                status="i"
                var="shipmentItem">

            <g:set var="receiptItems" value="${shipmentItem.receiptItems.sort { !it.isSplitItem }}"/>
            <g:set var="hasSplit" value="${receiptItems.any { it.isSplitItem }}"/>

            <!-- first row, displayed when there is a split of the original item -->
            <g:if test="${hasSplit}">
                <tr style="border-top: 2px solid lightgrey">

                    <!-- only the first line can have split, so we're displaying a number for it -->
                    <td>${i + 1}</td>

                    <!-- product code -->
                    <td class="canceled">${shipmentItem?.product?.productCode}</td>
                    <!-- display name -->
                    <td class="canceled">${shipmentItem?.product?.displayNameOrDefaultName}</td>

                    <!-- lot number -->
                    <td class="canceled">${shipmentItem?.inventoryItem?.lotNumber}</td>

                    <!-- expiration date -->
                    <td class="canceled">
                        <g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}"
                                      format="dd/MMM/yyyy"/>
                    </td>

                    <!-- unit of measure -->
                    <td class="canceled">
                        ${shipmentItem?.inventoryItem?.product?.unitOfMeasure
                                ?: warehouse.message(code: "default.each.label")}
                    </td>

                    <!-- quantity shipped -->
                    <td class="canceled">${shipmentItem?.quantity}</td>

                     <!-- generating columns for each receipt, displays quantity received in exact receipt -->
                    <g:each in="${shipment.receipts}" var="receipt">
                        <td></td>
                    </g:each>

                    <!-- we're not displaying discrepancy for split line -->
                    <td></td>

                    <!-- shipment item doesn't have a comment, so it's blank -->
                    <td></td>

                </tr>
            </g:if>

            <!-- executed when there is a split line -->
            <g:each in="${receiptItems}" status="j" var="receiptItem">

                <tr>
                    <!-- the number is displayed only for the first row of a column of items -->
                    <td>
                        <g:if test="${!hasSplit && j == 0}">
                            ${i + 1}
                        </g:if>
                    </td>

                    <!-- product code -->
                    <td>${shipmentItem?.product?.productCode}</td>
                    <!-- display name -->
                    <td>${shipmentItem?.product?.displayNameOrDefaultName}</td>

                    <!-- lot number -->
                    <td>${receiptItem?.inventoryItem?.lotNumber}</td>

                    <!-- expiration date -->
                    <td>
                        <g:formatDate date="${receiptItem?.inventoryItem?.expirationDate}"
                                      format="dd/MMM/yyyy"/>
                    </td>

                    <!-- unit of measure -->
                    <td>
                        <g:if test="${!hasSplit && j == 0}">
                            ${shipmentItem?.inventoryItem?.product?.unitOfMeasure
                                    ?: warehouse.message(code: "default.each.label")}
                        </g:if>
                    </td>

                    <!-- quantity shipped -->
                    <td>${receiptItem?.quantityShipped}</td>

                    <!-- generating columns for each receipt, displays quantity received in exact receipt -->
                    <g:each in="${shipment.receipts}" var="receipt">
                        <td>
                            <g:if test="${receiptItem.receipt == receipt}">
                                ${receiptItem.quantityReceived}
                            </g:if>
                            <g:else>
                                0
                            </g:else>
                        </td>
                    </g:each>

                    <!-- calculation of discrepancy -->
                    <td class="center">
                        ${receiptItem.quantityShipped - receiptItem.quantityReceived}
                    </td>

                    <!-- comment field -->
                    <td>${receiptItem?.comment}</td>
                </tr>

            </g:each>

        </g:each>

        </tbody>
    </table>
</div>
