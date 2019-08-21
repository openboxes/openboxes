<%@ page contentType="text/html;charset=UTF-8" %>
<html lang="en" ng-app='APP' >
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="" />
    <title>OpenBoxes Analytics</title>

    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">

    <!-- Optional theme -->
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css">

    <link rel="stylesheet" href="${createLinkTo(dir:'css/',file:'dashboard.css')}" type="text/css" media="all" />

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

                <a class="navbar-brand" href="#"><i class="glyphicon glyphicon-th"></i> OpenBoxes Analytics</a>
            </div>
        </div>
    </div>




<div id='content'  >
    <div class="container-fluid">
        <div class="row">
            <div class="col-sm-3 col-md-2 sidebar">
                <p>
                    Group by:
                    <a ng-click="groupBy('status')">Status</a> -
                    <a ng-click="groupBy('genericProduct')">Generic product</a>
                </p>

                <h2>
                    <i class="glyphicon glyphicon-tag"></i> <warehouse:message code="product.status.label"/>
                </h2>
                <ul class="nav nav-sidebar">
                    <li>
                        <a href="#"><span class="label label-info">All stock</span> <span class="badge">{{allStockCount}}</span></a>
                    </li>
                    <li>
                        <a href="#"><span class="label label-info">Overstock</span> <span class="badge">{{overStockCount}}</span></a>
                    </li>
                    <li class="">
                        <a href="#"><span class="label label-success">In stock</span> <span class="badge">{{inStockCount}}</span></a>
                    </li>
                    <li>
                        <a href="#"><span class="label label-warning">Reorder stock</span> <span class="badge">{{reorderStockCount}}</span></a>
                    </li>
                    <li>
                        <a href="#"><span class="label label-warning">Low stock</span> <span class="badge">{{lowStockCount}}</span></a>
                    </li>
                    <li>
                        <a href="#"><span class="label label-danger">Out of stock</span> <span class="badge">{{outOfStockCount}}</span></a>
                    </li>
                </ul>
                <h2>
                    <i class="glyphicon glyphicon-filter"></i> <warehouse:message code="default.filters.label"/>
                </h2>
                <div class="form-group">
                    <pre>searchTerm Object: {{searchTerm | json}}</pre>
                    <input type="text" ng-model="searchTerm.$">
                </div>
                <div class="form-group">
                    <input type='text' ng-model='searchTerm.name' class="form-control"/>

                </div>
                <div class="form-group">
                    <input type='text' ng-model='searchTerm.genericProduct' class="form-control"/>

                </div>
                <div class="form-group">
                    <input type='text' ng-model='searchTerm.pricePerUnit' class="form-control"/>

                </div>
                <div class="form-group">
                    <input type='text' ng-model='searchTerm.totalValue' class="form-control"/>
                </div>

                <div class="form-group">
                    <select ng-model="searchTerm.status" ng-options="label for label in availableStatuses" class="form-control">
                        <option value="">All statuses</option>
                    </select>
                </div>


                <div class="form-group">
                    <select ng-model="pageSize" class="form-control">
                        <option value="100000">All</option>
                        <option value="10">10</option>
                        <option value="25">25</option>
                        <option value="50">50</option>
                        <option value="100">100</option>
                        <option value="1000">1000</option>
                    </select>
                </div>

                <hr/>
                <h2>
                    <i class="glyphicon glyphicon-usd"></i>
                    <warehouse:message code="default.totalValue.label" default="Total value"/>
                </h2>
                <div class="form-group">
                    <div class="well">
                        {{totalValue|currency:'$'}}
                    </div>
                </div>

                <hr/>

                <div class="form-group">

                    <div ng-show="results|filter:searchText">
                        <button ng-click="export()" class="btn btn-primary">Export XLS</button>
                    </div>
                </div>
            </div>
            <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
                <div ng-controller="Ctrl">
                    <div ng-view></div>
                    <hr>

                    <footer>
                        <p>
                            &copy; OpenBoxes 2014
                            <a href="#" ng-click="changeLanguage('en')" translate="default.button.language.en.label" class="ng-scope">english</a> -
                            <a href="#" ng-click="changeLanguage('de')" translate="default.button.language.de.label" class="ng-scope">german</a>

                        </p>
                    </footer>
                </div>
            </div>
        </div>
    </div>
</div>

    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
    <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.15/angular.min.js"></script>
    <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.15/angular-route.min.js"></script>
    <script src="${createLinkTo(dir:'js/angular-translate.min.js', file:'angular-translate.min.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'analytics/app.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'analytics/controllers.js')}" type="text/javascript" ></script>
    <script src="//rawgithub.com/eligrey/FileSaver.js/master/FileSaver.js" type="text/javascript"></script>
    <script src="//rawgithub.com/bouil/angular-google-chart/gh-pages/ng-google-chart.js" type="text/javascript"></script>
</body>
</html>