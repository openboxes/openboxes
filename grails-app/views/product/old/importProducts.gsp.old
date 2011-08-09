<%@ page import="org.pih.warehouse.product.Product"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<content tag="pageTitle">Import Products</content>
<title>Import Products</title>
</head>
<body>
	<div class="body">
	
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if> 
		
		<g:hasErrors bean="${productInstance}">
			<div class="errors">
				<g:renderErrors bean="${productInstance}" as="list" />
			</div>
		</g:hasErrors> 
		
		<script type="text/javascript">
			$(function() { $("#importTabs").tabs(); });
		</script>
								
						
		<div class="dialog">
			
			<g:if test="${session.productTypes || session.dosageForms }">
				<table width="100%">
					<thead>
						<tr>         
							<th width="10%">Name</th>
						</tr>
					</thead>
					<tbody>
						<g:set var="rowStatus" value="${0 }"/>
						<g:each var="productType" in="${productTypes}" status="i">
							<tr class="${(rowStatus++ % 2) == 0 ? 'odd' : 'even'}">         
								<td>Product Type</td>
								<td align="center">
									${productType }
								</td>						
							</tr>
						</g:each>
						<g:each var="dosageForm" in="${dosageForms}" status="i">
							<tr class="${(rowStatus++ % 2) == 0 ? 'odd' : 'even'}">         
								<td>Dosage Form</td>
								<td align="center">${dosageForm}</td>						
							</tr>
						</g:each>
					</tbody>
				</table>
				<div class="buttonBar">
					<g:form action="importDependencies" method="post">
						<div class="buttons">
							<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="import" /> 
								${warehouse.message(code: 'default.button.import.label', default: 'Import')}</button>
							<a href="${createLink(controller: "product", action: "importProducts")}" id="" class="negative"> 
								<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> Cancel </a>															
						</div>						
					</g:form>			
				</div>			
			</g:if>
			<g:else>
				<h2>Products</h2>
				<table width="100%">
					<thead>
						<tr>         
							<th>Product Type</th>
							<th>Name</th>
							<th>French Name</th>
							<th>Product Code</th>
							<th>Dosage Strength</th>
							<th>Dosage Unit</th>
							<th>Dosage Form</th>								
						</tr>
					</thead>
					<tbody>
						<g:each var="productInstance" in="${products}" status="i">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">         
								<td>${fieldValue(bean: productInstance, field: "productType.name")}</td>
								<td align="center">${fieldValue(bean: productInstance, field: "name")}</td>
								<td>${fieldValue(bean: productInstance, field: "frenchName")}</td>
								<td>${fieldValue(bean: productInstance, field: "productCode")}</td>
								<g:if test="${productInstance?.class?.simpleName=='DrugProduct' }">
									<td>${fieldValue(bean: productInstance, field: "dosageStrength")}</td>
									<td>${fieldValue(bean: productInstance, field: "dosageUnit")}</td>
									<td>${fieldValue(bean: productInstance, field: "dosageForm.name")}</td>
								</g:if>
								<g:else>
									<td colspan="3">Not applicable</td>
								</g:else>
							</tr>
						</g:each>		                    
					</tbody>
				</table>				
				<div class="buttonBar">
					<g:form action="importProducts" method="post">
						<span class="buttons">
							<button type="submit" class="positive"><img src="${createLinkTo(dir:'images/icons/silk',file:'tick.png')}" alt="upload" /> 
								${warehouse.message(code: 'default.button.import.label', default: 'Import')}</button>
							<a href="${createLink(controller: "product", action: "importProducts")}" id="" class="negative"> 
								<img src="${createLinkTo(dir:'images/icons/silk',file:'cancel.png')}" alt="" /> Cancel </a>															
						</span>						
					</g:form>			
				</div>			
			</g:else>	
				

		</div>
	</div>
			
</body>
</html>
