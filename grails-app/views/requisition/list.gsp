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
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>

            <div class="yui-gd">

				<div class="yui-u first">

                    <div class="box">


                        <h2><warehouse:message code="default.filters.label"/></h2>

                        <g:form action="list" method="GET">
                            <div class="filter-list">
                                <div class="filter-list-item">
                                    <label><warehouse:message code="default.search.label"/></label>
                                    <p>
                                        <g:textField name="q" size="30" class="text" value="${params.q}" placeholder="Search by requisition number, name, etc"/>
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
                                <hr/>
                                <div class="filter-list-item">
                                    <g:submitButton name="search" class="button" value="${warehouse.message(code:'default.search.label')}"/>
                                </div>

                                <div class="clear"></div>
                            </div>
                        </g:form>

                    </div>
                </div>
                <div class="yui-u">
                    <div class="buttonBar button-group">
                        <g:link controller="requisition" action="list" class="button ${(!params.status)?'primary':''}">
                            <warehouse:message code="default.all.label"/>
                        </g:link>
                        <g:each var="requisitionStatus" in="${RequisitionStatus.list()}">
                            <g:set var="isPrimary" value="${params.status==requisitionStatus.name()?true:false}"/>
                            <g:link controller="requisition" action="list" params="[status:requisitionStatus]" class="button ${isPrimary?'primary':''}">
                                <format:metadata obj="${requisitionStatus}"/>
                            </g:link>
                        </g:each>
                    </div>
                    <div class="buttonBar button-group">
                        <g:link controller="requisition" action="list" params="['requestedBy.id':session.user.id]" class="button">
                            ${warehouse.message(code:'requisitions.submittedByMe.label', default: 'Submitted by me')}
                        </g:link>
                        <g:link controller="requisition" action="list" params="['createdBy.id':session.user.id]" class="button">
                            ${warehouse.message(code:'requisitions.createdByMe.label', default: 'Created by me')}
                        </g:link>
                        <g:link controller="requisition" action="list" params="['updatedBy.id':session.user.id]" class="button">
                            ${warehouse.message(code:'requisitions.updatedByMe.label', default: 'Updated by me')}
                        </g:link>
                    </div>



                    <g:set var="pageParams" value="['origin.id':params?.origin?.id,q:params.q,commodityClass:params.commodityClass,status:params.status,type:params.type,'createdBy.id':params?.createdBy?.id,sort:params?.sort,order:params?.order]"/>

                    <div class="box">

                        <h2>
                            ${warehouse.message(code:'default.results.label')} -
                            <span class="fade">
                                <warehouse:message code="default.showing.message" args="[requisitions.totalCount]"/>
                            </span>
                        </h2>
                        <g:render template="list" model="[requisitions:requisitions,pageParams:pageParams]"/>
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
                $(".dialog-box").dialog({ autoOpen:false, height: 400, width:600 });

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
