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
                    <td>
                        <table border="1" data-bind="foreach : requisitionItems">
                            <tbody class="research">
                                <tr class="accordion">
                                    <td>
                                        <span data-bind="text: rowIndex"></span>. <span data-bind="text: productName"></span>
                                        <span data-bind="text: quantity"></span>
                                        Picked: <span data-bind="text: quantityPicked"></span>
                                        Remaining: <span data-bind="text: quantityRemaining"></span>
                                        <div data-bind="css: status"></div>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="5">
                                        <table>
                                            <tr data-bind="foreach : requisitionItems">

                                            </tr>
                                        </table>
                                    </td>
                                </tr>

                            </tbody>

                        </table>
                    </td>
                    <td width="250">
                        <table border="1">
                            <tr>
                                <td>
                                    <table>
                                        <tr>
                                            <td><div class="Incomplete"></div>Incomplete</td>
                                        </tr>
                                        <tr>
                                            <td><div class="PartiallyComplete"></div>Partially Complete</td>
                                        </tr>
                                        <tr>
                                            <td><div class="Complete"></div>Complete</td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
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

        var mapping = {
            'requisitionItems': {
                create: function(options) {
                    return new RequisitionItem(options.data);
                }
            }
        };

        var RequisitionItem = function(data) {
            ko.mapping.fromJS(data, {}, this);

            this.quantityPicked = ko.computed(function() {
                return 0;
            }, this);
            this.quantityRemaining = ko.computed(function() {
                return this.quantity() - this.quantityPicked();
            }, this);
            this.status = ko.computed(function() {
                if(this.quantityPicked() == 0) return "Incomplete";
                if(this.quantityPicked() >= this.quantity()) return "Complete";
                return "PartiallyComplete";
            }, this);
            this.rowIndex = ko.computed(function() {
                return this.orderIndex() + 1;
            }, this);
        };

        var viewModel = ko.mapping.fromJS(${serverData}, mapping);
        ko.applyBindings(viewModel);

        var $research = $('.research');
        //$research.find("tr").not('.accordion').hide();
        //$research.find("tr").eq(0).show();

        $research.find(".accordion").click(function(){
            $research.find('.accordion').not(this).siblings().fadeOut(200);
            $(this).siblings().fadeToggle(200);
        }).eq(0).trigger('click');

    });
</script>

</body>
</html>