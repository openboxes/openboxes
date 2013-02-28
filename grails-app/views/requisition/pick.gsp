<%@ page import="org.pih.warehouse.requisition.Requisition"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript"></script>
<title><warehouse:message code="requisition.process.label" /></title>
<style>
	tr.selected { background-color: #E5ECF9; }
	tr * { background: transparent; }
</style>

</head>
<body>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		
		<g:render template="summary" model="[requisition:requisition]"/>
	
		<div class="yui-gc">
			<div class="yui-u first">
				<div id="picklist" class="left ui-validation">
					<g:form name="requisitionForm" method="post" action="save"
						controller="picklist">
						<div class="dialog">
						
							<%-- 
							<g:link controller="requisitionItem" action="substitute" class="button">Substitute</g:link>
							<g:link controller="requisitionItem" action="cancel" class="button">Cancel remaining</g:link>
							--%>
							<div>
											
								<table class="requisition">
									<tr>
										<th>
										
										</th>
										<th>
											${warehouse.message(code: 'product.label')}
										</th>
										<th>
											${warehouse.message(code: 'product.unitOfMeasure.label')}
										</th>
										<th class="center">
											${warehouse.message(code: 'requisitionItem.quantityRequested.label')}
										</th>
										<th class="center success">
											${warehouse.message(code: 'requisitionItem.quantityPicked.label')}
										</th>
										<th class="center error">
											${warehouse.message(code: 'requisitionItem.quantityCanceled.label')}
										</th>		
										<th class="center notice">
											${warehouse.message(code: 'requisitionItem.quantityRemaining.label')}
										</th>
										<th>
											${warehouse.message(code: 'requisitionItem.status.label')}
										</th>
										<th>
											%
										</th>
										<th>
											${warehouse.message(code: 'default.actions.label')}
										</th>
									</tr>
									<g:each var="${requisitionItem }" in="${requisition?.requisitionItems }" status="i">
										<g:set var="selected" value="${requisitionItem == selectedRequisitionItem }"/>
										<tr class="${selected ?'selected':''} ${i%2?'odd':'even'}">
											<%-- 
											<td>
												<g:render template="../requisitionItem/actions" model="[requisitionItem:requisitionItem]"/>
											</td>
											--%>
											<td>
												<g:if test="${selected }">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" />
												</g:if>
												<g:else>
													${i+1 }
												</g:else>
											</td>
											<td>
												<g:link controller="requisition" action="pick" id="${requisition.id }" params="['requisitionItem.id':requisitionItem?.id]">
												${requisitionItem?.product?.name }
												</g:link>
											</td>
											<td>
												${requisitionItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
											</td>
											<td class="center">
												${requisitionItem?.quantity?:0 }
											</td>
											<td class="center success">
												${requisitionItem?.calculateQuantityPicked()?:0 }									
											</td>
											<td class="center error">
												${requisitionItem?.quantityCanceled?:0}									
											</td>
											<td class="center notice">
												${requisitionItem?.calculateQuantityRemaining()?:0 }						
											</td>
											<td>
												<g:set var="value" value="${((requisitionItem?.calculateQuantityPicked()?:0)+(requisitionItem?.quantityCanceled?:0))/(requisitionItem?.quantity?:1) * 100 }" />
												<div id="progressbar-${requisitionItem?.id }" class="progressbar" style="width: 100px;"></div>
												<script type="text/javascript">
													    $(function() {
													    	$( "#progressbar-${requisitionItem?.id }" ).progressbar({value: ${value}});
													    });
											    </script>
											</td>
											<td>
												${value }%
											</td>
											<td>
												<g:link class="button" controller="requisition" action="pick" id="${requisition.id }" params="['requisitionItem.id':requisitionItem?.id]">
													${warehouse.message(code:'requisitionItem.process.label', default: 'Process') }
												</g:link>
											</td>
										</tr>						
									</g:each>
									<%-- 
									<tfoot>
										<tr>
											<td colspan="8" class="center">
												<g:link controller="requisition" action="finish" id="${requisition.id }" class="button">
													${warehouse.message(code: 'default.button.finish.label')}
												</g:link>
												<g:link action="show" id="${requisition.id}" class="button">
													Back
												</g:link>
											</td>
										</tr>
									</tfoot>
									--%>
								</table>
							</div>				
						</div>
					</g:form>
				</div>
				<div class="clear"></div>			

				
				<div class="buttons">
					<div class="right">
						<g:link controller="requisition" action="review" id="${requisition.id }" class="button">
							<warehouse:message code="default.button.back.label"/>	
						</g:link>

						<g:link controller="requisition" action="picked" id="${requisition.id }" class="button">
							<warehouse:message code="default.button.next.label"/>	
						</g:link>
					</div>
				</div>				
				
			</div>
			<div class="yui-u">
				<div class="box" >				
					<div style="padding:5px;">
						<h3 style="display: inline">${selectedRequisitionItem.product.name } </h3>
						<div class="button-group right middle">
							<g:link controller="requisition" action="pickPreviousItem" id="${selectedRequisitionItem?.requisition?.id }"
								params="['requisitionItem.id':selectedRequisitionItem?.id]"  
								class="button icon arrowleft"></g:link>
							<g:link controller="requisition" action="pickNextItem" id="${selectedRequisitionItem?.requisition?.id }"
								params="['requisitionItem.id':selectedRequisitionItem?.id]" 
								class="button icon arrowright"></g:link>
							
							
						</div>	
					</div>				
					<hr />
					
					
					
					<table class="requisition requisition-details">
						<tr>
							<td>
								<label>Quantity requested</label>					
								<p>${selectedRequisitionItem.quantity?:0 } 
								${selectedRequisitionItem?.product?.unitOfMeasure?:"EA"}</p>
							</td>
							<td class=success>
								<label>Quantity picked</label>							
								<p>${selectedRequisitionItem.calculateQuantityPicked()?:0 } 
								${selectedRequisitionItem?.product?.unitOfMeasure?:"EA"}</p>
							</td>
							<td class="error">
								<label>Quantity canceled</label>							
								<p>${selectedRequisitionItem.quantityCanceled?:0 } 
								${selectedRequisitionItem?.product?.unitOfMeasure?:"EA"}</p>
							</td>
							<td class="notice">
								<label>Quantity remaining</label>							
								<p>${selectedRequisitionItem.calculateQuantityRemaining()?:0 } 
								${selectedRequisitionItem?.product?.unitOfMeasure?:"EA"}</p>
							</td>
						</tr>
					</table>			
			
			
					<div class="tabs">					
						<ul>
							<li><a href="#tabs-picked"><warehouse:message code="requisitionItem.picked.label" default="Picked"/></a></li>
							<li><a href="#tabs-available"><warehouse:message code="requisitionItem.available.label" default="Available"/></a></li>
							<li><a href="#tabs-substitute"><warehouse:message code="requisitionItem.substitute.label" default="Substitute"/></a></li>						
							<li><a href="#tabs-cancel"><warehouse:message code="requisitionItem.cancel.label" default="Cancel"/></a></li>							
						</ul>	
						<div id="tabs-picked">
							<table>
								<tr>
									<th>${warehouse.message(code:'inventoryLevel.binLocation.label') }</th>
									<th>${warehouse.message(code:'inventoryItem.lotNumber.label') }</th>
									<th>${warehouse.message(code:'inventoryItem.expirationDate.label') }</th>
									<th>${warehouse.message(code:'picklistItem.quantity.label') }</th>
									<th></th>
								</tr>
								<g:each var="picklistItem" in="${selectedRequisitionItem?.retrievePicklistItems() }" status="status">
									<tr class="${status%2?'odd':'even' }">
										<td>
											<span class="fade">
											${picklistItem?.inventoryItem?.product?.getInventoryLevel(session?.warehouse?.id)?.binLocation?:"N/A" }
											</span>
										</td>
										<td>
											<span class="lotNumber">${picklistItem?.inventoryItem?.lotNumber }</span>
										</td>
										<td>
											<g:formatDate date="${picklistItem?.inventoryItem?.expirationDate }" format="MMM yyyy"/>
										</td>
										<td class="right">
											${picklistItem?.quantity }
											${picklistItem?.requisitionItem?.product?.unitOfMeasure?:"EA"}
										</td>
										<td>
											<g:link controller="picklistItem" action="delete" id="${picklistItem?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
												<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete" />
											</g:link>
										</td>
									</tr>
								</g:each>
								<g:unless test="${selectedRequisitionItem?.retrievePicklistItems() }">
									<tr style="height: 60px;">
										<td colspan="4" class='center middle'>
											<span class="fade">${warehouse.message(code: 'requisitionItem.noPicklistItems.label') }</span>
										</td>
									</tr>
								</g:unless>
								<g:if test="${selectedRequisitionItem?.retrievePicklistItems() }">
									<tfoot>
										<tr>
											<th>
												Total
											</th>
											<th colspan="2">
											</th>
											<th colspan="2" class="right">
												${selectedRequisitionItem.calculateQuantityPicked() } 
												${selectedRequisitionItem?.product?.unitOfMeasure?:"EA"}
											</th>
										</tr>
									</tfoot>
								</g:if>
							</table>					
						</div>
						<div id="tabs-available">
							<g:set var="inventoryItemMap" value="${selectedRequisitionItem?.retrievePicklistItems().groupBy { it.inventoryItem } }"/>				
							<g:form action="addToPicklistItems">
								<g:hiddenField name="id" value="${requisition?.id }"/>
								<g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
								
								<table>
									<thead>
										<tr>
											<%--
											<th>${warehouse.message(code:'product.label') }</th>
											 --%>
											<th>${warehouse.message(code:'inventoryItem.lotNumber.label') }</th>
											<th>${warehouse.message(code:'inventoryItem.expirationDate.label') }</th>
											<th>${warehouse.message(code:'inventoryItem.quantityOnHand.label') }</th>
											<%-- 
											<th>${warehouse.message(code:'inventoryItem.quantityAvailableToPromise.label') }</th>
											--%>
											<th>${warehouse.message(code:'picklistItem.quantity.label') }</th>
											<th>${warehouse.message(code:'product.uom.label') }</th>
											
										</tr>
									</thead>
									<tbody>
										<g:set var="inventoryItems" value="${productInventoryItemsMap[selectedRequisitionItem?.product?.id] }"/>
										<g:unless test="${inventoryItems }">
											<tr style="height: 60px;">
												<td colspan="6" class="center middle">
													<span class="fade">${warehouse.message(code:'requisitionItem.noInventoryItems.label', default: 'No available items') }</span>
												</td>
											</tr>
										</g:unless>
										<g:each var="inventoryItem" in="${inventoryItems }" status="status">
											<tr class="prop">
												<%-- 
												<td>							
													${inventoryItem?.product?.name }
												</td>
												--%>										
												<td class="middle">							
													<span class="lotNumber">${inventoryItem?.lotNumber }</span>
												</td>										
												<td class="middle">							
													<g:formatDate date="${inventoryItem?.expirationDate }" format="MMM yyyy"/>
												</td>										
												<td class="middle">		
													${inventoryItem?.quantity?:0 }
												</td>
												<td class="middle">							
												
													<g:set var="picklistItem" value="${inventoryItemMap[inventoryItem]?.first()}"/>
													<g:set var="quantity" value="${inventoryItemMap[inventoryItem]?.first()?.quantity?:0}"/>
													<g:if test="${picklistItem }">
														<g:hiddenField name="picklistItems[${status}].id" value="${picklistItem.id }"/>
													</g:if>
													<g:hiddenField name="picklistItems[${status}].requisitionItem.id" value="${selectedRequisitionItem.id }"/>
													<g:hiddenField name="picklistItems[${status}].inventoryItem.id" value="${inventoryItem.id }"/>
													<input name="picklistItems[${status}].quantity" value="${quantity }" size="5" type="text" class="text"/>
												</td>										
												<td>		
													${inventoryItem?.product?.unitOfMeasure?:"EA"}
												</td>
											</tr>
								 		</g:each>
								 	</tbody>
								 	
								 	<g:if test="${inventoryItems }">
								 		<tfoot>
									 		<tr>
									 			<td colspan="6" class="center">
									 				<button class="button">
														${warehouse.message(code:'default.button.save.label') }
													</button>
									 			</td>
									 		</tr>
								 		</tfoot>		
									</g:if>		
								</table>	
							</g:form>
						</div>
						<div id="tabs-substitute">							
							<g:form action="requisition" action="substitute">
								<g:autoSuggestSearchable id="searchable" name="searchable" width="100%"
									jsonUrl="${request.contextPath }/json/findInventoryItems" styleClass="text"/>
								<h4 id="productName"></h4>
								<g:hiddenField id="productId" name="product.id" value=""/>
								<g:hiddenField id="inventoryItemId" name="inventoryItem.id" value=""/>
								<g:hiddenField id="lotNumber" name="lotNumber" value=""/>
								<g:hiddenField name="id" value="${requisition?.id }"/>
								<g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
					
								<div id="substitutionItemDetails" style="display:none;">
									<table>
										<tr class="prop">
											<td class="name">
												<label>Lot Number</label> 
											</td>
											<td>
												<span id="lotNumberText" class="lotNumber"></span>
											</td>
										</tr>
										<tr class="prop">
											<td class="name">
												<label>Expiration date</label> 
											</td>
											<td>
												<span id="expirationDateText"></span>
											</td>
										</tr>
										<tr class="prop">
											<td class="name">
												<label>Quantity on hand</label> 
											</td>
											<td>
												<span id="maxQuantityText"></span>
											</td>
										</tr>
										<tr class="prop">
											<td class="name">
												<label>Quantity</label> 
											</td>
											<td>
												<g:textField id="quantity" name="quantity" value="" class="text" size="6"/>
												
											</td>
										</tr>
										<tr>
											<td>
											</td>
											<td>
												<button class="button">
													<warehouse:message code="default.button.add.label"/>
												</button>
											</td>									
										</tr>
									</table>
								</div>							
							</g:form>						
						</div>
						<div id="tabs-cancel">
							<g:form controller="requisitionItem" action="cancel">
								<g:hiddenField name="id" value="${selectedRequisitionItem?.id }"/>							
								<g:hiddenField name="requisition.id" value="${requisition?.id }"/>

								<table>
									<tbody>
										<tr>
											<td>
												<label>Cancellation quantity</label>									
											</td>
											<td class="middle">
												<g:if test="${selectedRequisitionItem?.quantityCanceled }">
													<g:textField name="quantityCanceled" 
														value="${selectedRequisitionItem.quantityCanceled }" class="text right" size="6"/>
														${selectedRequisitionItem?.product?.unitOfMeasure?:"EA"}
												</g:if>	
												<g:else>																							
													<g:textField name="quantityCanceled" 
														value="${selectedRequisitionItem.calculateQuantityRemaining() }" class="text right" size="6"/>
														${selectedRequisitionItem?.product?.unitOfMeasure?:"EA"}
												</g:else>
											</td>
										</tr>
										<tr>
											<td>
												<label>Cancellation reason</label>
											</td>
											<td>
												<g:select name="cancelReasonCode" from="['Stock out','Substituted','Damaged','Expired','Reserved',
													'Cancelled by requestor','Clinical override']" 
													noSelection="['null':'']" value="${selectedRequisitionItem.cancelReasonCode }"/>
											
											</td>
										</tr>
										<tr>
											<td>
												<label>Cancellation comments</label>
											</td>
											<td colspan="2">
												<g:textArea name="cancelComments" value="" style="width: 100%" rows="3">${selectedRequisitionItem?.cancelComments}</g:textArea>
												
											</td>
										</tr>
										<tr>
											<td colspan="2" class="right">
											
											</td>
										</tr>
									</tbody>
									<tfoot>
										<tr>	
											<td>
												<g:if test="${selectedRequisitionItem.quantityCanceled }">
													<g:link controller="requisitionItem" action="uncancel" id="${selectedRequisitionItem.id }">
														Uncancel ${selectedRequisitionItem.quantityCanceled?:0 } 
														${selectedRequisition?.product?.unitOfMeasure?:"EA" }
													</g:link>
												</g:if>
											
											</td>
											<td colspan="2" class="right">
											
												<button class="button">
													Cancel quantity
												</button>
											</td>
										</tr>
									</tfoot>								
								</table>
							</g:form>
						</div>				
					</div>
				</div>					
			</div>
		</div>
	    	
<script type="text/javascript">
	$(document).ready(function() {
        
    	$(".tabs").tabs(
   			{
   				cookie: {
   					// store cookie for a day, without, it would be a session cookie
   					expires: 1
   				}
   			}
		); 
    	

        //$("#requisitionForm").validate({ submitHandler: viewModel.save });

        $("#accordion").accordion({
          header: ".accordion-header", 
          icons: false, 
          active:false,
          collapsible: true,
          heightStyle: "content"
          });

        //setInterval(function () { saveToLocal(); }, 3000);

        $("#cancelRequisition").click(function() {
            if(confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}')) {
                //openboxes.requisition.deletePicklistFromLocal(picklistFromServer.id);
                return true;
            }
        });

        $(".quantity-picked input").keyup(function(){
           this.value=this.value.replace(/[^\d]/,'');      
           $(this).trigger("change");//Safari and IE do not fire change event for us!
        });

		
	$("#searchable-suggest").autocomplete({
		select: function(event, ui) {
			console.log(ui.item);
			$("#substitutionItemDetails").show();
			
			var expirationDate = new Date(ui.item.expirationDate)
			var expirationDateString = expirationDate.getMonth()+1 + "/" + expirationDate.getDate() + "/" + expirationDate.getFullYear()
			$("#productId").val(ui.item.productId);
			$("#productName").text(ui.item.productName);
			$("#inventoryItemId").val(ui.item.id);
			$("#lotNumber").val(ui.item.lotNumber);
			
			$("#lotNumberText").text(ui.item.lotNumber);
			$("#expirationDateText").text(expirationDateString);
			$("#maxQuantityText").text(ui.item.quantity);
			//$("#quantity").val(ui.item.quantity);
			$("#quantity").focus();
			$(this).val('');
		    return false;
		}
	});

    });
</script>

</body>
</html>
