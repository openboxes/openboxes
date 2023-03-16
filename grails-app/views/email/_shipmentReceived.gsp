<%@ page contentType="text/html"%>
<g:applyLayout name="email">


<div class="box">
    <h2>${warehouse.message(code:'default.summary.label', default:'Summary') }</h2>

    <div style="margin: 10px;">
        ${warehouse.message(code: 'email.shipmentReceived.message', args: [format.metadata(obj:shipmentInstance.shipmentType), shipmentInstance?.name])}
        &nbsp;
        <g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id }" absolute="true" class="button">
            ${warehouse.message(code: 'email.link.label', args: [shipmentInstance?.name])}
        </g:link>
    </div>
</div>

<div class="box">
    <h2>${warehouse.message(code:'default.details.label', default:'Details') }</h2>
    <table class="details stripe">
        <tbody>
        <tr class="prop">
            <td class="name">
                <label>${warehouse.message(code: 'shipping.shipmentNumber.label') }</label>
            </td>
            <td class="value">
                ${shipmentInstance?.shipmentNumber }
            </td>
        </tr>
        <tr class="prop">
            <td class="name"><label>${warehouse.message(code: 'shipping.origin.label') }</label></td>
            <td>${shipmentInstance?.origin?.name }</td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label>${warehouse.message(code: 'shipping.destination.label') }</label>
            </td>
            <td class="value">
                ${shipmentInstance?.destination?.name }
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label>${warehouse.message(code: 'shipping.expectedShippingDate.label') }</label>
            </td>
            <td class="value">
                <g:if test="${shipmentInstance?.expectedShippingDate}">
                    <g:formatDate date="${shipmentInstance?.expectedShippingDate }" format="d MMM yyyy"/>
                </g:if>
                <g:else>
                    <span class="fade"><warehouse:message code="default.notAvailable.label"/></span>
                </g:else>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label>${warehouse.message(code: 'shipping.actualShippingDate.label') }</label>
            </td>
            <td class="value">
                <g:if test="${shipmentInstance?.actualShippingDate}">
                    <g:formatDate date="${shipmentInstance?.actualShippingDate }" format="d MMM yyyy"/>
                </g:if>
                <g:else>
                    <span class="fade"><warehouse:message code="default.notAvailable.label"/></span>
                </g:else>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label>${warehouse.message(code: 'shipping.expectedDeliveryDate.label') }</label>
            </td>
            <td class="value">
                <g:if test="${shipmentInstance?.expectedDeliveryDate}">
                    <g:formatDate date="${shipmentInstance?.expectedDeliveryDate }" format="d MMM yyyy"/>
                </g:if>
                <g:else>
                    <span class="fade"><warehouse:message code="default.notAvailable.label"/></span>
                </g:else>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label>${warehouse.message(code: 'shipping.actualDeliveryDate.label') }</label>
            </td>
            <td class="value">
                <g:if test="${shipmentInstance?.actualDeliveryDate}">
                    <g:formatDate date="${shipmentInstance?.actualDeliveryDate }" format="d MMM yyyy"/>
                </g:if>
                <g:else>
                    <span class="fade"><warehouse:message code="default.notAvailable.label"/></span>
                </g:else>
            </td>
        </tr>

        <g:if test="${shipmentInstance?.referenceNumbers }">
            <g:each var="referenceNumber" in="${shipmentInstance?.referenceNumbers}" status="i">
                <tr class="prop">
                    <td valign="middle" class="name">
                        <label>
                            <format:metadata obj="${referenceNumber?.referenceNumberType}"/>
                        </label>
                    </td>
                    <td valign="middle" class="value">
                        ${referenceNumber?.identifier }
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:if test="${userInstance}">
            <tr class="prop">
                <td class="name">
                    <label>${warehouse.message(code: 'shipping.preparedBy.label') }</label>
                </td>
                <td class="value">
                    ${userInstance?.name }
                    <a href="mailto:${userInstance?.email }">${userInstance?.email }</a>
                </td>
            </tr>
        </g:if>
        <g:if test="${shipmentInstance?.carrier}">
            <tr class="prop">
                <td class="name">
                    <label>${warehouse.message(code: 'shipping.carriedBy.label') }</label>
                </td>
                <td class="value">
                    ${shipmentInstance?.carrier?.name }
                    <a href="mailto:${shipmentInstance?.carrier?.email }">${shipmentInstance?.carrier?.email }</a>
                </td>
            </tr>
        </g:if>
        <g:if test="${shipmentInstance?.additionalInformation}">
            <tr class="prop">
                <td class="name">
                    <label>${warehouse.message(code: 'shipping.additionalInformation.label') }</label>
                </td>
                <td class="value">
                    ${shipmentInstance?.additionalInformation}
                </td>
            </tr>
        </g:if>
        </tbody>
    </table>
</div>

<div class="box">
    <h2>${warehouse.message(code:'shipping.events.label') }</h2>
    <table class='stripe'>
        <thead>
        <tr>
            <th><warehouse:message code="default.date.label"/></th>
            <th><warehouse:message code="default.time.label"/></th>
            <th><warehouse:message code="default.event.label"/></th>
            <th><warehouse:message code="location.label"/></th>
        </tr>
        </thead>
        <tbody>
        <g:set var="i" value="${0 }"/>
        <g:each in="${shipmentInstance.events}" var="event">
            <tr class="${(i++ % 2) == 0 ? 'odd' : 'even'}">
                <td>
                    <g:formatDate date="${event.eventDate}" format="MMM d, yyyy"/>
                </td>
                <td>
                    <g:formatDate date="${event.eventDate}" format="hh:mma"/>
                </td>

                <td>
                    <format:metadata obj="${event?.eventType}"/>
                </td>
                <td>
                    ${event?.eventLocation?.name}
                </td>
            </tr>
        </g:each>
        <tr class="${(i++ % 2) == 0 ? 'odd' : 'even'}">
            <td>
                <g:formatDate date="${shipmentInstance?.dateCreated}" format="MMM d, yyyy"/>
            </td>
            <td>
                <g:formatDate date="${shipmentInstance?.dateCreated}" format="hh:mma"/>
            </td>
            <td>
                <warehouse:message code="default.created.label"/>
            </td>
            <td>
                ${shipmentInstance?.origin?.name}
            </td>
        </tr>
        </tbody>
    </table>
</div>

<div class="box">
    <h2>${warehouse.message(code:'shipment.comments.label', default: 'Comments') }</h2>
    <table class='stripe'>
        <tbody>
            <g:each var="comment" in="${shipmentInstance?.comments}">
                <tr>
                    <td>
                        ${comment?.sender?.name} · <g:formatDate date="${comment?.dateCreated}" format="MMM d hh:mma"/>
                        <blockquote class="fade">${comment.comment}</blockquote>
                    </td>
                </tr>
            </g:each>
            <g:unless test="${shipmentInstance?.comments}">
                <tr>
                    <td class="center empty">
                        ${warehouse.message(code:'comments.none.label', default: "No comments")}
                    </td>
                </tr>
            </g:unless>
        </tbody>

    </table>
</div>

<div class="box">
    <h2>${warehouse.message(code:'shipping.contents.label') }</h2>
    <table class='stripe'>
        <thead>
        <tr>
            <th style="text-align: left;">
                ${warehouse.message(code: 'container.label')}
            </th>
            <th style="text-align: left;">
                ${warehouse.message(code: 'product.productCode.label')}
            </th>
            <th style="text-align: left;">
                ${warehouse.message(code: 'default.item.label')}
            </th>
            <th>
                ${warehouse.message(code: 'product.unitOfMeasure.label')}
            </th>
            <th style="text-align: left;">
                ${warehouse.message(code: 'inventoryItem.lotNumber.label')}
            </th>
            <th style="text-align: left;">
                ${warehouse.message(code: 'inventoryItem.expirationDate.label')}
            </th>
            <th style="text-align: left;">
                ${warehouse.message(code: 'inventoryLevel.binLocation.label')}
            </th>
            <th>
                ${warehouse.message(code: 'shipmentItem.totalQuantityShipped.label', default: 'Shipped')}
            </th>
            <th>
                ${warehouse.message(code: 'shipmentItem.totalQuantityReceived.label', default: 'Received')}
            </th>
            <th>
                ${warehouse.message(code: 'shipping.recipient.label')}
            </th>
            <th>
                ${warehouse.message(code: 'product.coldChain.label')}
            </th>
            <th>
                ${warehouse.message(code: 'receiptItem.comments.label', default: "Comments")}
            </th>
        </tr>
        </thead>
        <g:if test="${shipmentInstance.shipmentItems}">
            <g:each var="shipmentItem" in="${shipmentInstance.shipmentItems.sort() }">
                <tr>
                    <td>
                        ${shipmentItem?.container?.name?:warehouse.message(code:'shipping.unpackedItems.label') }
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.product?.productCode}
                    </td>
                    <td>
                        ${format.product(product: shipmentItem?.inventoryItem?.product) }
                    </td>
                    <td class="center">
                        ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.lotNumber}
                    </td>
                    <td>
                        <g:formatDate date="${shipmentItem?.inventoryItem?.expirationDate}" format="d MMM yyyy"/>
                    </td>
                    <td>
                        ${shipmentItem?.inventoryItem?.product?.getBinLocation(session.warehouse.id)}
                    </td>
                    <td class="center">
                        <g:formatNumber number="${shipmentItem.quantity}" format="###,##0" />
                    </td>
                    <td class="center">
                        <g:formatNumber number="${shipmentItem.quantityReceived()}" format="###,##0" />
                    </td>
                    <td class="center">
                        ${shipmentItem?.recipient?.name?:warehouse.message(code:'default.none.label')}
                    </td>
                    <td class="center">
                        <g:if test="${shipmentItem?.inventoryItem?.product?.coldChain}">
                            ${warehouse.message(code:'default.yes.label')}
                        </g:if>
                        <g:else>
                            ${warehouse.message(code:'default.no.label')}
                        </g:else>
                    </td>
                    <td class="left">
                        <g:if test="${shipmentItem?.receiptItems}">
                            ${shipmentItem?.receiptItems*.comment}
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </g:if>
    </table>
</div>

</g:applyLayout>
