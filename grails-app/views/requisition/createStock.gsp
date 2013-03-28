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


			<g:form name="requisitionForm" method="post" action="saveStock">
                <g:hiddenField name="status" value="${org.pih.warehouse.requisition.RequisitionStatus.CREATED}"/>

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
                                    <label for="commodityClass">
                                        <warehouse:message code="requisition.commodityClass.label" />
                                    </label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'commodityClass', 'errors')}">
                                    <g:selectCommodityClass name="commodityClass" value="${requisition?.commodityClass}" noSelection="['':'']"/>
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
                                            <g:selectWardOrPharmacy name="origin.id" value="${requisition?.origin?.id}"
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
                                        <g:selectDepot name="origin.id" value="${requisition?.origin?.id}"
                                                       noSelection="['null':'']"/>
                                    </td>
                                </tr>
                            </g:elseif>
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
                                    <g:selectPerson name="requestedBy.id" value="${requisition?.requestedBy?.id}"
                                        noSelection="['null':'']"/>


                                </td>
                            </tr>
                            <g:if test="${requisition.isDepotRequisition()}">
                                <tr>
                                    <td class="name"><label><warehouse:message
                                                code="requisition.program.label" /></label></td>
                                    <td class="value">

                                    </td>
                                </tr>
                            </g:if>

                            <tr class="prop">
                                <td class="name">
                                    <label for="destination.id">
                                        <warehouse:message code="requisition.destination.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:hiddenField name="destination.id" value="${session?.warehouse?.id}"/>
                                    ${session?.warehouse?.name }
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
                                    <g:textArea name="description" cols="80" rows="5"
                                        placeholder="${warehouse.message(code:'requisition.description.message')}"
                                        class="text">${requisition.description }</g:textArea>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="description">
                                        <warehouse:message code="default.description.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <table>
                                        <tr>
                                            <th>
                                                ${warehouse.message(code: 'product.label')}
                                            </th>
                                            <th>
                                                ${warehouse.message(code: 'requisitionItem.quantity.label')}
                                            </th>
                                        </tr>
                                        <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">
                                            <tr>
                                                <td>
                                                    <g:hiddenField name="requisitionItems[${i}].product.id" value="${requisitionItem?.product?.id}"/>
                                                    ${requisitionItem?.product}
                                                </td>
                                                <td>
                                                    <g:hiddenField name="requisitionItems[${i}].quantity" value="${requisitionItem?.quantity}"/>
                                                    ${requisitionItem?.quantity}
                                                </td>
                                            </tr>
                                        </g:each>
                                    </table>
                                </td>



                        </tbody>
                    </table>

				</div>
				<div class="buttons">
                    <g:link controller="requisition" action="chooseTemplate" class="button">
                        ${warehouse.message(code:'default.button.back.label', default: 'Back')}
                    </g:link>
                    <button class="button" name="next">${warehouse.message(code:'default.button.next.label', default: 'Next') }</button>

                    <%--
                    <button class="button" name="save">${warehouse.message(code:'default.button.save.label', default: 'Save') }</button>
                    --%>
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
