<%@ page import="org.pih.warehouse.core.ActivityCode; org.pih.warehouse.order.OrderTypeCode" %>
<div class="box">
    <h2><warehouse:message code="default.filters.label"/></h2>
    <g:form id="listForm" action="orderSummary" method="GET">
        <g:hiddenField name="max" value="${params.max ?: 10}"/>
        <div class="filter-list">
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'default.orderNumber.label', default: "Order Number")}</label>
                <g:textField class="text" id="orderNumber" name="orderNumber" value="${params.orderNumber}" style="width:100%" placeholder="Search by order number"/>

            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'order.orderStatus.label', default: "Order Status")}</label>
                <g:select id="orderStatus"
                          name="orderStatus"
                          from="${org.pih.warehouse.order.OrderSummaryStatus.orderStatuses()}"
                          class="select2"
                          optionValue="${{ format.metadata(obj: it) }}"
                          value="${params.orderStatus}"
                          noSelection="['': '']"
                          multiple="true"
                />
            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'order.shipmentStatus.label', default: "Shipment Status")}</label>
                <g:select id="shipmentStatus"
                          name="shipmentStatus"
                          from="${org.pih.warehouse.order.OrderSummaryStatus.shipmentStatuses()}"
                          class="select2"
                          optionValue="${{ format.metadata(obj: it) }}"
                          value="${params.shipmentStatus}"
                          noSelection="['': '']"
                          multiple="true"
                />
            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'order.receiptStatus.label', default: "Receipt Status")}</label>
                <g:select id="receiptStatus"
                          name="receiptStatus"
                          from="${org.pih.warehouse.order.OrderSummaryStatus.receiptStatuses()}"
                          class="select2"
                          optionValue="${{ format.metadata(obj: it) }}"
                          value="${params.receiptStatus}"
                          noSelection="['': '']"
                          multiple="true"
                />
            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'order.paymentStatus.label', default: "Payment Status")}</label>
                <g:select id="paymentStatus"
                          name="paymentStatus"
                          from="${org.pih.warehouse.order.OrderSummaryStatus.paymentStatuses()}"
                          class="select2"
                          optionValue="${{ format.metadata(obj: it) }}"
                          value="${params.paymentStatus}"
                          noSelection="['': '']"
                          multiple="true"
                />
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
                <g:link controller="order" action="orderSummary" class="button icon reload">
                    <warehouse:message code="default.button.cancel.label"/>
                </g:link>
            </div>
        </div>
    </g:form>
</div>
