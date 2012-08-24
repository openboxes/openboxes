
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="request.confirmPicklist.label"/></title>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${requestInstance}">
			<div class="errors">
				<g:renderErrors bean="${requestInstance}" as="list" />
			</div>
		</g:hasErrors>
		
		<g:form action="createRequest" method="post">
			
			<g:hiddenField name="id" value="${picklist.id }"/>
			<div class="dialog">
			
			
           		<g:render template="../request/summary" model="[requestInstance:requestInstance]"/>
         		<g:render template="header" model="['state':'confirmPicklist']"/>
				
				<table>
					<thead>
						<tr class="odd">
							<th></th>
							<th>${warehouse.message(code:'default.item.label')}</th>
							<th class="center">${warehouse.message(code:'inventory.quantity.label')}</th>
							<%-- 
							<th class="center">${warehouse.message(code:'picklist.quantityToPick.label')}</th>
							--%>
							<th class="center">${warehouse.message(code:'picklist.quantity.label')}</th>
							<th >Comments</th>
							<th></th>
						</tr>
					</thead>					
					<tbody>
						<g:each var="picklistItem" in="${picklist?.picklistItems}" status="i">
							<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
								<g:hiddenField name="picklistItems[${i }].id" value="${picklistItem?.id }" size="5"/>
								<td>
									<g:if test="${picklistItem?.inventoryItem?.product?.images }">
										<g:each var="document" in="${picklistItem?.inventoryItem?.product?.images}" status="j">
											<g:if test="${j==0 }">
												<a class="open-dialog" href="javascript:openDialog('#dialog-${document.id }', '#img-${document.id }');">
													<img src="${createLink(controller:'product', action:'viewThumbnail', id:document.id)}" 
														class="middle" style="padding: 2px; margin: 2px; border: 1px solid lightgrey;" />		
												</a>
												
												<div id="dialog-${document.id }" title="${document.filename }" style="display:none;" class="dialog center">
													<div>
														<img id="img-${document.id }" src="${createLink(controller:'product', action:'viewImage', id:document.id, params:['width':'300','height':'300'])}" 
								           							class="middle image" style="border: 1px solid lightgrey" />
													</div>
													<g:link controller="document" action="download" id="${document.id}">Download</g:link>
												</div>		
											</g:if>				
										</g:each>								
									</g:if>
								</td>
								<td class="top">
									<div class="box">
										<g:link controller="inventoryItem" action="showStockCard" id="${picklistItem?.inventoryItem?.product?.id }" target="_blank">
											<label>${picklistItem?.inventoryItem?.product}</label>
										</g:link>		
										<div>							
											<span class="fade">Lot #:</span>
											${picklistItem?.inventoryItem?.lotNumber?.toUpperCase()}											
										</div>
										<div>
											<span class="fade">Expires:</span>
											<g:formatDate date="${picklistItem?.inventoryItem?.expirationDate}" format="MMMMM yyyy"/>
											(<g:prettyDateFormat date="${picklistItem?.inventoryItem?.expirationDate}" ></g:prettyDateFormat>)
										</div>
										<div>
											<span class="fade">Bin:</span>
											${session?.warehouse?.name }
										</div>
									</div>
								</td>
								<td class="center top">
									${picklistItem?.inventoryItem?.quantityOnHand}
									${picklistItem?.inventoryItem?.product?.unitOfMeasure}
								</td>
								<%-- 
								<td class="center top">
									${picklistItem?.requestItem?.quantity}
									${picklistItem?.inventoryItem?.product?.unitOfMeasure}
								</td>
								--%>
								<td class="top">	
									<g:textField name="picklistItems[${i }].quantity" value="${picklistItem.quantity }" size="5" class="text center"/>		
									<%-- 
									<g:hiddenField name="picklistItems[${i }].requestItem.id" value="${picklistItem?.requestItem?.id }"/>
									<g:hiddenField name="picklistItems[${i }].inventoryItem.id" value="${picklistItem?.inventoryItem?.id }"/>
									--%>
									${picklistItem?.inventoryItem?.product?.unitOfMeasure}						
								</td>
								<td class="top">
									<div style="padding: 1px;">
										<g:select 
											name="picklistItems[${i }].status"
											value="${picklistItem?.status }"
											from="${['Item has been picked','Item is out of stock', 'Items are damaged', 'Item has been canceled', 'Other']}" noSelection="${['null':'Select One...']}" 
											style="width:175px;" />
									</div>
									<div style="padding: 1px;">
										<g:textArea name="picklistItems[${i }].comment" cols="30" rows="3">${picklistItem?.comment }</g:textArea>
									</div>
								</td>
								<td class="top">
									<g:submitButton name="save" value="${warehouse.message(code:'default.button.save.label')}"></g:submitButton> 
								
								</td>
							</tr>
						</g:each>
					</tbody>	
				</table>

				<div class="buttons" style="border-top: 1px solid lightgrey">
					<span class="formButton"> 
						<g:submitButton name="back" value="${warehouse.message(code:'default.button.back.label')}"></g:submitButton> 
						<g:submitButton name="save" value="${warehouse.message(code:'default.button.save.label')}"></g:submitButton> 
						&nbsp;|&nbsp;
						<g:submitButton name="finish" value="Save & Finish"></g:submitButton>
						<g:link action="createRequest" event="cancel"><warehouse:message code="default.button.cancel.label"/></g:link>
					</span>
				</div>
			</div>				
		</g:form>
	</div>
</body>
</html>