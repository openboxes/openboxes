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
	<%--<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />--%>
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'menu.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'form.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'footer.css')}" type="text/css" media="screen, projection" />	
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'wizard.css')}" type="text/css" media="screen, projection" />
	<%-- 
	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tableScroll/',file:'jquery.tablescroll.css')}" type="text/css" media="screen, projection" />
	--%>
	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.megaMenu/',file:'jquery.megamenu.css')}" type="text/css" media="screen, projection" />
	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.megaMenu/',file:'example.css')}" type="text/css" media="screen, projection" />

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
	<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.cookies.2.2.0.min.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.tmpl.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.tmplPlus.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.livequery.min.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.hoverIntent.minified.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.tableScroll/', file:'jquery.tablescroll.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.watermark/', file:'jquery.watermark.min.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/', file:'global.js')}" type="text/javascript" ></script>
	<script src="${createLinkTo(dir:'js/jquery.megaMenu/', file:'jquery.megamenu.js')}" type="text/javascript" ></script>

	
 	<!-- Include Jquery Validation and Jquery Validation UI plugins -->
 	<jqval:resources />       
    <jqvalui:resources />

    <script type="text/javascript">
		var monthNamesShort = [];
		<g:each in="${1..12}" var="monthNum">
			monthNamesShort[${monthNum-1}] = '<warehouse:message code="month.short.${monthNum}.label"/>';
		</g:each>
    </script>

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
	<div class="notification-container"></div>
	
	<g:if test="${flash.message}">	
		<div id="notify-container" style="display: hidden;">
			<div id="notify-message" class="message">${flash.message}</div>	
		</div>
	</g:if>
 	--%>
 	
	<!-- Header "hd" includes includes logo, global navigation -->
	<div id="hd" role="banner">
	    <g:render template="/common/header"/>		    
	    <g:render template="/common/megaMenu"/>
	</div>
	<!-- Body includes the divs for the main body content and left navigation menu -->
		
	<div id="bd" role="main">
	    <div id="doc2" >		    	
	      	<div id="yui-main">
		    	<div id="content" class="yui-b">
		    		<g:if test="${session.user}">
		    			<h3 class="page-title">
						    <div>							    
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
			    			</div>
		    			</h3>
		    		</g:if>
					<g:layoutBody />
				</div>
	      	</div>
	      		      	
	      	 
		</div>
	</div>

	<!-- YUI "footer" block that includes footer information -->
	<div id="ft" role="contentinfo">
		<g:render template="/common/footer" />
	</div>
	<script type="text/javascript">
		$(function() { 

			$(".megamenu").megamenu();
			
			$("#warehouse-switch").click(function() {
				//$("#warehouse-menu").toggle();
				$("#warehouseMenu").dialog({ 
					autoOpen: true, 
					modal: true, 
					width: '400px'
				});
			});
			
			function show() {
				//$(this).children(".actions").show();
			}
			
			function hide() { 
				$(this).children(".actions").hide();
			}
			
			$(".action-menu").hoverIntent({
				sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
				interval: 5,   // number = milliseconds for onMouseOver polling interval
				over: show,     // function = onMouseOver callback (required)
				timeout: 100,   // number = milliseconds delay before onMouseOut
				out: hide       // function = onMouseOut callback (required)
			});  
			
			// Create an action button that toggles the action menu on click
			//button({ text: false, icons: {primary:'ui-icon-gear',secondary:'ui-icon-triangle-1-s'} }).
			$(".action-btn").click(function(event) {
				$(this).parent().children(".actions").toggle();
				event.preventDefault();
			});
	
			
			var accordion = 
				$('#leftnav-accordion-menu').accordion({
					active: true, 
					navigation: true, 
					autoHeight: false, 
					//alwaysOpen: true,
					//clearStyle: false, 
					//collapsible: false,
					//fillSpace: true,
					event: "click"  
				});
			
			/*
			$('.accordion h6').click(function() {
				$(this).next().toggle();
				return false;
			}).next().hide();
			*/
			<g:if test="${request.request.requestURL.toString().contains('category')}">
				accordion.accordion( "activate" , 5 );
			</g:if>
			<g:elseif test="${request.request.requestURL.toString().contains('locationGroup')}">
				accordion.accordion( "activate" , 5 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('locationType')}">
				accordion.accordion( "activate" , 5 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('Transaction')}">
				accordion.accordion( "activate" , 5 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('attribute')}">
				accordion.accordion( "activate" , 5 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('product/create')}">
				accordion.accordion( "activate" , 0 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('person')}">
				accordion.accordion( "activate" , 5 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('user')}">
				accordion.accordion( "activate" , 5 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('location')}">
				accordion.accordion( "activate" , 5 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('warehouse/warehouse')}">
				accordion.accordion( "activate" , 5 );
			</g:elseif>
			<g:elseif test="${request.request.requestURL.toString().contains('shipment') && request.request.queryString?.contains('incoming')}">
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
		
			$('.goto').click(function(){
				var li = $(this).parent().closest(".menu-section");
			});	
		});
	</script>
</body>
</html>
