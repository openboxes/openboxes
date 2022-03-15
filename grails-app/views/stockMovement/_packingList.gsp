<%@page import="org.pih.warehouse.inventory.LotStatusCode" %>
<g:set var="shipmentInstance" value="${stockMovement?.shipment}"/>
<g:set var="shipmentItemsByContainer" value="${shipmentInstance?.shipmentItems?.groupBy { it.container } }"/>
<style>
    .recalled {
        background-color: #ffcccb;
    }
</style>
<div id="packingList" class="box dialog">
    <h2>
        <img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="contents" style="vertical-align: middle"/>
        ${warehouse.message(code:'shipping.packingList.label')}
    </h2>
    <table>
        <tr>
            <th></th>
            <th><warehouse:message code="shipping.container.label"/></th>
            <g:if test="${shipmentInstance?.isFromPurchaseOrder}">
                <th><warehouse:message code="order.orderNumber.label"/></th>
            </g:if>
            <th><warehouse:message code="product.productCode.label"/></th>
            <th><warehouse:message code="product.label"/></th>
            <th class="left">
                <warehouse:message code="receiptItem.binLocation.label" default="Bin Location"/>
                <g:if test="${shipmentInstance?.origin?.id == session.warehouse?.id}">
                    <small><warehouse:message code="location.picking.label"/></small>
                </g:if>
                <g:elseif test="${shipmentInstance?.destination?.id == session.warehouse?.id}">
                    <small><warehouse:message code="location.putaway.label"/></small>
                </g:elseif>
            </th>
            <th class="left"><warehouse:message code="default.lotSerialNo.label"/></th>
            <th class="center"><warehouse:message code="default.expires.label"/></th>
            <th class="center"><warehouse:message code="shipmentItem.quantityShipped.label"/></th>
            <g:if test="${shipmentInstance?.wasReceived()||shipmentInstance?.wasPartiallyReceived()}">
                <th class="center"><warehouse:message code="shipmentItem.quantityReceived.label" default="Received"/></th>
                <th class="center"><warehouse:message code="shipmentItem.quantityCanceled.label" default="Canceled"/></th>
            </g:if>
            <th><warehouse:message code="product.uom.label"/></th>
            <th><warehouse:message code="shipping.recipient.label"/></th>
            <th class="left"><warehouse:message code="default.comment.label"/></th>
            <th><warehouse:message code="shipmentItem.isFullyReceived.label" default="Received?"/></th>
        </tr>
        <g:if test="${shipmentInstance?.shipmentItems}">
            <g:set var="count" value="${0 }"/>
            <g:set var="previousContainer"/>
            <g:each var="shipmentItem" in="${shipmentInstance.sortShipmentItemsBySortOrder()}" status="i">
                <g:set var="rowspan" value="${shipmentItemsByContainer[shipmentItem?.container]?.size() }"/>
                <g:set var="newContainer" value="${previousContainer != shipmentItem?.container }"/>
                <tr class="prop ${(count++ % 2 == 0)?'odd':'even'} ${newContainer?'new-container':''} ${shipmentItem?.hasRecalledLot?'recalled':''} shipmentItem">
                    <td>
                        <g:if test="${shipmentItem?.hasRecalledLot}">
                            <div data-toggle="tooltip" data-placement="top" title="${g.message(code:'inventoryItem.recalledLot.label')}">
                                %{-- &#x24C7; = hexadecimal circled letter R --}%
                                <b>&#x24C7;</b>
                            </div>
                        </g:if>
                    </td>
                    <g:if test="${newContainer }">
                        <td class="top left" rowspan="${rowspan}">
                            <g:set var="container" value="${shipmentItem?.container}"/>
                            <label><g:if test="${container?.parentContainer}">${container?.parentContainer?.name }&nbsp;&rsaquo;&nbsp;</g:if><g:if test="${container?.name }">${container?.name }</g:if><g:else><warehouse:message code="shipping.unpacked.label"/></g:else></label>
                            <g:if test="${showDetails}">
                                <div class="fade">
                                    <g:if test="${container?.weight || container?.width || container?.length || container?.height}">
                                        <g:if test="${container?.weight}">
                                            ${container?.weight} ${container?.weightUnits}
                                        </g:if>
                                        <g:if test="${container?.height && container?.width && container?.length }">
                                            ${container.height} ${container?.volumeUnits} x ${container.width} ${container?.volumeUnits} x ${container.length} ${container?.volumeUnits}
                                        </g:if>
                                    </g:if>
                                </div>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${shipmentInstance?.isFromPurchaseOrder}">
                        <td>
                            ${shipmentItem?.orderNumber}
                        </td>
                    </g:if>
                    <td>
                        ${shipmentItem?.inventoryItem?.product?.productCode}
                    </td>
                    <td class="product">
                        <g:link controller="inventoryItem" action="showStockCard" id="${shipmentItem?.inventoryItem?.product?.id}">
                            <format:product product="${shipmentItem?.inventoryItem?.product}"/>
                        </g:link>
                    </td>
                    <td>
                        <g:if test="${shipmentInstance?.origin?.id == session.warehouse?.id}">
                            <g:if test="${shipmentItem?.binLocation}">
                                ${shipmentItem?.binLocation?.name}
                            </g:if>
                            <g:else>
                                ${g.message(code:'default.label')}
                            </g:else>
                        </g:if>
                        <g:elseif test="${shipmentInstance?.destination?.id == session?.warehouse?.id}">
                            <g:if test="${shipmentItem?.receiptItems}">
                                <g:each var="receiptItem" in="${shipmentItem?.receiptItems.sort { it.sortOrder }}">
                                    <div style="margin-bottom: 10px;" title="${receiptItem?.quantityReceived} ${receiptItem?.inventoryItem?.product?.unitOfMeasure?:'EA'}">
                                        ${receiptItem?.binLocation?.name?:g.message(code:'default.label')}
                                    </div>
                                </g:each>
                            </g:if>
                        </g:elseif>
                    </td>
                    <td class="lotNumber">
                        <g:if test="${shipmentItem?.receiptItems}">
                            <g:each var="receiptItem" in="${shipmentItem?.receiptItems.sort { it.sortOrder }}">
                                <div style="margin-bottom: 10px;" title="${receiptItem?.quantityReceived} ${receiptItem?.inventoryItem?.product?.unitOfMeasure?:'EA'}">
                                    ${receiptItem?.lotNumber}
                                </div>
                            </g:each>
                        </g:if>
                        <g:else>
                            ${shipmentItem?.inventoryItem?.lotNumber}
                        </g:else>
                    </td>
                    <td class="center expirationDate" nowrap="nowrap">
                        <g:if test="${shipmentItem?.receiptItems}">
                            <g:each var="receiptItem" in="${shipmentItem?.receiptItems.sort { it.sortOrder }}">
                                <div style="margin-bottom: 10px;" title="${receiptItem?.quantityReceived} ${receiptItem?.inventoryItem?.product?.unitOfMeasure?:'EA'}">
                                    <g:if test="${receiptItem?.expirationDate}">
                                        <span class="expirationDate">
                                            <g:formatDate date="${receiptItem?.expirationDate}" format="d MMM yyyy"/>
                                        </span>
                                    </g:if>
                                    <g:else>
                                        <span class="fade">
                                            ${warehouse.message(code: 'default.never.label')}
                                        </span>
                                    </g:else>
                                </div>
                            </g:each>
                        </g:if>
                        <g:elseif test="${shipmentItem?.inventoryItem?.expirationDate}">
                            <span class="expirationDate">
                                <g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="d MMM yyyy"/>
                            </span>
                        </g:elseif>
                        <g:else>
                            <span class="fade">
                                ${warehouse.message(code: 'default.never.label')}
                            </span>
                        </g:else>
                    </td>
                    <td class="center quantity">
                        <g:formatNumber number="${shipmentItem?.quantity}" format="###,##0" />
                    </td>
                    <g:if test="${shipmentInstance?.wasReceived()||shipmentInstance?.wasPartiallyReceived()}">
                        <td class="center" style="white-space:nowrap;${shipmentItem?.quantityReceived() != shipmentItem?.quantity ? ' color:red;' : ''}">
                            <g:formatNumber number="${shipmentItem?.quantityReceived()}" format="###,##0"/>
                        </td>
                        <td class="center" style="white-space:nowrap;${shipmentItem?.quantityReceived() != shipmentItem?.quantity ? ' color:red;' : ''}">
                            <g:formatNumber number="${shipmentItem?.quantityCanceled()}" format="###,##0"/>
                        </td>
                    </g:if>
                    <td>
                        ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td class="left" nowrap="nowrap">
                        <g:if test="${shipmentItem?.receiptItems}">
                            <g:each var="receiptItem" in="${shipmentItem?.receiptItems.sort { it.sortOrder }}">
                                <div style="margin-bottom: 10px;" title="${receiptItem?.quantityReceived} ${receiptItem?.inventoryItem?.product?.unitOfMeasure?:'EA'}">
                                    ${receiptItem?.recipient?.name?:g.message(code:'default.none.label')}
                                </div>
                            </g:each>
                        </g:if>
                        <g:elseif test="${shipmentItem?.recipient }">
                            <div title="${shipmentItem?.recipient?.email}">${shipmentItem?.recipient?.name}</div>
                        </g:elseif>
                        <g:else>
                            <div class="fade"><g:message code="default.none.label"/></div>
                        </g:else>
                    </td>
                    <td class="left" >
                        <g:if test="${shipmentItem?.comments}">
                            <div title="${shipmentItem?.comments.join("\r\n")}">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'note.png')}" />
                            </div>
                        </g:if>
                        <g:else>
                            <div class="fade"><g:message code="default.empty.label"/></div>
                        </g:else>
                    </td>
                    <td>
                        ${shipmentItem?.isFullyReceived()}
                    </td>
                </tr>
                <g:set var="previousContainer" value="${shipmentItem.container }"/>
            </g:each>
        </g:if>
        <g:else>
            <tr>
                <td colspan="12" class="middle center fade empty">
                    <warehouse:message code="shipment.noShipmentItems.message"/>
                </td>
            </tr>
        </g:else>

    </table>
</div>
<div id="packages" class="box">
    <h2>
        <img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="contents" style="vertical-align: middle"/>
        ${warehouse.message(code:'shipping.packages.label', default: 'Packages')}
    </h2>
    <table>
        <thead>
            <tr>
                <th><g:message code="default.actions.label" /></th>
                <th><g:message code="container.name.label"/></th>
                <th><g:message code="container.containerNumber.label"/></th>
                <th><g:message code="container.containerType.label"/></th>
                <th><g:message code="default.weight.label" default="Weight"/></th>
                <th><g:message code="default.length.label" default="Length"/></th>
                <th><g:message code="default.width.label" default="Width"/></th>
                <th><g:message code="default.height.label" default="Height"/></th>
                <th><g:message code="default.volume.label" default="Volume"/></th>
            </tr>
        </thead>
        <tbody>
            <g:set var="totalVolume" value="${0}"/>
            <g:each in="${shipmentInstance?.containers}" var="container" status="i">
                <tr class="prop ${i%2==0 ? 'odd' : 'even'}">
                    <td>
                        <span id="package-action-menu" class="action-menu">
                            <button class="action-btn">
                                <img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_arrow_down.png')}" />
                            </button>
                            <div class="actions">
                                <div class="action-menu-item">
                                    <a href="javascript:void(0);" class="btn-show-dialog" data-height="400"
                                       data-title="${warehouse.message(code:'default.button.edit.label')}"
                                       data-url="${request.contextPath}/stockMovement/containerDialog/${container?.id}?stockMovement.id=${stockMovement.id}">
                                        <img src="${createLinkTo(dir:'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                                        ${warehouse.message(code:'default.button.edit.label')}
                                    </a>
                                </div>
                                <div class="action-menu-item">
                                    <g:link controller="stockMovement" action="deleteContainer" id="${container?.id}" params="['stockMovement.id':stockMovement.id]">
                                        <img src="${createLinkTo(dir:'images/icons/silk', file: 'delete.png')}"/>&nbsp;
                                        ${warehouse.message(code:'default.button.delete.label')}
                                    </g:link>
                                </div>
                            </div>
                        </span>
                    </td>
                    <td>
                       ${container?.name}
                    </td>
                    <td>
                        ${container.containerNumber}
                    </td>
                    <td>
                        <format:metadata obj="${container?.containerType?.name}"/>
                    </td>
                    <td>
                        ${container.weight} ${container?.weightUnits}
                    </td>
                    <td>
                        ${container.length} ${container?.volumeUnits}
                    </td>
                    <td>
                        ${container.width} ${container?.volumeUnits}
                    </td>
                    <td>
                        ${container.height} ${container?.volumeUnits}
                    </td>
                    <td>
                        <g:set var="volume" value="${(container.length/1000 * container?.height/1000 * container?.width/1000)}"/>
                        <g:formatNumber number="${volume}" minFractionDigits="5"/> cbm
                    </td>
                    <td>

                    </td>
                </tr>
                <g:set var="totalVolume" value="${totalVolume+volume}"/>
            </g:each>
            <g:unless test="${shipmentInstance?.containers}">
                <tr>
                    <td colspan="12" class="fade center middle empty">
                        <g:message code="shipping.noContainers.message" default="No containers"/>
                    </td>
                </tr>
            </g:unless>
        </tbody>
        <tfoot>
            <g:if test="${shipmentInstance?.containers}">
                <tr>
                    <td></td>
                    <td><g:message code="default.total.label"/></td>
                    <td></td>
                    <td></td>
                    <td>
                        ${shipmentInstance?.totalWeightInKilograms()} kg
                    </td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td>
                        <g:formatNumber number="${totalVolume}" minFractionDigits="5"/> cbm
                    </td>
                </tr>
            </g:if>
            <tr>
                <td colspan="12">
                    <div id="packages-action-menu" class="action-menu">
                        <button class="action-btn button">
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" />
                            ${warehouse.message(code: 'default.create.label', args: [warehouse.message(code: "container.label")])}
                            <img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_arrow_down.png')}" />
                        </button>
                        <div class="actions" >
                            <g:each var="packageType" in="${grailsApplication.config.openboxes.shipping.packageTypes}">
                                <div class="action-menu-item">
                                    <a href="javascript:void(0);" class="btn-show-dialog" data-height="400"
                                       data-title="${warehouse.message(code:'packingList.createNewContainer.label', default: 'Create container')}"
                                       data-url="${request.contextPath}/stockMovement/containerDialog?packageType=${packageType?.id}&stockMovement.id=${stockMovement.id}">
                                        <img src="${createLinkTo(dir:'images/icons/silk', file: 'add.png')}"/>&nbsp;
                                        ${warehouse.message(code:'default.create.label', args: [packageType?.name])}
                                    </a>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </td>
            </tr>
        </tfoot>
    </table>
</div>
