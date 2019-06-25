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
                <g:link class="button" controller="requisitionTemplate" action="list">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[warehouse.message(code:'requisitionTemplates.label').toLowerCase()]"/>
                </g:link>
                <g:isUserAdmin>
                    <g:link class="button" controller="requisitionTemplate" action="create" params="[type:'STOCK']">
                        <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                        <warehouse:message code="default.add.label" args="[g.message(code:'requisitionTemplate.label')]"/>
                    </g:link>
                </g:isUserAdmin>
                <g:link controller="requisitionTemplate" action="exportPage" class="button">
                    <img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}"/>
                    ${warehouse.message(code: 'stockList.export.label')}
                </g:link>
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
                                <g:selectLocation name="origin.id" value="${params?.origin?.id}"
                                                  noSelection="['':'']" class="chzn-select-deselect" />
                            </div>
                            <div class="filter-list-item">
                                <label>${g.message(code:'requisition.destination.label')}</label>
                                <g:selectLocation name="destination.id" value="${params?.destination?.id}"
                                                  noSelection="['':'']" class="chzn-select-deselect" />
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
                            <g:paginate params="${params}" total="${requisitions.totalCount}" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
