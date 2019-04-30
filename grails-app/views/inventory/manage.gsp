<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="inventory.manage.label" default="Browse inventory"/></title>
        <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
        <link href="//cdnjs.cloudflare.com/ajax/libs/x-editable/1.5.0/jqueryui-editable/css/jqueryui-editable.css" rel="stylesheet"/>
        <style>
        .dataTable > tr > td { white-space: nowrap; }
        </style>
    </head>
    <body>
        <div class="body">

            <div class="summary">
                <div class="title">
                    <warehouse:message code="inventory.manage.label" default="Manage inventory"/>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${command}">
                <div class="errors">
                    <g:renderErrors bean="${command}" as="list" />
                </div>
            </g:hasErrors>


            <div class="dialog">
                <div class="yui-ga">
					<div class="yui-u first">
%{--                        <div class="filters" >--}%
%{--                            <g:form method="GET" controller="inventory" action="manage">--}%
%{--                                <div class="box">--}%
%{--                                    <h2><warehouse:message code="inventory.filterByProduct.label"/></h2>--}%
%{--                                    <table>--}%
%{--                                        <tr>--}%
%{--                                            <td>--}%
%{--                                                <label><g:message code="product.tag.label"/></label>--}%
%{--                                                <g:selectTags name="tags" noSelection="['':'']"--}%
%{--                                                              value="${command.tags}"--}%
%{--                                                              data-placeholder="Select tags"--}%
%{--                                                              class="chzn-select-deselect"/>--}%
%{--                                            </td>--}%
%{--                                        </tr>--}%
%{--                                        <tr>--}%
%{--                                            <td>--}%
%{--                                                <label><g:message code="product.productCode.label"/></label>--}%
%{--                                                <g:textField id="productCodes" name="productCodes"--}%
%{--                                                             value="${command?.productCodes}" placeholder="Add products by product code"/>--}%
%{--                                            </td>--}%
%{--                                        </tr>--}%
%{--                                        <tr>--}%
%{--                                            <td>--}%
%{--                                                <div class="buttons">--}%
%{--                                                    <button type="submit" class="button icon search" name="searchPerformed" value="true">--}%
%{--                                                        <warehouse:message code="default.search.label"/>--}%
%{--                                                    </button>--}%
%{--                                                </div>--}%
%{--                                            </td>--}%
%{--                                        </tr>--}%
%{--                                    </table>--}%
%{--                                </div>--}%


%{--                            </g:form>--}%
%{--                        </div>--}%
                    </div>
					<div class="yui-u">




                        <form method="POST" action="saveInventoryChanges">

                            <div class="box dialog">
                                <h2><warehouse:message code="inventory.manage.label" default="Manage inventory"/></h2>
                                <table class="dataTable">
                                    <thead>
                                        <tr>
                                            <th class="middle" style="width: 1%">
                                                <g:message code="product.productCode.label"/>
                                            </th>
                                            <th class="middle">
                                                <g:message code="product.name.label"/>
                                            </th>
                                            <th class="middle">
                                                <g:message code="location.binLocation.label"/>
                                            </th>
                                            <th class="middle">
                                                <g:message code="inventoryItem.lotNumber.label"/>
                                            </th>
                                            <th class="middle">
                                                <g:message code="inventoryItem.expirationDate.label"/>
                                            </th>
                                            <th class="middle">
                                                <g:message code="default.quantityOnHand.label"/>
                                            </th>
                                            <th class="middle">
                                                <g:message code="default.quantityOnHand.label"/>
                                            </th>
                                            <th>
                                                <g:message code="default.reasonCode.label" default="Reason Code"/>
                                            </th>
                                        </tr>
                                    </thead>
                                    <tfoot>
                                    <tr>
                                        <th class="middle" style="width: 1%">
                                            <g:message code="product.productCode.label"/>
                                        </th>
                                        <th class="middle">
                                            <g:message code="product.name.label"/>
                                        </th>
                                        <th class="middle">
                                            <g:message code="location.binLocation.label"/>
                                        </th>
                                        <th class="middle">
                                            <g:message code="inventoryItem.lotNumber.label"/>
                                        </th>
                                        <th class="middle">
                                            <g:message code="inventoryItem.expirationDate.label"/>
                                        </th>
                                        <th class="middle">
                                            <g:message code="default.quantityOnHand.label"/>
                                        </th>
                                        <th class="middle">
                                            <g:message code="default.quantityOnHand.label"/>
                                        </th>
                                        <th>
                                            <g:message code="default.reasonCode.label" default="Reason Code"/>
                                        </th>
                                    </tr>
                                    </tfoot>

                                </table>

                                <div class="buttons">
                                    <button class="button"><g:message code="default.button.save.label"/></button>
                                </div>

                            </div>

                        </form>
					</div>
				</div>
			</div>
		</div>
        <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
        <script src="${createLinkTo(dir:'js/jquery.tagcloud', file:'jquery.tagcloud.js')}" type="text/javascript" ></script>
        <script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/x-editable/1.5.0/jqueryui-editable/js/jqueryui-editable.min.js"></script>

        <script>
			$(document).ready(function() {

			    $(".dataTable").dataTable({
                    "bJQueryUI": true,
                    // "sPaginationType": "full_numbers",
                    // "iDisplayLength": 10,
                    "bProcessing": true,
                    "sAjaxSource": "${request.contextPath}/inventory/binLocations",
                    "bDeferRender": true,
                    "bScrollInfinite": true,
                    "bScrollCollapse": true,
                    "sScrollY": "400px",
                    "fnDrawCallback": function () {
                        %{--$('.dataTable tbody td').editable('${request.contextPath}/inventory/saveBinLocation', {--}%
                        %{--    "callback": function( sValue, y ) {--}%
                        %{--        /* Redraw the table from the new data on the server */--}%
                        %{--        oTable.fnDraw();--}%
                        %{--    },--}%
                        %{--    "height": "14px"--}%
                        %{--} );--}%
                    }
                });

                $(".tabs").tabs({
                    cookie : {
                        expires : 1
                    }
                });

                $('#productCodes').tagsInput({
                    'autocomplete_url':'${createLink(controller: 'json', action: 'findProductCodes')}',
                    'width': 'auto',
                    'height': 'auto',
                    'placeholder':'test',
                    'removeWithBackspace' : true
                });
			});	
		</script>
    </body>
</html>
