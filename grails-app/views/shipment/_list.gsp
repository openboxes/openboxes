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
					${warehouse.message(code: 'shipping.shipment.label')}
				</th>
				<th><g:if test="${incoming}">
						<label class="block"><warehouse:message
								code="default.origin.label" /></label>
					</g:if> <g:else>
						<label class="block"><warehouse:message
								code="default.destination.label" /></label>
					</g:else></th>
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
			<tr class="odd">
				<td></td>
				<td colspan="2"><g:select name="shipmentType" class="filter"
						from="${org.pih.warehouse.shipping.ShipmentType.list()}"
						optionKey="id" optionValue="${{format.metadata(obj:it)}}"
						value="${shipmentType}"
						noSelection="['':warehouse.message(code:'default.all.label')]" />
				</td>
				<td><g:if test="${incoming}">
						<g:select name="origin" class="filter"
							from="${org.pih.warehouse.core.Location.list().sort()}"
							optionKey="id" optionValue="name" value="${origin}"
							noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;
				</g:if> <g:else>
						<g:select name="destination" class="filter"
							from="${org.pih.warehouse.core.Location.list().sort()}"
							optionKey="id" optionValue="name" value="${destination}"
							noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;
				</g:else></td>
				<%-- 
                    	<td>
	           	<g:jqueryDatePicker id="statusStartDate" name="statusStartDate"
					value="${statusStartDate}" format="MM/dd/yyyy"/>
					
                     	${warehouse.message(code: 'default.dateTo.label')}
				<g:jqueryDatePicker id="statusEndDate" name="statusEndDate"
				value="${statusEndDate}" format="MM/dd/yyyy"/>
                    	
                    		<a href="javascript:void(0);" class="clear-dates"><warehouse:message code="default.clear.label"/></a>
                    	
                    	</td>
                    	--%>
				<td>
					<%-- 
					<g:select name="status" class="filter"
						from="${org.pih.warehouse.shipping.ShipmentStatusCode.list()}"
						optionKey="name" optionValue="${{format.metadata(obj:it)}}"
						value="${status}"
						noSelection="['':warehouse.message(code:'default.all.label')]" />&nbsp;&nbsp;
					--%>
				</td>
				<td></td>
			</tr>



			<g:if test="${shipments.size()==0}">
				<tr class="even">
					<td colspan="7" class="center"><warehouse:message
							code="shipping.noShipmentsMatchingConditions.message" /></td>
				</tr>
			</g:if>

			<g:else>
				<g:each var="shipmentInstance" in="${shipments}" status="i">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td>
							<div class="action-menu">
								<button class="action-btn">
									<img
										src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" />
									<img
										src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
								</button>
								<div class="actions" style="position: absolute; display: none;">
									<g:render template="listShippingMenuItems"
										model="[shipmentInstance:shipmentInstance]" />
								</div>
							</div>
						</td>
						<td class="left"><img
							src="${createLinkTo(dir:'images/icons/shipmentType',file: 'ShipmentType' + format.metadata(obj:shipmentInstance?.shipmentType, locale:null) + '.png')}"
							alt="${format.metadata(obj:shipmentInstance?.shipmentType)}"
							style="vertical-align: middle; width: 24px; height: 24px;" /></td>
						<td class="left"><g:link action="showDetails"
								id="${shipmentInstance.id}">
								${fieldValue(bean: shipmentInstance, field: "name")}
							</g:link></td>
						<td align="center"><g:if test="${incoming}">
								${fieldValue(bean: shipmentInstance, field: "origin.name")}
							</g:if> <g:else>
								${fieldValue(bean: shipmentInstance, field: "destination.name")}
							</g:else></td>

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
			</g:else>
		</tbody>
	</table>
</div>