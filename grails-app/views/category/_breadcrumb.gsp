<g:set var="parents" value="${categoryInstance?.parents }"/>
<g:each var="parent" in="${parents}" status="status">
	<g:link action="browse" params="[categoryId:parent.id]">${parent?.name }</g:link>
	<g:if test="${parents?.size()-1 > status }">&rsaquo;</g:if>
</g:each>
