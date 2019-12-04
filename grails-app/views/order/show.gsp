<%@ page import="org.pih.warehouse.order.Order" %>
<%@ page import="org.pih.warehouse.order.OrderTypeCode" %>
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
                                <label><warehouse:message code="order.orderHeader.label" default="Order Header"/></label>
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
                                        <format:metadata obj="${orderInstance?.status}"/>
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
                                        <label><warehouse:message code="order.dateOrdered.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <format:date obj="${orderInstance?.dateOrdered}"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                            <label><warehouse:message code="order.orderedBy.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        ${orderInstance?.orderedBy?.name }
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td valign="top" class="name">
                                        <label><warehouse:message code="order.totalPrice.label"/></label>
                                    </td>
                                    <td valign="top" class="value">
                                        <g:formatNumber number="${orderInstance?.totalPrice()?:0 }"/>
                                        ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                    </td>
                                </tr>

                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="yui-u">
                        <div class="tabs tabs-ui">
                            <ul>
                                <li><a href="#tabs-items"><warehouse:message code="order.orderItems.label"/></a></li>
                                <g:if test="${orderInstance.orderTypeCode == OrderTypeCode.PURCHASE_ORDER}">
                                    <li><a href="#tabs-shipments"><warehouse:message code="shipments.label"/></a></li>
                                </g:if>
                                <li><a href="#tabs-documents"><warehouse:message code="documents.label"/></a></li>
                                <li><a href="#tabs-comments"><warehouse:message code="comments.label" default="Comments"/></a></li>

                            </ul>
                            <div id="tabs-items" class="ui-tabs-hide">
                                <div class="box">
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
                                                <th><warehouse:message code="order.qtyOrdered.label" /></th>
                                                <g:if test="${orderInstance.orderTypeCode==OrderTypeCode.PURCHASE_ORDER}">
                                                    <th><warehouse:message code="order.qtyFulfilled.label" /></th>
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
                                                    <td class="order-item-quantity">
                                                        ${orderItem?.quantity}
                                                        ${orderItem?.product?.unitOfMeasure?:"EA"}
                                                    </td>
                                                    <g:if test="${orderInstance.orderTypeCode==OrderTypeCode.PURCHASE_ORDER}">
                                                        <td class="order-item-fullfilled">
                                                            ${orderItem?.quantityFulfilled?:0}
                                                            ${orderItem?.product?.unitOfMeasure?:"EA"}
                                                        </td>
                                                        <td class="">
                                                            <g:formatNumber number="${orderItem?.unitPrice?:0}" />
                                                            ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
                                                        </td>
                                                        <td class="">
                                                            <g:formatNumber number="${orderItem?.totalPrice()?:0}" />
                                                            ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
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
                                                    <th colspan="5" class="left">
                                                        <warehouse:message code="default.total.label"/>
                                                    </th>
                                                    <th colspan="1" class="left">
                                                        <g:formatNumber number="${orderInstance?.totalPrice()?:0.0 }"/>
                                                        ${grailsApplication.config.openboxes.locale.defaultCurrencyCode}
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
                            <g:if test="${orderInstance.orderTypeCode == OrderTypeCode.PURCHASE_ORDER}">

                                <div id="tabs-shipments" class="ui-tabs-hide">

                                    <div class="box">
                                        <h2><warehouse:message code="shipments.label"/></h2>

                                        <g:if test="${orderInstance?.listShipments() }">
                                            <table>
                                                <thead>
                                                <tr class="odd">
                                                    <th><warehouse:message code="default.type.label"/></th>
                                                    <th><warehouse:message code="default.name.label"/></th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <g:each var="shipmentInstance" in="${orderInstance?.listShipments()}" status="i">
                                                    <tr>
                                                        <td>
                                                            <format:metadata obj="${shipmentInstance?.shipmentType}"/>
                                                        </td>
                                                        <td>
                                                            <g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id }">${shipmentInstance?.name }</g:link>
                                                        </td>
                                                    </tr>
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
