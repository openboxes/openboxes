<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><g:layoutTitle default="OpenBoxes"/></title>
    <r:layoutResources/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.2/css/all.min.css" rel="stylesheet"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/chartist.js/latest/chartist.min.css">

    <style>
    .sidebar {
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        z-index: 100;
        padding: 90px 0 0;
        box-shadow: inset -1px 0 0 rgba(0, 0, 0, .1);
        z-index: 99;
    }

    @media (max-width: 767.98px) {
        .sidebar {
            top: 11.5rem;
            padding: 0;
        }
    }

    .navbar {
        box-shadow: inset 0 -1px 0 rgba(0, 0, 0, .1);
    }

    @media (min-width: 767.98px) {
        .navbar {
            top: 0;
            position: sticky;
            z-index: 999;
        }
    }

    .sidebar .nav-link {
        color: #333;
    }

    .sidebar .nav-link.active {
        color: #0d6efd;
    }
    </style>

</head>
<body>
<nav class="navbar navbar-light bg-light p-3">
    <div class="d-flex col-12 col-md-3 col-lg-2 mb-2 mb-lg-0 flex-wrap flex-md-nowrap justify-content-between">
        <g:link controller="mobile"
                class="d-flex align-items-center my-2 my-lg-0 me-lg-auto text-decoration-none">
            <g:if test="${session.warehouse}">
                <g:displayLogo location="${session?.warehouse?.id}" includeLink="${false}"/>
            </g:if>
            <g:else>
                <a class="navbar-brand" href="#">
                    <img src="https://openboxes.com/img/logo_30.png" />
                </a>
            </g:else>
        </g:link>
        <button class="navbar-toggler d-md-none collapsed mb-3" type="button" data-toggle="collapse" data-target="#sidebar" aria-controls="sidebar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
    </div>
    <div class="col-12 col-md-4 col-lg-2">
        <input class="form-control form-control-dark" type="text" placeholder="Search" aria-label="Search">
    </div>
    <div class="col-12 col-md-5 col-lg-8 d-flex align-items-center justify-content-md-end mt-3 mt-md-0">
        <div class="dropdown">
            <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-expanded="false">
                Hello, ${session.user}
            </button>
            <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                <li><a class="dropdown-item" href="#">Profile</a></li>
                <li><a class="dropdown-item" href="${createLink(controller: 'auth', action: 'logout')}">Sign out</a></li>
            </ul>
        </div>
    </div>
</nav>
<div class="container-fluid">

    <div class="row">

        <!-- sidebar -->
        <nav id="sidebar" class="col-md-3 col-lg-2 d-md-block bg-light sidebar collapse">
            <div class="position-sticky">
                <ul class="nav flex-column">
                    <li class="nav-item">
                        <a class="nav-link active" aria-current="page" href="${createLink(controller: 'mobile', action: 'index')}">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-home"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
                            <span class="ml-2"> Dashboard </span>
                        </a>
                    </li>
                </ul>
            </div>
        </nav>

        <!-- main content -->
        <main class="col-md-9 ml-sm-auto col-lg-10 px-md-4 py-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><g:link controller="mobile">Home</g:link></li>
                    <g:if test="${session.warehouse}">
                        <li class="breadcrumb-item"><g:link controller="mobile"
                                                            action="chooseLocation" class="text-capitalize">${session?.warehouse?.name?.toLowerCase()}</g:link></li>
                    </g:if>
                    <li class="breadcrumb-item active" aria-current="page"><g:layoutTitle/></li>
                </ol>
            </nav>
            <g:if test="${flash.message}">
                <div class="alert alert-info">${flash.message}</div>
            </g:if>

            <g:layoutBody />
        </main>

        <!-- footer -->
%{--        <footer class="pt-5 d-flex justify-content-between">--}%
%{--            <span>Copyright © 2021 <a href="https://openboxes.com">Openboxes</a></span>--}%
%{--            <ul class="nav m-0">--}%
%{--                --}%
%{--            </ul>--}%
%{--        </footer>--}%
    </div>
</div>

<r:layoutResources/>
<script
        src="https://code.jquery.com/jquery-3.5.1.slim.min.js"
        integrity="sha256-4+XzXVhsDmqanXGHaHvgh1gMQKX40OUvDEBTu8JcmNs="
        crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM" crossorigin="anonymous"></script>

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
