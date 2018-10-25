$(document).ready(function() {
    var scanner = $("body").scannerDetection();
    scanner.bind('scannerDetectionComplete',function(event,data){
        console.log("scanner detected");
        console.log(event);
        console.log(data);
        var barcode = data.string;
        $.ajax({
            dataType: "json",
            url: contextPath + "/json/scanBarcode?barcode=" + barcode,
            success: function (data) {
                console.log(data);
                if (data.url) {
                    if (confirm("The system has detected that a USB scanner was used and the barcode '" + barcode + "' was successfully found.  You are about to be redirected to the " + data.type + " page (" + data.url + ").\n\nAre you sure you want to redirected?")) {
                        window.location.replace(data.url);
                    }
                }
                else {

                    if (confirm("The system has detected that a USB scanner was used, but the barcode '" + barcode + "' was not found.  Would you like to be redirected to Google?")) {
                        window.location.replace("http://www.google.com?q=" + barcode);
                    }
                }
            },
            error: function(xhr, status, error) {
                console.log(status);
            }
        });
    });

    scanner.bind('scannerDetectionError',function(event,data){
        console.log("Error detecting barcode scanner input", event, data);
        console.log(event);
        console.log(data);
    });
});