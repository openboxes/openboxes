<div>
	<g:form method="GET" controller="shipment" action="list">
        <g:hiddenField name="type" value="${params.type}"/>
		<div class="box">
            <h2><warehouse:message code="default.filters.label"/></h2>
			<table>
				<tr>
					<td>
						<g:hiddenField name="max" value="${params.max?:10 }"/>
                        <div>
                            <label>${warehouse.message(code:'default.searchTerms.label', default: "Search terms")}</label>
                        </div>
                        <div>
                            <g:textField name="terms"
                                value="${params.terms}"
                                style="width: 100%;"
                                placeholder="${warehouse.message(code:'shipping.searchTerms.label', default: 'Search by name, shipment number')}"
                                class="text medium"/>
                        </div>
					</td>
				</tr>
                <tr class="prop">
                    <td>
                        <div>
                            <div>
                                <label>${warehouse.message(code: 'shipping.status.label')}</label>
                            </div>
                            <div>
                                <g:select name="status" class="chzn-select-deselect"
                                          from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
                                          optionKey="name" optionValue="${{format.metadata(obj:it)}}"
                                          value="${status}"
                                          noSelection="['':warehouse.message(code:'default.all.label')]" />
                            </div>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td>
                        <div>
                            <div>
                                <label>${warehouse.message(code:'shipping.shipmentType.label', default: "Shipment type")}</label>
                            </div>
                            <div>
                                <g:select name="shipmentType" class="chzn-select-deselect"
                                    from="${org.pih.warehouse.shipping.ShipmentType.list()}"
                                    optionKey="id" optionValue="${{format.metadata(obj:it)}}"
                                    value="${shipmentType}"
                                    noSelection="['':warehouse.message(code:'default.all.label')]" />
                            </div>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td>
                        <g:if test="${incoming}">
                            <label>${warehouse.message(code: 'shipping.origin.label')}</label>
                            <div>
                            <g:select name="origin" class="chzn-select-deselect"
                                  from="${org.pih.warehouse.core.Location.list().sort()}"
                                  optionKey="id" optionValue="name" value="${origin}"
                                  noSelection="['null':warehouse.message(code:'default.all.label')]" />
                            </div>
                        </g:if>
                        <g:else>
                            <label>${warehouse.message(code: 'shipping.destination.label')}</label>
                            <div>
                                <g:select name="destination" class="chzn-select-deselect"
                                      from="${org.pih.warehouse.core.Location.list().sort()}"
                                      optionKey="id" optionValue="name" value="${destination}"
                                      noSelection="['null':warehouse.message(code:'default.all.label')]" />
                            </div>
                        </g:else>
                    </td>
                </tr>
                <%--
                <tr class="prop">
                    <td>
                        <label>${warehouse.message(code: 'shipping.statusDate.label', default: 'Status date')}</label>
                        <a href="javascript:void(0);" class="clear-dates"><warehouse:message code="default.clear.label"/></a>
                        <div>
                            <g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
                            value="${params.statusStartDate}" format="MM/dd/yyyy"/>

                            <g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
                            value="${params.statusEndDate}" format="MM/dd/yyyy"/>
                        </div>


                    </td>
                </tr>
                --%>
                <tr class="prop">
                    <td>
                        <label>${warehouse.message(code: 'default.lastUpdatedAfter.label', default: 'Last updated after')}</label>
                        <div>
                            <g:jqueryDatePicker id="lastUpdatedFrom" name="lastUpdatedFrom" numberOfMonths="2" changeMonthAndYear="true"
                                                value="${lastUpdatedFrom}" format="MM/dd/yyyy"/>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td>
                        <label>${warehouse.message(code: 'default.lastUpdatedBefore.label', default: 'Last updated before')}</label>
                        <div>
                            <g:jqueryDatePicker id="lastUpdatedTo" name="lastUpdatedTo" numberOfMonths="2" changeMonthAndYear="true"
                                                value="${lastUpdatedTo}" format="MM/dd/yyyy"/>
                        </div>

                    </td>
                </tr>
                <%--
                <tr class="prop">
                    <td>
                        <label>${warehouse.message(code: 'shipping.status.label')}</label>
                        <div>
                            <g:select name="status" class="filter"
                                from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
                                optionKey="name" optionValue="${{format.metadata(obj:it)}}"
                                value="${status}"
                                noSelection="['':warehouse.message(code:'default.all.label')]" />
                        </div>
                    </td>
                </tr>
                --%>
                <tr class="prop">
                    <td>
						<div class="right">
							<button type="submit" class="button icon search" name="search" value="true">
								<warehouse:message code="default.search.label"/>
							</button>
						</div>
					</td>
				</tr>
			</table>
        </div>
	</g:form>
</div>


