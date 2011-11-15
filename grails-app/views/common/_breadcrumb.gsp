<!-- Add breadcrumb -->
<span class="menuButton">
	<a class="home" href="${createLink(uri: '/home/index')}"><warehouse:message code="default.home.label"/></a>
	&raquo;
	<g:if test="${session?.warehouse}">
		<a class="building" href="${createLink(uri: '${request.contextPath}/show/' + session.warehouse?.id)}">${session.warehouse?.name}</a>
		&raquo;
	</g:if>
	<g:else><warehouse:message code="warehouse.unknown.label"/></g:else>
</span>
