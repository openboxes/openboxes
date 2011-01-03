<g:set var="parents" value="${categoryInstance?.parents }"/>
<g:each var="parent" in="${parents}" status="status">
	${parent?.name } 
	<g:if test="${parents?.size()-1 > status }">&rsaquo;</g:if>
</g:each>
