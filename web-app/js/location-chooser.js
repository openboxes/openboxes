$(document).ready(function() {
    // Location chooser
    $(".warehouse-switch").click(function() {
        $("#warehouse-menu").toggle();
        $("#warehouseMenu").dialog({
            autoOpen: true,
            modal: true,
            width: 800
        });
    });
});
