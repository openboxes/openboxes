<%@ page contentType="text/html"%>
<style>
    label {
        font-weight: bold;
    }
</style>
<div>
    <div class="header">
        <g:render template="/email/header"/>
    </div>
    <div>
        <div>
            ${warehouse.message(code: 'email.user.label', args: [recipient?.firstName]) },
        </div>
        <div>
            ${warehouse.message(code: 'email.shipmentItemReceived.label', args: [shipmentInstance?.destination?.name]) }:
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
        <div>
            <a class="name">
                <label>${warehouse.message(code: 'requisition.receivedBy.label') }:</label>
            </a>
            <a class="value">
                ${receivedBy?.name}
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
                <th>
                    ${warehouse.message(code: 'shipping.canceled.label')}
                </th>
                <th>
                    ${warehouse.message(code: 'comment.label')}
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
                        <td style="text-align: center;">
                            <g:formatNumber number="${receiptItem.quantityReceiving ?: 0}" format="###,##0" />
                        </td>
                        <td style="text-align: center;">
                            <g:formatNumber number="${receiptItem.quantityCanceled}" format="###,##0" />
                        </td>
                        <td>
                            ${receiptItem?.comment}
                        </td>
                    </tr>
                </g:each>
            </g:if>
        </table>
    </div>
    &nbsp;
    <div>
        ${warehouse.message(code: 'email.contactAdminReceiving.message')}
        <a href="${createLink(controller: 'stockMovement', action: 'show', id: shipmentInstance?.id, absolute: true)}">
            ${warehouse.message(code: 'email.thisLink.label')}.
        </a>
    </div>
</div>
