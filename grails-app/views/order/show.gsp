<%@ page import="org.pih.warehouse.order.Order" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
<%@ page import="org.pih.warehouse.order.OrderStatus" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'order.label', default: 'Order').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">

                <g:render template="summary" model="[orderInstance:orderInstance,currentState:'showOrder']"/>

                <g:hasErrors bean="${orderInstance}">
                    <div class="errors">
                        <g:renderErrors bean="${orderInstance}" as="list" />
                    </div>
                </g:hasErrors>

                <div class="yui-gf">

                    <div class="yui-u first">
                        <div id="details" class="box">
                            <h2>
                                <warehouse:message code="order.orderHeader.label" default="Order Header"/>
                            </h2>
                            <table>
                                <tbody>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderNumber.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.orderNumber}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="default.status.label" /></label>
                                    </td>
                                    <td valign="top" id="status" class="value">
                                        <format:metadata obj="${orderInstance?.displayStatus}"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderTypeCode.label" /></label>
                                    </td>
                                    <td valign="top" id="orderTypeCode" class="value">
                                        <format:metadata obj="${orderInstance?.orderTypeCode}"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.destination.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.destination?.name }
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.origin.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.origin?.name }
                                    </td>
                                </tr>

                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.subtotal.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.subtotal?:0 }"/>
                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="orderAdjustments.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.totalAdjustments?:0 }"/>
                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.totalPrice.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.total?:0 }"/>
                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>

                                </tbody>
                            </table>
                        </div>
                        <div class="box">
                            <h2><g:message code="default.auditing.label"/></h2>
                            <table>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.orderedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.orderedBy}">
                                            <div>${orderInstance?.orderedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateOrdered}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.approvedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.approvedBy}">
                                            <div>${orderInstance?.approvedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateApproved}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.completedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:if test="${orderInstance?.completedBy}">
                                            <div>${orderInstance?.completedBy?.name }</div>
                                            <small><format:date obj="${orderInstance?.dateCompleted}"/></small>
                                        </g:if>
                                        <g:else>
                                            <g:message code="default.none.label"/>
                                        </g:else>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.createdBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div>${orderInstance?.createdBy?.name }</div>
                                        <small><format:date obj="${orderInstance?.dateCreated}"/></small>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                            <label><warehouse:message code="default.updatedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <div>${orderInstance?.updatedBy?.name }</div>
                                        <small><format:date obj="${orderInstance?.lastUpdated}"/></small>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="yui-u">
                        <div class="tabs tabs-ui">
                            <ul>
                                <li><a href="#tabs-items"><warehouse:message code="order.orderItems.label"/></a></li>
                                <g:if test="${orderInstance.orderTypeCode == OrderTypeCode.PURCHASE_ORDER}">
                                    <li><a href="#tabs-adjustments"><warehouse:message code="orderAdjustments.label"/></a></li>
                                    <li><a href="#tabs-shipments"><warehouse:message code="shipments.label"/></a></li>
                                </g:if>
                                <li><a href="#tabs-documents"><warehouse:message code="documents.label"/></a></li>
                                <li><a href="#tabs-comments"><warehouse:message code="comments.label" default="Comments"/></a></li>

                            </ul>
                            <div id="tabs-items" class="ui-tabs-hide">
                                <div id="tab-content" class="box">
                                    <h2>
                                        <warehouse:message code="order.orderItems.label"/>
                                    </h2>
                                    <g:if test="${orderInstance?.orderItems }">
                                        <table class="table table-bordered">
                                            <thead>
                                            <tr class="odd">
                                                <g:if test="${orderInstance.orderTypeCode==OrderTypeCode.TRANSFER_ORDER}">
                                                    <th><warehouse:message code="orderItem.orderItemStatusCode.label" /></th>
                                                </g:if>
                                                <th><warehouse:message code="product.productCode.label" /></th>
                                                <th><warehouse:message code="product.label" /></th>
                                                <th class="center">${warehouse.message(code: 'product.unitOfMeasure.label')}</th>
                                                <th class="right">${warehouse.message(code: 'orderItem.quantity.label')}</th>
                                                <g:if test="${orderInstance.orderTypeCode==OrderTypeCode.PURCHASE_ORDER}">
                                                    <th class="right">${warehouse.message(code: 'order.ordered.label')}</th>
                                                    <th class="right">${warehouse.message(code: 'order.shipped.label')}</th>
                                                    <th class="right">${warehouse.message(code: 'order.received.label')}</th>
                                                    <th><warehouse:message code="order.unitPrice.label" /></th>
                                                    <th><warehouse:message code="order.totalPrice.label" /></th>
                                                </g:if>
                                                <g:elseif test="${orderInstance.orderTypeCode==OrderTypeCode.TRANSFER_ORDER}">
                                                    <th><warehouse:message code="inventoryItem.lotNumber.label" /></th>
                                                    <th><warehouse:message code="inventoryItem.expirationDate.label" /></th>
                                                    <th><warehouse:message code="orderItem.originBinLocation.label" /></th>
                                                    <th><warehouse:message code="orderItem.destinationBinLocation.label" /></th>
                                                </g:elseif>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <g:each var="orderItem" in="${orderInstance?.listOrderItems()}" status="i">
                                                <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'}">
                                                    <g:if test="${orderInstance.orderTypeCode==OrderTypeCode.TRANSFER_ORDER}">
                                                        <td>
                                                            ${orderItem?.orderItemStatusCode}
                                                        </td>
                                                    </g:if>
                                                    <td>
                                                        ${orderItem?.product?.productCode?:""}
                                                    </td>

                                                    <td class="order-item-product">
                                                        <g:if test="${orderItem?.product }">
                                                            <g:link controller="inventoryItem" action="showStockCard" params="['product.id':orderItem?.product?.id]">
                                                                <format:product product="${orderItem?.product}"/>
                                                            </g:link>
                                                        </g:if>
                                                        <g:else>
                                                            ${orderItem?.description }
                                                        </g:else>
                                                    </td>
                                                    <td class="center">
                                                        ${orderItem?.unitOfMeasure}
                                                    </td>
                                                    <td class="order-item-quantity right">
                                                        ${orderItem?.quantity}
                                                    </td>
                                                    <g:if test="${orderInstance.orderTypeCode==OrderTypeCode.PURCHASE_ORDER}">
                                                        <td class="order-item-ordered right">
                                                            ${orderInstance.isPlaced()?orderItem?.quantity:0}
                                                        </td>
                                                        <td class="order-item-fullfilled right">
                                                            ${orderItem?.quantityShipped}
                                                        </td>
                                                        <td class="order-item-received right">
                                                            ${orderItem?.quantityReceived}
                                                        </td>
                                                        <td class="">
                                                            <g:formatNumber number="${orderItem?.unitPrice?:0}" />
                                                            ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                                        </td>
                                                        <td class="">
                                                            <g:formatNumber number="${orderItem?.totalPrice()?:0}" />
                                                            ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                                        </td>
                                                    </g:if>
                                                    <g:elseif test="${orderInstance.orderTypeCode==OrderTypeCode.TRANSFER_ORDER}">
                                                        <td>
                                                            ${orderItem?.inventoryItem?.lotNumber}
                                                        </td>
                                                        <td>
                                                            <g:formatDate date="${orderItem?.inventoryItem?.expirationDate}" format="MM/dd/yyyy"/>
                                                        </td>
                                                        <td>
                                                            ${orderItem?.originBinLocation}
                                                        </td>
                                                        <td>
                                                            ${orderItem?.destinationBinLocation}
                                                        </td>
                                                    </g:elseif>
                                                </tr>
                                            </g:each>
                                            </tbody>
                                            <g:if test="${orderInstance.orderTypeCode==OrderTypeCode.PURCHASE_ORDER}">
                                                <tfoot>
                                                <tr class="">
                                                    <th colspan="8" class="right">
                                                    </th>
                                                    <th colspan="1" class="left">
                                                        <g:formatNumber number="${orderInstance?.totalPrice()?:0.0 }"/>
                                                        ${orderInstance?.currencyCode?:grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                                    </th>
                                                </tr>
                                                </tfoot>
                                            </g:if>

                                        </table>
                                    </g:if>
                                    <g:else>
                                        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
                                    </g:else>
                                </div>
                            </div>
                            <div id="tabs-adjustments" class="ui-tabs-hide">
                                <div class="box">
                                    <h2>
                                        <warehouse:message code="orderAdjustments.label"/>
                                    </h2>
                                    <g:if test="${orderInstance?.orderAdjustments }">
                                        <table class="table table-bordered">
                                            <thead>
                                            <tr class="odd">
                                                <th><warehouse:message code="order.orderItem.label"/></th>
                                                <th><warehouse:message code="default.type.label"/></th>
                                                <th><warehouse:message code="default.description.label"/></th>
                                                <th><warehouse:message code="orderAdjustment.percentage.label"/></th>
                                                <th><warehouse:message code="orderAdjustment.amount.label"/></th>
                                                <th class="right"><g:message code="default.actions.label"/></th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <g:each var="orderAdjustment" in="${orderInstance.orderAdjustments}" status="status">
                                                <tr class="${status%2==0?'odd':'even'}">
                                                    <td>
                                                        ${orderAdjustment?.orderItem?.product?:g.message(code:'default.all.label')}
                                                    </td>
                                                    <td>
                                                        ${orderAdjustment?.orderAdjustmentType?.name}
                                                    </td>
                                                    <td>
                                                        ${orderAdjustment.description}
                                                    </td>
                                                    <td>
                                                        ${orderAdjustment.percentage}
                                                    </td>
                                                    <td>
                                                        <g:if test="${orderAdjustment.amount}">
                                                            ${orderAdjustment.amount}
                                                        </g:if>
                                                        <g:elseif test="${orderAdjustment.percentage}">
                                                            <g:if test="${orderAdjustment.orderItem}">
                                                                <g:formatNumber number="${orderAdjustment.orderItem.totalAdjustments}"/>
                                                            </g:if>
                                                            <g:else>
                                                                <g:formatNumber number="${orderAdjustment.totalAdjustments}"/>
                                                            </g:else>
                                                        </g:elseif>
                                                    </td>
                                                    <td class="right">
                                                        <g:hasRoleApprover>
                                                            <g:set var="isApprover" value="${true}"/>
                                                        </g:hasRoleApprover>
                                                        <g:set var="canManageAdjustments" value="${orderInstance?.status >= OrderStatus.PLACED && isApprover
                                                                || orderInstance?.status == OrderStatus.PENDING}"/>
                                                        <g:link action="editAdjustment" id="${orderAdjustment.id}" params="['order.id':orderInstance?.id]" class="button"
                                                                disabled="${!canManageAdjustments}"
                                                                disabledMessage="${g.message(code:'errors.noPermissions.label')}">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" alt="Edit" />
                                                            <g:message code="default.button.edit.label"/>
                                                        </g:link>

                                                        <g:link action="deleteAdjustment" id="${orderAdjustment.id}" params="['order.id':orderInstance?.id]" class="button"
                                                                disabled="${!canManageAdjustments}"
                                                                disabledMessage="${g.message(code:'errors.noPermissions.label')}"
                                                                onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete" />
                                                            <g:message code="default.button.delete.label"/>
                                                        </g:link>

                                                    </td>
                                                </tr>
                                            </g:each>
                                            </tbody>
                                            <tfoot>
                                            <tr>
                                                <th colspan="4">
                                                </th>
                                                <th>
                                                    <g:formatNumber number="${orderInstance.totalAdjustments}"/>
                                                </th>
                                                <th></th>
                                            </tr>
                                            </tfoot>
                                        </table>
                                    </g:if>
                                    <g:else>
                                        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
                                    </g:else>

                                </div>
                            </div>



                            <g:if test="${orderInstance.orderTypeCode == OrderTypeCode.PURCHASE_ORDER}">

                                <div id="tabs-shipments" class="ui-tabs-hide">

                                    <div class="box">
                                        <h2><warehouse:message code="shipments.label"/></h2>
                                        <g:if test="${orderInstance?.orderItems?.shipmentItems }">
                                            <table>
                                                <thead>
                                                <tr class="odd">
                                                    <th><warehouse:message code="order.orderItem.label"/></th>
                                                    <th width="25%"><warehouse:message code="product.label"/></th>
                                                    <th><warehouse:message code="shipment.label"/></th>
                                                    <th><warehouse:message code="default.type.label"/></th>
                                                    <th><warehouse:message code="default.status.label"/></th>
                                                    <th><warehouse:message code="shipmentItem.packLevel.label" default="Pack Level"/></th>
                                                    <th><warehouse:message code="inventoryItem.lotNumber.label"/></th>
                                                    <th><warehouse:message code="inventoryItem.expirationDate.label"/></th>
                                                    <th class="right"><warehouse:message code="default.quantity.label"/></th>
                                                    <th class="center"><warehouse:message code="product.unitOfMeasure.label"/></th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <g:each var="orderItem" in="${orderInstance?.orderItems}" status="i">
                                                    <g:each var="shipmentItem" in="${orderItem.shipmentItems}" status="j">
                                                    <tr class="${i%2?'even':'odd'}">
                                                        <td>
                                                            <g:if test="${!j}">
                                                                ${i+1}
                                                            </g:if>
                                                        </td>
                                                        <td>
                                                            <g:if test="${!j}">
                                                                ${shipmentItem?.product?.productCode}
                                                                <format:product product="${shipmentItem?.product}"/>
                                                            </g:if>
                                                        </td>
                                                        <td>
                                                            <g:link controller="stockMovement" action="show" id="${shipmentItem?.shipment?.id }">${shipmentItem?.shipment?.shipmentNumber} ${shipmentItem?.shipment?.name }</g:link>
                                                        </td>
                                                        <td>
                                                            <format:metadata obj="${shipmentItem?.shipment?.shipmentType}"/>
                                                        </td>
                                                        <td>
                                                            <format:metadata obj="${shipmentItem?.shipment?.currentStatus}"/>
                                                        </td>
                                                        <td class="center middle">
                                                            <g:if test="${shipmentItem?.container?.parentContainer}">
                                                                ${shipmentItem?.container?.parentContainer?.name} &rsaquo;
                                                            </g:if>
                                                            ${shipmentItem?.container?.name}
                                                        </td>
                                                        <td>
                                                            ${shipmentItem?.inventoryItem?.lotNumber}
                                                        </td>
                                                        <td>
                                                            <g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
                                                        </td>
                                                        <td class="right">
                                                            ${shipmentItem?.quantity}
                                                        </td>
                                                        <td class="center">
                                                            ${shipmentItem?.product?.unitOfMeasure}
                                                        </td>
                                                    </tr>
                                                        </g:each>
                                                </g:each>
                                                </tbody>
                                            </table>
                                        </g:if>
                                        <g:else>
                                            <div class="fade center empty"><warehouse:message code="order.noShipments.label"/></div>
                                        </g:else>
                                    </div>
                                </div>
                            </g:if>
                            <div id="tabs-documents" class="ui-tabs-hide">
                                <div class="box">
                                    <h2><warehouse:message code="documents.label"/></h2>

                                    <g:if test="${orderInstance?.documents }">
                                        <table>
                                            <thead>
                                            <tr class="odd">
                                                <th><warehouse:message code="document.filename.label" /></th>
                                                <th><warehouse:message code="document.type.label" /></th>
                                                <th><warehouse:message code="default.description.label" /></th>
                                                <th><warehouse:message code="document.size.label" /></th>
                                                <th><warehouse:message code="default.lastUpdated.label" /></th>
                                                <th><warehouse:message code="default.actions.label" /></th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <g:each var="documentInstance" in="${orderInstance?.documents}">
                                                <tr>
                                                    <td>${documentInstance?.filename} (<g:link controller="document" action="download" id="${documentInstance.id}">download</g:link>)</td>
                                                    <td><format:metadata obj="${documentInstance?.documentType}"/></td>
                                                    <td>${documentInstance?.name}</td>
                                                    <td>${documentInstance?.size} bytes</td>
                                                    <td>${documentInstance?.lastUpdated}</td>
                                                    <td align="right">
                                                        <g:link action="editDocument" id="${documentInstance.id}" params="['order.id':orderInstance?.id]">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
                                                        </g:link>

                                                        <g:link action="deleteDocument" id="${documentInstance.id}" params="['order.id':orderInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                            <img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
                                                        </g:link>
                                                    </td>
                                                </tr>
                                            </g:each>
                                            </tbody>
                                        </table>
                                    </g:if>
                                    <g:else>
                                        <div class="fade center empty"><warehouse:message code="default.noDocuments.label" /></div>
                                    </g:else>
                                </div>
                            </div>
                            <div id="tabs-comments" class="ui-tabs-hide">
                                <div class="box">
                                    <h2><warehouse:message code="comments.label"/></h2>
                                    <g:if test="${orderInstance?.comments }">
                                        <table>
                                            <thead>
                                            <tr class="odd">
                                                <th><warehouse:message code="default.to.label" /></th>
                                                <th><warehouse:message code="default.from.label" /></th>
                                                <th><warehouse:message code="default.comment.label" /></th>
                                                <th><warehouse:message code="default.date.label" /></th>
                                                <th><warehouse:message code="default.actions.label" /></th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <g:each var="commentInstance" in="${orderInstance?.comments}">
                                                <tr>
                                                    <td>
                                                        ${commentInstance?.recipient?.name}
                                                    </td>
                                                    <td>
                                                        ${commentInstance?.sender?.name}
                                                    </td>
                                                    <td>
                                                        ${commentInstance?.comment}
                                                    </td>
                                                    <td>
                                                        ${commentInstance?.lastUpdated}
                                                    </td>
                                                    <td align="right">
                                                        <g:link action="editComment" id="${commentInstance.id}" params="['order.id':orderInstance?.id]">
                                                            <img src="${createLinkTo(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
                                                        </g:link>

                                                        <g:link action="deleteComment" id="${commentInstance.id}" params="['order.id':orderInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                            <img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
                                                        </g:link>
                                                    </td>
                                                </tr>
                                            </g:each>
                                            </tbody>
                                        </table>
                                    </g:if>
                                    <g:else>
                                        <div class="fade center empty"><warehouse:message code="default.noComments.label" /></div>
                                    </g:else>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <script>
            $(document).ready(function() {
                $(".tabs").tabs({
                    cookie: {
                        expires: 1
                    }
                });
            });
        </script>
    </body>
</html>
