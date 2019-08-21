<%@ page contentType="text/html;charset=UTF-8" %>
<html lang="en" ng-app='pivotApp' >
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="" />
    <title>OpenBoxes Analytics</title>

    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">

    <!-- Optional theme -->
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css">

    <link rel="stylesheet" href="${createLinkTo(dir:'css/',file:'dashboard.css')}" type="text/css" media="all" />

    <link rel="stylesheet" href="${createLinkTo(dir:'js/pivottable/',file:'pivot.css')}" type="text/css" media="all" />


</head>

<body ng-controller="appController">

    <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="#">OpenBoxes Analytics</a>
            </div>
        </div>
    </div>



    <div id='content' ng-controller='mainController' ><%--ng-init="init()"--%>
        <div class="container-fluid">
            <div class="row">
                <div class="col-sm-3 col-md-2 sidebar">

                    TBD

                </div>
                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
                    <h1 class="page-header"><warehouse:message code="inventory.label"/></h1>

                    <div class="alert alert-info" ng-show="loading">Loading...</div>

                    <div id="output" style="margin: 30px;"></div>


                </div>
                <hr>

                <footer>
                    <p>&copy; OpenBoxes 2014</p>
                </footer>

            </div>
        </div>



    </div>

<script src="http://nicolas.kruchten.com/pivottable/examples/ext/jquery-1.8.3.min.js"></script>
<script src="http://nicolas.kruchten.com/pivottable/examples/ext/jquery-ui-1.9.2.custom.min.js"></script>
<script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
<script src="${createLinkTo(dir:'js/', file:'angular.min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/pivottable', file:'pivot.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/', file:'pivot.js')}" type="text/javascript" ></script>
<script src="https://rawgithub.com/eligrey/FileSaver.js/master/FileSaver.js" type="text/javascript"></script>
<script src="https://rawgithub.com/bouil/angular-google-chart/gh-pages/ng-google-chart.js" type="text/javascript"></script>
<script type="text/javascript">
    $(function(){
        var derivers = $.pivotUtilities.derivers;

        $.getJSON("/${request.contextPath}/json/calculateQuantityOnHandByProduct", function(data) {
            $("#output").pivotUI(data, {
                derivedAttributes: {
                    "Age Bin": derivers.bin("Age", 10),
                    "Gender Imbalance": function(mp) {
                        return mp["Gender"] == "Male" ? 1 : -1;
                    }
                },
                rows: ["name"],
                cols: ["totalValue"],
                rendererName: "Heatmap"
            });
        });
    });
</script>

</body>
</html>