
<%@ page import="org.pih.warehouse.order.Order" %>
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

                <div class="box">
                    <h2><warehouse:message code="order.wizard.showOrder.label"/></h2>

                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for='order-description'><warehouse:message code="default.description.label" /></label>
                                </td>

                                <td valign="top" class="value" id="order-description">${fieldValue(bean: orderInstance, field: "description")}</td>

                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='order-origin'><warehouse:message code="order.orderedFrom.label"/></label></td>
                                <td valign='top' class='value' id="order-origin">
                                    ${orderInstance?.origin?.name?.encodeAsHTML()}
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for="order-destination"><warehouse:message code="order.orderedFor.label"/></label></td>
                                <td valign='top' class='value' id="order-destination">
                                    ${orderInstance?.destination?.name?.encodeAsHTML()}
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='orderedBy'><warehouse:message code="order.orderedBy.label"/></label></td>
                                <td valign='top'class='value' id="orderedBy">
                                    ${orderInstance?.orderedBy?.name }
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='totalPrice'><warehouse:message code="order.totalPrice.label"/></label></td>
                                <td valign='top'class='value' id="totalPrice">
                                    <g:formatNumber number="${orderInstance?.totalPrice()?:0 }" type="currency" currencyCode="USD"/>

                                </td>
                            </tr>


                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="order.shipments.label" /></label></td>
                                <td valign="top" class="value">

                                    <g:if test="${orderInstance?.shipments() }">
                                        <table>
                                            <thead>
                                                <tr class="odd">
                                                    <th><warehouse:message code="default.type.label"/></th>
                                                    <th><warehouse:message code="default.name.label"/></th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <g:each var="shipmentInstance" in="${orderInstance?.shipments()}" status="i">
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
                                        <span class="fade"><warehouse:message code="order.noShipments.label"/></span>
                                    </g:else>

                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name"><label for="comments"><warehouse:message code="default.comments.label" /></label></td>
                                <td valign="top" class="value" id="comments">
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
                                        <span class="fade"><warehouse:message code="default.noComments.label" /></span>
                                    </g:else>

                                </td>

                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comments"><warehouse:message code="documents.label" /></label>
                                </td>
                                <td valign="top" class="value">
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
                                        <span class="fade"><warehouse:message code="default.noDocuments.label" /></span>
                                    </g:else>

                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message code="default.items.label" /></label></td>
                                <td valign="top" class="value">
                                    <g:if test="${orderInstance?.orderItems }">
                                        <table>
                                            <thead>
                                                <tr class="odd">
                                                    <th><warehouse:message code="default.type.label" /></th>
                                                    <th><warehouse:message code="product.productCode.label" /></th>
                                                    <th><warehouse:message code="product.label" /></th>
                                                    <th><warehouse:message code="product.unitOfMeasure.label" /></th>
                                                    <th><warehouse:message code="order.qtyOrdered.label" /></th>
                                                    <th><warehouse:message code="order.qtyFulfilled.label" /></th>
                                                    <th><warehouse:message code="order.unitPrice.label" /></th>
                                                    <th><warehouse:message code="order.totalPrice.label" /></th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <g:each var="orderItem" in="${orderInstance?.orderItems}" status="i">
                                                    <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'}">
                                                        <td>
                                                            <g:if test="${orderItem?.product }">
                                                                <warehouse:message code="product.label" />
                                                            </g:if>
                                                            <g:elseif test="${orderItem?.category }">
                                                                <warehouse:message code="category.label" />
                                                            </g:elseif>
                                                            <g:else>
                                                                <warehouse:message code="default.unclassified.label" />
                                                            </g:else>
                                                        </td>
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
                                                        <td>
                                                            ${orderItem?.product?.unitOfMeasure?:""}
                                                        </td>
                                                        <td class="order-item-quantity">
                                                            ${orderItem?.quantity}
                                                        </td>
                                                        <td class="order-item-fullfilled">
                                                            ${orderItem?.quantityFulfilled()}
                                                        </td>
                                                        <td class="">
                                                            <g:formatNumber number="${orderItem?.unitPrice}" type="currency" currencyCode="USD"/>
                                                        </td>
                                                        <td class="">
                                                            <g:formatNumber number="${orderItem?.totalPrice()?:0}" type="currency" currencyCode="USD"/>

                                                        </td>
                                                    </tr>
                                                </g:each>
                                            </tbody>
                                        </table>
                                    </g:if>
                                    <g:else>
                                        <span class="fade"><warehouse:message code="default.noItems.label" /></span>
                                    </g:else>
                                </td>
                            </tr>

                        </tbody>
                    </table>
                    <div class="buttons center" style="border-top: 1px solid lightgrey;">

                        <g:link controller="purchaseOrderWorkflow" action="purchaseOrder" id="${orderInstance?.id}" class="button">
                            ${warehouse.message(code: 'default.button.next.label')}
                        </g:link>


                    </div>
                </div>

            </div>
        </div>
    </body>
</html>
