<table>
	<thead>
		<tr>
			<th>
				<warehouse:message code="default.actions.label"/>
			</th>
			<g:sortableColumn property="status"
				title="${warehouse.message(code: 'default.status.label', default: 'Status')}" />
				
			<g:sortableColumn property="type"
				title="${warehouse.message(code: 'default.type.label', default: 'Type')}" />
				
			<g:sortableColumn property="requestNumber"
				title="${warehouse.message(code: 'requisition.requestNumber.label', default: 'Request number')}" />
				
				
			<g:sortableColumn property="description"
				title="${warehouse.message(code: 'default.description.label', default: 'Description')}" />
			<th>
				<warehouse:message code="requisition.requisitionItem.label"/>
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
           		<td colspan="6" class="center">
           			<warehouse:message code="requisition.noRequisitionsMatchingCriteria.message"/>
	           	</td>
			</tr>     
		</g:unless>	
		<g:each in="${requisitions}" status="i" var="requisition">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>			
					<g:render template="/requisition/actions" model="[requisition:requisition]"/>
				</td>
				<td>
					<format:metadata obj="${requisition?.status}"/>
				</td>
				<td>
					<format:metadata obj="${requisition?.type}"/>
				</td>
				<td>
					${requisition.requestNumber }
				</td>
				<td>
					<g:link action="show" id="${requisition.id}">						
						${fieldValue(bean: requisition, field: "name")}
					</g:link>
				</td>
				<td>${requisition?.requisitionItems?.size() }</td>
				<td>${requisition.requestedBy}</td>

				<td><format:datetime obj="${requisition.lastUpdated}" /></td>

			</tr>
		</g:each>
	</tbody>
</table>
