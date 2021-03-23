<div class="box">
    <h2>
        <warehouse:message code="invoice.invoiceItems.label"/>
    </h2>
    <g:if test="${invoiceInstance?.invoiceItems}">
        <table class="table table-bordered">
            <thead>
            <tr class="odd">
                <th><warehouse:message code="default.code.label"/></th>
                <th><warehouse:message code="default.name.label"/></th>
                <th><warehouse:message code="order.orderItem.label"/></th>
                <th><warehouse:message code="glAccount.label"/></th>
                <th><warehouse:message code="budgetCode.label"/></th>
                <th><warehouse:message code="default.quantity.label"/></th>
                <th><warehouse:message code="invoiceItem.quantityPerUom.label"/></th>
                <th><warehouse:message code="invoiceItem.amount.label"/></th>
                <th><warehouse:message code="invoiceItem.totalAmount.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="invoiceItem" in="${invoiceInstance.invoiceItems}" status="status">
                <tr class="${status%2==0?'odd':'even'}">
                    <td>
                        ${invoiceItem?.product?.productCode}
                    </td>
                    <td>
                        ${invoiceItem?.product?.name}
                    </td>
                    <td>
                        <%-- ${invoiceItem?.orderItem} --%>
                    </td>
                    <td>
                        ${invoiceItem?.glAccount?.name}
                    </td>
                    <td>
                        ${invoiceItem?.createdBy}
                    </td>
                    <td>
                        ${invoiceItem?.quantity}
                    </td>
                    <td>
                        <g:formatNumber number="${invoiceItem?.quantityPerUom}" maxFractionDigits="0" />
                    </td>
                    <td>
                        ${g.formatNumber(number: invoiceItem?.amount?:0, format: '##,##0.00') }
                    </td>
                    <td>
                        ${g.formatNumber(number: invoiceItem?.totalAmount, format: '##,##0.00') }
                    </td>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <th colspan="8">
                </th>
                <th>
                    <g:formatNumber number="${invoiceInstance?.totalValue}"/>
                </th>
            </tr>
            </tfoot>
        </table>
    </g:if>
    <g:else>
        <div class="fade center empty"><warehouse:message code="default.noItems.label" /></div>
    </g:else>
</div>
