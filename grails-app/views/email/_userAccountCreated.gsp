<%@ page contentType="text/html"%>
<g:applyLayout name="email">
    <div class="box">
        <h2><g:message code="auth.email.newUserAccountCreated.message"/></h2>
        <div style="margin: 5px;">
            <div class="lead">
                ${warehouse.message(code: 'email.userAccountCreated.message', args: [userInstance.username])}
                <g:link controller="user" action="show" id="${userInstance?.id }" absolute="true" class="button">
                    ${warehouse.message(code: 'default.button.show.label')}
                </g:link>
                <g:link controller="user" action="delete" id="${userInstance?.id }" absolute="true" class="button">
                    ${warehouse.message(code: 'default.button.delete.label')}
                </g:link>
            </div>
            ${data}

            <table style="width:auto">
                <tbody>
                <tr class="">
                    <td class="right" width="150px">
                        <label><warehouse:message code="user.active.label" default="Active" /></label>
                    </td>
                    <td class="value">
                        ${userInstance?.active}
                    </td>
                </tr>
                <tr class="">
                    <td class="right" width="15%">
                        <label><warehouse:message code="user.name.label" default="Name" /></label>
                    </td>
                    <td class="value">
                        ${userInstance?.firstName} ${userInstance?.lastName}
                    </td>
                </tr>
                <tr class="">
                    <td class="name">
                        <label><warehouse:message code="user.email.label" default="Email" /></label>
                    </td>
                    <td class="value">
                        ${userInstance?.email}
                    </td>
                </tr>
                <tr class="">
                    <td class="name">
                        <label><warehouse:message code="user.roles.label" default="Roles" /></label>
                    </td>
                    <td class="value">
                        ${userInstance?.roles}
                    </td>
                </tr>                <tr class="">
                    <td class="name">
                        <label><warehouse:message code="default.locale.label"/></label>
                    </td>
                    <td class="value">
                        ${userInstance?.locale}
                    </td>
                </tr>

                <tr class="">
                    <td valign="top" class="name">
                        <label><warehouse:message
                                code="default.timezone.label" default="Timezone" /></label></td>
                    <td valign="top" class="value">
                        ${userInstance?.timezone}
                    </td>
                </tr>
                <g:if test="${grailsApplication.config.openboxes.signup.additionalQuestions.enabled}">
                    <g:each var="question" in="${grailsApplication.config.openboxes.signup.additionalQuestions.content}">
                        <tr class="">
                            <td class="name">
                                <label>${question?.label}</label>
                            </td>
                            <td class="value">
                                ${additionalQuestions[question?.id]}
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                <tr class="">
                    <td class="name">
                        <label><warehouse:message
                                code="default.comments.label" default="Comments" /></label>
                    </td>
                    <td valign="top">
                        ${additionalQuestions?.comments}
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</g:applyLayout>
