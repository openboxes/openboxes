<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'requests.label', default: 'Requisitions')}" />
    <title>
        <warehouse:message code="requisition.label"/>
    </title>
    <content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    <link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/daterangepicker.min.css" />
</head>
<body>

    <g:set var="pageParams"
       value="['origin.id':params?.origin?.id, 'destination.id':params?.destination?.id, q:params.q,
               commodityClass:params.commodityClass, status:params.status,
               requestedDateRange:params.requestedDateRange, issuedDateRange:params.issuedDateRange, type:params.type,
               'createdBy.id':params?.createdBy?.id, sort:params?.sort, order:params?.order, relatedToMe:params.relatedToMe]"/>


    <div class="body">
        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>

        <div class="summary">
            <table>
                <tbody>
                    <tr>
                        <td class="top">
                            <div class="title">
                                <warehouse:message code="default.list.label" args="${[entityName]}" /> (${requisitions.totalCount})
                            </div>
                        </td>
                        <td class="right">
                            <g:link controller="requisition" action="exportRequisitions" params="${pageParams.findAll {it.value != 'null' }}" class="button">
                                <warehouse:message code="requisition.button.export.label" default="Export requisitions"/>
                            </g:link>
                            <g:link controller="requisition" action="exportRequisitionItems" params="${pageParams.findAll {it.value != 'null' }}" class="button">
                                <warehouse:message code="requisition.button.export.label" default="Export requisition items"/>
                            </g:link>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="buttonBar">
            <div class="button-group">
                <g:link controller="requisition" action="list" params="['relatedToMe':true]" class="button icon user">
                    ${warehouse.message(code:'requisitions.relatedToMe.label', default: 'My requisitions')}
                    (${requisitionStatistics["MINE"]?:0 })
                </g:link>
            </div>
            <div class="button-group">
                <g:link controller="requisition" action="list" class="button ${(!params.status)?'primary':''}">
                    <warehouse:message code="default.all.label"/>
                    (${requisitionStatistics["ALL"]})
                </g:link>
                <g:each var="requisitionStatus" in="${RequisitionStatus.list()}">
                    <g:if test="${requisitionStatistics[requisitionStatus]>0}">
                        <g:set var="isPrimary" value="${params.status==requisitionStatus.name()?true:false}"/>
                        <g:link controller="requisition" action="list" params="[status:requisitionStatus]" class="button ${isPrimary?'primary':''}">
                            <format:metadata obj="${requisitionStatus}"/>
                            (${requisitionStatistics[requisitionStatus]?:0 })
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
                                <label><warehouse:message code="requisition.status.label"/></label>
                                <p>
                                    <g:selectRequisitionStatus name="status" value="${params?.status}"
                                                               noSelection="['null':'']" class="chzn-select-deselect"/>
                                </p>
                            </div>
                            <div class="filter-list-item">
                                <label><warehouse:message code="requisition.requisitionType.label"/></label>
                                <p>
                                    <g:selectRequisitionType name="type" value="${params?.type}"
                                                               noSelection="['null':'']" class="chzn-select-deselect"/>
                                </p>
                            </div>
                            <div class="filter-list-item">
                                <label><warehouse:message code="requisition.origin.label"/></label>
                                <p>
                                    <g:selectLocation name="origin.id" value="${params?.origin?.id}"
                                                            noSelection="['null':'']" class="chzn-select-deselect"/>
                                </p>
                            </div>
                            <div class="filter-list-item">
                                <label><warehouse:message code="requisition.destination.label"/></label>
                                <p style="line-height: 16px; font-size: 1.2em;">
                                    <g:selectLocation name="destination.id" value="${params?.destination?.id}"
                                                      noSelection="['null':'']" class="chzn-select-deselect" />
                                </p>
                            </div>

                            <div class="filter-list-item">
                                <label><warehouse:message code="requisition.dateRequested.label"/></label>
                                <p>
                                    <g:textField id="requestedDateRange" name="requestedDateRange" style="width:100%" class="daterange text" value="${params.requestedDateRange}"/>
                                </p>
                            </div>

                            <div class="filter-list-item">
                                <label><warehouse:message code="requisition.dateIssued.label"/></label>
                                <p>
                                    <g:textField id="issuedDateRange" name="issuedDateRange" style="width:100%" class="daterange text" value="${params.issuedDateRange}"/>
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
                            <div class="filter-list-item">
                                <label>
                                    <g:checkBox name="relatedToMe" value="${params?.relatedToMe}"></g:checkBox>
                                    <warehouse:message code="requisition.relatedToMe.label" default="Only include requisitions related to me"/>
                                </label>
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
                    <%--<g:render template="list" model="[requisitions:requisitions,pageParams:pageParams]"/>--%>
                    <h2>
                        <warehouse:message code="requisitions.label"/>
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
                                              title="${warehouse.message(code: 'requisition.requestNumber.label', default: 'Request number')}" />

                            <th>
                                <warehouse:message code="requisition.requisitionType.label"/>
                            </th>

                            <th><g:message code="default.name.label"/></th>

                            <g:sortableColumn property="requestedBy" params="${pageParams}"
                                              title="${warehouse.message(code: 'requisition.requestedBy.label', default: 'Requested by')}" />

                            <g:sortableColumn property="dateRequested" params="${pageParams}"
                                              title="${warehouse.message(code: 'requisition.dateRequested.label', default: 'Date requested')}" />

                            <g:sortableColumn property="dateIssued" params="${pageParams}"
                                              title="${warehouse.message(code: 'requisition.dateIssued.label', default: 'Date issued')}" />
                            <th>
                                <g:message code="requisition.timeToProcess.label"/>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:unless test="${requisitions}">
                            <tr class="prop odd">
                                <td colspan="11" class="center">
                                    <div class="empty">
                                        <warehouse:message code="requisition.noRequisitionsMatchingCriteria.message"/>
                                    </div>
                                </td>
                            </tr>
                        </g:unless>
                        <g:each in="${requisitions}" status="i" var="requisition">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                <td style="width: 60px; text-align: center;" class="middle center">
                                    <g:render template="/requisition/actions" model="[requisition:requisition]"/>


                                    <button class="dialog-trigger" data-id="#dialog-box-${requisition?.id}">
                                        <img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
                                    </button>

                                </td>



                                <td class="left middle">
                                    <div class="count">${requisition?.requisitionItems?.size()?:0}</div>
                                </td>
                                <td class="middle">
                                    <label class="status"><format:metadata obj="${requisition?.status}"/></label>
                                </td>
                                <td class="middle">
                                    <g:link controller="requisition" action="show" id="${requisition.id}">
                                        <strong>${requisition.requestNumber }</strong>
                                    </g:link>
                                </td>
                                <td class="middle">
                                    <format:metadata obj="${requisition?.type}"/>
                                </td>
                                <td class="middle">
                                    <g:link controller="requisition" action="show" id="${requisition.id}">
                                        ${requisition.name}
                                    </g:link>
                                </td>
                                <td class="middle">
                                    ${requisition.requestedBy?:warehouse.message(code:'default.none.label')}
                                </td>
                                <td class="middle">
                                    <div title="<g:formatDate date="${requisition.dateRequested }"/>">
                                        <g:prettyDateFormat date="${requisition.dateRequested}"/>
                                    </div>
                                </td>
                                <td class="middle">
                                    <div title="<g:formatDate date="${requisition.dateIssued }"/>">
                                        <g:prettyDateFormat date="${requisition.dateIssued}"/>
                                    </div>
                                </td>
                                <td class="middle">
                                    <g:if test="${requisition.dateIssued && requisition.dateCreated}">
                                        <g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition.dateIssued, requisition.dateCreated)}"/>
                                    </g:if>
                                    <g:elseif test="${requisition.dateChecked && requisition.dateCreated}">
                                        <i><g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition.dateChecked, requisition.dateCreated)}"/></i>
                                    </g:elseif>
                                    <g:elseif test="${requisition?.picklist?.datePicked && requisition.dateCreated}">
                                        <i><g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition?.picklist?.datePicked, requisition.dateCreated)}"/></i>
                                    </g:elseif>
                                    <g:elseif test="${requisition.dateVerified && requisition.dateCreated}">
                                        <i><g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition.dateVerified, requisition.dateCreated)}"/></i>
                                    </g:elseif>
                                    <g:elseif test="${requisition.lastUpdated && requisition.dateCreated}">
                                        <i><g:relativeTime timeDuration="${groovy.time.TimeCategory.minus(requisition.lastUpdated, requisition.dateCreated)}"/></i>
                                    </g:elseif>
                                </td>

                                <td class="middle center">

                                    <div class="dialog-box" title="${requisition.requestNumber } ${requisition?.name}" id="dialog-box-${requisition?.id}" style="display:none;">
                                        <div class="list">

                                            <table class="box">
                                                <tr class="prop">
                                                    <td class="name"><label><warehouse:message
                                                            code="requisition.requestedBy.label" /></label></td>
                                                    <td class="value">
                                                        <g:if test="${requisition?.requestedBy}">
                                                            ${requisition?.requestedBy?.name } &nbsp;&bull;&nbsp;
                                                            <g:formatDate date="${requisition?.dateRequested }" format="MMMMM dd, yyyy hh:mma"/>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td class="name"><label><warehouse:message
                                                            code="requisition.verifiedBy.label" /></label></td>
                                                    <td class="value">
                                                        <g:if test="${requisition?.verifiedBy}">
                                                            ${requisition?.verifiedBy?.name } &nbsp;&bull;&nbsp;
                                                            <g:formatDate date="${requisition?.dateVerified }" format="MMMMM dd, yyyy hh:mma"/>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td class="name"><label><warehouse:message
                                                            code="picklist.picker.label" /></label></td>
                                                    <td class="value">
                                                        <g:if test="${requisition?.picklist?.picker}">
                                                            ${requisition?.picklist?.picker?.name } &nbsp;&bull;&nbsp;
                                                            <g:formatDate date="${requisition?.picklist?.datePicked }" format="MMMMM dd, yyyy hh:mma"/>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td class="name"><label><warehouse:message
                                                            code="requisition.checkedBy.label" /></label></td>
                                                    <td class="value">
                                                        <g:if test="${requisition?.reviewedBy}">
                                                            ${requisition?.reviewedBy?.name }&nbsp;&bull;&nbsp;
                                                            <g:formatDate date="${requisition?.dateReviewed }" format="MMMMM dd, yyyy hh:mma"/>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td class="name"><label><warehouse:message
                                                            code="requisition.receivedBy.label" /></label></td>
                                                    <td class="value">
                                                        <g:if test="${requisition?.receivedBy}">
                                                            ${requisition?.receivedBy?.name }&nbsp;&bull;&nbsp;
                                                            <g:formatDate date="${requisition?.dateReviewed }" format="MMMMM dd, yyyy hh:mma"/>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td class="name">
                                                        <label><warehouse:message
                                                                code="requisition.createdBy.label" /></label>
                                                    </td>
                                                    <td class="value">
                                                        <g:if test="${requisition?.createdBy}">
                                                            ${requisition?.createdBy?.name} &nbsp;&bull;&nbsp;
                                                            <g:formatDate date="${requisition?.dateCreated }" format="MMMMM dd, yyyy hh:mma"/>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                                <tr class="prop">
                                                    <td class="name">
                                                        <label><warehouse:message
                                                                code="default.updatedBy.label" /></label>
                                                    </td>
                                                    <td class="value">
                                                        <g:if test="${requisition.updatedBy}">
                                                            ${requisition?.updatedBy?.name }&nbsp;&bull;&nbsp;
                                                            <g:formatDate date="${requisition?.lastUpdated }" format="MMMMM dd, yyyy hh:mma"/>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
                                        <div class="box">
                                            <table>
                                                <tr>
                                                    <th><warehouse:message code="product.productCode.label"/></th>
                                                    <th><warehouse:message code="product.label"/></th>
                                                    <th><warehouse:message code="default.quantity.label"/></th>
                                                    <th><warehouse:message code="requisitionItem.productPackage.label"/></th>
                                                </tr>
                                                <g:unless test="${requisition?.requisitionItems}">
                                                    <tr>
                                                        <td colspan="4">
                                                            <div class="empty center">
                                                                <warehouse:message code="default.none.label"/>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </g:unless>
                                                <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="status">
                                                    <tr class="${status%2?'even':'odd'}">
                                                        <td>
                                                            ${requisitionItem?.product?.productCode}
                                                        </td>
                                                        <td>
                                                            <format:product product="${requisitionItem?.product}"/>
                                                        </td>
                                                        <td>
                                                            ${requisitionItem?.quantity}
                                                        </td>
                                                        <td>
                                                            <g:if test="${requisitionItem?.productPackage}">
                                                                ${requisitionItem?.productPackage?.uom?.code}/
                                                                ${requisitionItem?.productPackage?.quantity}
                                                            </g:if>
                                                            <g:else>
                                                                ${requisitionItem?.product?.unitOfMeasure}
                                                            </g:else>
                                                        </td>
                                                    </tr>
                                                </g:each>
                                            </table>
                                        </div>
                                    </div>

                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </div>
                <div class="paginateButtons">
                    <g:paginate total="${requisitions.totalCount}" controller="requisition" action="list" max="${params.max}"
                        params="${pageParams.findAll {it.value}}"/>

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


                $('#requestedDateRange').dateRangePicker({format: 'D/MMM/YYYY', separator: '-', autoClose: true});
                $('#issuedDateRange').dateRangePicker({format: 'D/MMM/YYYY', separator: '-', autoClose: true});


            });
        </script>        
        
    </body>
</html>
