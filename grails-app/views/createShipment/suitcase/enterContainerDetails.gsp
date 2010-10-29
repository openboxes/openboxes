                                            
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Enter Container Details</title>         
    </head>
    <body>
    
    
    
		<div class="body">
			
			<g:if test="${message}">
				<div class="message">${message}</div>
			</g:if>
			<g:hasErrors bean="${containerInstance}">
				<div class="errors">
					<g:renderErrors bean="${containerInstance}" as="list" />
				</div>				
			</g:hasErrors>          
	
							
	
			<fieldset>
				<legend>Step 3&nbsp;Add shipment items</legend>	
			 
			 		<g:render template="flowHeader" model="['currentState':'Pack']"/>		
			 
					<div class="dialog">
					<table style="border: 1px solid #CCC;" border="0">
						<thead>	
							<tr>
								<th>Item</th>
								<th>Qty</th>
								<th></th>
								<th>Actions</th>
							</tr>
						</thead>
						<g:set var="count" value="${0 }"/>	
						<g:each var="suitcaseInstance" in="${shipmentInstance?.containers}">
							<tbody>
								<tr class="${count++%2==0?'odd':'even' }">
									<td style="width:30%;">
										<span>
											<img src="${createLinkTo(dir:'images/icons/silk',file:'briefcase.png')}" alt="Add a box" style="vertical-align: middle"/>&nbsp;
											<b>${suitcaseInstance?.containerType?.name } ${suitcaseInstance?.name }</b>
										</span>
									</td>
									<td style="text-align:center;width:5%;">-</td>
									<td></td>
									<td style="width:25%; text-align: left;">
										<span nowrap>
											<a href="#" id="btnAddItem-${suitcaseInstance?.id}">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" alt="Add an item" style="vertical-align: middle"/>
												&nbsp;add item
											</a> 													
											&nbsp;
											<g:link action="suitcase" event="addBox" id="${shipmentInstance?.id}" params="['suitcase.id':suitcaseInstance?.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" alt="Add a box" style="vertical-align: middle"/>
												&nbsp;add box
											</g:link>
										</span>
										<g:render template="addShipmentItem" model="['containerInstance':suitcaseInstance]"/>
									</td>
								</tr>
								<g:each var="itemInstance" in="${shipmentInstance?.shipmentItems}">		
									<g:if test="${itemInstance?.container?.id == suitcaseInstance?.id}">	
										<tr class="${count++%2==0?'odd':'even' }">
											<td>
												<span style="padding-left: 32px;">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Item" style="vertical-align: middle"/>
													&nbsp;${itemInstance?.product?.name } 	
												</span>
											</td>
											<td style="text-align:center;">
												${itemInstance?.quantity}
											</td>
											<td></td>
											<td style="text-align: left;">		
												<g:link action="suitcase" event="removeItem" id="${itemInstance?.id}" params="['shipment.id':shipmentInstance?.id]">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'page_delete.png')}" alt="remove item" style="vertical-align: middle"/>
													&nbsp;remove item
												</g:link>
											</td>
										</tr>
									</g:if>
								</g:each>
	
								<g:each in="${suitcaseInstance?.containers}" var="boxInstance">
									<tr class="${count++%2==0?'odd':'even' }">
										<td>
											<span style="padding-left: 32px;">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="Package" style="vertical-align: middle"/>
												&nbsp;${boxInstance?.containerType?.name} ${boxInstance?.name}
											</span>
										</td>
										<td style="text-align:center;">-</td>
										<td></td>
										<td style="text-align: left;">
											<a href="#" id="btnAddItem-${boxInstance?.id}">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" alt="Add an item" style="vertical-align: middle"/>
												&nbsp;add item
											</a> 		
											&nbsp;
											
											<g:link action="suitcase" event="removeBox" id="${shipmentInstance?.id}" params="['suitcase.id':suitcaseInstance?.id,'box.id':boxInstance?.id]">
												<img src="${createLinkTo(dir:'images/icons/silk',file:'package_delete.png')}" alt="Add an item" style="vertical-align: middle"/>
												&nbsp;remove box
											</g:link>
											
											<g:render template="addShipmentItem" model="['containerInstance':boxInstance]"/>															
										</td>
									</tr>
									<g:each var="itemInstance" in="${shipmentInstance?.shipmentItems}">	
										<g:if test="${boxInstance?.id == itemInstance?.container?.id }">
											<tr class="${count++%2==0?'odd':'even' }">
												<td>
													<span style="padding-left: 64px;">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'page.png')}" alt="Item" style="vertical-align: middle"/>
														&nbsp;${itemInstance?.product?.name }																	
													</span>
												</td>
												<td style="text-align:center;">
													${itemInstance?.quantity}
												</td>
												<td></td>
												<td style="text-align: left;">		
													<g:link action="suitcase" event="removeItem" id="${itemInstance?.id}" params="['shipment.id':shipmentInstance?.id]">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'page_delete.png')}" alt="remove item" style="vertical-align: middle"/>
														&nbsp;remove item
													</g:link>
												</td>
											</tr>
										</g:if>
									</g:each>												
								</g:each>
							</tbody>											
						</g:each>
						<tfoot>
							<tr class="${count++%2==0?'odd':'even' }">
								<td></td>
								<td></td>
								<td></td>
								<td>
									<g:link action="suitcase" event="addSuitcase" id="${shipmentInstance?.id}">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'briefcase.png')}" alt="add a suitcase" style="vertical-align: middle"/>
										&nbsp;add a suitcase
									</g:link>
								</td>
							</tr>
						</tfoot>
					</table>
				</div>		
				<div class="buttons">
					<g:form action="suitcase" method="post" >
						<table>
							<tr>
								<td style="text-align: center;">
									<g:submitButton name="back" value="Back"></g:submitButton>	
									<g:submitButton name="submit" value="Next"></g:submitButton> 
								</td>
								<td style="text-align: right;">
									<g:submitButton name="done" value="Exit"></g:submitButton>						
								</td>
							</tr>
						</table>
		            </g:form>
				</div>
			</fieldset>
        </div>
    </body>
</html>
