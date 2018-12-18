<%@ page import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta name="layout" content="custom" />
<g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
<title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
</head>
<body>

	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${requisition}">
		<div class="errors">
			<g:renderErrors bean="${requisition}" as="list" />
		</div>
	</g:hasErrors>

    <g:render template="summary" model="[requisition:requisition]"/>

    <div class="yui-ga">
		<div class="yui-u first">
			<g:form name="requisitionForm" method="post" action="save" useToken="true">
                <g:hiddenField name="status" value="${org.pih.warehouse.requisition.RequisitionStatus.CREATED}"/>
                <g:hiddenField name="createdBy.id" value="${requisition?.createdBy?.id?:session?.user?.id }"/>

				<div id="requisition-template-details" class="dialog ui-validation box">

                    <g:if test="${requisition?.id }">
                        <div class="box">
                            <a class="toggle" href="javascript:void(0);">
                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'section_collapsed.png')}" style="vertical-align: bottom;"/>
                            </a>
                            <h2 style="display: inline" class="toggle">${requisition?.requestNumber } ${requisition?.name }</h2>
                            <g:if test="${requisition?.id }">
                                <g:if test="${!params.editHeader }">
                                    <g:link controller="requisition" action="editHeader" id="${requisition?.id }">
                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: bottom;"/>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="requisition" action="edit" id="${requisition?.id }">
                                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png')}" style="vertical-align: bottom;"/>
                                    </g:link>
                                </g:else>
                            </g:if>
                        </div>

                    </g:if>
                    <g:else>
                        <h2>${requisition?.name?:warehouse.message(code: 'requisition.new.label') }</h2>



                    </g:else>
                    <table id="requisition-template-table">

                        <tbody>
                            <%--
                            <tr class="prop">
                                <td class="name">
                                    <label for="type">
                                        <warehouse:message code="requisition.requisitionNumber.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:hiddenField name="requestNumber" value="${requisition.requestNumber}"/>
                                    <format:metadata obj="${requisition.requestNumber}"/>
                                </td>
                            </tr>
                            --%>
                            <tr class="prop">
                                <td class="name">
                                    <label for="type">
                                        <warehouse:message code="requisition.requisitionType.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:hiddenField name="type" value="${requisition.type}"/>
                                    <format:metadata obj="${requisition.type}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="origin.id">
                                        <warehouse:message code="requisition.origin.label" />
                                    </label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
                                    <g:hiddenField name="origin.id" value="${requisition?.origin?.id?:session?.warehouse?.id}"/>
                                    ${requisition?.origin?.name?:session?.warehouse?.name }

                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="destination.id">
                                        <warehouse:message code="requisition.destination.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:selectLocation name="destination.id" value="${requisition?.destination?.id}"
                                                      class="chzn-select-deselect" noSelection="['null':'']"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="commodityClass">
                                        <warehouse:message code="requisition.commodityClass.label" />
                                    </label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'commodityClass', 'errors')}">
                                    <g:selectCommodityClass name="commodityClass" value="${requisition?.commodityClass}" noSelection="['':'']"
                                                            style="width:300px" class="chzn-select-deselect"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message
                                            code="requisition.requestedBy.label" /></label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'requestedBy', 'errors')}">
                                    <%--
                                    <g:hiddenField name="requestedBy.id" value="${requisition?.requestedBy?.id?:session?.user?.id }"/>
                                    ${requisition?.requestedBy?.name?:session?.user?.name }
                                    --%>
                                    <g:selectPerson name="requestedBy" value="${requisition?.requestedBy?.id}"
                                        noSelection="['null':'']" size="40" class="chzn-select-deselect"/>


                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="description">
                                        <warehouse:message code="requisition.description.label" />
                                    </label>
                                </td>

                                <td class="value">
                                    <g:textArea name="description" cols="80" rows="2"
                                        placeholder="${warehouse.message(code:'requisition.description.message')}"
                                        class="text">${requisition.description }</g:textArea>
                                </td>
                            </tr>
                        </tbody>
                    </table>
				</div>
                <div class="buttons center">
                    <button class="button" name="save">${warehouse.message(code:'default.button.save.label', default: 'Save') }</button>
                    &nbsp;
                    <g:link controller="requisitionTemplate" action="list">
                        <warehouse:message code="default.button.cancel.label"/>
                    </g:link>
                </div>
			</g:form>
		</div>
	</div>
</body>
</html>
