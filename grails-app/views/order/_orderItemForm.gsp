<tr>
    <td class="middle">
    </td>
    <td>
%{--        <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName?skipQuantity=true&supplierId=${order?.originParty?.id}"--}%
%{--                       styleClass="text large"/>--}%

        <g:select id="product" name="product.id" from="[]" class="chzn-select-deselect"
                  data-ajax--url="${request.contextPath }/json/findProductByName?skipQuantity=true&supplierId=${order?.originParty?.id}"
                  data-allow-clear="true"
                  data-ajax--cache="true"/>

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
        <input type="number" id="quantity" name="quantity" value="" size="2" class="text" />
    </td>
    <td class="center middle">
        <g:selectUnitOfMeasure id="quantityUom"
                               name="quantityUom.id" class="chzn-select"
                               noSelection="['':'']"/>
    </td>
    <td>
        <input type="number" id="quantityPerUom" name="quantityPerUom" class="text" value=""  placeholder="Quantity per UoM"/>
    </td>
    <td class="center middle">
        <input type="number" id="unitPrice" required name="unitPrice" size="2" class="text"/>
    </td>
    <td class="center middle">
        <input type="number" id="totalPrice" required name="totalPrice" size="2" class="text"/>
    </td>
    <td class="center middle">
        <g:hiddenField id="defaultRecipient" name="defaultRecipient" value="${order?.orderedBy?.id}"/>
%{--        <g:selectPerson id="recipient" name="recipient" value="${order?.orderedBy?.id}"--}%
%{--                        noSelection="['':'']" class="chzn-select-deselect"/>--}%

        <g:select name="recipient" value="${order?.orderedBy?.id}" from="[]" class="chzn-select-deselect"
                  data-ajax--url="${request.contextPath}/json/findPersonByName"
                  data-allow-clear="true"
                  data-ajax--cache="true"/>

    </td>
    <td class="center middle">
        <g:jqueryDatePicker id="estimatedReadyDate" name="estimatedReadyDate" value=""
                            autocomplete="off" noSelection="['':'']"/>
    </td>
    <td class="center" colspan="2">
        <button id="save-item-button" class="button save-item">
            <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />&nbsp;
            <warehouse:message code="default.button.save.label"/>
        </button>
    </td>
</tr>
