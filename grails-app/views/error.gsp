<!doctype html>
<html>
    <head>
        <title>General Error</title>
        <meta name="layout" content="custom">
        <asset:stylesheet src="errors.css"/>
        <asset:stylesheet src="main.css"/>
        <style>
            /* Fix for some CSS incompatibilities between openboxes and Grails */
            h2 { vertical-align: bottom; line-height: 2.5em; margin: 0; margin-top: 1em;}
            .snippet { margin: 0 }
            .snippet .line { line-height: 1.5em }
            .snippet > .line.error { padding: 0 }
            .error { border: 0; margin: 0 }
            .stack { margin: 0; height: 300px; }
            .error-details { margin: 1em }
        </style>
    </head>
    <body>
        <div class="body">
            <div class="button-container button-bar">
                <g:link controller="dashboard" action="index" onClick="javascript:window.history.go(-1);" class="button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'reverse_green.png')}" />&nbsp;
                    <g:message code="default.ignoreError.label"/>&nbsp;
                </g:link>
                <button class="open-dialog button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'bug.png')}" />&nbsp;
                    <warehouse:message code="default.reportAsBug.label"/>
                </button>
            </div>

            <div class="yui-ga">
                <div class="yui-u first">
                    <div class="box content">
                        <g:if test="${Throwable.isInstance(exception)}">
                            <g:renderException exception="${exception}" />
                        </g:if>
                        <g:elseif test="${request.getAttribute('javax.servlet.error.exception')}">
                            <g:renderException exception="${request.getAttribute('javax.servlet.error.exception')}" />
                        </g:elseif>
                        <g:else>
                            <ul class="errors">
                                <li>An error has occurred</li>
                                <li>Exception: ${exception}</li>
                                <li>Message: ${message}</li>
                                <li>Path: ${path}</li>
                            </ul>
                        </g:else>
                    </div>
                </div>
            </div>
        </div>

        <g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + (request.queryString?'?':'') + (request.queryString?:'') }"/>
	    <div id="error-dialog" class="dialog" title="Report a Bug">
            <g:if test="${util.ConfigHelper.booleanValue(grailsApplication.config.openboxes.mail.errors.enabled)}">
                <div id="errors"></div>
                <g:form controller="errors" action="processError" onsubmit="return checkFormSubmission();">
                    <g:hiddenField id="dom" name="dom" value=""/>
                    <g:hiddenField name="reportedBy" value="${session?.user?.username}"/>
                    <g:hiddenField name="targetUri" value="${targetUri}"/>
                    <g:hiddenField name="request.statusCode" value="${request?.'javax.servlet.error.status_code'}"/>
                    <g:hiddenField name="request.errorMessage" value="${request?.'javax.servlet.error.message'?.encodeAsHTML()}"/>
                    <g:hiddenField name="exception.message" value="${exception?.message?.encodeAsHTML()}"/>
                    <g:hiddenField name="exception.class" value="${exception?.className}"/>
                    <g:hiddenField name="exception.date" value="${new Date() }"/>
                    <g:set var="absoluteTargetUri" value="${g.resource(url: targetUri, absolute: true) }"/>
                    <g:hiddenField name="absoluteTargetUri" value="${absoluteTargetUri}"/>
                    <g:set var="summary" value="${exception?.cause?.class?.name?:exception?.className}: ${exception?.cause?.message?.encodeAsHTML()}"/>
                    <table>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="error.reportedTo.label"/></label>
                            </td>
                            <td class="value">
                                <g:set var="recipients" value="${util.ConfigHelper.listValue(grailsApplication.config.openboxes.mail.errors.recipients)}"/>
                                <g:if test="${recipients}">
                                    ${recipients.join(";")}
                                </g:if>
                                <g:else>
                                    errors@openboxes.com
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="error.reportedBy.label"/></label>
                            </td>
                            <td class="value">
                                <g:if test="${session.user}">
                                    ${session?.user?.name }
                                    <a href="mailto:${session?.user?.email }" target="_blank">${session?.user?.email }</a>

                                    <g:if test="${session.user}">
                                        <g:checkBox name="ccMe" value="${true }" />&nbsp;
                                        <warehouse:message code="default.reportCcMe.label" />
                                    </g:if>

                                </g:if>
                                <g:else>
                                    ${grailsApplication.config.grails.mail.from}
                                </g:else>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="error.summary.label"/></label>
                            </td>
                            <td class="value">
                                <g:textField name="summary" class="text" size="80"
                                    value="${exception?.message}" placeholder="${warehouse.message(code:'error.summary.message') }"/>
                                <br/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label><warehouse:message code="error.details.label"/></label>
                            </td>
                            <td class="value">
                                <g:textArea id="comments" name="comments" cols="80" rows="10"
                                    placeholder="${warehouse.message(code:'error.details.message')}"></g:textArea>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name"></td>
                            <td class="value">
                                <button class="button icon mail">
                                    <warehouse:message code="default.button.submit.label"/>
                                </button>
                                &nbsp;
                                <button class="close-dialog button icon remove">
                                    <warehouse:message code="default.button.close.label"/>
                                </button>
                            </td>
                        </tr>
                    </table>
                </g:form>
            </g:if>
            <g:else>
                <div class="empty fade center">
                    ${warehouse.message(code: 'email.errorReportDisabled.message')}
                </div>
            </g:else>
	    </div>

        <script>


            function checkFormSubmission() {
                var comments = $("#comments").val();
                console.log(comments);
                if (!comments) {
                    $("#errors").html("<li>Please describe your bug, including steps to reproduce and any other information you can gather.</li>").addClass("errors");
                    return false;
                }
                return true;
            }

            $("#error-dialog").hide();

            $(".go-back").click(function() {
                parent.history.back();
                return false;
            });
            $(".open-dialog").click(function() {
                $("#error-dialog").dialog({
                    autoOpen: true,
                    modal: true,
                    width: '1000px'
                });

                $("#comments").focus();


            });
            $(".close-dialog").click(function(event) {
                event.preventDefault();
                $("#error-dialog").dialog("close");
            });

        </script>
    </body>
</html>
