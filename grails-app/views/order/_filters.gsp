<div>
    <g:form id="listForm" action="list" method="GET">
        <g:hiddenField name="type" value="${params.type}"/>
        <g:hiddenField name="max" value="${params.max ?: 10}"/>
        <div class="box">
            <h2><warehouse:message code="default.filters.label"/></h2>
            <table>
                <%--
                <tr>
                    <td>
                        <label class="block">${warehouse.message(code:'default.searchTerms.label', default: "Search terms")}</label>
                        <div>
                            <g:textField name="terms"
                                         value="${params.terms}"
                                         style="width: 100%;"
                                         placeholder="${warehouse.message(code:'order.searchTerms.label', default: 'Search by PO number, description, item name')}"
                                         class="text medium"/>
                        </div>
                    </td>
                </tr>

                <tr class="prop">
                    <td>
                        <label class="block">${warehouse.message(code: 'order.orderNumber.label')}</label>
                        <div>
                            <g:textField class="text" id="orderNumber" name="orderNumber" value="${params.orderNumber}" readonly="readonly" style="width: 100%;"
                                         onclick="alert('This filter is not currently supported.');"/>

                        </div>
                    </td>
                </tr>
                --%>
                <%--
                <tr class="prop">
                    <td>
                        <label>Total price</label>
                        <div>
                            <g:textField class="text" id="totalPrice" name="totalPrice" value="${params.totalPrice}" style="width: 100%;"
                                         readonly="readonly" size="10" onclick="alert('This filter is not currently supported.')"/>
                        </div>
                    </td>
                </tr>
                --%>

                <tr class="prop">
                    <td>
                        <label class="block">${warehouse.message(code: 'order.status.label')}</label>
                        <div>
                            <g:select id="status" name="status"
                                      from="${org.pih.warehouse.order.OrderStatus.list()}" class="chzn-select-deselect"
                                      optionValue="${{ format.metadata(obj: it) }}" value="${status}"
                                      noSelection="['': warehouse.message(code: 'default.all.label')]"/>

                        </div>
                    </td>
                </tr>

                <tr class="prop">
                    <td>
                        <label><warehouse:message code="order.destination.label"/></label>
                        <div>
                            <g:select id="destination" name="destination" class="chzn-select-deselect"
                                      from="${[session.warehouse]}"
                                      optionKey="id" optionValue="name" value="${session.warehouse.id}"/>
                        </div>
                    </td>
                </tr>

                <tr class="prop">
                    <td>
                        <label><warehouse:message code="order.origin.label"/></label>
                        <div>
                            <g:select id="origin" name="origin" class="chzn-select-deselect"
                                      from="${suppliers}"
                                      optionKey="id" optionValue="name" value="${origin}"
                                      noSelection="['': warehouse.message(code: 'default.all.label')]"/>
                        </div>
                    </td>
                </tr>

                <tr class="prop">
                    <td>
                        <label><warehouse:message code="order.orderedBy.label"/></label>

                        <div>
                            <g:select id="orderedById" name="orderedById" class="chzn-select-deselect"
                                      from="${orderedByList}"
                                      optionKey="id" optionValue="name" value="${params?.orderedById}"
                                      noSelection="['': warehouse.message(code: 'default.all.label')]"/>
                        </div>
                    </td>
                </tr>

                <tr class="prop">
                    <td>

                        <label class="block">${warehouse.message(code: 'default.lastUpdateAfter.label', default: 'Last updated after')}</label>
                        <g:jqueryDatePicker id="statusStartDate" name="statusStartDate" placeholder="Start date"
                                            size="40" numberOfMonths="2" changeMonthAndYear="false"
                                            value="${statusStartDate}" format="MM/dd/yyyy"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td>
                        <label class="block">${warehouse.message(code: 'default.lastUpdatedBefore.label', default: 'Last updated before')}</label>
                        <g:jqueryDatePicker id="statusEndDate" name="statusEndDate" placeholder="End date" size="40"
                                            numberOfMonths="2" changeMonthAndYear="true"
                                            value="${statusEndDate}" format="MM/dd/yyyy"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td colspan="2">
                        <div class="center">

                            <button type="submit" class="button icon search" name="search" value="true">
                                <warehouse:message code="default.search.label"/>
                            </button>

                            <g:link controller="order" action="list" class="button icon reload">
                                <warehouse:message code="default.button.cancel.label"/>
                            </g:link>
                        </div>
                    </td>
                </tr>
            </table>
        </div>
    </g:form>
</div>