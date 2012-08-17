<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Product')}" />
	<title><warehouse:message code="default.search.label" args="[entityName]" /></title>
</head>
<body>

	<div class="body">
	
		<g:if test="${flash.message }">
			<div class="message">
				${flash.message }
			</div>
		</g:if>
	
		<h1>Search by National Drug Code </h1>
		<div class="box center">
			<g:form action="lookupProducts" method="get">
			
				<g:textField name="searchTerms" class="text" size="60" value="${search.searchTerms}"/>
				<g:submitButton name="Search"/>
			</g:form>
		</div>
		
		
		<div class="results">
			<g:if test="${search.results }">
				<table>
					<thead>
						<tr>
							<th>NDC Code</th>
							<th>Labeler</th>
							<th>Non-Proprietary Name</th>
							<th>Proprietary Name</th>
							<th>Package Description</th>
							<th>Dosage Form</th>
							<th>Route of Admin</th>
							<th>Product NDC Code</th>
							<th>Strength Number</th>
							<th>Strength Unit</th>
							<th>Product Type</th>
							<th></th>
						</tr>
					</thead>
					
					<g:each var="product" in="${search.results }" status="status">
						<tr class="${status%2?'even':'odd' }">
							<td>
								<g:link controller="rxNorm" action="getCode" id="${product.ndcCode }">${product.ndcCode }</g:link>
							</td>
							<td>
								${product.labelerName }
							</td>
							<td>
								${product.nonProprietaryName }
							</td>
							<td>
								${product.proprietaryName }
							</td>
							<td>
								<label>${product.packageDescription }</label>
							</td>					
							<td>	
								${product.dosageForm }
							</td>
							<td>	
								${product.route }
							</td>
							<td>
								<g:link controller="rxNorm" action="lookupProducts" params="[searchTerms:product.productNdcCode]">${product.productNdcCode }</g:link>
							</td>
							<td>
								${product.strengthNumber }
							</td>
							<td>
								${product.strengthUnit }
							</td>
							<td>
								${product.productType }
							</td>
							<td>
								<g:link controller="rxNorm" action="createProduct" id="${product.ndcCode}">
									<img src='${fam.icon(name: 'add')}'/>						
								</g:link>
							</td>
							
						</tr>				
					</g:each>
				</table>
			</g:if>

		</div>
	</div>

</body>

</html>