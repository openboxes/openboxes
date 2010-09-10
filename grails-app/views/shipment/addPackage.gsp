<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Add Package</content>
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
				<td width="75%">
					<fieldset>
						<g:render template="summary" />

						
							<g:form action="savePackage">
								<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
								<table>
									<tbody>
										<tr class="prop">
				                            <td valign="top" class="name"><label><g:message code="container." default="Type" /></label></td>                            
				                            <td valign="top" class="value ${hasErrors(bean: container, field: 'containerType', 'errors')}">
												<g:select id="containerType.id" name='containerType.id' noSelection="${['':'Select one ...']}" 
			                                    	from='${org.pih.warehouse.shipping.ContainerType.list()}' optionKey="id" optionValue="name"></g:select>
			                                </td>
				                        </tr>  	          
										<tr class="prop">										
											<td class="name"><label>#</label></td>
											<td class="value">
												<g:textField name="name" value="${container?.name}" size="2" />
											</td>
										</tr>
										<tr class="prop">
											<td class="name"><label class="optional">Identifier</label></td>
											<td class="value">
												<g:textField name="containerNumber" value="${container.containerNumber}" size="15"/> &nbsp;		
												<span class="fade"></span>																
											</td>													
										</tr>																	
										<tr class="prop">										
											<td class="name"><label class="optional">Weight</label></td>
											<td class="value">
												<g:textField name="weight" value="${container?.weight}" size="7"/> 
												<g:select name="weightUnits" 
													from="${[' ', 'lb', 'kg']}"
													value="${container?.weightUnits}">
												</g:select>																	
												<span class="fade">e.g. '100 lb' or '120 kg' </span>
											</td>
										</tr>
										<tr class="prop">
											<td class="name"><label class="optional">Dimensions</label></td>
											<td class="value">
												<g:textField name="height" value="${container?.height}" size="2"/> x
												<g:textField name="width" value="${container?.width}" size="2"/> x
												<g:textField name="length" value="${container?.length}" size="2"/> 																																								
												<g:select name="volumeUnits" 
													from="${['', 'in', 'ft', 'cm']}"
													value="${container?.volumeUnits}">																							
												</g:select>
												
												 <span class="fade">e.g. '10.1" x 4.2" x 2.8"'</span>
											</td>		
										</tr>
										<tr class="prop">
											<td class="name"><label class="optional">Description</label></td>
											<td class="value">
												<g:textField name="description" value="${container?.description}" size="20"/> &nbsp;
											</td>
										</tr>
                     
										
										<tr class="prop">
											<td valign="top" class="name"></td>
											<td valign="top" class="value">
												<div class="buttons">
													<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="save" /> Add</button>
													<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative">
														<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="Cancel" /> Cancel 
													</g:link>
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
