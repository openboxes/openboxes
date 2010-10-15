
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipmentType.label', default: 'Shipment Type')}" />
	<title><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></title>        
	<content tag="pageTitle"><g:message code="shipment.create.label" default="Create Suitcase Shipment" /></content>
	<style>
		label { display: block; text-decoration: none; } 
		fieldset .prop { margin: 10px; }
		.radio { padding-left: 10px; } 
		.body {  } 
		#progressbar { border: 1px solid #aaa; background-color: whitesmoke; padding: 10px; margin: 10px; } 
		#progressbar .step { padding-left: 0;}
		.step.selected { font-weight: bold; text-decoration: underline;} 
		span.right { float: left; } 
		span.left { width: 10%;  float: left; } 
		#wizard { }
	</style>
</head>

<body>    
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		
		<g:hasErrors bean="${suitcaseCommand}">
            <div class="errors">
                <g:renderErrors bean="${suitcaseCommand}" as="list" />
            </div>
        </g:hasErrors>		
				
		<div class="dialog">
	              		
			<div style="text-align: center; margin: 10px;">
				<span id="progressbar">
					<span class="step ${(suitcaseCommand?.stepNumber==1)?'selected':''}">
						<g:message code="shipment.wizard.step1.label" default="Step 1" /></span> 
					<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle"/>
					<span class="step ${(suitcaseCommand?.stepNumber==2)?'selected':''}">
						<g:message code="shipment.wizard.step2.label" default="Step 2" /></span> 
					<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle"/>
					<span class="step ${(suitcaseCommand?.stepNumber==3)?'selected':''}">
						<g:message code="shipment.wizard.step3.label" default="Step 3" /></span> 
					<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle"/>
					<span class="step ${(suitcaseCommand?.stepNumber==4)?'selected':''}">
						<g:message code="shipment.wizard.step4.label" default="Step 4" /></span> 
					<img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_right.png')}" style="vertical-align: middle"/>
					<span class="step ${(suitcaseCommand?.stepNumber==5)?'selected':''}">
						<g:message code="shipment.wizard.step5.label" default="Step 5" /></span> 
				</span>	    
			</div>	         
			
			       
			<div id="wizard" class="dialog">			
				<fieldset>
					<legend>
						<g:message code="shipment.wizard.step${suitcaseCommand?.stepNumber}.label" default="Step ${params.stepNumber}" />
					</legend>				
				
	
						<g:form action="saveContents" method="post">
			                <g:hiddenField name="id" value="${suitcaseCommand?.shipment?.id}" />
			                <g:hiddenField name="stepNumber" value="${suitcaseCommand?.stepNumber}" />
			                <g:hiddenField name="version" value="${suitcaseCommand?.shipment?.version}" />
							
							<label><g:message code="shipment.contents.label" default="Suitcase Contents" /></label>
							<span style="float: right;">
								<a  id="add-item" href="#">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" style="vertical-align: middle;" />
									add item 
								</a>		
								&nbsp;							
								<a id="add-person" href="#">
									<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" style="vertical-align: middle;" /> 
									add recipient
								</a>
							</span>
	
							<table id="packingList">
								<thead>
									<tr class="odd">
										<th>Item</th>
										<th>Qty</th>
										<th>Lot Number</th>
										<th>Exp Date</th>
										<th>Recipient</th>
									</tr>
								</thead>
								<tbody>
									<g:if test="${suitcaseCommand?.suitcase?.shipmentItems}">
										<g:each var="item" in="${suitcaseCommand?.suitcase?.shipmentItems}" status="status">
											<tr class="prop" style="background-color: #FFF6BF;">
												<td style="vertical-align: middle; text-align: left;">								
													<g:autoSuggest id="selectedItem-${status}" name="selectedItem" jsonUrl="/warehouse/json/findProductByName" 
														valueId="${item?.product?.id}" valueName="${item?.product?.name}" width="200"/>	
												</td>
												<td style="vertical-align: middle; text-align: center;">
													<g:textField name="quantity-${status}" value="1" size="2" />
												</td>
												<td style="vertical-align: middle; text-align: left;">													
													<g:textField name="serialNumber-${status}" value="${item?.serialNumber}" size="10" style="" />
												</td>
												<td style="vertical-align: middle; text-align: left;">													
													<g:datePicker name="expiryDate-${status}" value="" size="10" style="" precision="day" />
												</td>
												<td style="vertical-align: middle; text-align: left;">
													<g:autoSuggest id="recipient-${status}" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
														width="150" valueId="${item?.recipient?.id}" valueName="${item?.recipient?.email}"/>	
												</td>
											</tr>											
										</g:each>
										<%-- 										
										<tr class="prop" style="background-color: #FFF6BF;">
											<td style="vertical-align: middle; text-align: left;">								
												<g:autoSuggest id="product" name="product" jsonUrl="/warehouse/json/findProductByName" 
													valueId="" valueName="" width="200"/>	
											</td>
											<td style="vertical-align: middle; text-align: center;">
												<g:textField name="quantity" value="1" size="2" />
											</td>
											<td style="vertical-align: middle; text-align: left;">													
												<g:textField name="serialNumber" value="" size="10" style="" />
											</td>
											<td style="vertical-align: middle; text-align: left;">													
												<g:textField name="expiryDate" value="" size="10" style="" />
											</td>
											<td style="vertical-align: middle; text-align: left;">
												<g:autoSuggest id="recipient" name="recipient" jsonUrl="/warehouse/json/findPersonByName" 
													width="150" valueId="" valueName=""/>	
											</td>
										</tr>
										--%>
									</g:if>									
									<g:else>
										<tr>
											<td colspan="5" style="text-align: center"><span class="fade">empty</span>
										</tr>									
									</g:else>
								</tbody>
							</table>						
							<div class="prop">		
								<div class="buttons" style="text-align: right;">
									<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'bullet_go.png')}" alt="next" /> Next</button>											
								</div>					
							</div>	
						</g:form>
					
				</fieldset>

				<div class="fade" style="border: 1px dashed #aaa; padding: 10px;">
					<label>Debugging</label>
					<ul>
						<li>Step: ${session?.suitcase?.stepNumber }</li>
						<li>Name: ${session?.suitcase?.shipment?.name }</li>
						<li>Type: ${session?.suitcase?.shipment?.shipmentType?.name }</li>
						<li></li>
					</ul>
				</div>
			</div>
		</div>
	</div>

	<div id="add-person-dialog" title="Add a new recipient" style="display: none; padding: 50px;" >							
		<g:form name="addPersonForm" url="${[controller: 'shipment', action:'savePerson']}" >
			<g:hiddenField name="id" value="0" />
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message
							code="person.firstName.label" default="First Name" /></label></td>
						<td valign="top"
							class="value ${hasErrors(bean: personInstance, field: 'firstName', 'errors')}">
						<g:textField id="firstName" name="firstName" size="15" /></td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message
							code="person.lastName.label" default="Last Name" /></label></td>
						<td valign="top"
							class="value ${hasErrors(bean: personInstance, field: 'lastName', 'errors')}">
						<g:textField id="lastName" name="lastName" size="15" /></td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><g:message
							code="person.email.label" default="Email" /></label></td>
						<td valign="top"
							class="value ${hasErrors(bean: personInstance, field: 'email', 'errors')}">
						<g:textField id="email" name="email" size="15" /></td>
					</tr>				
				</tbody>
			</table>
		</g:form>																	
	</div>							

<div id="add-item-dialog" title="Add a new item" style="display: none; padding: 50px;" >							

	<g:form>
		<g:hiddenField name="id" value="0" />
		<table>
			<tbody>
			
			
			</tbody>
		</table>
	</g:form>
</div>				

											

	
	<script type="text/javascript">
		$(document).ready(function(){
	
			$("#add-item").live('click', function(){
				var row = '<tr class="prop" style="background-color: #FFF6BF;"><td colspan="5" align="center"><span class="fade">specify new product here</span></td></tr>';
								
	       		$('#packingList > tbody:last').append(row);
			});
	
			$("#add-person").click(function() {
				$('#add-person-dialog').dialog('open');																																		
			});																			
	
			$('#add-person-dialog').dialog({
				autoOpen: false, 
				modal: true, 
				width: '400px',
				buttons: {
		           'Add a New Recipient': function() {
						var firstName = $("#firstName").val();
						var lastName = $("#lastName").val();
						var email = $("#email").val();
	
		                $.post('/warehouse/json/savePerson', 
				                {firstName: firstName, lastName: lastName, email: email}, 
				                function(data) {
		                    		//var item = $("<li>");
		                    		//var link = $("<a>").attr("href", "/warehouse/person/show/" + data.id).html(data.firstName + " " + data.lastName);
		                    		//item.append(link);
		                    		//$('#peopleAdded').append(item);
		                		}, 'json');
		                $(this).dialog('close');
		            },	
		            Cancel: function() {
		                $(this).dialog('close');
		            }																	            																		            
	    		}, 
	    		close: function() { 
	                //window.location.reload(true);
					// does nothing 
		    	}
			});
		});
	</script>		
	
	
	
	
</body>
</html>
