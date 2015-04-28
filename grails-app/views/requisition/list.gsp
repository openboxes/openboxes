<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requests.label', default: 'Requisitions').toLowerCase()}" />
        <title>
	        <warehouse:message code="requisition.label"/>
		</title>
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
        
    </head>
    <body>

        <g:set var="pageParams"
           value="['origin.id':params?.origin?.id,q:params.q,commodityClass:params.commodityClass,status:params.status,type:params.type,'createdBy.id':params?.createdBy?.id,sort:params?.sort,order:params?.order,relatedToMe:params.relatedToMe]"/>


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
                                    <warehouse:message code="requisition.list.label" /> (${requisitions.totalCount})
                                </div>
                            </td>
                            <td class="right">
                                <g:link controller="requisition" action="exportRequisitions" params="${pageParams.findAll {it.value != 'null' }}" class="button icon arrowdown">
                                    <warehouse:message code="requisition.button.export.label" default="Export requisitions"/>
                                </g:link>
                                <g:link controller="requisition" action="exportRequisitionItems" params="${pageParams.findAll {it.value != 'null' }}" class="button icon arrowdown">
                                    <warehouse:message code="requisition.button.export.label" default="Export requisition items"/>
                                </g:link>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="buttonBar">
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
                <div class="button-group">
                    <g:link controller="requisition" action="list" params="['relatedToMe':true]" class="button icon user">
                        ${warehouse.message(code:'requisitions.relatedToMe.label', default: 'My requisitions')}
                        (${requisitionStatistics["MINE"]?:0 })
                    </g:link>
                </div>
                <%--
                <div class="buttonBar button-group">
                    <g:link controller="requisition" action="list" params="['requestedBy.id':session.user.id]" class="button">
                        ${warehouse.message(code:'requisitions.submittedByMe.label', default: 'Submitted by me')}
                        (${requisitionsMap["submittedByMe"]?:0 })
                    </g:link>
                    <g:link controller="requisition" action="list" params="['createdBy.id':session.user.id]" class="button">
                        ${warehouse.message(code:'requisitions.createdByMe.label', default: 'Created by me')}
                        (${requisitionsMap["createdByMe"]?:0 })
                    </g:link>
                    <g:link controller="requisition" action="list" params="['updatedBy.id':session.user.id]" class="button">
                        ${warehouse.message(code:'requisitions.updatedByMe.label', default: 'Updated by me')}
                        (${requisitionsMap["updatedByMe"]?:0 })
                    </g:link>
                </div>
                --%>

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
                                    <label><warehouse:message code="requisition.destination.label"/></label>
                                    <p style="line-height: 16px; font-size: 1.2em;">
                                        ${session.warehouse.name}
                                        <g:hiddenField name="destination.id" value="${session?.warehouse?.id}"/>
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
                                    <label><warehouse:message code="requisition.origin.label"/></label>
                                    <p>
                                        <g:selectWardOrPharmacy name="origin.id" value="${params?.origin?.id}"
                                            noSelection="['null':'']" class="chzn-select-deselect"/>
                                    </p>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="requisition.commodityClass.label"/></label>
                                    <p>
                                        <g:selectCommodityClass name="commodityClass" value="${params?.commodityClass}"
                                                                noSelection="['null':'']" class="chzn-select-deselect"/>
                                    </p>
                                    <p>
                                        <g:checkBox name="commodityClassIsNull" value="${params?.commodityClassIsNull}"/>
                                        <label><warehouse:message code="requisition.commodityClassIsNull" default="Commodity class is null"/></label>
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
                                    <p>
                                        <g:checkBox name="relatedToMe" value="${params?.relatedToMe}"/>
                                        <label><warehouse:message code="requisition.relatedToMe" default="Only include requisitions related to me"/></label>
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

                                        <g:sortableColumn property="type" params="${pageParams}"
                                                          title="${warehouse.message(code: 'default.type.label', default: 'Type')}" />

                                        <g:sortableColumn property="commodityClass" params="${pageParams}"
                                                          title="${warehouse.message(code: 'requisition.commodityClass.label', default: 'Commodity class')}" />
                                        <%--
                                        <g:sortableColumn property="description" params="${pageParams}"
                                                          title="${warehouse.message(code: 'default.description.label', default: 'Description')}" />

                                        --%>
                                        <g:sortableColumn property="origin" params="${pageParams}"
                                                          title="${warehouse.message(code: 'requisition.origin.label', default: 'Origin')}" />

                                        <g:sortableColumn property="dateRequested" params="${pageParams}"
                                                          title="${warehouse.message(code: 'requisition.dateRequested.label', default: 'Date requested')}" />

                                        <g:sortableColumn property="requestedBy" params="${pageParams}"
                                                          title="${warehouse.message(code: 'requisition.requested.label', default: 'Requested by')}" />
                                        <%--
                                        <th>
                                            <warehouse:message code="default.created.label"/>
                                        </th>
                                        <th>
                                            <warehouse:message code="default.updated.label"/>
                                        </th>

                                        <g:sortableColumn property="createdBy" params="${pageParams}"
                                            title="${warehouse.message(code: 'default.createdBy.label', default: 'Created by')}" />

                                        <g:sortableColumn property="updatedBy" params="${pageParams}"
                                                          title="${warehouse.message(code: 'default.updatedBy.label', default: 'Updated by')}" />

                                        <g:sortableColumn property="dateCreated" params="${pageParams}"
                                                          title="${warehouse.message(code: 'default.dateCreated.label', default: 'Date created')}" />

                                        <g:sortableColumn property="lastUpdated" params="${pageParams}"
                                                          title="${warehouse.message(code: 'default.lastUpdated.label', default: 'Last updated')}" />
                                        --%>
                                        <th>

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

                                                <%--
                                                ${warehouse.message(code: 'requisition.numRequisitionItems.label', args:[requisition?.requisitionItems?.size()?:0]) }
                                                --%>
                                            </td>
                                            <td class="middle center">
                                                <label class="status"><format:metadata obj="${requisition?.status}"/></label>
                                            </td>
                                            <td class="middle center">
                                                ${requisition.requestNumber }
                                            </td>
                                            <td class="middle left">
                                                <format:metadata obj="${requisition?.type}"/>
                                            </td>
                                            <td class="middle left">
                                                <format:metadata obj="${requisition?.commodityClass?:warehouse.message(code:'default.none.label')}"/>
                                            </td>
                                            <td class="middle left">
                                                ${requisition?.origin?.name}
                                            </td>
                                            <%--
                                            <td class="middle left">
                                                <g:link action="show" id="${requisition.id}">
                                                    ${fieldValue(bean: requisition, field: "name")}
                                                </g:link>
                                            </td>
                                            --%>
                                            <td class="middle center">
                                                <format:date obj="${requisition.dateRequested}"/>
                                            </td>
                                            <td class="middle left">
                                                ${requisition.requestedBy?:warehouse.message(code:'default.none.label')}
                                            </td>
                                            <%--
                                            <td class="middle center">${requisition.createdBy?:warehouse.message(code:'default.none.label')}</td>
                                            <td class="middle center">${requisition.updatedBy?:warehouse.message(code:'default.none.label')}</td>
                                            --%>
                                            <%--
                                            <td>${requisition.createdBy}</td>
                                            <td>${requisition.updatedBy}</td>
                                            <td><format:datetime obj="${requisition.dateCreated}" /></td>
                                            <td><format:datetime obj="${requisition.lastUpdated}" /></td>
                                            --%>

                                            <td class="middle center">

                                                <div class="dialog-box" title="${requisition.requestNumber } ${requisition?.name}" id="dialog-box-${requisition?.id}" style="display:none;">
                                                    <%--
                                                       <div class="summary">
                                                           <g:render template="header" model="[requisition:requisition]"/>
                                                       </div>
                                                    --%>
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
                                                    <div class="box">
                                                        <table>
                                                            <tr>
                                                                <th><warehouse:message code="default.quantity.label"/></th>
                                                                <th><warehouse:message code="product.productCode.label"/></th>
                                                                <th><warehouse:message code="product.label"/></th>
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
                    <%--
					<g:set var="requisitions" value="${requisitions?.sort { it.status }}"/>
					<g:set var="requisitionMap" value="${requisitions?.groupBy { it.status }}"/>
					<div class="tabs">
						<ul>
							<g:each var="status" in="${org.pih.warehouse.requisition.RequisitionStatus.list() }">
								<li>
									<a href="#${format.metadata(obj: status) }">
										<format:metadata obj="${status }"/>
										<span class="fade">(${requisitionMap[status]?.size()?:0})</span>
									</a>
								</li>
							</g:each>
						</ul>		
						<g:each var="status" in="${org.pih.warehouse.requisition.RequisitionStatus.list() }">
							<div id="${format.metadata(obj: status) }">	            	
								<g:render template="list" model="[requisitions:requisitionMap[status]]"/>
							</div>
						</g:each>
					</div>
					--%>
                    <div class="paginateButtons">
                        <g:paginate total="${requisitions.totalCount}" controller="requisition" action="list" max="${params.max}"
                            params="${pageParams.findAll {it.value}}"/>

                    </div>
				</div>
			</div>		
        </div>
        
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
                $(".dialog-box").dialog({ autoOpen:false, height: 400, width:800 });

                $(".dialog-trigger").toggle(function(){
                        $($(this).attr("data-id")).dialog('open');
                    },
                    function() {
                        $($(this).attr("data-id")).dialog('close');
                    }
                );
            });
        </script>        
        
    </body>
</html>
