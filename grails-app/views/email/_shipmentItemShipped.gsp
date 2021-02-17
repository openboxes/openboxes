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
            ${warehouse.message(code: 'email.shipmentItemShipped.label') }:
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
                    ${warehouse.message(code: 'shipping.shipped.label')}
                </th>
            </tr>
            </thead>
            <g:if test="${shipmentItems}">
                <g:each var="shipmentItem" in="${shipmentItems}">
                    <tr>
                        <td>
                            ${shipmentItem?.inventoryItem?.product?.productCode}
                        </td>
                        <td>
                            ${format.product(product: shipmentItem?.inventoryItem?.product) }
                        </td>
                        <td class="center">
                            <g:formatNumber number="${shipmentItem.quantity}" format="###,##0" />
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
