<g:if test="${userInstance}">
    <div class="${(userInstance?.active) ? 'active':'inactive'} summary">
        <table>
            <tr>
                <td class="top" width="1%">
                    <g:render template="actions"/>
                </td>
                <td width="1%">
                    <div class="nailthumb-container">
                        <g:userPhoto user="${userInstance}"/>
                    </div>
                </td>
                <td>
                    <div class="title">
                        <g:link action="edit" id="${userInstance?.id}">
                            ${fieldValue(bean: userInstance, field: "firstName")} ${fieldValue(bean: userInstance, field: "lastName")}
                        </g:link>
                    </div>
                </td>
                <td class="top right">
                    <div class="right">
                        <span class="tag">
                            ${userInstance?.active ? warehouse.message(code:'user.active.label') : warehouse.message(code:'user.inactive.label')}
                        </span>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</g:if>
<g:else>
    <div class="summary">
        <div class="title"><g:message code="users.label"/></div>
    </div>
</g:else>
<div class="button-bar">
    <g:link class="button" action="list" controller="user">
        <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}"/>
        <g:message code="default.list.label" args="[g.message(code: 'users.label')]" />
    </g:link>
    <g:link class="button" action="create" controller="user">
        <img src="${createLinkTo(dir:'images/icons/silk',file:'user_add.png')}"/>
        <g:message code="default.create.label" args="[g.message(code: 'user.label')]" />
    </g:link>
    <g:if test="${userInstance}">
        <g:link class="button" action="edit" controller="user" id="${userInstance?.id}">
            <img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}"/>
            <g:message code="default.edit.label" args="[g.message(code: 'user.label')]" />
        </g:link>
    </g:if>
    <g:isSuperuser>
        <g:if test="${userInstance && userInstance?.active}">
            <g:link class="button" action="impersonate" controller="user" id="${userInstance?.id}" target="_blank">
                <img src="${createLinkTo(dir:'images/icons/silk',file:'arrow_switch.png')}"/>
                <g:message code="user.impersonate.label" args="[g.message(code: 'user.label')]" />
            </g:link>
        </g:if>
    </g:isSuperuser>
</div>


<script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
<script>
    $(document).ready(function() {
        $('.nailthumb-container').nailthumb({ width: 24, height: 24 });
    });
</script>
