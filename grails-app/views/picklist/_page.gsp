<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%
    def comparator = { a,b ->
      def itemA = a.retrievePicklistItemsSortedByBinName()[0]
      def itemB = b.retrievePicklistItemsSortedByBinName()[0]
      def nameA = itemA?.binLocation?.name
      def nameB = itemB?.binLocation?.name
      def orderA = itemA?.sortOrder
      def orderB = itemB?.sortOrder
      /* null is > than string */
      /* if both names are null or both names are equal, then compare sortOrder */
      return !nameA ? !nameB ? orderA <=> orderB : 1 : !nameB ? -1 : nameA <=> nameB ?: orderA <=> orderB
    }
%>
<div class="page-content">
    <table id="requisition-items" class="fs-repeat-header">
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
            <th class="center border-right">${warehouse.message(code: 'requisitionItem.quantityRequested.label')}</th>
            <th class="center">${warehouse.message(code: 'requisitionItem.suggestedPick.label')}</th>
            <th class="center">${warehouse.message(code:'requisitionItem.confirmedPick.label')}</th>
            <th class="center">${warehouse.message(code:'stockMovement.comments.label')}</th>
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
                    <g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItemsSortedByBinName()}"/>
                </g:if>
                <g:else>
                    <g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItems()}"/>
                </g:else>
                <g:set var="numInventoryItem" value="${picklistItems?.size() ?: 1}"/>
            </g:if>
            <g:else>
            <%--<g:set var="numInventoryItem" value="${requisitionItem?.calculateNumInventoryItem() ?: 1}"/>--%>
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
                            <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                <div class="canceled">${requisitionItem?.parentRequisitionItem?.product?.productCode}</div>
                            </g:if>
                            <div class="${requisitionItem?.status}">
                                ${requisitionItem?.product?.productCode}
                            </div>
                        </g:if>
                    </td>
                    <td class="middle">
                        <g:if test="${j==0}">
                            <g:if test="${requisitionItem?.parentRequisitionItem?.isSubstituted()}">
                                <div class="canceled">
                                    ${StringEscapeUtils.escapeXml(requisitionItem?.parentRequisitionItem?.product?.name)}
                                </div>
                            </g:if>
                            <div class="${requisitionItem?.status}">
                                ${StringEscapeUtils.escapeXml(requisitionItem?.product?.name)}
                            </div>
                        </g:if>
                    </td>
                    <td class="center middle">
                        <g:set var="binLocation" value="${requisitionItem?.product?.getInventoryLevel(location?.id)?.binLocation}"/>
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
                    <td class="center middle">
                        <g:if test="${j==0}">
                            <g:if test="${requisitionItem.parentRequisitionItem?.isChanged()}">
                                <div class="canceled">
                                    ${requisitionItem?.parentRequisitionItem?.quantity ?: 0}
                                    ${requisitionItem?.parentRequisitionItem?.product?.unitOfMeasure ?: "EA"}
                                </div>
                            </g:if>
                            <div class="${requisitionItem?.status}">
                                ${requisitionItem?.quantity ?: 0} ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                            </div>
                        </g:if>
                    </td>
                    <td class="middle center">
                        <g:if test="${picklistItems}">
                            ${picklistItems[j]?.quantity ?: 0}
                            ${requisitionItem?.product?.unitOfMeasure ?: "EA"}
                        </g:if>
                    </td>
                    <td class="center middle">
                        <!-- Checked by -->
                    </td>
                    <td class="middle">
                        <g:if test="${requisitionItem?.parentRequisitionItem?.cancelReasonCode}">
                            <g:if test="${requisitionItem.parentRequisitionItem?.isSubstituted()}">
                                ${warehouse.message(code:'requisitionItem.substituted.label')}
                            </g:if>
                            <g:elseif test="${requisitionItem.parentRequisitionItem?.isChanged()}">
                                ${warehouse.message(code:'requisitionItem.modified.label')}
                            </g:elseif>
                            <i>
                                ${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.parentRequisitionItem?.cancelReasonCode)}
                            </i>
                            <g:if test="${requisitionItem?.parentRequisitionItem?.cancelComments}">
                                <blockquote>
                                    ${requisitionItem?.parentRequisitionItem?.cancelComments}
                                </blockquote>
                            </g:if>
                        </g:if>
                        <g:if test="${requisitionItem?.cancelReasonCode}">
                            <g:if test="${requisitionItem?.isCanceled()}">
                                ${warehouse.message(code:'requisitionItem.canceled.label')}
                            </g:if>
                            <i>
                                ${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.cancelReasonCode)}
                            </i>
                            <g:if test="${requisitionItem?.cancelComments}">
                                <blockquote>
                                    ${requisitionItem?.cancelComments}
                                </blockquote>
                            </g:if>
                        </g:if>
                    </td>

                    <% j++ %>
                </tr>
            </g:while>
        </g:each>
        </tbody>
    </table>
</div>
