<style>
    .productSupplier > div > span {
        width: 120px !important;
    }
</style>

<tr>
    <td class="middle">
    </td>
    <td class="middle">
        <g:autoSuggest id="product" name="product"
                       jsonUrl="${request.contextPath }/json/findProductByName?skipQuantity=true&supplierId=${order?.originParty?.id}"
                       styleClass="text large required" showColor="true"/>
    </td>
    <td class="middle center">
        <g:selectProductSupplier
            id="productSupplier"
            name="productSupplier"
            class="select2"
            noSelection="['':'']"
            disabled="${true}"
            data-placeholder="${g.message(code: 'default.selectAnOption.label', default: 'Select an Option')}"
        />
    </td>
    <td class="middle center" style="width: 100px; word-wrap: break-word;" id="supplierCode">
    </td>
    <td class="middle center" style="width: 100px; word-wrap: break-word;" id="manufacturer">
    </td>
    <td class="middle center" style="width: 100px; word-wrap: break-word;" id="manufacturerCode">
    </td>
    <td class="middle center">
        <input type="number" id="quantity" name="quantity" class="text" placeholder="${g.message(code:'default.quantity.label', default: 'Quantity')}" style="width: 100px"/>
    </td>
    <td class="center middle">
        <g:selectUnitOfMeasure
            id="quantityUom"
            data-placeholder="${g.message(code: 'default.uom.label', default: 'UoM')}"
            name="quantityUom.id"
            class="select2 required"
            style="width: 100px"
            noSelection="['':'']"
        />
    </td>
    <td class="center middle">
        <g:decimalNumberField decimal="0" id="quantityPerUom" name="quantityPerUom" placeholder="${g.message(code: 'default.qtyPerUom.label', default: 'Qty per UoM')}" style="width: 100px" />
    </td>
    <td class="center middle">
        <g:decimalNumberField id="unitPrice" name="unitPrice" required="true" placeholder="${g.message(code: 'default.pricePerUom.label', default: 'Price per UoM')}" style="width: 100px" />
    </td>
    <td class="center middle">
    </td>
    <td class="center middle">
        <g:selectPerson
            id="recipient"
            name="recipient"
            noSelection="['':'']" class="select2"
            data-placeholder="${g.message(code: 'default.selectAnOption.label', default: 'Select an Option')}"
        />
    </td>
    <td class="center middle">
        <g:jqueryDatePicker
            id="estimatedReadyDate"
            name="estimatedReadyDate"
            value=""
            placeholder="${g.message(code: 'order.expectedReadyDate.label', default: 'Expected ready date')}"
            autocomplete="off"
            noSelection="['':'']"
        />
    </td>
    <td class="center middle">
        <g:selectBudgetCode
            name="budgetCode"
            id="budgetCode"
            class="select2"
            active="true"
            noSelection="['':'']"
            data-placeholder="${g.message(code: 'default.selectAnOption.label', default: 'Select an Option')}"
        />
    </td>
    <td class="center middle">
        <button id="save-item-button" class="button save-item">
            <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />&nbsp;
            <warehouse:message code="default.button.save.label"/>
        </button>
    </td>
</tr>
<script>
  function clearSource() {
    $("#supplierCode").val("");
    $("#manufacturer").val(null).trigger('change');
    $("#manufacturerCode").val("");
  }

  $('#productSupplier').on('select2:select', function (e) {
      clearSource();
      $("#supplierCode").val(e.params.data.supplierCode);
      $("#manufacturerCode").val(e.params.data.manufacturerCode);
      $("#manufacturer").val(e.params.data.manufacturer).trigger('change');
  });

  $('#productSupplier').on('select2:unselect', function (e) {
    clearSource();
  });

  $('#productSupplier').on('select2:clear', function (e) {
    clearSource();
  });
</script>
