<%@ page contentType="text/html"%>
<g:applyLayout name="email">

    <div class="box">
        <h2><g:message code="auth.email.newUserAccountCreated.message"/></h2>
        <div style="margin: 10px;">

            <p>

                ${warehouse.message(code: 'email.userAccountCreated.message', args: [userInstance.username])}
                <g:link controller="user" action="show" id="${userInstance?.id }" absolute="true">
                    ${warehouse.message(code: 'email.link.label', args: [userInstance?.username])}
                </g:link>
            </p>
            <table style="width: auto;">
                <tbody>
                <tr class="prop">
                    <td class="name">
                        <label for="firstName"><warehouse:message code="user.firstName.label" default="First Name" /></label>
                    </td>
                    <td class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
                        ${userInstance?.firstName}
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="lastName"><warehouse:message code="user.lastName.label" default="Last Name" /></label>
                    </td>
                    <td class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
                        ${userInstance?.lastName}
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="email"><warehouse:message code="user.email.label" default="Email" /></label>
                    </td>
                    <td class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
                        ${userInstance?.email}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="locale"><warehouse:message code="default.locale.label"/></label>
                    </td>
                    <td class="value">
                        <div style="width: 235px">
                            ${userInstance?.locale}
                        </div>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="locale"><warehouse:message
                                code="default.timezone.label" default="Timezone" /></label></td>
                    <td valign="top" class="value">
                        ${userInstance?.timezone}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="interest"><warehouse:message code="user.interest.label" default="Interest" /></label>
                    </td>
                    <td class="value">
                        ${params.interest}
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label for="comments"><warehouse:message
                                code="default.comments.label" default="Comments" /></label>
                    </td>
                    <td valign="top">
                        ${params?.comments}

                    </td>
                </tr>

                </tbody>
            </table>



        </div>
    </div>

	
</g:applyLayout>
