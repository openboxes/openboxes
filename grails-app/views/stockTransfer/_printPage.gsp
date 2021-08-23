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
                <g:set var="backgroundColor" value="${(i % 2) == 0 ? '#fff' : '#f7f7f7'}"/>
                <tr class="prop" style="background-color: ${backgroundColor}">
                    <td class="center" width="1%">
                        ${i + 1}
                    </td>
                    <td class="center" width="1%">
                        ${stockTransferItem.originBinLocation?.name}
                    </td>
                    <td class="center" width="1%">
                        ${stockTransferItem.product?.productCode}
                    </td>
                    <td width="50%">
                        ${stockTransferItem.product?.name}
                    </td>
                    <td class="center" width="1%">
                        ${stockTransferItem.inventoryItem?.lotNumber}
                    </td>
                    <td class="center" width="1%">
                        <g:formatDate date="${stockTransferItem.inventoryItem?.expirationDate}" format="MM/dd/yyyy"/>
                    </td>
                    <td class="center" width="1%">
                        ${stockTransferItem.destinationBinLocation?.name}
                    </td>
                    <td class="center" width="1%">
                        ${stockTransferItem.quantity}
                    </td>
                    <td class="middle" width=30%">
                        <!-- Notes -->
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>
