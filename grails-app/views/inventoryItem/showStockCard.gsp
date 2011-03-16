
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
	
			<div class="nav">
				<g:render template="../inventory/nav"/>
			</div>
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
								model="[productInstance:commandInstance?.productInstance, inventoryInstance:commandInstance?.inventoryInstance, inventoryLevelInstance: commandInstance?.inventoryLevelInstance, totalQuantity: commandInstance?.totalQuantity]"/>
								
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
	</body>
</html>
