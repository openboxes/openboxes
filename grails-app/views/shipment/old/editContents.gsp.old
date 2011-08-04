<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'shipment.label', default: 'Shipment')}" />
	<title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Edit Shipment Contents</content>
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
			<tbody>		
				<tr>
					<td style="width: 75%" valign="top">
						<fieldset>
							<g:render template="summary"/>
							<table>
								<tr>
									<td width="30%">	
										<div style="padding: 1px;">																	
											<g:each in="${shipmentInstance?.containers}" var="container" status="i">											
												<g:if test="${!container?.parentContainer}">															
													<div style="padding: 1px; ">
														<g:render template="containerSummary" model="['containerInstance':container, 'selected':containerInstance]"/>	
														<g:each var="childContainer" in="${container.containers}">
															<div style="padding:1px; padding-left: 5px;">
																<g:link controller="shipment" action="editContents" id="${shipmentInstance.id}" params="['container.id':childContainer.id]">																	
																	<g:render template="containerSummary" model="['containerInstance':childContainer, 'selected':containerInstance]"/>	
																</g:link>
															</div>
														</g:each>
													</div>
												</g:if>		
											</g:each>
										</div>
									</td>
									<td width="70%">
										<g:if test="${containerInstance}">			
											<div id="container-${containerInstance?.id}" class="details">																							
												<script type="text/javascript">
													$(function() {
														$("#tabs").tabs();
													});
												</script>
												<div class="demo">								
													<div id="tabs">
														<ul>
															
															<li><a href="#tabs-1">Contents</a></li>
															<li><a href="#tabs-2">Details</a></li>
															<li><a href="#tabs-3">Clone</a></li>
														</ul>
														<div id="tabs-1">
															<g:render template="containerSummary" />				
															<div style="text-align: right;">						
																<a id="add-person-link" href="#">
																	<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add" style="vertical-align: absmiddle;" /> Add a new person
																</a>
																<script type="text/javascript">
																	$(document).ready(function(){
																		$('#add-person-dialog').dialog({
																			autoOpen: false, 
																			modal: true, 
																			width: '400px',
																			buttons: {
																	           'Add a Person': function() {
																					var firstName = $("#firstName").val();
																					var lastName = $("#lastName").val();
																					var email = $("#email").val();

																	                $.post('/warehouse/json/savePerson', 
																			                {firstName: firstName, lastName: lastName, email: email}, 
																			                function(data) {
																	                    		var item = $("<li>");
																	                    		var link = $("<a>").attr("href", "/warehouse/person/show/" + data.id).html(data.firstName + " " + data.lastName);
																	                    		item.append(link);
																	                    		$('#peopleAdded').append(item);
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
																		$("#add-person-link").click(function() {
																			$('#add-person-dialog').dialog('open');	
																		});																			
																	});
																</script>
															</div>
																			
																															
															<div>
																<h2>Add an item</h2>																	
																<g:form action="saveItem" id="${shipmentInstance.id}">	
																	<g:hiddenField name="container.id" value="${containerInstance?.id}"></g:hiddenField>
																
																															
																	<table>
																		<thead>
																			<tr class="prop">
																				<th></th>
																				<th>Qty</th>
																				<th>Item</th>
																				<th>Lot / Serial No</th>
																				<th>Recipient</th>
																				<th></th>
																			</tr>
																		</thead>
																	
																		<tbody>
																			<tr class="prop" style="background-color: #FFF6BF;">
																				<td width="7%" style="vertical-align: middle; text-align: center"> (new) </td>
																				<td width="10%" style="vertical-align: middle; text-align: center;">
																					<g:textField name="quantity" value="1" size="2" />
																				</td>
																				<td width="20%" style="vertical-align: middle; text-align: left;">
																					<g:autoSuggest id="selectedItem" name="selectedItem" 
																						jsonUrl="/warehouse/json/findProductByName" width="200"/>
																				</td>
																				<td width="15%" style="vertical-align: middle; text-align: left;">	
																					<g:textField name="lotNumber" value="" size="10" style="" />
																				</td>
																				<td width="20%" style="vertical-align: middle; text-align: left;">
																					<g:autoSuggest id="recipient" name="recipient" 
																						jsonUrl="/warehouse/json/findPersonByName"
																						width="150" 
																						valueId="${shipmentInstance?.recipient?.id}" 
																						valueName="${shipmentInstance?.recipient?.email}"/>	
																						
																				</td>
																				<td width="10%" style="vertical-align: middle; text-align: right">
																					<span class="buttons" style="padding: 0px">
																						<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="Add"/> Add</button>
																					</span>
																				</td>
																			</tr>	
																		</tbody>
																	</table>									
																
																</g:form>

																<br/>
																<br/>
																<h2>Included items</h2>
																<g:form action="editContainer">
																	<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}"/>
																	<g:hiddenField name="containerId" value="${containerInstance?.id}"/>
																	
																	
																		<table border="0">
																			<g:if test="${containerInstance?.shipmentItems}">
																				<thead>
																					<tr>
																						<td colspan="6"
																							<div class="fade">
																								To remove an item, enter '0' in <b>Qty</b> field. After modifying
																								any of the values below, click <b>Save</b>.
																							</div>
																						</td>
																					</tr>
																					<tr class="prop">
																						<th></th>
																						<th>Qty</th>
																						<th>Item</th>
																						<th>Lot / Serial No</th>
																						<th>Recipient </th>
																						<th></th>
																					</tr>		
																				</thead>
																				<tbody>
																				
																					<g:each var="item" in="${containerInstance.shipmentItems}" status="itemStatus">
																						<tr class="prop ${(itemStatus % 2) == 0 ? 'odd' : 'even'}">
																							<td width="7%" style="text-align: center;">${itemStatus+1}</td>
																							<td width="10%">
																								<g:hiddenField name="shipmentItems[${itemStatus}].id" value="${item.id}"></g:hiddenField>	
																								<g:textField name="shipmentItems[${itemStatus}].quantity" value="${item.quantity}" size="2" />
																							</td>
																							<td width="20%">
																								<g:autoSuggest id="shipmentItems${itemStatus}-product" name="shipmentItems[${itemStatus}].product" jsonUrl="/warehouse/json/findProductByName" 
																									width="150" 
																									valueId="${item?.product?.id}" 
																									valueName="${item?.product?.name}"/>																								
																							</td>
																							<td width="20%">							
																								<g:autoSuggest id="shipmentItems${itemStatus}-recipient" name="shipmentItems[${itemStatus}].recipient" jsonUrl="/warehouse/json/findPersonByName" 
																									width="150" 
																									valueId="${item?.recipient?.id}" 
																									valueName="${item?.recipient?.email}"/>												
																									
																								
																							</td>
																							<td width="10%" style="vertical-align: bottom; text-align: right">
																								<span class="buttons" style="padding: 0px">
																								
																									<%-- <button type="submit" class="negative"><img src="${createLinkTo(dir:'images/icons',file:'trash.png')}" alt="Delete"/></button>--%>
																								</span>
																							</td>
																						</tr>							
																					</g:each>	
																				</tbody>
																				<tfoot>																																													
																					<tr >
																						<td colspan="6">
																							<div class="buttons" style="padding: 15px; float: right;">
																								<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save"/> Save</button>
																								<%-- <g:link class="negative" controller="shipment" action="deleteContainer" id="${containerInstance.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete"/> Delete</a></g:link> &nbsp;--%>
																							</div>
																						</td>
																					</tr>
																				</tfoot>
																			</g:if>
																			<g:else>
																				<tbody>
																					<tr>
																						<td style="text-align: center" colspan="6" >
																							<div class="fade" style="padding: 20px;">(empty)</div>
																						</td>
																					</tr>	
																				</tbody>												
																			</g:else>	
																		</table>	
																</g:form>	
															</div>																		
															
															<%-- 
																Add a New Person dialog box															
															--%>
															<div id="add-person-dialog" title="Add a new recipient" style="display: none; padding: 50px;" >
																<g:form name="addPersonForm" url="${[controller: 'shipment', action:'savePerson']}" >
																	<g:hiddenField name="id" value="0" />
																	<table>
																		<tbody>
																			<tr class="prop">
													                            <td valign="top" class="name"><label><warehouse:message code="person.firstName.label" default="First Name" /></label></td>                            
													                            <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'firstName', 'errors')}">
																					<g:textField id="firstName" name="firstName" size="15" />
												                                </td>
													                        </tr>  	          
																			<tr class="prop">
													                            <td valign="top" class="name"><label><warehouse:message code="person.lastName.label" default="Last Name" /></label></td>                            
													                            <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'lastName', 'errors')}">
												                                    <g:textField id="lastName" name="lastName" size="15" /> 
												                                </td>
													                        </tr>  	        
																			<tr class="prop">
													                            <td valign="top" class="name"><label><warehouse:message code="person.email.label" default="Email" /></label></td>                            
													                            <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'email', 'errors')}">
												                                    <g:textField id="email" name="email" size="15" /> 
												                                </td>
													                        </tr>											                        
													                        <%--   	        
													                        <tr>
													                        	<td></td>
																				<td valign="top" style="text-align: left">
																					<div class="buttons">		
																						<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" alt="save" /> Save</button>
																						<button type="submit" class="negative"><img src="${createLinkTo(dir:'images/icons/silk',file:'cross.png')}" alt="close" /> Done</button>
																					</div>
																				</td>
																			</tr>
																			--%>				                          
																		</tbody>
																	</table>
																</g:form>																	
															</div>								
														</div><!-- tabs-1 -->	 
														
															
														<div id="tabs-2">
														
															<g:render template="containerSummary" />	
															<g:form action="editContainer">		
																<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}"></g:hiddenField>												    
																<g:hiddenField name="containerId" value="${containerInstance?.id}"></g:hiddenField>										
																			
															    	<table>
															    	
																		<tr class="prop">
												                            <td valign="top" class="name"><label><warehouse:message code="container.parentContainer.label" default="Parent" /></label></td>                            
												                            <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'parentContainer', 'errors')}">
																				<g:select id="parentContainer.id" name='parentContainer.id' noSelection="${['':'']}" 
											                                    	from='${shipmentInstance?.containers}' optionKey="id" optionValue="name"></g:select>
											                                </td>
												                        </tr>  	          
															    	
																		<tr class="prop">										
																			<td class="name"><label>${containerInstance?.containerType?.name} #</label></td>
																			<td class="value">
																				<g:textField name="name" value="${containerInstance?.name}" size="2" />
																			</td>
																		</tr>
																		<tr class="prop">
																			<td class="name"><label class="optional">Contents</label></td>
																			<td class="value">
																				<g:textArea name="description" value="${containerInstance?.description}" cols="40" rows="3"/> &nbsp;
																			</td>
																		</tr>
																	
																		<tr class="prop">										
																			<td class="name"><label class="optional">Weight</label></td>
																			<td class="value">
																				<g:textField name="weight" value="${containerInstance?.weight}" size="7"/> 
																				<g:select name="weightUnits" 
																					from="${[' ', 'lb', 'kg']}"
																					value="${containerInstance?.weightUnits}">
																				</g:select>																	
																				<span class="fade">e.g. '100 lb' or '120 kg' </span>
																			</td>
																		</tr>
																		<tr class="prop">
																			<td class="name"><label class="optional">Dimensions</label></td>
																			<td class="value">
																				<g:textField name="height" value="${containerInstance?.height}" size="2"/> x
																				<g:textField name="width" value="${containerInstance?.width}" size="2"/> x
																				<g:textField name="length" value="${containerInstance?.length}" size="2"/> 																																								
																				<g:select name="volumeUnits" 
																					from="${['', 'in', 'ft', 'cm']}"
																					value="${containerInstance?.volumeUnits}">																							
																				</g:select>
																				
																				 <span class="fade">e.g. '10.1" x 4.2" x 2.8"'</span>
																			</td>		
																		</tr>
																		<tr class="prop">																	
																			<td class=""></td>
																			<td class="value" colspan="2">
																				<div class="buttons">
																					<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="Save" /> Save</button>
																					<g:link class="negative" controller="shipment" action="deleteContainer" id="${containerInstance.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" alt="Delete"/> Delete</a></g:link> &nbsp;
																				</div>
																			</td>
																		</tr>
																
																	</table>
																
															</g:form>		
														</div><!-- tabs-2 -->
												
														<div id="tabs-3">
														
															<g:render template="containerSummary" />	
															<g:form action="copyContainer">
																<g:hiddenField name="id" value="${containerInstance?.id}" />	
																<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />															
																<table>
																	<tbody>
																		<tr class="prop">
													                           <td valign="top" class="name"><label><warehouse:message code="container.name.label" default="Copying unit" /></label></td>                            
													                           <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
																				${containerInstance.containerType.name}-${containerInstance.name}
																			</td>
													                       </tr>  	          
																			<tr class="prop">
													                           <td valign="top" class="name"><label><warehouse:message code="container.copies.label" default="How Many?" /></label></td>                            
													                           <td valign="top" class="value ${hasErrors(bean: containerInstance, field: 'name', 'errors')}">
																				<g:textField name="copies" value="1" size="3"/>
													                              </td>
													                       </tr>  	          
													                       <tr class="prop">
																		    <td class="name"></td>
																		    <td class="value">
																				<div class="buttons">
																					<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Copy</button>
																					</div>	
																			    </td>					                        
													                        </tr>         
													                    </tbody>
													                </table>																			                	               
															    </g:form>
															</div><!-- tabs-3 -->
														</div><!-- tabs -->
													</div><!-- demo -->
												</div><!-- details -->
										</g:if>
										<g:else>												
											<div style="padding: 15px;" class="notice">
												Choose a package to edit
											</div>												
										</g:else>												
									</td>
								</tr>																										
							</table>
						</fieldset>
					</td>						
					<td valign="top" width="20%">							
						<g:render template="sidebar" />
					</td>
				</tr>
			</tbody>
		</table>							
	</div>
</body>
</html>


