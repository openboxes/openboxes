<%@ page import="org.pih.warehouse.order.OrderStatus;" %>

<div class="box">
    <h2><warehouse:message code="default.filters.label"/></h2>
    <g:form id="listForm" action="list" method="GET">
        <g:hiddenField name="max" value="${params.max ?: 10}"/>
        <div class="filter-list">
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'default.search.label')}</label>
                <g:textField class="text" id="q" name="q" value="${params.q}" style="width:100%" placeholder="Search by order number or description"/>

            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'order.status.label')}</label>
                <g:select id="status"
                          name="status"
                          from="${OrderStatus.listStockTransfer()}"
                          class="select2"
                          optionValue="${{ format.metadata(obj: it) }}"
                          value="${params.status}"
                          noSelection="['': '']"/>
            </div>
            <div class="filter-list-item">
                <label><warehouse:message code="order.createdBy.label"/></label>
                <g:selectPersonViaAjax id="createdBy"
                                         name="createdBy"
                                         class="ajaxSelect2"
                                         noSelection="['':'']"
                                         value="${params.createdBy}"
                                         data-allow-clear="true"
                                         data-ajax--url="${request.contextPath }/json/findPersonByName"
                                         data-ajax--cache="true"/>
            </div>
            <div class="filter-list-item">
                <label>
                    ${warehouse.message(code: 'default.lastUpdateAfter.label', default: 'Last updated after')}
                </label>
                <a href="javascript:void(0);" id="clearStartDate">Clear</a>
                <g:jqueryDatePicker id="lastUpdatedStartDate"
                                    name="lastUpdatedStartDate"
                                    placeholder="Start date"
                                    size="40"
                                    changeMonthAndYear="true"
                                    value="${params.lastUpdatedStartDate}"
                                    format="MM/dd/yyyy"/>
            </div>

            <div class="filter-list-item">
                <label>${warehouse.message(code: 'default.lastUpdatedBefore.label', default: 'Last updated before')}</label>
                <a href="javascript:void(0);" id="clearEndDate">Clear</a>
                <g:jqueryDatePicker id="lastUpdatedEndDate"
                                    name="lastUpdatedEndDate"
                                    placeholder="End date"
                                    size="40"
                                    changeMonthAndYear="true"
                                    value="${params.lastUpdatedEndDate}"
                                    format="MM/dd/yyyy"/>
            </div>
            <div class="filter-list-item buttons center">
                <button type="submit" class="button icon search" name="search" value="true">
                    <warehouse:message code="default.search.label"/>
                </button>
                <g:link controller="stockTransfer" action="list" class="button icon reload">
                    <warehouse:message code="default.button.cancel.label"/>
                </g:link>
            </div>
        </div>
    </g:form>
</div>
