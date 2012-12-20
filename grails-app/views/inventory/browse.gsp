<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title>
        	Browse inventory
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
					         	<table>
									<tr>
						         		<td style="padding: 0; margin: 0; vertical-align: middle;">
											<div class="tabs">
												<ul>
													<li>
														<a href="#tabs-1">
															<format:category category="${commandInstance?.categoryInstance}"/>
															<g:if test="${commandInstance?.subcategoryInstance && commandInstance?.subcategoryInstance != commandInstance?.categoryInstance}"> 
																&nbsp;&rsaquo;&nbsp;
																<format:category category="${commandInstance?.subcategoryInstance}"/>
															</g:if>
															&nbsp;&rsaquo;&nbsp;
															<g:if test="${commandInstance?.searchTerms }">
																${commandInstance.searchTerms }
                                                                (${commandInstance?.categoryToProductMap?.values()?.flatten()?.size()} of ${numProducts} products)
                                                            </g:if>
															<g:else>
																${warehouse.message(code: 'products.all.label') }
															</g:else>
														</a>
													</li>
												</ul>		
												<div id="tabs-1" style="padding: 0px;">	
										            <form id="inventoryActionForm" name="inventoryActionForm" action="createTransaction" method="POST">
										                <table class="tableScroll" border="0"> 
															<thead> 
									           					<tr>
																	<th class="center middle" style="width: 1%">
																		<input type="checkbox" id="toggleCheckbox">	
																	</th>
																	<th class="middle" style="width: 1%">
																		
																	</th>
																	<th class="middle">
																		<warehouse:message code="product.name.label"/>
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
																		<td colspan="7" class="even center">
																			<div class="fade padded">
																				<g:if test="${params.searchPerformed }">
																					<warehouse:message code="inventory.searchNoMatch.message" args="[commandInstance?.searchTerms?:'',format.metadata(obj:commandInstance?.categoryInstance)]"/>
																				</g:if>
																			</div>
																		</td>
																	</tr>
																</tbody>
															</g:else>
															<tfoot>
																<tr>
																	<td colspan="3" class="left middle">
																		<g:render template="./actions" model="[]"/>
																	</td>			
																	<td colspan="1" class="middle ">
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
																		<div class="paginateButtons">
                                                                        <g:paginate total="${numProducts}" params="${params}" action="browse" max="${25}" />
                                                                        </div>
													</form>		
												</div>							
											</div>
						         		</td>
						         	</tr>
						        </table>
							</td>
						</tr>
					</table>						        
				</div>
			</div>
		</div>
		
		
		<script>
			$(document).ready(function() {
				//$('.tableScroll').tableScroll({height: 400, width: '99%'});

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
                    var checked = ($(this).attr("checked") == 'checked');
		            $(".checkbox").attr("checked", checked);
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
