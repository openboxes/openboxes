<div class="filters">
    <g:form id="listForm" action="list" method="GET">
        <g:hiddenField name="type" value="${params.type}"/>
        <g:hiddenField name="max" value="${params.max ?: 10}"/>

        <h3><warehouse:message code="default.filters.label"/></h3>
        <%--

            <label class="block">${warehouse.message(code:'default.searchTerms.label', default: "Search terms")}</label>
            <div>
                <g:textField name="terms"
                             value="${params.terms}"
                             style="width: 100%;"
                             placeholder="${warehouse.message(code:'order.searchTerms.label', default: 'Search by PO number, description, item name')}"
                             class="text medium"/>
            </div>



            <label class="block">${warehouse.message(code: 'order.orderNumber.label')}</label>
            <div>
                <g:textField class="text" id="orderNumber" name="orderNumber" value="${params.orderNumber}" readonly="readonly" style="width: 100%;"
                             onclick="alert('This filter is not currently supported.');"/>

            </div>

        --%>
        <%--

            <label>Total price</label>
            <div>
                <g:textField class="text" id="totalPrice" name="totalPrice" value="${params.totalPrice}" style="width: 100%;"
                             readonly="readonly" size="10" onclick="alert('This filter is not currently supported.')"/>
            </div>

        --%>

        <label class="block">${warehouse.message(code: 'order.status.label')}</label>
        <div class="filter-list-item">
            <g:select id="status" name="status"
                      from="${org.pih.warehouse.order.OrderStatus.list()}" class="chzn-select-deselect"
                      optionValue="${{ format.metadata(obj: it) }}" value="${status}"
                      noSelection="['': warehouse.message(code: 'default.all.label')]"/>

        </div>



        <label><warehouse:message code="order.destination.label"/></label>
        <div class="filter-list-item">
            <g:select id="destination" name="destination" class="chzn-select-deselect"
                      from="${[session.warehouse]}"
                      optionKey="id" optionValue="name" value="${session.warehouse.id}"/>
        </div>



        <label><warehouse:message code="order.origin.label"/></label>
        <div class="filter-list-item">
            <g:select id="origin" name="origin" class="chzn-select-deselect"
                      from="${suppliers}"
                      optionKey="id" optionValue="name" value="${origin}"
                      noSelection="['': warehouse.message(code: 'default.all.label')]"/>
        </div>



        <label><warehouse:message code="order.orderedBy.label"/></label>

        <div class="filter-list-item">
            <g:select id="orderedById" name="orderedById" class="chzn-select-deselect"
                      from="${orderedByList}"
                      optionKey="id" optionValue="name" value="${params?.orderedById}"
                      noSelection="['': warehouse.message(code: 'default.all.label')]"/>
        </div>



        <label>${warehouse.message(code: 'default.lastUpdated.label', default: 'Last updated')}</label>
        <a href="javascript:void(0);" id="clearDates">${warehouse.message(code: 'default.clearDates.label', default: 'Clear dates')}</a>

        <div class="filter-list-item">
            <g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
                                placeholder="${warehouse.message(code: 'default.from.label', default: 'From')}"
                                numberOfMonths="2" changeMonthAndYear="false"
                                value="${statusStartDate}" format="MM/dd/yyyy"/>

        </div>
        <div class="filter-list-item">
            <g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
                                placeholder="${warehouse.message(code: 'default.to.label', default: 'To')}"
                                numberOfMonths="2" changeMonthAndYear="true"
                                value="${statusEndDate}" format="MM/dd/yyyy"/>
        </div>

        <hr/>
        <div class="buttons">

            <button type="submit" class="button icon search" name="search" value="true">
                <warehouse:message code="default.search.label"/>
            </button>

            <g:link controller="order" action="list" class="button icon reload">
                <warehouse:message code="default.button.cancel.label"/>
            </g:link>
        </div>

    </g:form>
</div>







