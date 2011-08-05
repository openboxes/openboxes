
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${warehouse.message(code: 'default.items.label')}" />
		<title>
			<warehouse:message code="default.import.label" args="[entityName.toLowerCase()]" />	
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
					<warehouse:message code="inventory.thereAreRowsIn.message" args="[inventoryMapList.size(), commandInstance.filename]" />
				</div>
				<div class="list">
					<fieldset>
						<div style="overflow: auto; height: 300px; ">
							
							<table style="display: inline-block;">		
								<thead>
									<tr>
										<th><warehouse:message code="category.label"/></th>
										<th><warehouse:message code="product.description.label"/></th>
										<th><warehouse:message code="product.upc.label"/></th>
										<th><warehouse:message code="product.gtin.label"/></th>
										<th><warehouse:message code="product.manufacturer.label"/></th>
										<th><warehouse:message code="product.manufacturerCode.label"/></th>
										<th><warehouse:message code="product.lotNumber.label"/></th>
										<th><warehouse:message code="product.expirationDate.label"/></th>
										<th><warehouse:message code="default.quantity.label"/></th>
										<th><warehouse:message code="default.unitOfMeasure.label"/></th>
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
									&nbsp;${warehouse.message(code: 'default.button.import.label', default: 'Import Now')}</button>
									
								&nbsp;
								<a href="${createLink(controller: "inventoryItem", action: "importInventoryItems")}" class="negative"><warehouse:message code="default.button.clear.label"/></a>
									
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
