<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="pageTitle">Attach Document</content>
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
						<span style="font-size: 90%">Attach Document</span>
					</div>					
				</td>
			</tr>		
		
			<tr>
				<td>
					<fieldset>
						<div id="header">
							<table>
								<tbody>
									<tr>
										<td width="24px;">
											<%-- 
											<img src="${createLinkTo(dir:'images/icons/silk/',file: 'lorry.png')}"
												valign="top" style="vertical-align: middle;" /> 
											--%>
											<img src="${createLinkTo(dir:'images/icons',file: 'ShipmentType' + shipmentInstance?.shipmentType?.name + '.png')}"
												alt="${shipmentInstance?.shipmentType?.name}" style="vertical-align: middle; width: 24px; height: 24px;" />						
										</td>
										<td>
											<span style="font-size: 1.2em;">${shipmentInstance.name}</span> 
											&nbsp; 
											<br/>
											<span style="color: #aaa; font-size: 0.8em;">
												last modified: <g:formatDate date="${shipmentInstance?.lastUpdated}" format="dd MMM yyyy hh:mm" />	&nbsp;							
												created: <g:formatDate date="${shipmentInstance?.dateCreated}" format="dd MMM yyyy hh:mm" />			
											</span>	
										</td>		
										<td style="text-align: right;">
											<span class="fade">[Shipment No. ${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}]</span>
										</td>
									</tr>
								</tbody>							
							</table>
						</div>	
				
					
					
					
						<div>
							<g:uploadForm controller="document" action="upload">
								<g:hiddenField name="shipmentId" value="${shipmentInstance?.id}" />
								<table>
									<tbody>
										<tr class="prop">
											<td valign="top" class="name"><label><g:message
												code="document.documentType.label" default="Document Type" /></label></td>
											<td valign="top"
												class="value ${hasErrors(bean: documentInstance, field: 'documentType', 'errors')}">
												<g:select name="typeId" from="${org.pih.warehouse.core.DocumentType.list()}" optionKey="id" optionValue="name"/>
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"><label class="optional"><g:message
												code="document.name.label" default="Description" /></label>
											</td>
											<td valign="top"
												class="value ${hasErrors(bean: documentInstance, field: 'name', 'errors')}">
												<g:textField name="name" value="${documentInstance?.name}" />
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"><label class="optional"><g:message
												code="document.number.label" default="Document Number" /></label>
											</td>
											<td valign="top"
												class="value ${hasErrors(bean: documentInstance, field: 'documentNumber', 'errors')}">
												<g:textField name="documentNumber" value="${documentInstance?.documentNumber}" />
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"><label><g:message
												code="document.file.label" default="Select a file" /></label>
											</td>
											<td valign="top"
												class="value ${hasErrors(bean: documentInstance, field: 'fileContents', 'errors')}">
												<input name="fileContents" type="file" />
											</td>
										</tr>
										<tr class="prop">
											<td valign="top" class="name"></td>
											<td valign="top" class="value">
												<div class="buttons">
													<button type="submit" class="positive"><img
														src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}"
														alt="save" /> Upload</button>
													<g:link controller="shipment" action="showDetails" id="${shipmentInstance?.id}" class="negative">
														<img
															src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}"
															alt="Cancel" /> Cancel </g:link>
												</div>
											</td>
										</tr>
									</tbody>
								</table>
							</g:uploadForm>
						</div>
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
