<div class="filters">
	<g:form method="GET" controller="shipment" action="list">
        <g:hiddenField name="type" value="${params.type}"/>

        <h3><warehouse:message code="default.filters.label"/></h3>

        <g:hiddenField name="max" value="${params.max?:10 }"/>


        <label>${warehouse.message(code:'default.searchTerms.label', default: "Search terms")}</label>
        <div class="filter-list-item">
            <g:textField name="terms"
                value="${params.terms}"
                placeholder="${warehouse.message(code:'shipping.searchTerms.label', default: 'Search by name, shipment number')}"
                class="text medium"/>
        </div>

            <%-- working on this - can't get the translation to work correctly because there's no way to pass number of days or weeks as an argument --%>
            <%--
            <tr class="prop">
                <td>
                    <div>
                        <div>
                            <label>${warehouse.message(code: 'shipping.lastUpdated.label')}</label>
                        </div>
                        <div>
                            <g:select name="lastUpdated" class="chzn-select-deselect"
                                      from="${[1:'default.lastUpdated.day.label', 7:'default.lastUpdated.week.label', 30: 'default.lastUpdated.month.label', 365:'default.lastUpdated.year.label' ]}"
                                      optionKey="key"
                                      optionValue="${{warehouse.message(code:it.value, args:[1])}}"
                                      value="${params.statusChanged}"
                                      noSelection="['':warehouse.message(code:'default.all.label')]" />
                        </div>
                    </div>
                </td>
            </tr>
            --%>

        <label>${warehouse.message(code: 'shipping.status.label')}</label>
        <div class="filter-list-item">
            <div>
                <g:select name="status" class="chzn-select-deselect"
                          from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
                          optionKey="name" optionValue="${format.metadata(obj:it)}"
                          value="${status}"
                          noSelection="['':warehouse.message(code:'default.all.label')]" />
            </div>
        </div>

        <label>${warehouse.message(code:'shipping.shipmentType.label', default: "Shipment type")}</label>
        <div class="filter-list-item">
            <g:select name="shipmentType" class="chzn-select-deselect"
                from="${org.pih.warehouse.shipping.ShipmentType.list()}"
                optionKey="id" optionValue="${{format.metadata(obj:it)}}"
                value="${shipmentType}"
                noSelection="['':warehouse.message(code:'default.all.label')]" />

        </div>

        <g:if test="${incoming}">
            <label>${warehouse.message(code: 'shipping.origin.label')}</label>
            <div class="filter-list-item">
            <g:select name="origin" class="chzn-select-deselect"
                  from="${org.pih.warehouse.core.Location.list().sort()}"
                  optionKey="id" optionValue="name" value="${origin}"
                  noSelection="['null':warehouse.message(code:'default.all.label')]" />
            </div>
        </g:if>
        <g:else>
            <label>${warehouse.message(code: 'shipping.destination.label')}</label>
            <div class="filter-list-item">
                <g:select name="destination" class="chzn-select-deselect"
                      from="${org.pih.warehouse.core.Location.list().sort()}"
                      optionKey="id" optionValue="name" value="${destination}"
                      noSelection="['null':warehouse.message(code:'default.all.label')]" />
            </div>
        </g:else>

        <%--
                <label>${warehouse.message(code: 'shipping.statusDate.label', default: 'Status date')}</label>
                <a href="javascript:void(0);" class="clear-dates"><warehouse:message code="default.clear.label"/></a>
                <div>
                    <g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
                    value="${params.statusStartDate}" format="MM/dd/yyyy"/>

                    <g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
                    value="${params.statusEndDate}" format="MM/dd/yyyy"/>
                </div>
        --%>

        <label>${warehouse.message(code: 'default.lastUpdated.label', default: 'Last updated')}</label>
        <div class="filter-list-item">
            <g:jqueryDatePicker id="lastUpdatedFrom" name="lastUpdatedFrom" numberOfMonths="2" changeMonthAndYear="true"
                                placeholder="${warehouse.message(code: 'default.from.label', default: 'From')}"
                                value="${lastUpdatedFrom}" format="MM/dd/yyyy"/>

        </div>
        <div class="filter-list-item">

                    <g:jqueryDatePicker id="lastUpdatedTo" name="lastUpdatedTo" numberOfMonths="2" changeMonthAndYear="true"
                            placeholder="${warehouse.message(code: 'default.to.label', default: 'To')}"
                                        value="${lastUpdatedTo}" format="MM/dd/yyyy" />
        </div>


        <label>${warehouse.message(code: 'default.dateCreated.label', default: 'Date created')}</label>
        <div class="filter-list-item">
            <g:jqueryDatePicker id="dateCreatedFrom" name="dateCreatedFrom" numberOfMonths="2" changeMonthAndYear="true"
                                placeholder="${warehouse.message(code: 'default.from.label', default: 'From')}"
                                value="${dateCreatedFrom}" format="MM/dd/yyyy"/>

        </div>
        <div class="filter-list-item">
            <g:jqueryDatePicker id="dateCreatedTo" name="dateCreatedTo" numberOfMonths="2" changeMonthAndYear="true"
                                placeholder="${warehouse.message(code: 'default.to.label', default: 'To')}"
                                value="${dateCreatedTo}" format="MM/dd/yyyy"/>

        </div>
<%--

        <label>${warehouse.message(code: 'shipping.status.label')}</label>
        <div>
            <g:select name="status" class="filter"
                from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
                optionKey="name" optionValue="${{format.metadata(obj:it)}}"
                value="${status}"
                noSelection="['':warehouse.message(code:'default.all.label')]" />
        </div>

--%>
        <div class="buttons">
            <button type="submit" class="button icon search" name="search" value="true">
                <warehouse:message code="default.search.label"/>
            </button>
            <g:link controller="shipment" action="list" class="button icon reload">
                <warehouse:message code="default.button.cancel.label"/>
            </g:link>
        </div>


	</g:form>
</div>


