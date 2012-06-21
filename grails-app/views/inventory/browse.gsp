<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title>
        	<warehouse:message code="default.browse.label" args="[entityName]" /> 
        	&rsaquo;
        	${commandInstance?.categoryInstance }
        </title>    
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${commandInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${commandInstance}" as="list" />
	            </div>
            </g:hasErrors>            
			<div>
				<!-- Inventory Browser -->
				<div>
		        	<g:set var="varStatus" value="${0}"/>
		        	<g:set var="totalProducts" value="${0}"/> 							        	

					<table>
						<tr>
							<td style="width: 250px;">
			       				<g:render template="filters" model="[commandInstance:commandInstance, quickCategories:quickCategories]"/>						
							</td>
							<td>
								<%-- 
			       				<g:render template="tabs" model="[commandInstance:commandInstance, quickCategories:quickCategories]"/>						
			       				--%>
					         	<table>
									<tr>
						         		<td style="padding: 0; margin: 0; vertical-align: middle;">
								           								            
											<div class="tabs">
												<ul>
													<li>
														<a href="#tabs-1">
															Results for 
															<g:if test="${commandInstance?.searchTerms }">
																<b>${commandInstance.searchTerms }</b>															
															</g:if>
															<g:else>
																<b>all products</b>
															</g:else>
															<g:if test="${commandInstance?.subcategoryInstance }"> 
																in <b><format:category category="${commandInstance?.subcategoryInstance}"/></b>
															</g:if>
															(${commandInstance?.categoryToProductMap?.values()?.flatten()?.size()} products)
															
														</a>
													</li>
												</ul>		
												<div id="tabs-1" style="padding: 0px;">	
										            <form id="inventoryActionForm" name="inventoryActionForm" action="createTransaction" method="POST">
										                <table class="tableScroll" border="0"> 
															<thead> 
									           					<tr>
																	<th class="center middle">
																		<input type="checkbox" id="toggleCheckbox">	
																	</th>
																	<th class="middle">
																		
																	</th>
																	<th class="middle">
																		<warehouse:message code="default.description.label"/>
																	</th>
																	<th class="center middle" style="width: 10%;">
																		<warehouse:message code="product.manufacturer.label"/>
																	</th>
																	<th class="center" style="width: 7%;">
																		<warehouse:message code="inventory.qtyin.label"/>
																	</th>
																	<th class="center" style="width: 7%;">
																		<warehouse:message code="inventory.qtyout.label"/>
																	</th>
																	<th class="center middle" style="width: 7%;">
																		<warehouse:message code="default.qty.label"/>
																	</th>
									           					</tr>
															</thead> 
										                
									            			<g:if test="${commandInstance?.categoryToProductMap}">
																<tbody> 
																	<g:each var="entry" in="${commandInstance?.categoryToProductMap}" status="i">
																		<g:set var="category" value="${entry.key }"/>
																		<g:set var="categoryInventoryItems" value="${commandInstance?.categoryToProductMap[entry.key]}"/>
																		<tr class="">
																			<td colspan="7" style="padding:0; margin:0;">
																				<span class="fade">
																					<h2 style="border-top: 2px solid lightgrey;">
																						<%-- 
																						<g:checkBox id="${category?.id }" name="category.id" 
																							class="checkbox" style="top:0em;" checked="${false }" 
																								value="${category?.id }" />
																						&nbsp;
																						--%>
																						<format:category category="${category }"/>
																						(${categoryInventoryItems.size() })
																						
																					</h2> 
																				</span>
																			
																			</td>
																		</tr>
																		<g:set var="counter" value="${0 }"/>
																		
																		<style>
																			tr.product { }
																			tr.productGroup {  }
																			tr.productGroupProduct { }
																			tr.productGroupProducts { }
																		</style>
																		<g:each var="inventoryItem" in="${categoryInventoryItems}" status="status">																			
																			<g:if test="${inventoryItem.product }">
																				<g:render template="browseProduct" model="[counter:counter,inventoryItem:inventoryItem,cssClass:'product']"/>
																			</g:if>
																			<g:elseif test="${inventoryItem.productGroup }">
																				<g:render template="browseProductGroup" model="[counter:counter,inventoryItem:inventoryItem,cssClass:'productGroup']"/>
																			</g:elseif>
																			<g:set var="counter" value="${counter+1 }"/>
																			
																		</g:each>
																	</g:each>
																</tbody>
															</g:if>	    
															<g:else>
																<tbody>
																	<tr>
																		<td colspan="7" class="middle center">
																			<div style="height: 200px">
																				<warehouse:message code="inventory.searchNoMatch.message" args="[commandInstance?.searchTerms?:'',format.metadata(obj:commandInstance?.categoryInstance)]"/>
																			</div>
																		</td>
																	</tr>
																</tbody>
															</g:else>
															<tfoot>
																<tr>
																	<td colspan="2" class="left middle">
																		<g:render template="./actions" model="[]"/>
																	</td>			
																	<td colspan="2" class="middle ">
																	</td>
																	<td colspan="3" class="right middle">
																		<%-- 
																		<warehouse:message code="inventory.showingProductsInCategories.label" args="[totalProducts,commandInstance?.categoryToProductMap?.keySet()?.size()]" />
																		(<g:each var="category" in="${commandInstance?.categoryToProductMap?.keySet()}">
																			<g:link controller="inventory" action="browse" params="['categoryId':category.id]">
																				<format:metadata obj="${category}"/>&nbsp;
																			</g:link>
																		</g:each>)
																		--%>
																	</td>
																</tr>
															</tfoot>
														</table>
													</form>		
												</div>							
											</div>
						         		</td>
						         	</tr>
						        </table>
								<div style="padding: 5px" class="right">
									
									<label>Key:</label>
									<img src="${createLinkTo(dir:'images/icons/silk',file:'flag_green.png')}" alt="${warehouse.message(code: 'inventory.markAsSupported.label') }" style="vertical-align: middle"/>
									&nbsp;<warehouse:message code="enum.InventoryStatus.SUPPORTED"/>
									&nbsp;
									<img src="${createLinkTo(dir:'images/icons/silk',file:'flag_orange.png')}" alt="${warehouse.message(code: 'inventory.markAsNonInventoried.label') }" style="vertical-align: middle"/>
									&nbsp;<warehouse:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/>
									&nbsp;
									<img src="${createLinkTo(dir:'images/icons/silk',file:'flag_red.png')}" alt="${warehouse.message(code: 'inventory.markAsNotSupported.label') }" style="vertical-align: middle"/>
									&nbsp;<warehouse:message code="enum.InventoryStatus.NOT_SUPPORTED"/>																	
								</div>
							</td>
						</tr>
					</table>						        
				</div>
			</div>
		</div>
		
		
		<script>
			$(document).ready(function() {
				$('.tableScroll').tableScroll({height: 400, width: '99%'});
				
				$(".checkable a").click(function(event) {
					event.stopPropagation();
				});
				$('.checkable').toggle(
					function(event) {
						$(this).parent().find('input').click();
						//$(this).parent().addClass('checked');
						return false;
					},
					function(event) {
						$(this).parent().find('input').click();
						//$(this).parent().removeClass('checked');
						return false;
					}
				);

				
				//$(".megamenu").megamenu();
				
				$("#toggleCheckbox").click(function(event) {
					$(".checkbox").attr("checked", $(this).attr("checked"));
				});			
				
		    	$(".tabs").tabs(
	    			{
	    				cookie: {
	    					// store cookie for a day, without, it would be a session cookie
	    					expires: 1
	    				}
	    			}
				); 


		    	$(".expandable").click(function(event) {
					
		    		$("#productGroupProducts-"+event.target.id).toggle();
					
		    	});
				$(".collapsable").click(function(event) { 

		    		$("#productGroupProducts-"+event.target.id).toggle();
				});				
			});	
		</script>	
    </body>
</html>
