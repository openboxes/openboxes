<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom">
</head>
<body>
	<div class="body">	
	
	
		<g:render template="actions"/>
	
		<div class="dialog">
			<g:if test="${issues }">		
				<h1>Issues</h1>
				<table>
					<g:each var="issue" in="${issues }">
						<tr>
							<td>			
								${issue }
							</td>
						</tr>
					</g:each>
				</table>
			</g:if>
			
			
		</div>
	</div>
</body>

</html>

