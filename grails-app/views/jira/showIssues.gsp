<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom">
</head>
<body>
	<div class="body">	
	
		<g:render template="actions"/>

		<g:if test="${errors }">
			<div class="errors">
				<ul>
					<li>${errors }</li>
				</ul>
			</div>
		
		</g:if>

		<g:render template="searchBox"/>
		
		<div class="dialog">
			<g:if test="${issues }">		
				<table>
					<tr>
						<th>Key</th>
						<th>Summary</th>
						<th>Created</th>
						<th>Updated</th>
					</tr>				
					<g:each var="issue" in="${issues }" status="i">
						<tr class="${i%2?'odd':'even' }">
							<td>
								<g:link action="showIssue" id="${issue.key }">
								${issue.key }
								</g:link>
							</td>
							<td>
								${issue.summary }
							</td>
							<td>
								${issue.created }
							</td>
							<td>
								${issue.updated }
							</td>
						</tr>
					</g:each>
				</table>
			</g:if>			
		</div>
	</div>
</body>

</html>

