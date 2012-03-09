<div class="right">
	<span class="linkButton">
		<g:link class="home" action="index">Home</g:link>
	</span>			
	<g:if test="${session.issues }">
		<span class="linkButton">
			<g:link class="list" action="showIssues">Back to Issues (${session.issues.size() } issues)</g:link>
		</span>			
	</g:if>
	<span class="linkButton">
		<g:link class="new" action="createIssue">Create Issue</g:link>
	</span>			
	<span class="linkButton">			
		<g:link class="view" action="searchIssues">New Search</g:link>	
	</span>
	<span class="linkButton">
		<g:link class="bullet" action="serverInfo">Server Info</g:link>
	</span>			
	<span class="linkButton">
		<g:link class="bullet" action="showVersions">Show Versions</g:link>
	</span>
	
</div>
<br clear="all"/>