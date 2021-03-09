<g:form name="productSourceForm" action="order" method="post">
    <table>
        <tbody>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="ps-dlgProduct"><warehouse:message code="product.label"/></label>
                </td>
                <td valign="top" class="value">
                    <format:metadata obj="${product?.productCode}"/>
                    <format:product product="${product}"/>
                    <g:hiddenField id="ps-dlgProduct" name="product.id" value="${product?.id}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="ps-dlgSourceCode"><warehouse:message code="productSupplier.sourceCode.label"/></label>
                </td>
                <td valign="top" class="value">
                    <input type="text" id="ps-dlgSourceCode" name="sourceCode" class="text" size="24" placeholder="Source Code" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="ps-dlgSourceName"><warehouse:message code="productSupplier.sourceName.label"/></label>
                </td>
                <td>
                    <input type="text" id="ps-dlgSourceName" name="sourceName" class="text" size="24" placeholder="Source Name" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="ps-dlgSupplier"><warehouse:message code="productSupplier.supplier.label"/></label>
                </td>
                <td valign="top" class="value">
                    <format:metadata obj="${supplier?.name}"/>
                    <g:hiddenField id="ps-dlgSupplier" name="supplier.id" value="${supplier?.id}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="ps-dlgSupplierCode"><warehouse:message code="product.supplierCode.label"/></label>
                </td>
                <td>
                    <input type="text" id="ps-dlgSupplierCode" name="supplierCode" class="text" size="24" placeholder="Supplier Code" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="ps-dlgManufacturer"><warehouse:message code="product.manufacturer.label"/></label>
                </td>
                <td>
                    <g:selectOrganization name="manufacturer"
                                          id="ps-dlgManufacturer"
                                          value=""
                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                                          noSelection="['':'']"
                                          class="select2"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="ps-dlgManufacturerCode"><warehouse:message code="product.manufacturerCode.label"/></label>
                </td>
                <td valign="top" class="value">
                    <input type="text"
                           id="ps-dlgManufacturerCode"
                           name="manufacturerCode"
                           value=""
                           size="24"
                           class="text" placeholder="Manufacturer Code" />
                </td>
            </tr>
        </tbody>
        <tfoot>
        <tr class="prop">
            <td></td>
            <td valign="top" class="value">
                <button class="button save-product-source-button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'tick.png')}" />&nbsp;
                    <warehouse:message code="default.button.save.label"/>
                </button>
                <div class="right">
                    <button class="button close-dialog-button">
                        <img src="${resource(dir: 'images/icons/silk', file: 'bullet_cross.png')}" />&nbsp;
                        <warehouse:message code="default.button.close.label"/>
                    </button>
                </div>
            </td>
        </tr>
        </tfoot>
    </table>
</g:form>
<script>
    function saveProductSourceDialog() {
        var data = $("#productSourceForm").serialize();
        $.ajax({
            url: "${g.createLink(controller:'order', action:'createProductSource')}",
            data: data,
            success: function (response) {
                $.notify("Product source created successfully", "success");
                $("#create-product-source-dialog").dialog("close");

                // Reload product sources in orderItemForm to include newly created
                var productId = $("#product-id").val();
                var supplierId = $("#supplierId").val();
                var destinationPartyId = $("#destinationPartyId").val();
                clearSource();
                productChanged(productId, supplierId, destinationPartyId, response);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.responseText) {
                    $.notify(jqXHR.responseText, "error");
                } else {
                    $.notify("An error occurred", "error");
                }
            }
        });
    }

    $(document).ready(function() {
        $(".close-dialog-button").click(function(event){
            event.preventDefault();
            $("#create-product-source-dialog").dialog("close");
        });

        $(".save-product-source-button").click(function(event){
            event.preventDefault();
            saveProductSourceDialog();
        });
    });
</script>
