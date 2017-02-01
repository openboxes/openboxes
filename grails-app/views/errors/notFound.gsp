<html>
<head>
	
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="custom" />
	<g:set var="entityName" value="${warehouse.message(code: 'notFound.label', default: 'Page Not Found')}" />
	<title><warehouse:message code="notFound.label" default="Page Not Found"/></title>
	<!-- Specify content to overload like global navigation links, page titles, etc. -->
	<content tag="title"><warehouse:message code="notFound.label" default="Page Not Found"/></content>
    <script src="${resource(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
	
	
</head>

<body>
	<div class="body">
        <div class="summary">
            <span class="title">
                <img src="${resource(dir: 'images/icons/silk', file: 'error.png')}" style="vertical-align: middle"/>
                <warehouse:message code="notFound.label" default="Page Not Found"/></span>
            <div class="right">
                <button class="button open-dialog">
                    <img src="${resource(dir: 'images/icons/silk', file: 'bug.png')}" />
                <warehouse:message code="default.reportAsBug.label"/></button>
                <button class="button go-back">
                    <img src="${resource(dir: 'images/icons/silk', file: 'arrow_turn_left.png')}" />
                    <warehouse:message code="default.ignoreError.label"/></button>
            </div>
        </div>


        <g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>

        <div>

            <table style="width:auto;">
                <tr>
                    <td>
                        <div style="">
                            <img src="${resource(dir:'images',file:'jgreenspan.jpg')}"/>
                        </div>

                    </td>
                    <td>
                        <div class="triangle-isosceles">
                            <warehouse:message code="errors.notFound.message"/>
                        </div>
                    </td>
                </tr>
            </table>
        </div>
	</div>

<g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + (request.queryString?'?':'') + (request.queryString?:'') }"/>
<div id="error-dialog" class="dialog" title="Report a Bug" style="display: none;">
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
                            Support <a href="mailto:${recipients.join(";")}" target="_blank">${recipients.join(";")}</a>
                        </g:if>
                        <g:else>
                            OpenBoxes Support <a href="mailto:errors@openboxes.com" target="_blank">errors@openboxes.com</a>
                        </g:else>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="error.reportedBy.label"/></label>
                    </td>
                    <td class="value">
                        ${session?.user?.name }
                        <a href="mailto:${session?.user?.email }" target="_blank">${session?.user?.email }</a>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="error.summary.label"/></label>
                    </td>
                    <td class="value">
                        <g:textField name="summary" class="text" size="80"
                                     value="${warehouse.message(code:'errors.notFound')} ${targetUri}" placeholder="${warehouse.message(code:'error.summary.message') }" />
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
                    <td class="name">

                    </td>
                    <td class="value">
                        <g:checkBox name="ccMe" value="${true }" />&nbsp;
                        <warehouse:message code="default.reportCcMe.label" /> <span class="fade">${session?.user?.email}</span>
                    </td>
                </tr>
                <g:hiddenField name="clickstream" value="${util.ClickstreamUtil.getClickstreamAsString(session.clickstream)}"/>

                <%--
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="error.clickstream.label" default="Clickstream"/></label>
                    </td>
                    <td class="value">
                        <g:if test="${session.clickstream}">
                            <g:textArea id="clickstream" name="clickstream" cols="120" rows="5"
                                    placeholder="${warehouse.message(code:'error.clickstream.message')}">${util.ClickstreamUtil.getClickstreamAsString(session.clickstream)}</g:textArea>
                        </g:if>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="name">
                        <label><warehouse:message code="error.stacktrace.label" default="Stacktrace"/></label>
                    </td></div>
                    <td>
                        <g:if test="${exception}">
                            <g:textArea name="stacktrace" cols="120" rows="10" readonly="readonly"><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}</g:each></g:textArea>
                        </g:if>
                    </td>
                </tr>
                --%>
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
        <div class="fade center">
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
        var dom = document.getElementsByTagName('html')[0].innerHTML;
        dom = "<html>" + dom + "</html>"
        $("#dom").val(dom);

        $("#error-dialog").dialog({
            autoOpen: true,
            modal: true,
            width: 800
        });

        $("#comments").focus();


    });
    $(".close-dialog").click(function(event) {
        event.preventDefault();
        $("#error-dialog").dialog("close");
    });

</script>

	<script>
		//$(function() {
			//$('.nailthumb-container img').nailthumb({width : 100, height : 100});
		//});
	</script>	
</body>