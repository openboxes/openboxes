<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><g:layoutTitle default="OpenBoxes"/></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl"
          crossorigin="anonymous">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.2/css/all.min.css"
          rel="stylesheet"/>
</head>

<body class="d-flex flex-column h-100">
    <g:include controller="mobile" action="menu"/>
    <main class="flex-shrink-0">
        <div class="container">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><g:link controller="mobile">Home</g:link></li>
                    <g:if test="${session.warehouse}">
                        <li class="breadcrumb-item"><g:link controller="mobile"
                                                            action="chooseLocation">${session?.warehouse?.name}</g:link></li>
                    </g:if>
                    <g:if test="${actionName == 'outboundDetails'}">
                        <li class="breadcrumb-item"><g:link controller="mobile"
                                                            action="outboundList">Outbound List</g:link></li>
                    </g:if>
                    <g:elseif test="${actionName == 'inboundDetails'}">
                        <li class="breadcrumb-item"><g:link controller="mobile"
                                                            action="inboundList">Inbound List</g:link></li>
                    </g:elseif>

                    <li class="breadcrumb-item active" aria-current="page"><g:layoutTitle/></li>
                </ol>
            </nav>
            <g:if test="${flash.message}">
                <div class="alert alert-info">${flash.message}</div>
            </g:if>
            <g:layoutBody/>
        </div>
    </main>
%{--    <footer class="footer mt-auto py-3 bg-light">--}%
%{--        <div class="container-fluid">--}%
%{--            <span class="text-muted">Place sticky footer content here.</span>--}%
%{--        </div>--}%
%{--    </footer>--}%

<script
        src="https://code.jquery.com/jquery-3.5.1.slim.min.js"
        integrity="sha256-4+XzXVhsDmqanXGHaHvgh1gMQKX40OUvDEBTu8JcmNs="
        crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0"
        crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.2/js/all.min.js"></script>
<script src="/openboxes/js/onScan/onScan.min.js" type="text/javascript"></script>
<script>
  $(document)
  .ready(function () {
    // Enable scan events for the entire document
    console.log("initialize onScan");
    onScan.attachTo(document, {
      minLength: 3,
      suffixKeyCodes: [13], // enter-key expected at the end of a scan
      //reactToPaste: true, // Compatibility to built-in scanners in paste-mode (as opposed to keyboard-mode)
      onScan: function (scanned, count) {
        console.log('Scanned: ', count, 'x ', scanned);
        alert("Scanned " + scanned)
      },
      onKeyDetect: function (keyCode, event) {
        console.log('Pressed: ', keyCode, event);
      },
      onScanError: function (obj) {
        console.log('onScanError: ', obj);
      },
      onScanButtonLongPress: function (obj) {
        console.log('onScanButtonLongPress: ', obj);
      },
      onKeyProcess: function (char, event) {
        console.log('onKeyProcess: ', char, event);
      }
    });
  });
</script>
</body>
</html>
