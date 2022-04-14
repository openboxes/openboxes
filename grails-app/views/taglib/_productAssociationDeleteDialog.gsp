<div id="product-association-delete-dialog" title="${warehouse.message(code: 'productAssociation.delete.label')}">
    <g:hiddenField id="productAssociationId" name="productAssociationId" value="${productAssociationId}"></g:hiddenField>
    <div style="margin:12px 20px;">${warehouse.message(code: 'productAssociation.delete.message')}</div>
</div>
<script type="text/javascript">
    function deleteProductAssociation(mutualDelete) {
        const productAssociationId = $("#productAssociationId").val();

        if(productAssociationId) {
            $.ajax({
                url: "${g.createLink(controller:'productAssociation', action:'delete')}",
                type: "POST",
                data: {
                    id: productAssociationId,
                    mutualDelete: mutualDelete,
                },
                success: function () {
                    $.notify("Product association deleted successfully", "success");
                    $("#product-association-delete-dialog").dialog("close");
                    window.location.href = '${request.contextPath}/productAssociation/list';
                },
                error: function (jqXHR, textStatus, errorThrown) {
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
        } else {
            $.notify("Missing product association id");
        }
      return false;
    }

    $(function() {
        $("#product-association-delete-dialog").dialog({
            autoOpen: false,
            resizable: false,
            height: "auto",
            width: 400,
            modal: true,
            buttons: {
                Yes: function() {
                    deleteProductAssociation(true);
                },
                No: function() {
                    deleteProductAssociation(false);
                },
                Cancel: function() {
                    $(this).dialog("close");
                }
            }
        });
    });
</script>
