<%@ page contentType="text/html;charset=UTF-8" %>
<html lang="en" ng-app='APP' >
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="" />
    <title>OpenBoxes Analytics</title>

    <%--<link rel="stylesheet" href="${createLinkTo(dir:'js/bootstrap/css',file:'bootstrap.css')}" type="text/css" media="all" />--%>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">

    <!-- Optional theme -->
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css">


    <style type="text/css">


        .fade { color: #666; }
    /*
     * Base structure
     */

    /* Move down content because we have a fixed navbar that is 50px tall */
    body {
        padding-top: 50px;
    }


    /*
     * Global add-ons
     */

    .sub-header {
        padding-bottom: 10px;
        border-bottom: 1px solid #eee;
    }


    /*
     * Sidebar
     */

    /* Hide for mobile, show later */
    .sidebar {
        display: none;
    }
    @media (min-width: 768px) {
        .sidebar {
            position: fixed;
            top: 51px;
            bottom: 0;
            left: 0;
            z-index: 1000;
            display: block;
            padding: 20px;
            overflow-x: hidden;
            overflow-y: auto; /* Scrollable contents if viewport is shorter than content. */
            background-color: #f5f5f5;
            border-right: 1px solid #eee;
        }
    }

    /* Sidebar navigation */
    .nav-sidebar {
        margin-right: -21px; /* 20px padding + 1px border */
        margin-bottom: 20px;
        margin-left: -20px;
    }
    .nav-sidebar > li > a {
        padding-right: 20px;
        padding-left: 20px;
    }
    .nav-sidebar > .active > a {
        color: #fff;
        background-color: #428bca;
    }


    /*
     * Main content
     */

    .main {
        padding: 20px;
    }
    @media (min-width: 768px) {
        .main {
            padding-right: 40px;
            padding-left: 40px;
        }
    }
    .main .page-header {
        margin-top: 0;
    }


    /*
     * Placeholder dashboard ideas
     */

    .placeholders {
        margin-bottom: 30px;
        text-align: center;
    }
    .placeholders h4 {
        margin-bottom: 0;
    }
    .placeholder {
        margin-bottom: 20px;
    }
    .placeholder img {
        display: inline-block;
        border-radius: 50%;
    }
    </style>

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
            <%--
            <div class="navbar-collapse collapse">
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="#">Dashboard</a></li>
                    <li><a href="#">Settings</a></li>
                    <li><a href="#">Profile</a></li>
                    <li><a href="#">Help</a></li>
                </ul>
                <form class="navbar-form navbar-right">
                    <input type="text" class="form-control" placeholder="Search...">
                </form>
            </div>
            --%>
        </div>
    </div>



    <div id='content' ng-controller='mainController' ><%--ng-init="init()"--%>
        <div class="container-fluid">
            <div class="row">
                <div class="col-sm-3 col-md-2 sidebar">
                    <%--
                    <ul class="nav nav-sidebar">
                        <li class="active"><a href="#">Overview</a></li>
                        <li><a href="#">Reports</a></li>
                        <li><a href="#">Analytics</a></li>
                        <li><a href="#">Export</a></li>
                    </ul>
                    <ul class="nav nav-sidebar">
                        <li><a href="">Nav item</a></li>
                        <li><a href="">Nav item again</a></li>
                        <li><a href="">One more nav</a></li>
                        <li><a href="">Another nav item</a></li>
                        <li><a href="">More navigation</a></li>
                    </ul>
                    <ul class="nav nav-sidebar">
                        <li><a href="">Nav item again</a></li>
                        <li><a href="">One more nav</a></li>
                        <li><a href="">Another nav item</a></li>
                    </ul>
                    --%>
                    <h2>
                        <i class="glyphicon glyphicon-tag"></i> <warehouse:message code="product.status.label"/>
                    </h2>
                    <ul class="nav nav-sidebar">
                        <li class="active">
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
<%--
                    <h2>
                        <i class="glyphicon glyphicon-stats"></i> <warehouse:message code="default.chart.label" default="Chart"/>
                    </h2>
                    <div class="row">
                        <div google-chart chart="chart" style="{{chart.cssStyle}}"></div>
                    </div>
--%>
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
                        <select ng-model="statusFilter" ng-options="label for label in availableStatuses" class="form-control">
                            <option value="">All statuses</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <select ng-model="limitTo" class="form-control">
                            <option value="10">10</option>
                            <option value="25">25</option>
                            <option value="50">50</option>
                            <option value="100">100</option>
                            <option value="-1">All</option>
                        </select>
                    </div>

                    <%--
                    <div class="form-group">
                        <div class="input-group">
                            <span class="input-group-addon">@</span>
                            <input type="text" class="form-control" placeholder="Username">
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="input-group">
                            <input type="text" class="form-control">
                            <span class="input-group-addon">.00</span>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="input-group">
                            <span class="input-group-addon">$</span>
                            <input type="text" class="form-control">
                            <span class="input-group-addon">.00</span>
                        </div>
                    </div>
                    --%>
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

                        <%--
                        <div>
                            <button ng-click="refresh()" class="btn btn-primary">Refresh</button>
                        </div>
                        --%>
                    </div>
                </div>
                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
                    <h1 class="page-header"><warehouse:message code="inventory.label"/></h1>
                    <%--
                    <div class="navbar navbar-default" role="navigation">
                        <div class="container-fluid">
                            <div class="navbar-header">
                                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                                    <span class="sr-only">Toggle navigation</span>
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                </button>
                                <a class="navbar-brand" href="#">Inventory</a>
                            </div>
                            <div class="navbar-collapse collapse">
                                <ul class="nav navbar-nav navbar-right">
                                    <li><a href="#">Dashboard</a></li>
                                    <li><a href="#">Settings</a></li>
                                    <li><a href="#">Profile</a></li>
                                    <li><a href="#">Help</a></li>
                                </ul>
                                <form class="navbar-form navbar-right">
                                    <input type="text" class="form-control" placeholder="Search...">
                                </form>
                            </div>
                        </div>
                    </div>
                    --%>




                    <%--
                    <div class="row placeholders">
                        <div class="col-xs-6 col-sm-3 placeholder">
                            <img data-src="holder.js/200x200/auto/sky" class="img-responsive" alt="Generic placeholder thumbnail">
                            <h4>Label</h4>
                            <span class="text-muted">Something else</span>
                        </div>
                        <div class="col-xs-6 col-sm-3 placeholder">
                            <img data-src="holder.js/200x200/auto/vine" class="img-responsive" alt="Generic placeholder thumbnail">
                            <h4>Label</h4>
                            <span class="text-muted">Something else</span>
                        </div>
                        <div class="col-xs-6 col-sm-3 placeholder">
                            <img data-src="holder.js/200x200/auto/sky" class="img-responsive" alt="Generic placeholder thumbnail">
                            <h4>Label</h4>
                            <span class="text-muted">Something else</span>
                        </div>
                        <div class="col-xs-6 col-sm-3 placeholder">
                            <img data-src="holder.js/200x200/auto/vine" class="img-responsive" alt="Generic placeholder thumbnail">
                            <h4>Label</h4>
                            <span class="text-muted">Something else</span>
                        </div>
                    </div>
                    --%>

                    <div class="alert alert-info" ng-show="loading">Loading...</div>

                    <div ng-init="filtered = (results | filter: searchTerm)">

                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="panel-title">
                                    Results &raquo; Returned {{(results | filter: searchTerm).length}} items in {{elapsedTime}} ms
                                </h3>

                            </div>

                            <div ng-switch="(results | filter:searchTerm).length">
                                <div ng-switch-when="0" class="panel-body">
                                    No results were found  <button ng-click='init()' class="button icon search">Show all</button>
                                </div>
                                <div id="exportable" class="table-responsive" ng-switch-default>

                                    <table class="table table-striped">
                                        <thead>
                                        <tr>
                                            <th><a href="" ng-click="sortBy='status'; reverse=!reverse;"><warehouse:message code="product.status.label"/></a></th>
                                            <th><warehouse:message code="product.productCode.label"/></th>
                                            <th><a href="" ng-click="sortBy='name'; reverse=!reverse;"><warehouse:message code="product.name.label"/></a></th>
                                            <th><a href="" ng-click="sortBy='genericProduct'; reverse=!reverse;"><warehouse:message code="product.genericProduct.label"/></a></th>
                                            <th><a href="" ng-click="sortBy='unitOfMeasure'; reverse=!reverse;"><warehouse:message code="product.unitOfMeasure.label"/></a></th>
                                            <th width="7%"><a href="" ng-click="sortBy='minQuantity'; reverse=!reverse;"><warehouse:message code="inventoryLevel.minQuantity.label"/></a></th>
                                            <th width="7%"><a href="" ng-click="sortBy='reorderQuantity'; reverse=!reverse;"><warehouse:message code="inventoryLevel.reorderQuantity.label"/></a></th>
                                            <th width="7%"><a href="" ng-click="sortBy='maxQuantity'; reverse=!reverse;"><warehouse:message code="inventoryLevel.maxQuantity.label"/></a></th>
                                            <th width="7%"><a href="" ng-click="sortBy='onHandQuantity'; reverse=!reverse;"><warehouse:message code="inventoryItem.quantityOnHand.label"/></a></th>
                                            <th width="7%"><a href="" ng-click="sortBy='pricePerUnit'; reverse=!reverse;"><warehouse:message code="product.pricePerUnit.label"/></a></th>
                                            <th width="10%"><a href="" ng-click="sortBy='totalValue'; reverse=!reverse;"><warehouse:message code="product.totalValue.label" default="Total value"/></a></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            <tr ng-repeat="result in results | filter:searchTerm | orderBy:sortBy:reverse | limitTo: limitTo">
                                                <td>
                                                    <span class="label" ng-class="{'label-success': result.status == 'IN_STOCK' || result.status == 'OVERSTOCK', 'label-danger': result.status == 'STOCK_OUT', 'label-warning': result.status == 'REORDER' || result.status == 'LOW_STOCK' }">
                                                        {{result.status}}
                                                    </span>
                                                </td>
                                                <td>
                                                    {{result.productCode}}
                                                </td>
                                                <td>
                                                    {{result.name}}
                                                </td>
                                                <td>
                                                    {{result.genericProduct | uppercase}}
                                                </td>
                                                <td>
                                                    {{result.unitOfMeasure | uppercase}}
                                                </td>
                                                <td>
                                                    {{result.minQuantity}}
                                                </td>
                                                <td>
                                                    {{result.reorderQuantity}}
                                                </td>
                                                <td>
                                                    {{result.maxQuantity}}
                                                </td>
                                                <td>
                                                    {{result.onHandQuantity}}
                                                </td>
                                                <td>
                                                    {{result.unitPrice | currency:"$" }}
                                                </td>
                                                <td>
                                                    {{result.totalValue | currency:"$" }}
                                                </td>
                                            </tr>
                                        </tbody>
                                        <tfoot>
                                            <tr>
                                                <th colspan="10">

                                                </th>
                                            </tr>
                                        </tfoot>

                                    </table>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
                <hr>

                <footer>
                    <p>&copy; OpenBoxes 2014</p>
                </footer>

            </div>
        </div>



    </div>


<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
<script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
<script src="${createLinkTo(dir:'js/', file:'angular.min.js')}" type="text/javascript" ></script>
<script src="${createLinkTo(dir:'js/', file:'app.js')}" type="text/javascript" ></script>
<script src="https://rawgithub.com/eligrey/FileSaver.js/master/FileSaver.js" type="text/javascript"></script>
<script src="https://rawgithub.com/bouil/angular-google-chart/gh-pages/ng-google-chart.js" type="text/javascript"></script>


</body>
</html>