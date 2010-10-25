<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipmentType.label', default: 'Shipment Type')}" />
	<title><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></title>        
	<content tag="pageTitle"><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></content>
	<style>
	</style>
</head>

<body>    

	<script type="text/javascript">
		function showErrors(target, errors) {
		    var errorList = $("<ul>");
		    for (field in errors) {
		        errorList.append("<li>" + errors[field] + "</li>")
		    }
		    $(target).html("").append(errorList).show(500);
		}

		function cleanup() {
		    $('#dlgAddPerson .errors').html("");
		    $('#dlgAddPerson .errors').hide();
		    clearForm('#dlgAddPerson form');
		}

		function clearForm(target) {
		    $(':input', target).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
		}
	</script>


	<div class="body">		
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>							
				
		<g:form action="createSuitcase">
			<g:hiddenField name="id" value="${shipmentInstance?.id}" />
			<g:hiddenField name="version" value="${shipmentInstance?.version}" />
	
			<fieldset>
				<legend>Step 3. Add Contents</legend>
				
				<div class="">				
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.name.label" default="Shipment Number" /></label>
								</td>
								<td colspan="3" valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
									${shipmentInstance?.shipmentNumber}
								</td>
							</tr>
							<tr class="prop">
								<td valign="middle" class="name"><label><g:message
									code="shipment.shipmentType.label" default="Type" /></label></td>
								<td valign="middle" class="value" nowrap="nowrap">
									<g:hiddenField name="shipmentType.id" value="${shipmentInstance?.shipmentType?.id}"/>
									${shipmentInstance?.shipmentType?.name}								
								</td>
							</tr>			
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.name.label" default="Name" /></label>
								</td>
								<td colspan="3" valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
									${shipmentInstance?.name}
								</td>
							</tr>									
							<tr class="prop">
								<td valign="top" class="name"><label>Route</label></td>
								<td valign="top"
									class="value">
										${shipmentInstance?.origin?.name}
										&nbsp;							
										<img src="${createLinkTo(dir:'images/icons/silk',file: 'arrow_right.png')}" />
										&nbsp;							
										${shipmentInstance?.destination?.name}
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.totalValue.label" default="Total Value (USD)" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'totalValue', 'errors')}"
									nowrap="nowrap">
										$ ${formatNumber(number: shipmentInstance?.totalValue, format: '###0.00')}
								</td>
							</tr>															
							<tr class="prop">
								<td valign="top" class="name" style="width: 10%;"><label><g:message
									code="shipment.traveler.label" default="Traveler" /></label></td>
								<td class="value" valign="top" style="width: 30%;">
									${shipmentInstance?.carrier?.name} 
									<g:if test="${shipmentInstance?.flightInformation}">
										&nbsp;<span class="fade">(flight #${shipmentInstance?.flightInformation})</span>
									</g:if>
								</td>
							</tr>
							<tr>
								<td colspan="2"><hr/></td>
							</tr>	
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.expectedShippingDate.label" default="Expected shipping date" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"
									nowrap="nowrap">
										${formatDate(date: shipmentInstance?.expectedShippingDate, format: 'MMMMM dd, yyyy')}
								</td>
							</tr>		
							<tr class="prop">
								<td valign="top" class="name"><label><g:message
									code="shipment.expectedShippingDate.label" default="Expected arrival date" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: shipmentInstance, field: 'expectedDeliveryDate', 'errors')}"
									nowrap="nowrap">									
										${formatDate(date: shipmentInstance?.expectedDeliveryDate, format: 'MMMMM dd, yyyy')}
								</td>
							</tr>		
							<tr class="prop">
								<td valign="top" class="name"></td>
								
								<td valign="top" class="value">
									<g:each var="containerInstance" in="${shipmentInstance?.containers}">
										${containerInstance?.id}
										${containerInstance?.shipmentItems?.size()}
										
										<g:each var="itemInstance" in="${containerInstance?.shipmentItems}">
											${itemInstance?.product?.id}
										</g:each>
										
									</g:each>							
								</td>							
							
							</tr>
							
							
							<tr>
								<td colspan="2"><hr/></td>
							</tr>	
							<tr class="prop">
								<td class="name"><label>Contents</label></td>
								<td class="value">
									<div style="text-align: right;">						
										<a id="btnAddPerson" href="#">
											<img src="${createLinkTo(dir:'images/icons/silk',file:'user_add.png')}" 
												alt="Add a person" 
												style="vertical-align: middle;" /> Add a recipient</a>
										&nbsp;&nbsp;
										<a href="#" id="btnAddItem">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'page_add.png')}" alt="Add an item" style="vertical-align: middle"/>&nbsp;Add an item</a> 													
										&nbsp;&nbsp;
										<a href="#" id="btnAddBox">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'package_add.png')}" alt="Add a box" style="vertical-align: middle"/>&nbsp;Add a box</a>		
									</div>								


									<g:each var="containerInstance" in="${shipmentInstance?.containers}" status="containerStatus">
										<table width="100%" border="0" cellspacing="0" cellpadding="0" style="border: 1px solid #aaa">
											<tr>
												<td colspan="2" style="background-color: #eee;">
													<a name="${containerInstance?.containerType?.name}-${containerInstance.name}">
														<b>${containerInstance?.containerType?.name} ${containerInstance?.name }</b>	
													</a>												
													<span style="float: right">
														<%-- 
														<g:link action="createSuitcase" event="addItem" id="${shipmentInstance?.id}"
															params="['container.id':containerInstance?.id]">
															<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Item" style="vertical-align: middle"/>&nbsp;
															Add Item</g:link>		
																
														&nbsp; <span class="fade">|</span> &nbsp;  
															
														<g:if test="${containerInstance?.containerType?.name != 'Suitcase' }">
															<g:link action="createSuitcase" event="deleteBox" id="${shipmentInstance?.id}"
																params="['container.id':containerInstance?.id]">
																	<img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Remove Box" style="vertical-align: middle"/>&nbsp;Delete Box</g:link>																				
														</g:if>
														<g:else>
															<g:link action="createSuitcase" event="addBox" id="${shipmentInstance?.id}" params="['container.id':containerInstance?.id]">
																<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add Box" style="vertical-align: middle"/>&nbsp;
																Add Box</g:link>		
														</g:else>		
														--%>					
													</span>							
												</td>
											</tr>
											<tr>
												<td>
													<table id="shipmentItems" border="0" width="100%">
														<thead>
															<tr class="odd">
																<th>Product</th>
																<th>Quantity</th>
																<th>Serial Number</th>
																<th>Lot Number</th>
																<th>Recipient</th>
																<th>Remove?</th>																
															</tr>
														</thead>
														<tbody>		
															<g:if test="${containerInstance.shipmentItems}">	
																<g:each var="itemInstance" in="${containerInstance.shipmentItems}" status="itemStatus"> 
																	<tr class="${itemStatus % 2 == 0 ? 'odd':'even'}">
																		<td>
																			<g:hiddenField
																			${containerStatus}].shipmentItems[${itemStatus}].id"
																				value="${itemInstance?.id}" />
																			<!--  
																			<g:hiddenField
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].product.id"
																				value="${itemInstance?.product?.id}" />
																				-->
																			<g:autoSuggest id="containers${containerStatus}-shipmentItems${itemStatus}-product" name="containers[${containerStatus}].shipmentItems[${itemStatus}].product" jsonUrl="/warehouse/json/findProductByName" 
																				width="150" 
																				valueId="${itemInstance?.product?.id}" 
																				valueName="${itemInstance?.product?.name}"/>																								
																				
																		</td>
																		<td>
																			<g:textField size="3"
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].quantity"
																				value="${itemInstance?.quantity}" />												 
																		</td>
																		<td>
																			<g:textField size="10"
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].serialNumber"
																				value="${itemInstance?.serialNumber}" />
																		</td>
																		<td>
																			<g:textField size="10"
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].lotNumber"
																				value="${itemInstance?.lotNumber}" />
																		</td>
																		<td>
																			<g:autoSuggest id="containers${containerStatus}-shipmentItems${itemStatus}-recipient" 
																				name="containers[${containerStatus}].shipmentItems[${itemStatus}].recipient" jsonUrl="/warehouse/json/findPersonByName" 
																				width="150" 
																				valueId="${itemInstance?.recipient?.id}" 
																				valueName="${itemInstance?.recipient?.name}"/>												
																		</td>																		
																		<td>
																			<g:link action="createSuitcase" event="deleteItem" id="${shipmentInstance?.id}"
																				params="['container.id':containerInstance?.id, 'item.id':itemInstance?.id]">Remove</g:link>																			
																		</td>
																	</tr>			
																</g:each>		
																<g:if test="${containerInstance?.containerType?.name == 'Suitcase' }">
																	<g:if test="${shipmentInstance?.containers?.size() > 1}">
																		<tr>
																			<td colspan="6" style="text-align: left"> Including boxes 
																				<g:each var="innerContainerInstance" in="${shipmentInstance?.containers}" status="innerContainerStatus">
																					<g:if test="${innerContainerInstance?.containerType?.name != 'Suitcase' }">
																						 <a href="#${innerContainerInstance?.containerType?.name}-${innerContainerInstance.name}">
																							${innerContainerInstance?.containerType?.name} ${innerContainerInstance.name}</a> &nbsp;
																					</g:if>
																				</g:each>
																			</td>
																		</tr>
																	</g:if>																
																</g:if>
															</g:if>
															<g:else>
																<!-- box is empty -->
																<tr>
																	<td colspan="6" style="text-align: center; height: 25px;">
																		<span class="fade">empty</span></td>
																</tr>
															</g:else>															
														</tbody>																					
													</table>													
												</td>												
											</tr>
											<tr>
												<td>

													<table border="0" width="100%">
														<tbody>		
															<g:if test="${containerInstance?.containers}">	
																<g:each in="${containerInstance?.containers}" var="innerContainer">
																	<tr>
																		<td>${innerContainer?.containerType?.name} ${innerContainer?.name}</td>																	
																	</tr>																
																</g:each>
															</g:if>
														</tbody>
													</table>
												</td>
											</tr>
											
										</table>	
										<br/>														
									</g:each>
								</td>
							</tr>
							<tr class="prop">
								<td class=""></td>
								<td class="">
									<div class="">									
									    <g:submitButton name="next" value="Next"></g:submitButton>
										<g:submitButton name="back" value="Back"></g:submitButton>
										<g:link action="createSuitcase" event="cancel" id="${shipmentInstance?.id}">Cancel</g:link>
								    </div>
								</td>
							</tr>
						</tbody>
					</table>										
				</div>
			</fieldset>
		</g:form>		
	</div>	
	<div id="dlgAddPerson" title="Add a recipient" style="display: none; padding: 10px;" >
		<div id="messages"></div>	
		<g:form controller="suitcase" action="savePerson" method="POST">
			<g:hiddenField name="id" value="0" />
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="person.firstName.label" default="First Name" /></label></td>                            
						<td valign="top" class="value ${hasErrors(bean: personInstance, field: 'firstName', 'errors')}">
							<g:textField id="firstName" name="firstName" size="15" />
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="person.lastName.label" default="Last Name" /></label></td>                            
						<td valign="top" class="value ${hasErrors(bean: personInstance, field: 'lastName', 'errors')}">
							<g:textField id="lastName" name="lastName" size="15" /> 
						</td>
					</tr>  	        
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="person.email.label" default="Email" /></label></td>                            
						<td valign="top" class="value ${hasErrors(bean: personInstance, field: 'email', 'errors')}">
							<g:textField id="email" name="email" size="30" /> 
						</td>
					</tr>
				</tbody>
			</table>
		</g:form>																	
	</div>			

	<div id="dlgAddItem" title="Add an item" style="display: none; padding: 10px;" >
		<div id="dlgAddItem-messages"></div>	
		<g:form action="createSuitcase">
			<g:hiddenField name="shipment.id" value="${shipmentInstance?.id }"/>
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="shipmentItem.container.label" default="Container" /></label></td>                            
						<td valign="top" class="value">
							<g:select id="container.id" name="container.id" from="${shipmentInstance?.containers}" optionKey="id" optionValue="${{it?.containerType?.name + ' ' + it?.name}}" noSelection="['null': '']" />

						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="shipmentItem.product.label" default="Product" /></label></td>                            
						<td valign="top" class="value">
							<g:autoSuggest id="product" name="product" jsonUrl="/warehouse/json/findProductByName" 
								width="150" valueId="" valueName=""/>
							
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="shipmentItem.quantity.label" default="Quantity" /></label></td>                            
						<td valign="top" class="value">
							<g:textField id="quantity" name="quantity" size="15" /> 
						</td>
					</tr>  	        
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="shipmentItem.lotNumber.label" default="Lot Number" /></label></td>                            
						<td valign="top" class="value">
							<g:textField id="lotNumber" name="lotNumber" size="30" /> 
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message code="shipmentItem.recipient.label" default="Recipient" /></label></td>                            
						<td valign="top" class="value">
							<g:autoSuggest id="recipient" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
								width="150" valueId="" valueName=""/>							
							
						</td>
					</tr>
					<tr>
						<td>
							<g:submitButton name="addItem" value="Add Item"></g:submitButton>
						</td>
					</tr>
				</tbody>
			</table>
		</g:form>																	
	</div>			
	<script type="text/javascript">
		$(document).ready(function(){

			$("#btnAddPerson").click(function() { 
				$('#dlgAddPerson').dialog('open');
			});																						
			$('#dlgAddPerson').dialog({
				autoOpen: false, 
				modal: true, 
				width: '600px',
		        buttons: {
		            'Create a new person': function() {
						var firstName = ("#firstName").val();
						var lastName = $("#lastName").val();
						var email = $("#email").val();
		                $.post('/warehouse/json/savePerson', 
			                {firstName: firstName, lastName: lastName, email: email}, 
			                function(data) {
				                if (data.success) { 
		                    		var item = $("<div>");
		                    		var span = $("<span>").html("You have successfully created a new person " + data.domainInstance.firstName + " " + data.domainInstance.lastName);
		                    		item.append(span);
		                    		$('#messages').append(item);
		                    		$('#messages').addClass("message");
		                    		cleanup();
					                $(this).dialog('close');
				                }
				                else { 
				                	showErrors("#dlgAddPerson #messages", data.errors);
		                    		$('#messages').addClass("errors");
				                }
	                		}, 'json');
		            },
		            Cancel: function() {
		                $(this).dialog('close');
		            }
				},				
	    		close: function(event) { 
	    			event.preventDefault()
		    		//alert("close");
	                //window.location.reload(true);
		    	}
			});

			$("#btnAddItem").click(function() { 
				$('#dlgAddItem').dialog('open'); 
			});									
			$('#dlgAddItem').dialog({
				autoOpen: false, 
				modal: true, 
				width: '600px',
		        buttons: {
		            'Add an item': function() {
		                $.post('/warehouse/json/saveItem', 
			                {	
								'shipmentId': $("#shipment\\.id").val(), 
								'containerId': $("#container\\.id").val(), 
								'productId': $("#product-id").val(), 
								'recipientId': $("#recipient-id").val(),
								'quantity': $("#quantity").val(), 
								'lotNumber': $("#lotNumber").val()
							}, 
			                function(data) {
				                if (data.success) { 
		                    		var item = $("<div>");
		                    		item.html("You have successfully created a new shipment item for " + data.domainInstance.product.name);
		                    		$('#dlgAddItem-messages').append(item);
		                    		$('#dlgAddItem-messages').addClass("message");
		                    		//cleanup();
					                $(this).dialog('close');
				                }
				                else { 
		                    		$('#dlgAddItem #dlgAddItem-#messages').addClass("errors");
				                	showErrors("#dlgAddItem #dlgAddItem-messages", data.errors);
				                }
	                		}, 'json');
		            },
		            Cancel: function() {
		                $(this).dialog('close');
		            }
				},				
	    		close: function(event) { 
	    			event.preventDefault()
		    		//alert("close");
	                //window.location.reload(true);
		    	}
			});

			$("#btnAddBox").click(function() { 
				$('#dlgAddBox').dialog('open'); 
			});									
			$('#dlgAddBox').dialog({
				autoOpen: false, 
				modal: true, 
				width: '600px',
		        buttons: {
		            'Add a box': function() {
		                $.post('/warehouse/json/saveItem', 
			                {	
								'name': $("#name").val()
								
							}, 
			                function(data) {
				                if (data.success) { 
		                    		var item = $("<div>");
		                    		item.html("You have successfully created a new shipment item for " + data.domainInstance.product.name);
		                    		$('#messages').append(item);
		                    		$('#messages').addClass("message");
		                    		cleanup();
					                $(this).dialog('close');
				                }
				                else { 
				                	showErrors("#dlgAddBox #messages", data.errors);
		                    		$('#messages').addClass("errors");
				                }
	                		}, 'json');
		            },
		            Cancel: function() {
		                $(this).dialog('close');
		            }
				},				
	    		close: function(event) { 
	    			event.preventDefault()
		    		//alert("close");
	                //window.location.reload(true);
		    	}
			});
				
		});
	</script>	
	
	
</body>
</html>