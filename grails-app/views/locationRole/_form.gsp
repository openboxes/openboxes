<g:form method="post" controller="user" autocomplete="off">
    <g:hiddenField name="id" value="${locationRoleInstance?.id}" />
    <g:hiddenField name="version" value="${locationRoleInstance?.version}" />
    <g:hiddenField name="user.id" value="${locationRoleInstance?.user?.id}"/>
    <table>
        <tbody>
            <g:if test="${locationRoleInstance?.id}">
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="id"><warehouse:message code="default.id.label" /></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: locationRoleInstance, field: 'id', 'errors')}">
                        ${locationRoleInstance?.id}
                    </td>
                </tr>
            </g:if>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="user.id"><warehouse:message code="locationRole.user.label" default="User" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: locationRoleInstance, field: 'user', 'errors')}">
                    ${locationRoleInstance?.user?.name} ${locationRoleInstance?.user?.username}
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="location.id"><warehouse:message code="locationRole.location.label" default="Location" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: locationRoleInstance, field: 'location', 'errors')}">
                    <g:selectLocation
                            class="chzn-select-deselect"
                            name="location.id"
                            noSelection="['':'']"
                            activityCode="${org.pih.warehouse.core.ActivityCode.MANAGE_INVENTORY}"
                            value="${locationRoleInstance?.location?.id?:session.warehouse.id}"/>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="role.id"><warehouse:message code="locationRole.role.label" default="Role" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: locationRoleInstance, field: 'role', 'errors')}">
                    <g:if test="${!locationRoleInstance?.id}">
                        <g:select class="chzn-select-deselect" name="role.id" from="${org.pih.warehouse.core.Role.list()}" multiple="multiple"
                                  style="height: 30px" placeholder="words Words"
                                  optionKey="id" value="${locationRoleInstance?.role?.id}" noSelection="['':'']" />

                    </g:if>
                    <g:else>
                        <g:select class="chzn-select-deselect" name="role.id" from="${org.pih.warehouse.core.Role.list()}"
                                  optionKey="id" value="${locationRoleInstance?.role?.id}" noSelection="['':'']" />

                    </g:else>
                </td>
            </tr>
        </tbody>
        <tfoot>
            <tr class="prop">
                <td></td>
                <td valign="top">
                    <div class="buttons left">
                        <g:actionSubmit class="button" action="saveLocationRole" value="${warehouse.message(code: 'default.button.save.label', default: 'Save')}" />
                        <g:if test="${locationRoleInstance.id}">
                            <g:actionSubmit class="button" action="deleteLocationRole" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                        </g:if>
                    </div>
                </td>
            </tr>
        </tfoot>
    </table>
</g:form>
