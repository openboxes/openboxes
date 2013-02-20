
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        <title><warehouse:message code="default.view.label" args="[entityName.toLowerCase()]" /></title>    
        <style>
        /*
        	optgroup { font-weight: bold; } 
        	#transactionEntryTable { border: 1px solid #ccc; } 
			#transactionEntryTable td { padding: 5px; text-align: center; }
			#transactionEntryTable th { text-align: center; } 
        	#prodSelectRow { padding: 10px; }  
        	#transactionEntryTable td.prodNameCell { text-align: left; } 
			.dialog form label { position: absolute; display: inline; width: 140px; text-align: right;}
        	.dialog form .value { margin-left: 160px; }
        	.dialog form ul li { padding: 10px; } 
        	.dialog form { width: 100%; } 
        	.header th { background-color: #525D76; color: white; }         	
        */
        </style>
    </head>    

    <body>
        <div class="body">
     
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    

			<div class="dialog">
				<g:render template="../transaction/summary"/>
				<div class="yui-gf">
					<div class="yui-u first">
						<g:render template="../transaction/details" model="[transactionInstance:transactionInstance]"/>
					</div>
					<div class="yui-u">									
						<g:if test="${transactionInstance?.id }">
							<div class="box">
								<table id="prodEntryTable">
									<thead>
										<tr class="prop">
											<th></th>
											<th><warehouse:message code="product.label"/></th>
											<th style="text-align: center"><warehouse:message code="product.lotNumber.label"/></th>
											<th style="text-align: center"><warehouse:message code="product.expirationDate.label"/></th>
											<th style="text-align: center"><warehouse:message code="default.qty.label"/></th>
										</tr>
									</thead>
									<tbody>
										<g:set var="transactionSum" value="${0 }"/>
										<g:set var="transactionCount" value="${0 }"/>
										<g:if test="${transactionInstance?.transactionEntries }">
											<g:each in="${transactionInstance?.transactionEntries.sort { it?.inventoryItem?.product?.name } }" var="transactionEntry" status="status">														
												<g:if test="${params?.showAll || !params?.product || transactionEntry?.inventoryItem?.product?.id == params?.product?.id}">
													<g:set var="transactionSum" value="${transactionSum + transactionEntry?.quantity}"/>
													<g:set var="transactionCount" value="${transactionCount+1 }"/>
													<tr class="${status%2?'odd':'even' }">
														<td>
															<g:link controller="transactionEntry" action="edit" id="${transactionEntry?.id}">
																edit
															</g:link>
														
														</td>
														<td style="text-align: left;">
															<%-- 
															<g:if test="${params?.showAll || !params.product }">		
																<g:link controller="inventory" action="showTransaction" id="${transactionInstance.id}" params="['product.id':transactionEntry?.inventoryItem?.product?.id]">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" alt="${warehouse.message(code: 'transaction.showSingleProduct.label') }" style="vertical-align: middle"/>
																</g:link>
															</g:if>
															<g:else>
																<g:if test="${transactionInstance?.transactionEntries?.size() > transactionCount || params?.product?.id }">
																	<a href="?showAll=true">																			
																		<img src="${createLinkTo(dir:'images/icons/silk',file:'decline.png')}" alt="${warehouse.message(code: 'transaction.showAllProducts.label') }" style="vertical-align: middle"/>
																	</a>
																</g:if>														
															</g:else>
															--%>																
															<g:link controller="inventoryItem" action="showStockCard" params="['product.id':transactionEntry?.inventoryItem?.product?.id]">
																<format:product product="${transactionEntry?.inventoryItem?.product}"/>																		
															</g:link>
														</td>										
														<td class="center">
															${transactionEntry?.inventoryItem?.lotNumber }
														</td>		
														<td class="center">
															<format:expirationDate obj="${transactionEntry?.inventoryItem?.expirationDate}"/>															
														</td>
														<td class="center">
															${transactionEntry?.quantity}
														</td>
													</tr>
												</g:if>
											</g:each>
											<g:if test="${transactionInstance?.transactionEntries?.size() > transactionCount || params?.product?.id }">
												<tr>
													<td>
														<a href="?showAll=true">show all</a>
													</td>
												</tr>
											</g:if>
											
										</g:if>
										<g:else>
											<tr>
												<td colspan="4">
													<warehouse:message code="transaction.noEntries.message"/>
												</td>
											</tr>
										</g:else>
									</tbody>
									<%-- 
									<tfoot>
										<tr>
											<td colspan="2">
											
											</td>
											<td class="right">
												<label>Total</label>																
											</td>															
											<td class="center">
												${transactionSum }																			
											</td>			
										</tr>
									</tfoot>
									--%>
								</table>	
							</div>
						</g:if>		
					</div>		
				</div>
			</div>
		</div>
		
    </body>
</html>
