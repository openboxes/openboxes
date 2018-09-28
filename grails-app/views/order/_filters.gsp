<div>
    <g:form id="listForm" action="list" method="GET">
        <g:hiddenField name="type" value="${params.type}"/>
        <g:hiddenField name="max" value="${params.max ?: 10}"/>
        <div class="box">
            <h2><warehouse:message code="default.filters.label"/></h2>
            <table>
                <tr class="prop">
                    <td>
                        <label class="block">${warehouse.message(code: 'order.orderNumber.label')}</label>
                        <div>
                            <g:textField class="text" id="orderNumber" name="orderNumber" value="${params.orderNumber}" style="width:100%"/>

                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td>
                        <label class="block">${warehouse.message(code: 'order.orderTypeCode.label')}</label>
                        <div>
                            <g:select id="orderTypeCode" name="orderTypeCode"
                                      from="${org.pih.warehouse.order.OrderTypeCode.list()}" class="chzn-select-deselect"
                                      optionValue="${{ format.metadata(obj: it) }}" value="${params.orderTypeCode}"
                                      noSelection="['': warehouse.message(code: 'default.all.label')]"/>

                        </div>
                    </td>
                </tr>

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
                        <label><warehouse:message code="order.origin.label"/></label>
                        <div>
                            <g:selectLocation id="origin" name="origin" class="chzn-select-deselect"
                                      optionKey="id" optionValue="name" value="${params.origin}" noSelection="['':'']" />
                        </div>
                    </td>
                </tr>

                <tr class="prop">
                    <td>
                        <label><warehouse:message code="order.destination.label"/></label>
                        <div>
                            <g:selectLocation id="destination" name="destination" class="chzn-select-deselect"
                                      optionKey="id" optionValue="name"
                                      value="${params.destination?:session?.warehouse?.id}" noSelection="['':'']" />
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
                        <div>
                            <g:jqueryDatePicker id="statusStartDate" name="statusStartDate" placeholder="Start date"
                                                size="40" numberOfMonths="2" changeMonthAndYear="false"
                                                value="${statusStartDate}" format="MM/dd/yyyy"/>
                            <a href="javascript:void(0);" id="clearStartDate">Clear</a>
                        </div>
                    </td>
                </tr>

                <tr class="prop">
                    <td>
                        <label class="block">${warehouse.message(code: 'default.lastUpdatedBefore.label', default: 'Last updated before')}</label>
                        <div>
                            <g:jqueryDatePicker id="statusEndDate" name="statusEndDate" placeholder="End date" size="40"
                                                numberOfMonths="2" changeMonthAndYear="true"
                                                value="${statusEndDate}" format="MM/dd/yyyy"/>
                            <a href="javascript:void(0);" id="clearEndDate">Clear</a>
                        </div>
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