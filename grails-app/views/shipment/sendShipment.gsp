
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">
		Edit Shipment
	</content>
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
					<div style="padding-bottom: 10px;">
						<g:link controller="shipment" action="showDetails" id="${shipmentInstance.id}">${shipmentInstance?.name}</g:link> 
						 &nbsp; &raquo; &nbsp; 
						<span style="font-size: 90%">Add Comment</span>
					</div>					
				</td>
			</tr>		
			<tr>
				<td width="75%">
					<fieldset>
						<g:render template="summary" />

						
							<g:form action="sendShipment" method="POST">
								<g:hiddenField name="id" value="${shipmentInstance?.id}" />
								<table>
									<tbody>
									
									<tr class="prop">
										<td valign="top" class="name" style="width: 10%;"><label><g:message
											code="shipment.carrier.label" default="Carrier" /></label></td>
										<td valign="top" style="width: 30%;">											

											<div>
												<g:if test="${shipmentInstance?.carrier}">
													<input id="carrier-suggest" type="text" value="${shipmentInstance?.carrier?.firstName} ${shipmentInstance?.carrier?.lastName}">
												</g:if>
												<g:else>
													<input id="carrier-suggest" type="text" value="">
												</g:else>										
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
											
												<g:if test="${shipmentInstance?.recipient}">
													<input id="recipient-suggest" type="text" value="${shipmentInstance?.recipient?.firstName} ${shipmentInstance?.recipient?.lastName}">
												</g:if>
												<g:else>
													<input id="recipient-suggest" type="text" value="">
												</g:else>										
											
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
											code="shipment.actualShippingDate.label" default="Shipping date" /></label></td>
										<td valign="top"
											class=" ${hasErrors(bean: shipmentInstance, field: 'actualShippingDate', 'errors')}"
											nowrap="nowrap">
												<g:jqueryDatePicker name="actualShippingDate"
											value="${shipmentInstance?.expectedShippingDate}" format="MM/dd/yyyy"/>
										</td>
									</tr>											
									<tr class="prop">
			                            <td valign="top" class="name"><label><g:message code="comment.comment.label" default="Comment" /></label></td>                            
			                            <td valign="top" class="value ${hasErrors(bean: commentInstance, field: 'comment', 'errors')}">
		                                    <g:textArea name="comment" cols="60" rows="5"/>
		                                </td>
			                        </tr>  	        
											
										<tr class="prop">
											<td valign="top" class="name"></td>
											<td valign="top" class="value">
												<div class="buttons">
													<button type="submit" class="positive"><img
														src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
														alt="save" /> Send Shipment</button>
													<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative">
														<img
															src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
															alt="Cancel" /> Cancel </g:link>
												</div>				
											</td>
										</tr>
								</tbody>
							</table>
						</g:form>
					</fieldset>
				</td>
				<td width="20%">
					<g:render template="sidebar" />						
				</td>				
			</tr>
		</table>

			
			
	</div>
</body>
</html>
