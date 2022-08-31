<%@ page import="org.pih.warehouse.core.ActivityCode; org.pih.warehouse.order.OrderTypeCode" %>
<div class="box">
    <h2><warehouse:message code="default.filters.label"/></h2>
    <g:form id="listForm" action="${actionName}" method="GET">
        <g:hiddenField name="max" value="${params.max ?: 10}"/>
        <div class="filter-list">
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'default.orderNumber.label', default: "Order Number")}</label>
                <g:textField class="text" id="orderNumber" name="orderNumber" value="${params.orderNumber}" style="width:100%" placeholder="Search by order number"/>

            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'order.derivedStatus.label', default: "Derived Status")}</label>
                <g:select id="derivedStatus"
                          name="derivedStatus"
                          from="${org.pih.warehouse.order.OrderSummaryStatus.derivedStatuses()}"
                          class="select2"
                          optionValue="${{ format.metadata(obj: it) }}"
                          value="${params.derivedStatus}"
                          noSelection="['': '']"
                          multiple="true"
                />
            </div>
            <div class="filter-list-item buttons center">
                <button type="submit" class="button icon search" name="search" value="true">
                    <warehouse:message code="default.search.label"/>
                </button>
                <g:link controller="order" action="${actionName}" class="button icon reload">
                    <warehouse:message code="default.button.cancel.label"/>
                </g:link>
            </div>
        </div>
    </g:form>
</div>
