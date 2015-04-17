<g:if test="${location?.logo }">
    <a href="${createLink(uri: '/dashboard/index')}">
        <img class="logo" src="${createLink(controller:'location', action:'viewLogo', id:location?.id)}" class="middle" />
    </a>
</g:if>
<g:else>
    <a href="${createLink(uri: '/dashboard/index')}">
        <span class="middle"><warehouse:message code="default.openboxes.label"/></span></a>
</g:else>