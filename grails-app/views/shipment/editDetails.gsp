<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Edit Shipment</content>
</head>

<body>
	<div class="body">
	
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${shipmentInstance}">
			<div class="errors">
				<g:renderErrors bean="${shipmentInstance}" as="list" />
			</div>
		</g:hasErrors>	



		<table>
			<tr>
				<td colspan="2">		
					<table>
						<tr>
							<td>
								<div style="padding-bottom: 10px;">
									<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
									&raquo; <span style="font-size: 90%">Edit Details</span>
								</div>										
							</td>
						</tr>
					</table>		
				</td>	
			</tr>
			<tr>
				<td>
					<g:form action="update" method="post">
						<fieldset>			
							
							<g:render template="summary"/>
							
						
						
							<g:hiddenField name="id" value="${shipmentInstance?.id}" />
							<g:hiddenField name="version" value="${shipmentInstance?.version}" />
							<table>
								<tbody>
									<tr >
										<td>&nbsp;</td>
									</tr>
								
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.name.label" default="Shipment Number" /></label></td>
										<td colspan="3" valign="top"
											class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
										<span style="line-height: 1.5em">${shipmentInstance?.shipmentNumber}</span></td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.name.label" default="Nickname" /></label></td>
										<td colspan="3" valign="top"
											class="value ${hasErrors(bean: shipmentInstance, field: 'name', 'errors')}">
										<g:textField name="name" value="${shipmentInstance?.name}" /></td>
									</tr>
									<tr class="prop">
										<td valign="middle" class="name"><label><g:message
											code="shipment.shipmentType.label" default="Type" /></label></td>
										<td valign="middle" class="value" nowrap="nowrap"><g:select
											name="shipmentType.id"
											from="${org.pih.warehouse.shipping.ShipmentType.list()}"
											optionKey="id" optionValue="name" value="${shipmentInstance?.shipmentType?.id}" />
										</td>
									</tr>							
									<%-- 
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.method.label" default="Shipment method" /></label></td>
										<td valign="top"
											class="value ${hasErrors(bean: shipmentInstance, field: 'shipmentMethod', 'errors')}">
										<g:select name="shipmentMethod.id"
											from="${org.pih.warehouse.shipping.ShipmentMethod.list()}"
											optionKey="id" optionValue="name"
											value="${shipmentInstance?.shipmentMethod?.id}" /></td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.trackingNumber.label" default="Tracking number" /></label></td>
										<td valign="top"
											class="value ${hasErrors(bean: shipmentInstance, field: 'trackingNumber', 'errors')}">
											<g:textField name="trackingNumber"
												value="${shipmentInstance?.trackingNumber}" /> </td>
									</tr>
									--%>
									<tr class="prop">
										<td valign="top" class="name" style="width: 10%;"><label><g:message
											code="shipment.carrier.label" default="Carrier" /></label></td>
										<td valign="top" style="width: 30%;">
											
											<%-- 
											<g:textField name="carrier"
												value="${shipmentInstance?.carrier}" /> 	
												--%>
												
											<div>
												<input id="carrier-suggest" type="text" value="${shipmentInstance?.carrier?.firstName} ${shipmentInstance?.carrier?.lastName}"> 	
												<img id="carrier-icon" src="/warehouse/images/icons/search.png" style="vertical-align: middle;"/>
												<input id="carrier-id" name="carrier.id" type="hidden" value="${shipmentInstance?.carrier?.id}"/>
												<span id="carrier-name"></span>		
											</div>		
											<script>
												$(document).ready(function() {
													$('#carrier-suggest').focus();
													$("#carrier-name").click(function() {
														$('#carrier-suggest').val("");
														$('#carrier-name').hide();
														$('#carrier-suggest').show();				
														$('#carrier-suggest').focus();
														$('#carrier-suggest').select();
													});
											      	$("#carrier-suggest").autocomplete({
											            width: 400,
											            minLength: 2,
											            dataType: 'json',
											            highlight: true,
											            selectFirst: true,
											            scroll: true,
											            autoFill: true,
											            //scrollHeight: 300,
														//define callback to format results
														source: function(req, add){
															//pass request to server
															$.getJSON("/warehouse/test/searchByName", req, function(data) {
																var people = [];
																$.each(data, function(i, item){
																	people.push(item);
																});
																add(people);
															});
												      	},
												        focus: function(event, ui) {			        
												      		$('#carrier-suggest').val(ui.item.label);					
												      		return false;
												        },	
														select: function(event, ui) {	
															$('#carrier-suggest').val(ui.item.label);
															$('#carrier-name').html(ui.item.label);
															$('#carrier-id').val(ui.item.value);
															$('#carrier-icon').attr('src', '/warehouse/images/icons/silk/user.png');
															$('#carrier-suggest').hide();
															$('#carrier-name').show();
															return false;
														}
													});
												});
											</script>													
												
												
										</td>
									</tr>
									<tr class="prop">
										<td class="name"  style="width: 10%;">
											<label><g:message code="shipment.destination.label" default="Recipient" /></label>
										</td>
										<td class="value" style="width: 30%;">								

											
											<div>
												
												<input id="recipient-suggest" type="text" value="${shipmentInstance?.recipient?.firstName} ${shipmentInstance?.recipient?.lastName}"> 												
												<img id="recipient-icon" src="/warehouse/images/icons/search.png" style="vertical-align: middle;"/>
												<input id="recipient-id" name="recipient.id" type="hidden" value="${shipmentInstance?.recipient?.id }"/>												
												<span id="recipient-name"></span>		
											</div>		
											<script>
												$(document).ready(function() {
													//$('#recipient-suggest').focus();
													$("#recipient-name").click(function() {
														$('#recipient-suggest').val("");
														$('#recipient-name').hide();
														$('#recipient-suggest').show();				
														$('#recipient-suggest').focus();
														$('#recipient-suggest').select();
													});
											      	$("#recipient-suggest").autocomplete({
											            width: 400,
											            minLength: 2,
											            dataType: 'json',
											            highlight: true,
											            selectFirst: true,
											            scroll: true,
											            autoFill: true,
											            //scrollHeight: 300,
														//define callback to format results
														source: function(req, add){
															//pass request to server
															$.getJSON("/warehouse/test/searchByName", req, function(data) {
																var people = [];
																$.each(data, function(i, item){
																	people.push(item);
																});
																add(people);
															});
												      	},
												        focus: function(event, ui) {			        
												      		$('#recipient-suggest').val(ui.item.label);					
												      		return false;
												        },	
														select: function(event, ui) {	
															$('#recipient-suggest').val(ui.item.label);
															$('#recipient-name').html(ui.item.label);
															$('#recipient-id').val(ui.item.value);
															$('#recipient-icon').attr('src', '/warehouse/images/icons/silk/user.png');
															$('#recipient-suggest').hide();
															$('#recipient-name').show();
															return false;
														}
													});
												});
											</script>		
	

									</td>
									</tr>						
									
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.expectedShippingDate.label" default="Expected shipping date" /></label></td>
										<td valign="top"
											class=" ${hasErrors(bean: shipmentInstance, field: 'expectedShippingDate', 'errors')}"
											nowrap="nowrap">
												<g:jqueryDatePicker name="expectedShippingDate"
											value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>
										</td>
									</tr>		
									<tr class="prop">
										<td valign="top" class="name"><label><g:message
											code="shipment.totalValue.label" default="Total Value (USD)" /></label></td>
										<td valign="top"
											class=" ${hasErrors(bean: shipmentInstance, field: 'totalValue', 'errors')}"
											nowrap="nowrap">
												<g:textField name="totalValue" value="${shipmentInstance?.totalValue}" />
										</td>
									</tr>		
									<tr class="prop">
										<td class="name"></td>
										<td>
											<div class="buttons">
												<button type="submit" class="positive"><img
												src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
												alt="Save" /> Save</button>
												<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">
													<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
													alt="Cancel" /> Cancel</g:link>
											</div>
										</td>
									</tr>
								</tbody>
							</table>
						</fieldset>
					</g:form>
					
				</td>		
				<td width="20%">
					<g:render template="sidebar" />				
				</td>				
			</tr>
		</table>
			
			
	</div>
</body>
</html>
