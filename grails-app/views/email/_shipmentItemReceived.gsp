<%@ page contentType="text/html"%>
<g:applyLayout name="email">
<div>
    <div>
        <div>
            ${warehouse.message(code: 'email.user.label') },
        </div>
        <div>
            ${warehouse.message(code: 'email.shipmentItemReceived.label') }:
        </div>
        &nbsp;
    </div>
    <div>
        <div>
            <a class="name">
                <label>${warehouse.message(code: 'shipping.shipmentNumber.label') }:</label>
            </a>
            <a class="value">
                ${shipmentInstance?.shipmentNumber }
            </a>
        </div>
        <div>
            <a class="name">
                <label>${warehouse.message(code: 'shipping.origin.label') }:</label>
            </a>
            <a class="value">
                ${shipmentInstance?.origin?.name }
            </a>
        </div>
        <div>
            <a class="name">
                <label>${warehouse.message(code: 'shipping.destination.label') }:</label>
            </a>
            <a class="value">
                ${shipmentInstance?.destination?.name }
            </a>
        </div>
        &nbsp;
    </div>
    <div>
         <table style="width: 50%;">
            <thead>
            <tr>
                <th style="text-align: left;">
                    ${warehouse.message(code: 'product.productCode.label')}
                </th>
                <th style="text-align: left;">
                    ${warehouse.message(code: 'default.name.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'shipping.received.label')}
                </th>
            </tr>
            </thead>
            <g:if test="${receiptItems}">
                <g:each var="receiptItem" in="${receiptItems}">
                    <tr>
                        <td>
                            ${receiptItem?.inventoryItem?.product?.productCode}
                        </td>
                        <td>
                            ${format.product(product: receiptItem?.inventoryItem?.product) }
                        </td>
                        <td class="center">
                            <g:formatNumber number="${receiptItem.quantityReceived}" format="###,##0" />
                        </td>
                    </tr>
                </g:each>
            </g:if>
        </table>
    </div>
    &nbsp;
    <div>
        ${warehouse.message(code: 'email.contactAdmin.message')}
        <a href="${createLink(controller: 'stockMovement', action: 'show', id: shipmentInstance?.id, absolute: true)}">
            ${warehouse.message(code: 'email.thisLink.label')}.
        </a>
    </div>
</div>
</g:applyLayout>
