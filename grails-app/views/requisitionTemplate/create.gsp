<%@ page import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta name="layout" content="custom" />
<g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
<title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
<script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'knockout_binding.js')}" type="text/javascript"></script>
<script src="${createLinkTo(dir:'js/', file:'requisition.js')}" type="text/javascript"></script>
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


        <div class="yui-u">


			<g:form name="requisitionForm" method="post" action="save">
                <g:hiddenField name="isTemplate" value="${requisition.isTemplate}"/>
                <g:hiddenField name="createdBy.id" value="${requisition?.createdBy?.id?:session?.user?.id }"/>


				<g:if test="${requisition?.id }">
					<div class="box">
						<a class="toggle" href="javascript:void(0);">
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'section_collapsed.png')}" style="vertical-align: bottom;"/>
						</a>
						<h3 style="display: inline" class="toggle">${requisition?.requestNumber } ${requisition?.name }</h3>				
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


                <div id="requisition-template-details" class="dialog ui-validation box">
                    <h2>${warehouse.message(code:'requisitionTemplate.label')}</h2>

                    <table id="requisition-template-table">

                        <tbody>
                            <tr class="prop">
                                <td class="name">
                                    <label for="type">
                                        <warehouse:message code="requisition.requisitionType.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <%--
                                    <g:selectRequisitionType name="type" value="${requisition.type}"
                                                             class="chzn-select-deselect"/>
                                                             --%>
                                    <g:hiddenField name="type" value="${requisition?.type}"/>
                                    <format:metadata obj="${requisition?.type}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="name">
                                        <warehouse:message code="default.name.label" />
                                    </label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${requisition.name}" class="text" size="80"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message
                                            code="requisitionTemplate.requestedBy.label" /></label>
                                </td>
                                <td class="value">
                                    <g:autoSuggest id="requestedBy" name="requestedBy" jsonUrl="${request.contextPath }/json/findPersonByName"
                                                   valueId="${requisition?.requestedBy?.id}"
                                                   valueName="${requisition?.requestedBy?.name}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="origin.id">
                                        <warehouse:message code="requisition.origin.label" />
                                    </label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
                                    <g:selectLocation name="origin.id" value="${requisition?.origin?.id}"
                                                      class="chzn-select-deselect" noSelection="['null':'']"/>
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
                                    <label for="replenishmentPeriod">
                                        <warehouse:message code="requisition.replenishmentPeriod.label" />
                                        <small>(${warehouse.message(code:'requisitionTemplate.replenishmentPeriodUnit.label')})</small>
                                    </label>
                                </td>
                                <td class="value">
                                    <g:textField name="replenishmentPeriod" value="${requisition.replenishmentPeriod}" class="text large" size="80"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="commodityClass">
                                        <warehouse:message code="requisition.commodityClass.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:selectCommodityClass name="commodityClass" class="chzn-select-deselect"
                                                            value="${requisition?.commodityClass}" noSelection="['':'']"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="description">
                                        <warehouse:message code="default.description.label" />
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
