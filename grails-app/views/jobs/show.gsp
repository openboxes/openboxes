<%@ page import="org.quartz.impl.triggers.CronTriggerImpl" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <title><warehouse:message code="users.label" /></title>
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>


    <div class="buttonBar">
        <g:link class="button icon settings" controller="admin" action="showSettings" fragment="tab-5">${g.message(code:'admin.backToSettings.label', default: 'Back to Settings')}</g:link>
        <g:link class="button icon log" action="list"><warehouse:message code="default.list.label" args="[g.message(code:'jobKeys.label', default: 'Background Jobs')]"/></g:link>
    </div>

    <div class="yui-ga">
        <div class="yui-u first">


            <div class="box dialog">
                <h2>${jobDetail.name}</h2>
                <table>
                    <g:each in="${jobDetail.properties}" var="property">
                        <tr class="prop">
                            <td class="name">${property.key}</td>
                            <td class="value">${property.value}</td>
                        </tr>
                    </g:each>
                    <tr class="prop">
                        <td class="name">Triggers</td>
                        <td class="value">
                            <table>
                                <tr>
                                    <th>ID</th>
                                    <th>Summary</th>
                                    <th>Previous fire time</th>
                                    <th>Next fire time</th>
                                    <th>Actions</th>

                                </tr>
                                <g:each var="trigger" in="${triggers}">
                                    <tr>
                                        <td>${trigger.key}</td>
                                        <td>
                                            <g:if test="${trigger instanceof org.quartz.impl.triggers.CronTriggerImpl}">
                                                ${trigger?.cronExpression}
                                                (${trigger?.expressionSummary})

                                            </g:if>
                                            <g:else>
                                                ${trigger.properties}
                                            </g:else>

                                        </td>
                                        <td>${trigger.previousFireTime}</td>
                                        <td>${trigger.nextFireTime}</td>
                                        <td>
                                            <g:link class="button" controller="jobs" action="unscheduleTrigger" id="${trigger.name}">Delete</g:link>

                                        </td>
                                    </tr>
                                </g:each>
                                <g:unless test="${triggers}">
                                    <tr>
                                        <td colspan="6">
                                            There are no triggers for job ${jobDetail}
                                        </td>
                                    </tr>
                                </g:unless>
                            </table>

                            <div class="button-bar">
                                <g:form action="scheduleJob">
                                    <g:textField name="id" class="text" size="40" value="${jobDetail.name}"/>
                                    <g:textField name="cronExpression" size="20" class="text" placeholder="0 0 22 * * ?"/>
                                    <g:submitButton class="button" name="Add trigger"></g:submitButton>
                                </g:form>

                            </div>

                        </td>
                    </tr>
                </table>

            </div>

        </div>
    </div>
</div>

</body>
</html>
