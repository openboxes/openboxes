<%@ page import="org.pih.warehouse.api.StockMovementType; org.pih.warehouse.requisition.RequisitionStatus" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>
<%@ page import="org.pih.warehouse.requisition.RequisitionType" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'stockMovements.label', default: 'Stock Movements')}" />
    <title>
        ${entityName}
        <g:if test="${params.sourceType}">
            &rsaquo; <warehouse:message code="requests.label"/>
        </g:if>
        <g:elseif test="${params.direction}">
            &rsaquo; <warehouse:message code="enum.StockMovementType.${params.direction}"/>
        </g:elseif>
    </title>
    <content tag="pageTitle">${entityName}</content>
    <link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/daterangepicker.min.css" />
</head>
<body>

<g:set var="pageParams"
       value="['origin.id':params?.origin?.id, 'destination.id':params?.destination?.id, q:params.q,
               commodityClass:params.commodityClass, status:params.status, direction: params?.direction,
               requestedDateRange:params.requestedDateRange, issuedDateRange:params.issuedDateRange, type:params.type,
               'createdBy.id':params?.createdBy?.id, sort:params?.sort, order:params?.order,
               'requestedBy.id': params?.requestedBy?.id, receiptStatusCode: params.receiptStatusCode,
               'createdAfter': params?.createdAfter, 'createdBefore': params?.createdBefore,
               'sourceType': params?.sourceType, 'updatedBy.id':params?.updatedBy?.id]"/>

<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:if test="${flash.error}">
        <div class="error">${flash.error}</div>
    </g:if>

    <div class="summary">
        <div class="title">
            ${entityName}
            <g:if test="${params.sourceType}">
                &rsaquo; <warehouse:message code="requests.label"/>
            </g:if>
            <g:elseif test="${params.direction}">
                &rsaquo; <warehouse:message code="enum.StockMovementType.${params.direction}"/>
            </g:elseif>
        </div>
    </div>

    <div class="buttonBar">

        <div class="right">
            <div class="button-container">
                <g:if test="${params.direction as StockMovementType == StockMovementType.INBOUND}">
                    <g:link controller="stockMovement" action="exportItems" class="button">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}" />
                        ${warehouse.message(code: 'stockMovements.exportIncomingItems.label', default: 'Export all incoming items')}
                    </g:link>
                </g:if>
                <g:if test="${params.direction as StockMovementType == StockMovementType.OUTBOUND}">
                    <g:link class="button" action="exportPendingRequisitionItems">
                        <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                        <warehouse:message code="stockMovements.exportPendingShipmentItems.label" args="['Preference Type']"/>
                    </g:link>
                </g:if>
                <g:link controller="stockMovement" action="list" params="${pageParams + ['createdBy.id':session.user.id, 'requestedBy.id':session.user.id]}" class="button">
                    <img src="${resource(dir: 'images/icons/silk', file: 'user.png')}" />&nbsp;
                    ${warehouse.message(code:'stockMovements.myStockMovements.label', default: 'My stock movements')}
                </g:link>
            </div>
        </div>

        <div class="button-container">
            <g:link controller="stockMovement" action="list" class="button" params="[direction:params.direction]">
                <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovement.label')]"/>
            </g:link>
            <g:if test="${params.direction as StockMovementType == StockMovementType.OUTBOUND}">
                <g:link controller="stockMovement" action="list" class="button" params="[direction:'OUTBOUND', sourceType: 'ELECTRONIC']">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                    <warehouse:message code="default.open.label" args="[warehouse.message(code: 'requests.label')]"/>
                </g:link>
            </g:if>
            <g:link controller="stockMovement" action="create" class="button" params="[direction:params.direction]">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.label')]" />
            </g:link>
            <g:if test="${params.direction as StockMovementType == StockMovementType.INBOUND}">
                <g:link controller="stockMovement" action="createCombinedShipments" class="button" params="[direction:'INBOUND']">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.create.label" args="[warehouse.message(code: 'shipmentFromPO.label')]"/>
                </g:link>
            </g:if>
        </div>


    </div>

    <div class="yui-gf">
        <div class="yui-u first">
            <div class="box">
                <h2><warehouse:message code="default.filters.label"/></h2>
                <g:form action="list" method="GET">
                    <g:hiddenField name="max" value="${params.max?:10}"/>
                    <g:hiddenField name="offset" value="${0}"/>
                    <g:hiddenField name="sourceType" value="${params?.sourceType}" />
                    <div class="filter-list">
                        <div class="filter-list-item">
                            <label><warehouse:message code="default.search.label"/></label>
                            <p>
                                <g:textField name="q" style="width:100%" class="text" value="${params.q}" placeholder="Search by requisition number, name, etc"/>
                            </p>
                        </div>
                        <g:if test="${!params.direction || params.direction as StockMovementType == StockMovementType.OUTBOUND}">
                            <div class="filter-list-item">
                                <label><warehouse:message code="stockMovement.status.label"/></label>
                                <p>
                                    <g:select name="status"
                                              from="${RequisitionStatus.list()}"
                                              keys="${RequisitionStatus.list()*.name()}"
                                              value="${params?.list('status')}"
                                              noSelection="['':'']"
                                              class="chzn-select-deselect" multiple="true"/>
                                </p>
                            </div>
                        </g:if>
                        <g:if test="${!params.direction || params.direction as StockMovementType == StockMovementType.INBOUND}">
                            <div class="filter-list-item">
                                <label><warehouse:message code="stockMovement.receiptStatus.label" default="Receipt Status"/></label>
                                <p>
                                    <g:select name="receiptStatusCode" value="${params?.list('receiptStatusCode')}" from="${ShipmentStatusCode.list()}"
                                     keys="${org.pih.warehouse.shipping.ShipmentStatusCode.values()*.name()}"  noSelection="['':'']" class="chzn-select-deselect" multiple="true"/>
                                </p>
                            </div>
                        </g:if>
                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.origin.label"/></label>
                            <p>
                                <g:selectLocation name="origin.id" value="${params?.origin?.id}"
                                                        noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.destination.label"/></label>
                            <p>
                                <g:selectLocation name="destination.id" value="${params?.destination?.id}"
                                                  noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="requisition.requestedBy.label"/></label>
                            <p>
                                <g:selectUser name="requestedBy.id" value="${params?.requestedBy?.id}"
                                              noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="default.createdBy.label"/></label>
                            <p>
                                <g:selectUser name="createdBy.id" value="${params?.createdBy?.id}"
                                              noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="default.updatedBy.label"/></label>
                            <p>
                                <g:selectUser name="updatedBy.id" value="${params?.updatedBy?.id}"
                                              noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <g:if test="${!params.direction || params.direction as StockMovementType == StockMovementType.OUTBOUND}">
                            <div class="filter-list-item">
                                <label><warehouse:message code="stockMovement.requestType.label" default="Request type"/></label>
                                <p>
                                    <g:select name="type" value="${params?.type}" from="${RequisitionType.listRequestTypes()}"
                                              noSelection="['':'']" class="chzn-select-deselect"/>
                                </p>
                            </div>
                        </g:if>
                        <div class="filter-list-item">
                            <label>
                                ${warehouse.message(code: 'default.createdAfter.label', default: 'Created after')}
                            </label>
                            <a href="javascript:void(0);" id="clearCreatedAfterDate">Clear</a>
                            <g:jqueryDatePicker id="createdAfter"
                                                name="createdAfter"
                                                placeholder="Select date"
                                                size="40"
                                                autocomplete="off"
                                                value="${params?.createdAfter}"
                                                format="MM/dd/yyyy"/>
                        </div>
                        <div class="filter-list-item">
                            <label>
                                ${warehouse.message(code: 'default.createdBefore.label', default: 'Created before')}
                            </label>
                            <a href="javascript:void(0);" id="clearCreatedBeforeDate">Clear</a>
                            <g:jqueryDatePicker id="createdBefore"
                                                name="createdBefore"
                                                placeholder="Select date"
                                                size="40"
                                                autocomplete="off"
                                                value="${params?.createdBefore}"
                                                format="MM/dd/yyyy"/>
                        </div>
                        <hr/>
                        <div class="buttons">
                            <button name="search" class="button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" />&nbsp;
                                ${warehouse.message(code:'default.search.label')}
                            </button>
                            <button name="format" value="csv" class="button">
                                <img src="${resource(dir: 'images/icons/silk', file: 'page_excel.png')}" />&nbsp;
                                <warehouse:message code="default.button.download.label" default="Download"/>
                            </button>
                        </div>

                        <div class="clear"></div>
                    </div>
                </g:form>
            </div>
        </div>
        <div class="yui-u">
            <g:if test="${stockMovements}">
                <g:render template="list" model="[stockMovements:stockMovements,
                                                  entityName:entityName,
                                                  totalCount:stockMovements.totalCount,
                                                  pageParams:pageParams]"/>
            </g:if>
            <g:else>
                <div class="box">
                    <h2>${warehouse.message(code:'stockMovements.label')}</h2>
                    <div class="center empty">
                        <warehouse:message code="default.noResultsFound.label" default="No results found" />
                    </div>
                </div>
            </g:else>

        </div>
    </div>
</div>
<script type="text/javascript" src="//cdn.jsdelivr.net/momentjs/latest/moment.min.js"></script>
<script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/jquery.daterangepicker.min.js"></script>


<script type="text/javascript">
			$(function() {
		    	$(".tabs").tabs(
	    			{
	    				cookie: {
	    					// store cookie for a day, without, it would be a session cookie
	    					expires: 1
	    				}
	    			}
				);

                $(".dialog-box").hide();
                $(".dialog-box").dialog({ autoOpen:false, height: 600, width:800 });

                $(".dialog-trigger").click(function(event){
                    $($(this).attr("data-id")).dialog('open');
                });

                $("#clearCreatedAfterDate")
                .click(function () {
                  $('#createdAfter-datepicker')
                    .datepicker('setDate', null);
                });
                $("#clearCreatedBeforeDate")
                .click(function () {
                  $('#createdBefore-datepicker')
                    .datepicker('setDate', null);
                });

            });
        </script>

</body>
</html>
