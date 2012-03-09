<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom">
</head>
<body>
	<div class="body">	
	
	
		<g:render template="actions"/>
	
		<div class="dialog">
			<g:if test="${serverInfo }">		
				<table>
					<g:each var="field" in="${serverInfo.keySet() }">
						<tr>
							<td>			
								${field.key }
							</td>
							<td>			
								${field.value }
							</td>
						</tr>
					</g:each>
				</table>
			</g:if>
			
			
		</div>
	</div>
</body>

</html>

