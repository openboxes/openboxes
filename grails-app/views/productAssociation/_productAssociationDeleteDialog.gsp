<div id="product-association-delete-dialog" title="${warehouse.message(code: 'productAssociation.delete.label')}">
    <div style="margin:12px 20px;">${warehouse.message(code: 'productAssociation.delete.message')}</div>
</div>
<script type="text/javascript">
    function deleteProductAssociation(productAssociationId, mutualDelete, reload) {
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

                    if (reload) {
                      location.reload();
                      return;
                    }

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
            $("#product-association-delete-dialog").dialog("close");
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
                    deleteProductAssociation($(this).data('productAssociationId'), true, $(this).data('reload'));
                },
                No: function() {
                    deleteProductAssociation($(this).data('productAssociationId'), false, $(this).data('reload'));
                },
                Cancel: function() {
                    $(this).dialog("close");
                }
            }
        });
    });
</script>
