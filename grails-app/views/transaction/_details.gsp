<div class="box">
	<table>
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="transaction.transactionNumber.label" /></label></td>
			<td class="value"><span class="transactionNumber"> <g:if
						test="${transactionInstance?.transactionNumber }">
						${transactionInstance?.transactionNumber }
					</g:if> <g:else>
						<span class="fade"><warehouse:message
								code="transaction.new.label" /></span>
					</g:else>
			</span></td>
		</tr>
		
		
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="transaction.type.label" /></label></td>
			<td
				class="value transaction-type-${transactionInstance?.transactionType?.transactionCode?.name()?.toLowerCase()}">
					<format:metadata obj="${transactionInstance?.transactionType}" /> <g:if
						test="${transactionInstance?.source }">
						<warehouse:message code="default.from.label" />
						${transactionInstance?.source?.name }
					</g:if> <g:if test="${transactionInstance?.destination }">
						<warehouse:message code="default.to.label" />
						${transactionInstance?.destination?.name }
					</g:if>
			</td>
		</tr>
		<g:if test="${transactionInstance?.outgoingShipment }">
			<tr class="prop">
				<td class="name"><label><warehouse:message
							code="shipping.shipment.label" /></label></td>
				<td class="value"><g:link controller="shipment"
						action="showDetails"
						id="${transactionInstance?.outgoingShipment?.id }">
						${transactionInstance?.outgoingShipment?.name}
					</g:link></td>
			</tr>
		</g:if>
		<g:if test="${transactionInstance?.incomingShipment }">
			<tr class="prop">
				<td class="name"><label><warehouse:message
							code="shipping.shipment.label" /></label></td>
				<td class="value"><g:link controller="shipment"
						action="showDetails"
						id="${transactionInstance?.incomingShipment?.id }">
						${transactionInstance?.incomingShipment?.name}
					</g:link></td>
			</tr>
		</g:if>
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="transaction.date.label" /></label></td>
			<td class="value"><span> <format:datetime
						obj="${transactionInstance?.transactionDate}" />
			</span></td>
		</tr>
		<tr id="inventory" class="prop">
			<td class="name"><label><warehouse:message
						code="inventory.label" /></label></td>
			<td class="value"><span > <format:metadata
						obj="${transactionInstance?.inventory?.warehouse }" />
			</span></td>
		</tr>
		<g:if test="${transactionInstance?.comment }">
			<tr class="prop">
				<td class="name"><label><warehouse:message
							code="default.comment.label" /></label></td>
				<td class="value"><span> ${transactionInstance?.comment }
				</span></td>
			</tr>
		</g:if>
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="transaction.createdBy.label" /></label></td>
			<td class="value">
				${transactionInstance?.createdBy}
			</td>
		</tr>
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="default.dateCreated.label" /></label></td>
			<td class="value"><format:datetime
					obj="${transactionInstance?.dateCreated}" /></td>
		</tr>
		<tr class="prop">
			<td class="name"><label><warehouse:message
						code="default.lastUpdated.label" /></label></td>
			<td class="value"><format:datetime
					obj="${transactionInstance?.lastUpdated}" /></td>
		</tr>
		
		<g:if test="${localTransfer }">
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="localTransfer.destinationTransaction.label" default="Outgoing transaction"/></label>
				</td>
				<td class="value">
					<g:link id="${localTransfer.destinationTransaction?.id}" controller="inventory" action="showTransaction">
						${localTransfer?.destinationTransaction?.transactionNumber?:localTransfer?.destinationTransaction?.transactionType?.name }
					</g:link>
					
				</td>
			</tr>
			<tr class="prop">
				<td class="name">
					<label><warehouse:message code="localTransfer.sourceTransaction.label" default="Incoming transaction"/></label>				
				</td>
				<td class="value">
					<g:link id="${localTransfer.sourceTransaction?.id}" controller="inventory" action="showTransaction">
						${localTransfer?.sourceTransaction?.transactionNumber?:localTransfer?.sourceTransaction?.transactionType?.name }
					</g:link>
				</td>
			</tr>
		</g:if>		
		
	</table>
</div>