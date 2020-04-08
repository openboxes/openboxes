<g:form name="orderItemForm" action="purchaseOrder" method="post">
    <g:hiddenField id="orderId" name="order.id" value="${order?.id }"></g:hiddenField>
    <g:hiddenField id="orderItemId" name="orderItem.id" value="${orderItem?.id }"></g:hiddenField>
    <g:hiddenField id="supplierId" name="supplier.id" value="${order?.originParty?.id }"></g:hiddenField>
    <tr>
        <td class="middle">
        </td>
        <td>
            <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName?skipQuantity=true&supplierId=${order?.originParty?.id}"
                           styleClass="text large"/>
        </td>
        <td class="middle center">
            <select id="productSupplier" name="productSupplier.id"></select>
        </td>
        <td class="middle center">
            <div id="supplierCode"></div>
        </td>
        <td class="middle center">
            <div id="manufacturer"></div>
        </td>
        <td class="middle center">
            <div id="manufacturerCode"></div>
        </td>
        <td class="middle center">
            <input type="text" id="quantity" name='quantity' value="" size="10" class="text" />
        </td>
        <td class="center middle">
            <g:hiddenField name="unitOfMeasure" value="each"/>
            each
        </td>
        <td class="center middle">
            <input type="text" id="unitPrice" name='unitPrice' size="5" class="text" />
        </td>
        <td></td>
        <td>
            <g:selectPerson id="recipient" name="recipient" value="${order?.orderedBy?.id}"
                            noSelection="['':'']" class="chzn-select-deselect"/>
        </td>
        <td>
            <g:jqueryDatePicker id="estimatedReadyDate" name="estimatedReadyDate" value=""
                                autocomplete="off" noSelection="['':'']"/>
        </td>
        <td></td>
        <td class="center" colspan="2">
            <button class="button save-item">
                <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />&nbsp;
                <warehouse:message code="default.button.save.label"/>
            </button>
        </td>
    </tr>
</g:form>
