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
                <th class="center">${warehouse.message(code: 'orderItem.currentBin.label')}</th>
                <th>${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.name.label')}</th>
                <th class="center" style="min-width: 150px;">${warehouse.message(code: 'default.lotSerialNo.label')}</th>
                <th class="center">${warehouse.message(code: 'orderItem.expiry.label')}</th>
                <th class="center">${warehouse.message(code: 'orderItem.transferToBin.label')}</th>
                <th class="center">${warehouse.message(code: 'orderItem.qtyToTransfer.label')}</th>
                <th class="center">${warehouse.message(code: 'requisitionItem.suggestedPick.label')}</th>
                <th class="center" style="min-width: 100px">${warehouse.message(code: 'default.notes.label')}</th>
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
            <g:each var="lineItem" in="${lineItems}" status="i">

                <g:set var="groupedPicklistItems" value="${pickListItems[lineItem]}"/>
                <g:set var="numInventoryItem" value="${groupedPicklistItems?.size() ?: 1}"/>
                <g:set var="backgroundColor" value="${(i % 2) == 0 ? '#fff' : '#f7f7f7'}"/>
                <g:set var="j" value="${0}"/>

                <g:while test="${j < numInventoryItem}">
                    <tr class="prop" style="background-color: ${backgroundColor}">
                        <g:if test="${j == 0}">
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                ${i + 1}
                            </td>
                        </g:if>
                        <td class="center middle">
                            <g:if test="${groupedPicklistItems}">
                                <div class="binLocation">
                                    ${groupedPicklistItems[j]?.binLocation?.name}
                                </div>
                            </g:if>
                        </td>
                        <g:if test="${j == 0}">
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                ${lineItem?.product?.productCode}
                            </td>
                        </g:if>
                        <g:if test="${j == 0}">
                            <td width="50%" rowspan="${numInventoryItem}">
                                ${lineItem?.product?.name}
                            </td>
                        </g:if>
                        <td class="center" width="1%">
                            ${groupedPicklistItems[j]?.inventoryItem?.lotNumber}
                        </td>
                        <td class="center" width="1%">
                            <g:formatDate date="${groupedPicklistItems[j]?.inventoryItem?.expirationDate}"
                                          format="MM/dd/yyyy"/>
                        </td>
                        <g:if test="${j == 0}">
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                ${lineItem?.destinationBinLocation?.name}
                            </td>
                        </g:if>
                        <g:if test="${j == 0}">
                            <td class="center" width="1%" rowspan="${numInventoryItem}">
                                ${lineItem?.quantity}
                            </td>
                        </g:if>
                        <td class="middle center">
                            <g:if test="${groupedPicklistItems}">
                                ${groupedPicklistItems[j]?.quantity ?: 0}
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
