<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom">
</head>
<body>
	<div class="body">	
	
		<g:render template="actions"/>

		<div class="dialog">
			<table>
				<g:each in="${issue.keySet() }" var="key">
					<tr class="prop">
						<td class="name">
							${key }
						</td>
						<td class="value">
							${issue[key] }
						</td>
					</tr>				
				</g:each>
			</table>			
		</div>
	</div>
</body>

</html>

