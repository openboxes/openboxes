<%@ page import="org.pih.warehouse.requisition.Requisition" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <script src="${createLinkTo(dir:'js/knockout/', file:'knockout-2.2.0.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'knockout.mapping-latest.js')}" type="text/javascript" ></script>
    <script src="${createLinkTo(dir:'js/', file:'processRequisition.js')}" type="text/javascript" ></script>
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    <!-- Specify content to overload like global navigation links, page titles, etc. -->
    <content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
</head>
<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${requisition}">
        <div class="errors">
            <g:renderErrors bean="${requisition}" as="list" />
        </div>
    </g:hasErrors>

    <div id="requisition-header">
        <div class="title" id="description">${requisition.name ?: warehouse.message(code: 'requisition.label', default: 'Requisition')}</div>
        <g:if test="${requisition.lastUpdated}">
            <div class="time-stamp fade"><g:formatDate date="${requisition.lastUpdated }" format="dd/MMM/yyyy hh:mm a"/></div>
        </g:if>
        <div class="status fade">${requisition.status.toString()}</div>
    </div>

    <g:form name="requisitionForm" method="post" action="save">
        <div class="dialog">
            <table>
                <tbody>
                <tr>
                    <td><span data-bind="text: id"></span>
                        <table>
                            <tbody data-bind="foreach : requisitionItems">
                                <div class="requisition-item">
                                        %{--class="requisitionItem ${i%2?'even':'odd' } accordion"--}%
                                    <tr  style="border-top-width: 1px; border-top-style: solid; border-bottom-width: 1px; border-bottom-style: solid">
                                        <td valign='top' class='value' style="border-left-width: 1px; border-left-style: solid; border-left-color: black">
                                            %{--<label>${i+1}. ${requisitionItem.product?.name}</label>--}%
                                            <span data-bind="text: id"></span>%{--<span data-bind="text: product.name"></span>--}%
                                        </td>
                                        <td valign='top' class='value'>
                                            %{--Requested:<span data-bind="text: quantity"></span>--}%
                                        </td>
                                        <td valign='top' class='value'>
                                            %{--Picked: <span data-bind="text: quantityPicked"></span>--}%
                                        </td>
                                        <td valign='top' class='value'>
                                            %{--Remaining: <span data-bind="text: quantityRemaining"></span>--}%
                                        </td>
                                        <td style="border-right-width: 1px; border-right-style: solid; border-right-color: black">
                                            %{--<div data-bind="class: status"></div>--}%
                                        </td>
                                    </tr>
                                    %{--<g:each var="inventoryItem" in="${requisition?.findExistingInventoryItems()}" status="i">--}%
                                    %{--<tr>--}%
                                    %{--<td>${inventoryItem?.product?.name}</td>--}%
                                    %{--</tr>--}%
                                    %{--</g:each>--}%

                                    %{--<input type="hidden" data-bind="value: id"/>--}%
                                    %{--<label>Product</label>--}%
                                    %{--<input type="hidden" data-bind="value: productId"/>--}%
                                    %{--<input type="text" class="required" data-bind="search_product: {source: '${request.contextPath }/json/searchProduct'}, uniqueName: true"/>--}%
                                    %{--<label>Quantity</label>--}%
                                    %{--<input type="text" class="required number" data-bind="value: quantity,uniqueName: true"/>--}%
                                    %{--<label>Substitutable</label>--}%
                                    %{--<input type="checkbox" data-bind="checked: substitutable"/>--}%
                                    %{--<label>Recipient</label>--}%
                                    %{--<input type="text" data-bind="value: recipient"/>--}%
                                    %{--<span>product:</span><span data-bind="text: productId"/>--}%
                                </div>
                            </tbody>
                        </table>
                    </td>
                    <td>
                        %{--key--}%
                    </td>
                </tr>

                <tr>
                    <td valign="top">
                    </td>
                    <td colspan="5">
                        <div class="buttons right">
                            <button type="submit">
                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'accept.png')}" class="top"/>
                                <g:link action="save" id="${requisition.id}">
                                    <warehouse:message code="default.button.save.label"/>
                                </g:link>
                            </button>
                            &nbsp;
                            <g:link action="list">
                                ${warehouse.message(code: 'default.button.cancel.label')}
                            </g:link>
                        </div>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>


    </g:form>
</div>


%{--<h2>ID: <span data-bind="text: requisition.id"> </span> . </h2>--}%
%{--<h2>ID: <span data-bind="text: requisition.color"> </span> . </h2>--}%


<script type="text/javascript">


    $(function(){
        %{--var requisition = new process_requisition.Requisition("${requisition?.id}", ${requisitionItems});--}%
        %{--console.log("We got some data from the server. " + JSON.stringify(requisition.requisitionItems))--}%
        %{--console.log("Processing requisition " + requisition.id());--}%
        %{--var viewModel = new process_requisition.ViewModel(requisition);--}%
        var viewModel = ko.mapping.fromJS(${serverData});
        ko.applyBindings(viewModel);
    });
</script>

</body>
</html>