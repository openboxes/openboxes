<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'stockMovements.label', default: 'Stock Movements')}" />
    <title>
        <warehouse:message code="stockMovements.label"/>
    </title>
    <content tag="pageTitle">${entityName}</content>
    <link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/daterangepicker.min.css" />
</head>
<body>

<g:set var="pageParams"
       value="['origin.id':params?.origin?.id, q:params.q, commodityClass:params.commodityClass, status:params.status,
               requestedDateRange:params.requestedDateRange, issuedDateRange:params.issuedDateRange, type:params.type,
               'createdBy.id':params?.createdBy?.id, sort:params?.sort, order:params?.order, relatedToMe:params.relatedToMe,
               'requestedBy.id': params?.requestedBy?.id]"/>


<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="summary">
        <div class="title">
            ${entityName}
        </div>
    </div>

    <div class="buttonBar">
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

    <div class="yui-gf">
        <div class="yui-u first">
            <div class="box">
                <h2><warehouse:message code="default.filters.label"/></h2>
                <g:form action="list" method="GET">
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
                                                  noSelection="['null':'']" class="chzn-select"/>
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
            <div class="box">
                <h2>
                    <warehouse:message code="stockMovements.label"/> (${stockMovements?.totalCount?:0})
                </h2>
                <table>
                    <thead>
                    <tr>
                        <th>
                            <warehouse:message code="default.actions.label"/>
                        </th>
                        <th>
                            <warehouse:message code="default.numItems.label"/>
                        </th>
                        <g:sortableColumn property="status" params="${pageParams}"
                                          title="${warehouse.message(code: 'default.status.label', default: 'Status')}" />

                        <g:sortableColumn property="requestNumber" params="${pageParams}"
                                          title="${warehouse.message(code: 'stockMovement.identifier.label', default: 'Stock movement number')}" />

                        <th><g:message code="default.name.label"/></th>
                        <th><g:message code="stockMovement.origin.label"/></th>
                        <th><g:message code="stockMovement.destination.label"/></th>
                        <th><g:message code="stockMovement.stocklist.label"/></th>


                        <g:sortableColumn property="requestedBy" params="${pageParams}"
                                          title="${warehouse.message(code: 'stockMovement.requestedBy.label', default: 'Requested by')}" />

                        <g:sortableColumn property="dateRequested" params="${pageParams}"
                                          title="${warehouse.message(code: 'stockMovement.dateRequested.label', default: 'Date requested')}" />

                    </tr>
                    </thead>
                    <tbody>
                    <g:unless test="${stockMovements}">
                        <tr class="prop odd">
                            <td colspan="11" class="center">
                                <div class="empty">
                                    <warehouse:message code="default.noItems.label"/>
                                </div>
                            </td>
                        </tr>
                    </g:unless>
                    <g:each in="${stockMovements}" status="i" var="stockMovement">
                        <g:set var="requisition" value="${stockMovement.requisition}"/>
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td>
                                <g:render template="/stockMovement/actions" model="[stockMovement:stockMovement]"/>
                            </td>
                            <td>
                                <div class="count">${stockMovement?.lineItems?.size()?:0}</div>
                            </td>
                            <td>
                                <label class="status"><format:metadata obj="${stockMovement?.status}"/></label>
                            </td>
                            <td>
                                <g:link controller="stockMovement" action="show" id="${stockMovement.id}">
                                    <strong>${stockMovement.identifier }</strong>
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="stockMovement" action="show" id="${stockMovement.id}">
                                    <div title="${stockMovement.name}">${stockMovement.description}</div>
                                </g:link>
                            </td>
                            <td>
                                ${stockMovement?.origin?.name}
                            </td>
                            <td>
                                ${stockMovement?.destination?.name}
                            </td>
                            <td>
                                ${stockMovement?.stocklist?.name?:"N/A"}
                            </td>
                            <td>
                                ${stockMovement.requestedBy?:warehouse.message(code:'default.noone.label')}
                            </td>
                            <td>
                                <div title="<g:formatDate date="${stockMovement.dateRequested }"/>">
                                    <g:prettyDateFormat date="${stockMovement.dateRequested}"/>
                                </div>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
                <div class="paginateButtons">
                    <g:paginate total="${stockMovements.totalCount}" controller="stockMovement" action="list" max="${params.max}"
                                params="${pageParams.findAll {it.value}}"/>

                </div>
            </div>
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
