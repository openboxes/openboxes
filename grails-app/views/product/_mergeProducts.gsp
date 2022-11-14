<div class="dialog">
    <p class="message m-2">
        <warehouse:message code="product.mergeProducts.description.label"/>
    </p>
    <table>
        <tbody>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="primary-product">
                        <warehouse:message code="product.mergeProducts.primaryProduct.label"/>
                    </label>
                </td>
                <td valign="top" class="value">
                    <g:autoSuggest
                       id="primary-product"
                       name="primary-product"
                       jsonUrl="${request.contextPath}/json/findProductByName?skipQuantity=true"
                       styleClass="tex"
                       showColor="true"
                    />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="duplicate-product">
                        <warehouse:message code="product.mergeProducts.duplicateProduct.label"/>
                    </label>
                </td>
                <td valign="top" class="value">
                    <g:autoSuggest
                        id="duplicate-product"
                        name="duplicate-product"
                        jsonUrl="${request.contextPath}/json/findProductByName?skipQuantity=true"
                        styleClass="text"
                        showColor="true"
                    />
                </td>
            </tr>
        </tbody>
        <tfoot>
            <tr class="prop">
                <td></td>
                <td valign="top" class="value">
                    <button
                        id="merge-button"
                        type="button"
                        class="button icon approve"
                    >
                        <g:message code="product.mergeProducts.label" default="Merge Products"/>
                    </button>
                </td>
            </tr>
        </tfoot>
    </table>
    <g:render template="/product/mergeProductConfirmationDialog" />
</div>
<script type="text/javascript">
  $(function() {
   $("#merge-button")
     .click(function () {
       const primaryProductId = $("#primary-product-id").val();
       const duplicateProductId = $("#duplicate-product-id").val();

       if (primaryProductId && duplicateProductId) {
         $('#merge-product-confirmation-dialog')
           .data('primaryProduct', primaryProductId)
           .data('duplicateProduct', duplicateProductId)
           .dialog('open')
       }
       if (!primaryProductId) $("#primary-product-suggest").notify("primary product must be selected")
       if (!duplicateProductId) $("#duplicate-product-suggest").notify("duplicate product must be selected")
     })
  });
</script>
