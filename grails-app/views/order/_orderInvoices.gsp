<div class="box">
    <h2><warehouse:message code="invoices.label"/></h2>
    <g:if test="${orderInstance?.invoiceItems?.size() > 0 }">
        <table>
            <thead>
            <tr class="odd">
                <th><warehouse:message code="order.orderItem.label" default="Order Item"/></th>
                <th><warehouse:message code="product.code.label" default="Product Code"/></th>
                <th><warehouse:message code="default.description.label" default="Description"/></th>
                <th><warehouse:message code="invoice.invoiceNumber.label" default="Invoice Number"/></th>
                <th><warehouse:message code="default.invoiceType.label" default="Invoice Type"/></th>
                <th><warehouse:message code="default.invoiceStatus.label" default="Invoice Status"/></th>
                <th><warehouse:message code="default.quantity.label" default="Quantity"/></th>
                <th><warehouse:message code="default.uom.label" default="UoM"/></th>
            </tr>
            </thead>
            <tbody>
                <g:each var="invoiceItem" in="${orderInstance?.invoiceItems?.sort { it?.invoice?.invoiceNumber }}" status="i">
                    <tr class="${i%2?'even':'odd'}">
                        <td>${invoiceItem?.orderItem?.id}</td>
                        <td>${invoiceItem?.product?.productCode}</td>
                        <td>${invoiceItem?.description}</td>
                        <td>
                            <g:link controller="invoice" action="show" id="${invoiceItem?.invoice?.id }">${invoiceItem?.invoice?.invoiceNumber}</g:link>
                        </td>
                        <td>${invoiceItem?.invoice?.invoiceType?.name}</td>
                        <td>${invoiceItem?.invoice?.status}</td>
                        <td>${invoiceItem?.quantity}</td>
                        <td>${invoiceItem?.unitOfMeasure}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="order.noInvoices.label"/></div>
    </g:else>
</div>
