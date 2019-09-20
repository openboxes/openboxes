<%@ page defaultCodec="html" %>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title><warehouse:message code="report.showInventoryReport.label" default="Inventory report" /></title>
    <!-- DataTables CSS -->
    <link rel="stylesheet" type="text/css" href="//ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css">

    <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.24/themes/smoothness/jquery-ui.css" type="text/css" media="all" />

    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css" type="text/css" media="all" />

    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css">

    <link rel="stylesheet" href="${createLinkTo(dir:'css/',file:'dashboard.css')}" type="text/css" media="all" />

    <script type="text/javascript" charset="utf8" src="//ajax.aspnetcdn.com/ajax/jQuery/jquery-1.8.2.min.js"></script>

    <script type="text/javascript" charset="utf8" src="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.2/jquery-ui.min.js"></script>

    <script type="text/javascript" charset="utf8" src="//ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"></script>

    <script type="text/javascript" charset="utf8" src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>

    <style>
    .navbar-collapse.in {
        overflow-y: visible;
    }
    </style>


</head>
<body>



    <input type="hidden" id="locationId" name="locationId" value="${session.warehouse.id}"/>

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
            <div class="navbar-collapse collapse">
                <ul class="nav navbar-nav">
                    <li><a href="${createLink(uri: '/dashboard/index')}">Dashboard</a></li>
                    <li class="active"><a href="${createLink(uri: '/dashboard/index')}">Analytics</a></li>
                </ul>
                <ul class="nav navbar-nav navbar-right">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">${session.user.name} <b class="caret"></b></a>
                        <ul class="dropdown-menu">
                            <li>
                                <g:link controller="user" action="show" id="${session.user.id }">
                                    <warehouse:message code="profile.label" default="Profile"/>
                                </g:link>
                            </li>
                            <li>
                                <g:link  controller="dashboard" action="index">
                                    <warehouse:message code="dashboard.label" default="Dashboard"/>
                                </g:link>
                            </li>
                            <g:isUserAdmin>
                                <g:if test="${session._showTime}">
                                    <li class="action-menu-item">
                                        <g:link controller="dashboard" action="index" params="[showTime:'off']">
                                            <warehouse:message code="dashboard.disableShowTime.label" default="Disable show time"/>
                                        </g:link>
                                    </li>
                                </g:if>
                                <g:else>
                                    <li>
                                        <g:link controller="dashboard" action="index" params="[showTime:'on']">
                                            <warehouse:message code="dashboard.enableShowTime.label" default="Enable show time"/>
                                        </g:link>
                                    </li>
                                </g:else>
                                <g:if test="${session.useDebugLocale }">
                                    <li>
                                        <g:link controller="user" action="disableLocalizationMode">
                                            ${warehouse.message(code:'debug.disable.label', default: 'Disable debug mode')}
                                        </g:link>
                                    </li>
                                </g:if>
                                <g:else>
                                    <li>
                                        <g:link controller="user" action="enableLocalizationMode">
                                            ${warehouse.message(code:'debug.enable.label', default: 'Enable debug mode')}
                                        </g:link>
                                    </li>
                                </g:else>
                                <li>
                                    <g:link controller="dashboard" action="flushCache">
                                        ${warehouse.message(code:'cache.flush.label', default: 'Flush cache')}
                                    </g:link>
                                </li>
                            </g:isUserAdmin>
                            <li class="divider"></li>
                            <li>
                                <g:link class="list" controller="auth" action="logout">
                                    <warehouse:message code="default.logout.label"/>
                                </g:link>
                            </li>


                        </ul>
                    </li>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">${session.warehouse.name} <b class="caret"></b></a>
                        <ul class="dropdown-menu">
                            <g:each var="entry" in="${session.loginLocationsMap}" status="i">
                                <li class="dropdown-header">
                                    <g:if test="${!entry?.key }">
                                        ${warehouse.message(code: 'default.others.label', default: 'Others')}
                                    </g:if>
                                    <g:else>
                                        ${entry.key }
                                    </g:else>
                                </li>
                                <g:each var="warehouse" in="${entry.value.sort() }">
                                    <li>
                                        <g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
                                        <a class="button" href='${createLink(controller: "dashboard", action:"chooseLocation", id: warehouse.id, params:['targetUri':targetUri])}'>
                                            <format:metadata obj="${warehouse}"/>

                                        </a>
                                    </li>
                                </g:each>
                            </g:each>


                        </ul>
                    </li>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Help <b class="caret"></b></a>
                        <ul class="dropdown-menu">
                            <li>
                                <g:link url="http://openboxes.atlassian.net/wiki/questions" target="_blank">
                                    <warehouse:message code="docs.faq.label" default="Questions"/>
                                </g:link>
                            </li>
                            <li>
                                <g:link url="https://www.dropbox.com/sh/okkhdne14rju65d/JD9TpTUOt6" target="_blank">
                                    <warehouse:message code="docs.userGuide.label" default="User Guide"/>
                                </g:link>
                            </li>
                            <li>
                                <g:link url="https://groups.google.com/forum/#!forum/openboxes" target="_blank">
                                    <warehouse:message code="docs.forum.label" default="Forum"/>
                                </g:link>
                            </li>
                            <li>
                                <g:link url="https://github.com/openboxes/openboxes/releases/tag/v${g.meta(name:'app.version')}" target="_blank">
                                    <warehouse:message code="docs.releaseNotes.label" default="Release Notes"/> (${g.meta(name:'app.version')})
                                </g:link>
                            </li>
                            <li class="divider"></li>
                            <li>
                                <g:link url="https://openboxes.atlassian.net/secure/CreateIssue!default.jspa" target="_blank">
                                    <warehouse:message code="docs.reportBug.label" default="Report a Bug"/>
                                </g:link>
                            </li>
                            <li>
                                <g:link url="mailto:support@openboxes.com" target="_blank">
                                    <warehouse:message code="docs.contactSupport.label" default="Contact Support"/>
                                </g:link>
                            </li>
                            <li>
                                <g:link url="mailto:feedback@openboxes.com" data-uv-trigger="contact" target="_blank">
                                    <warehouse:message code="docs.provideFeedback.label" default="Provide Feedback"/>
                                </g:link>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div><!--/.nav-collapse -->
        </div>

    </div>
<div id='content' class="body">
    <div class="container-fluid">
        <div class="row">
            <div class="col-sm-3 col-md-2 sidebar">
                <form role="form" controlle="report" action="exportInventoryReport">
                    <div class="form-group">
                        <label><i class="glyphicon glyphicon-tag"></i> <warehouse:message code="product.status.label"/></label>
                    </div>
                    <g:each in="${['OVERSTOCK','IN_STOCK','IDEAL_STOCK','REORDER','LOW_STOCK','STOCK_OUT','NOT_STOCKED','INVALID']}" var="status" status="i">
                        <div class="checkbox">
                            <label for="status-${i}" title="${status}">
                                <g:checkBox id="status-${i}" name="status" value="${status}" checked="${false}" class="status-filter"/>
                                <span class=""><warehouse:message code="enum.InventoryLevelStatus.${status}"/></span>
                                <span class="badge" id="badge-status-${status}"></span>
                                <span class="" id="badge-percentage-${status}"></span>
                            </label>
                        </div>

                    </g:each>

                    <button id="refresh-btn" class="btn btn-primary">Refresh</button>
                    <button id="submit-btm" class="btn btn-default" target="_blank">Export</button>

                </form>
            </div>
            <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                <g:if test="${flash.message}">
                    <div id="message" class="alert alert-warning">
                        ${flash.message}

                    </div>
                </g:if>

                <div>
                    <div id="dataTableDiv">
                        <table id="dataTable">
                            <thead>
                            <tr>
                                <th>Product Group ID</th>
                                <th>Inventory Level ID</th>
                                <th>Status</th>
                                <th>Name</th>
                                <th>Product codes</th>
                                <th>Min</th>
                                <th>Reorder</th>
                                <th>Max</th>
                                <th>QoH</th>
                                <th>Total Value</th>
                                <th>Has Product Group</th>
                                <th>Has Inventory Level</th>
                            </tr>
                            </thead>
                        </table>

                    </div>

                    <hr>

                    <footer>
                        <p>
                        &copy; OpenBoxes 2014
                        </p>
                    </footer>
                </div>
            </div>

        </div>
    </div>
</div>



<g:if test="${session?.warehouse}">

    <li>
        <a href="javascript:void(0);" class="warehouse-switch" style="color: #666">
            <img src="${resource(dir: 'images/icons/silk', file: 'map.png')}"/>
            <warehouse:message code="dashboard.changeLocation.label" default="Change location"/>
        </a>
        <div id="warehouseMenu" title="${warehouse.message(code:'dashboard.chooseLocation.label')}" style="display: none; padding: 10px;">
            <div style="max-height: 400px; overflow: auto;">
                <table>
                    <g:set var="count" value="${0 }"/>

                    <g:each var="entry" in="${session.loginLocationsMap}" status="i">
                        <tr class="prop">
                            <td class="name">
                                <g:if test="${!entry?.key }">
                                    <h3>${warehouse.message(code: 'default.others.label', default: 'Others')}</h3>
                                </g:if>
                                <g:else>
                                    <h3>${entry.key }</h3>
                                </g:else>
                            </td>
                            <td class="value">
                                <div>
                                    <g:each var="warehouse" in="${entry.value.sort() }">
                                        <div class="left" style="margin: 1px;">
                                            <g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
                                            <a class="button" href='${createLink(controller: "dashboard", action:"chooseLocation", id: warehouse.id, params:['targetUri':targetUri])}'>
                                                <format:metadata obj="${warehouse}"/>

                                            </a>
                                        </div>
                                    </g:each>
                                </div>
                            </td>
                        </tr>
                    </g:each>
                </table>
                <g:unless test="${session.loginLocationsMap }">
                    <div style="background-color: black; color: white;" class="warehouse button">
                        <warehouse:message code="dashboard.noWarehouse.message"/>
                    </div>
                </g:unless>
            </div>
        </div>
    </li>
</g:if>
    <script type="text/javascript">
        $.fn.dataTableExt.oApi.fnReloadAjax = function ( oSettings, sNewSource, fnCallback, bStandingRedraw ) {
            // DataTables 1.10 compatibility - if 1.10 then versionCheck exists.
            // 1.10s API has ajax reloading built in, so we use those abilities
            // directly.
            if ( $.fn.dataTable.versionCheck ) {
                var api = new $.fn.dataTable.Api( oSettings );

                if ( sNewSource ) {
                    api.ajax.url( sNewSource ).load( fnCallback, !bStandingRedraw );
                }
                else {
                    api.ajax.reload( fnCallback, !bStandingRedraw );
                }
                return;
            }

            if ( sNewSource !== undefined && sNewSource !== null ) {
                oSettings.sAjaxSource = sNewSource;
            }

            // Server-side processing should just call fnDraw
            if ( oSettings.oFeatures.bServerSide ) {
                this.fnDraw();
                return;
            }

            this.oApi._fnProcessingDisplay( oSettings, true );
            var that = this;
            var iStart = oSettings._iDisplayStart;
            var aData = [];

            this.oApi._fnServerParams( oSettings, aData );

            oSettings.fnServerData.call( oSettings.oInstance, oSettings.sAjaxSource, aData, function(json) {
                /* Clear the old information from the table */
                that.oApi._fnClearTable( oSettings );

                /* Got the data - add it to the table */
                var aData =  (oSettings.sAjaxDataProp !== "") ?
                        that.oApi._fnGetObjectDataFn( oSettings.sAjaxDataProp )( json ) : json;

                for ( var i=0 ; i<aData.length ; i++ )
                {
                    that.oApi._fnAddData( oSettings, aData[i] );
                }

                oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();

                that.fnDraw();

                if ( bStandingRedraw === true )
                {
                    oSettings._iDisplayStart = iStart;
                    that.oApi._fnCalculateEnd( oSettings );
                    that.fnDraw( false );
                }

                that.oApi._fnProcessingDisplay( oSettings, false );

                /* Callback user function - for event handlers etc */
                if ( typeof fnCallback == 'function' && fnCallback !== null ) {
                    fnCallback( oSettings );
                }
            }, oSettings );
        };



        $( document ).ready(function() {
            var locationId = $("#locationId").val();

            var dataTable = $('#dataTable').dataTable( {
                "bProcessing": true,
                "sServerMethod": "GET",
                "sPaginationType": "full_numbers",
                "sAjaxSource": "${request.contextPath}/json/getQuantityOnHandByProductGroup",
                "fnServerParams": function ( data ) {
                    data.push({ "name": "location.id", "value": locationId });
                    console.log(data);
                    $(".status-filter").each(function(index, value) {
                        if (this.checked) {
                            data.push({ "name": this.name, "value": this.value });
                        }
                    });
                },
                "iDisplayLength" : -1,
                "aLengthMenu": [
                    [25, 50, 100, 500, 1000, -1],
                    [25, 50, 100, 500, 1000, "All"]
                ],
                "aoColumns": [
                    { "mData": "id", "bSearchable": false, "bVisible": false },
                    { "mData": "inventoryLevelId", "bSearchable": false, "bVisible": false },
                    { "mData": "status" }, // 0
                    { "mData": "name" }, // 1
                    { "mData": "productCodes" }, // 2
                    { "mData": "minQuantity" }, // 3
                    { "mData": "reorderQuantity" }, // 4
                    { "mData": "maxQuantity" }, // 5
                    { "mData": "onHandQuantity" }, //6
                    { "mData": "totalValue" }, // 7
                    { "mData": "hasProductGroup" },  // 8
                    { "mData": "hasInventoryLevel" } // 9

                ],
                "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
                    console.log(aData);
                    switch(aData["status"]){
                        case 'IN_STOCK':
                            $(nRow).css('color', 'green')
                            break;
                        case 'NOT_STOCKED':
                            $(nRow).css('color', 'grey')
                            break;
                        case 'STOCK_OUT':
                            $(nRow).css('color', 'red')
                            break;
                        case 'LOW_STOCK':
                            $(nRow).css('color', 'orange')
                            break;
                        case 'REORDER':
                            $(nRow).css('color', '#eed7b0;')
                            break;
                        case 'IDEAL_STOCK':
                            $(nRow).css('color', 'green')
                            break;
                        case 'OVERSTOCK':
                            $(nRow).css('color', 'blue')
                            break;
                        case 'INVALID':
                            $(nRow).css('color', 'grey')
                            break;
                    }
                    if (aData["id"]) {
                        $('td:eq(1)', nRow).html('<a href="/openboxes/productGroup/edit/' + aData["id"] + '" target="_blank">' + aData["name"] + '</a>');
                    }
                    if (aData["inventoryLevelId"]) {
                        $('td:eq(3)', nRow).html('<a href="/openboxes/inventoryLevel/edit/' + aData["inventoryLevelId"] + '" target="_blank">' + aData["minQuantity"] + '</a>');
                        $('td:eq(4)', nRow).html('<a href="/openboxes/inventoryLevel/edit/' + aData["inventoryLevelId"] + '" target="_blank">' + aData["reorderQuantity"] + '</a>');
                        $('td:eq(5)', nRow).html('<a href="/openboxes/inventoryLevel/edit/' + aData["inventoryLevelId"] + '" target="_blank">' + aData["maxQuantity"] + '</a>');
                    }
                    return nRow;
                }
            });


            $('#refresh-btn').click( function (event) {
                event.preventDefault();
                dataTable.fnClearTable();
                dataTable.fnReloadAjax('${request.contextPath}/json/getQuantityOnHandByProductGroup');
            } );

            $.ajax({
                dataType: "json",
                timeout: 60000,
                url: "${request.contextPath}/json/getSummaryByProductGroup?location.id=${session.warehouse.id}",
                success: function (data) {
                    console.log("Loading data ...");
                    console.log(data);
                    $.each(data, function( key, value ) {
                        console.log(key);

                        console.log(value);
                        $("#badge-status-" + key).html(value.numProductGroups);
                        $("#badge-percentage-" + key).html(Math.round(value.percentage * 100) + "%");
                    });
                },
                error: function(xhr, status, error) {
                    console.log(xhr);
                    console.log(status);
                    console.log(error);

                }
            });

        });

    </script>

</body>
</html>
