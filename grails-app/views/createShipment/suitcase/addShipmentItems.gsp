                                                                                                 
<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="custom" />
         <title>Add Shipment Items</title>         
    </head>
    <body>
        <div class="body">

           <g:if test="${flash.message}">
                 <div class="message">${flash.message}</div>
           </g:if>
           <g:hasErrors bean="${itemInstance}">
                <div class="errors">
                    <g:renderErrors bean="${itemInstance}" as="list" />
                </div>
           </g:hasErrors>
           
			
			<fieldset>
				<div class="dialog">


								
					<table border="0">
						<thead>
							<tr>
								<th>Suitcase/Box</th>
								<th>Item</th>
								<th>Qty</th>
								<th>Recipient</th>
								<th>Lot Number</th>
								<th></th>
							</tr>
						</thead>												

						<g:set var="count" value="${0 }"/>				
							<g:if test="${shipmentInstance?.allShipmentItems }">
								<g:each var="itemInstance" in="${shipmentInstance?.allShipmentItems}" status="status">
									<tr class="${count++%2==0?'odd':'even'}">
										<td>
											<g:if test="${itemInstance?.container?.parentContainer}">
												${itemInstance?.container?.parentContainer?.containerType?.name} 
												${itemInstance?.container?.parentContainer?.name} 
												/
											</g:if>
											${itemInstance?.container?.containerType?.name} 
											${itemInstance?.container?.name} 
										
										</td>

										<td>${itemInstance?.product?.name}</td>
										<td>${itemInstance?.quantity}</td>
										<td>${itemInstance?.recipient?.name}</td>
										<td>${itemInstance?.lotNumber}</td>
										<td>
										</td>
									</tr>
								</g:each>
							</g:if>
						<g:form action="suitcase" method="post" >
							<tr>
								<td>
									<select name="container.id">
										<g:each var="suitcaseInstance" in="${shipmentInstance?.containers}" status="statusSuitcase">
											<option value="${suitcaseInstance?.id}">Suitcase ${suitcaseInstance?.name}</option>
												<g:each var="boxInstance" in="${suitcaseInstance?.containers}" status="statusBox">
													<option value="${boxInstance?.id}"> &nbsp;&nbsp; Box ${boxInstance?.name}</option>
												</g:each>
										</g:each>
									</select>
								</td>
								<td class="value">
									<g:autoSuggest id="product-${suitcaseInstance?.id}" name="product" jsonUrl="/warehouse/json/findProductByName" 
										width="200" valueId="${itemInstance?.product?.id}" valueName="${itemInstance?.product?.name}"/>
								</td>
								<td>
									<g:textField id="quantity-${suitcaseInstance?.id}" name="quantity" size="5" /> 
								</td>
								<td>
									<g:autoSuggest id="recipient-${suitcaseInstance?.id}" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
										width="150" valueId="" valueName=""/>							
								</td>
								<td>
									<g:textField id="lotNumber-${suitcaseInstance?.id}" name="lotNumber" value="${itemInstance?.lotNumber}" size="10" /> 
								</td>													
								<td>
									<g:submitButton name="addItem" value="Add" />								
								</td>
								
							</tr>
						</g:form>				
						
						
					</table>
							
				</div>
	   	    </div>
	   	   </fieldset>
	   	   
				<g:form action="suitcase" method="post" >
					<div class="buttons">
						<span class="formButton">
							<g:submitButton name="back" value="Back"></g:submitButton>								
							<g:submitButton name="submit" value="Next"></g:submitButton>								
						</span>
					</div>
	            </g:form>
	   	   
	   	   
    </body>
</html>
