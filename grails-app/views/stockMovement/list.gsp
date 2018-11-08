<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'stockMovements.label', default: 'Stock Movements')}" />
    <title>
        ${entityName} &rsaquo; <warehouse:message code="enum.StockMovementDirection.${params.direction}"/>
    </title>
    <content tag="pageTitle">${entityName}</content>
    <link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/daterangepicker.min.css" />
</head>
<body>

<g:set var="pageParams"
       value="['origin.id':params?.origin?.id, 'destination.id':params?.destination?.id, q:params.q,
               commodityClass:params.commodityClass, status:params.status, direction: params?.direction,
               requestedDateRange:params.requestedDateRange, issuedDateRange:params.issuedDateRange, type:params.type,
               'createdBy.id':params?.createdBy?.id, sort:params?.sort, order:params?.order, relatedToMe:params.relatedToMe,
               'requestedBy.id': params?.requestedBy?.id]"/>

<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="summary">
        <div class="title">
            ${entityName} &rsaquo; <warehouse:message code="enum.StockMovementDirection.${params.direction}"/>
        </div>
    </div>

    <div class="buttonBar">

        <div class="right">

            <div class="button-group">
                <g:link controller="stockMovement" action="list" params="['requestedBy.id':session?.user?.id]" class="button icon user">
                    ${warehouse.message(code:'stockMovements.relatedToMe.label', default: 'My stock movements')}
                    (${statistics["MINE"]?:0 })
                </g:link>
            </div>
            <div class="button-group">
                <g:link controller="stockMovement" action="list" class="button ${(!params.status)?'primary':''}">
                    <warehouse:message code="default.all.label"/>
                    (${statistics["ALL"]})
                </g:link>
                <g:each var="status" in="${RequisitionStatus.list()}">
                    <g:if test="${statistics[status]>0}">
                        <g:set var="isPrimary" value="${params.status==status.name()?true:false}"/>
                        <g:link controller="stockMovement" action="list" params="[status:status]" class="button ${isPrimary?'primary':''}">
                            <format:metadata obj="${status}"/>
                            (${statistics[status]?:0 })
                        </g:link>
                    </g:if>
                </g:each>
            </div>
        </div>

        <div class="button-group">
            <g:link controller="stockMovement" action="list" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'text_list_bullets.png')}" />&nbsp;
                <warehouse:message code="default.button.list.label" />
            </g:link>
            <g:link controller="stockMovement" action="index" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.button.create.label" />
            </g:link>
        </div>


    </div>

    <div class="yui-gf">
        <div class="yui-u first">
            <div class="box">
                <h2><warehouse:message code="default.filters.label"/></h2>
                <g:form action="list" method="GET">
                    <g:hiddenField name="max" value="${params.max}"/>
                    <g:hiddenField name="offset" value="${params.offset}"/>
                    <div class="filter-list">
                        <div class="filter-list-item">
                            <label><warehouse:message code="default.search.label"/></label>
                            <p>
                                <g:textField name="q" style="width:100%" class="text" value="${params.q}" placeholder="Search by requisition number, name, etc"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.status.label"/></label>
                            <p>
                                <g:selectRequisitionStatus name="status" value="${params?.status}"
                                                           noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
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
                        <%--

                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.dateRequested.label"/></label>
                            <p>
                                <g:textField id="requestedDateRange" name="requestedDateRange" style="width:100%" class="daterange text" value="${params.requestedDateRange}"/>
                            </p>
                        </div>

                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.dateIssued.label"/></label>
                            <p>
                                <g:textField id="issuedDateRange" name="issuedDateRange" style="width:100%" class="daterange text" value="${params.issuedDateRange}"/>
                            </p>
                        </div>

                        --%>
                        <div class="filter-list-item">
                            <label><warehouse:message code="requisition.requestedBy.label"/></label>
                            <p>
                                <g:selectUser name="requestedBy.id" value="${params?.requestedBy?.id}"
                                              noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <hr/>
                        <div class="filter-list-item">
                            <button class="button icon search" name="search" class="button">
                                ${warehouse.message(code:'default.search.label')}
                            </button>
                        </div>

                        <div class="clear"></div>
                    </div>
                </g:form>

            </div>
        </div>
        <div class="yui-u">
            <g:set var="stockMovements" value="${stockMovements.sort { it?.shipmentStatusCode }}"/>
            <g:set var="stockMovementsMap" value="${stockMovements.groupBy { it?.shipmentStatusCode }}"/>
            <g:if test="${stockMovements.size()}">
                <div class="tabs">
                    <ul>
                        <g:each var="shipmentStatusCode" in="${stockMovementsMap.keySet() }">
                            <li>
                                <a href="#shipment-status-${shipmentStatusCode}">
                                    <format:metadata obj="${shipmentStatusCode }"/>
                                    (${stockMovementsMap[shipmentStatusCode]?.size() })
                                </a>
                            </li>
                        </g:each>
                    </ul>
                    <g:each var="shipmentStatusCode" in="${stockMovementsMap.keySet() }">
                        <div id="shipment-status-${shipmentStatusCode}">
                            <g:render template="list" model="[stockMovements:stockMovementsMap[shipmentStatusCode],
                                                              entityName:entityName,
                                                              totalCount:stockMovements.totalCount,
                                                              shipmentStatusCode:shipmentStatusCode,
                                                              pageParams:pageParams]"/>
                        </div>
                    </g:each>
                </div>
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


                %{--$('#requestedDateRange').dateRangePicker({format: 'D/MMM/YYYY', separator: '-', autoClose: true});--}%
                %{--$('#issuedDateRange').dateRangePicker({format: 'D/MMM/YYYY', separator: '-', autoClose: true});--}%


            });
        </script>

</body>
</html>
