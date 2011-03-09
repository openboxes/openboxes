
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${message(code: 'inventoryItem.label', default: 'Inventory Item')}" />
		<title><g:message code="default.import.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="body">
	
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if> 
			<g:hasErrors bean="${commandInstance}">
				<div class="errors"><g:renderErrors bean="${commandInstance}" as="list" /></div>
			</g:hasErrors>
			<g:if test="${errorMessages }">
				<div class="errors">
					<ul>
						<g:each var="erroMessage" in="${errorMessages.unique() }">
							<li>${erroMessage }</li>
						</g:each>
					</ul>
				</div>
			</g:if>
		

			<div class="dialog">
				<g:render template="uploadFileForm"/>
			</div>
						
			<g:if test="${inventoryMapList}">
				<div class="list">
					<fieldset>
						<div style="overflow: auto; height: 300px; ">
							
							<table style="display: inline-block;">		
								<thead>
									<tr>
										<th>Serenic Code</th>
										<th>Product</th>
										<th>Category</th>
										<th>Make</th>
										<th>Model</th>
										<th>Expiration Date</th>
										<th>Lot Number</th>
										<th>Dosage / Size</th>
										<th>Unit of Measure</th>
										<th>Quantity</th>
										<th>Comments</th>
									</tr>							
								</thead>
								<tbody>							
									<g:each var="entry" in="${inventoryMapList}" status="status">
										<tr class="${status%2?'even':'odd' }">
											<td>
												${entry.serenicCode }
											</td>
											<td>
												${entry.product }
											</td>
											<td>
												${entry.category}
											</td>
											<td>
												${entry.make}
											</td>
											<td>
												${entry.model}
											</td>
											<td>
												${entry.expirationDate}
											</td>
											<td>
												${entry.lotNumber}
											</td>
											<td>
												${entry.dosage}
											</td>
											<td>
												${entry.unitOfMeasure}
											</td>
											<td>
												${entry.quantity}
											</td>
											<td>
												${entry.comments}
											</td>
										</tr>
									</g:each>
								</tbody>
							</table>
						</div>
						
					</fieldset>
					
					<g:form controller="inventoryItem" action="importInventoryItems" method="POST"> 
						<button type="submit" name="importNow" value="true"><img src="${createLinkTo(dir:'images/icons/silk',file:'disk.png')}" alt="Import Now" /> 
							&nbsp;${message(code: 'default.button.import.label', default: 'Import Now')}</button>
					</g:form>			
				</div>
			</g:if>						
		</div>
	</body>
</html>
