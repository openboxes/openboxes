<g:if test="${userInstance.photo}">
    <g:link action="viewPhoto" id="${userInstance?.id }">
        <img
                src="${createLink(controller:'user', action:'viewThumb', id:userInstance.id)}"
                style="vertical-align: middle" />
    </g:link>
</g:if>
<g:else>
    <g:if test="${userInstance?.active}">
        <img class="photo" src="${resource(dir: 'images/icons/user', file: 'default-avatar.jpg') }"
             style="vertical-align: bottom;" />

    </g:if>
    <g:else>
        <img class="photo" src="${resource(dir: 'images/icons/user', file: 'default-avatar.jpg') }"
             style="vertical-align: bottom;" />
    </g:else>
</g:else>
