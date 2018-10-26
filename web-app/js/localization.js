// Define the localization
if(typeof openboxes === "undefined") openboxes = {};
if(typeof openboxes.localization === "undefined") openboxes.localization = {};
openboxes.localization.Localization = function(data) {
    console.log(data);
    var self = this;
    if(!data) data = {};
    self.id = ko.observable(data.id);
    self.code = ko.observable(data.code);
    self.locale = ko.observable(data.locale);
    self.text = ko.observable(data.text);
    self.translation = ko.observable(data.translation);
    //self.deleteUrl = ko.observable(contextPath + "/json/deleteLocalization?id=" + data.id);
    //self.resolvedText = ko.observablae(data.resolvedText);
    //self.lastUpdated = ko.observable(data.lastUpdated);
    //self.version = ko.observable(data.version);
};

$(document).ready(function() {
    // Initialize the localization dialog
    $("#localization-dialog").dialog({ autoOpen: false, modal: true, width: '600px' });


    // Instantiate a new localization object to be used
    var data = { id:"", code: "", text: "", translation: "" };
    var viewModel = new openboxes.localization.Localization(data);
    ko.applyBindings(viewModel);

    // Delete localization event handler
    $("#delete-localization-btn").click(function() {
        event.preventDefault();
        console.log("delete localization");
        console.log($(this));
        console.log($(event));
        if (viewModel.id() == undefined) {
            alert("This translation is not currently saved to the database so it cannot be deleted.");
        }
        else {
            $.ajax({
                url: contextPath + "/json/deleteLocalization",
                type: "get",
                contentType: 'text/json',
                dataType: "json",
                data: {id: viewModel.id() },
                success: function(data) {
                    alert("You have successfully deleted this localization.");
                    location.reload();
                },
                error: function(data) {
                    alert("An error occurred while deleting this translation.");
                }
            });
        }

    });

    // Close dialog event handler
    $("#close-localization-dialog-btn").click(function() {
        event.preventDefault();
        $("#localization-dialog").dialog("close");
    });

    // Help event handler
    $("#help-localization-btn").click(function() {
        event.preventDefault();
        $.ajax({
            url: contextPath + "/json/getTranslation",
            type: "get",
            contentType: 'text/json',
            dataType: "json",
            data: {text: viewModel.text, src: "en", dest: "fr"},
            success: function(data) {
                //alert("success: " + data);
                console.log(data);
                viewModel.translation = data;
                //ko.applyBindings(viewModel);
            },
            error: function(data) {
                //console.log(data);
                //alert("error");
                viewModel.translation = "Error. Try again.";
                //ko.applyBindings(viewModel);
            }
        });
    });

    // Save event handler
    $("#save-localization-btn").click(function() {
        event.preventDefault();
        var jsonData = ko.toJSON(viewModel);
        console.log("save localization");
        console.log(jsonData);

        $.ajax({
            url: contextPath + "/json/saveLocalization",
            type: "post",
            contentType: 'text/json',
            dataType: "json",
            data: jsonData,
            success: function(data) {
                //alert("success");
                $("#localization-dialog").dialog("close");
                location.reload();
            },
            error: function(data) {
                //alert("fail");
                $("#localization-dialog").dialog("close");
                location.reload();
            }
        });
    });

    // Open dialog event handler
    $(".open-localization-dialog").click(function() {
        var id = $(this).attr("data-id");
        var code = $(this).attr("data-code");
        var resolvedMessage = $(this).attr("data-resolved-message");
        console.log("Get localization");
        console.log(id);
        console.log(code);
        var url = contextPath + "/json/getLocalization";
        $.getJSON( url, { id: id, code: code, resolvedMessage: resolvedMessage },
                function (data, status, jqxhr) {
                    console.log("getJSON response: ");
                    console.log(data);
                    viewModel.id(data.id);
                    viewModel.code(data.code);
                    viewModel.text(data.text);
                    viewModel.locale(data.locale);
                    viewModel.translation(data.translation);
                }
        );

        $("#localization-dialog").dialog('open');
        event.preventDefault();
    });

});