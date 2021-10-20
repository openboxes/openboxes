<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%
    def comparator = { a,b ->
      def pickItemsA = pickListItemsByOrder[a.id]?.sort { pickA, pickB -> pickA.binLocation?.name <=> pickB.binLocation?.name }
      def pickItemsB = pickListItemsByOrder[b.id]?.sort { pickA, pickB -> pickA.binLocation?.name <=> pickB.binLocation?.name }
      def itemA = pickItemsA[0]
      def itemB = pickItemsB[0]
      def nameA = itemA?.binLocation?.name
      def nameB = itemB?.binLocation?.name
      def orderA = itemA?.sortOrder
      def orderB = itemB?.sortOrder
      /* null is > than string */
      /* if both names are null or both names are equal, then compare sortOrder */
      return !nameA ? !nameB ? orderA <=> orderB : 1 : !nameB ? -1 : nameA <=> nameB ?: orderA <=> orderB
    }
%>
<div>
    <table id="order-items" class="fs-repeat-header">
        <thead>
        <tr>
            <th colspan="10">${pageTitle}</th>
        </tr>
        <tr>
            <th><warehouse:message code="report.number.label"/></th>
            <th class="center">${warehouse.message(code: 'product.productCode.label')}</th>
            <th>${warehouse.message(code: 'product.label')}</th>
            <th class="center">${warehouse.message(code: 'inventoryLevel.binLocation.label')}</th>
            <th class="center">${warehouse.message(code: 'inventoryItem.lotNumber.label')}</th>
            <th class="center">${warehouse.message(code: 'inventoryItem.expirationDate.label')}</th>
            <th class="center">${warehouse.message(code: 'orderItem.suggestedPick.label')}</th>
            <th class="center">${warehouse.message(code:'orderItem.confirmedPick.label')}</th>
            <th class="center">${warehouse.message(code:'stockMovement.comments.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:unless test="${orderItems}">
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
            <g:set var="sortedOrderItems" value="${orderItems?.sort() { a,b -> comparator(a,b) }}"/>
        </g:if>
        <g:else>
            <g:set var="sortedOrderItems" value="${orderItems?.sort()}"/>
        </g:else>

        <g:each in="${sortedOrderItems}" status="i" var="orderItem">
            <g:if test="${picklist}">
                <g:if test="${sorted}">
                    <g:set var="picklistItems" value="${pickListItemsByOrder[orderItem.id]?.sort { pickA, pickB -> pickA.binLocation?.name <=> pickB.binLocation?.name }?.findAll { it.quantity > 0 }}"/>
                </g:if>
                <g:else>
                    <g:set var="picklistItems" value="${pickListItemsByOrder[orderItem.id]?.findAll { it.quantity > 0 }}"/>
                </g:else>
                <g:set var="numInventoryItem" value="${picklistItems?.size() ?: 1}"/>
            </g:if>
            <g:else>
                <g:set var="numInventoryItem" value="${1}"/>
            </g:else>
            <g:set var="j" value="${0}"/>
            <g:while test="${j < numInventoryItem}">
                <tr class="prop">
                    <td class=" middle center">
                        <g:if test="${j==0}">
                            ${i + 1}
                        </g:if>
                    </td>
                    <td class="middle center">
                        <g:if test="${j==0}">
                            ${orderItem?.product?.productCode}
                        </g:if>
                    </td>
                    <td class="middle">
                        <g:if test="${j==0}">
                            ${StringEscapeUtils.escapeXml(orderItem?.product?.name)}
                        </g:if>
                    </td>
                    <td class="center middle">
                        <g:set var="binLocation" value="${orderItem?.product?.getInventoryLevel(location?.id)?.binLocation}"/>
                        <g:each in="${binLocation?.split(';')}" var="binLocationPart">
                            ${binLocationPart}<br/>
                        </g:each>
                    </td>
                    <td class="middle center" width="25%">
                        <g:if test="${picklistItems}">
                            <span class="lotNumber">${picklistItems[j]?.inventoryItem?.lotNumber}</span>
                        </g:if>
                    </td>
                    <td class="middle center">
                        <g:if test="${picklistItems}">
                            <g:formatDate date="${picklistItems[j]?.inventoryItem?.expirationDate}" format="d MMM yyyy"/>
                        </g:if>
                    </td>
                    <td class="middle center">
                        <g:if test="${picklistItems}">
                            ${picklistItems[j]?.quantity ?: 0}
                            ${orderItem?.product?.unitOfMeasure ?: "EA"}
                        </g:if>
                    </td>
                    <td class="center middle">
                        <!-- Checked by -->
                    </td>
                    <td class="middle">
                        <!-- Comments -->
                    </td>

                    <% j++ %>
                </tr>
            </g:while>
        </g:each>
        </tbody>
    </table>
</div>
