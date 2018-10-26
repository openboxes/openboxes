function showActions() {
    //$(this).children(".actions").show();
}

function hideActions() {
    $(this).children(".actions").hide();
}

$(document).ready(function() {
    /* This is used to remove the action menu when the cursor is no longer over the menu */
    $(".action-menu").hoverIntent({
        sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
        interval: 5,   // number = milliseconds for onMouseOver polling interval
        over: showActions,     // function = onMouseOver callback (required)
        timeout: 100,   // number = milliseconds delay before onMouseOut
        out: hideActions       // function = onMouseOut callback (required)
    });

    // Added to fix bug with the now dynamically load Current Stock tab on the stock card page
    $(".action-menu").livequery(function() {
        $(this).hoverIntent({
            sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
            interval: 5,   // number = milliseconds for onMouseOver polling interval
            over: showActions,     // function = onMouseOver callback (required)
            timeout: 100,   // number = milliseconds delay before onMouseOut
            out: hideActions       // function = onMouseOut callback (required)
        });
    });


    // Create an action button that toggles the action menu on click
    //button({ text: false, icons: {primary:'ui-icon-gear',secondary:'ui-icon-triangle-1-s'} }).
    /*
     $(".action-btn").click(function(event) {
     $(this).parent().children(".actions").toggle();
     event.preventDefault();
     });
     */
    /*
     $(".action-btn").button({ text: false, icons: {primary:'ui-icon-gear',secondary:'ui-icon-triangle-1-s'} });
     */

    $(".action-btn").live('click', function(event) {
        //show the menu directly over the placeholder
        var actions = $(this).parent().children(".actions");

        // Need to toggle before setting the position
        actions.toggle();

        // Set the position for the actions menu
        actions.position({
            my: "left top",
            at: "left bottom",
            of: $(this).closest(".action-btn"),
            collision: "flip fit"
        });

        // To prevent the action button from POST'ing to the server
        event.preventDefault();
    });

    $(".action-menu-item").click(function(event) {
        var actions = $(this).parent().children(".actions");

        // Need to toggle before setting the position
        actions.toggle();
    });

    $(".action-hover-btn").click(function(event) {
        //show the menu directly over the placeholder
        var actions = $(this).parent().children(".actions");

        // Need to toggle before setting the position
        actions.toggle();

        // Set the position for the actions menu
        actions.position({
            my: "right top",
            at: "right bottom",
            of: $(this).closest(".action-hover-btn"),
            //offset: "0 0"
            collision: "flip"
        });

        // To prevent the action button from POST'ing to the server
        event.preventDefault();
    });
});