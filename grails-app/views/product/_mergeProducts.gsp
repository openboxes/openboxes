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
                       styleClass="text"
                       showColor="true"
                        valueId="${primaryProduct.id}"
                        valueName="${primaryProduct.productCode} ${primaryProduct.name}"
                    />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="obsolete-product">
                        <warehouse:message code="product.mergeProducts.obsoleteProduct.label"/>
                    </label>
                </td>
                <td valign="top" class="value">
                    <g:autoSuggest
                        id="obsolete-product"
                        name="obsolete-product"
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
       const obsoleteProductId = $("#obsolete-product-id").val();

       if (primaryProductId && obsoleteProductId) {
         const warningMessage = $("#merge-product-warning-message")
           .text()
           .replaceAll("{0}", primaryProductId)
           .replaceAll("{1}", obsoleteProductId);
         $("#merge-product-warning-message").text(warningMessage)

         $('#merge-product-confirmation-dialog')
           .data('primaryProduct', primaryProductId)
           .data('obsoleteProduct', obsoleteProductId)
           .dialog('open')
       }
       if (!primaryProductId) $("#primary-product-suggest").notify("primary product must be selected")
       if (!obsoleteProductId) $("#obsolete-product-suggest").notify("obsolete product must be selected")
     })
  });
</script>
