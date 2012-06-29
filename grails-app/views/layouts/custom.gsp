<?xml version="1.0" encoding="UTF-8"?>
<html>
<head>
	<!-- Include default page title -->
	<title><g:layoutTitle default="OpenBoxes" /></title>
	
	<%--<link rel="stylesheet" href="http://yui.yahooapis.com/2.7.0/build/reset-fonts-grids/reset-fonts-grids.css" type="text/css"> --%>
	<link rel="stylesheet" href="${createLinkTo(dir:'js/yui/2.7.0/reset-fonts-grids',file:'reset-fonts-grids.css')}" type="text/css"/>
	
	<!-- Include Favicon -->
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
	
	<!-- Include Main CSS -->
	<!-- TODO Apparently there's a slight distinction between these two ... need to figure out what that distinction is -->
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'menu.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'form.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footer.css')}" type="text/css" media="screen, projection" />	
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'wizard.css')}" type="text/css" media="screen, projection" />
	
	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.megaMenu/',file:'jquery.megamenu.css')}" type="text/css" media="screen, projection" />

	<!-- Include javascript files -->
	<g:javascript library="application"/>

	<!-- Include jQuery UI files -->
	<g:javascript library="jquery" plugin="jquery" />
	<jqui:resources />
	<link href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<%-- 
	<jqui:resources />
	<jqui:resources theme="smoothness" />
	--%><%--  
	<link href="${createLinkTo(dir:'js/jquery.ui/css/smoothness', file:'jquery-ui.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery-ui-1.8.7.js')}" type="text/javascript" ></script>
	--%>
	<!-- Include other plugins -->
	<script src="${createLinkTo(dir:'js/jquery.ui/js/', file:'jquery.ui.autocomplete.selectFirst.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.cookies/', file:'jquery.cookies.2.2.0.min.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.cookie/', file:'jquery.cookie.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.tmpl/', file:'jquery.tmpl.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.tmplPlus/', file:'jquery.tmplPlus.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.livequery/', file:'jquery.livequery.min.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.livesearch/', file:'jquery.livesearch.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.hoverIntent/', file:'jquery.hoverIntent.minified.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.tableScroll/', file:'jquery.tablescroll.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.watermark/', file:'jquery.watermark.min.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.periodicalupdater/', file:'jquery.periodicalupdater.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.flot/', file:'jquery.flot.js')}" type="text/javascript"></script>
	<script src="${createLinkTo(dir:'js/', file:'global.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.megaMenu/', file:'jquery.megamenu.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.expand/', file:'expand.js')}" type="text/javascript" ></script>
	
	<!-- File upload 
	<script src="${createLinkTo(dir:'js/jquery.fileupload/', file:'jquery.fileupload.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.fileupload/', file:'jquery.iframe-transport.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.fileupload/vendor/', file:'jquery.ui.widget.js')}" type="text/javascript" ></script>
	-->
		
	
 	<!-- Include Jquery Validation and Jquery Validation UI plugins -->
 	<jqval:resources />       
    <jqvalui:resources />


	<%--
	<link href="${createLinkTo(dir:'js/jquery.jqGrid/css', file:'ui.jqgrid.css')}" type="text/css" rel="stylesheet" media="screen, projection" />
	<script src="${createLinkTo(dir:'js/jquery.jqGrid/js', file:'jquery.jqGrid.min.js')}" type="text/javascript" ></script>
	 --%>
	<%--
    <script type="text/javascript" src="${createLinkTo(dir:'js/jquery/', file:'fg.menu.js')}"></script>
    <link type="text/css" href="${createLinkTo(dir:'js/jquery/', file:'fg.menu.css')}" media="screen" rel="stylesheet" />	
	--%>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'custom.css')}" type="text/css" media="screen, projection" />
	
	<!-- Custom styles to be applied to all pages -->
	<style type="text/css" media="screen"></style>
	
	<!-- Grails Layout : write head element for page-->
	<g:layoutHead />

	<g:render template="/common/customCss"/>
	
	
</head>
<body class="yui-skin-sam">
	<g:render template="/common/customVariables"/>
	
	<%-- 
	
	<g:if test="${flash.message}">	
		<div id="notify-container" style="display: hidden;">
			<div id="notify-message" class="message">${flash.message}</div>	
		</div>
	</g:if>
 	--%>
 	
	<!-- Header "hd" includes includes logo, global navigation -->
	<div id="hd" role="banner">
	    <g:render template="/common/header"/>		    
	</div>
	<g:if test="${session?.user && session?.warehouse}">
		<div id="megamenu" class="left">    
			<g:include controller="dashboard" action="megamenu"/>		    
		</div>
	</g:if>	    
  	<g:if test="${session.user}">
 		<div class="breadcrumb">
 		
			<g:link controller="dashboard" action="index">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'house.png')}" class="middle"/>
			</g:link>
			&nbsp;
			&rsaquo; 		
			&nbsp;
   			<g:if test="${session?.user && session?.warehouse}">
	    		<h1 style="display:inline" class="middle">${g.pageProperty(name: 'page.label2') ?: g.layoutTitle()}</h1>	
	    	</g:if>
	    	<%-- 				    
		    	<g:link controller="dashboard" action="index">
			    	<img src="${createLinkTo(dir: 'images/icons/silk', file: 'house.png')}" style="vertical-align: bottom;"/>
		    	</g:link>
			    &nbsp;&rsaquo;&nbsp;								
				<g:if test="${session?.warehouse}">									
					<g:if test="${session.warehouse.logo }">
						<img class="photo" width="25" height="25" 
							src="${createLink(controller:'location', action:'viewLogo', id:session.warehouse.id)}" style="vertical-align: middle" />
					</g:if>
					${session?.warehouse?.name} &nbsp;&rsaquo;&nbsp;
				</g:if> 
				<!--  note that both breadcrumbs are overrideable by using the content tag is the view to set the value of label1 or label2 -->
			    <g:set var="label1">${g.pageProperty(name: 'page.label1') ?: warehouse.message(code: "breadcrumbs." + params.controller + ".label")}</g:set>
			    <g:set var="label2">${g.pageProperty(name: 'page.label2') ?: g.layoutTitle()}</g:set>
			   		${label1 ?: params.controller}
			    <g:if test="${label1 != label2}">
					&nbsp;&rsaquo;&nbsp;								
	    			${label2}
	    		</g:if>
    		--%>
   		</div>
  	</g:if>
	
	<!-- Body includes the divs for the main body content and left navigation menu -->
		
	<div id="bd" role="main">
	    <div id="doc3"><!--class="yui-t3"-->		    	
	      	<div id="yui-main">
		    	<div id="content" class="yui-b">
					<g:layoutBody />
				</div>
	      	</div>
	      		      	
	      	<!-- YUI nav block that includes the local navigation menu -->
	      	<%-- 
	      	<div id="menu" class="yui-b">
		  		<g:if test="${session?.user && session?.warehouse}">
					<g:include controller="dashboard" action="menu"/>
				</g:if>
			</div>
			--%>			 
		</div>
	</div>

	<!-- YUI "footer" block that includes footer information -->
	<div id="ft" role="contentinfo">
		<g:render template="/common/footer" />
	</div>
	<script type="text/javascript">

		$(function() { 		
		
			$(".megamenu").megamenu({'show_method':'simple', 'hide_method': 'simple'});
		
			<g:if test="${session.useDebugLocale}">
				$('.copy').click(function(event) {				
					var copyText = $(this).siblings('.text').text();				
					alert(copyText);
					event.preventDefault();
				});
			</g:if>
			
			<g:if test="${session.user && session.warehouse}">
				var handler = $.PeriodicalUpdater('/warehouse/dashboard/status', 
					{ 
						method: 'get', // method; get or post 
						data: '', // array of values to be passed to the page - e.g. {name: "John", greeting: "hello"} 
						minTimeout: 5000, // starting value for the timeout in milliseconds 
						maxTimeout: 60000, // maximum length of time between requests 
						multiplier: 2, // the amount to expand the timeout by if the response hasn't changed (up to maxTimeout) 
						type: 'json', // response type - text, xml, json, etc. See $.ajax config options 
						maxCalls: 10, // maximum number of calls. 0 = no limit. 
						autoStop: 0 // automatically stop requests after this many returns of the same data. 0 = disabled. 
					}, 
					function(remoteData, success, xhr, handle) { 
						if (remoteData != '') {
							for (var i = 0; i < remoteData.length; i++) {
								$('#status').text(remoteData[i].comment);
							}
							$('#status').addClass("notice");						
						}
					}
				);
			</g:if>
			$("#warehouse-switch").click(function() {
				//$("#warehouse-menu").toggle();
				$("#warehouseMenu").dialog({ 
					autoOpen: true, 
					modal: true, 
					width: '500'
				});
			});

			function showActions() {
				//$(this).children(".actions").show();
			}
			
			function hideActions() { 
				$(this).children(".actions").hide();
			}

			/* This is used to remove the action menu when the */
			$(".action-menu").hoverIntent({
				sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
				interval: 5,   // number = milliseconds for onMouseOver polling interval
				over: showActions,     // function = onMouseOver callback (required)
				timeout: 100,   // number = milliseconds delay before onMouseOut
				out: hideActions       // function = onMouseOut callback (required)
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
			$(".action-btn").click(function(event) {
				//show the menu directly over the placeholder
				var actions = $(this).parent().children(".actions");

				// Need to toggle before setting the position 
				actions.toggle();

				// Set the position for the actions menu
			    actions.position({
					my: "left top",
					at: "left bottom",				  
					of: $(this).closest(".action-btn"),
					//offset: "0 0"
					collision: "flip"
				});
				
				// To prevent the action button from POST'ing to the server
				event.preventDefault();
			});
			
			var accordion = 
				$('.accordion').accordion({
					active: true, 
					navigation: true, 
					autoHeight: false, 
					//alwaysOpen: true,
					clearStyle: true, 
					//collapsible: false,
					//fillSpace: true,
					event: "click"  
				});			

			<g:if test="${request.request.requestURL.toString().contains('category')}">
				accordion.accordion( "activate" , 6 );
			</g:if>
			<g:elseif test="${request.request.requestURL.toString().contains('locationGroup')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('locationType')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('Transaction')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('attribute')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('product/create')}">
				accordion.accordion( "activate" , 0 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('person')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('user')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('location')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('warehouse/warehouse')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>
			<g:elseif test="${(request.request.requestURL.toString().contains('shipment') && request.request.queryString?.contains('incoming'))}" >
				accordion.accordion( "activate" , 4 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('shipment')}">
				accordion.accordion( "activate" , 3 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('createShipmentWorkflow')}">
				accordion.accordion( "activate" , 3 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('createRequestWorkflow')}">
				accordion.accordion( "activate" , 2 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('fulfillRequestWorkflow')}">
				accordion.accordion( "activate" , 2 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('request')}">
				accordion.accordion( "activate" , 2 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('purchaseOrderWorkflow')}">
				accordion.accordion( "activate" , 1 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('order')}">
				accordion.accordion( "activate" , 1 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('inventory/list')}">
				accordion.accordion( "activate" , 0 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('inventory/show')}">
				accordion.accordion( "activate" , 0 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('inventory')}">
				accordion.accordion( "activate" , 0 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('product')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>			
			<g:elseif test="${request.request.requestURL.toString().contains('batch')}">
				accordion.accordion( "activate" , 6 );
			</g:elseif>			
		
			$('.goto').click(function(){
				var li = $(this).parent().closest(".menu-section");
			});	
		});
	</script>
    <script type="text/javascript">
		var monthNamesShort = [];
		<g:each in="${1..12}" var="monthNum">
			monthNamesShort[${monthNum-1}] = '<warehouse:message code="month.short.${monthNum}.label"/>';
		</g:each>
    </script>    
	<script type="text/javascript">
	  var uvOptions = {};
	  (function() {
	    var uv = document.createElement('script'); uv.type = 'text/javascript'; uv.async = true;
	    uv.src = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'widget.uservoice.com/gMxKSy5iKCBPkbBzs8Q.js';
	    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(uv, s);
	  })();
	</script>    
	
</body>
</html>
