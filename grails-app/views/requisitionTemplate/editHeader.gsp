<%@ page import="org.pih.warehouse.requisition.RequisitionItemSortByCode; grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
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
	<g:hasErrors bean="${requisition}">
		<div class="errors">
			<g:renderErrors bean="${requisition}" as="list" />
		</div>
	</g:hasErrors>
    <%--
	<g:render template="summary" model="[requisition:requisition]"/>
    --%>

    <g:render template="summary" model="[requisition:requisition]"/>

    <div class="yui-gf">
		<div class="yui-u first">
            <g:render template="header" model="[requisition:requisition]"/>

        </div>
        <div class="yui-u">
			<g:form name="requisitionForm" method="post" action="update">
                <g:hiddenField name="id" value="${requisition.id}"/>
                <g:hiddenField name="version" value="${requisition.version}"/>
                <g:hiddenField name="viewName" value="editHeader"/>
                <g:hiddenField name="type" value="${requisition.type}"/>
                <g:hiddenField name="isPublished" value="${requisition.isPublished}"/>
                <g:hiddenField name="commodityClass" value="${requisition.commodityClass}"/>
                <g:hiddenField name="createdBy.id" value="${requisition?.createdBy?.id?:session?.user?.id }"/>
                <g:hiddenField name="updatedBy" value="${requisition?.updatedBy?.id?:session?.user?.id }"/>

                <div id="requisition-template-details" class="dialog ui-validation box">

                    <h2>${warehouse.message(code:'requisitionTemplate.label', default: 'Stock list')}</h2>

                    <table id="requisition-template-table">

                        <tbody>


                            <tr class="prop">
                                <td class="name">
                                    <label for="name">
                                        <warehouse:message code="default.name.label" />
                                    </label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${requisition.name}" class="text large" size="80"/>
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
                                                      noSelection="['null':'']"  class="chzn-select-deselect"/>
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
                                                           noSelection="['null':'']"  class="chzn-select-deselect"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label><warehouse:message
                                            code="requisitionTemplate.requestedBy.label" /></label>
                                </td>
                                <td class="value">
                                    <g:selectPerson id="requestedBy" name="requestedBy" value="${requisition?.requestedBy?.id}"
                                                    class="chzn-select-deselect"/>
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
                                    <label for="sortByCode">
                                        <warehouse:message code="requisition.sortByCode.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:select id="sortByCode" name="sortByCode" class="chzn-select-deselect"
                                              from="${RequisitionItemSortByCode.list()}"
                                              optionValue="friendlyName" value="${requisition?.sortByCode}"
                                              noSelection="['null':'']"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="description">
                                        <warehouse:message code="default.description.label" />
                                    </label>
                                </td>

                                <td class="value">
                                    <g:textArea name="description" rows="5" style="width: 100%"
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

	</div>
    <script>
        $(document).ready(function() {
            $("#product-suggest").focus();
            $("#add-requisition-item").click(function(event) {
                event.preventDefault();
                var productId = $("#product-value").val();
                var requisitionId = $("#id").val();
                var quantity = $("#quantity").val();
                console.log(productId);
                console.log(requisitionId);

                var jsonData = { "productId": productId, "requisitionId": requisitionId, "quantity": quantity }
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

            $("#selectAllProducts").click(function(event) {
                var checked = ($(this).attr("checked") == 'checked');
                $("input.select-product[type='checkbox']").attr("checked", checked);
            });

            $('#productCodesInput').tagsInput({
                'autocomplete_url':'${createLink(controller: 'json', action: 'findProductCodes')}',
                'defaultText': '...',
                'width': 'auto',
                'height': 'auto',
                'removeWithBackspace' : true
            });


            $(".sortable tbody").sortable({
                handle : '.sorthandle',
                forcePlaceholderSize: true,
                placeholder: "ui-state-highlight",
                update : function() {
                    var updateUrl = "${createLink(controller:'json', action:'sortRequisitionItems') }";
                    var sortOrder = $(this).sortable('serialize');
                    $.post(updateUrl, sortOrder);
                    $(".sortable tbody tr").removeClass("odd").removeClass("even").filter(":odd").addClass("odd")
                            .filter(":even").addClass("even");
                }
            });
        });
    </script>
</body>
</html>
