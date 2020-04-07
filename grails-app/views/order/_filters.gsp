<div class="box">
    <h2><warehouse:message code="default.filters.label"/></h2>
    <g:form id="listForm" action="list" method="GET">
        <g:hiddenField name="type" value="${params.type}"/>
        <g:hiddenField name="max" value="${params.max ?: 10}"/>
        <div class="filter-list">
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'order.orderNumber.label')}</label>
                <g:textField class="text" id="orderNumber" name="orderNumber" value="${params.orderNumber}" style="width:100%"/>

            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'order.orderTypeCode.label')}</label>
                <g:select id="orderTypeCode" name="orderTypeCode"
                          from="${org.pih.warehouse.order.OrderTypeCode.list()}" class="chzn-select-deselect"
                          optionValue="${{ format.metadata(obj: it) }}" value="${params.orderTypeCode}"
                          noSelection="['': warehouse.message(code: 'default.all.label')]"/>
            </div>
            <div class="filter-list-item">
                <label>${warehouse.message(code: 'order.status.label')}</label>
                <g:select id="status" name="status"
                          from="${org.pih.warehouse.order.OrderStatus.list()}" class="chzn-select-deselect"
                          optionValue="${{ format.metadata(obj: it) }}" value="${params.status}"
                          noSelection="['': warehouse.message(code: 'default.all.label')]"/>
            </div>
            <div class="filter-list-item">
                <label><warehouse:message code="order.origin.label"/></label>
                <g:selectLocation id="origin" name="origin" class="chzn-select-deselect"
                          optionKey="id" optionValue="name" value="${params.origin}" noSelection="['':'']" />
            </div>
            <div class="filter-list-item">
                <label><warehouse:message code="order.destination.label"/></label>
                <g:selectLocation id="destination" name="destination" class="chzn-select-deselect"
                          optionKey="id" optionValue="name"
                          value="${params.destination?:session?.warehouse?.id}" noSelection="['':'']" />
            </div>
            <div class="filter-list-item">
                <label><warehouse:message code="order.orderedBy.label"/></label>
                <g:selectPerson id="orderedBy" name="orderedBy"
                                class="chzn-select-deselect" value="${params?.orderedBy}"
                                noSelection="['': warehouse.message(code: 'default.all.label')]"/>
            </div>
            <div class="filter-list-item">
                <label><warehouse:message code="order.createdBy.label"/></label>
                <g:selectPerson id="createdBy" name="createdBy"
                                class="chzn-select-deselect" value="${params?.createdBy}"
                                noSelection="['': warehouse.message(code: 'default.all.label')]"/>
            </div>
            <div class="filter-list-item">
                <label>
                    ${warehouse.message(code: 'default.lastUpdateAfter.label', default: 'Last updated after')}
                </label>
                <a href="javascript:void(0);" id="clearStartDate">Clear</a>
                <g:jqueryDatePicker id="statusStartDate" name="statusStartDate" placeholder="Start date"
                                    size="40" numberOfMonths="2" changeMonthAndYear="false"
                                    value="${params.statusStartDate}" format="MM/dd/yyyy"/>
            </div>

            <div class="filter-list-item">
                <label>${warehouse.message(code: 'default.lastUpdatedBefore.label', default: 'Last updated before')}</label>
                <a href="javascript:void(0);" id="clearEndDate">Clear</a>
                <g:jqueryDatePicker id="statusEndDate" name="statusEndDate" placeholder="End date" size="40"
                                    numberOfMonths="2" changeMonthAndYear="true"
                                    value="${params.statusEndDate}" format="MM/dd/yyyy"/>
            </div>
            <div class="filter-list-item buttons center">
                <button type="submit" class="button icon search" name="search" value="true">
                    <warehouse:message code="default.search.label"/>
                </button>
                <g:link controller="order" action="list" class="button icon reload">
                    <warehouse:message code="default.button.cancel.label"/>
                </g:link>
            </div>
        </div>
    </g:form>
</div>
