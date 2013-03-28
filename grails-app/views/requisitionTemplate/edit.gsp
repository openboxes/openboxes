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
    <%--
	<g:render template="summary" model="[requisition:requisition]"/>
    --%>
	<div class="yui-g">
		<div class="yui-u first">
			<g:form name="requisitionForm" method="post" action="update">
                <g:hiddenField name="id" value="${requisition.id}"/>
                <g:hiddenField name="version" value="${requisition.version}"/>

				<div id="requisition-template-details" class="dialog ui-validation box">

                    <table id="requisition-template-table">

                        <tbody>
                            <tr class="prop">
                                <td class="name">
                                    <label for="type">
                                        <warehouse:message code="requisition.isTemplate.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:hiddenField name="isTemplate" value="${requisition.isTemplate}"/>
                                    ${requisition.isTemplate}
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="type">
                                        <warehouse:message code="requisition.isPublished.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:checkBox name="isPublished" value="${requisition.isPublished}"/>
                                    <g:if test="${requisition?.isPublished}">
                                        ${requisition.datePublished}
                                    </g:if>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="type">
                                        <warehouse:message code="requisition.requisitionType.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:hiddenField name="type" value="${requisition.type}"/>
                                    ${requisition.type}
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="commodityClass">
                                        <warehouse:message code="requisition.commodityClass.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:selectCommodityClass name="commodityClass" value="${requisition?.commodityClass}" noSelection="['null':'']"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="origin.id">
                                        <warehouse:message code="requisition.requestingLocation.label" />
                                    </label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
                                    <g:selectWardOrPharmacy name="origin.id" value="${requisition?.origin?.id}"
                                        locationGroup="${session?.warehouse?.locationGroup}" noSelection="['null':'']"/>
                                </td>
                            </tr>
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
                                    <label><warehouse:message
                                            code="requisition.requestedBy.label" /></label>
                                </td>
                                <td class="value">
                                    <g:hiddenField name="requestedBy.id" value="${requisition?.requestedBy?.id?:session?.user?.id }"/>
                                    ${requisition?.requestedBy?.name?:session?.user?.name }
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
                        </tbody>
                    </table>
				</div>
				<div class="buttons">
                    <button class="button" name="save">${warehouse.message(code:'default.button.save.label', default: 'Save') }</button>
                    &nbsp;
                    <g:link controller="requisitionTemplate" action="list">
                        <warehouse:message code="default.button.cancel.label"/>
                    </g:link>
				</div>
			</g:form>
		</div>
        <div class="yui-u">

            <div>

                <div class="center" style="padding: 20px;">
                    <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                   width="400" styleClass="text"/>
                    <button class="button" id="add-requisition-item"><warehouse:message code="default.button.add.label"/></button>
                </div>
                <g:form name="requisitionItemForm" method="post" action="update">

                    <g:hiddenField name="id" value="${requisition.id}"/>
                    <g:hiddenField name="version" value="${requisition.version}"/>

                    <div class="box">
                        <table id="stock-requisition-items">
                            <thead>
                                <tr>
                                    <th><warehouse:message code="product.label"/></th>
                                    <th><warehouse:message code="default.quantity.label"/></th>
                                    <th><warehouse:message code="unitOfMeasure.label"/></th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">
                                    <tr class="prop ${i%2?'even':'odd'}">
                                        <td>
                                            <g:hiddenField name="requisitionItems[${i}].product.id" value="${requisitionItem?.product?.id}"/>
                                            ${requisitionItem?.product?.productCode} ${requisitionItem?.product?.name}
                                        </td>
                                        <td>
                                            <g:textField name="requisitionItems[${i}].quantity" value="${requisitionItem?.quantity}" class="text" size="10"/>
                                        </td>
                                        <td>
                                            <g:selectUnitOfMeasure name="requisitionItems[${i}].unitOfMeasure" product="${requisitionItem?.product}"/>
                                        </td>
                                        <td>
                                            <g:link controller="requisitionTemplate" action="removeFromRequisitionItems" id="${requisition?.id}" params="['requisitionItem.id':requisitionItem?.id]" class="button">
                                                ${warehouse.message(code:'default.button.delete.label')}
                                            </g:link>
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                    <div class="buttons">
                        <button class="button" name="save">${warehouse.message(code:'default.button.save.label', default: 'Save') }</button>
                        &nbsp;
                        <g:link controller="requisitionTemplate" action="list">
                            <warehouse:message code="default.button.cancel.label"/>
                        </g:link>
                    </div>

                </g:form>
            </div>
        </div>
	</div>
    <script>
        $(document).ready(function() {
            $("#product-suggest").focus();
            $("#add-requisition-item").click(function(event) {
                event.preventDefault();
                var productId = $("#product-value").val();
                var requisitionId = $("#id").val();
                console.log(productId);
                console.log(requisitionId);

                var jsonData = { "productId": productId, "requisitionId": requisitionId }
                console.log(jsonData);
                $.ajax({
                    url: "${request.contextPath}/json/addToRequisitionItems",
                    type: "get",
                    contentType: 'text/json',
                    dataType: "json",
                    data: jsonData,
                    success: function(data) {
                        console.log("success");
                        console.log(data);
                        location.reload();
                    },
                    error: function(data) {
                        console.log("error");
                        console.log(data);
                        location.reload();
                    }
                });

            });
        });
    </script>
</body>
</html>
