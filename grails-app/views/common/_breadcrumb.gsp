<!-- Add breadcrumb -->
<span class="menuButton">
	<a class="home" href="${createLink(uri: '/home/index')}">Home</a>
	&raquo;
	<g:if test="${session.warehouse != null}">
		<a class="none" href="${createLink(uri: '/warehouse/show/' + session.warehouse?.id)}">${session.warehouse?.name}</a>
	</g:if>
	<g:else>(warehouse not selected)</g:else>
	&raquo;
</span>
