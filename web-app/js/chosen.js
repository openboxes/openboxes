$(document).ready(function() {
    // Chozen select default
    $(".chzn-select").chosen({ width: '100%', search_contains: true });
    $(".chzn-select-deselect").chosen({ allow_single_deselect:true, width: '100%', search_contains: true });
    $(".chzn-select-deselect").livequery(function(){
        $(this).chosen({allow_single_deselect:true, width:'100%', search_contains: true});
    });
});
