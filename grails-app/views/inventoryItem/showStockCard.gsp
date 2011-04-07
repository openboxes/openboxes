
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${message(code: 'stockCard.label', default: 'Stock Card')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
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
				<div class="actionsMenu" style="float: left;">					
					<ul>
						<li>
							<g:link controller="inventory" action="browse" >
								<button>		
									<img src="${resource(dir: 'images/icons/silk', file: 'arrow_left.png')}" style="vertical-align: middle;"/>
									&nbsp;<span style="vertical-align: middle;">Back to <b>Inventory</b></span>
								</button>
							</g:link>
						</li>
					</ul>
				</div>	
				<div class="actionsMenu" style="float: right;">					
					<ul>
						<li>
							<g:link controller="inventoryItem" action="showRecordInventory" params="['product.id':commandInstance?.productInstance?.id,'inventory.id':commandInstance?.inventoryInstance?.id]">
								<button class="">
									<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
									&nbsp;
									<span style="vertical-align: middle;">Record inventory</span>
								</button>
							</g:link>
						</li>
						<li>
							<g:link class="new button" controller="inventory" action="createTransaction">
								<button class="">
									<img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" style="vertical-align: middle"/>
									<span style="vertical-align: middle;">&nbsp;Add new transaction</span>
								</button>
							</g:link>
						</li>	
					</ul>					
				</div>				
				<br clear="all">
								
				<table>
					<tr>
						<td style="width: 300px;">
							<g:render template="productDetails" 
								model="[productInstance:commandInstance?.productInstance, inventoryInstance:commandInstance?.inventoryInstance, 
									inventoryLevelInstance: commandInstance?.inventoryLevelInstance, totalQuantity: commandInstance?.totalQuantity]"/>
								
							<br/>
								
							<g:render template="showTransactionLog"/>
								
						</td>
						<td>			
							<g:render template="showCurrentStock"/>
						</td>
					</tr>
				</table>
			</div>
			<div id="transaction-details" style="height: 200px; overflow: auto;">
			<!-- will be populated by an jquery ajax  -->
			</div>
		</div>
		
		<script>
			jQuery(document).ready(function() {
				jQuery(".toggleDetails").click(function(event) {
					//event.preventDefault();
				});
				/*
				jQuery(".toggleDetails").mouseover(function(event) {
					jQuery("#transactionEntries" + this.id).toggle('slow');								
				});
				jQuery(".toggleDetails").mouseout(function(event) {
					jQuery("#transactionEntries" + this.id).toggle('slow');								
				});
				*/

				jQuery(".toggleDetails").hoverIntent({
					over: function(event) {
						jQuery("#transactionEntries" + this.id).slideDown('fast');													
					},
					timeout: 500,
					out: function(event) {
						jQuery("#transactionEntries" + this.id).slideUp('fast');								
					}
				});	

				// Define dialog
				jQuery("#transaction-details").dialog({ title: "Transaction Details", 
					modal: true, autoOpen: false, width: 800, height: 400, position: 'middle' });    //end dialog									    

				// Click event -> open dialog
				jQuery('.show-details').click(
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

				// Click event -> open dialog
				jQuery('#show-filters').click(function(event) {
					jQuery("#filters").toggle();
					event.preventDefault();
				});		


				/* Action Menu */

				function show() {
					jQuery(this).children(".actions").show();
				}
				  
				function hide() { 
					jQuery(this).children(".actions").hide();
				}
					 
				jQuery(".action-menu").hoverIntent({
					sensitivity: 1, // number = sensitivity threshold (must be 1 or higher)
					interval: 50,   // number = milliseconds for onMouseOver polling interval
					over: show,     // function = onMouseOver callback (required)
					timeout: 100,   // number = milliseconds delay before onMouseOut
					out: hide       // function = onMouseOut callback (required)
				});

				
				
			});	


			
		</script>		
	</body>
</html>
