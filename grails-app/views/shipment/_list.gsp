<div class="box">
    <h2>${warehouse.message(code:'shipments.label')}</h2>
	<table class="dataTable">
		<thead>
			<tr>
                <%--
				<th style="width: 1%">
					${warehouse.message(code: 'default.actions.label')}
				</th>
				--%>
				<th>
				</th>
				<th>
				</th>
                <th>
                    ${warehouse.message(code: 'default.status.label')}
                </th>
				<th class="center">
					${warehouse.message(code: 'shipping.shipmentNumber.label')}
				</th>
				<th>
					${warehouse.message(code: 'shipping.shipment.label')}
				</th>
				<th class="center">
					${warehouse.message(code: 'shipping.shipmentItems.label', default: "Items")}
				</th>
				<th>
                    <warehouse:message code="default.origin.label" />
				</th>
                <th>
                    <warehouse:message code="default.destination.label" />
			    </th>
                <th>
                    ${warehouse.message(code: 'shipping.shippingDate.label', default: 'Shipped')}
                </th>
                <th>
                    ${warehouse.message(code: 'shipping.receivingDate.label', default: 'Received')}
                </th>
                <th>
                    ${warehouse.message(code: 'default.dateCreated.label')}
                </th>
				<th>
					${warehouse.message(code: 'default.lastUpdated.label')}
				</th>
			</tr>
        </thead>
		<tbody>

			<g:each var="shipmentInstance" in="${shipments}" status="i">
				<tr >
                    <%--
					<td>
						<div class="action-menu">
							<button class="action-btn">
								<img
									src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
							</button>
							<div class="actions" style="position: absolute; display: none;">
								<g:render template="listShippingMenuItems"
									model="[shipmentInstance:shipmentInstance]" />
							</div>
						</div>
					</td>
					--%>
                    <td>
                        <g:checkBox class="shipment-item ${shipmentInstance?.status.code}" name="shipment.id" value="${shipmentInstance.id}" checked="${params['shipment.id']}" />
                    </td>
					<td class="center middle"><img
						src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + format.metadata(obj:shipmentInstance?.shipmentType, locale:null) + '.png')}"
						alt="${format.metadata(obj:shipmentInstance?.shipmentType)}"
						style="vertical-align: middle; width: 24px; height: 24px;" />
					</td>
                    <td class="middle">
                        <format:metadata obj="${shipmentInstance?.currentStatus}" />
                    </td>
					<td class="middle center">
						<g:link action="showDetails" id="${shipmentInstance.id}">
							${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}
						</g:link>
					</td>

					<td class="middle left shipment-name">
                        <g:link action="showDetails" id="${shipmentInstance.id}">
							${fieldValue(bean: shipmentInstance, field: "name")}
						</g:link>
					</td>
					<td class="middle center">
						${shipmentInstance?.shipmentItemCount}
					</td>
					<td class="middle">
                        ${fieldValue(bean: shipmentInstance, field: "origin.name")}
					</td>
                    <td class="middle">
                        ${fieldValue(bean: shipmentInstance, field: "destination.name")}
					</td>

					<td class="middle">
                        <g:set var="today" value="${new Date() }" />
                        <g:if test="${shipmentInstance?.actualShippingDate}">
                            <div title="${g.formatDate(date: shipmentInstance?.actualShippingDate)}">
                                <g:if test="${shipmentInstance?.actualShippingDate?.equals(today) }">
                                    <warehouse:message code="default.today.label" />
                                </g:if>
                                <g:else>
                                    <g:prettyDateFormat date="${shipmentInstance?.actualShippingDate}" />
                                </g:else>
                            </div>
						</g:if>
                        <g:else>
                            <div title="${g.formatDate(date: shipmentInstance?.expectedShippingDate)}">
                                Expected
                                <g:if
                                    test="${shipmentInstance?.expectedShippingDate?.equals(today) }">
                                    <warehouse:message code="default.today.label" />
                                </g:if>
                                <g:else>
                                    <g:prettyDateFormat
                                        date="${shipmentInstance?.expectedShippingDate}" />
                                </g:else>
                            </div>
						</g:else>
                    </td>
                    <td class="middle">
                        <g:set var="today" value="${new Date() }" />
                        <g:if test="${shipmentInstance?.actualDeliveryDate}">
                            <div title="${g.formatDate(date: shipmentInstance?.actualDeliveryDate)}">
                                <g:if test="${shipmentInstance?.actualDeliveryDate?.equals(today) }">
                                    <warehouse:message code="default.today.label" />
                                </g:if>
                                <g:else>
                                    <g:prettyDateFormat date="${shipmentInstance?.actualDeliveryDate}" />
                                </g:else>
                        </g:if>
                        <g:else>
                            <div title="${g.formatDate(date: shipmentInstance?.expectedDeliveryDate)}">
                                Expected
                                <g:if test="${shipmentInstance?.expectedDeliveryDate?.equals(today) }">
                                    <warehouse:message code="default.today.label" />
                                </g:if>
                                <g:else>
                                    <g:prettyDateFormat
                                            date="${shipmentInstance?.expectedDeliveryDate}" />
                                </g:else>
                            </div>
                        </g:else>
                    </td>
                    <td class="middle center">
                        <div title="${g.formatDate(date: shipmentInstance?.dateCreated)}">
                            ${g.formatDate(date: shipmentInstance?.dateCreated)}
                        </div>
                    </td>
					<td class="middle center">
                        <div title="${g.formatDate(date: shipmentInstance?.lastUpdated)}">
                            ${g.formatDate(date: shipmentInstance?.lastUpdated)}
                        </div>
                    </td>
				</tr>
			</g:each>
		</tbody>


        <g:isSuperuser>
            <g:if test="${statusCode==org.pih.warehouse.shipping.ShipmentStatusCode.SHIPPED || statusCode == org.pih.warehouse.shipping.ShipmentStatusCode.RECEIVED}">
                <tfoot>
                    <tr>
                        <td>
                            <input type="checkbox" class="checkAll"/>
                        </td>
                        <td colspan="11">
                            <g:if test="${statusCode==org.pih.warehouse.shipping.ShipmentStatusCode.SHIPPED}">
                                <div class="button-group">
                                    <button type="submit" class="button icon approve bulkReceive">
                                        <warehouse:message code="bulk.receive.label" default="Bulk Receive"/>
                                    </button>
                                    <button type="submit" class="button icon tag bulkMarkAsReceived">
                                        <warehouse:message code="bulk.markAsReceived.label" default="Bulk Mark as Received"/>
                                    </button>
                                </div>
                                <div class="button-group">
                                    <button type="submit" class="button icon approve bulkRollback">
                                        <warehouse:message code="bulk.receive.label" default="Bulk Rollback"/>
                                    </button>
                                </div>
                            </g:if>
                            <g:elseif test="${statusCode==org.pih.warehouse.shipping.ShipmentStatusCode.RECEIVED}">
                                <div class="button-group">
                                    <button type="submit" class="button icon approve bulkRollback">
                                        <warehouse:message code="bulk.receive.label" default="Bulk Rollback"/>
                                    </button>
                                </div>
                            </g:elseif>


                        </td>
                    </tr>
                </tfoot>
            </g:if>
        </g:isSuperuser>
	</table>
</div>
