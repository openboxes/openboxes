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
		<div class="yui-u first">


			<g:form name="requisitionForm" method="post" action="save" useToken="true">
                <g:hiddenField name="status" value="${org.pih.warehouse.requisition.RequisitionStatus.CREATED}"/>

				<div id="requisition-template-details" class="dialog ui-validation box">
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
                    <g:else>
                        <h2>${warehouse.message(code: 'requisition.details.label', default: 'Requisition details') }</h2>
                    </g:else>
						
                    <table id="requisition-template-table">

                        <tbody>
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
                                    <g:hiddenField name="origin.id" value="${session?.warehouse?.id}"/>
                                    ${session?.warehouse?.name }
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="destination.id">
                                        <warehouse:message code="requisition.destination.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:selectLocation name="destination.id" value="${requisition?.destination?.id}" class="chzn-select-deselect"
                                                      noSelection="['null':'']"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="commodityClass">
                                        <warehouse:message code="requisition.commodityClass.label" />
                                    </label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'commodityClass', 'errors')}">
                                    <g:selectCommodityClass name="commodityClass" value="${requisition?.commodityClass}"
                                                            class="chzn-select-deselect"
                                                            noSelection="['null':'']"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message
                                            code="requisition.requestedBy.label" /></label>
                                </td>
                                <td class="value">
                                    <%--
                                    <g:hiddenField name="requestedBy.id" value="${requisition?.requestedBy?.id?:session?.user?.id }"/>
                                    ${requisition?.requestedBy?.name?:session?.user?.name }
                                    --%>
                                    <g:selectPerson name="requestedBy" value="${requisition?.requestedBy?.id}" size="60"
                                        noSelection="['null':'']" class="chzn-select-deselect"/>


                                </td>
                            </tr>
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
                                <td class="name">
                                    <label><warehouse:message
                                            code="requisition.createdBy.label" /></label>
                                </td>
                                <td class="value">
                                    <g:hiddenField name="createdBy.id" value="${requisition?.createdBy?.id?:session?.user?.id }"/>
                                    ${requisition?.createdBy?.name?:session?.user?.name }
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="description">
                                        <warehouse:message code="default.description.label" />
                                    </label>
                                </td>

                                <td class="value">
                                    <g:textArea name="description" cols="80" rows="2" style="width:100%"
                                        placeholder="${warehouse.message(code:'requisition.description.message')}"
                                        class="text large">${requisition.description }</g:textArea>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="requisitionItems">
                                        <warehouse:message code="requisition.requisitionItems.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <table id="requisition-item-template-table">
                                        <tbody>
                                            <tr>
                                                <td >
                                                    <table >
                                                        <tr>
                                                            <th>
                                                                ${warehouse.message(code: 'product.productCode.label')}
                                                            </th>
                                                            <th>
                                                                ${warehouse.message(code: 'product.label')}
                                                            </th>
                                                            <th>
                                                                ${warehouse.message(code: 'inventoryLevel.maxQuantity.label')}
                                                            </th>
                                                            <th>
                                                                ${warehouse.message(code: 'requisitionItem.quantity.label')}
                                                            </th>
                                                            <th>
                                                                ${warehouse.message(code: 'requisitionItem.productPackage.label')}
                                                            </th>
                                                            <th>
                                                                ${warehouse.message(code: 'requisitionItem.orderIndex.label', default: 'Sort order')}
                                                            </th>
                                                        </tr>
                                                        <g:each var="requisitionItem" in="${requisition?.requisitionItems?.sort()}" status="i">
                                                            <tr class="${i%2?'even':'odd'}">
                                                                <td>
                                                                    ${requisitionItem?.product?.productCode}
                                                                </td>
                                                                <td>
                                                                    <g:hiddenField name="requisitionItems[${i}].product.id" value="${requisitionItem?.product?.id}"/>
                                                                    <format:product product="${requisitionItem?.product}"/>
                                                                </td>
                                                                <td>
                                                                    ${requisitionItem?.quantity}
                                                                </td>
                                                                <td>
                                                                    <g:textField name="requisitionItems[${i}].quantity" value="${requisitionItem?.quantity}" class="large text" size="5"/>
                                                                </td>
                                                                <td>
                                                                    EA/1
                                                                </td>
                                                                <td>
                                                                    <g:hiddenField name="requisitionItems[${i}].orderIndex" value="${requisitionItem?.orderIndex}"/>
                                                                    ${requisitionItem?.orderIndex}
                                                                </td>
                                                            </tr>
                                                        </g:each>
                                                    </table>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
				<div class="buttons">
                    <g:link controller="requisition" action="chooseTemplate" class="button">
                        ${warehouse.message(code:'default.button.back.label', default: 'Back')}
                    </g:link>
                    <button class="button" name="next">${warehouse.message(code:'default.button.next.label', default: 'Next') }</button>

				</div>
			</g:form>
		</div>
	</div>
</body>
</html>
