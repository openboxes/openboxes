<div class="list">
	<table>
		<thead>
			<tr>
				<th>
					${warehouse.message(code: 'default.actions.label')}
				</th>
				<th>
					${warehouse.message(code: 'shipping.shipmentType.label')}
				</th>
				<th>
					${warehouse.message(code: 'shipping.shipmentNumber.label')}
				</th>
				<th>
					${warehouse.message(code: 'shipping.shipment.label')}
				</th>
				<th>
                    <label class="block"><warehouse:message
								code="default.origin.label" /></label>
				</th>
                <th>
                    <label class="block"><warehouse:message
								code="default.destination.label" /></label>
			    </th>
				<%-- 
                    	<th>
                    		<label class="block">${warehouse.message(code: 'shipping.expectedShippingDate.label')}</label>
                    	</th>
                    	--%>
				<th><label class="block">
						${warehouse.message(code: 'default.status.label')}
				</label></th>
				<th>
					${warehouse.message(code: 'default.lastUpdated.label')}
				</th>
			</tr>

		</thead>
		<tbody>
			<g:each var="shipmentInstance" in="${shipments}" status="i">
				<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
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
					<td class="center"><img
						src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + format.metadata(obj:shipmentInstance?.shipmentType, locale:null) + '.png')}"
						alt="${format.metadata(obj:shipmentInstance?.shipmentType)}"
						style="vertical-align: middle; width: 24px; height: 24px;" />
					</td>
					<td class="left">
						<g:link action="showDetails" id="${shipmentInstance.id}">
							S-${fieldValue(bean: shipmentInstance, field: "shipmentNumber")}
						</g:link>
					</td>
						
					<td class="left shipment-name"><g:link action="showDetails"
							id="${shipmentInstance.id}">
							${fieldValue(bean: shipmentInstance, field: "name")}
						</g:link></td>
					<td align="center">
                        ${fieldValue(bean: shipmentInstance, field: "origin.name")}
					</td>
                    <td>
                        ${fieldValue(bean: shipmentInstance, field: "destination.name")}
					</td>

					<td><g:set var="today" value="${new Date() }" /> <format:metadata
							obj="${shipmentInstance?.status.code}" /> <g:if
							test="${shipmentInstance?.status.date}">
						<g:if test="${shipmentInstance?.status?.date?.equals(today) }">
								<warehouse:message code="default.today.label" />
							</g:if>
							<g:else>
								<g:prettyDateFormat date="${shipmentInstance?.status?.date}" />
							</g:else>
							<%-- 
						<format:date obj="${shipmentInstance?.status.date}"/>
						--%>
						</g:if> <g:else>
						- Expected to ship 
						<%-- 
						<format:date obj="${shipmentInstance?.expectedShippingDate}"/>
						--%>
							<g:if
								test="${shipmentInstance?.expectedShippingDate?.equals(today) }">
								<warehouse:message code="default.today.label" />
							</g:if>
							<g:else>
								<g:prettyDateFormat
									date="${shipmentInstance?.expectedShippingDate}" />
							</g:else>
						</g:else></td>
					<td align="center"><format:date
							obj="${shipmentInstance?.lastUpdated}" /></td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>
