<div class="box center">
	<g:form action="searchIssues" method="GET">
		<g:textField name="text" value="${params.text }" size="60"/>
		<g:select from="${session.statuses }" name="status"/>
		<g:submitButton name="query"/>
	</g:form>
	
	${session.statuses }
</div>
