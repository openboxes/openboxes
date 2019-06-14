<%@ page import="org.pih.warehouse.requisition.RequisitionItemSortByCode; grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code='default.button.email.label'/></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
    <script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
</head>
<body>

	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
    <g:if test="${flash.error}">
        <div class="errors">${flash.error}</div>
    </g:if>
    <g:render template="summary" model="[requisition:requisition]"/>

    <div class="yui-gf">
		<div class="yui-u first">
            <g:render template="header" model="[requisition:requisition]"/>

        </div>
        <div class="yui-u">
            <g:form controller="stocklist" action="sendMail" method="POST" enctype="multipart/form-data" useToken="true">
                <div id="requisition-template-details" class="dialog ui-validation box">
                <h2>${warehouse.message(code:'default.button.email.label', default: 'Email')}</h2>
                <table class="table table-bordered">
                    <tbody>
                    <g:hiddenField name="id" value="${requisition?.id}" />
                    <tr class="prop">
                        <td class="name">
                            <label><warehouse:message code="mail.to.label" default="To"/></label>
                        </td>
                        <td class="value">
                            <g:selectRecipient name="recipients" noSelection="['':'']"
                                          value="${requisition?.requestedBy?.email}"
                                          multiple="true"
                                          class="chzn-select-deselect"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <label><warehouse:message code="mail.subject.label" default="Subject"/></label>
                        </td>
                        <td class="value">
                            <g:textField name="subject" value="${params.subject?:warehouse.message(code: 'stockList.emailSubject.label')}" class="text" size="60"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <label><warehouse:message code="mail.message.label" default="Message"/></label>
                        </td>
                        <td class="value">
                            <g:textArea name="body" value="${params?.body?:warehouse.message(code: 'stockList.emailMessage.label')}" class="text" cols="60" rows="10"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="includePdf"><warehouse:message code="stockList.includePdf.label" default="Include PDF document" /></label>
                        </td>
                        <td valign="top" class="value">
                            <g:checkBox name="includePdf" value="${true}" />
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="includeXls"><warehouse:message code="stockList.includeXls.label" default="Include XLS document" /></label>
                        </td>
                        <td valign="top" class="value">
                            <g:checkBox name="includeXls" value="${true}" />
                        </td>
                    </tr>
                    </tbody>
                    <tfoot>
                    <tr>
                        <td colspan="2" class="center">
                            <g:submitButton name="Send" class="button icon email"/>
                            <g:link controller="requisitionTemplate" action="show" id="${requisition?.id}" class="button icon reload">
                                <warehouse:message code="default.button.cancel.label"/>
                            </g:link>
                        </td>
                    </tr>
                    </tfoot>
                </table>
				</div>
			</g:form>
		</div>
	</div>
</body>
</html>
