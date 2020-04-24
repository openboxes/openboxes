<tr>
    <td class="middle">
    </td>
    <td class="middle">
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
        <input type="number" id="quantity" name="quantity" class="text"/>
    </td>
    <td class="center middle">
        <g:selectUnitOfMeasure id="quantityUom"
                               name="quantityUom.id" class="chzn-select-deselect"
                               noSelection="['':'']"/>
        <input type="number" id="quantityPerUom" name="quantityPerUom" class="text" placeholder="Quantity per UoM"/>
    </td>
    <td class="center middle">
        <input type="number" id="unitPrice" required name="unitPrice" size="2" class="text" placeholder="Price per UoM"/>
    </td>
    <td class="center middle">
    </td>
    <td class="center middle">
        <g:hiddenField id="defaultRecipient" name="defaultRecipient" value="${order?.orderedBy?.id}"/>
        <g:selectPerson id="recipient" name="recipient" value="${order?.orderedBy?.id}"
                        noSelection="['':'']" class="chzn-select-deselect"/>
    </td>
    <td class="center middle">
        <g:jqueryDatePicker id="estimatedReadyDate" name="estimatedReadyDate" value=""
                            autocomplete="off" noSelection="['':'']"/>
    </td>
    <td class="center middle" colspan="2">
        <button id="save-item-button" class="button save-item">
            <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />&nbsp;
            <warehouse:message code="default.button.save.label"/>
        </button>
    </td>
</tr>
