<%@ page
	import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
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
		                	
	
			<g:form name="requisitionForm" method="post" action="saveHeader">
			
				<g:hiddenField name="id" value="${requisition?.id }"/>
			
				<div id="requisition-header-details" class="dialog ui-validation expandable box">
                    <h2>
                        ${warehouse.message(code: 'requisition.editHeader.label', default: 'Edit requisition header')}

                        <div class="right">
                            <g:if test="${requisition?.id }">
                                <g:link controller="requisition" action="edit" id="${requisition?.id }">
                                    <warehouse:message code="default.button.cancel.label"/>
                                </g:link>

                            </g:if>
                        </div>


                    </h2>



                    <div class="yui-g">
						<div class="yui-u first">
                            <table id="requisition-header-details-table" class="header-summary-table">
                                <tbody>
                                    <tr class="prop">
                                        <td class="name"><label><warehouse:message
                                                code="requisition.requisitionNumber.label" /></label></td>
                                        <td class="value">
                                            ${requisition.requestNumber}
                                        </td>
                                    </tr>
                                    <g:isUserAdmin>
                                        <tr class="prop">
                                            <td class="name"><label><warehouse:message
                                                    code="requisition.status.label" /></label></td>
                                            <td class="value">
                                                <g:selectRequisitionStatus name="status" value="${requisition.status}"/>
                                            </td>
                                        </tr>
                                    </g:isUserAdmin>
                                    <tr class="prop">
                                        <td class="name"><label><warehouse:message
                                                    code="requisition.requisitionType.label" /></label></td>
                                        <td class="value">
                                           <g:selectRequisitionType name="type" value="${requisition?.type }" noSelection="['':'']" class="chzn-select-deselect"/>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name"><label><warehouse:message
                                                    code="requisition.commodityClass.label" /></label></td>
                                        <td class="value">
                                            <g:selectCommodityClass name="commodityClass" value="${requisition?.commodityClass }" noSelection="['':'']" class="chzn-select-deselect"/>
                                        </td>
                                    </tr>
                                    <g:if test="${requisition.isWardRequisition()}">
                                        <tr class="prop">
                                            <td class="name">
                                                <label for="origin.id">
                                                    <warehouse:message code="requisition.wardOrPharmacy.label" />
                                                </label>
                                            </td>
                                            <td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
                                                <g:selectWardOrPharmacy name="origin.id" value="${requisition?.origin?.id}" class="chzn-select-deselect"
                                                                        noSelection="['null':'']"/>
                                            </td>
                                        </tr>
                                    </g:if>
                                    <g:elseif test="${requisition.isDepotRequisition()}">
                                        <tr class="prop">
                                            <td class="name">
                                                <label for="origin.id">
                                                    <warehouse:message code="requisition.depot.label" />
                                                </label>
                                            </td>
                                            <td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
                                                <g:selectDepot name="origin.id" value="${requisition?.origin?.id}" class="chzn-select-deselect"
                                                               noSelection="['null':'']"/>
                                            </td>
                                        </tr>
                                    </g:elseif>
                                    <tr class="prop">
                                        <td class="name"><label><warehouse:message
                                                    code="requisition.requestedBy.label" /></label></td>
                                        <td class="value">

                                            <%--
                                            <g:autoSuggest name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName"
                                                width="200" valueId="${requisition?.requestedBy?.id }" valueName="${requisition?.requestedBy?.name }" styleClass="text"/>
                                            --%>
                                            <g:selectPerson name="requestedBy" value="${requisition?.requestedBy}"
                                                            noSelection="['null':'']" size="40"/>

                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="yui-u">

                            <table>
                                <tbody>
                                    <tr class="prop">
                                        <td class="name">
                                            <label for="destination.id">
                                                <warehouse:message code="requisition.destination.label" />
                                            </label>
                                        </td>
                                        <td class="value">
                                            ${requisition?.destination?.name}
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name">
                                            <label><warehouse:message
                                                    code="requisition.processedBy.label" /></label>
                                        </td>
                                        <td class="value">
                                            ${requisition?.createdBy?.name }
                                        </td>


                                    </tr>
                                    <g:if test="${requisition.isDepotRequisition()}">
                                        <tr>
                                            <td class="name"><label><warehouse:message
                                                        code="requisition.program.label" /></label></td>
                                            <td class="value">
                                                <input id="recipientProgram"
                                                    name="recipientProgram" class="autocomplete text" size="60"
                                                    placeholder="${warehouse.message(code:'requisition.program.label')}"
                                                    data-bind="autocomplete: {source: '${request.contextPath }/json/findPrograms'}, value: requisition.recipientProgram" />
                                            </td>
                                        </tr>
                                    </g:if>
                                    <tr class="prop">
                                        <td class="name">
                                            <label><warehouse:message
                                                    code="requisition.dateRequested.label" /></label></td>
                                        <td class="value">
                                            <g:jqueryDatePicker id="dateRequested" name="dateRequested"
                                                                value="${requisition?.dateRequested}" format="MM/dd/yyyy"/>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name"><label><warehouse:message code="requisition.requestedDeliveryDate.label" /></label></td>
                                        <td class="value">

                                            <g:jqueryDatePicker id="requestedDeliveryDate" name="requestedDeliveryDate"
                                                                value="${requisition?.requestedDeliveryDate}" format="MM/dd/yyyy"/>
                                        </td>
                                    </tr>


                                    <tr class="prop">
                                        <td class="name">
                                            <label for="description">
                                                <warehouse:message code="default.description.label" />
                                            </label>
                                        </td>

                                        <td class="value">

                                            <g:textArea name="description" cols="60" rows="5"
                                                placeholder="${warehouse.message(code:'requisition.description.message')}"
                                                class="text">${requisition.description }</g:textArea>
                                        </td>
                                    </tr>

                                </tbody>
                            </table>
                        </div>
                    </div>
				</div>
					
				<div class="buttons">

                    <g:link controller="requisition" action="${requisition?.id ? 'show': 'list'}" id="${requisition?.id }" class="button">
                        <warehouse:message code="default.button.back.label"/>
                    </g:link>
					<button class="button" name="saveAndContinue">${warehouse.message(code:'default.button.next.label', default: 'Next') }</button>

                    <%--
					&nbsp;
					<g:link controller="requisition" action="edit" id="${requisition?.id }">
						<warehouse:message code="default.button.cancel.label"/>
					</g:link>
                    --%>

				</div>					
			</g:form>
		</div>
	</div>
</body>
</html>
