<div id="merge-product-confirmation-dialog" class="p-2" title="${warehouse.message(code: 'product.mergeProducts.label')}">
    <div id="merge-product-warning-message" class="message">
      ${warehouse.message(code: 'product.mergeProducts.confirmation.warning.label')}
    </div>
  <div class="my-2">
    <input
        id="product-merger-confirmation-input"
        class="w-100 text mb-2"
        type="text"
        placeholder="MERGE"
    />
    <i class="fade">
      ${warehouse.message(code: 'product.mergeProducts.proceed.label')}
    </i>
  </div>

</div>
<style type="text/css">
.ui-dialog-buttonset {
  width: 100%;
  display: flex;
  justify-content: space-between;
}
</style>
<script type="text/javascript">
  function mergeProductValidate (primaryProduct, obsoleteProduct) {
    const keyword = "MERGE";
    const errors = [];
    const inputVal = $("#product-merger-confirmation-input").val();
    if (inputVal !== keyword) {
      errors.push("Wrong confirmation keyword - \"" + inputVal + "\" does not equal to \"" + keyword + "\"");
    }
    if (errors.length === 0) {
      mergeProduct(primaryProduct, obsoleteProduct);
    } else {
      errors.forEach(err => {
        $("#product-merger-confirmation-input").notify(err, "error");
      })
    }
  }

  function mergeProduct(primaryProduct, obsoleteProduct) {
      $(".loading").show();
      $.ajax({
        url: "${g.createLink(controller:'product', action:'merge')}",
        type: "POST",
        data: {
          primaryProduct,
          obsoleteProduct
        },
        success: function () {
          $.notify("Products merged successfully", "success");
          $(".loading").hide();
          closeModal();
          window.location.href = '${request.contextPath}/inventoryItem/showStockCard/' + primaryProduct;
        },
        error: function (jqXHR) {
          $(".loading").hide();
          if (jqXHR.responseText) {
            try {
              let data = JSON.parse(jqXHR.responseText);
              $.notify(data.errorMessage, "error");
            } catch (e) {
              $.notify(jqXHR.responseText, "error");
            }
          } else {
            $.notify("An error occurred", "error");
          }
        }
      });
  }

  function closeModal() {
    $("#merge-product-confirmation-dialog").dialog("close");
  }

  $(function() {
    $("#merge-product-confirmation-dialog").dialog({
      autoOpen: false,
      resizable: false,
      height: "auto",
      width: 400,
      modal: true,
      buttons: {
        cancel: {
          text: "Cancel",
          click: closeModal
        },
        merge: {
          id: "submit-merge-button",
          text: "Merge",
          click: function () {
            mergeProductValidate($(this).data('primaryProduct'), $(this).data('obsoleteProduct'));
          }
        },
      }
    });
  });
</script>
