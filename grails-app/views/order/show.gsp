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


                <g:hasErrors bean="${orderInstance}">
                    <div class="errors">
                        <g:renderErrors bean="${orderInstance}" as="list" />
                    </div>
                </g:hasErrors>

                <g:render template="summary" model="[orderInstance:orderInstance,currentState:'showOrder']"/>


                <div class="tabs tabs-ui">
                    <ul>
                        <li><a href="#tabs-items"><warehouse:message code="order.orderItems.label"/></a></li>
                        <li><a href="#tabs-shipments"><warehouse:message code="shipments.label"/></a></li>
                        <li><a href="#tabs-documents"><warehouse:message code="documents.label"/></a></li>
                        <li><a href="#tabs-comments"><warehouse:message code="comments.label" default="Comments"/></a></li>

                    </ul>
                    <div id="tabs-items" style="padding: 10px;" class="ui-tabs-hide">
                        <g:if test="${orderInstance?.orderItems }">
                            <table class="table table-bordered">
                                <thead>
                                <tr class="odd">
                                    <th><warehouse:message code="product.productCode.label" /></th>
                                    <th><warehouse:message code="product.label" /></th>
                                    <th><warehouse:message code="order.qtyOrdered.label" /></th>
                                    <th><warehouse:message code="order.qtyFulfilled.label" /></th>
                                    <th><warehouse:message code="order.unitPrice.label" /></th>
                                    <th><warehouse:message code="order.totalPrice.label" /></th>
                                </tr>
                                </thead>
                                <tbody>
                                <g:each var="orderItem" in="${orderInstance?.listOrderItems()}" status="i">
                                    <tr class="order-item ${(i % 2) == 0 ? 'even' : 'odd'}">
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
                                        <td class="order-item-fullfilled">
                                            ${orderItem?.quantityFulfilled()}
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
                                    </tr>
                                </g:each>
                                </tbody>
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

                            </table>
                        </g:if>
                        <g:else>
                            <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
                    </g:else>

                    </div>
                    <div id="tabs-shipments" style="padding: 10px;" class="ui-tabs-hide">
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
                    <div id="tabs-documents" style="padding: 10px;" class="ui-tabs-hide">
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
                                                <img src="${resource(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
                                            </g:link>

                                            <g:link action="deleteDocument" id="${documentInstance.id}" params="['order.id':orderInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                <img src="${resource(dir:'images/icons',file:'trash.png')}" alt="Delete" />
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
                    <div id="tabs-comments" style="padding: 10px;" class="ui-tabs-hide">
                        <g:if test="${orderInstance?.comments }">
                            <table>
                                <thead>
                                <tr class="odd">
                                    <th><warehouse:message code="default.comment.label" /></th>
                                    <th><warehouse:message code="default.author.label" default="Author"/></th>
                                    <th><warehouse:message code="default.date.label" /></th>
                                    <th><warehouse:message code="default.actions.label" /></th>
                                </tr>
                                </thead>
                                <tbody>
                                <g:each var="commentInstance" in="${orderInstance?.comments}">
                                    <tr>
                                        <td>
                                            ${commentInstance?.comment}
                                        </td>
                                        <td>
                                            ${commentInstance?.sender?.name}
                                        </td>
                                        <td>
                                            <g:formatDate date="${commentInstance?.lastUpdated}"/>
                                        </td>
                                        <td align="right">
                                            <g:link action="editComment" id="${commentInstance.id}" params="['order.id':orderInstance?.id]">
                                                <img src="${resource(dir:'images/icons/silk',file:'page_edit.png')}" alt="Edit" />
                                            </g:link>

                                            <g:link action="deleteComment" id="${commentInstance.id}" params="['order.id':orderInstance?.id]" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                <img src="${resource(dir:'images/icons',file:'trash.png')}" alt="Delete" />
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
