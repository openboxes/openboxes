<g:set var="breadcrumbs" value="${categoryInstance?.parents }"/>
<g:if test="${breadcrumbs}">
	<g:each var="breadcrumb" in="${breadcrumbs}" status="status">
		<g:if test="${breadcrumb?.name != 'ROOT' }">
			${breadcrumb?.name}
			<g:if test="${breadcrumbs?.size()-1 > status }">&rsaquo;</g:if>
		</g:if>	
	</g:each>
</g:if>
<g:else>
	Uncategorized
</g:else>

