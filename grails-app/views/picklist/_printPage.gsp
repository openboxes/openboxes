<%@ page import="org.apache.commons.lang.StringEscapeUtils" defaultCodec="html" %>
<%
    def comparator = { a,b ->
      def pickItemsA = pickListItemsByRequisition[a.id]?.sort { pickA, pickB -> pickA.binLocation?.name <=> pickB.binLocation?.name }
      def pickItemsB = pickListItemsByRequisition[b.id]?.sort { pickA, pickB -> pickA.binLocation?.name <=> pickB.binLocation?.name }
      def itemA = pickItemsA ? pickItemsA[0] : null
      def itemB = pickItemsB ? pickItemsB[0] : null
      def nameA = itemA?.binLocation?.name
      def nameB = itemB?.binLocation?.name
      def orderA = itemA?.sortOrder
      def orderB = itemB?.sortOrder
      /* null is > than string */
      /* if both names are null or both names are equal, then compare sortOrder */
      return !nameA ? !nameB ? orderA <=> orderB : 1 : !nameB ? -1 : nameA <=> nameB ?: orderA <=> orderB
    }
%>
<div class="page" style="page-break-after: ${pageBreakAfter};">
    <table id="requisition-items" class="fs-repeat-header" border="0">
        <thead style="display: table-row-group">
            <tr class="">
                <td colspan="13">
                    <h4 class="title">${groupName}</h4>
                </td>
            </tr>
            <tr class="theader">
                <th><warehouse:message code="report.number.label"/></th>
                <th class="center">${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.label')}</th>
                <th class="center border-right">${warehouse.message(code: 'requisitionItem.quantityRequested.label')}</th>
                <th class="center" style="min-width: 150px;">${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
                <th class="center">${warehouse.message(code: 'inventoryItem.expiry.label', default: 'Expiry')}</th>
                <th class="center">${warehouse.message(code: 'inventoryLevel.binLocation.label')}</th>
                <th class="center">${warehouse.message(code:'picklistItem.quantityRequired.label', default: 'Required')}</th>
                <th class="center">${warehouse.message(code:'picklistItem.quantityPicked.label', default: 'Picked')}</th>
                <th class="center">${warehouse.message(code:'picklistItem.quantityCanceled.label', default: 'Canceled')}</th>
                <th class="center">${warehouse.message(code:'picklistItem.quantityRemaining.label', default: 'Remaining')}</th>
                <th class="center">${warehouse.message(code:'requisitionItem.confirmedPick.label', default: 'Confirmed')}</th>
                <th class="center" style="min-width: 100px">${warehouse.message(code:'stockMovement.comments.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:unless test="${requisitionItems}">
                <tr>
                    <td colspan="10" class="middle center">
                        <span class="fade">
                            <warehouse:message code="default.none.label"/>
                        </span>
                    </td>
                </tr>

            </g:unless>

            <g:if test="${sorted}">
                <!-- Sort ascending with nulls as highest values -->
                <g:set var="sortedRequisitionItems" value="${requisitionItems?.sort() { a,b -> comparator(a,b) }}"/>
            </g:if>
            <g:else>
                <g:set var="sortedRequisitionItems" value="${requisitionItems?.sort()}"/>
            </g:else>

            <g:each in="${sortedRequisitionItems}" status="i" var="requisitionItem">

                <g:if test="${picklist}">
                    <g:if test="${sorted}">
                        <g:set var="picklistItems" value="${pickListItemsByRequisition[requisitionItem.id]?.sort { pickA, pickB -> pickA.binLocation?.name <=> pickB.binLocation?.name }?.findAll { it.quantity > 0 }}"/>
                    </g:if>
                    <g:else>
                        <g:set var="picklistItems" value="${pickListItemsByRequisition[requisitionItem.id]?.findAll { it.quantity > 0 }}"/>
                    </g:else>
                    <g:set var="numInventoryItem" value="${picklistItems?.size() ?: 1}"/>
                </g:if>
                <g:else>
                    <%--<g:set var="numInventoryItem" value="${requisitionItem?.calculateNumInventoryItem() ?: 1}"/>--%>
                    <g:set var="numInventoryItem" value="${1}"/>
                </g:else>
                <g:set var="backgroundColor" value="${(i % 2) == 0 ? '#fff' : '#f7f7f7'}"/>
                <g:set var="j" value="${0}"/>
                <g:while test="${j < numInventoryItem}">
                    <tr class="prop" style="background-color: ${backgroundColor}">
                        <g:if test="${j==0}">
                            <td class="center" rowspan="${numInventoryItem}">
                                ${i + 1}
                            </td>
                            <td class="center" rowspan="${numInventoryItem}">
                                <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                    <div class="canceled">${requisitionItem?.parentRequisitionItem?.product?.productCode}</div>
                                </g:if>
                                <div class="${requisitionItem?.status}">
                                    ${requisitionItem?.product?.productCode}
                                </div>
                                <g:displayBarcode showData="${true}" data="${requisitionItem?.product?.productCode}"/>

                            </td>
                            <td class="center" rowspan="${numInventoryItem}">
                                <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                    <div class="canceled">
                                        ${StringEscapeUtils.escapeXml(requisitionItem?.parentRequisitionItem?.product?.name)}
                                    </div>
                                </g:if>
                                <div class="${requisitionItem?.status}">
                                    ${StringEscapeUtils.escapeXml(requisitionItem?.product?.name)}
                                </div>
                            </td>
                            <td class="center" rowspan="${numInventoryItem}">
                                <g:if test="${requisitionItem?.parentRequisitionItem?.isChanged()}">
                                    <div class="canceled">
                                        ${requisitionItem?.parentRequisitionItem?.quantity ?: 0}
                                        ${requisitionItem?.parentRequisitionItem?.product?.unitOfMeasure ?: "EA"}
                                    </div>
                                </g:if>
                                <div class="${requisitionItem?.status}">
                                    ${requisitionItem?.quantity ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                                </div>
                            </td>
                        </g:if>
                        <td class="middle center">
                            <g:if test="${picklistItems}">
                                <span class="lotNumber">${picklistItems[j]?.inventoryItem?.lotNumber}</span>
                                <g:if test="${picklistItems[j]?.inventoryItem?.lotNumber}">
                                    <g:displayBarcode showData="${true}" data="${picklistItems[j]?.inventoryItem?.lotNumber}"/>
                                </g:if>
                            </g:if>
                        </td>
                        <td class="middle center">
                            <g:if test="${picklistItems}">
                                <g:formatDate date="${picklistItems[j]?.inventoryItem?.expirationDate}" format="d MMM yyyy"/>
                            </g:if>
                        </td>
                        <td class="center middle">
                            <g:if test="${picklistItems}">
                                <div class="binLocation">
                                    ${picklistItems[j]?.binLocation?.name}
                                    <g:if test="${picklistItems[j]?.binLocation?.locationNumber}">
                                        <g:displayBarcode showData="${true}" data="${picklistItems[j]?.binLocation?.locationNumber}"/>
                                    </g:if>
                                </div>
                            </g:if>
                        </td>
                        <td class="middle center">
                            <g:if test="${picklistItems}">
                                ${picklistItems[j]?.quantity ?: 0}
                                ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </g:if>
                        </td>
                        <td class="middle center">
                            <g:if test="${picklistItems}">
                                ${picklistItems[j]?.quantityPicked ?: 0}
                                ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </g:if>
                        </td>
                        <td class="middle center">
                            <g:if test="${picklistItems}">
                                ${picklistItems[j]?.quantityCanceled?:0}
                                ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </g:if>
                        </td>
                        <td class="middle center">
                            <g:if test="${picklistItems}">
                                ${picklistItems[j]?.quantityRemaining?:0}
                                ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </g:if>
                        </td>
                        <td class="middle center">
                            <!-- Confirmed quantity -->
                        </td>
                        <td class="middle">
                            <!-- Comments -->
                            <g:if test="${picklistItems}">
                                ${picklistItems[j].status}
                                <g:if test="${picklistItems[j].shortage}">
                                    <small>Shortage of ${picklistItems[j]?.quantityCanceled}
                                        ${requisitionItem?.product?.unitOfMeasure ?: "EA"} recorded by ${picklistItems[j]?.picker}
                                    (${picklistItems[j]?.reasonCode})</small>
                                </g:if>
                                <g:elseif test="${picklistItems[j].picker}">
                                    Picked by ${picklistItems[j].picker}
                                </g:elseif>
                            </g:if>
                        </td>
                        <% j++ %>
                    </tr>
                </g:while>
            </g:each>
        </tbody>
    </table>
</div>
