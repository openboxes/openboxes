<%@ page import="org.pih.warehouse.core.Constants" %>
<%@ page import="org.pih.warehouse.core.ActivityCode" %>

<section id="picklist-tab" class="box dialog" aria-label="Picklist">
    <h2>
        <img src="${resource(dir:'images/icons/silk',file:'package.png')}" alt="picklist" style="vertical-align: middle"/>
        <warehouse:message code="picklist.label" default="Picklist"/>
    </h2>

    <div class="button-bar">
        <div class="button-group">
            <g:link controller="stockMovement" action="edit" id="${stockMovement?.id}" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" />&nbsp;
                <warehouse:message code="picklist.editPicks.label" default="Edit Picks"/>
            </g:link>
            <g:link controller="picklist" action="renderPdf" id="${stockMovement?.requisition?.id}" class="button" target="_blank">
                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />&nbsp;
                <warehouse:message code="picklist.button.print.label" default="Print Picklist"/>
            </g:link>
            <g:link controller="picklist" action="exportPicklistItems" id="${stockMovement?.requisition?.id}" params="[format:'csv']" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'page_save.png')}" />&nbsp;
                <warehouse:message code="picklist.button.download.label" default="Download Picklist"/>
            </g:link>
        </div>
        <g:if test="${grailsApplication.config.openboxes.stockMovement.allocate.enabled}">
            <g:isUserAdmin>
                <div class="button-group">
                    <g:if test="${stockMovement?.canGeneratePickList()}">
                        <g:link
                            controller="stockMovement"
                            action="allocate"
                            id="${stockMovement?.requisition?.id}"
                            class="button"
                        >
                            <img src="${resource(dir: 'images/icons/silk', file: 'wand.png')}" />&nbsp;
                            <warehouse:message code="stockMovement.allocate.label" default="Allocate"/>
                        </g:link>
                    </g:if>
                    <g:if test="${picklistItems}">
                        <g:link
                            controller="stockMovement"
                            action="clearPicklist"
                            id="${stockMovement?.requisition?.id}"
                            class="button"
                        >
                            <img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />&nbsp;
                            <warehouse:message code="stockMovement.clearPicklist.label" default="Clear Picklist"/>
                        </g:link>
                        <g:link
                            controller="stockMovement"
                            action="redoAutopick"
                            id="${stockMovement?.requisition?.id}"
                            class="button"
                        >
                            <img src="${resource(dir: 'images/icons/silk', file: 'arrow_redo.png')}" />&nbsp;
                            <warehouse:message code="stockMovement.redoAutopick.label" default="Redo Autopick"/>
                        </g:link>
                    </g:if>
                </div>
            </g:isUserAdmin>
        </g:if>
    </div>

    <g:if test="${picklistItems}">
        <table>
            <thead>
                <tr>
                    <th><warehouse:message code="default.actions.label" default="Actions"/></th>
                    <th><warehouse:message code="default.status.label" default="Status"/></th>
                    <th><warehouse:message code="product.productCode.label"/></th>
                    <th><warehouse:message code="product.label"/></th>
                    <th><warehouse:message code="inventoryItem.binLocation.label" default="Bin Location"/></th>
                    <th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
                    <th><warehouse:message code="inventoryItem.expirationDate.label"/></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityRequested.label" default="Qty Required"/></th>
                    <th class="center"><warehouse:message code="picklist.quantity.label" default="Qty Picked"/></th>
                    <th><warehouse:message code="reasonCode.label" default="Reason Code"/></th>
                </tr>
            </thead>
            <tbody>
                <g:each var="picklistItem" in="${picklistItems}" status="i">
                    <tr class="prop ${i % 2 == 0 ? 'odd' : 'even'}">
                        <td class="middle center">
                            <span class="action-menu">
                                <button class="action-btn">
                                    <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
                                </button>
                                <div class="actions" style="min-width: 150px;">
                                    <div class="action-menu-item">
                                        <g:link controller="picklist" action="printPickTicket"
                                                id="${picklistItem.id}" target="_blank">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" class="middle"/>
                                            &nbsp;<warehouse:message code="picklist.printPickTicket.label" default="Print Pick Ticket"/>
                                        </g:link>
                                    </div>
                                </div>
                            </span>
                        </td>
                        <td>
                            ${picklistItem.statusMessage}
                        </td>
                        <td>
                            ${picklistItem.requisitionItem?.product?.productCode}
                        </td>
                        <td>
                            <g:link controller="inventoryItem" action="showStockCard" id="${picklistItem.requisitionItem?.product?.id}">
                                ${picklistItem.requisitionItem?.product?.name}
                            </g:link>
                        </td>
                        <td>
                            ${picklistItem.binLocation?.name}
                        </td>
                        <td>
                            ${picklistItem.inventoryItem?.lotNumber}
                        </td>
                        <td class="center" nowrap="nowrap">
                            <g:if test="${picklistItem.inventoryItem?.expirationDate}">
                                <g:formatDate
                                    date="${picklistItem.inventoryItem?.expirationDate}"
                                    format="${Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT}"
                                />
                            </g:if>
                            <g:else>
                                <span class="fade">
                                    ${warehouse.message(code: 'default.never.label')}
                                </span>
                            </g:else>
                        </td>
                        <td class="center">
                            <g:formatNumber number="${picklistItem.requisitionItem?.quantity}" format="###,##0"/>
                        </td>
                        <td class="center">
                            <g:formatNumber number="${picklistItem.quantity}" format="###,##0"/>
                        </td>
                        <td>
                            ${picklistItem.reasonCode}
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="empty fade center">
            <warehouse:message code="picklist.noItems.label" default="No picklist items"/>
        </div>
    </g:else>
</section>
