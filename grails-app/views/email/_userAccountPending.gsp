<%@ page contentType="text/html"%>
<g:applyLayout name="email">


    <div class="box">
        <h2>${warehouse.message(code:'default.summary.label', default:'Summary') }</h2>
        <div style="margin: 10px;">

            ${warehouse.message(code: 'email.userAccountPending.message', args: [userInstance.username])}
            <g:link controller="user" action="show" id="${userInstance?.id }" absolute="true">
                ${warehouse.message(code: 'email.link.label', args: [userInstance?.username])}
            </g:link>

        </div>
    </div>

</g:applyLayout>
