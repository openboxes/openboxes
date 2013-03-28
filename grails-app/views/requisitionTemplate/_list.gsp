<table>
	<thead>
		<tr>
			<th>
				<warehouse:message code="default.actions.label"/>
			</th>

            <%--
			<g:sortableColumn property="status"
				title="${warehouse.message(code: 'default.status.label', default: 'Status')}" />
				
				
			<g:sortableColumn property="requestNumber"
				title="${warehouse.message(code: 'requisition.requestNumber.label', default: 'Request number')}" />
				
				
			<g:sortableColumn property="description"
				title="${warehouse.message(code: 'default.description.label', default: 'Description')}" />
			--%>

			<g:sortableColumn property="type"
				title="${warehouse.message(code: 'default.type.label', default: 'Type')}" />
            <g:sortableColumn property="type"
                title="${warehouse.message(code: 'commodityClass.label', default: 'Commodity Class')}" />
            <th>
                <warehouse:message code="requisition.origin.label"/>
            </th>
            <th>
                <warehouse:message code="requisition.destination.label"/>
            </th>
			<th>
				<warehouse:message code="requisition.requisitionItems.label"/>
			</th>
			<g:sortableColumn property="createdBy"
				title="${warehouse.message(code: 'default.createdBy.label', default: 'Created by')}" />
			
			<g:sortableColumn property="lastUpdated"
				title="${warehouse.message(code: 'default.lastUpdated.label', default: 'Last updated')}" />

		</tr>
	</thead>
	<tbody>
        <g:unless test="${requisitions}">
           	<tr class="prop odd">
           		<td colspan="8" class="center">
           			<warehouse:message code="requisition.noRequisitionsMatchingCriteria.message"/>
	           	</td>
			</tr>     
		</g:unless>	
		<g:each in="${requisitions}" status="i" var="requisition">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>			
					<g:render template="/requisitionTemplate/actions" model="[requisition:requisition]"/>
				</td>
                <%--
				<td>
					<label class="status"><format:metadata obj="${requisition?.status}"/></label>
				</td>
				<td>
					${requisition.requestNumber }
				</td>
				<td>
					<g:link action="show" id="${requisition.id}">						
						${fieldValue(bean: requisition, field: "name")}
					</g:link>
				</td>
				--%>
				<td>
					<format:metadata obj="${requisition?.type}"/>
				</td>
                <td>
                    <format:metadata obj="${requisition?.commodityClass}"/>
                </td>
                <td>
                    <format:metadata obj="${requisition?.origin?.name}"/>
                </td>
                <td>
                    <format:metadata obj="${requisition?.destination?.name}"/>
                </td>
				<td>
					${warehouse.message(code: 'requisition.numRequisitionItems.label', args:[requisition?.requisitionItems?.size()?:0]) }
				</td>
				<td>${requisition.requestedBy}</td>

				<td><format:datetime obj="${requisition.lastUpdated}" /></td>

			</tr>
		</g:each>
	</tbody>
</table>
