<div>
	<g:form method="GET" controller="shipment" action="list">
		<div class="box">
            <h2><warehouse:message code="default.filters.label"/></h2>
			<table>
				<tr>
					<td>
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
                                          optionKey="name" optionValue="${format.metadata(obj:it)}"
                                          value="${status}"
                                          noSelection="['':warehouse.message(code:'default.all.label')]" />
                            </div>
                        </div>
                    </td>
                </tr>
                <tr class="prop">
                    <td>
                        <div>
                            <label>${warehouse.message(code:'shipment.directionType.label', default: "Direction Type")}</label>
                        </div>
                        <div>
                            <g:select name="type" from="['outgoing':'Outbound', 'incoming': 'Inbound']" class="chzn-select-deselect"
                                      optionKey="key" optionValue="value" value="${params.type}"/>
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
                                <g:selectLocation name="origin" class="chzn-select-deselect"
                                                  optionKey="id" optionValue="name" value="${origin}"
                                                  noSelection="['null':warehouse.message(code:'default.all.label')]" />

                            </div>
                        </g:if>
                        <g:else>
                            <label>${warehouse.message(code: 'shipping.destination.label')}</label>
                            <div>
                                <g:selectLocation name="destination" class="chzn-select-deselect"
                                                  optionKey="id" optionValue="name" value="${destination}"
                                                  noSelection="['null':warehouse.message(code:'default.all.label')]" />
                            </div>
                        </g:else>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="left">
                        <label>${warehouse.message(code: 'default.limit.label', default: 'Limit')}</label>
                        <g:select name="max" from="[10,25,50,100,250,500,1000,2000,5000,10000]" noSelection="['':'']" class="chzn-select-deselect" value="${params.max}"></g:select>

                    </td>
                </tr>
                <tr class="prop">
                    <td colspan="2">
						<div class="center">
							<button type="submit" class="button icon search" name="search" value="true">
								<warehouse:message code="default.search.label"/>
							</button>
                            <g:link controller="shipment" action="list" class="button icon reload">
                                <warehouse:message code="default.button.reset.label" default="Reset"/>
                            </g:link>
						</div>
					</td>
				</tr>
			</table>
        </div>
	</g:form>
</div>
