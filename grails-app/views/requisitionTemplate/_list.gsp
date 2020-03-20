<table>
	<thead>
		<tr>
            <g:isUserAdmin>
                <th>
                    <warehouse:message code="default.actions.label"/>
                </th>
            </g:isUserAdmin>
            <th>
                <warehouse:message code="requisition.isPublished.label"/>
            </th>
            <th>
                <warehouse:message code="default.name.label"/>
            </th>
            <th>
                <warehouse:message code="requisition.origin.label"/>
            </th>
            <th>
                <warehouse:message code="requisition.destination.label"/>
            </th>
			<th>
				<warehouse:message code="requisition.requisitionItems.label"/>
			</th>
			<g:sortableColumn property="requestedBy"
				title="${warehouse.message(code: 'requisition.requestedBy.label', default: 'Requested by')}" />

            <g:sortableColumn property="createdBy"
                              title="${warehouse.message(code: 'default.createdBy.label', default: 'Created by')}" />

            <g:sortableColumn property="updateBy"
                              title="${warehouse.message(code: 'default.updatedBy.label', default: 'Updated by')}" />

            <g:sortableColumn property="dateCreated"
                              title="${warehouse.message(code: 'default.dateCreated.label', default: 'Date created')}" />

            <g:sortableColumn property="lastUpdated"
				title="${warehouse.message(code: 'default.lastUpdated.label', default: 'Last updated')}" />



        </tr>
	</thead>
	<tbody>
        <g:unless test="${requisitions}">
           	<tr class="prop odd">
           		<td colspan="13" class="center">
                    <div class="empty">
           			    <warehouse:message code="requisition.noRequisitionsMatchingCriteria.message"/>
                    </div>
	           	</td>
			</tr>
		</g:unless>
		<g:each in="${requisitions}" status="i" var="requisition">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                <g:isUserAdmin>
                    <td>
                        <g:render template="/requisitionTemplate/actions" model="[requisition:requisition]"/>
                    </td>
                </g:isUserAdmin>
                <td>
                    <g:if test="${requisition.isPublished}">
                        <div class="tag tag-alert">
                            <warehouse:message code="default.published.label" default="Published"/>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="tag tag-danger">
                            <warehouse:message code="default.draft.label" default="Draft"/>
                        </div>
                    </g:else>
                </td>
                <td>
                    <g:link action="show" id="${requisition?.id}">
                        <format:metadata obj="${requisition?.name}"/>
                    </g:link>
                </td>
                <td>
                    <format:metadata obj="${requisition?.origin?.name}"/>
                </td>
                <td>
                    <format:metadata obj="${requisition?.destination?.name}"/>
                </td>
				<td>
                    ${requisition?.requisitionItems?.size()}
				</td>
				<td>${requisition.requestedBy}</td>

                <td>${requisition.createdBy}</td>
                <td>${requisition.updatedBy}</td>
                <td><format:datetime obj="${requisition.dateCreated}" /></td>
                <td><format:datetime obj="${requisition.lastUpdated}" /></td>

			</tr>
		</g:each>
	</tbody>
</table>
