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
    <style>
    .sortable { list-style-type: none; margin: 0; padding: 0; width: 100%; }
    .sortable tr { margin: 0 5px 5px 5px; padding: 5px; font-size: 1.2em; height: 1.5em; }
    html>body .sortable li { height: 1.5em; line-height: 1.2em; }
    .ui-state-highlight { height: 1.5em; line-height: 1.2em; }
    </style>
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

<div class="yui-gd">
    <div class="yui-u first">
        <g:render template="header" model="[requisition:requisition]"/>

    </div>
    <div class="yui-u">
        <div class="box">
            <h2>${warehouse.message(code:'requisitionTemplate.addRequisitionItems.label', default: "Add multiple requisition items by product code")}</h2>

            <g:form method="post" controller="requisitionTemplate" action="addToRequisitionItems">
                <g:hiddenField name="id" value="${requisition?.id}" />
                <g:hiddenField name="version" value="${requisition?.version}" />
                <table>
                    <tr>
                        <td width="75%">
                            <g:textField id="productCodesInput" name="multipleProductCodes" value="" class="text large"/>
                        </td>
                        <td class="left">
                            <button class="button icon add">
                                ${warehouse.message(code:'requisitionTemplate.addToProducts.label', default: 'Add to products')}
                            </button>
                        </td>
                    </tr>
                </table>
            </g:form>


        </div>


        <div class="box">
            <h2>
                ${warehouse.message(code:'requisitionTemplate.requisitionItems.label')}
                <div class="button-group right">
                    <g:link controller="requisitionTemplate" action="changeSortOrderAlpha" class="button" id="${requisition.id}">
                        <warehouse:message code="requisitionTemplate.button.sortAlphabetically.label" default="Sort alphabetically"/>
                    </g:link>
                    <g:link controller="requisitionTemplate" action="changeSortOrderChrono" class="button" id="${requisition.id}">
                        <warehouse:message code="requisitionTemplate.button.sortChronologically.label" default="Sort chronologically"/>
                    </g:link>
                </div>
                <div class="clear"></div>
            </h2>
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



            <g:form name="requisitionItemForm" method="post" controller="requisitionTemplate" action="update">


                <g:hiddenField name="id" value="${requisition.id}"/>
                <g:hiddenField name="version" value="${requisition.version}"/>

                <div>
                    <table class="sortable" data-update-url="${createLink(controller:'json', action:'sortRequisitionItems')}">
                        <thead>
                        <tr>
                            <th></th>
                            <th class="center"><warehouse:message code="product.productCode.label" default="#"/></th>
                            <th><warehouse:message code="product.label"/></th>
                            <th class="center"><warehouse:message code="default.quantity.label"/></th>
                            <th class="center"><warehouse:message code="unitOfMeasure.label"/></th>
                            <th class="center"><warehouse:message code="requisitionItem.sortOrder.label" default="Sort order"/></th>
                            <th><warehouse:message code="default.actions.label"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">
                            <tr class="prop ${i%2?'even':'odd'}" id="requisitionItem_${requisitionItem?.id }" requisitionItem="${requisitionItem?.id}">
                                <td>
                                    <span class="sorthandle"></span>
                                </td>
                                <td class="center">
                                    ${requisitionItem?.product?.productCode}
                                </td>
                                <td>
                                    <g:hiddenField name="requisitionItems[${i}].product.id" value="${requisitionItem?.product?.id}"/>
                                    <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                                        ${requisitionItem?.product?.name}
                                    </g:link>
                                </td>
                                <td class="center">
                                    <g:textField name="requisitionItems[${i}].quantity" value="${requisitionItem?.quantity}" class="text" size="6"/>
                                </td>
                                <td class="center middle">
                                    <g:selectProductPackage name="requisitionItems[${i}].productPackage.id"
                                                           product="${requisitionItem?.product}"
                                                           class="chzn-select-deselect"
                                                           value="${requisitionItem?.productPackage?.id}"/>
                                </td>
                                <td class="center middle">
                                    ${(requisitionItem?.orderIndex?:0)+1}
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
                                <td colspan="2"></td>
                                <td>
                                    <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                                   width="350" styleClass="text"/>
                                </td>
                                <td class="center">
                                    <g:textField name="quantity" value="" class="text" size="6"/>
                                </td>
                                <td class="center">
                                    <g:select name="unitOfMeasure"
                                                       class="chzn-select-deselect"
                                                           from="['EA/1']"/>
                                </td>
                                <td class="center">
                                    <g:set var="orderIndex" value="${requisition.requisitionItems.size()}"/>
                                    <g:hiddenField id="orderIndex" name="orderIndex" value="${orderIndex}"/>
                                    ${orderIndex+1}
                                </td>
                                <td>
                                    <button class="button icon add" id="add-requisition-item"><warehouse:message code="default.button.add.label"/></button>

                                </td>

                            </tr>
                            <tr>
                                <td colspan="7">
                                    <div class="buttons">
                                        <button class="button" name="save">${warehouse.message(code:'default.button.save.label', default: 'Save') }</button>
                                        <g:link controller="requisitionTemplate" action="list" class="button">
                                            <warehouse:message code="default.button.done.label" default="Done"/>
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
            var productId = $("#product-id").val();
            var requisitionId = $("#id").val();
            var quantity = $("#quantity").val();
            var orderIndex = $("#orderIndex").val();
            console.log(productId);
            console.log(requisitionId);

            var params = { "product.id": productId, "requisition.id": requisitionId, "quantity": quantity, "orderIndex": orderIndex }
            console.log(params);
            $.ajax({
                url: "${request.contextPath}/json/addToRequisitionItems",
                type: "get",
                contentType: 'text/json',
                dataType: "json",
                data: params,
                success: function(data) {
                    console.log(data);
                    location.reload();
                },
                error: function(data) {
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
            axis : "y",
            helper: "clone",
            forcePlaceholderSize: true,
            placeholder: "ui-state-highlight",
            //connectWith: ".connectable",
            update : function() {
                var updateUrl = "${createLink(controller:'json', action:'sortRequisitionItems') }";
                var sortOrder = $(this).sortable('serialize');
                $.post(updateUrl, sortOrder);
                //$(".sortable tbody tr").removeClass("odd").removeClass("even").filter(":odd").addClass("odd")
                //        .filter(":even").addClass("even");
                //location.reload();
                refreshTable();
            }
        });
        $( ".sortable" ).disableSelection();
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


    function refreshTable(id) {

    }

</script>
</body>
</html>
