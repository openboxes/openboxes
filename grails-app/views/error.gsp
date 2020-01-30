<html>
  <head>
	  <title>General Error</title>
	  <meta name="layout" content="custom" />
	  <style type="text/css">
	  		.message {
	  			border: 1px solid black;
	  			padding: 5px;
	  			background-color:#E9E9E9;
	  		}
	  		.stack {
	  			border: 1px solid black;
	  			padding: 5px;
	  			overflow:auto;
	  			height: 300px;
	  		}
	  		.snippet {
	  			padding: 5px;
	  			background-color:white;
	  			border:1px solid black;
	  			margin:3px;
	  			font-family:courier;
	  		}
	  		.dialog {
	  			display: none;
	  		}
	  </style>
  </head>

  <body>

  	<div class="" style="padding: 10px;">


        <button class="open-dialog">
            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'bug.png')}" style="vertical-align: middle" />&nbsp;
            <warehouse:message code="default.reportAsBug.label"/>
            &nbsp;
        </button>
        &nbsp;
        <button class="go-back">
            <img src="${createLinkTo(dir: 'images/icons/silk', file: 'reload.png')}" style="vertical-align: middle" />&nbsp;
        <warehouse:message code="default.ignoreError.label"/>
        &nbsp;
        </button>
	</div>

    <h2>Error Details</h2>
  	<div class="message">
		<strong>Error ${request?.'javax.servlet.error.status_code'}:</strong>
    		${request?.'javax.servlet.error.message'?.encodeAsHTML()}<br/>
		<strong>Servlet:</strong> ${request?.'javax.servlet.error.servlet_name'}<br/>
		<strong>URI:</strong> ${request?.'javax.servlet.error.request_uri'}<br/>
		<g:if test="${exception}">
	  		<strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
	  		<strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
	  		<strong>Class:</strong> ${exception.className} <br />
	  		<strong>At Line:</strong> [${exception.lineNumber}] <br />
	  		<strong>Code Snippet:</strong><br />
	  		<div class="snippet">
	  			<g:each var="cs" in="${exception.codeSnippet}">
	  				${cs?.encodeAsHTML()}<br />
	  			</g:each>
	  		</div>
		</g:if>
  	</div>
	<g:if test="${exception}">
	    <h2>Stack Trace</h2>
	    <div class="stack">
	      <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
	    </div>
	</g:if>
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
                <g:set var="absoluteTargetUri" value="${g.createLinkTo(url: targetUri, absolute: true) }"/>
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

                    <g:hiddenField name="stacktrace" value="${org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(exception)}"/>

                    <tr class="prop">
                        <td class="name"></td>
                        <td class="value">
                            <button class="button icon mail">
                                <warehouse:message code="default.button.submit.label"/>
                            </button>
                            &nbsp;
                            <button class="btn-close-dialog button icon remove">
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
