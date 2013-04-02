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
                                    <label><warehouse:message code="requisition.destination.label"/></label>
                                    <p>
                                        ${session.warehouse.name}
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
                                    <label><warehouse:message code="requisition.createdBy.label"/></label>
                                    <p>
                                        <g:selectUser name="createdBy.id" value="${params?.createdBy?.id?:session?.user?.id}"
                                                                noSelection="['null':'']" class="chzn-select-deselect"/>
                                    </p>
                                </div>
                                <div class="filter-list-item">
                                    <label><warehouse:message code="default.search.label"/></label>
                                    <p>
                                        <g:textField name="q" size="30" class="text" value="${params.q}" placeholder="Search by requisition number, name, etc"/>
                                    </p>
                                </div>
                                <div class="center">
                                    <g:submitButton name="search" class="button" value="${warehouse.message(code:'default.search.label')}"/>
                                </div>
                                <div class="clear"></div>
                            </div>
                        </g:form>

                    </div>
                </div>
                <div class="yui-u">
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
                    <div class="fade">
                        <warehouse:message code="default.showing.message" args="[requisitions.size()]"/>
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
				
			});
        </script>        
        
    </body>
</html>
