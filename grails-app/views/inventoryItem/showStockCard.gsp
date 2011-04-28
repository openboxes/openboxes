
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${message(code: 'stockCard.label', default: 'Stock Card')}" />
		<title>
			<g:message code="default.show.label" args="[entityName]" /> &nbsp;&rsaquo;&nbsp; 
			<span style="color: grey">${commandInstance?.productInstance?.name }</span>
		</title>
	</head>
	<body>
		<div class="body">	
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if> 
			
			<g:hasErrors bean="${commandInstance}">
				<div class="errors"><g:renderErrors bean="${commandInstance}" as="list" /></div>
			</g:hasErrors>

			<div class="dialog" style="min-height: 880px">
			
				<!-- Action menu -->
				<div>
					<span class="action-menu">
						<button class="action-btn">
							<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
							<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
						</button>
						<div class="actions">
							<g:render template="showStockCardMenuItems" model="[commandInstance: commandInstance]"/>																				
						</div>
					</span>				
				</div>			
			
			
				<!-- Content -->
				<table>				
					<tr>
					
						<!--  Product Details -->
						<td style="width: 250px;">
							<g:render template="productDetails" 
								model="[productInstance:commandInstance?.productInstance, inventoryInstance:commandInstance?.inventoryInstance, 
									inventoryLevelInstance: commandInstance?.inventoryLevelInstance, totalQuantity: commandInstance?.totalQuantity]"/>
						</td>
						
						<!--  Current Stock and Transaction Log -->
						<td>
							<g:render template="showCurrentStock"/>
							<br/>
							<g:render template="showTransactionLog"/>
							
						</td>
					</tr>
				</table>
			</div>
			<div id="transaction-details" style="height: 200px; overflow: auto;">
				<!-- will be populated by an jquery ajax call -->
			</div>
		</div>
		
		<script>
			$(document).ready(function() {

				// Define dialog
				$("#transaction-details").dialog({ title: "Transaction Details", 
					modal: true, autoOpen: false, width: 800, height: 400, position: 'middle' });    //end dialog									    

				// Click event -> open dialog
				$('.show-details').click(
			        function(event) {
				        //$("#example").load("", [], function() { 
				        //    jQuery("#example").dialog("open");
				        //});
				        //return false
						var link = $(this);
						var dialog = jQuery('#transaction-details').load(link.attr('href')).dialog("open");										        
				        event.preventDefault();
			        }
			    );	
				
				/* Action Menu */

				function show() {
					//$(this).children(".actions").show();
				}
				  
				function hide() { 
					$(this).children(".actions").hide();
				}

				$(".action-btn").click(function() {
					$(this).parent().children(".actions").toggle();
				});
					 
				$(".action-menu").hoverIntent({
					sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
					interval: 50,   // number = milliseconds for onMouseOver polling interval
					over: show,     // function = onMouseOver callback (required)
					timeout: 100,   // number = milliseconds delay before onMouseOut
					out: hide       // function = onMouseOut callback (required)
				});

				//$( ".actions" ).position({ my: "right top", at: "right bottom" });	
				
			});	


			
		</script>		
	</body>
</html>
