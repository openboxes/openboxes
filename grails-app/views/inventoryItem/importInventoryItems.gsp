
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${message(code: 'inventoryItem.label', default: 'Inventory Item')}" />
		<title>
			<g:message code="default.import.label" args="[entityName]" /> &rsaquo;
			<span style="color: grey">${session?.warehouse?.name }</span>
		
		</title>
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

			<div class="dialog">
				<g:render template="uploadFileForm"/>
			</div>
						
			<g:if test="${inventoryMapList}">
				<div>
					There are ${inventoryMapList.size() } rows in ${commandInstance.filename }
				</div>
				<div class="list">
					<fieldset>
						<div style="overflow: auto; height: 300px; ">
							
							<table style="display: inline-block;">		
								<thead>
									<tr>
										<th>Category</th>
										<th>Product Description</th>
										<th>UPC</th>
										<th>GTIN</th>
										<th>Manufacturer</th>
										<th>ManufacturerCode</th>
										<th>Lot Number</th>
										<th>Expiration Date</th>
										<th>Quantity</th>
										<th>Unit of Measure</th>
									</tr>							
								</thead>
								<tbody>							
									<g:each var="entry" in="${inventoryMapList}" status="status">
										<tr class="${status%2?'even':'odd' }">
											<td>
												${entry.category}
											</td>
											<td>
												${entry.productDescription }
											</td>
											<td>
												${entry.upc}
											</td>
											<td>
												${entry.ndc}
											</td>
											<td>
												${entry.manufacturer}
											</td>
											<td>
												${entry.manufacturerCode}
											</td>
											<td>
												${entry.lotNumber}
											</td>
											<td>
												${entry.expirationDate}
											</td>
											<td>
												${entry.quantity}
											</td>
											<td>
												${entry.unitOfMeasure}
											</td>
										</tr>
									</g:each>
								</tbody>
							</table>
						</div>
					</fieldset>
					<g:if test="${!commandInstance.errors.hasErrors()}">
						<div style="text-align: center; display: inline">
							<g:form controller="inventoryItem" action="importInventoryItems" method="POST"> 
								<button type="submit" name="importNow" value="true"><img src="${createLinkTo(dir:'images/icons/silk',file:'disk.png')}" alt="Import Now" /> 
									&nbsp;${message(code: 'default.button.import.label', default: 'Import Now')}</button>
									
								&nbsp;
								<a href="${createLink(controller: "inventoryItem", action: "importInventoryItems")}" class="negative"> Clear </a>
									
							</g:form>			
						</div>
					</g:if>
				</div>
			</g:if>		
			<g:else>
				
			</g:else>
		</div>
	</body>
</html>
