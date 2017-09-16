<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitions.label', default: 'Stock Requisitions').toLowerCase()}" />
        <title>
	        <warehouse:message code="requisition.label"/>
		</title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>

            <div class="buttonBar">
                <g:link class="button icon log" controller="requisitionTemplate" action="list">
                    <warehouse:message code="default.list.label" args="[warehouse.message(code:'requisitionTemplates.label').toLowerCase()]"/>
                </g:link>
                <g:isUserAdmin>
                    <g:each var="stockType" in="${org.pih.warehouse.requisition.RequisitionType.listStockTypes()}">
                        <g:link class="button icon add" controller="requisitionTemplate" action="create" params="[type:stockType]">
                            <g:set var="stockTypeName" value="${format.metadata(obj: stockType).toLowerCase()}"/>
                            <warehouse:message code="default.add.label" args="[stockTypeName]"/>
                        </g:link>

                    </g:each>
                </g:isUserAdmin>
            </div>
            <div class="yui-gf">
                <div class="yui-u first">
                    <div class="box">
                        <h2><g:message code="default.filters.label"/></h2>
                        <g:form action="list" method="GET">
                            <div class="filter-list-item">
                                <label>${g.message(code:'default.search.label')}</label>
                                <input type="text" name="q" class="text large"
                                       placeholder="${g.message(code: 'default.name.label')}" value="${params.q}"/>
                            </div>
                            <div class="filter-list-item">
                                <label>${g.message(code:'requisition.origin.label')}</label>
                                <g:selectRequestOrigin name="origin.id" value="${params?.origin?.id}" noSelection="['':'']" class="chzn-select-deselect" />
                            </div>


                            <div class="filter-list-item">
                                <label>${g.message(code:'requisition.requisitionType.label')}</label>
                                <g:selectRequisitionType name="requisitionType" value="${params.requisitionType}"
                                                         noSelection="['':'']" class="chzn-select-deselect"/>
                            </div>
                            <div class="filter-list-item">
                                <label>${g.message(code:'requisition.commodityClass.label')}</label>
                                <g:selectCommodityClass name="commodityClass" value="${params.commodityClass}"
                                                        noSelection="['':'']" class="chzn-select-deselect"/>
                            </div>
                            <div class="buttons">
                                <button class="button"><g:message code="default.search.label"/></button>

                            </div>
                        </g:form>
                    </div>

                </div>
                <div class="yui-u">
                    <g:set var="requisitions" value="${requisitions?.sort { it.status }}"/>
                    <g:set var="requisitionMap" value="${requisitions?.groupBy { it.status }}"/>
                    <div class="box">

                        <h2>
                            ${g.message(code:'requisitionTemplates.label')}
                            <small><g:message code="default.showingResults.message"
                                              args="[requisitions.size(), requisitions.totalCount]"/></small>
                        </h2>

                        <g:render template="list" model="[requisitions:requisitions]"/>
                        <div class="paginateButtons">
                            <g:paginate total="${requisitions.totalCount}" params="${params}"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
