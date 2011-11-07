<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />        
        <title><warehouse:message code="inventory.expiredStock.label"/></title>    
    </head>    

	<body>
		<div class="body">
       		
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<table>
				<tr>
            		<td style="border: 1px solid lightgrey; background-color: #f5f5f5;">
			            <g:form action="listExpiredStock" method="get">
			            	<table id="expiredStockTable">
			            		<tr>
			            			<th><warehouse:message code="category.label"/></th>
			            			
			            		</tr>
			            		<tr>
						           	<td class="filter-list-item">
						           		<g:select name="category"
														from="${categories}"
														optionKey="id" optionValue="${{format.category(category:it)}}" value="${categorySelected?.id}" 
														noSelection="['': warehouse.message(code:'default.all.label')]" />   
									</td>
									<%-- 
									<td>
						           		<g:select name="threshhold"
														from="['1': warehouse.message(code:'default.week.oneWeek.label'), '14': warehouse.message(code:'default.week.twoWeeks.label'), 
															   '30': warehouse.message(code:'default.month.oneMonth.label'), '60': warehouse.message(code:'default.month.twoMonths.label'), 
															   '90': warehouse.message(code:'default.month.threeMonths.label'), '180': warehouse.message(code:'default.month.sixMonths.label'), 
															   '365': warehouse.message(code:'default.year.oneYear.label')]"
														optionKey="key" optionValue="value" value="${threshholdSelected}" 
														noSelection="['': warehouse.message(code:'default.all.label')]" />   
						           	</td>
						           	--%>
									<td class="filter-list-item" style="height: 100%; vertical-align: bottom">
										<button name="filter">
											<img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}"/>&nbsp;<warehouse:message code="default.button.filter.label"/> </button>
									</td>							           	
								</tr>
							</table>
			            </g:form>
            		</td>
            	</tr>
			</table>			
				
			<table>
				<tr>					
					<td style="padding: 0px">
						<div class="">
				            <form id="inventoryActionForm" name="inventoryActionForm" method="POST">
								<table class="tableScroll">
				                    <thead>
				                        <tr class="odd">   
				                        	<th class="center" style="width: 50px; text-align: center;">
				                        		<input type="checkbox" id="toggleCheckbox"/>
				                        	</th>
											<th style="width: 600px;"><warehouse:message code="item.label"/></th>
											<th style="width: 200px;"><warehouse:message code="inventory.lotNumber.label"/></th>
											<th style="width: 150px;"><warehouse:message code="inventory.expires.label"/></th>
											<th style="width: 100px;"><warehouse:message code="default.qty.label"/></th>
				                        </tr>
				                    </thead>
				       	           	<tbody>			
				       	     			<g:set var="counter" value="${0 }" />
				       	     			<g:set var="anyExpiredStock" value="${false }"/>
				       	     			
										<g:each var="inventoryItem" in="${inventoryItems}" status="i">           
											<g:set var="quantity" value="${quantityMap[inventoryItem] }"/>
											<g:if test="${quantity > 0 }">
												<g:set var="anyExpiredStock" value="${true }"/>
												<tr class="${(counter++ % 2) == 0 ? 'even' : 'odd'}">            
													<td class="center" style="width: 50px;">
														<g:checkBox id="${inventoryItem?.product?.id }" name="product.id" 
															class="checkbox" style="top:0em;" checked="${false }" 
																value="${inventoryItem?.product?.id }" />
													
													</td>
													<td class="checkable" style="width: 600px;">
														<g:link controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]">
															<format:product product="${inventoryItem?.product}"/> 
															<span class="fade"><format:category category="${inventoryItem?.product?.category}"/> </span>
														</g:link>
														
													</td>
													<td class="checkable" style="width: 250px;">
														${inventoryItem?.lotNumber }
													</td>
													<td class="checkable" style="width: 150px;">
														<g:formatDate date="${inventoryItem?.expirationDate}" format="MMM yyyy"/>
														<%-- 
														${prettyDateFormat(date: inventoryItem.expirationDate)}
														--%>
														
													</td>
													<td class="center checkable" style="width: 100px;">
														${quantity }
													</td>									
												</tr>						
											</g:if>		
										</g:each>
										<g:if test="${!anyExpiredStock }">
											<tr>
												<td colspan="5">
													<div class="padded center fade">
														<warehouse:message code="inventory.noExpiredStock.label" />
													</div>
												</td>
											</tr>
										</g:if>
									</tbody>
									<tfoot>
										<tr style="border-top: 1px solid lightgrey">
											<td colspan="5">
												<div>
													<g:render template="./actionsExpiredStock" />									
												</div>
											</td>
										</tr>									
									</tfoot>
								</table>		
							</form>		
						</div>
					</td>
				</tr>			
			</table>
		</div>
		<script>
			$(document).ready(function() {

				$('.tableScroll').tableScroll({height:400});
				
				$(".checkable a").click(function(event) {
					event.stopPropagation();
				});
				$('.checkable').toggle(
					function(event) {
						$(this).parent().find('input').click();
						$(this).parent().addClass('checked');
						return false;
					},
					function(event) {
						$(this).parent().find('input').click();
						$(this).parent().removeClass('checked');
						return false;
					}
				);
				
				$("#toggleCheckbox").click(function(event) {
					$(".checkbox").attr("checked", $(this).attr("checked"));
				});	
			});	
		</script>	
	</body>
</html>
