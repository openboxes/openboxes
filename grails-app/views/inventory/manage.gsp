<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="inventory.manage.label" default="Browse inventory"/></title>
        <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
        <style>
        .dataTable > tr > td { white-space: nowrap; }
        .dataTable tr.even:hover { background-color: #b2d1ff; }
        .dataTable tr.odd:hover { background-color: #b2d1ff; }
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
                    </div>
					<div class="yui-u">

                        <div class="box dialog">
                            <h2><warehouse:message code="inventory.manage.label" default="Manage inventory"/></h2>
                            <table id="manageInventoryTable" class="dataTable">
                                <thead>
                                    <tr>
                                        <th width="1%">
                                            <g:message code="product.productCode.label"/>
                                        </th>
                                        <th width="30%">
                                            <g:message code="product.name.label"/>
                                        </th>
                                        <th width="5%">
                                            <g:message code="location.binLocation.label"/>
                                        </th>
                                        <th width="5%">
                                            <g:message code="inventoryItem.lotNumber.label"/>
                                        </th>
                                        <th width="5%">
                                            <g:message code="inventoryItem.expirationDate.label"/>
                                        </th>
                                        <th width="5%">
                                            <g:message code="default.quantityOnHand.label"/>
                                        </th>
                                    </tr>
                                </thead>
                                <tfoot>
                                <tr>
                                    <th>
                                        <g:message code="product.productCode.label"/>
                                    </th>
                                    <th>
                                        <g:message code="product.name.label"/>
                                    </th>
                                    <th>
                                        <g:message code="location.binLocation.label"/>
                                    </th>
                                    <th>
                                        <g:message code="inventoryItem.lotNumber.label"/>
                                    </th>
                                    <th>
                                        <g:message code="inventoryItem.expirationDate.label"/>
                                    </th>
                                    <th>
                                        <g:message code="default.quantityOnHand.label"/>
                                    </th>
                                </tr>
                                </tfoot>

                            </table>
                        </div>
					</div>
				</div>
			</div>
		</div>
        <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
        <script src="${createLinkTo(dir:'js/jquery.tagcloud', file:'jquery.tagcloud.js')}" type="text/javascript" ></script>
        <script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>

        <script>
			$(document).ready(function() {

			    $(".dataTable").dataTable({
                    "bJQueryUI": true,
                    "iDisplayLength": 100,
                    "bProcessing": true,
                    "sAjaxSource": "${request.contextPath}/inventory/binLocations",
                    "bDeferRender": true,
                    "bSortClasses": false,
                    "bScrollInfinite": true,
                    "bScrollCollapse": true,
                    "sScrollY": 500
                });

                $('#manageInventoryTable tbody').on('click', 'tr', function () {
                    console.log(this);
                    var nTds = $('td', this);
                    var productCode = $(nTds[0]).text();
                    var binLocation = $(nTds[2]).text();
                    var lotNumber = escape($(nTds[3]).html());
                    var url = "${request.contextPath}/inventory/editBinLocation?productCode=" + productCode + "&binLocation=" + binLocation + "&lotNumber=" + lotNumber;
                    openModalDialog("#dlgShowDialog", "Adjust Stock", 1000, 400, url);

                    // Focus and select quantity field
                    $("#newQuantity").livequery(function(){
                        $(this).focus();
                        $(this).select();
                    });
                } );

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
