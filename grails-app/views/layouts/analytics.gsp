<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="analytics" />
    <title><warehouse:message code="openboxes.analytics.title" default="OpenBoxes Analytics"/> | <g:layoutTitle/></title>

    <!-- Include Favicon -->
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

    <!-- CSS Stylesheets -->
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css" type="text/css" media="all" />
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css">
    <link rel="stylesheet" href="${createLinkTo(dir:'css/',file:'dashboard.css')}" type="text/css" media="all" />

    <link rel="stylesheet" href="//cdn.datatables.net/plug-ins/e9421181788/integration/bootstrap/3/dataTables.bootstrap.css" type="text/css" media="all" />

    <!-- Add some weird styling like black borders and weird button hover animations-->
    <link rel="stylesheet" type="text/css" href="//cdn.datatables.net/1.10.0/css/jquery.dataTables.css">
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css">

    <link rel="stylesheet" href="${createLinkTo(dir:'js/chosen', file:'chosen.css')}">
    <link rel="stylesheet" href="${createLinkTo(dir:'js/bootstrap-datepicker-1.3.0/css', file:'datepicker.css')}">

    <!-- Javascript -->
    <script type="text/javascript" charset="utf8" src="//ajax.aspnetcdn.com/ajax/jQuery/jquery-1.8.2.min.js"></script>
    <script type="text/javascript" charset="utf8" src="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.2/jquery-ui.min.js"></script>
    <script type="text/javascript" charset="utf8" src="//cdn.datatables.net/1.10.0/js/jquery.dataTables.js"></script>
    <script type="text/javascript" charset="utf8" src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
    <script type="text/javascript" charset="utf8" src="//cdn.datatables.net/plug-ins/e9421181788/integration/bootstrap/3/dataTables.bootstrap.js"></script>
    <script src="${createLinkTo(dir:'js/chosen', file:'chosen.jquery.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/bootstrap-datepicker-1.3.0/js', file:'bootstrap-datepicker.js')}" type="text/javascript" ></script>

    <g:layoutHead/>
    <r:layoutResources/>

    <style>
    .navbar-collapse.in {
        overflow-y: visible;

    }

    #dataTable_processing {
        //background-color:rgba(224,224,224,0.9);
        color: black;
        height: 100%;
        border: 1px dotted lightgrey;
        vertical-align: middle;
    }

    .scrollable-menu {
        height: auto;
        max-height: 600px;
        overflow-x: hidden;
    }

    </style>


</head>
<body>



<g:render template="/common/navbar" />

<input type="hidden" id="currentLocationId" value="${session?.warehouse?.id}"/>
<div id='content' class="body">
    <div class="container-fluid">
        <div class="row">
            <div class="col-sm-4 col-md-2 sidebar">

                <g:render template="sidebar"/>

            </div>
            <div class="col-sm-8 col-sm-offset-3 col-md-10 col-md-offset-2 main">

                <g:if test="${flash.message}">
                    <div id="message" class="alert alert-warning">
                        ${flash.message}

                    </div>
                </g:if>

                <div id="body">
                    <g:layoutBody/>
                </div>
            </div>
            <hr>

            <footer>
                <p>
                &copy; OpenBoxes 2017
                </p>
            </footer>

        </div>
    </div>
</div>


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

    $.fn.dataTableExt.oApi.fnProcessingDisplay = function ( oSettings, onoff ) {
        if ( typeof( onoff ) == 'undefined' ) {
            onoff = true;
        }
        this.oApi._fnProcessingDisplay( oSettings, onoff );
    };
</script>
<r:layoutResources/>
</body>
</html>