<!-- Add breadcrumb -->
<span class="menuButton">
	<a class="home" href="${createLink(uri: '/home/index')}">Home</a>
	&raquo;
	<g:if test="${session?.warehouse}">
		<a class="building" href="${createLink(uri: '/warehouse/show/' + session.warehouse?.id)}">${session.warehouse?.name}</a>
		&raquo;
	</g:if>
	<g:else>(unknown warehouse)</g:else>
</span>
