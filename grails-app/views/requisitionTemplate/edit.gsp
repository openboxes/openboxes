<%@ page import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
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


            <div class="buttonBar">
                <g:link class="button icon log" controller="requisitionTemplate" action="list"><warehouse:message code="default.list.label" args="[warehouse.message(code:'requisitionTemplates.label').toLowerCase()]"/></g:link>
                <g:isUserAdmin>
                    <g:link class="button icon add" controller="requisitionTemplate" action="create" params="[type:'WARD_STOCK']"><warehouse:message code="default.add.label" args="[warehouse.message(code:'requisitionTemplate.label').toLowerCase()]"/></g:link>
                </g:isUserAdmin>
            </div>
			<g:form name="requisitionForm" method="post" action="update">
                <g:hiddenField name="id" value="${requisition.id}"/>
                <g:hiddenField name="version" value="${requisition.version}"/>

				<div id="requisition-template-details" class="dialog ui-validation box">

                    <h2>${warehouse.message(code:'requisitionTemplate.label')}</h2>

                    <table id="requisition-template-table">

                        <tbody>
                            <tr class="prop">
                                <td class="name">
                                    <label for="destination.id">
                                        <warehouse:message code="requisition.destination.label" />
                                    </label>
                                </td>
                                <td class="value">
                                    <g:hiddenField name="destination.id" value="${requisition?.destination?.id?:session?.warehouse?.id}"/>
                                    ${requisition?.destination?.name?:session?.warehouse?.name }
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
                                    <g:selectCommodityClass name="commodityClass" value="${requisition?.commodityClass}" noSelection="['null':'']" class="chzn-select-deselect"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="origin.id">
                                        <warehouse:message code="requisition.requestingLocation.label" />
                                    </label>
                                </td>
                                <td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
                                    <g:selectWardOrPharmacy name="origin.id" value="${requisition?.origin?.id}" noSelection="['null':'']"  class="chzn-select-deselect"/>
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
        <div class="yui-u">


            <div class="box">
                <h2>${warehouse.message(code:'requisitionTemplate.addRequisitionItems.label', default: "Add multiple requisition items by product code")}</h2>

                <g:form method="post" controller="requisitionTemplate" action="addToRequisitionItems">
                    <g:hiddenField name="id" value="${requisition?.id}" />
                    <g:hiddenField name="version" value="${requisition?.version}" />
                    <table>
                        <tr>
                            <td>
                                <g:textField id="productCodesInput" name="multipleProductCodes" value=""/>
                            </td>
                        </tr>
                        <tr>
                            <td class="right">
                                <button class="button icon add">
                                    ${warehouse.message(code:'requisitionTemplate.addToProducts.label', default: 'Add to products')}
                                </button>
                            </td>
                        </tr>
                    </table>
                </g:form>


            </div>


            <div class="box">
                <h2>${warehouse.message(code:'requisitionTemplate.requisitionItems.label')}</h2>
                <%--
                <div class="center">
                    <g:form controller="requisitionTemplate" action="addToRequisitionItems">
                        <g:hiddenField name="id" value="${requisition.id}"/>

                        <g:textArea name="multipleProductCodes" cols="75" rows="3"
                                    placeholder="${warehouse.message(code:'requisitionTemplate.enterProductCodes.message', default:'Enter multiple product codes separated by commas')}"></g:textArea>

                        <button class="button" id="add-requisition-items"><warehouse:message code="default.button.add.label"/></button>
                    </g:form>
                </div>
                --%>

                <g:form name="requisitionItemForm" method="post" action="update">


                    <g:hiddenField name="id" value="${requisition.id}"/>
                    <g:hiddenField name="version" value="${requisition.version}"/>

                    <div>
                        <table id="stock-requisition-items" class="sortable" data-update-url="${createLink(controller:'json', action:'sortRequisitionItems')}">
                            <thead>
                                <tr>
                                    <th></th>
                                    <th><warehouse:message code="row.label" default="#"/></th>
                                    <th><warehouse:message code="product.label"/></th>
                                    <th><warehouse:message code="default.quantity.label"/></th>
                                    <th><warehouse:message code="unitOfMeasure.label"/></th>
                                    <th><warehouse:message code="default.actions.label"/></th>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each var="requisitionItem" in="${requisition?.requisitionItems.sort()}" status="i">
                                    <tr class="prop ${i%2?'even':'odd'}" id="requisitionItem_${requisitionItem?.id }" requisitionItem="${requisitionItem?.id}">
                                        <td>
                                            <span class="sorthandle"></span>
                                        </td>
                                        <td class="center">
                                            ${i+1}
                                        </td>
                                        <td>
                                            <g:hiddenField name="requisitionItems[${i}].product.id" value="${requisitionItem?.product?.id}"/>
                                            <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                                                ${requisitionItem?.product?.productCode} ${requisitionItem?.product?.name}
                                            </g:link>
                                        </td>
                                        <td>
                                            <g:textField name="requisitionItems[${i}].quantity" value="${requisitionItem?.quantity}" class="text" size="6"/>
                                        </td>
                                        <td>
                                            <g:selectUnitOfMeasure name="requisitionItems[${i}].productPackage.id"
                                                product="${requisitionItem?.product}" value="${requisitionItem?.productPackage?.id}"/>
                                        </td>
                                        <td>
                                            <g:link controller="requisitionTemplate" action="removeFromRequisitionItems" id="${requisition?.id}"
                                                    params="['requisitionItem.id':requisitionItem?.id]" class="button icon trash">
                                                ${warehouse.message(code:'default.button.delete.label')}
                                            </g:link>
                                        </td>
                                    </tr>
                                </g:each>
                                <g:unless test="${requisition?.requisitionItems}">
                                    <tr>
                                        <td colspan="6" class="center">
                                            <span class="fade empty">${warehouse.message(code: "requisition.noRequisitionItems.message")}</span>
                                        </td>

                                    </tr>
                                </g:unless>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td>

                                    </td>
                                    <td>

                                    </td>
                                    <td>
                                        <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                                       width="350" styleClass="text"/>
                                    </td>
                                    <td>
                                        <g:textField name="quantity" value="" class="text" size="6"/>
                                    </td>
                                    <td class="center">
                                        EA/1
                                    </td>
                                    <td>
                                        <button class="button icon add" id="add-requisition-item"><warehouse:message code="default.button.add.label"/></button>

                                    </td>

                                </tr>
                                <tr>
                                    <td colspan="7">
                                        <div class="buttons">
                                            <button class="button" name="save">${warehouse.message(code:'default.button.save.label', default: 'Save') }</button>
                                            &nbsp;
                                            <g:link controller="requisitionTemplate" action="list">
                                                <warehouse:message code="default.button.cancel.label"/>
                                            </g:link>
                                        </div>
                                    </td>
                                </tr>


                            </tfoot>
                        </table>
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
                //axis : "y",
                //helper: "clone",
                forcePlaceholderSize: true,
                placeholder: "ui-state-highlight",
                //connectWith: ".connectable",
                update : function() {
                    var updateUrl = "${createLink(controller:'json', action:'sortRequisitionItems') }";
                    var sortOrder = $(this).sortable('serialize');
                    $.post(updateUrl, sortOrder);
                    $(".sortable tbody tr").removeClass("odd").removeClass("even").filter(":odd").addClass("odd")
                            .filter(":even").addClass("even");
                }
            });

            /*
            //$('.selectable').selectable();
            $('.draggable').draggable({
                handle		: ".draghandle",
                helper		: "clone",
                //helper		: function( event ) { return $("<div class='ui-widget-header'>I'm a custom helper</div>"); },
                revert		: true,
                zIndex		: 2700,
                autoSize	: true,
                ghosting	: true,
                onStart		: function ( event ) { alert("started") },
                onStop		: function() { $('.droppable').each(function() { this.expanded = false; }); }
            });
            */
            /*
            $('.droppable').droppable( {
                accept: '.draggable',
                tolerance: 'intersect',
                //greedy: true,
                over: function(event, ui) {
                    $( this ).addClass( "ui-state-highlight" );
                },
                out: function(event, ui) {
                    $( this ).removeClass( "ui-state-highlight" );
                },
                drop: function( event, ui ) {
                    //alert("dropped");
                    //ui.draggable.hide();
                    ui.draggable.addClass( "strikethrough" );
                    $( this ).removeClass( "ui-state-highlight" );
                    var requisitionItem = ui.draggable.attr("data-requisitionItem");
                    var container = $(this).attr("container");
                    $("#shipmentItemRow-" + shipmentItem).hide();
                    moveShipmentItemToContainer(shipmentItem, container);
                    window.location.reload();
                    //alert("Move item " + shipmentItem + " to container " + container);
                }
            });
            */


        });
    </script>
</body>
</html>
