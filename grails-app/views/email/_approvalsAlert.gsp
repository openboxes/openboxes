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
        <g:message
                code="email.requestReceived.message"
                args="[requisition.destination,
                       requisition.requestedBy,
                       g.createLink(uri: redirectUrl, absolute: true),
                ]"
        />
    </div>
    &nbsp;
</div>
<div>
    <table>
        <thead style="text-align: left">
        <tr>
            <th>
                <g:message code="product.productCode.label" />
            </th>
            <th>
                <g:message code="product.title.label" />
            </th>
            <th>
                <g:message code="product.uom.label" />
            </th>
            <th>
                <g:message code="requisition.quantity.label" />
            </th>
        </tr>
        </thead>
        <g:if test="${requisition.requisitionItems}">
            <g:each var="item" in="${requisition.requisitionItems}">
                <tr>
                    <td>
                        ${item?.product?.productCode}
                    </td>
                    <td>
                        ${item?.product?.name}
                    </td>
                    <td>
                        ${item?.product?.unitOfMeasure}
                    </td>
                    <td style="text-align: center">
                        ${item?.quantity}
                    </td>
                </tr>
            </g:each>
        </g:if>
    </table>
</div>
