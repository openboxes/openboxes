<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Product')}" />
	<title><warehouse:message code="default.search.label" args="[entityName]" /></title>
</head>
<body>

	<div class="body">
		<h1>Search RxNorm Display Names </h1>
		<div class="box center">
			<g:form action="lookupDisplayNames" method="get">
				<%-- 
				<g:textField name="q" class="text" size="60" value="${params.q}"/>
				--%>
				<g:autoSuggest id="q" name="q" size="60" jsonUrl="${request.contextPath}/json/findRxNormDisplayNames" 
					width="200" valueId="" valueName=""/>
																
				<g:submitButton name="Search"/>
			</g:form>
		</div>
		<div class="results">
			<table>
				<thead>
					<tr>
						<th>Terms</th>
					</tr>
				</thead>
				<g:each var="term" in="${terms }" status="status">
					<tr class="${status%2?'even':'odd' }">
						<td>
							${term }
						</td>
						
					</tr>				
				</g:each>
			</table>

		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
    		$("#q-suggest").focus();
		});

	</script>
</body>

</html>