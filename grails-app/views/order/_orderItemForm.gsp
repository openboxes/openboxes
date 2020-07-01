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
                       styleClass="text large required"/>
    </td>
    <td class="middle center">
        <g:selectProductSupplier id="productSupplier"
                               name="productSupplier.id" class="select2withTag"
                               noSelection="['':'']" />
    </td>
    <td class="middle center">
        <input type="text" id="supplierCode" name="supplierCode" class="text" placeholder="Supplier code" style="width: 100px" disabled />
    </td>
    <td class="middle center">
        <g:selectOrganization name="manufacturer"
                              id="manufacturer"
                              roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                              noSelection="['':'']"
                              class="select2" disabled="${true}" />
    </td>
    <td class="middle center">
        <input type="text" id="manufacturerCode" name="manufacturerCode" class="text" placeholder="Manufacturer code" style="width: 100px" disabled />
    </td>
    <td class="middle center">
        <input type="number" id="quantity" name="quantity" class="text" placeholder="Quantity" style="width: 100px"/>
    </td>
    <td class="center middle">
        <g:selectUnitOfMeasure id="quantityUom" data-placeholder="UoM"
                               name="quantityUom.id" class="select2 required" style="width: 100px"
                               noSelection="['':'']"/>
    </td>
    <td class="center middle">
        <input type="number" id="quantityPerUom" name="quantityPerUom" class="text required" placeholder="Qty per UoM" style="width: 100px"/>
    </td>
    <td class="center middle">
        <input type="number" id="unitPrice" required name="unitPrice" size="2" class="text required" placeholder="Price per UoM" style="width: 100px"/>
    </td>
    <td class="center middle">
    </td>
    <td class="center middle">
        <g:hiddenField id="defaultRecipient" name="defaultRecipient" value="${order?.orderedBy?.id}"/>
        <g:selectPerson id="recipient" name="recipient" value="${order?.orderedBy?.id}"
                        noSelection="['':'']" class="select2"/>
    </td>
    <td class="center middle">
        <g:jqueryDatePicker id="estimatedReadyDate" name="estimatedReadyDate" value="" placeholder="Expected ready date"
                            autocomplete="off" noSelection="['':'']"/>
    </td>
    <td class="center middle">
        <button id="save-item-button" class="button save-item">
            <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />&nbsp;
            <warehouse:message code="default.button.save.label"/>
        </button>
    </td>
</tr>
<script>

  function enableEditing() {
    $("#supplierCode").removeAttr("disabled");
    $("#manufacturerCode").removeAttr("disabled");
    $("#manufacturer").removeAttr("disabled");
  }

  function disableEditing() {
    $("#supplierCode").attr("disabled", true);
    $("#manufacturerCode").attr("disabled", true);
    $("#manufacturer").attr("disabled", true);
  }
  
  function clearSource() {
    $("#supplierCode").val("");
    $("#manufacturer").val(null).trigger('change');
    $("#manufacturerCode").val("");
  }

  $('#productSupplier').on('select2:select', function (e) {
    if (e.params.data.isNew) {
      clearSource();
      enableEditing();
      $("#supplierCode").val(e.params.data.id);
    } else {
      clearSource();
      disableEditing();
      $("#supplierCode").val(e.params.data.supplierCode);
      $("#manufacturerCode").val(e.params.data.manufacturerCode);
      $("#manufacturer").val(e.params.data.manufacturer).trigger('change');
    }
  });

  $('#productSupplier').on('select2:unselect', function (e) {
    clearSource();
    disableEditing();
  });

  $('#productSupplier').on('select2:clear', function (e) {
    clearSource();
    disableEditing();
  });

</script>
